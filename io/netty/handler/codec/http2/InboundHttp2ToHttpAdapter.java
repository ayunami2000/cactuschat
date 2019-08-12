// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http2;

import io.netty.handler.codec.AsciiString;
import io.netty.handler.codec.Headers;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpStatusClass;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpHeaderUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.internal.ObjectUtil;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.util.collection.IntObjectMap;

public class InboundHttp2ToHttpAdapter extends Http2EventAdapter
{
    private static final ImmediateSendDetector DEFAULT_SEND_DETECTOR;
    private final int maxContentLength;
    protected final Http2Connection connection;
    protected final boolean validateHttpHeaders;
    private final ImmediateSendDetector sendDetector;
    protected final IntObjectMap<FullHttpMessage> messageMap;
    private final boolean propagateSettings;
    
    protected InboundHttp2ToHttpAdapter(final Builder builder) {
        ObjectUtil.checkNotNull(builder.connection, "connection");
        if (builder.maxContentLength <= 0) {
            throw new IllegalArgumentException("maxContentLength must be a positive integer: " + builder.maxContentLength);
        }
        this.connection = builder.connection;
        this.maxContentLength = builder.maxContentLength;
        this.validateHttpHeaders = builder.validateHttpHeaders;
        this.propagateSettings = builder.propagateSettings;
        this.sendDetector = InboundHttp2ToHttpAdapter.DEFAULT_SEND_DETECTOR;
        this.messageMap = new IntObjectHashMap<FullHttpMessage>();
    }
    
    protected void removeMessage(final int streamId) {
        this.messageMap.remove(streamId);
    }
    
    @Override
    public void streamRemoved(final Http2Stream stream) {
        this.removeMessage(stream.id());
    }
    
    protected void fireChannelRead(final ChannelHandlerContext ctx, final FullHttpMessage msg, final int streamId) {
        this.removeMessage(streamId);
        HttpHeaderUtil.setContentLength(msg, msg.content().readableBytes());
        ctx.fireChannelRead(msg);
    }
    
    protected FullHttpMessage newMessage(final int streamId, final Http2Headers headers, final boolean validateHttpHeaders) throws Http2Exception {
        return (FullHttpMessage)(this.connection.isServer() ? HttpUtil.toHttpRequest(streamId, headers, validateHttpHeaders) : HttpUtil.toHttpResponse(streamId, headers, validateHttpHeaders));
    }
    
    protected FullHttpMessage processHeadersBegin(final ChannelHandlerContext ctx, final int streamId, final Http2Headers headers, final boolean endOfStream, final boolean allowAppend, final boolean appendToTrailer) throws Http2Exception {
        FullHttpMessage msg = this.messageMap.get(streamId);
        Label_0065: {
            if (msg == null) {
                msg = this.newMessage(streamId, headers, this.validateHttpHeaders);
            }
            else {
                if (allowAppend) {
                    try {
                        HttpUtil.addHttp2ToHttpHeaders(streamId, headers, msg, appendToTrailer);
                        break Label_0065;
                    }
                    catch (Http2Exception e) {
                        this.removeMessage(streamId);
                        throw e;
                    }
                }
                msg = null;
            }
        }
        if (this.sendDetector.mustSendImmediately(msg)) {
            final FullHttpMessage copy = endOfStream ? null : this.sendDetector.copyIfNeeded(msg);
            this.fireChannelRead(ctx, msg, streamId);
            return copy;
        }
        return msg;
    }
    
    private void processHeadersEnd(final ChannelHandlerContext ctx, final int streamId, final FullHttpMessage msg, final boolean endOfStream) {
        if (endOfStream) {
            this.fireChannelRead(ctx, msg, streamId);
        }
        else {
            this.messageMap.put(streamId, msg);
        }
    }
    
    @Override
    public int onDataRead(final ChannelHandlerContext ctx, final int streamId, final ByteBuf data, final int padding, final boolean endOfStream) throws Http2Exception {
        final FullHttpMessage msg = this.messageMap.get(streamId);
        if (msg == null) {
            throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Data Frame recieved for unknown stream id %d", streamId);
        }
        final ByteBuf content = msg.content();
        final int dataReadableBytes = data.readableBytes();
        if (content.readableBytes() > this.maxContentLength - dataReadableBytes) {
            throw Http2Exception.connectionError(Http2Error.INTERNAL_ERROR, "Content length exceeded max of %d for stream id %d", this.maxContentLength, streamId);
        }
        content.writeBytes(data, data.readerIndex(), dataReadableBytes);
        if (endOfStream) {
            this.fireChannelRead(ctx, msg, streamId);
        }
        return dataReadableBytes + padding;
    }
    
