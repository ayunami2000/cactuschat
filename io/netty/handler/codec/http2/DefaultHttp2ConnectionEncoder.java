// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http2;

import io.netty.channel.ChannelFutureListener;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.ObjectUtil;
import java.util.ArrayDeque;

public class DefaultHttp2ConnectionEncoder implements Http2ConnectionEncoder
{
    private final Http2FrameWriter frameWriter;
    private final Http2Connection connection;
    private final Http2LifecycleManager lifecycleManager;
    private final ArrayDeque<Http2Settings> outstandingLocalSettingsQueue;
    
    public static Builder newBuilder() {
        return new Builder();
    }
    
    protected DefaultHttp2ConnectionEncoder(final Builder builder) {
        this.outstandingLocalSettingsQueue = new ArrayDeque<Http2Settings>(4);
        this.connection = ObjectUtil.checkNotNull(builder.connection, "connection");
        this.frameWriter = ObjectUtil.checkNotNull(builder.frameWriter, "frameWriter");
        this.lifecycleManager = ObjectUtil.checkNotNull(builder.lifecycleManager, "lifecycleManager");
        if (this.connection.remote().flowController() == null) {
            this.connection.remote().flowController(new DefaultHttp2RemoteFlowController(this.connection));
        }
    }
    
    @Override
    public Http2FrameWriter frameWriter() {
        return this.frameWriter;
    }
    
    @Override
    public Http2Connection connection() {
        return this.connection;
    }
    
    @Override
    public final Http2RemoteFlowController flowController() {
        return this.connection().remote().flowController();
    }
    
    @Override
    public void remoteSettings(final Http2Settings settings) throws Http2Exception {
        final Boolean pushEnabled = settings.pushEnabled();
        final Http2FrameWriter.Configuration config = this.configuration();
        final Http2HeaderTable outboundHeaderTable = config.headerTable();
        final Http2FrameSizePolicy outboundFrameSizePolicy = config.frameSizePolicy();
        if (pushEnabled != null) {
            if (!this.connection.isServer()) {
                throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Client received SETTINGS frame with ENABLE_PUSH specified", new Object[0]);
            }
            this.connection.remote().allowPushTo(pushEnabled);
        }
        final Long maxConcurrentStreams = settings.maxConcurrentStreams();
        if (maxConcurrentStreams != null) {
            this.connection.local().maxStreams((int)Math.min(maxConcurrentStreams, 2147483647L));
        }
        final Long headerTableSize = settings.headerTableSize();
        if (headerTableSize != null) {
            outboundHeaderTable.maxHeaderTableSize((int)Math.min(headerTableSize, 2147483647L));
        }
        final Integer maxHeaderListSize = settings.maxHeaderListSize();
        if (maxHeaderListSize != null) {
            outboundHeaderTable.maxHeaderListSize(maxHeaderListSize);
        }
        final Integer maxFrameSize = settings.maxFrameSize();
        if (maxFrameSize != null) {
            outboundFrameSizePolicy.maxFrameSize(maxFrameSize);
        }
        final Integer initialWindowSize = settings.initialWindowSize();
        if (initialWindowSize != null) {
            this.flowController().initialWindowSize(initialWindowSize);
        }
    }
    
    @Override
    public ChannelFuture writeData(final ChannelHandlerContext ctx, final int streamId, final ByteBuf data, final int padding, final boolean endOfStream, final ChannelPromise promise) {
        Http2Stream stream;
        try {
            if (this.connection.isGoAway()) {
                throw new IllegalStateException("Sending data after connection going away.");
            }
            stream = this.connection.requireStream(streamId);
            switch (stream.state()) {
                case OPEN:
                case HALF_CLOSED_REMOTE: {
                    if (endOfStream) {
                        this.lifecycleManager.closeLocalSide(stream, promise);
                    }
                    break;
                }
                default: {
                    throw new IllegalStateException(String.format("Stream %d in unexpected state: %s", stream.id(), stream.state()));
                }
            }
        }
        catch (Throwable e) {
            data.release();
            return promise.setFailure(e);
        }
        this.flowController().sendFlowControlled(ctx, stream, new FlowControlledData(ctx, stream, data, padding, endOfStream, promise));
        return promise;
    }
    
