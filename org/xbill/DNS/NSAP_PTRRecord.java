// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

public class NSAP_PTRRecord extends SingleNameBase
{
    private static final long serialVersionUID = 2386284746382064904L;
    
    NSAP_PTRRecord() {
    }
    
    Record getObject() {
        return new NSAP_PTRRecord();
    }
    
    public NSAP_PTRRecord(final Name name, final int dclass, final long ttl, final Name target) {
        super(name, 23, dclass, ttl, target, "target");
    }
    
    public Name getTarget() {
        return this.getSingleName();
    }
}
