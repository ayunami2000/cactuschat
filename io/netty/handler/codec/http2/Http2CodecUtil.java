// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http2;

import io.netty.util.concurrent.Promise;
import io.netty.util.concurrent.EventExecutor;
import io.netty.channel.Channel;
import io.netty.channel.DefaultChannelPromise;
import io.netty.channel.ChannelPromise;
import io.netty.util.CharsetUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.ObjectUtil;
import io.netty.buffer.Unpooled;
import io.netty.buffer.ByteBuf;

public final class Http2CodecUtil
{
    private static final byte[] CONNECTION_PREFACE;
    private static final byte[] EMPTY_PING;
    public static final int CONNECTION_STREAM_ID = 0;
    public static final int HTTP_UPGRADE_STREAM_ID = 1;
    public static final String HTTP_UPGRADE_SETTINGS_HEADER = "HTTP2-Settings";
    public static final String HTTP_UPGRADE_PROTOCOL_NAME = "h2c-16";
    public static final String TLS_UPGRADE_PROTOCOL_NAME = "h2-16";
    public static final int PING_FRAME_PAYLOAD_LENGTH = 8;
    public static final short MAX_UNSIGNED_BYTE = 255;
    public static final int MAX_UNSIGNED_SHORT = 65535;
    public static final long MAX_UNSIGNED_INT = 4294967295L;
    public static final int FRAME_HEADER_LENGTH = 9;
    public static final int SETTING_ENTRY_LENGTH = 6;
    public static final int PRIORITY_ENTRY_LENGTH = 5;
    public static final int INT_FIELD_LENGTH = 4;
    public static final short MAX_WEIGHT = 256;
    public static final short MIN_WEIGHT = 1;
    private static final int MAX_PADDING_LENGTH_LENGTH = 1;
    public static final int DATA_FRAME_HEADER_LENGTH = 10;
    public static final int HEADERS_FRAME_HEADER_LENGTH = 15;
    public static final int PRIORITY_FRAME_LENGTH = 14;
    public static final int RST_STREAM_FRAME_LENGTH = 13;
    public static final int PUSH_PROMISE_FRAME_HEADER_LENGTH = 14;
    public static final int GO_AWAY_FRAME_HEADER_LENGTH = 17;
    public static final int WINDOW_UPDATE_FRAME_LENGTH = 13;
    public static final int CONTINUATION_FRAME_HEADER_LENGTH = 10;
    public static final int SETTINGS_HEADER_TABLE_SIZE = 1;
    public static final int SETTINGS_ENABLE_PUSH = 2;
    public static final int SETTINGS_MAX_CONCURRENT_STREAMS = 3;
    public static final int SETTINGS_INITIAL_WINDOW_SIZE = 4;
    public static final int SETTINGS_MAX_FRAME_SIZE = 5;
    public static final int SETTINGS_MAX_HEADER_LIST_SIZE = 6;
    public static final int MAX_HEADER_TABLE_SIZE = Integer.MAX_VALUE;
    public static final long MAX_CONCURRENT_STREAMS = 4294967295L;
    public static final int MAX_INITIAL_WINDOW_SIZE = Integer.MAX_VALUE;
    public static final int MAX_FRAME_SIZE_LOWER_BOUND = 16384;
    public static final int MAX_FRAME_SIZE_UPPER_BOUND = 16777215;
    public static final long MAX_HEADER_LIST_SIZE = Long.MAX_VALUE;
    public static final long MIN_HEADER_TABLE_SIZE = 0L;
    public static final long MIN_CONCURRENT_STREAMS = 0L;
    public static final int MIN_INITIAL_WINDOW_SIZE = 0;
    public static final long MIN_HEADER_LIST_SIZE = 0L;
    public static final int DEFAULT_WINDOW_SIZE = 65535;
    public static final boolean DEFAULT_ENABLE_PUSH = true;
    public static final short DEFAULT_PRIORITY_WEIGHT = 16;
    public static final int DEFAULT_HEADER_TABLE_SIZE = 4096;
    public static final int DEFAULT_MAX_HEADER_SIZE = 8192;
    public static final int DEFAULT_MAX_FRAME_SIZE = 16384;
    
    public static boolean isMaxFrameSizeValid(final int maxFrameSize) {
        return maxFrameSize >= 16384 && maxFrameSize <= 16777215;
    }
    
    public static ByteBuf connectionPrefaceBuf() {
        return Unpooled.wrappedBuffer(Http2CodecUtil.CONNECTION_PREFACE);
    }
    
    public static ByteBuf emptyPingBuf() {
        return Unpooled.wrappedBuffer(Http2CodecUtil.EMPTY_PING);
    }
    
    public static Http2StreamRemovalPolicy immediateRemovalPolicy() {
        return new Http2StreamRemovalPolicy() {
            private Action action;
            
            @Override
            public void setAction(final Action action) {
                this.action = ObjectUtil.checkNotNull(action, "action");
            }
            
            @Override
            public void markForRemoval(final Http2Stream stream) {
                if (this.action == null) {
                    throw new IllegalStateException("Action must be called before removing streams.");
                }
                this.action.removeStream(stream);
            }
        };
    }
    
