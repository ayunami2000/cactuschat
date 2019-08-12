// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.io.IOException;

public class PXRecord extends Record
{
    private static final long serialVersionUID = 1811540008806660667L;
    private int preference;
    private Name map822;
    private Name mapX400;
    
    PXRecord() {
    }
    
    Record getObject() {
        return new PXRecord();
    }
    
    public PXRecord(final Name name, final int dclass, final long ttl, final int preference, final Name map822, final Name mapX400) {
        super(name, 26, dclass, ttl);
        this.preference = Record.checkU16("preference", preference);
        this.map822 = Record.checkName("map822", map822);
        this.mapX400 = Record.checkName("mapX400", mapX400);
    }
    
    void rrFromWire(final DNSInput in) throws IOException {
        this.preference = in.readU16();
        this.map822 = new Name(in);
        this.mapX400 = new Name(in);
    }
    
    void rdataFromString(final Tokenizer st, final Name origin) throws IOException {
        this.preference = st.getUInt16();
        this.map822 = st.getName(origin);
        this.mapX400 = st.getName(origin);
    }
    
    String rrToString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(this.preference);
        sb.append(" ");
        sb.append(this.map822);
        sb.append(" ");
        sb.append(this.mapX400);
        return sb.toString();
    }
    
    void rrToWire(final DNSOutput out, final Compression c, final boolean canonical) {
        out.writeU16(this.preference);
        this.map822.toWire(out, null, canonical);
        this.mapX400.toWire(out, null, canonical);
    }
    
    public int getPreference() {
        return this.preference;
    }
    
    public Name getMap822() {
        return this.map822;
    }
    
    public Name getMapX400() {
        return this.mapX400;
    }
}
