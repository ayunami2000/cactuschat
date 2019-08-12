// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS.utils;

public class hexdump
{
    private static final char[] hex;
    
    public static String dump(final String description, final byte[] b, final int offset, final int length) {
        final StringBuffer sb = new StringBuffer();
        sb.append(length + "b");
        if (description != null) {
            sb.append(" (" + description + ")");
        }
        sb.append(':');
        int prefixlen = sb.toString().length();
        prefixlen = (prefixlen + 8 & 0xFFFFFFF8);
        sb.append('\t');
        final int perline = (80 - prefixlen) / 3;
        for (int i = 0; i < length; ++i) {
            if (i != 0 && i % perline == 0) {
                sb.append('\n');
                for (int j = 0; j < prefixlen / 8; ++j) {
                    sb.append('\t');
                }
            }
            final int value = b[i + offset] & 0xFF;
            sb.append(hexdump.hex[value >> 4]);
            sb.append(hexdump.hex[value & 0xF]);
            sb.append(' ');
        }
        sb.append('\n');
        return sb.toString();
    }
    
    public static String dump(final String s, final byte[] b) {
        return dump(s, b, 0, b.length);
    }
    
    static {
        hex = "0123456789ABCDEF".toCharArray();
    }
}
