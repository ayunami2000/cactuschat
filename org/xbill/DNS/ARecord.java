// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.net.UnknownHostException;
import java.io.IOException;
import java.net.InetAddress;

public class ARecord extends Record
{
    private static final long serialVersionUID = -2172609200849142323L;
    private int addr;
    
    ARecord() {
    }
    
    Record getObject() {
        return new ARecord();
    }
    
    private static final int fromArray(final byte[] array) {
        return (array[0] & 0xFF) << 24 | (array[1] & 0xFF) << 16 | (array[2] & 0xFF) << 8 | (array[3] & 0xFF);
    }
    
    private static final byte[] toArray(final int addr) {
        final byte[] bytes = { (byte)(addr >>> 24 & 0xFF), (byte)(addr >>> 16 & 0xFF), (byte)(addr >>> 8 & 0xFF), (byte)(addr & 0xFF) };
        return bytes;
    }
    
    public ARecord(final Name name, final int dclass, final long ttl, final InetAddress address) {
        super(name, 1, dclass, ttl);
        if (Address.familyOf(address) != 1) {
            throw new IllegalArgumentException("invalid IPv4 address");
        }
        this.addr = fromArray(address.getAddress());
    }
    
    void rrFromWire(final DNSInput in) throws IOException {
        this.addr = fromArray(in.readByteArray(4));
    }
    
    void rdataFromString(final Tokenizer st, final Name origin) throws IOException {
        final InetAddress address = st.getAddress(1);
        this.addr = fromArray(address.getAddress());
    }
    
    String rrToString() {
        return Address.toDottedQuad(toArray(this.addr));
    }
    
    public InetAddress getAddress() {
        try {
            return InetAddress.getByAddress(toArray(this.addr));
        }
        catch (UnknownHostException e) {
            return null;
        }
    }
    
    void rrToWire(final DNSOutput out, final Compression c, final boolean canonical) {
        out.writeU32((long)this.addr & 0xFFFFFFFFL);
    }
}
