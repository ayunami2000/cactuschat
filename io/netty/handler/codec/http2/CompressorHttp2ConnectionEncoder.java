// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http2;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.PromiseAggregator;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.compression.ZlibCodecFactory;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.compression.ZlibWrapper;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.AsciiString;
import io.netty.buffer.Unpooled;
import io.netty.util.concurrent.Promise;
import io.netty.channel.ChannelPromiseAggregator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class CompressorHttp2ConnectionEncoder extends DefaultHttp2ConnectionEncoder
{
    private static final Http2ConnectionAdapter CLEAN_UP_LISTENER;
    private final int compressionLevel;
    private final int windowBits;
    private final int memLevel;
    
    protected CompressorHttp2ConnectionEncoder(final Builder builder) {
        super(builder);
        if (builder.compressionLevel < 0 || builder.compressionLevel > 9) {
            throw new IllegalArgumentException("compressionLevel: " + builder.compressionLevel + " (expected: 0-9)");
        }
        if (builder.windowBits < 9 || builder.windowBits > 15) {
            throw new IllegalArgumentException("windowBits: " + builder.windowBits + " (expected: 9-15)");
        }
        if (builder.memLevel < 1 || builder.memLevel > 9) {
            throw new IllegalArgumentException("memLevel: " + builder.memLevel + " (expected: 1-9)");
        }
        this.compressionLevel = builder.compressionLevel;
        this.windowBits = builder.windowBits;
        this.memLevel = builder.memLevel;
        this.connection().addListener(CompressorHttp2ConnectionEncoder.CLEAN_UP_LISTENER);
    }
    
    @Override
    public ChannelFuture writeData(final ChannelHandlerContext ctx, final int streamId, final ByteBuf data, int padding, final boolean endOfStream, final ChannelPromise promise) {
        final Http2Stream stream = this.connection().stream(streamId);
        final EmbeddedChannel channel = (stream == null) ? null : stream.getProperty(CompressorHttp2ConnectionEncoder.class);
        if (channel == null) {
            return super.writeData(ctx, streamId, data, padding, endOfStream, promise);
        }
        try {
            channel.writeOutbound(data);
            ByteBuf buf = nextReadableBuf(channel);
            if (buf != null) {
                final ChannelPromiseAggregator aggregator = new ChannelPromiseAggregator(promise);
                ChannelPromise bufPromise = ctx.newPromise();
                ((PromiseAggregator<Void, Future>)aggregator).add(bufPromise);
                while (true) {
                    ByteBuf nextBuf = nextReadableBuf(channel);
                    boolean compressedEndOfStream = nextBuf == null && endOfStream;
                    if (compressedEndOfStream && channel.finish()) {
                        nextBuf = nextReadableBuf(channel);
                        compressedEndOfStream = (nextBuf == null);
                    }
                    ChannelPromise nextPromise;
                    if (nextBuf != null) {
                        nextPromise = ctx.newPromise();
                        ((PromiseAggregator<Void, Future>)aggregator).add(nextPromise);
                    }
                    else {
                        nextPromise = null;
                    }
                    super.writeData(ctx, streamId, buf, padding, compressedEndOfStream, bufPromise);
                    if (nextBuf == null) {
                        break;
                    }
                    padding = 0;
                    buf = nextBuf;
                    bufPromise = nextPromise;
                }
                return promise;
            }
            if (endOfStream) {
                if (channel.finish()) {
                    buf = nextReadableBuf(channel);
                }
                return super.writeData(ctx, streamId, (buf == null) ? Unpooled.EMPTY_BUFFER : buf, padding, true, promise);
            }
            promise.setSuccess();
            return promise;
        }
        finally {
            if (endOfStream) {
                cleanup(stream, channel);
            }
        }
    }
    
    @Override
    public ChannelFuture writeHeaders(final ChannelHandlerContext ctx, final int streamId, final Http2Headers headers, final int padding, final boolean endStream, final ChannelPromise promise) {
        this.initCompressor(streamId, headers, endStream);
        return super.writeHeaders(ctx, streamId, headers, padding, endStream, promise);
    }
    
    @Override
    public ChannelFuture writeHeaders(final ChannelHandlerContext ctx, final int streamId, final Http2Headers headers, final int streamDependency, final short weight, final boolean exclusive, final int padding, final boolean endOfStream, final ChannelPromise promise) {
        this.initCompressor(streamId, headers, endOfStream);
        return super.writeHeaders(ctx, streamId, headers, streamDependency, weight, exclusive, padding, endOfStream, promise);
    }
    
    protected EmbeddedChannel newContentCompressor(final AsciiString contentEncoding) throws Http2Exception {
        if (HttpHeaderValues.GZIP.equalsIgnoreCase(contentEncoding) || HttpHeaderValues.X_GZIP.equalsIgnoreCase(contentEncoding)) {
            return this.newCompressionChannel(ZlibWrapper.GZIP);
        }
        if (HttpHeaderValues.DEFLATE.equalsIgnoreCase(contentEncoding) || HttpHeaderValues.X_DEFLATE.equalsIgnoreCase(contentEncoding)) {
            return this.newCompressionChannel(ZlibWrapper.ZLIB);
        }
        return null;
    }
    
    protected AsciiString getTargetContentEncoding(final AsciiString contentEncoding) throws Http2Exception {
        return contentEncoding;
    }
    
    private EmbeddedChannel newCompressionChannel(final ZlibWrapper wrapper) {
        return new EmbeddedChannel(new ChannelHandler[] { ZlibCodecFactory.newZlibEncoder(wrapper, this.compressionLevel, this.windowBits, this.memLevel) });
    }
    
    private void initCompressor(final int streamId, final Http2Headers headers, final boolean endOfStream) {
        final Http2Stream stream = this.connection().stream(streamId);
        if (stream == null) {
            return;
        }
        EmbeddedChannel compressor = stream.getProperty(CompressorHttp2ConnectionEncoder.class);
        if (compressor == null) {
            if (!endOfStream) {
                AsciiString encoding = headers.get(HttpHeaderNames.CONTENT_ENCODING);
                if (encoding == null) {
                    encoding = HttpHeaderValues.IDENTITY;
                }
                try {
                    compressor = this.newContentCompressor(encoding);
                    if (compressor != null) {
                        stream.setProperty(CompressorHttp2ConnectionEncoder.class, compressor);
                        final AsciiString targetContentEncoding = this.getTargetContentEncoding(encoding);
                        if (HttpHeaderValues.IDENTITY.equalsIgnoreCase(targetContentEncoding)) {
                            headers.remove(HttpHeaderNames.CONTENT_ENCODING);
                        }
                        else {
                            headers.set(HttpHeaderNames.CONTENT_ENCODING, targetContentEncoding);
                        }
                    }
                }
                catch (Throwable t) {}
            }
        }
        else if (endOfStream) {
            cleanup(stream, compressor);
        }
        if (compressor != null) {
            headers.remove(HttpHeaderNames.CONTENT_LENGTH);
        }
    }
    
    private static void cleanup(final Http2Stream stream, final EmbeddedChannel compressor) {
        if (compressor.finish()) {
            while (true) {
                final ByteBuf buf = compressor.readOutbound();
                if (buf == null) {
                    break;
                }
                buf.release();
            }
        }
        stream.removeProperty(CompressorHttp2ConnectionEncoder.class);
    }
    
    private static ByteBuf nextReadableBuf(final EmbeddedChannel compressor) {
        while (true) {
            final ByteBuf buf = compressor.readOutbound();
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
                final EmbeddedChannel compressor = stream.getProperty(CompressorHttp2ConnectionEncoder.class);
                if (compressor != null) {
                    cleanup(stream, compressor);
                }
            }
        };
    }
    
    public static class Builder extends DefaultHttp2ConnectionEncoder.Builder
    {
        protected int compressionLevel;
        protected int windowBits;
        protected int memLevel;
        
        public Builder() {
            this.compressionLevel = 6;
            this.windowBits = 15;
            this.memLevel = 8;
        }
        
        public Builder compressionLevel(final int compressionLevel) {
            this.compressionLevel = compressionLevel;
            return this;
        }
        
        public Builder windowBits(final int windowBits) {
            this.windowBits = windowBits;
            return this;
        }
        
        public Builder memLevel(final int memLevel) {
            this.memLevel = memLevel;
            return this;
        }
        
        @Override
        public CompressorHttp2ConnectionEncoder build() {
            return new CompressorHttp2ConnectionEncoder(this);
        }
    }
}
