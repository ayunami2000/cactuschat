// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import org.xbill.DNS.utils.base16;
import java.io.IOException;
import java.io.ByteArrayOutputStream;

public class NSAPRecord extends Record
{
    private static final long serialVersionUID = -1037209403185658593L;
    private byte[] address;
    
    NSAPRecord() {
    }
    
    Record getObject() {
        return new NSAPRecord();
    }
    
    private static final byte[] checkAndConvertAddress(final String address) {
        if (!address.substring(0, 2).equalsIgnoreCase("0x")) {
            return null;
        }
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        boolean partial = false;
        int current = 0;
        for (int i = 2; i < address.length(); ++i) {
            final char c = address.charAt(i);
            if (c != '.') {
                final int value = Character.digit(c, 16);
                if (value == -1) {
                    return null;
                }
                if (partial) {
                    current += value;
                    bytes.write(current);
                    partial = false;
                }
                else {
                    current = value << 4;
                    partial = true;
                }
            }
        }
        if (partial) {
            return null;
        }
        return bytes.toByteArray();
    }
    
    public NSAPRecord(final Name name, final int dclass, final long ttl, final String address) {
        super(name, 22, dclass, ttl);
        this.address = checkAndConvertAddress(address);
        if (this.address == null) {
            throw new IllegalArgumentException("invalid NSAP address " + address);
        }
    }
    
    void rrFromWire(final DNSInput in) throws IOException {
        this.address = in.readByteArray();
    }
    
    void rdataFromString(final Tokenizer st, final Name origin) throws IOException {
        final String addr = st.getString();
        this.address = checkAndConvertAddress(addr);
        if (this.address == null) {
            throw st.exception("invalid NSAP address " + addr);
        }
    }
    
    public String getAddress() {
        return Record.byteArrayToString(this.address, false);
    }
    
    void rrToWire(final DNSOutput out, final Compression c, final boolean canonical) {
        out.writeByteArray(this.address);
    }
    
    String rrToString() {
        return "0x" + base16.toString(this.address);
    }
}
