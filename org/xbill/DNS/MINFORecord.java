// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.io.IOException;

public class MINFORecord extends Record
{
    private static final long serialVersionUID = -3962147172340353796L;
    private Name responsibleAddress;
    private Name errorAddress;
    
    MINFORecord() {
    }
    
    Record getObject() {
        return new MINFORecord();
    }
    
    public MINFORecord(final Name name, final int dclass, final long ttl, final Name responsibleAddress, final Name errorAddress) {
        super(name, 14, dclass, ttl);
        this.responsibleAddress = Record.checkName("responsibleAddress", responsibleAddress);
        this.errorAddress = Record.checkName("errorAddress", errorAddress);
    }
    
    void rrFromWire(final DNSInput in) throws IOException {
        this.responsibleAddress = new Name(in);
        this.errorAddress = new Name(in);
    }
    
    void rdataFromString(final Tokenizer st, final Name origin) throws IOException {
        this.responsibleAddress = st.getName(origin);
        this.errorAddress = st.getName(origin);
    }
    
    String rrToString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(this.responsibleAddress);
        sb.append(" ");
        sb.append(this.errorAddress);
        return sb.toString();
    }
    
    public Name getResponsibleAddress() {
        return this.responsibleAddress;
    }
    
    public Name getErrorAddress() {
        return this.errorAddress;
    }
    
    void rrToWire(final DNSOutput out, final Compression c, final boolean canonical) {
        this.responsibleAddress.toWire(out, null, canonical);
        this.errorAddress.toWire(out, null, canonical);
    }
}
