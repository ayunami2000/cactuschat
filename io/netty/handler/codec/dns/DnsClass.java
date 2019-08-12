// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.dns;

public final class DnsClass implements Comparable<DnsClass>
{
    public static final DnsClass IN;
    public static final DnsClass CSNET;
    public static final DnsClass CHAOS;
    public static final DnsClass HESIOD;
    public static final DnsClass NONE;
    public static final DnsClass ANY;
    private static final String EXPECTED;
    private final int intValue;
    private final String name;
    
    public static DnsClass valueOf(final String name) {
        if (DnsClass.IN.name().equals(name)) {
            return DnsClass.IN;
        }
        if (DnsClass.NONE.name().equals(name)) {
            return DnsClass.NONE;
        }
        if (DnsClass.ANY.name().equals(name)) {
            return DnsClass.ANY;
        }
        if (DnsClass.CSNET.name().equals(name)) {
            return DnsClass.CSNET;
        }
        if (DnsClass.CHAOS.name().equals(name)) {
            return DnsClass.CHAOS;
        }
        if (DnsClass.HESIOD.name().equals(name)) {
            return DnsClass.HESIOD;
        }
        throw new IllegalArgumentException("name: " + name + DnsClass.EXPECTED);
    }
    
    public static DnsClass valueOf(final int intValue) {
        switch (intValue) {
            case 1: {
                return DnsClass.IN;
            }
            case 2: {
                return DnsClass.CSNET;
            }
            case 3: {
                return DnsClass.CHAOS;
            }
            case 4: {
                return DnsClass.HESIOD;
            }
            case 254: {
                return DnsClass.NONE;
            }
            case 255: {
                return DnsClass.ANY;
            }
            default: {
                return new DnsClass(intValue, "UNKNOWN");
            }
        }
    }
    
    public static DnsClass valueOf(final int clazz, final String name) {
        return new DnsClass(clazz, name);
    }
    
    private DnsClass(final int intValue, final String name) {
        if ((intValue & 0xFFFF) != intValue) {
            throw new IllegalArgumentException("intValue: " + intValue + " (expected: 0 ~ 65535)");
        }
        this.intValue = intValue;
        this.name = name;
    }
    
    public String name() {
        return this.name;
    }
    
    public int intValue() {
        return this.intValue;
    }
    
    @Override
    public int hashCode() {
        return this.intValue;
    }
    
    @Override
    public boolean equals(final Object o) {
        return o instanceof DnsClass && ((DnsClass)o).intValue == this.intValue;
    }
    
    @Override
    public int compareTo(final DnsClass o) {
        return this.intValue() - o.intValue();
    }
    
    @Override
    public String toString() {
        return this.name;
    }
    
    static {
        IN = new DnsClass(1, "IN");
        CSNET = new DnsClass(2, "CSNET");
        CHAOS = new DnsClass(3, "CHAOS");
        HESIOD = new DnsClass(4, "HESIOD");
        NONE = new DnsClass(254, "NONE");
        ANY = new DnsClass(255, "ANY");
        EXPECTED = " (expected: " + DnsClass.IN + '(' + DnsClass.IN.intValue() + "), " + DnsClass.CSNET + '(' + DnsClass.CSNET.intValue() + "), " + DnsClass.CHAOS + '(' + DnsClass.CHAOS.intValue() + "), " + DnsClass.HESIOD + '(' + DnsClass.HESIOD.intValue() + "), " + DnsClass.NONE + '(' + DnsClass.NONE.intValue() + "), " + DnsClass.ANY + '(' + DnsClass.ANY.intValue() + "))";
    }
}
