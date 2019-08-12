// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http2;

import java.util.List;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.ObjectUtil;

public class DefaultHttp2ConnectionDecoder implements Http2ConnectionDecoder
{
    private final Http2FrameListener internalFrameListener;
    private final Http2Connection connection;
    private final Http2LifecycleManager lifecycleManager;
    private final Http2ConnectionEncoder encoder;
    private final Http2FrameReader frameReader;
    private final Http2FrameListener listener;
    private boolean prefaceReceived;
    
    public static Builder newBuilder() {
        return new Builder();
    }
    
    protected DefaultHttp2ConnectionDecoder(final Builder builder) {
        this.internalFrameListener = new FrameReadListener();
        this.connection = ObjectUtil.checkNotNull(builder.connection, "connection");
        this.frameReader = ObjectUtil.checkNotNull(builder.frameReader, "frameReader");
        this.lifecycleManager = ObjectUtil.checkNotNull(builder.lifecycleManager, "lifecycleManager");
        this.encoder = ObjectUtil.checkNotNull(builder.encoder, "encoder");
        this.listener = ObjectUtil.checkNotNull(builder.listener, "listener");
        if (this.connection.local().flowController() == null) {
            this.connection.local().flowController(new DefaultHttp2LocalFlowController(this.connection, this.encoder.frameWriter()));
        }
    }
    
    @Override
    public Http2Connection connection() {
        return this.connection;
    }
    
    @Override
    public final Http2LocalFlowController flowController() {
        return this.connection.local().flowController();
    }
    
    @Override
    public Http2FrameListener listener() {
        return this.listener;
    }
    
    @Override
    public boolean prefaceReceived() {
        return this.prefaceReceived;
    }
    
    @Override
    public void decodeFrame(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) throws Http2Exception {
        this.frameReader.readFrame(ctx, in, this.internalFrameListener);
    }
    
    @Override
    public Http2Settings localSettings() {
        final Http2Settings settings = new Http2Settings();
        final Http2FrameReader.Configuration config = this.frameReader.configuration();
        final Http2HeaderTable headerTable = config.headerTable();
        final Http2FrameSizePolicy frameSizePolicy = config.frameSizePolicy();
        settings.initialWindowSize(this.flowController().initialWindowSize());
        settings.maxConcurrentStreams(this.connection.remote().maxStreams());
        settings.headerTableSize(headerTable.maxHeaderTableSize());
        settings.maxFrameSize(frameSizePolicy.maxFrameSize());
        settings.maxHeaderListSize(headerTable.maxHeaderListSize());
        if (!this.connection.isServer()) {
            settings.pushEnabled(this.connection.local().allowPushTo());
        }
        return settings;
    }
    
    @Override
    public void localSettings(final Http2Settings settings) throws Http2Exception {
        final Boolean pushEnabled = settings.pushEnabled();
        final Http2FrameReader.Configuration config = this.frameReader.configuration();
        final Http2HeaderTable inboundHeaderTable = config.headerTable();
        final Http2FrameSizePolicy inboundFrameSizePolicy = config.frameSizePolicy();
        if (pushEnabled != null) {
            if (this.connection.isServer()) {
                throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Server sending SETTINGS frame with ENABLE_PUSH specified", new Object[0]);
            }
            this.connection.local().allowPushTo(pushEnabled);
        }
        final Long maxConcurrentStreams = settings.maxConcurrentStreams();
        if (maxConcurrentStreams != null) {
            final int value = (int)Math.min(maxConcurrentStreams, 2147483647L);
            this.connection.remote().maxStreams(value);
        }
        final Long headerTableSize = settings.headerTableSize();
        if (headerTableSize != null) {
            inboundHeaderTable.maxHeaderTableSize((int)Math.min(headerTableSize, 2147483647L));
        }
        final Integer maxHeaderListSize = settings.maxHeaderListSize();
        if (maxHeaderListSize != null) {
            inboundHeaderTable.maxHeaderListSize(maxHeaderListSize);
        }
        final Integer maxFrameSize = settings.maxFrameSize();
        if (maxFrameSize != null) {
            inboundFrameSizePolicy.maxFrameSize(maxFrameSize);
        }
        final Integer initialWindowSize = settings.initialWindowSize();
        if (initialWindowSize != null) {
            this.flowController().initialWindowSize(initialWindowSize);
        }
    }
    
    @Override
    public void close() {
        this.frameReader.close();
    }
    
    private int unconsumedBytes(final Http2Stream stream) {
        return this.flowController().unconsumedBytes(stream);
    }
    
