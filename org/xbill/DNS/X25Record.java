// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.io.IOException;

public class X25Record extends Record
{
    private static final long serialVersionUID = 4267576252335579764L;
    private byte[] address;
    
    X25Record() {
    }
    
    Record getObject() {
        return new X25Record();
    }
    
    private static final byte[] checkAndConvertAddress(final String address) {
        final int length = address.length();
        final byte[] out = new byte[length];
        for (int i = 0; i < length; ++i) {
            final char c = address.charAt(i);
            if (!Character.isDigit(c)) {
                return null;
            }
            out[i] = (byte)c;
        }
        return out;
    }
    
    public X25Record(final Name name, final int dclass, final long ttl, final String address) {
        super(name, 19, dclass, ttl);
        this.address = checkAndConvertAddress(address);
        if (this.address == null) {
            throw new IllegalArgumentException("invalid PSDN address " + address);
        }
    }
    
    void rrFromWire(final DNSInput in) throws IOException {
        this.address = in.readCountedString();
    }
    
    void rdataFromString(final Tokenizer st, final Name origin) throws IOException {
        final String addr = st.getString();
        this.address = checkAndConvertAddress(addr);
        if (this.address == null) {
            throw st.exception("invalid PSDN address " + addr);
        }
    }
    
    public String getAddress() {
        return Record.byteArrayToString(this.address, false);
    }
    
    void rrToWire(final DNSOutput out, final Compression c, final boolean canonical) {
        out.writeCountedString(this.address);
    }
    
    String rrToString() {
        return Record.byteArrayToString(this.address, true);
    }
}
