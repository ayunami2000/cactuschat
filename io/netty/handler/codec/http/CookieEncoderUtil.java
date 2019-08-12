// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http;

import io.netty.util.internal.InternalThreadLocalMap;

final class CookieEncoderUtil
{
    static StringBuilder stringBuilder() {
        return InternalThreadLocalMap.get().stringBuilder();
    }
    
    static String stripTrailingSeparatorOrNull(final StringBuilder buf) {
        return (buf.length() == 0) ? null : stripTrailingSeparator(buf);
    }
    
    static String stripTrailingSeparator(final StringBuilder buf) {
        if (buf.length() > 0) {
            buf.setLength(buf.length() - 2);
        }
        return buf.toString();
    }
    
    static void addUnquoted(final StringBuilder sb, final String name, final String val) {
        sb.append(name);
        sb.append('=');
        sb.append(val);
        sb.append(';');
        sb.append(' ');
    }
    
    static void add(final StringBuilder sb, final String name, final long val) {
        sb.append(name);
        sb.append('=');
        sb.append(val);
        sb.append(';');
        sb.append(' ');
    }
    
    private CookieEncoderUtil() {
    }
}
