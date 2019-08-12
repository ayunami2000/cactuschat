// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http.websocketx;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.channel.ChannelFutureListener;
import io.netty.buffer.Unpooled;
import java.util.List;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;

public class WebSocketServerProtocolHandler extends WebSocketProtocolHandler
{
    private static final AttributeKey<WebSocketServerHandshaker> HANDSHAKER_ATTR_KEY;
    private final String websocketPath;
    private final String subprotocols;
    private final boolean allowExtensions;
    private final int maxFramePayloadLength;
    private final boolean allowMaskMismatch;
    
    public WebSocketServerProtocolHandler(final String websocketPath) {
        this(websocketPath, null, false);
    }
    
    public WebSocketServerProtocolHandler(final String websocketPath, final String subprotocols) {
        this(websocketPath, subprotocols, false);
    }
    
    public WebSocketServerProtocolHandler(final String websocketPath, final String subprotocols, final boolean allowExtensions) {
        this(websocketPath, subprotocols, allowExtensions, 65536);
    }
    
    public WebSocketServerProtocolHandler(final String websocketPath, final String subprotocols, final boolean allowExtensions, final int maxFrameSize) {
        this(websocketPath, subprotocols, allowExtensions, maxFrameSize, false);
    }
    
    public WebSocketServerProtocolHandler(final String websocketPath, final String subprotocols, final boolean allowExtensions, final int maxFrameSize, final boolean allowMaskMismatch) {
        this.websocketPath = websocketPath;
        this.subprotocols = subprotocols;
        this.allowExtensions = allowExtensions;
        this.maxFramePayloadLength = maxFrameSize;
        this.allowMaskMismatch = allowMaskMismatch;
    }
    
    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) {
        final ChannelPipeline cp = ctx.pipeline();
        if (cp.get(WebSocketServerProtocolHandshakeHandler.class) == null) {
            ctx.pipeline().addBefore(ctx.name(), WebSocketServerProtocolHandshakeHandler.class.getName(), new WebSocketServerProtocolHandshakeHandler(this.websocketPath, this.subprotocols, this.allowExtensions, this.maxFramePayloadLength, this.allowMaskMismatch));
        }
        if (cp.get(Utf8FrameValidator.class) == null) {
            ctx.pipeline().addBefore(ctx.name(), Utf8FrameValidator.class.getName(), new Utf8FrameValidator());
        }
    }
    
    @Override
    protected void decode(final ChannelHandlerContext ctx, final WebSocketFrame frame, final List<Object> out) throws Exception {
        if (frame instanceof CloseWebSocketFrame) {
            final WebSocketServerHandshaker handshaker = getHandshaker(ctx);
            if (handshaker != null) {
                frame.retain();
                handshaker.close(ctx.channel(), (CloseWebSocketFrame)frame);
            }
            else {
                ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener((GenericFutureListener<? extends Future<? super Void>>)ChannelFutureListener.CLOSE);
            }
            return;
        }
        super.decode(ctx, frame, out);
    }
    
    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        if (cause instanceof WebSocketHandshakeException) {
            final FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.wrappedBuffer(cause.getMessage().getBytes()));
            ctx.channel().writeAndFlush(response).addListener((GenericFutureListener<? extends Future<? super Void>>)ChannelFutureListener.CLOSE);
        }
        else {
            ctx.close();
        }
    }
    
    static WebSocketServerHandshaker getHandshaker(final ChannelHandlerContext ctx) {
        return ctx.attr(WebSocketServerProtocolHandler.HANDSHAKER_ATTR_KEY).get();
    }
    
    static void setHandshaker(final ChannelHandlerContext ctx, final WebSocketServerHandshaker handshaker) {
        ctx.attr(WebSocketServerProtocolHandler.HANDSHAKER_ATTR_KEY).set(handshaker);
    }
    
    static ChannelHandler forbiddenHttpRequestResponder() {
        return new ChannelHandlerAdapter() {
            @Override
            public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
                if (msg instanceof FullHttpRequest) {
                    final FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN);
                    ctx.channel().writeAndFlush(response);
                }
                else {
                    ctx.fireChannelRead(msg);
                }
            }
        };
    }
    
    static {
        HANDSHAKER_ATTR_KEY = AttributeKey.valueOf(WebSocketServerHandshaker.class, "HANDSHAKER");
    }
    
    public enum ServerHandshakeStateEvent
    {
        HANDSHAKE_COMPLETE;
    }
}