    public static class Builder implements Http2ConnectionDecoder.Builder
    {
        private Http2Connection connection;
        private Http2LifecycleManager lifecycleManager;
        private Http2ConnectionEncoder encoder;
        private Http2FrameReader frameReader;
        private Http2FrameListener listener;
        
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
        public Builder frameReader(final Http2FrameReader frameReader) {
            this.frameReader = frameReader;
            return this;
        }
        
        @Override
        public Builder listener(final Http2FrameListener listener) {
            this.listener = listener;
            return this;
        }
        
        @Override
        public Builder encoder(final Http2ConnectionEncoder encoder) {
            this.encoder = encoder;
            return this;
        }
        
        @Override
        public Http2ConnectionDecoder build() {
            return new DefaultHttp2ConnectionDecoder(this);
        }
    }
    
    private final class FrameReadListener implements Http2FrameListener
    {
        @Override
        public int onDataRead(final ChannelHandlerContext ctx, final int streamId, final ByteBuf data, final int padding, final boolean endOfStream) throws Http2Exception {
            this.verifyPrefaceReceived();
            final Http2Stream stream = DefaultHttp2ConnectionDecoder.this.connection.requireStream(streamId);
            this.verifyGoAwayNotReceived();
            final boolean shouldIgnore = this.shouldIgnoreFrame(stream, false);
            Http2Exception error = null;
            switch (stream.state()) {
                case OPEN:
                case HALF_CLOSED_LOCAL: {
                    break;
                }
                case HALF_CLOSED_REMOTE: {
                    error = Http2Exception.streamError(stream.id(), Http2Error.STREAM_CLOSED, "Stream %d in unexpected state: %s", stream.id(), stream.state());
                    break;
                }
                case CLOSED: {
                    if (!shouldIgnore) {
                        error = Http2Exception.streamError(stream.id(), Http2Error.STREAM_CLOSED, "Stream %d in unexpected state: %s", stream.id(), stream.state());
                        break;
                    }
                    break;
                }
                default: {
                    if (!shouldIgnore) {
                        error = Http2Exception.streamError(stream.id(), Http2Error.PROTOCOL_ERROR, "Stream %d in unexpected state: %s", stream.id(), stream.state());
                        break;
                    }
                    break;
                }
            }
            int bytesToReturn = data.readableBytes() + padding;
            int unconsumedBytes = DefaultHttp2ConnectionDecoder.this.unconsumedBytes(stream);
            final Http2LocalFlowController flowController = DefaultHttp2ConnectionDecoder.this.flowController();
            try {
                flowController.receiveFlowControlledFrame(ctx, stream, data, padding, endOfStream);
                unconsumedBytes = DefaultHttp2ConnectionDecoder.this.unconsumedBytes(stream);
                if (shouldIgnore) {
                    return bytesToReturn;
                }
                if (error != null) {
                    throw error;
                }
                bytesToReturn = DefaultHttp2ConnectionDecoder.this.listener.onDataRead(ctx, streamId, data, padding, endOfStream);
                return bytesToReturn;
            }
            catch (Http2Exception e) {
                final int delta = unconsumedBytes - DefaultHttp2ConnectionDecoder.this.unconsumedBytes(stream);
                bytesToReturn -= delta;
                throw e;
            }
            catch (RuntimeException e2) {
                final int delta = unconsumedBytes - DefaultHttp2ConnectionDecoder.this.unconsumedBytes(stream);
                bytesToReturn -= delta;
                throw e2;
            }
            finally {
                if (bytesToReturn > 0) {
                    flowController.consumeBytes(ctx, stream, bytesToReturn);
                }
                if (endOfStream) {
                    DefaultHttp2ConnectionDecoder.this.lifecycleManager.closeRemoteSide(stream, ctx.newSucceededFuture());
                }
            }
        }
        
        private void verifyPrefaceReceived() throws Http2Exception {
            if (!DefaultHttp2ConnectionDecoder.this.prefaceReceived) {
                throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Received non-SETTINGS as first frame.", new Object[0]);
            }
        }
        
        @Override
        public void onHeadersRead(final ChannelHandlerContext ctx, final int streamId, final Http2Headers headers, final int padding, final boolean endOfStream) throws Http2Exception {
            this.onHeadersRead(ctx, streamId, headers, 0, (short)16, false, padding, endOfStream);
        }
        
