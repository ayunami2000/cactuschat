// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http;

import java.util.Date;
import java.text.ParsePosition;

public final class ClientCookieDecoder
{
    public static Cookie decode(final String header) {
        if (header == null) {
            throw new NullPointerException("header");
        }
        final int headerLen = header.length();
        if (headerLen == 0) {
            return null;
        }
        CookieBuilder cookieBuilder = null;
        int i = 0;
        while (i != headerLen) {
            final char c = header.charAt(i);
            if (c == ',') {
                return cookieBuilder.cookie();
            }
            if (c == '\t' || c == '\n' || c == '\u000b' || c == '\f' || c == '\r' || c == ' ' || c == ';') {
                ++i;
            }
            else {
                final int newNameStart = i;
                int newNameEnd = 0;
                String value = null;
                String rawValue = null;
                Label_0442: {
                    if ((newNameEnd = i) == headerLen) {
                        rawValue = (value = null);
                    }
                    else {
                        while (true) {
                            final char curChar = header.charAt(i);
                            if (curChar == ';') {
                                newNameEnd = i;
                                rawValue = (value = null);
                                break;
                            }
                            if (curChar == '=') {
                                newNameEnd = i;
                                if (++i == headerLen) {
                                    rawValue = (value = "");
                                    break;
                                }
                                final int newValueStart = i;
                                char c2 = header.charAt(i);
                                if (c2 == '\"') {
                                    final StringBuilder newValueBuf = CookieEncoderUtil.stringBuilder();
                                    final int rawValueStart = i;
                                    int rawValueEnd = i;
                                    final char q = c2;
                                    boolean hadBackslash = false;
                                    ++i;
                                    while (i != headerLen) {
                                        if (hadBackslash) {
                                            hadBackslash = false;
                                            c2 = header.charAt(i++);
                                            rawValueEnd = i;
                                            if (c2 == '\\' || c2 == '\"') {
                                                newValueBuf.setCharAt(newValueBuf.length() - 1, c2);
                                            }
                                            else {
                                                newValueBuf.append(c2);
                                            }
                                        }
                                        else {
                                            c2 = header.charAt(i++);
                                            rawValueEnd = i;
                                            if (c2 == q) {
                                                value = newValueBuf.toString();
                                                rawValue = header.substring(rawValueStart, rawValueEnd);
                                                break Label_0442;
                                            }
                                            newValueBuf.append(c2);
                                            if (c2 != '\\') {
                                                continue;
                                            }
                                            hadBackslash = true;
                                        }
                                    }
                                    value = newValueBuf.toString();
                                    rawValue = header.substring(rawValueStart, rawValueEnd);
                                    break;
                                }
                                final int semiPos = header.indexOf(59, i);
                                if (semiPos > 0) {
                                    rawValue = (value = header.substring(newValueStart, semiPos));
                                    i = semiPos;
                                }
                                else {
                                    rawValue = (value = header.substring(newValueStart));
                                    i = headerLen;
                                }
                                break;
                            }
                            else {
                                if (++i == headerLen) {
                                    newNameEnd = i;
                                    rawValue = (value = null);
                                    break;
                                }
                                continue;
                            }
                        }
                    }
                }
                if (cookieBuilder == null) {
                    cookieBuilder = new CookieBuilder(header, newNameStart, newNameEnd, value, rawValue);
                }
                else {
                    cookieBuilder.appendAttribute(header, newNameStart, newNameEnd, value);
                }
            }
        }
        return cookieBuilder.cookie();
    }
    
    private ClientCookieDecoder() {
    }
    
    private static class CookieBuilder
    {
        private final String name;
        private final String value;
        private final String rawValue;
        private String domain;
        private String path;
        private long maxAge;
        private String expires;
        private boolean secure;
        private boolean httpOnly;
        
        public CookieBuilder(final String header, final int keyStart, final int keyEnd, final String value, final String rawValue) {
            this.maxAge = Long.MIN_VALUE;
            this.name = header.substring(keyStart, keyEnd);
            this.value = value;
            this.rawValue = rawValue;
        }
        
        private long mergeMaxAgeAndExpire(final long maxAge, final String expires) {
            if (maxAge != Long.MIN_VALUE) {
                return maxAge;
            }
            if (expires != null) {
                final Date expiresDate = HttpHeaderDateFormat.get().parse(expires, new ParsePosition(0));
                if (expiresDate != null) {
                    final long maxAgeMillis = expiresDate.getTime() - System.currentTimeMillis();
                    return maxAgeMillis / 1000L + ((maxAgeMillis % 1000L != 0L) ? 1 : 0);
                }
            }
            return Long.MIN_VALUE;
        }
        
        public Cookie cookie() {
            if (this.name == null) {
                return null;
            }
            final DefaultCookie cookie = new DefaultCookie(this.name, this.value);
            cookie.setValue(this.value);
            cookie.setRawValue(this.rawValue);
            cookie.setDomain(this.domain);
            cookie.setPath(this.path);
            cookie.setMaxAge(this.mergeMaxAgeAndExpire(this.maxAge, this.expires));
            cookie.setSecure(this.secure);
            cookie.setHttpOnly(this.httpOnly);
            return cookie;
        }
        
        public void appendAttribute(final String header, final int keyStart, final int keyEnd, final String value) {
            this.setCookieAttribute(header, keyStart, keyEnd, value);
        }
        
        private void setCookieAttribute(final String header, final int keyStart, final int keyEnd, final String value) {
            final int length = keyEnd - keyStart;
            if (length == 4) {
                this.parse4(header, keyStart, value);
            }
            else if (length == 6) {
                this.parse6(header, keyStart, value);
            }
            else if (length == 7) {
                this.parse7(header, keyStart, value);
            }
            else if (length == 8) {
                this.parse8(header, keyStart, value);
            }
        }
        
        private void parse4(final String header, final int nameStart, final String value) {
            if (header.regionMatches(true, nameStart, "Path", 0, 4)) {
                this.path = value;
            }
        }
        
        private void parse6(final String header, final int nameStart, final String value) {
            if (header.regionMatches(true, nameStart, "Domain", 0, 5)) {
                this.domain = (value.isEmpty() ? null : value);
            }
            else if (header.regionMatches(true, nameStart, "Secure", 0, 5)) {
                this.secure = true;
            }
        }
        
        private void setExpire(final String value) {
            this.expires = value;
        }
        
        private void setMaxAge(final String value) {
            try {
                this.maxAge = Math.max(Long.valueOf(value), 0L);
            }
            catch (NumberFormatException ex) {}
        }
        
        private void parse7(final String header, final int nameStart, final String value) {
            if (header.regionMatches(true, nameStart, "Expires", 0, 7)) {
                this.setExpire(value);
            }
            else if (header.regionMatches(true, nameStart, "Max-Age", 0, 7)) {
                this.setMaxAge(value);
            }
        }
        
        private void parse8(final String header, final int nameStart, final String value) {
            if (header.regionMatches(true, nameStart, "HttpOnly", 0, 8)) {
                this.httpOnly = true;
            }
        }
    }
}