    @Override
    public ChannelFuture writeHeaders(final ChannelHandlerContext ctx, final int streamId, final Http2Headers headers, final int padding, final boolean endStream, final ChannelPromise promise) {
        return this.writeHeaders(ctx, streamId, headers, 0, (short)16, false, padding, endStream, promise);
    }
    
    @Override
    public ChannelFuture writeHeaders(final ChannelHandlerContext ctx, final int streamId, final Http2Headers headers, final int streamDependency, final short weight, final boolean exclusive, final int padding, final boolean endOfStream, final ChannelPromise promise) {
        try {
            if (this.connection.isGoAway()) {
                throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Sending headers after connection going away.", new Object[0]);
            }
            Http2Stream stream = this.connection.stream(streamId);
            if (stream == null) {
                stream = this.connection.createLocalStream(streamId);
            }
            switch (stream.state()) {
                case RESERVED_LOCAL:
                case IDLE: {
                    stream.open(endOfStream);
                    break;
                }
                case OPEN:
                case HALF_CLOSED_REMOTE: {
                    break;
                }
                default: {
                    throw new IllegalStateException(String.format("Stream %d in unexpected state: %s", stream.id(), stream.state()));
                }
            }
            this.flowController().sendFlowControlled(ctx, stream, new FlowControlledHeaders(ctx, stream, headers, streamDependency, weight, exclusive, padding, endOfStream, promise));
            if (endOfStream) {
                this.lifecycleManager.closeLocalSide(stream, promise);
            }
            return promise;
        }
        catch (Http2NoMoreStreamIdsException e) {
            this.lifecycleManager.onException(ctx, e);
            return promise.setFailure((Throwable)e);
        }
        catch (Throwable e2) {
            return promise.setFailure(e2);
        }
    }
    
    @Override
    public ChannelFuture writePriority(final ChannelHandlerContext ctx, final int streamId, final int streamDependency, final short weight, final boolean exclusive, final ChannelPromise promise) {
        try {
            if (this.connection.isGoAway()) {
                throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Sending priority after connection going away.", new Object[0]);
            }
            Http2Stream stream = this.connection.stream(streamId);
            if (stream == null) {
                stream = this.connection.createLocalStream(streamId);
            }
            stream.setPriority(streamDependency, weight, exclusive);
        }
        catch (Throwable e) {
            return promise.setFailure(e);
        }
        final ChannelFuture future = this.frameWriter.writePriority(ctx, streamId, streamDependency, weight, exclusive, promise);
        ctx.flush();
        return future;
    }
    
    @Override
    public ChannelFuture writeRstStream(final ChannelHandlerContext ctx, final int streamId, final long errorCode, final ChannelPromise promise) {
        return this.lifecycleManager.writeRstStream(ctx, streamId, errorCode, promise);
    }
    
    public ChannelFuture writeRstStream(final ChannelHandlerContext ctx, final int streamId, final long errorCode, final ChannelPromise promise, final boolean writeIfNoStream) {
        final Http2Stream stream = this.connection.stream(streamId);
        if (stream == null && !writeIfNoStream) {
            promise.setSuccess();
            return promise;
        }
        final ChannelFuture future = this.frameWriter.writeRstStream(ctx, streamId, errorCode, promise);
        ctx.flush();
        if (stream != null) {
            stream.resetSent();
            this.lifecycleManager.closeStream(stream, promise);
        }
        return future;
    }
    
    @Override
    public ChannelFuture writeSettings(final ChannelHandlerContext ctx, final Http2Settings settings, final ChannelPromise promise) {
        this.outstandingLocalSettingsQueue.add(settings);
        try {
            if (this.connection.isGoAway()) {
                throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Sending settings after connection going away.", new Object[0]);
            }
            final Boolean pushEnabled = settings.pushEnabled();
            if (pushEnabled != null && this.connection.isServer()) {
                throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Server sending SETTINGS frame with ENABLE_PUSH specified", new Object[0]);
            }
        }
        catch (Throwable e) {
            return promise.setFailure(e);
        }
        final ChannelFuture future = this.frameWriter.writeSettings(ctx, settings, promise);
        ctx.flush();
        return future;
    }
    
