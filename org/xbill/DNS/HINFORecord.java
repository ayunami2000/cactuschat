// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.io.IOException;

public class HINFORecord extends Record
{
    private static final long serialVersionUID = -4732870630947452112L;
    private byte[] cpu;
    private byte[] os;
    
    HINFORecord() {
    }
    
    Record getObject() {
        return new HINFORecord();
    }
    
    public HINFORecord(final Name name, final int dclass, final long ttl, final String cpu, final String os) {
        super(name, 13, dclass, ttl);
        try {
            this.cpu = Record.byteArrayFromString(cpu);
            this.os = Record.byteArrayFromString(os);
        }
        catch (TextParseException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
    
    void rrFromWire(final DNSInput in) throws IOException {
        this.cpu = in.readCountedString();
        this.os = in.readCountedString();
    }
    
    void rdataFromString(final Tokenizer st, final Name origin) throws IOException {
        try {
            this.cpu = Record.byteArrayFromString(st.getString());
            this.os = Record.byteArrayFromString(st.getString());
        }
        catch (TextParseException e) {
            throw st.exception(e.getMessage());
        }
    }
    
    public String getCPU() {
        return Record.byteArrayToString(this.cpu, false);
    }
    
    public String getOS() {
        return Record.byteArrayToString(this.os, false);
    }
    
    void rrToWire(final DNSOutput out, final Compression c, final boolean canonical) {
        out.writeCountedString(this.cpu);
        out.writeCountedString(this.os);
    }
    
    String rrToString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(Record.byteArrayToString(this.cpu, true));
        sb.append(" ");
        sb.append(Record.byteArrayToString(this.os, true));
        return sb.toString();
    }
}
