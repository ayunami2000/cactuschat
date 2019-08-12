// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.net.UnknownHostException;
import java.net.InetAddress;

public final class ReverseMap
{
    private static Name inaddr4;
    private static Name inaddr6;
    
    private ReverseMap() {
    }
    
    public static Name fromAddress(final byte[] addr) {
        if (addr.length != 4 && addr.length != 16) {
            throw new IllegalArgumentException("array must contain 4 or 16 elements");
        }
        final StringBuffer sb = new StringBuffer();
        if (addr.length == 4) {
            for (int i = addr.length - 1; i >= 0; --i) {
                sb.append(addr[i] & 0xFF);
                if (i > 0) {
                    sb.append(".");
                }
            }
        }
        else {
            final int[] nibbles = new int[2];
            for (int j = addr.length - 1; j >= 0; --j) {
                nibbles[0] = (addr[j] & 0xFF) >> 4;
                nibbles[1] = (addr[j] & 0xFF & 0xF);
                for (int k = nibbles.length - 1; k >= 0; --k) {
                    sb.append(Integer.toHexString(nibbles[k]));
                    if (j > 0 || k > 0) {
                        sb.append(".");
                    }
                }
            }
        }
        try {
            if (addr.length == 4) {
                return Name.fromString(sb.toString(), ReverseMap.inaddr4);
            }
            return Name.fromString(sb.toString(), ReverseMap.inaddr6);
        }
        catch (TextParseException e) {
            throw new IllegalStateException("name cannot be invalid");
        }
    }
    
    public static Name fromAddress(final int[] addr) {
        final byte[] bytes = new byte[addr.length];
        for (int i = 0; i < addr.length; ++i) {
            if (addr[i] < 0 || addr[i] > 255) {
                throw new IllegalArgumentException("array must contain values between 0 and 255");
            }
            bytes[i] = (byte)addr[i];
        }
        return fromAddress(bytes);
    }
    
    public static Name fromAddress(final InetAddress addr) {
        return fromAddress(addr.getAddress());
    }
    
    public static Name fromAddress(final String addr, final int family) throws UnknownHostException {
        final byte[] array = Address.toByteArray(addr, family);
        if (array == null) {
            throw new UnknownHostException("Invalid IP address");
        }
        return fromAddress(array);
    }
    
    public static Name fromAddress(final String addr) throws UnknownHostException {
        byte[] array = Address.toByteArray(addr, 1);
        if (array == null) {
            array = Address.toByteArray(addr, 2);
        }
        if (array == null) {
            throw new UnknownHostException("Invalid IP address");
        }
        return fromAddress(array);
    }
    
    static {
        ReverseMap.inaddr4 = Name.fromConstantString("in-addr.arpa.");
        ReverseMap.inaddr6 = Name.fromConstantString("ip6.arpa.");
    }
}
