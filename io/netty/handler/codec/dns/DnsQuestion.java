// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.dns;

public final class DnsQuestion extends DnsEntry
{
    public DnsQuestion(final String name, final DnsType type) {
        this(name, type, DnsClass.IN);
    }
    
    public DnsQuestion(final String name, final DnsType type, final DnsClass qClass) {
        super(name, type, qClass);
        if (name.isEmpty()) {
            throw new IllegalArgumentException("name must not be left blank.");
        }
    }
    
    @Override
    public boolean equals(final Object other) {
        return other instanceof DnsQuestion && super.equals(other);
    }
    
    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