        @Override
        public void onHeadersRead(final ChannelHandlerContext ctx, final int streamId, final Http2Headers headers, final int streamDependency, final short weight, final boolean exclusive, final int padding, final boolean endOfStream) throws Http2Exception {
            this.verifyPrefaceReceived();
            Http2Stream stream = DefaultHttp2ConnectionDecoder.this.connection.stream(streamId);
            this.verifyGoAwayNotReceived();
            if (this.shouldIgnoreFrame(stream, false)) {
                return;
            }
            if (stream == null) {
                stream = DefaultHttp2ConnectionDecoder.this.connection.createRemoteStream(streamId).open(endOfStream);
            }
            else {
                switch (stream.state()) {
                    case RESERVED_REMOTE:
                    case IDLE: {
                        stream.open(endOfStream);
                        break;
                    }
                    case OPEN:
                    case HALF_CLOSED_LOCAL: {
                        break;
                    }
                    case HALF_CLOSED_REMOTE:
                    case CLOSED: {
                        throw Http2Exception.streamError(stream.id(), Http2Error.STREAM_CLOSED, "Stream %d in unexpected state: %s", stream.id(), stream.state());
                    }
                    default: {
                        throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Stream %d in unexpected state: %s", stream.id(), stream.state());
                    }
                }
            }
            DefaultHttp2ConnectionDecoder.this.listener.onHeadersRead(ctx, streamId, headers, streamDependency, weight, exclusive, padding, endOfStream);
            stream.setPriority(streamDependency, weight, exclusive);
            if (endOfStream) {
                DefaultHttp2ConnectionDecoder.this.lifecycleManager.closeRemoteSide(stream, ctx.newSucceededFuture());
            }
        }
        
        @Override
        public void onPriorityRead(final ChannelHandlerContext ctx, final int streamId, final int streamDependency, final short weight, final boolean exclusive) throws Http2Exception {
            this.verifyPrefaceReceived();
            Http2Stream stream = DefaultHttp2ConnectionDecoder.this.connection.stream(streamId);
            this.verifyGoAwayNotReceived();
            if (this.shouldIgnoreFrame(stream, true)) {
                return;
            }
            if (stream == null) {
                stream = DefaultHttp2ConnectionDecoder.this.connection.createRemoteStream(streamId);
            }
            stream.setPriority(streamDependency, weight, exclusive);
            DefaultHttp2ConnectionDecoder.this.listener.onPriorityRead(ctx, streamId, streamDependency, weight, exclusive);
        }
        
        @Override
        public void onRstStreamRead(final ChannelHandlerContext ctx, final int streamId, final long errorCode) throws Http2Exception {
            this.verifyPrefaceReceived();
            final Http2Stream stream = DefaultHttp2ConnectionDecoder.this.connection.requireStream(streamId);
            if (stream.state() == Http2Stream.State.CLOSED) {
                return;
            }
            DefaultHttp2ConnectionDecoder.this.listener.onRstStreamRead(ctx, streamId, errorCode);
            DefaultHttp2ConnectionDecoder.this.lifecycleManager.closeStream(stream, ctx.newSucceededFuture());
        }
        
        @Override
        public void onSettingsAckRead(final ChannelHandlerContext ctx) throws Http2Exception {
            this.verifyPrefaceReceived();
            final Http2Settings settings = DefaultHttp2ConnectionDecoder.this.encoder.pollSentSettings();
            if (settings != null) {
                this.applyLocalSettings(settings);
            }
            DefaultHttp2ConnectionDecoder.this.listener.onSettingsAckRead(ctx);
        }
        
        private void applyLocalSettings(final Http2Settings settings) throws Http2Exception {
            final Boolean pushEnabled = settings.pushEnabled();
            final Http2FrameReader.Configuration config = DefaultHttp2ConnectionDecoder.this.frameReader.configuration();
            final Http2HeaderTable headerTable = config.headerTable();
            final Http2FrameSizePolicy frameSizePolicy = config.frameSizePolicy();
            if (pushEnabled != null) {
                if (DefaultHttp2ConnectionDecoder.this.connection.isServer()) {
                    throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Server sending SETTINGS frame with ENABLE_PUSH specified", new Object[0]);
                }
                DefaultHttp2ConnectionDecoder.this.connection.local().allowPushTo(pushEnabled);
            }
            final Long maxConcurrentStreams = settings.maxConcurrentStreams();
            if (maxConcurrentStreams != null) {
                final int value = (int)Math.min(maxConcurrentStreams, 2147483647L);
                DefaultHttp2ConnectionDecoder.this.connection.remote().maxStreams(value);
            }
            final Long headerTableSize = settings.headerTableSize();
            if (headerTableSize != null) {
                headerTable.maxHeaderTableSize((int)Math.min(headerTableSize, 2147483647L));
            }
            final Integer maxHeaderListSize = settings.maxHeaderListSize();
            if (maxHeaderListSize != null) {
                headerTable.maxHeaderListSize(maxHeaderListSize);
            }
            final Integer maxFrameSize = settings.maxFrameSize();
            if (maxFrameSize != null) {
                frameSizePolicy.maxFrameSize(maxFrameSize);
            }
            final Integer initialWindowSize = settings.initialWindowSize();
            if (initialWindowSize != null) {
                DefaultHttp2ConnectionDecoder.this.flowController().initialWindowSize(initialWindowSize);
            }
        }
        
