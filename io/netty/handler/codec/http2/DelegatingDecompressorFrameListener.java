// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http2;

import io.netty.util.internal.ObjectUtil;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.compression.ZlibCodecFactory;
import io.netty.handler.codec.compression.ZlibWrapper;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.AsciiString;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.buffer.Unpooled;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class DelegatingDecompressorFrameListener extends Http2FrameListenerDecorator
{
    private static final Http2ConnectionAdapter CLEAN_UP_LISTENER;
    private final Http2Connection connection;
    private final boolean strict;
    private boolean flowControllerInitialized;
    
    public DelegatingDecompressorFrameListener(final Http2Connection connection, final Http2FrameListener listener) {
        this(connection, listener, true);
    }
    
    public DelegatingDecompressorFrameListener(final Http2Connection connection, final Http2FrameListener listener, final boolean strict) {
        super(listener);
        this.connection = connection;
        this.strict = strict;
        connection.addListener(DelegatingDecompressorFrameListener.CLEAN_UP_LISTENER);
    }
    
    @Override
    public int onDataRead(final ChannelHandlerContext ctx, final int streamId, final ByteBuf data, int padding, final boolean endOfStream) throws Http2Exception {
        final Http2Stream stream = this.connection.stream(streamId);
        final Http2Decompressor decompressor = decompressor(stream);
        if (decompressor == null) {
            return this.listener.onDataRead(ctx, streamId, data, padding, endOfStream);
        }
        final EmbeddedChannel channel = decompressor.decompressor();
        final int compressedBytes = data.readableBytes() + padding;
        int processedBytes = 0;
        decompressor.incrementCompressedBytes(compressedBytes);
        try {
            channel.writeInbound(data.retain());
            ByteBuf buf = nextReadableBuf(channel);
            if (buf == null && endOfStream && channel.finish()) {
                buf = nextReadableBuf(channel);
            }
            if (buf == null) {
                if (endOfStream) {
                    this.listener.onDataRead(ctx, streamId, Unpooled.EMPTY_BUFFER, padding, true);
                }
                decompressor.incrementDecompressedByes(compressedBytes);
                processedBytes = compressedBytes;
            }
            else {
                try {
                    decompressor.incrementDecompressedByes(padding);
                    while (true) {
                        ByteBuf nextBuf = nextReadableBuf(channel);
                        boolean decompressedEndOfStream = nextBuf == null && endOfStream;
                        if (decompressedEndOfStream && channel.finish()) {
                            nextBuf = nextReadableBuf(channel);
                            decompressedEndOfStream = (nextBuf == null);
                        }
                        decompressor.incrementDecompressedByes(buf.readableBytes());
                        processedBytes += this.listener.onDataRead(ctx, streamId, buf, padding, decompressedEndOfStream);
                        if (nextBuf == null) {
                            break;
                        }
                        padding = 0;
                        buf.release();
                        buf = nextBuf;
                    }
                }
                finally {
                    buf.release();
                }
            }
            decompressor.incrementProcessedBytes(processedBytes);
            return processedBytes;
        }
        catch (Http2Exception e) {
            decompressor.incrementProcessedBytes(compressedBytes);
            throw e;
        }
        catch (Throwable t) {
            decompressor.incrementProcessedBytes(compressedBytes);
            throw Http2Exception.streamError(stream.id(), Http2Error.INTERNAL_ERROR, t, "Decompressor error detected while delegating data read on streamId %d", stream.id());
        }
    }
    
    @Override
    public void onHeadersRead(final ChannelHandlerContext ctx, final int streamId, final Http2Headers headers, final int padding, final boolean endStream) throws Http2Exception {
        this.initDecompressor(streamId, headers, endStream);
        this.listener.onHeadersRead(ctx, streamId, headers, padding, endStream);
    }
    
    @Override
    public void onHeadersRead(final ChannelHandlerContext ctx, final int streamId, final Http2Headers headers, final int streamDependency, final short weight, final boolean exclusive, final int padding, final boolean endStream) throws Http2Exception {
        this.initDecompressor(streamId, headers, endStream);
        this.listener.onHeadersRead(ctx, streamId, headers, streamDependency, weight, exclusive, padding, endStream);
    }
    
    protected EmbeddedChannel newContentDecompressor(final AsciiString contentEncoding) throws Http2Exception {
        if (HttpHeaderValues.GZIP.equalsIgnoreCase(contentEncoding) || HttpHeaderValues.X_GZIP.equalsIgnoreCase(contentEncoding)) {
            return new EmbeddedChannel(new ChannelHandler[] { ZlibCodecFactory.newZlibDecoder(ZlibWrapper.GZIP) });
        }
        if (HttpHeaderValues.DEFLATE.equalsIgnoreCase(contentEncoding) || HttpHeaderValues.X_DEFLATE.equalsIgnoreCase(contentEncoding)) {
            final ZlibWrapper wrapper = this.strict ? ZlibWrapper.ZLIB : ZlibWrapper.ZLIB_OR_NONE;
            return new EmbeddedChannel(new ChannelHandler[] { ZlibCodecFactory.newZlibDecoder(wrapper) });
        }
        return null;
    }
    
    protected AsciiString getTargetContentEncoding(final AsciiString contentEncoding) throws Http2Exception {
        return HttpHeaderValues.IDENTITY;
    }
    
    private void initDecompressor(final int streamId, final Http2Headers headers, final boolean endOfStream) throws Http2Exception {
        final Http2Stream stream = this.connection.stream(streamId);
        if (stream == null) {
            return;
        }
        Http2Decompressor decompressor = decompressor(stream);
        if (decompressor == null && !endOfStream) {
            AsciiString contentEncoding = headers.get(HttpHeaderNames.CONTENT_ENCODING);
            if (contentEncoding == null) {
                contentEncoding = HttpHeaderValues.IDENTITY;
            }
            final EmbeddedChannel channel = this.newContentDecompressor(contentEncoding);
            if (channel != null) {
                decompressor = new Http2Decompressor(channel);
                stream.setProperty(Http2Decompressor.class, decompressor);
                final AsciiString targetContentEncoding = this.getTargetContentEncoding(contentEncoding);
                if (HttpHeaderValues.IDENTITY.equalsIgnoreCase(targetContentEncoding)) {
                    headers.remove(HttpHeaderNames.CONTENT_ENCODING);
                }
                else {
                    headers.set(HttpHeaderNames.CONTENT_ENCODING, targetContentEncoding);
                }
            }
        }
        if (decompressor != null) {
            headers.remove(HttpHeaderNames.CONTENT_LENGTH);
            if (!this.flowControllerInitialized) {
                this.flowControllerInitialized = true;
                this.connection.local().flowController(new ConsumedBytesConverter(this.connection.local().flowController()));
            }
        }
    }
    
    private static Http2Decompressor decompressor(final Http2Stream stream) {
        return (stream == null) ? null : stream.getProperty(Http2Decompressor.class);
    }
    
    private static void cleanup(final Http2Stream stream, Http2Decompressor decompressor) {
        final EmbeddedChannel channel = decompressor.decompressor();
        if (channel.finish()) {
            while (true) {
                final ByteBuf buf = channel.readInbound();
                if (buf == null) {
                    break;
                }
                buf.release();
            }
        }
        decompressor = stream.removeProperty(Http2Decompressor.class);
    }
    
    private static ByteBuf nextReadableBuf(final EmbeddedChannel decompressor) {
        while (true) {
            final ByteBuf buf = decompressor.readInbound();
            if (buf == null) {
                return null;
            }
            if (buf.isReadable()) {
                return buf;
            }
            buf.release();
        }
    }
    
    static {
        CLEAN_UP_LISTENER = new Http2ConnectionAdapter() {
            @Override
            public void streamRemoved(final Http2Stream stream) {
                final Http2Decompressor decompressor = decompressor(stream);
                if (decompressor != null) {
                    cleanup(stream, decompressor);
                }
            }
        };
    }
    
    private static final class ConsumedBytesConverter implements Http2LocalFlowController
    {
        private final Http2LocalFlowController flowController;
        
        ConsumedBytesConverter(final Http2LocalFlowController flowController) {
            this.flowController = ObjectUtil.checkNotNull(flowController, "flowController");
        }
        
        @Override
        public void initialWindowSize(final int newWindowSize) throws Http2Exception {
            this.flowController.initialWindowSize(newWindowSize);
        }
        
        @Override
        public int initialWindowSize() {
            return this.flowController.initialWindowSize();
        }
        
        @Override
        public int windowSize(final Http2Stream stream) {
            return this.flowController.windowSize(stream);
        }
        
        @Override
        public void incrementWindowSize(final ChannelHandlerContext ctx, final Http2Stream stream, final int delta) throws Http2Exception {
            this.flowController.incrementWindowSize(ctx, stream, delta);
        }
        
        @Override
        public void receiveFlowControlledFrame(final ChannelHandlerContext ctx, final Http2Stream stream, final ByteBuf data, final int padding, final boolean endOfStream) throws Http2Exception {
            this.flowController.receiveFlowControlledFrame(ctx, stream, data, padding, endOfStream);
        }
        
        @Override
        public void consumeBytes(final ChannelHandlerContext ctx, final Http2Stream stream, int numBytes) throws Http2Exception {
            final Http2Decompressor decompressor = decompressor(stream);
            Http2Decompressor copy = null;
            try {
                if (decompressor != null) {
                    copy = new Http2Decompressor(decompressor);
                    numBytes = decompressor.consumeProcessedBytes(numBytes);
                }
                this.flowController.consumeBytes(ctx, stream, numBytes);
            }
            catch (Http2Exception e) {
                if (copy != null) {
                    stream.setProperty(Http2Decompressor.class, copy);
                }
                throw e;
            }
            catch (Throwable t) {
                if (copy != null) {
                    stream.setProperty(Http2Decompressor.class, copy);
                }
                throw new Http2Exception(Http2Error.INTERNAL_ERROR, "Error while returning bytes to flow control window", t);
            }
        }
        
        @Override
        public int unconsumedBytes(final Http2Stream stream) {
            return this.flowController.unconsumedBytes(stream);
        }
    }
    
    private static final class Http2Decompressor
    {
        private final EmbeddedChannel decompressor;
        private int processed;
        private int compressed;
        private int decompressed;
        
        Http2Decompressor(final Http2Decompressor rhs) {
            this(rhs.decompressor);
            this.processed = rhs.processed;
            this.compressed = rhs.compressed;
            this.decompressed = rhs.decompressed;
        }
        
        Http2Decompressor(final EmbeddedChannel decompressor) {
            this.decompressor = decompressor;
        }
        
        EmbeddedChannel decompressor() {
            return this.decompressor;
        }
        
        void incrementProcessedBytes(final int delta) {
            if (this.processed + delta < 0) {
                throw new IllegalArgumentException("processed bytes cannot be negative");
            }
            this.processed += delta;
        }
        
        void incrementCompressedBytes(final int delta) {
            if (this.compressed + delta < 0) {
                throw new IllegalArgumentException("compressed bytes cannot be negative");
            }
            this.compressed += delta;
        }
        
        void incrementDecompressedByes(final int delta) {
            if (this.decompressed + delta < 0) {
                throw new IllegalArgumentException("decompressed bytes cannot be negative");
            }
            this.decompressed += delta;
        }
        
        int consumeProcessedBytes(final int processedBytes) {
            this.incrementProcessedBytes(-processedBytes);
            final double consumedRatio = processedBytes / (double)this.decompressed;
            final int consumedCompressed = Math.min(this.compressed, (int)Math.ceil(this.compressed * consumedRatio));
            this.incrementDecompressedByes(-Math.min(this.decompressed, (int)Math.ceil(this.decompressed * consumedRatio)));
            this.incrementCompressedBytes(-consumedCompressed);
            return consumedCompressed;
        }
    }
}
