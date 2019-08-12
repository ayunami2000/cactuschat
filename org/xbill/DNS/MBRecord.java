// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

public class MBRecord extends SingleNameBase
{
    private static final long serialVersionUID = 532349543479150419L;
    
    MBRecord() {
    }
    
    Record getObject() {
        return new MBRecord();
    }
    
    public MBRecord(final Name name, final int dclass, final long ttl, final Name mailbox) {
        super(name, 7, dclass, ttl, mailbox, "mailbox");
    }
    
    public Name getMailbox() {
        return this.getSingleName();
    }
    
    public Name getAdditionalName() {
        return this.getSingleName();
    }
}
