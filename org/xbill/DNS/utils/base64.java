// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.DataOutputStream;
import java.io.ByteArrayOutputStream;

public class base64
{
    private static final String Base64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
    
    private base64() {
    }
    
    public static String toString(final byte[] b) {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        for (int i = 0; i < (b.length + 2) / 3; ++i) {
            final short[] s = new short[3];
            final short[] t = new short[4];
            for (int j = 0; j < 3; ++j) {
                if (i * 3 + j < b.length) {
                    s[j] = (short)(b[i * 3 + j] & 0xFF);
                }
                else {
                    s[j] = -1;
                }
            }
            t[0] = (short)(s[0] >> 2);
            if (s[1] == -1) {
                t[1] = (short)((s[0] & 0x3) << 4);
            }
            else {
                t[1] = (short)(((s[0] & 0x3) << 4) + (s[1] >> 4));
            }
            if (s[1] == -1) {
                t[2] = (t[3] = 64);
            }
            else if (s[2] == -1) {
                t[2] = (short)((s[1] & 0xF) << 2);
                t[3] = 64;
            }
            else {
                t[2] = (short)(((s[1] & 0xF) << 2) + (s[2] >> 6));
                t[3] = (short)(s[2] & 0x3F);
            }
            for (int j = 0; j < 4; ++j) {
                os.write("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=".charAt(t[j]));
            }
        }
        return new String(os.toByteArray());
    }
    
    public static String formatString(final byte[] b, final int lineLength, final String prefix, final boolean addClose) {
        final String s = toString(b);
        final StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.length(); i += lineLength) {
            sb.append(prefix);
            if (i + lineLength >= s.length()) {
                sb.append(s.substring(i));
                if (addClose) {
                    sb.append(" )");
                }
            }
            else {
                sb.append(s.substring(i, i + lineLength));
                sb.append("\n");
            }
        }
        return sb.toString();
    }
    
    public static byte[] fromString(final String str) {
        final ByteArrayOutputStream bs = new ByteArrayOutputStream();
        final byte[] raw = str.getBytes();
        for (int i = 0; i < raw.length; ++i) {
            if (!Character.isWhitespace((char)raw[i])) {
                bs.write(raw[i]);
            }
        }
        final byte[] in = bs.toByteArray();
        if (in.length % 4 != 0) {
            return null;
        }
        bs.reset();
        final DataOutputStream ds = new DataOutputStream(bs);
        for (int j = 0; j < (in.length + 3) / 4; ++j) {
            final short[] s = new short[4];
            final short[] t = new short[3];
            for (int k = 0; k < 4; ++k) {
                s[k] = (short)"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=".indexOf(in[j * 4 + k]);
            }
            t[0] = (short)((s[0] << 2) + (s[1] >> 4));
            if (s[2] == 64) {
                t[1] = (t[2] = -1);
                if ((s[1] & 0xF) != 0x0) {
                    return null;
                }
            }
            else if (s[3] == 64) {
                t[1] = (short)((s[1] << 4) + (s[2] >> 2) & 0xFF);
                t[2] = -1;
                if ((s[2] & 0x3) != 0x0) {
                    return null;
                }
            }
            else {
                t[1] = (short)((s[1] << 4) + (s[2] >> 2) & 0xFF);
                t[2] = (short)((s[2] << 6) + s[3] & 0xFF);
            }
            try {
                for (int k = 0; k < 3; ++k) {
                    if (t[k] >= 0) {
                        ds.writeByte(t[k]);
                    }
                }
            }
            catch (IOException ex) {}
        }
        return bs.toByteArray();
    }
}
