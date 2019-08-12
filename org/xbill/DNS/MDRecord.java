// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

public class MDRecord extends SingleNameBase
{
    private static final long serialVersionUID = 5268878603762942202L;
    
    MDRecord() {
    }
    
    Record getObject() {
        return new MDRecord();
    }
    
    public MDRecord(final Name name, final int dclass, final long ttl, final Name mailAgent) {
        super(name, 3, dclass, ttl, mailAgent, "mail agent");
    }
    
    public Name getMailAgent() {
        return this.getSingleName();
    }
    
    public Name getAdditionalName() {
        return this.getSingleName();
    }
}
