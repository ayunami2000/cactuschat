// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.io.IOException;

public class NSECRecord extends Record
{
    private static final long serialVersionUID = -5165065768816265385L;
    private Name next;
    private TypeBitmap types;
    
    NSECRecord() {
    }
    
    Record getObject() {
        return new NSECRecord();
    }
    
    public NSECRecord(final Name name, final int dclass, final long ttl, final Name next, final int[] types) {
        super(name, 47, dclass, ttl);
        this.next = Record.checkName("next", next);
        for (int i = 0; i < types.length; ++i) {
            Type.check(types[i]);
        }
        this.types = new TypeBitmap(types);
    }
    
    void rrFromWire(final DNSInput in) throws IOException {
        this.next = new Name(in);
        this.types = new TypeBitmap(in);
    }
    
    void rrToWire(final DNSOutput out, final Compression c, final boolean canonical) {
        this.next.toWire(out, null, false);
        this.types.toWire(out);
    }
    
    void rdataFromString(final Tokenizer st, final Name origin) throws IOException {
        this.next = st.getName(origin);
        this.types = new TypeBitmap(st);
    }
    
    String rrToString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(this.next);
        if (!this.types.empty()) {
            sb.append(' ');
            sb.append(this.types.toString());
        }
        return sb.toString();
    }
    
    public Name getNext() {
        return this.next;
    }
    
    public int[] getTypes() {
        return this.types.toArray();
    }
    
    public boolean hasType(final int type) {
        return this.types.contains(type);
    }
}
