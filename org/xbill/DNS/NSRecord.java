// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

public class NSRecord extends SingleCompressedNameBase
{
    private static final long serialVersionUID = 487170758138268838L;
    
    NSRecord() {
    }
    
    Record getObject() {
        return new NSRecord();
    }
    
    public NSRecord(final Name name, final int dclass, final long ttl, final Name target) {
        super(name, 2, dclass, ttl, target, "target");
    }
    
    public Name getTarget() {
        return this.getSingleName();
    }
    
    public Name getAdditionalName() {
        return this.getSingleName();
    }
}
