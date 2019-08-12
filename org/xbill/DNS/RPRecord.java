// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.io.IOException;

public class RPRecord extends Record
{
    private static final long serialVersionUID = 8124584364211337460L;
    private Name mailbox;
    private Name textDomain;
    
    RPRecord() {
    }
    
    Record getObject() {
        return new RPRecord();
    }
    
    public RPRecord(final Name name, final int dclass, final long ttl, final Name mailbox, final Name textDomain) {
        super(name, 17, dclass, ttl);
        this.mailbox = Record.checkName("mailbox", mailbox);
        this.textDomain = Record.checkName("textDomain", textDomain);
    }
    
    void rrFromWire(final DNSInput in) throws IOException {
        this.mailbox = new Name(in);
        this.textDomain = new Name(in);
    }
    
    void rdataFromString(final Tokenizer st, final Name origin) throws IOException {
        this.mailbox = st.getName(origin);
        this.textDomain = st.getName(origin);
    }
    
    String rrToString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(this.mailbox);
        sb.append(" ");
        sb.append(this.textDomain);
        return sb.toString();
    }
    
    public Name getMailbox() {
        return this.mailbox;
    }
    
    public Name getTextDomain() {
        return this.textDomain;
    }
    
    void rrToWire(final DNSOutput out, final Compression c, final boolean canonical) {
        this.mailbox.toWire(out, null, canonical);
        this.textDomain.toWire(out, null, canonical);
    }
}
