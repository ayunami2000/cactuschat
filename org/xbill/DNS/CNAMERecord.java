// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

public class CNAMERecord extends SingleCompressedNameBase
{
    private static final long serialVersionUID = -4020373886892538580L;
    
    CNAMERecord() {
    }
    
    Record getObject() {
        return new CNAMERecord();
    }
    
    public CNAMERecord(final Name name, final int dclass, final long ttl, final Name alias) {
        super(name, 5, dclass, ttl, alias, "alias");
    }
    
    public Name getTarget() {
        return this.getSingleName();
    }
    
    public Name getAlias() {
        return this.getSingleName();
    }
}
