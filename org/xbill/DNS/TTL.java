// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

public final class TTL
{
    public static final long MAX_VALUE = 2147483647L;
    
    private TTL() {
    }
    
    static void check(final long i) {
        if (i < 0L || i > 2147483647L) {
            throw new InvalidTTLException(i);
        }
    }
    
    public static long parse(final String s, final boolean clamp) {
        if (s == null || s.length() == 0 || !Character.isDigit(s.charAt(0))) {
            throw new NumberFormatException();
        }
        long value = 0L;
        long ttl = 0L;
        for (int i = 0; i < s.length(); ++i) {
            final char c = s.charAt(i);
            final long oldvalue = value;
            if (Character.isDigit(c)) {
                value = value * 10L + Character.getNumericValue(c);
                if (value < oldvalue) {
                    throw new NumberFormatException();
                }
            }
            else {
                switch (Character.toUpperCase(c)) {
                    case 'W': {
                        value *= 7L;
                    }
                    case 'D': {
                        value *= 24L;
                    }
                    case 'H': {
                        value *= 60L;
                    }
                    case 'M': {
                        value *= 60L;
                    }
                    case 'S': {
                        ttl += value;
                        value = 0L;
                        if (ttl > 4294967295L) {
                            throw new NumberFormatException();
                        }
                        break;
                    }
                    default: {
                        throw new NumberFormatException();
                    }
                }
            }
        }
        if (ttl == 0L) {
            ttl = value;
        }
        if (ttl > 4294967295L) {
            throw new NumberFormatException();
        }
        if (ttl > 2147483647L && clamp) {
            ttl = 2147483647L;
        }
        return ttl;
    }
    
    public static long parseTTL(final String s) {
        return parse(s, true);
    }
    
    public static String format(long ttl) {
        check(ttl);
        final StringBuffer sb = new StringBuffer();
        final long secs = ttl % 60L;
        ttl /= 60L;
        final long mins = ttl % 60L;
        ttl /= 60L;
        final long hours = ttl % 24L;
        ttl /= 24L;
        final long days = ttl % 7L;
        final long weeks;
        ttl = (weeks = ttl / 7L);
        if (weeks > 0L) {
            sb.append(weeks + "W");
        }
        if (days > 0L) {
            sb.append(days + "D");
        }
        if (hours > 0L) {
            sb.append(hours + "H");
        }
        if (mins > 0L) {
            sb.append(mins + "M");
        }
        if (secs > 0L || (weeks == 0L && days == 0L && hours == 0L && mins == 0L)) {
            sb.append(secs + "S");
        }
        return sb.toString();
    }
}
