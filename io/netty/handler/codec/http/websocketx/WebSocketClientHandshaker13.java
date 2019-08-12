// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http.websocketx;

import io.netty.handler.codec.Headers;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.handler.codec.AsciiString;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.TextHeaders;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import java.net.URI;
import io.netty.util.internal.logging.InternalLogger;

public class WebSocketClientHandshaker13 extends WebSocketClientHandshaker
{
    private static final InternalLogger logger;
    public static final String MAGIC_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
    private String expectedChallengeResponseString;
    private final boolean allowExtensions;
    private final boolean performMasking;
    private final boolean allowMaskMismatch;
    
    public WebSocketClientHandshaker13(final URI webSocketURL, final WebSocketVersion version, final String subprotocol, final boolean allowExtensions, final HttpHeaders customHeaders, final int maxFramePayloadLength) {
        this(webSocketURL, version, subprotocol, allowExtensions, customHeaders, maxFramePayloadLength, true, false);
    }
    
    public WebSocketClientHandshaker13(final URI webSocketURL, final WebSocketVersion version, final String subprotocol, final boolean allowExtensions, final HttpHeaders customHeaders, final int maxFramePayloadLength, final boolean performMasking, final boolean allowMaskMismatch) {
        super(webSocketURL, version, subprotocol, customHeaders, maxFramePayloadLength);
        this.allowExtensions = allowExtensions;
        this.performMasking = performMasking;
        this.allowMaskMismatch = allowMaskMismatch;
    }
    
    @Override
    protected FullHttpRequest newHandshakeRequest() {
        final URI wsURL = this.uri();
        String path = wsURL.getPath();
        if (wsURL.getQuery() != null && !wsURL.getQuery().isEmpty()) {
            path = wsURL.getPath() + '?' + wsURL.getQuery();
        }
        if (path == null || path.isEmpty()) {
            path = "/";
        }
        final byte[] nonce = WebSocketUtil.randomBytes(16);
        final String key = WebSocketUtil.base64(nonce);
        final String acceptSeed = key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
        final byte[] sha1 = WebSocketUtil.sha1(acceptSeed.getBytes(CharsetUtil.US_ASCII));
        this.expectedChallengeResponseString = WebSocketUtil.base64(sha1);
        if (WebSocketClientHandshaker13.logger.isDebugEnabled()) {
            WebSocketClientHandshaker13.logger.debug("WebSocket version 13 client handshake key: {}, expected response: {}", key, this.expectedChallengeResponseString);
        }
        int wsPort = wsURL.getPort();
        if (wsPort == -1) {
            if ("wss".equals(wsURL.getScheme())) {
                wsPort = 443;
            }
            else {
                wsPort = 80;
            }
        }
        final FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, path);
        final HttpHeaders headers = request.headers();
        headers.add((CharSequence)HttpHeaderNames.UPGRADE, (CharSequence)HttpHeaderValues.WEBSOCKET).add((CharSequence)HttpHeaderNames.CONNECTION, (CharSequence)HttpHeaderValues.UPGRADE).add((CharSequence)HttpHeaderNames.SEC_WEBSOCKET_KEY, (CharSequence)key).add((CharSequence)HttpHeaderNames.HOST, (CharSequence)(wsURL.getHost() + ':' + wsPort));
        String originValue = "http://" + wsURL.getHost();
        if (wsPort != 80 && wsPort != 443) {
            originValue = originValue + ':' + wsPort;
        }
        headers.add((CharSequence)HttpHeaderNames.SEC_WEBSOCKET_ORIGIN, (CharSequence)originValue);
        final String expectedSubprotocol = this.expectedSubprotocol();
        if (expectedSubprotocol != null && !expectedSubprotocol.isEmpty()) {
            headers.add((CharSequence)HttpHeaderNames.SEC_WEBSOCKET_PROTOCOL, (CharSequence)expectedSubprotocol);
        }
        headers.add((CharSequence)HttpHeaderNames.SEC_WEBSOCKET_VERSION, (CharSequence)"13");
        if (this.customHeaders != null) {
            headers.add((TextHeaders)this.customHeaders);
        }
        return request;
    }
    
    @Override
    protected void verify(final FullHttpResponse response) {
        final HttpResponseStatus status = HttpResponseStatus.SWITCHING_PROTOCOLS;
        final HttpHeaders headers = response.headers();
        if (!response.status().equals(status)) {
            throw new WebSocketHandshakeException("Invalid handshake response getStatus: " + response.status());
        }
        final CharSequence upgrade = ((Headers<AsciiString>)headers).get(HttpHeaderNames.UPGRADE);
        if (!HttpHeaderValues.WEBSOCKET.equalsIgnoreCase(upgrade)) {
            throw new WebSocketHandshakeException("Invalid handshake response upgrade: " + (Object)upgrade);
        }
        final CharSequence connection = ((Headers<AsciiString>)headers).get(HttpHeaderNames.CONNECTION);
        if (!HttpHeaderValues.UPGRADE.equalsIgnoreCase(connection)) {
            throw new WebSocketHandshakeException("Invalid handshake response connection: " + (Object)connection);
        }
        final CharSequence accept = ((Headers<AsciiString>)headers).get(HttpHeaderNames.SEC_WEBSOCKET_ACCEPT);
        if (accept == null || !accept.equals(this.expectedChallengeResponseString)) {
            throw new WebSocketHandshakeException(String.format("Invalid challenge. Actual: %s. Expected: %s", accept, this.expectedChallengeResponseString));
        }
    }
    
    @Override
    protected WebSocketFrameDecoder newWebsocketDecoder() {
        return new WebSocket13FrameDecoder(false, this.allowExtensions, this.maxFramePayloadLength(), this.allowMaskMismatch);
    }
    
    @Override
    protected WebSocketFrameEncoder newWebSocketEncoder() {
        return new WebSocket13FrameEncoder(this.performMasking);
    }
    
    static {
        logger = InternalLoggerFactory.getInstance(WebSocketClientHandshaker13.class);
    }
}
