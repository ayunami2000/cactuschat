// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.io.IOException;

abstract class U16NameBase extends Record
{
    private static final long serialVersionUID = -8315884183112502995L;
    protected int u16Field;
    protected Name nameField;
    
    protected U16NameBase() {
    }
    
    protected U16NameBase(final Name name, final int type, final int dclass, final long ttl) {
        super(name, type, dclass, ttl);
    }
    
    protected U16NameBase(final Name name, final int type, final int dclass, final long ttl, final int u16Field, final String u16Description, final Name nameField, final String nameDescription) {
        super(name, type, dclass, ttl);
        this.u16Field = Record.checkU16(u16Description, u16Field);
        this.nameField = Record.checkName(nameDescription, nameField);
    }
    
    void rrFromWire(final DNSInput in) throws IOException {
        this.u16Field = in.readU16();
        this.nameField = new Name(in);
    }
    
    void rdataFromString(final Tokenizer st, final Name origin) throws IOException {
        this.u16Field = st.getUInt16();
        this.nameField = st.getName(origin);
    }
    
    String rrToString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(this.u16Field);
        sb.append(" ");
        sb.append(this.nameField);
        return sb.toString();
    }
    
    protected int getU16Field() {
        return this.u16Field;
    }
    
    protected Name getNameField() {
        return this.nameField;
    }
    
    void rrToWire(final DNSOutput out, final Compression c, final boolean canonical) {
        out.writeU16(this.u16Field);
        this.nameField.toWire(out, null, canonical);
    }
}
