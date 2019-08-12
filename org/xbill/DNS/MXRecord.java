// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

public class MXRecord extends U16NameBase
{
    private static final long serialVersionUID = 2914841027584208546L;
    
    MXRecord() {
    }
    
    Record getObject() {
        return new MXRecord();
    }
    
    public MXRecord(final Name name, final int dclass, final long ttl, final int priority, final Name target) {
        super(name, 15, dclass, ttl, priority, "priority", target, "target");
    }
    
    public Name getTarget() {
        return this.getNameField();
    }
    
    public int getPriority() {
        return this.getU16Field();
    }
    
    void rrToWire(final DNSOutput out, final Compression c, final boolean canonical) {
        out.writeU16(this.u16Field);
        this.nameField.toWire(out, c, canonical);
    }
    
    public Name getAdditionalName() {
        return this.getNameField();
    }
}
