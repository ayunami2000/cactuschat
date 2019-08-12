// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http;

import java.util.Iterator;

public final class ClientCookieEncoder
{
    public static String encode(final String name, final String value) {
        return encode(new DefaultCookie(name, value));
    }
    
    public static String encode(final Cookie cookie) {
        if (cookie == null) {
            throw new NullPointerException("cookie");
        }
        final StringBuilder buf = CookieEncoderUtil.stringBuilder();
        encode(buf, cookie);
        return CookieEncoderUtil.stripTrailingSeparator(buf);
    }
    
    public static String encode(final Cookie... cookies) {
        if (cookies == null) {
            throw new NullPointerException("cookies");
        }
        if (cookies.length == 0) {
            return null;
        }
        final StringBuilder buf = CookieEncoderUtil.stringBuilder();
        for (final Cookie c : cookies) {
            if (c == null) {
                break;
            }
            encode(buf, c);
        }
        return CookieEncoderUtil.stripTrailingSeparatorOrNull(buf);
    }
    
    public static String encode(final Iterable<Cookie> cookies) {
        if (cookies == null) {
            throw new NullPointerException("cookies");
        }
        if (!cookies.iterator().hasNext()) {
            return null;
        }
        final StringBuilder buf = CookieEncoderUtil.stringBuilder();
        for (final Cookie c : cookies) {
            if (c == null) {
                break;
            }
            encode(buf, c);
        }
        return CookieEncoderUtil.stripTrailingSeparatorOrNull(buf);
    }
    
    private static void encode(final StringBuilder buf, final Cookie c) {
        final String value = (c.rawValue() != null) ? c.rawValue() : ((c.value() != null) ? c.value() : "");
        CookieEncoderUtil.addUnquoted(buf, c.name(), value);
    }
    
    private ClientCookieEncoder() {
    }
}
