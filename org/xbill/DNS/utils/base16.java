// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.DataOutputStream;
import java.io.ByteArrayOutputStream;

public class base16
{
    private static final String Base16 = "0123456789ABCDEF";
    
    private base16() {
    }
    
    public static String toString(final byte[] b) {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        for (int i = 0; i < b.length; ++i) {
            final short value = (short)(b[i] & 0xFF);
            final byte high = (byte)(value >> 4);
            final byte low = (byte)(value & 0xF);
            os.write("0123456789ABCDEF".charAt(high));
            os.write("0123456789ABCDEF".charAt(low));
        }
        return new String(os.toByteArray());
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
        if (in.length % 2 != 0) {
            return null;
        }
        bs.reset();
        final DataOutputStream ds = new DataOutputStream(bs);
        for (int j = 0; j < in.length; j += 2) {
            final byte high = (byte)"0123456789ABCDEF".indexOf(Character.toUpperCase((char)in[j]));
            final byte low = (byte)"0123456789ABCDEF".indexOf(Character.toUpperCase((char)in[j + 1]));
            try {
                ds.writeByte((high << 4) + low);
            }
            catch (IOException ex) {}
        }
        return bs.toByteArray();
    }
}
