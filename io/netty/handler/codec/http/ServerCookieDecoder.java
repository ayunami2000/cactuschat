// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http;

import java.util.TreeSet;
import java.util.Collections;
import java.util.Set;

public final class ServerCookieDecoder
{
    public static Set<Cookie> decode(final String header) {
        if (header == null) {
            throw new NullPointerException("header");
        }
        final int headerLen = header.length();
        if (headerLen == 0) {
            return Collections.emptySet();
        }
        final Set<Cookie> cookies = new TreeSet<Cookie>();
        int i = 0;
        boolean rfc2965Style = false;
        if (header.regionMatches(true, 0, "$Version", 0, 8)) {
            i = header.indexOf(59) + 1;
            rfc2965Style = true;
        }
        while (i != headerLen) {
            final char c = header.charAt(i);
            if (c == '\t' || c == '\n' || c == '\u000b' || c == '\f' || c == '\r' || c == ' ' || c == ',' || c == ';') {
                ++i;
            }
            else {
                final int newNameStart = i;
                int newNameEnd = 0;
                String value = null;
                Label_0426: {
                    if ((newNameEnd = i) == headerLen) {
                        value = null;
                    }
                    else {
                        while (true) {
                            final char curChar = header.charAt(i);
                            if (curChar == ';') {
                                newNameEnd = i;
                                value = null;
                                break;
                            }
                            if (curChar == '=') {
                                newNameEnd = i;
                                if (++i == headerLen) {
                                    value = "";
                                    break;
                                }
                                final int newValueStart = i;
                                char c2 = header.charAt(i);
                                if (c2 == '\"') {
                                    final StringBuilder newValueBuf = CookieEncoderUtil.stringBuilder();
                                    final char q = c2;
                                    boolean hadBackslash = false;
                                    ++i;
                                    while (i != headerLen) {
                                        if (hadBackslash) {
                                            hadBackslash = false;
                                            c2 = header.charAt(i++);
                                            if (c2 == '\\' || c2 == '\"') {
                                                newValueBuf.setCharAt(newValueBuf.length() - 1, c2);
                                            }
                                            else {
                                                newValueBuf.append(c2);
                                            }
                                        }
                                        else {
                                            c2 = header.charAt(i++);
                                            if (c2 == q) {
                                                value = newValueBuf.toString();
                                                break Label_0426;
                                            }
                                            newValueBuf.append(c2);
                                            if (c2 != '\\') {
                                                continue;
                                            }
                                            hadBackslash = true;
                                        }
                                    }
                                    value = newValueBuf.toString();
                                    break;
                                }
                                final int semiPos = header.indexOf(59, i);
                                if (semiPos > 0) {
                                    value = header.substring(newValueStart, semiPos);
                                    i = semiPos;
                                }
                                else {
                                    value = header.substring(newValueStart);
                                    i = headerLen;
                                }
                                break;
                            }
                            else {
                                if (++i == headerLen) {
                                    newNameEnd = headerLen;
                                    value = null;
                                    break;
                                }
                                continue;
                            }
                        }
                    }
                }
                if (rfc2965Style && (header.regionMatches(newNameStart, "$Path", 0, "$Path".length()) || header.regionMatches(newNameStart, "$Domain", 0, "$Domain".length()) || header.regionMatches(newNameStart, "$Port", 0, "$Port".length()))) {
                    continue;
                }
                final String name = header.substring(newNameStart, newNameEnd);
                cookies.add(new DefaultCookie(name, value));
            }
        }
        return cookies;
    }
    
    private ServerCookieDecoder() {
    }
}
