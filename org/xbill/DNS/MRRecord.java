// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

public class MRRecord extends SingleNameBase
{
    private static final long serialVersionUID = -5617939094209927533L;
    
    MRRecord() {
    }
    
    Record getObject() {
        return new MRRecord();
    }
    
    public MRRecord(final Name name, final int dclass, final long ttl, final Name newName) {
        super(name, 9, dclass, ttl, newName, "new name");
    }
    
    public Name getNewName() {
        return this.getSingleName();
    }
}
