// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.DataOutputStream;
import java.io.ByteArrayOutputStream;

public class base32
{
    private String alphabet;
    private boolean padding;
    private boolean lowercase;
    
    public base32(final String alphabet, final boolean padding, final boolean lowercase) {
        this.alphabet = alphabet;
        this.padding = padding;
        this.lowercase = lowercase;
    }
    
    private static int blockLenToPadding(final int blocklen) {
        switch (blocklen) {
            case 1: {
                return 6;
            }
            case 2: {
                return 4;
            }
            case 3: {
                return 3;
            }
            case 4: {
                return 1;
            }
            case 5: {
                return 0;
            }
            default: {
                return -1;
            }
        }
    }
    
    private static int paddingToBlockLen(final int padlen) {
        switch (padlen) {
            case 6: {
                return 1;
            }
            case 4: {
                return 2;
            }
            case 3: {
                return 3;
            }
            case 1: {
                return 4;
            }
            case 0: {
                return 5;
            }
            default: {
                return -1;
            }
        }
    }
    
    public String toString(final byte[] b) {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        for (int i = 0; i < (b.length + 4) / 5; ++i) {
            final short[] s = new short[5];
            final int[] t = new int[8];
            int blocklen = 5;
            for (int j = 0; j < 5; ++j) {
                if (i * 5 + j < b.length) {
                    s[j] = (short)(b[i * 5 + j] & 0xFF);
                }
                else {
                    s[j] = 0;
                    --blocklen;
                }
            }
            final int padlen = blockLenToPadding(blocklen);
            t[0] = (byte)(s[0] >> 3 & 0x1F);
            t[1] = (byte)((s[0] & 0x7) << 2 | (s[1] >> 6 & 0x3));
            t[2] = (byte)(s[1] >> 1 & 0x1F);
            t[3] = (byte)((s[1] & 0x1) << 4 | (s[2] >> 4 & 0xF));
            t[4] = (byte)((s[2] & 0xF) << 1 | (s[3] >> 7 & 0x1));
            t[5] = (byte)(s[3] >> 2 & 0x1F);
            t[6] = (byte)((s[3] & 0x3) << 3 | (s[4] >> 5 & 0x7));
            t[7] = (byte)(s[4] & 0x1F);
            for (int k = 0; k < t.length - padlen; ++k) {
                char c = this.alphabet.charAt(t[k]);
                if (this.lowercase) {
                    c = Character.toLowerCase(c);
                }
                os.write(c);
            }
            if (this.padding) {
                for (int k = t.length - padlen; k < t.length; ++k) {
                    os.write(61);
                }
            }
        }
        return new String(os.toByteArray());
    }
    
    public byte[] fromString(final String str) {
        final ByteArrayOutputStream bs = new ByteArrayOutputStream();
        final byte[] raw = str.getBytes();
        for (int i = 0; i < raw.length; ++i) {
            char c = (char)raw[i];
            if (!Character.isWhitespace(c)) {
                c = Character.toUpperCase(c);
                bs.write((byte)c);
            }
        }
        if (this.padding) {
            if (bs.size() % 8 != 0) {
                return null;
            }
        }
        else {
            while (bs.size() % 8 != 0) {
                bs.write(61);
            }
        }
        final byte[] in = bs.toByteArray();
        bs.reset();
        final DataOutputStream ds = new DataOutputStream(bs);
        for (int j = 0; j < in.length / 8; ++j) {
            final short[] s = new short[8];
            final int[] t = new int[5];
            int padlen = 8;
            for (int k = 0; k < 8; ++k) {
                final char c2 = (char)in[j * 8 + k];
                if (c2 == '=') {
                    break;
                }
                s[k] = (short)this.alphabet.indexOf(in[j * 8 + k]);
                if (s[k] < 0) {
                    return null;
                }
                --padlen;
            }
            final int blocklen = paddingToBlockLen(padlen);
            if (blocklen < 0) {
                return null;
            }
            t[0] = (s[0] << 3 | s[1] >> 2);
            t[1] = ((s[1] & 0x3) << 6 | s[2] << 1 | s[3] >> 4);
            t[2] = ((s[3] & 0xF) << 4 | (s[4] >> 1 & 0xF));
            t[3] = (s[4] << 7 | s[5] << 2 | s[6] >> 3);
            t[4] = ((s[6] & 0x7) << 5 | s[7]);
            try {
                for (int l = 0; l < blocklen; ++l) {
                    ds.writeByte((byte)(t[l] & 0xFF));
                }
            }
            catch (IOException ex) {}
        }
        return bs.toByteArray();
    }
    
    public static class Alphabet
    {
        public static final String BASE32 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567=";
        public static final String BASE32HEX = "0123456789ABCDEFGHIJKLMNOPQRSTUV=";
        
        private Alphabet() {
        }
    }
}
