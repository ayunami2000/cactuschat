// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.io.IOException;

abstract class SingleNameBase extends Record
{
    private static final long serialVersionUID = -18595042501413L;
    protected Name singleName;
    
    protected SingleNameBase() {
    }
    
    protected SingleNameBase(final Name name, final int type, final int dclass, final long ttl) {
        super(name, type, dclass, ttl);
    }
    
    protected SingleNameBase(final Name name, final int type, final int dclass, final long ttl, final Name singleName, final String description) {
        super(name, type, dclass, ttl);
        this.singleName = Record.checkName(description, singleName);
    }
    
    void rrFromWire(final DNSInput in) throws IOException {
        this.singleName = new Name(in);
    }
    
    void rdataFromString(final Tokenizer st, final Name origin) throws IOException {
        this.singleName = st.getName(origin);
    }
    
    String rrToString() {
        return this.singleName.toString();
    }
    
    protected Name getSingleName() {
        return this.singleName;
    }
    
    void rrToWire(final DNSOutput out, final Compression c, final boolean canonical) {
        this.singleName.toWire(out, null, canonical);
    }
}