    @Override
    public ChannelFuture writeSettingsAck(final ChannelHandlerContext ctx, final ChannelPromise promise) {
        final ChannelFuture future = this.frameWriter.writeSettingsAck(ctx, promise);
        ctx.flush();
        return future;
    }
    
    @Override
    public ChannelFuture writePing(final ChannelHandlerContext ctx, final boolean ack, final ByteBuf data, final ChannelPromise promise) {
        if (this.connection.isGoAway()) {
            data.release();
            return promise.setFailure((Throwable)Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Sending ping after connection going away.", new Object[0]));
        }
        final ChannelFuture future = this.frameWriter.writePing(ctx, ack, data, promise);
        ctx.flush();
        return future;
    }
    
    @Override
    public ChannelFuture writePushPromise(final ChannelHandlerContext ctx, final int streamId, final int promisedStreamId, final Http2Headers headers, final int padding, final ChannelPromise promise) {
        try {
            if (this.connection.isGoAway()) {
                throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Sending push promise after connection going away.", new Object[0]);
            }
            final Http2Stream stream = this.connection.requireStream(streamId);
            this.connection.local().reservePushStream(promisedStreamId, stream);
        }
        catch (Throwable e) {
            return promise.setFailure(e);
        }
        final ChannelFuture future = this.frameWriter.writePushPromise(ctx, streamId, promisedStreamId, headers, padding, promise);
        ctx.flush();
        return future;
    }
    
    @Override
    public ChannelFuture writeGoAway(final ChannelHandlerContext ctx, final int lastStreamId, final long errorCode, final ByteBuf debugData, final ChannelPromise promise) {
        return this.lifecycleManager.writeGoAway(ctx, lastStreamId, errorCode, debugData, promise);
    }
    
    @Override
    public ChannelFuture writeWindowUpdate(final ChannelHandlerContext ctx, final int streamId, final int windowSizeIncrement, final ChannelPromise promise) {
        return promise.setFailure((Throwable)new UnsupportedOperationException("Use the Http2[Inbound|Outbound]FlowController objects to control window sizes"));
    }
    
    @Override
    public ChannelFuture writeFrame(final ChannelHandlerContext ctx, final byte frameType, final int streamId, final Http2Flags flags, final ByteBuf payload, final ChannelPromise promise) {
        return this.frameWriter.writeFrame(ctx, frameType, streamId, flags, payload, promise);
    }
    
    @Override
    public void close() {
        this.frameWriter.close();
    }
    
    @Override
    public Http2Settings pollSentSettings() {
        return this.outstandingLocalSettingsQueue.poll();
    }
    
    @Override
    public Http2FrameWriter.Configuration configuration() {
        return this.frameWriter.configuration();
    }
    
    public static class Builder implements Http2ConnectionEncoder.Builder
    {
        protected Http2FrameWriter frameWriter;
        protected Http2Connection connection;
        protected Http2LifecycleManager lifecycleManager;
        
        @Override
        public Builder connection(final Http2Connection connection) {
            this.connection = connection;
            return this;
        }
        
        @Override
        public Builder lifecycleManager(final Http2LifecycleManager lifecycleManager) {
            this.lifecycleManager = lifecycleManager;
            return this;
        }
        
        @Override
        public Http2LifecycleManager lifecycleManager() {
            return this.lifecycleManager;
        }
        
        @Override
        public Builder frameWriter(final Http2FrameWriter frameWriter) {
            this.frameWriter = frameWriter;
            return this;
        }
        
        @Override
        public Http2ConnectionEncoder build() {
            return new DefaultHttp2ConnectionEncoder(this);
        }
    }
    
    private final class FlowControlledData extends FlowControlledBase
    {
        private ByteBuf data;
        private int size;
        
        private FlowControlledData(final ChannelHandlerContext ctx, final Http2Stream stream, final ByteBuf data, final int padding, final boolean endOfStream, final ChannelPromise promise) {
            super(ctx, stream, padding, endOfStream, promise);
            this.data = data;
            this.size = data.readableBytes() + padding;
        }
        
        @Override
        public int size() {
            return this.size;
        }
        
        @Override
        public void error(final Throwable cause) {
            ReferenceCountUtil.safeRelease(this.data);
            DefaultHttp2ConnectionEncoder.this.lifecycleManager.onException(this.ctx, cause);
            this.data = null;
            this.size = 0;
            this.promise.tryFailure(cause);
        }
        
        @Override
        public boolean write(final int allowedBytes) {
            if (this.data == null) {
                return false;
            }
            if (allowedBytes == 0 && this.size() != 0) {
                return false;
            }
            final int maxFrameSize = DefaultHttp2ConnectionEncoder.this.frameWriter().configuration().frameSizePolicy().maxFrameSize();
            try {
                int bytesWritten = 0;
                do {
                    final int allowedFrameSize = Math.min(maxFrameSize, allowedBytes - bytesWritten);
                    int writeableData = this.data.readableBytes();
                    ByteBuf toWrite;
                    if (writeableData > allowedFrameSize) {
                        writeableData = allowedFrameSize;
                        toWrite = this.data.readSlice(writeableData).retain();
                    }
                    else {
                        toWrite = this.data;
                        this.data = Unpooled.EMPTY_BUFFER;
                    }
                    final int writeablePadding = Math.min(allowedFrameSize - writeableData, this.padding);
                    this.padding -= writeablePadding;
                    bytesWritten += writeableData + writeablePadding;
                    ChannelPromise writePromise;
                    if (this.size == bytesWritten) {
                        writePromise = this.promise;
                    }
                    else {
                        writePromise = this.ctx.newPromise();
                        writePromise.addListener((GenericFutureListener<? extends Future<? super Void>>)this);
                    }
                    DefaultHttp2ConnectionEncoder.this.frameWriter().writeData(this.ctx, this.stream.id(), toWrite, writeablePadding, this.size == bytesWritten && this.endOfStream, writePromise);
                } while (this.size != bytesWritten && allowedBytes > bytesWritten);
                this.size -= bytesWritten;
                return true;
            }
            catch (Throwable e) {
                this.error(e);
                return false;
            }
        }
    }
    
    private final class FlowControlledHeaders extends FlowControlledBase
    {
        private final Http2Headers headers;
        private final int streamDependency;
        private final short weight;
        private final boolean exclusive;
        
        private FlowControlledHeaders(final ChannelHandlerContext ctx, final Http2Stream stream, final Http2Headers headers, final int streamDependency, final short weight, final boolean exclusive, final int padding, final boolean endOfStream, final ChannelPromise promise) {
            super(ctx, stream, padding, endOfStream, promise);
            this.headers = headers;
            this.streamDependency = streamDependency;
            this.weight = weight;
            this.exclusive = exclusive;
        }
        
        @Override
        public int size() {
            return 0;
        }
        
        @Override
        public void error(final Throwable cause) {
            DefaultHttp2ConnectionEncoder.this.lifecycleManager.onException(this.ctx, cause);
            this.promise.tryFailure(cause);
        }
        
        @Override
        public boolean write(final int allowedBytes) {
            DefaultHttp2ConnectionEncoder.this.frameWriter().writeHeaders(this.ctx, this.stream.id(), this.headers, this.streamDependency, this.weight, this.exclusive, this.padding, this.endOfStream, this.promise);
            return true;
        }
    }
    
    public abstract class FlowControlledBase implements Http2RemoteFlowController.FlowControlled, ChannelFutureListener
    {
        protected final ChannelHandlerContext ctx;
        protected final Http2Stream stream;
        protected final ChannelPromise promise;
        protected final boolean endOfStream;
        protected int padding;
        
        public FlowControlledBase(final ChannelHandlerContext ctx, final Http2Stream stream, final int padding, final boolean endOfStream, final ChannelPromise promise) {
            this.ctx = ctx;
            if (padding < 0) {
                throw new IllegalArgumentException("padding must be >= 0");
            }
            this.padding = padding;
            this.endOfStream = endOfStream;
            this.stream = stream;
            (this.promise = promise).addListener((GenericFutureListener<? extends Future<? super Void>>)this);
        }
        
        @Override
        public void operationComplete(final ChannelFuture future) throws Exception {
            if (!future.isSuccess()) {
                this.error(future.cause());
            }
        }
    }
}
