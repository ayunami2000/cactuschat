// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http;

import io.netty.handler.codec.Headers;
import io.netty.buffer.ByteBuf;
import java.util.Iterator;
import java.util.List;
import io.netty.handler.codec.AsciiString;

public final class HttpHeaderUtil
{
    public static boolean isKeepAlive(final HttpMessage message) {
        final CharSequence connection = ((Headers<AsciiString>)message.headers()).get(HttpHeaderNames.CONNECTION);
        if (connection != null && HttpHeaderValues.CLOSE.equalsIgnoreCase(connection)) {
            return false;
        }
        if (message.protocolVersion().isKeepAliveDefault()) {
            return !HttpHeaderValues.CLOSE.equalsIgnoreCase(connection);
        }
        return HttpHeaderValues.KEEP_ALIVE.equalsIgnoreCase(connection);
    }
    
    public static void setKeepAlive(final HttpMessage message, final boolean keepAlive) {
        final HttpHeaders h = message.headers();
        if (message.protocolVersion().isKeepAliveDefault()) {
            if (keepAlive) {
                ((Headers<AsciiString>)h).remove(HttpHeaderNames.CONNECTION);
            }
            else {
                h.set((CharSequence)HttpHeaderNames.CONNECTION, (CharSequence)HttpHeaderValues.CLOSE);
            }
        }
        else if (keepAlive) {
            h.set((CharSequence)HttpHeaderNames.CONNECTION, (CharSequence)HttpHeaderValues.KEEP_ALIVE);
        }
        else {
            ((Headers<AsciiString>)h).remove(HttpHeaderNames.CONNECTION);
        }
    }
    
    public static long getContentLength(final HttpMessage message) {
        final Long value = ((Headers<AsciiString>)message.headers()).getLong(HttpHeaderNames.CONTENT_LENGTH);
        if (value != null) {
            return value;
        }
        final long webSocketContentLength = getWebSocketContentLength(message);
        if (webSocketContentLength >= 0L) {
            return webSocketContentLength;
        }
        throw new NumberFormatException("header not found: " + (Object)HttpHeaderNames.CONTENT_LENGTH);
    }
    
    public static long getContentLength(final HttpMessage message, final long defaultValue) {
        final Long value = ((Headers<AsciiString>)message.headers()).getLong(HttpHeaderNames.CONTENT_LENGTH);
        if (value != null) {
            return value;
        }
        final long webSocketContentLength = getWebSocketContentLength(message);
        if (webSocketContentLength >= 0L) {
            return webSocketContentLength;
        }
        return defaultValue;
    }
    
    private static int getWebSocketContentLength(final HttpMessage message) {
        final HttpHeaders h = message.headers();
        if (message instanceof HttpRequest) {
            final HttpRequest req = (HttpRequest)message;
            if (HttpMethod.GET.equals(req.method()) && ((Headers<AsciiString>)h).contains(HttpHeaderNames.SEC_WEBSOCKET_KEY1) && ((Headers<AsciiString>)h).contains(HttpHeaderNames.SEC_WEBSOCKET_KEY2)) {
                return 8;
            }
        }
        else if (message instanceof HttpResponse) {
            final HttpResponse res = (HttpResponse)message;
            if (res.status().code() == 101 && ((Headers<AsciiString>)h).contains(HttpHeaderNames.SEC_WEBSOCKET_ORIGIN) && ((Headers<AsciiString>)h).contains(HttpHeaderNames.SEC_WEBSOCKET_LOCATION)) {
                return 16;
            }
        }
        return -1;
    }
    
    public static void setContentLength(final HttpMessage message, final long length) {
        message.headers().setLong((CharSequence)HttpHeaderNames.CONTENT_LENGTH, length);
    }
    
    public static boolean isContentLengthSet(final HttpMessage m) {
        return ((Headers<AsciiString>)m.headers()).contains(HttpHeaderNames.CONTENT_LENGTH);
    }
    
    public static boolean is100ContinueExpected(final HttpMessage message) {
        if (!(message instanceof HttpRequest)) {
            return false;
        }
        if (message.protocolVersion().compareTo(HttpVersion.HTTP_1_1) < 0) {
            return false;
        }
        final CharSequence value = ((Headers<AsciiString>)message.headers()).get(HttpHeaderNames.EXPECT);
        return value != null && (HttpHeaderValues.CONTINUE.equalsIgnoreCase(value) || message.headers().contains(HttpHeaderNames.EXPECT, HttpHeaderValues.CONTINUE, true));
    }
    
    public static void set100ContinueExpected(final HttpMessage message, final boolean expected) {
        if (expected) {
            message.headers().set((CharSequence)HttpHeaderNames.EXPECT, (CharSequence)HttpHeaderValues.CONTINUE);
        }
        else {
            ((Headers<AsciiString>)message.headers()).remove(HttpHeaderNames.EXPECT);
        }
    }
    
    public static boolean isTransferEncodingChunked(final HttpMessage message) {
        return message.headers().contains(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED, true);
    }
    
    public static void setTransferEncodingChunked(final HttpMessage m, final boolean chunked) {
        if (chunked) {
            m.headers().add((CharSequence)HttpHeaderNames.TRANSFER_ENCODING, (CharSequence)HttpHeaderValues.CHUNKED);
            ((Headers<AsciiString>)m.headers()).remove(HttpHeaderNames.CONTENT_LENGTH);
        }
        else {
            final List<CharSequence> values = (List<CharSequence>)((Headers<AsciiString>)m.headers()).getAll(HttpHeaderNames.TRANSFER_ENCODING);
            if (values.isEmpty()) {
                return;
            }
            final Iterator<CharSequence> valuesIt = values.iterator();
            while (valuesIt.hasNext()) {
                final CharSequence value = valuesIt.next();
                if (HttpHeaderValues.CHUNKED.equalsIgnoreCase(value)) {
                    valuesIt.remove();
                }
            }
            if (values.isEmpty()) {
                ((Headers<AsciiString>)m.headers()).remove(HttpHeaderNames.TRANSFER_ENCODING);
            }
            else {
                m.headers().set((CharSequence)HttpHeaderNames.TRANSFER_ENCODING, (Iterable<? extends CharSequence>)values);
            }
        }
    }
    
    static void encodeAscii0(final CharSequence seq, final ByteBuf buf) {
        for (int length = seq.length(), i = 0; i < length; ++i) {
            buf.writeByte((byte)seq.charAt(i));
        }
    }
    
    private HttpHeaderUtil() {
    }
}
