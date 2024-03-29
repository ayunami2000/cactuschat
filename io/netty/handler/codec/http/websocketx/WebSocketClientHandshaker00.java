// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http.websocketx;

import io.netty.handler.codec.Headers;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.TextHeaders;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.buffer.Unpooled;
import java.nio.ByteBuffer;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import java.net.URI;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.AsciiString;

public class WebSocketClientHandshaker00 extends WebSocketClientHandshaker
{
    private static final AsciiString WEBSOCKET;
    private ByteBuf expectedChallengeResponseBytes;
    
    public WebSocketClientHandshaker00(final URI webSocketURL, final WebSocketVersion version, final String subprotocol, final HttpHeaders customHeaders, final int maxFramePayloadLength) {
        super(webSocketURL, version, subprotocol, customHeaders, maxFramePayloadLength);
    }
    
    @Override
    protected FullHttpRequest newHandshakeRequest() {
        final int spaces1 = WebSocketUtil.randomNumber(1, 12);
        final int spaces2 = WebSocketUtil.randomNumber(1, 12);
        final int max1 = Integer.MAX_VALUE / spaces1;
        final int max2 = Integer.MAX_VALUE / spaces2;
        final int number1 = WebSocketUtil.randomNumber(0, max1);
        final int number2 = WebSocketUtil.randomNumber(0, max2);
        final int product1 = number1 * spaces1;
        final int product2 = number2 * spaces2;
        String key1 = Integer.toString(product1);
        String key2 = Integer.toString(product2);
        key1 = insertRandomCharacters(key1);
        key2 = insertRandomCharacters(key2);
        key1 = insertSpaces(key1, spaces1);
        key2 = insertSpaces(key2, spaces2);
        final byte[] key3 = WebSocketUtil.randomBytes(8);
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(number1);
        final byte[] number1Array = buffer.array();
        buffer = ByteBuffer.allocate(4);
        buffer.putInt(number2);
        final byte[] number2Array = buffer.array();
        final byte[] challenge = new byte[16];
        System.arraycopy(number1Array, 0, challenge, 0, 4);
        System.arraycopy(number2Array, 0, challenge, 4, 4);
        System.arraycopy(key3, 0, challenge, 8, 8);
        this.expectedChallengeResponseBytes = Unpooled.wrappedBuffer(WebSocketUtil.md5(challenge));
        final URI wsURL = this.uri();
        String path = wsURL.getPath();
        if (wsURL.getQuery() != null && !wsURL.getQuery().isEmpty()) {
            path = wsURL.getPath() + '?' + wsURL.getQuery();
        }
        if (path == null || path.isEmpty()) {
            path = "/";
        }
        final FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, path);
        final HttpHeaders headers = request.headers();
        headers.add((CharSequence)HttpHeaderNames.UPGRADE, (CharSequence)WebSocketClientHandshaker00.WEBSOCKET).add((CharSequence)HttpHeaderNames.CONNECTION, (CharSequence)HttpHeaderValues.UPGRADE).add((CharSequence)HttpHeaderNames.HOST, (CharSequence)wsURL.getHost());
        final int wsPort = wsURL.getPort();
        String originValue = "http://" + wsURL.getHost();
        if (wsPort != 80 && wsPort != 443) {
            originValue = originValue + ':' + wsPort;
        }
        headers.add((CharSequence)HttpHeaderNames.ORIGIN, (CharSequence)originValue).add((CharSequence)HttpHeaderNames.SEC_WEBSOCKET_KEY1, (CharSequence)key1).add((CharSequence)HttpHeaderNames.SEC_WEBSOCKET_KEY2, (CharSequence)key2);
        final String expectedSubprotocol = this.expectedSubprotocol();
        if (expectedSubprotocol != null && !expectedSubprotocol.isEmpty()) {
            headers.add((CharSequence)HttpHeaderNames.SEC_WEBSOCKET_PROTOCOL, (CharSequence)expectedSubprotocol);
        }
        if (this.customHeaders != null) {
            headers.add((TextHeaders)this.customHeaders);
        }
        headers.setInt((CharSequence)HttpHeaderNames.CONTENT_LENGTH, key3.length);
        request.content().writeBytes(key3);
        return request;
    }
    
    @Override
    protected void verify(final FullHttpResponse response) {
        final HttpResponseStatus status = new HttpResponseStatus(101, "WebSocket Protocol Handshake");
        if (!response.status().equals(status)) {
            throw new WebSocketHandshakeException("Invalid handshake response getStatus: " + response.status());
        }
        final HttpHeaders headers = response.headers();
        final CharSequence upgrade = ((Headers<AsciiString>)headers).get(HttpHeaderNames.UPGRADE);
        if (!WebSocketClientHandshaker00.WEBSOCKET.equalsIgnoreCase(upgrade)) {
            throw new WebSocketHandshakeException("Invalid handshake response upgrade: " + (Object)upgrade);
        }
        final CharSequence connection = ((Headers<AsciiString>)headers).get(HttpHeaderNames.CONNECTION);
        if (!HttpHeaderValues.UPGRADE.equalsIgnoreCase(connection)) {
            throw new WebSocketHandshakeException("Invalid handshake response connection: " + (Object)connection);
        }
        final ByteBuf challenge = response.content();
        if (!challenge.equals(this.expectedChallengeResponseBytes)) {
            throw new WebSocketHandshakeException("Invalid challenge");
        }
    }
    
    private static String insertRandomCharacters(String key) {
        final int count = WebSocketUtil.randomNumber(1, 12);
        final char[] randomChars = new char[count];
        for (int randCount = 0; randCount < count; ++randCount) {
            final int rand = (int)(Math.random() * 126.0 + 33.0);
            if ((33 < rand && rand < 47) || (58 < rand && rand < 126)) {
                randomChars[randCount] = (char)rand;
            }
        }
        for (int i = 0; i < count; ++i) {
            final int split = WebSocketUtil.randomNumber(0, key.length());
            final String part1 = key.substring(0, split);
            final String part2 = key.substring(split);
            key = part1 + randomChars[i] + part2;
        }
        return key;
    }
    
    private static String insertSpaces(String key, final int spaces) {
        for (int i = 0; i < spaces; ++i) {
            final int split = WebSocketUtil.randomNumber(1, key.length() - 1);
            final String part1 = key.substring(0, split);
            final String part2 = key.substring(split);
            key = part1 + ' ' + part2;
        }
        return key;
    }
    
    @Override
    protected WebSocketFrameDecoder newWebsocketDecoder() {
        return new WebSocket00FrameDecoder(this.maxFramePayloadLength());
    }
    
    @Override
    protected WebSocketFrameEncoder newWebSocketEncoder() {
        return new WebSocket00FrameEncoder();
    }
    
    static {
        WEBSOCKET = new AsciiString("WebSocket");
    }
}
