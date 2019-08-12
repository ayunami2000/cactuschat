// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http2;

import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.ByteBuf;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.InternalLogLevel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.channel.ChannelHandlerAdapter;

public class Http2FrameLogger extends ChannelHandlerAdapter
{
    private final InternalLogger logger;
    private final InternalLogLevel level;
    
    public Http2FrameLogger(final InternalLogLevel level) {
        this(level, InternalLoggerFactory.getInstance(Http2FrameLogger.class));
    }
    
    public Http2FrameLogger(final InternalLogLevel level, final InternalLogger logger) {
        this.level = ObjectUtil.checkNotNull(level, "level");
        this.logger = ObjectUtil.checkNotNull(logger, "logger");
    }
    
    public void logData(final Direction direction, final int streamId, final ByteBuf data, final int padding, final boolean endStream) {
        this.log(direction, "DATA: streamId=%d, padding=%d, endStream=%b, length=%d, bytes=%s", streamId, padding, endStream, data.readableBytes(), ByteBufUtil.hexDump(data));
    }
    
    public void logHeaders(final Direction direction, final int streamId, final Http2Headers headers, final int padding, final boolean endStream) {
        this.log(direction, "HEADERS: streamId:%d, headers=%s, padding=%d, endStream=%b", streamId, headers, padding, endStream);
    }
    
    public void logHeaders(final Direction direction, final int streamId, final Http2Headers headers, final int streamDependency, final short weight, final boolean exclusive, final int padding, final boolean endStream) {
        this.log(direction, "HEADERS: streamId:%d, headers=%s, streamDependency=%d, weight=%d, exclusive=%b, padding=%d, endStream=%b", streamId, headers, streamDependency, weight, exclusive, padding, endStream);
    }
    
    public void logPriority(final Direction direction, final int streamId, final int streamDependency, final short weight, final boolean exclusive) {
        this.log(direction, "PRIORITY: streamId=%d, streamDependency=%d, weight=%d, exclusive=%b", streamId, streamDependency, weight, exclusive);
    }
    
    public void logRstStream(final Direction direction, final int streamId, final long errorCode) {
        this.log(direction, "RST_STREAM: streamId=%d, errorCode=%d", streamId, errorCode);
    }
    
    public void logSettingsAck(final Direction direction) {
        this.log(direction, "SETTINGS ack=true", new Object[0]);
    }
    
    public void logSettings(final Direction direction, final Http2Settings settings) {
        this.log(direction, "SETTINGS: ack=false, settings=%s", settings);
    }
    
    public void logPing(final Direction direction, final ByteBuf data) {
        this.log(direction, "PING: ack=false, length=%d, bytes=%s", data.readableBytes(), ByteBufUtil.hexDump(data));
    }
    
    public void logPingAck(final Direction direction, final ByteBuf data) {
        this.log(direction, "PING: ack=true, length=%d, bytes=%s", data.readableBytes(), ByteBufUtil.hexDump(data));
    }
    
    public void logPushPromise(final Direction direction, final int streamId, final int promisedStreamId, final Http2Headers headers, final int padding) {
        this.log(direction, "PUSH_PROMISE: streamId=%d, promisedStreamId=%d, headers=%s, padding=%d", streamId, promisedStreamId, headers, padding);
    }
    
    public void logGoAway(final Direction direction, final int lastStreamId, final long errorCode, final ByteBuf debugData) {
        this.log(direction, "GO_AWAY: lastStreamId=%d, errorCode=%d, length=%d, bytes=%s", lastStreamId, errorCode, debugData.readableBytes(), ByteBufUtil.hexDump(debugData));
    }
    
    public void logWindowsUpdate(final Direction direction, final int streamId, final int windowSizeIncrement) {
        this.log(direction, "WINDOW_UPDATE: streamId=%d, windowSizeIncrement=%d", streamId, windowSizeIncrement);
    }
    
    public void logUnknownFrame(final Direction direction, final byte frameType, final int streamId, final Http2Flags flags, final ByteBuf data) {
        this.log(direction, "UNKNOWN: frameType=%d, streamId=%d, flags=%d, length=%d, bytes=%s", frameType & 0xFF, streamId, flags.value(), data.readableBytes(), ByteBufUtil.hexDump(data));
    }
    
    private void log(final Direction direction, final String format, final Object... args) {
        if (this.logger.isEnabled(this.level)) {
            final StringBuilder b = new StringBuilder(200);
            b.append("\n----------------").append(direction.name()).append("--------------------\n").append(String.format(format, args)).append("\n------------------------------------");
            this.logger.log(this.level, b.toString());
        }
    }
    
    public enum Direction
    {
        INBOUND, 
        OUTBOUND;
    }
}
