// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

public class RTRecord extends U16NameBase
{
    private static final long serialVersionUID = -3206215651648278098L;
    
    RTRecord() {
    }
    
    Record getObject() {
        return new RTRecord();
    }
    
    public RTRecord(final Name name, final int dclass, final long ttl, final int preference, final Name intermediateHost) {
        super(name, 21, dclass, ttl, preference, "preference", intermediateHost, "intermediateHost");
    }
    
    public int getPreference() {
        return this.getU16Field();
    }
    
    public Name getIntermediateHost() {
        return this.getNameField();
    }
}