        @Override
        public void onSettingsRead(final ChannelHandlerContext ctx, final Http2Settings settings) throws Http2Exception {
            DefaultHttp2ConnectionDecoder.this.encoder.remoteSettings(settings);
            DefaultHttp2ConnectionDecoder.this.encoder.writeSettingsAck(ctx, ctx.newPromise());
            DefaultHttp2ConnectionDecoder.this.prefaceReceived = true;
            DefaultHttp2ConnectionDecoder.this.listener.onSettingsRead(ctx, settings);
        }
        
        @Override
        public void onPingRead(final ChannelHandlerContext ctx, final ByteBuf data) throws Http2Exception {
            this.verifyPrefaceReceived();
            DefaultHttp2ConnectionDecoder.this.encoder.writePing(ctx, true, data.retain(), ctx.newPromise());
            ctx.flush();
            DefaultHttp2ConnectionDecoder.this.listener.onPingRead(ctx, data);
        }
        
        @Override
        public void onPingAckRead(final ChannelHandlerContext ctx, final ByteBuf data) throws Http2Exception {
            this.verifyPrefaceReceived();
            DefaultHttp2ConnectionDecoder.this.listener.onPingAckRead(ctx, data);
        }
        
        @Override
        public void onPushPromiseRead(final ChannelHandlerContext ctx, final int streamId, final int promisedStreamId, final Http2Headers headers, final int padding) throws Http2Exception {
            this.verifyPrefaceReceived();
            final Http2Stream parentStream = DefaultHttp2ConnectionDecoder.this.connection.requireStream(streamId);
            this.verifyGoAwayNotReceived();
            if (this.shouldIgnoreFrame(parentStream, false)) {
                return;
            }
            switch (parentStream.state()) {
                case OPEN:
                case HALF_CLOSED_LOCAL: {
                    DefaultHttp2ConnectionDecoder.this.connection.remote().reservePushStream(promisedStreamId, parentStream);
                    DefaultHttp2ConnectionDecoder.this.listener.onPushPromiseRead(ctx, streamId, promisedStreamId, headers, padding);
                }
                default: {
                    throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Stream %d in unexpected state for receiving push promise: %s", parentStream.id(), parentStream.state());
                }
            }
        }
        
        @Override
        public void onGoAwayRead(final ChannelHandlerContext ctx, final int lastStreamId, final long errorCode, final ByteBuf debugData) throws Http2Exception {
            DefaultHttp2ConnectionDecoder.this.connection.goAwayReceived(lastStreamId);
            DefaultHttp2ConnectionDecoder.this.listener.onGoAwayRead(ctx, lastStreamId, errorCode, debugData);
        }
        
        @Override
        public void onWindowUpdateRead(final ChannelHandlerContext ctx, final int streamId, final int windowSizeIncrement) throws Http2Exception {
            this.verifyPrefaceReceived();
            final Http2Stream stream = DefaultHttp2ConnectionDecoder.this.connection.requireStream(streamId);
            this.verifyGoAwayNotReceived();
            if (stream.state() == Http2Stream.State.CLOSED || this.shouldIgnoreFrame(stream, false)) {
                return;
            }
            DefaultHttp2ConnectionDecoder.this.encoder.flowController().incrementWindowSize(ctx, stream, windowSizeIncrement);
            DefaultHttp2ConnectionDecoder.this.listener.onWindowUpdateRead(ctx, streamId, windowSizeIncrement);
        }
        
        @Override
        public void onUnknownFrame(final ChannelHandlerContext ctx, final byte frameType, final int streamId, final Http2Flags flags, final ByteBuf payload) {
            DefaultHttp2ConnectionDecoder.this.listener.onUnknownFrame(ctx, frameType, streamId, flags, payload);
        }
        
        private boolean shouldIgnoreFrame(final Http2Stream stream, final boolean allowResetSent) {
            return (DefaultHttp2ConnectionDecoder.this.connection.goAwaySent() && (stream == null || DefaultHttp2ConnectionDecoder.this.connection.remote().lastStreamCreated() <= stream.id())) || (stream != null && !allowResetSent && stream.isResetSent());
        }
        
        private void verifyGoAwayNotReceived() throws Http2Exception {
            if (DefaultHttp2ConnectionDecoder.this.connection.goAwayReceived()) {
                throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Received frames after receiving GO_AWAY", new Object[0]);
            }
        }
    }
}
