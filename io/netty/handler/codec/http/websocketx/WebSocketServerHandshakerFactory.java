// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http.websocketx;

import io.netty.handler.codec.Headers;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.channel.ChannelPromise;
import io.netty.channel.ChannelFuture;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.AsciiString;
import io.netty.handler.codec.http.HttpRequest;

public class WebSocketServerHandshakerFactory
{
    private final String webSocketURL;
    private final String subprotocols;
    private final boolean allowExtensions;
    private final int maxFramePayloadLength;
    private final boolean allowMaskMismatch;
    
    public WebSocketServerHandshakerFactory(final String webSocketURL, final String subprotocols, final boolean allowExtensions) {
        this(webSocketURL, subprotocols, allowExtensions, 65536);
    }
    
    public WebSocketServerHandshakerFactory(final String webSocketURL, final String subprotocols, final boolean allowExtensions, final int maxFramePayloadLength) {
        this(webSocketURL, subprotocols, allowExtensions, maxFramePayloadLength, false);
    }
    
    public WebSocketServerHandshakerFactory(final String webSocketURL, final String subprotocols, final boolean allowExtensions, final int maxFramePayloadLength, final boolean allowMaskMismatch) {
        this.webSocketURL = webSocketURL;
        this.subprotocols = subprotocols;
        this.allowExtensions = allowExtensions;
        this.maxFramePayloadLength = maxFramePayloadLength;
        this.allowMaskMismatch = allowMaskMismatch;
    }
    
    public WebSocketServerHandshaker newHandshaker(final HttpRequest req) {
        final CharSequence version = ((Headers<AsciiString>)req.headers()).get(HttpHeaderNames.SEC_WEBSOCKET_VERSION);
        if (version == null) {
            return new WebSocketServerHandshaker00(this.webSocketURL, this.subprotocols, this.maxFramePayloadLength);
        }
        if (version.equals(WebSocketVersion.V13.toHttpHeaderValue())) {
            return new WebSocketServerHandshaker13(this.webSocketURL, this.subprotocols, this.allowExtensions, this.maxFramePayloadLength, this.allowMaskMismatch);
        }
        if (version.equals(WebSocketVersion.V08.toHttpHeaderValue())) {
            return new WebSocketServerHandshaker08(this.webSocketURL, this.subprotocols, this.allowExtensions, this.maxFramePayloadLength, this.allowMaskMismatch);
        }
        if (version.equals(WebSocketVersion.V07.toHttpHeaderValue())) {
            return new WebSocketServerHandshaker07(this.webSocketURL, this.subprotocols, this.allowExtensions, this.maxFramePayloadLength, this.allowMaskMismatch);
        }
        return null;
    }
    
    public static ChannelFuture sendUnsupportedVersionResponse(final Channel channel) {
        return sendUnsupportedVersionResponse(channel, channel.newPromise());
    }
    
    public static ChannelFuture sendUnsupportedVersionResponse(final Channel channel, final ChannelPromise promise) {
        final HttpResponse res = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.UPGRADE_REQUIRED);
        res.headers().set((CharSequence)HttpHeaderNames.SEC_WEBSOCKET_VERSION, (CharSequence)WebSocketVersion.V13.toHttpHeaderValue());
        return channel.write(res, promise);
    }
}
