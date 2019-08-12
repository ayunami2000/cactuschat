// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.dns;

public final class DnsQueryHeader extends DnsHeader
{
    public DnsQueryHeader(final DnsMessage parent, final int id) {
        super(parent);
        this.setId(id);
        this.setRecursionDesired(true);
    }
    
    @Override
    public int type() {
        return 0;
    }
    
    @Override
    public DnsQueryHeader setType(final int type) {
        if (type != 0) {
            throw new IllegalArgumentException("type cannot be anything but TYPE_QUERY (0) for a query header.");
        }
        super.setType(type);
        return this;
    }
    
    @Override
    public DnsQueryHeader setId(final int id) {
        super.setId(id);
        return this;
    }
    
    @Override
    public DnsQueryHeader setRecursionDesired(final boolean recursionDesired) {
        super.setRecursionDesired(recursionDesired);
        return this;
    }
    
    @Override
    public DnsQueryHeader setOpcode(final int opcode) {
        super.setOpcode(opcode);
        return this;
    }
    
    @Override
    public DnsQueryHeader setZ(final int z) {
        super.setZ(z);
        return this;
    }
}