    public static Http2Exception getEmbeddedHttp2Exception(Throwable cause) {
        while (cause != null) {
            if (cause instanceof Http2Exception) {
                return (Http2Exception)cause;
            }
            cause = cause.getCause();
        }
        return null;
    }
    
    public static ByteBuf toByteBuf(final ChannelHandlerContext ctx, final Throwable cause) {
        if (cause == null || cause.getMessage() == null) {
            return Unpooled.EMPTY_BUFFER;
        }
        final byte[] msg = cause.getMessage().getBytes(CharsetUtil.UTF_8);
        final ByteBuf debugData = ctx.alloc().buffer(msg.length);
        debugData.writeBytes(msg);
        return debugData;
    }
    
    public static int readUnsignedInt(final ByteBuf buf) {
        return (buf.readByte() & 0x7F) << 24 | (buf.readByte() & 0xFF) << 16 | (buf.readByte() & 0xFF) << 8 | (buf.readByte() & 0xFF);
    }
    
    public static void writeUnsignedInt(final long value, final ByteBuf out) {
        out.writeByte((int)(value >> 24 & 0xFFL));
        out.writeByte((int)(value >> 16 & 0xFFL));
        out.writeByte((int)(value >> 8 & 0xFFL));
        out.writeByte((int)(value & 0xFFL));
    }
    
    public static void writeUnsignedShort(final int value, final ByteBuf out) {
        out.writeByte(value >> 8 & 0xFF);
        out.writeByte(value & 0xFF);
    }
    
    public static void writeFrameHeader(final ByteBuf out, final int payloadLength, final byte type, final Http2Flags flags, final int streamId) {
        out.ensureWritable(9 + payloadLength);
        writeFrameHeaderInternal(out, payloadLength, type, flags, streamId);
    }
    
    static void writeFrameHeaderInternal(final ByteBuf out, final int payloadLength, final byte type, final Http2Flags flags, final int streamId) {
        out.writeMedium(payloadLength);
        out.writeByte(type);
        out.writeByte(flags.value());
        out.writeInt(streamId);
    }
    
    public static <T extends Throwable> T failAndThrow(final ChannelPromise promise, final T cause) throws T, Throwable {
        if (!promise.isDone()) {
            promise.setFailure((Throwable)cause);
        }
        throw cause;
    }
    
    private Http2CodecUtil() {
    }
    
    static {
        CONNECTION_PREFACE = "PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n".getBytes(CharsetUtil.UTF_8);
        EMPTY_PING = new byte[8];
    }
    
    static class SimpleChannelPromiseAggregator extends DefaultChannelPromise
    {
        private final ChannelPromise promise;
        private int expectedCount;
        private int successfulCount;
        private int failureCount;
        private boolean doneAllocating;
        
        SimpleChannelPromiseAggregator(final ChannelPromise promise, final Channel c, final EventExecutor e) {
            super(c, e);
            assert promise != null;
            this.promise = promise;
        }
        
        public ChannelPromise newPromise() {
            if (this.doneAllocating) {
                throw new IllegalStateException("Done allocating. No more promises can be allocated.");
            }
            ++this.expectedCount;
            return this;
        }
        
        public ChannelPromise doneAllocatingPromises() {
            if (!this.doneAllocating) {
                this.doneAllocating = true;
                if (this.successfulCount == this.expectedCount) {
                    this.promise.setSuccess();
                    return super.setSuccess();
                }
            }
            return this;
        }
        
        @Override
        public boolean tryFailure(final Throwable cause) {
            if (!this.allowNotificationEvent()) {
                return false;
            }
            ++this.failureCount;
            if (this.failureCount == 1) {
                this.promise.tryFailure(cause);
                return super.tryFailure(cause);
            }
            return true;
        }
        
        @Override
        public ChannelPromise setFailure(final Throwable cause) {
            if (this.allowNotificationEvent()) {
                ++this.failureCount;
                if (this.failureCount == 1) {
                    this.promise.setFailure(cause);
                    return super.setFailure(cause);
                }
            }
            return this;
        }
        
        private boolean allowNotificationEvent() {
            return this.successfulCount + this.failureCount < this.expectedCount;
        }
        
        @Override
        public ChannelPromise setSuccess(final Void result) {
            if (this.allowNotificationEvent()) {
                ++this.successfulCount;
                if (this.successfulCount == this.expectedCount && this.doneAllocating) {
                    this.promise.setSuccess(result);
                    return super.setSuccess(result);
                }
            }
            return this;
        }
        
        @Override
        public boolean trySuccess(final Void result) {
            if (!this.allowNotificationEvent()) {
                return false;
            }
            ++this.successfulCount;
            if (this.successfulCount == this.expectedCount && this.doneAllocating) {
                this.promise.trySuccess(result);
                return super.trySuccess(result);
            }
            return true;
        }
    }
}