    @Override
    public void onHeadersRead(final ChannelHandlerContext ctx, final int streamId, final Http2Headers headers, final int padding, final boolean endOfStream) throws Http2Exception {
        final FullHttpMessage msg = this.processHeadersBegin(ctx, streamId, headers, endOfStream, true, true);
        if (msg != null) {
            this.processHeadersEnd(ctx, streamId, msg, endOfStream);
        }
    }
    
    @Override
    public void onHeadersRead(final ChannelHandlerContext ctx, final int streamId, final Http2Headers headers, final int streamDependency, final short weight, final boolean exclusive, final int padding, final boolean endOfStream) throws Http2Exception {
        final FullHttpMessage msg = this.processHeadersBegin(ctx, streamId, headers, endOfStream, true, true);
        if (msg != null) {
            this.processHeadersEnd(ctx, streamId, msg, endOfStream);
        }
    }
    
    @Override
    public void onRstStreamRead(final ChannelHandlerContext ctx, final int streamId, final long errorCode) throws Http2Exception {
        final FullHttpMessage msg = this.messageMap.get(streamId);
        if (msg != null) {
            this.fireChannelRead(ctx, msg, streamId);
        }
    }
    
    @Override
    public void onPushPromiseRead(final ChannelHandlerContext ctx, final int streamId, final int promisedStreamId, final Http2Headers headers, final int padding) throws Http2Exception {
        final FullHttpMessage msg = this.processHeadersBegin(ctx, promisedStreamId, headers, false, false, false);
        if (msg == null) {
            throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Push Promise Frame recieved for pre-existing stream id %d", promisedStreamId);
        }
        msg.headers().setInt((CharSequence)HttpUtil.ExtensionHeaderNames.STREAM_PROMISE_ID.text(), streamId);
        this.processHeadersEnd(ctx, promisedStreamId, msg, false);
    }
    
    @Override
    public void onSettingsRead(final ChannelHandlerContext ctx, final Http2Settings settings) throws Http2Exception {
        if (this.propagateSettings) {
            ctx.fireChannelRead(settings);
        }
    }
    
    static {
        DEFAULT_SEND_DETECTOR = new ImmediateSendDetector() {
            @Override
            public boolean mustSendImmediately(final FullHttpMessage msg) {
                if (msg instanceof FullHttpResponse) {
                    return ((FullHttpResponse)msg).status().codeClass() == HttpStatusClass.INFORMATIONAL;
                }
                return msg instanceof FullHttpRequest && ((Headers<AsciiString>)msg.headers()).contains(HttpHeaderNames.EXPECT);
            }
            
            @Override
            public FullHttpMessage copyIfNeeded(final FullHttpMessage msg) {
                if (msg instanceof FullHttpRequest) {
                    final FullHttpRequest copy = ((FullHttpRequest)msg).copy((ByteBuf)null);
                    ((Headers<AsciiString>)copy.headers()).remove(HttpHeaderNames.EXPECT);
                    return copy;
                }
                return null;
            }
        };
    }
    
    public static class Builder
    {
        private final Http2Connection connection;
        private int maxContentLength;
        private boolean validateHttpHeaders;
        private boolean propagateSettings;
        
        public Builder(final Http2Connection connection) {
            this.connection = connection;
        }
        
        public Builder maxContentLength(final int maxContentLength) {
            this.maxContentLength = maxContentLength;
            return this;
        }
        
        public Builder validateHttpHeaders(final boolean validate) {
            this.validateHttpHeaders = validate;
            return this;
        }
        
        public Builder propagateSettings(final boolean propagate) {
            this.propagateSettings = propagate;
            return this;
        }
        
        public InboundHttp2ToHttpAdapter build() {
            final InboundHttp2ToHttpAdapter instance = new InboundHttp2ToHttpAdapter(this);
            this.connection.addListener(instance);
            return instance;
        }
    }
    
    private interface ImmediateSendDetector
    {
        boolean mustSendImmediately(final FullHttpMessage p0);
        
        FullHttpMessage copyIfNeeded(final FullHttpMessage p0);
    }
}
