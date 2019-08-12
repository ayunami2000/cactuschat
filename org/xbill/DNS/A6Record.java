// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.net.UnknownHostException;
import java.io.IOException;
import java.net.InetAddress;

public class A6Record extends Record
{
    private static final long serialVersionUID = -8815026887337346789L;
    private int prefixBits;
    private InetAddress suffix;
    private Name prefix;
    
    A6Record() {
    }
    
    Record getObject() {
        return new A6Record();
    }
    
    public A6Record(final Name name, final int dclass, final long ttl, final int prefixBits, final InetAddress suffix, final Name prefix) {
        super(name, 38, dclass, ttl);
        this.prefixBits = Record.checkU8("prefixBits", prefixBits);
        if (suffix != null && Address.familyOf(suffix) != 2) {
            throw new IllegalArgumentException("invalid IPv6 address");
        }
        this.suffix = suffix;
        if (prefix != null) {
            this.prefix = Record.checkName("prefix", prefix);
        }
    }
    
    void rrFromWire(final DNSInput in) throws IOException {
        this.prefixBits = in.readU8();
        final int suffixbits = 128 - this.prefixBits;
        final int suffixbytes = (suffixbits + 7) / 8;
        if (this.prefixBits < 128) {
            final byte[] bytes = new byte[16];
            in.readByteArray(bytes, 16 - suffixbytes, suffixbytes);
            this.suffix = InetAddress.getByAddress(bytes);
        }
        if (this.prefixBits > 0) {
            this.prefix = new Name(in);
        }
    }
    
    void rdataFromString(final Tokenizer st, final Name origin) throws IOException {
        this.prefixBits = st.getUInt8();
        if (this.prefixBits > 128) {
            throw st.exception("prefix bits must be [0..128]");
        }
        if (this.prefixBits < 128) {
            final String s = st.getString();
            try {
                this.suffix = Address.getByAddress(s, 2);
            }
            catch (UnknownHostException e) {
                throw st.exception("invalid IPv6 address: " + s);
            }
        }
        if (this.prefixBits > 0) {
            this.prefix = st.getName(origin);
        }
    }
    
    String rrToString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(this.prefixBits);
        if (this.suffix != null) {
            sb.append(" ");
            sb.append(this.suffix.getHostAddress());
        }
        if (this.prefix != null) {
            sb.append(" ");
            sb.append(this.prefix);
        }
        return sb.toString();
    }
    
    public int getPrefixBits() {
        return this.prefixBits;
    }
    
    public InetAddress getSuffix() {
        return this.suffix;
    }
    
    public Name getPrefix() {
        return this.prefix;
    }
    
    void rrToWire(final DNSOutput out, final Compression c, final boolean canonical) {
        out.writeU8(this.prefixBits);
        if (this.suffix != null) {
            final int suffixbits = 128 - this.prefixBits;
            final int suffixbytes = (suffixbits + 7) / 8;
            final byte[] data = this.suffix.getAddress();
            out.writeByteArray(data, 16 - suffixbytes, suffixbytes);
        }
        if (this.prefix != null) {
            this.prefix.toWire(out, null, canonical);
        }
    }
}
