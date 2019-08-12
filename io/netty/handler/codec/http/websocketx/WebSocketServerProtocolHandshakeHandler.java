// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http.websocketx;

import io.netty.handler.codec.AsciiString;
import io.netty.handler.codec.Headers;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.ssl.SslHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpHeaderUtil;
import io.netty.channel.ChannelHandler;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Future;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandlerAdapter;

class WebSocketServerProtocolHandshakeHandler extends ChannelHandlerAdapter
{
    private final String websocketPath;
    private final String subprotocols;
    private final boolean allowExtensions;
    private final int maxFramePayloadSize;
    private final boolean allowMaskMismatch;
    
    WebSocketServerProtocolHandshakeHandler(final String websocketPath, final String subprotocols, final boolean allowExtensions, final int maxFrameSize, final boolean allowMaskMismatch) {
        this.websocketPath = websocketPath;
        this.subprotocols = subprotocols;
        this.allowExtensions = allowExtensions;
        this.maxFramePayloadSize = maxFrameSize;
        this.allowMaskMismatch = allowMaskMismatch;
    }
    
    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        final FullHttpRequest req = (FullHttpRequest)msg;
        try {
            if (req.method() != HttpMethod.GET) {
                sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN));
                return;
            }
            final WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(ctx.pipeline(), req, this.websocketPath), this.subprotocols, this.allowExtensions, this.maxFramePayloadSize, this.allowMaskMismatch);
            final WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(req);
            if (handshaker == null) {
                WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            }
            else {
                final ChannelFuture handshakeFuture = handshaker.handshake(ctx.channel(), req);
                handshakeFuture.addListener((GenericFutureListener<? extends Future<? super Void>>)new ChannelFutureListener() {
                    @Override
                    public void operationComplete(final ChannelFuture future) throws Exception {
                        if (!future.isSuccess()) {
                            ctx.fireExceptionCaught(future.cause());
                        }
                        else {
                            ctx.fireUserEventTriggered(WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE);
                        }
                    }
                });
                WebSocketServerProtocolHandler.setHandshaker(ctx, handshaker);
                ctx.pipeline().replace(this, "WS403Responder", WebSocketServerProtocolHandler.forbiddenHttpRequestResponder());
            }
        }
        finally {
            req.release();
        }
    }
    
    private static void sendHttpResponse(final ChannelHandlerContext ctx, final HttpRequest req, final HttpResponse res) {
        final ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!HttpHeaderUtil.isKeepAlive(req) || res.status().code() != 200) {
            f.addListener((GenericFutureListener<? extends Future<? super Void>>)ChannelFutureListener.CLOSE);
        }
    }
    
    private static String getWebSocketLocation(final ChannelPipeline cp, final HttpRequest req, final String path) {
        String protocol = "ws";
        if (cp.get(SslHandler.class) != null) {
            protocol = "wss";
        }
        return protocol + "://" + (Object)((Headers<AsciiString>)req.headers()).get(HttpHeaderNames.HOST) + path;
    }
}
