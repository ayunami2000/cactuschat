// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.net.Inet6Address;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public final class Address
{
    public static final int IPv4 = 1;
    public static final int IPv6 = 2;
    
    private Address() {
    }
    
    private static byte[] parseV4(final String s) {
        final byte[] values = new byte[4];
        final int length = s.length();
        int currentOctet = 0;
        int currentValue = 0;
        int numDigits = 0;
        for (int i = 0; i < length; ++i) {
            final char c = s.charAt(i);
            if (c >= '0' && c <= '9') {
                if (numDigits == 3) {
                    return null;
                }
                if (numDigits > 0 && currentValue == 0) {
                    return null;
                }
                ++numDigits;
                currentValue *= 10;
                currentValue += c - '0';
                if (currentValue > 255) {
                    return null;
                }
            }
            else {
                if (c != '.') {
                    return null;
                }
                if (currentOctet == 3) {
                    return null;
                }
                if (numDigits == 0) {
                    return null;
                }
                values[currentOctet++] = (byte)currentValue;
                currentValue = 0;
                numDigits = 0;
            }
        }
        if (currentOctet != 3) {
            return null;
        }
        if (numDigits == 0) {
            return null;
        }
        values[currentOctet] = (byte)currentValue;
        return values;
    }
    
    private static byte[] parseV6(final String s) {
        int range = -1;
        final byte[] data = new byte[16];
        final String[] tokens = s.split(":", -1);
        int first = 0;
        int last = tokens.length - 1;
        if (tokens[0].length() == 0) {
            if (last - first <= 0 || tokens[1].length() != 0) {
                return null;
            }
            ++first;
        }
        if (tokens[last].length() == 0) {
            if (last - first <= 0 || tokens[last - 1].length() != 0) {
                return null;
            }
            --last;
        }
        if (last - first + 1 > 8) {
            return null;
        }
        int i = first;
        int j = 0;
        while (i <= last) {
            if (tokens[i].length() == 0) {
                if (range >= 0) {
                    return null;
                }
                range = j;
            }
            else if (tokens[i].indexOf(46) >= 0) {
                if (i < last) {
                    return null;
                }
                if (i > 6) {
                    return null;
                }
                final byte[] v4addr = toByteArray(tokens[i], 1);
                if (v4addr == null) {
                    return null;
                }
                for (int k = 0; k < 4; ++k) {
                    data[j++] = v4addr[k];
                }
                break;
            }
            else {
                try {
                    for (int l = 0; l < tokens[i].length(); ++l) {
                        final char c = tokens[i].charAt(l);
                        if (Character.digit(c, 16) < 0) {
                            return null;
                        }
                    }
                    final int x = Integer.parseInt(tokens[i], 16);
                    if (x > 65535 || x < 0) {
                        return null;
                    }
                    data[j++] = (byte)(x >>> 8);
                    data[j++] = (byte)(x & 0xFF);
                }
                catch (NumberFormatException e) {
                    return null;
                }
            }
            ++i;
        }
        if (j < 16 && range < 0) {
            return null;
        }
        if (range >= 0) {
            final int empty = 16 - j;
            System.arraycopy(data, range, data, range + empty, j - range);
            for (i = range; i < range + empty; ++i) {
                data[i] = 0;
            }
        }
        return data;
    }
    
    public static int[] toArray(final String s, final int family) {
        final byte[] byteArray = toByteArray(s, family);
        if (byteArray == null) {
            return null;
        }
        final int[] intArray = new int[byteArray.length];
        for (int i = 0; i < byteArray.length; ++i) {
            intArray[i] = (byteArray[i] & 0xFF);
        }
        return intArray;
    }
    
    public static int[] toArray(final String s) {
        return toArray(s, 1);
    }
    
    public static byte[] toByteArray(final String s, final int family) {
        if (family == 1) {
            return parseV4(s);
        }
        if (family == 2) {
            return parseV6(s);
        }
        throw new IllegalArgumentException("unknown address family");
    }
    
    public static boolean isDottedQuad(final String s) {
        final byte[] address = toByteArray(s, 1);
        return address != null;
    }
    
    public static String toDottedQuad(final byte[] addr) {
        return (addr[0] & 0xFF) + "." + (addr[1] & 0xFF) + "." + (addr[2] & 0xFF) + "." + (addr[3] & 0xFF);
    }
    
    public static String toDottedQuad(final int[] addr) {
        return addr[0] + "." + addr[1] + "." + addr[2] + "." + addr[3];
    }
    
    private static Record[] lookupHostName(final String name) throws UnknownHostException {
        try {
            final Record[] records = new Lookup(name).run();
            if (records == null) {
                throw new UnknownHostException("unknown host");
            }
            return records;
        }
        catch (TextParseException e) {
            throw new UnknownHostException("invalid name");
        }
    }
    
    private static InetAddress addrFromRecord(final String name, final Record r) throws UnknownHostException {
        final ARecord a = (ARecord)r;
        return InetAddress.getByAddress(name, a.getAddress().getAddress());
    }
    
    public static InetAddress getByName(final String name) throws UnknownHostException {
        try {
            return getByAddress(name);
        }
        catch (UnknownHostException e) {
            final Record[] records = lookupHostName(name);
            return addrFromRecord(name, records[0]);
        }
    }
    
    public static InetAddress[] getAllByName(final String name) throws UnknownHostException {
        try {
            final InetAddress addr = getByAddress(name);
            return new InetAddress[] { addr };
        }
        catch (UnknownHostException e) {
            final Record[] records = lookupHostName(name);
            final InetAddress[] addrs = new InetAddress[records.length];
            for (int i = 0; i < records.length; ++i) {
                addrs[i] = addrFromRecord(name, records[i]);
            }
            return addrs;
        }
    }
    
    public static InetAddress getByAddress(final String addr) throws UnknownHostException {
        byte[] bytes = toByteArray(addr, 1);
        if (bytes != null) {
            return InetAddress.getByAddress(bytes);
        }
        bytes = toByteArray(addr, 2);
        if (bytes != null) {
            return InetAddress.getByAddress(bytes);
        }
        throw new UnknownHostException("Invalid address: " + addr);
    }
    
    public static InetAddress getByAddress(final String addr, final int family) throws UnknownHostException {
        if (family != 1 && family != 2) {
            throw new IllegalArgumentException("unknown address family");
        }
        final byte[] bytes = toByteArray(addr, family);
        if (bytes != null) {
            return InetAddress.getByAddress(bytes);
        }
        throw new UnknownHostException("Invalid address: " + addr);
    }
    
    public static String getHostName(final InetAddress addr) throws UnknownHostException {
        final Name name = ReverseMap.fromAddress(addr);
        final Record[] records = new Lookup(name, 12).run();
        if (records == null) {
            throw new UnknownHostException("unknown address");
        }
        final PTRRecord ptr = (PTRRecord)records[0];
        return ptr.getTarget().toString();
    }
    
    public static int familyOf(final InetAddress address) {
        if (address instanceof Inet4Address) {
            return 1;
        }
        if (address instanceof Inet6Address) {
            return 2;
        }
        throw new IllegalArgumentException("unknown address family");
    }
    
    public static int addressLength(final int family) {
        if (family == 1) {
            return 4;
        }
        if (family == 2) {
            return 16;
        }
        throw new IllegalArgumentException("unknown address family");
    }
}
