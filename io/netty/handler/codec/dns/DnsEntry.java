// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.dns;

import io.netty.util.internal.StringUtil;

public class DnsEntry
{
    private final String name;
    private final DnsType type;
    private final DnsClass dnsClass;
    
    DnsEntry(final String name, final DnsType type, final DnsClass dnsClass) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        if (type == null) {
            throw new NullPointerException("type");
        }
        if (dnsClass == null) {
            throw new NullPointerException("dnsClass");
        }
        this.name = name;
        this.type = type;
        this.dnsClass = dnsClass;
    }
    
    public String name() {
        return this.name;
    }
    
    public DnsType type() {
        return this.type;
    }
    
    public DnsClass dnsClass() {
        return this.dnsClass;
    }
    
    @Override
    public int hashCode() {
        return (this.name.hashCode() * 31 + this.type.hashCode()) * 31 + this.dnsClass.hashCode();
    }
    
    @Override
    public String toString() {
        return new StringBuilder(128).append(StringUtil.simpleClassName(this)).append("(name: ").append(this.name).append(", type: ").append(this.type).append(", class: ").append(this.dnsClass).append(')').toString();
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DnsEntry)) {
            return false;
        }
        final DnsEntry that = (DnsEntry)o;
        return this.type().intValue() == that.type().intValue() && this.dnsClass().intValue() == that.dnsClass().intValue() && this.name().equals(that.name());
    }
}
