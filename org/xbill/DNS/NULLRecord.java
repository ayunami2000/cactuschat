// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.io.IOException;

public class NULLRecord extends Record
{
    private static final long serialVersionUID = -5796493183235216538L;
    private byte[] data;
    
    NULLRecord() {
    }
    
    Record getObject() {
        return new NULLRecord();
    }
    
    public NULLRecord(final Name name, final int dclass, final long ttl, final byte[] data) {
        super(name, 10, dclass, ttl);
        if (data.length > 65535) {
            throw new IllegalArgumentException("data must be <65536 bytes");
        }
        this.data = data;
    }
    
    void rrFromWire(final DNSInput in) throws IOException {
        this.data = in.readByteArray();
    }
    
    void rdataFromString(final Tokenizer st, final Name origin) throws IOException {
        throw st.exception("no defined text format for NULL records");
    }
    
    String rrToString() {
        return Record.unknownToString(this.data);
    }
    
    public byte[] getData() {
        return this.data;
    }
    
    void rrToWire(final DNSOutput out, final Compression c, final boolean canonical) {
        out.writeByteArray(this.data);
    }
}
