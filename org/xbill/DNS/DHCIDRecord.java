// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import org.xbill.DNS.utils.base64;
import java.io.IOException;

public class DHCIDRecord extends Record
{
    private static final long serialVersionUID = -8214820200808997707L;
    private byte[] data;
    
    DHCIDRecord() {
    }
    
    Record getObject() {
        return new DHCIDRecord();
    }
    
    public DHCIDRecord(final Name name, final int dclass, final long ttl, final byte[] data) {
        super(name, 49, dclass, ttl);
        this.data = data;
    }
    
    void rrFromWire(final DNSInput in) throws IOException {
        this.data = in.readByteArray();
    }
    
    void rdataFromString(final Tokenizer st, final Name origin) throws IOException {
        this.data = st.getBase64();
    }
    
    void rrToWire(final DNSOutput out, final Compression c, final boolean canonical) {
        out.writeByteArray(this.data);
    }
    
    String rrToString() {
        return base64.toString(this.data);
    }
    
    public byte[] getData() {
        return this.data;
    }
}
