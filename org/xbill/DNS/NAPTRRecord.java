// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.io.IOException;

public class NAPTRRecord extends Record
{
    private static final long serialVersionUID = 5191232392044947002L;
    private int order;
    private int preference;
    private byte[] flags;
    private byte[] service;
    private byte[] regexp;
    private Name replacement;
    
    NAPTRRecord() {
    }
    
    Record getObject() {
        return new NAPTRRecord();
    }
    
    public NAPTRRecord(final Name name, final int dclass, final long ttl, final int order, final int preference, final String flags, final String service, final String regexp, final Name replacement) {
        super(name, 35, dclass, ttl);
        this.order = Record.checkU16("order", order);
        this.preference = Record.checkU16("preference", preference);
        try {
            this.flags = Record.byteArrayFromString(flags);
            this.service = Record.byteArrayFromString(service);
            this.regexp = Record.byteArrayFromString(regexp);
        }
        catch (TextParseException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        this.replacement = Record.checkName("replacement", replacement);
    }
    
    void rrFromWire(final DNSInput in) throws IOException {
        this.order = in.readU16();
        this.preference = in.readU16();
        this.flags = in.readCountedString();
        this.service = in.readCountedString();
        this.regexp = in.readCountedString();
        this.replacement = new Name(in);
    }
    
    void rdataFromString(final Tokenizer st, final Name origin) throws IOException {
        this.order = st.getUInt16();
        this.preference = st.getUInt16();
        try {
            this.flags = Record.byteArrayFromString(st.getString());
            this.service = Record.byteArrayFromString(st.getString());
            this.regexp = Record.byteArrayFromString(st.getString());
        }
        catch (TextParseException e) {
            throw st.exception(e.getMessage());
        }
        this.replacement = st.getName(origin);
    }
    
    String rrToString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(this.order);
        sb.append(" ");
        sb.append(this.preference);
        sb.append(" ");
        sb.append(Record.byteArrayToString(this.flags, true));
        sb.append(" ");
        sb.append(Record.byteArrayToString(this.service, true));
        sb.append(" ");
        sb.append(Record.byteArrayToString(this.regexp, true));
        sb.append(" ");
        sb.append(this.replacement);
        return sb.toString();
    }
    
    public int getOrder() {
        return this.order;
    }
    
    public int getPreference() {
        return this.preference;
    }
    
    public String getFlags() {
        return Record.byteArrayToString(this.flags, false);
    }
    
    public String getService() {
        return Record.byteArrayToString(this.service, false);
    }
    
    public String getRegexp() {
        return Record.byteArrayToString(this.regexp, false);
    }
    
    public Name getReplacement() {
        return this.replacement;
    }
    
    void rrToWire(final DNSOutput out, final Compression c, final boolean canonical) {
        out.writeU16(this.order);
        out.writeU16(this.preference);
        out.writeCountedString(this.flags);
        out.writeCountedString(this.service);
        out.writeCountedString(this.regexp);
        this.replacement.toWire(out, null, canonical);
    }
    
    public Name getAdditionalName() {
        return this.replacement;
    }
}
