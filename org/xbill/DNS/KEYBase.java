// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import org.xbill.DNS.utils.base64;
import java.io.IOException;
import java.security.PublicKey;

abstract class KEYBase extends Record
{
    private static final long serialVersionUID = 3469321722693285454L;
    protected int flags;
    protected int proto;
    protected int alg;
    protected byte[] key;
    protected int footprint;
    protected PublicKey publicKey;
    
    protected KEYBase() {
        this.footprint = -1;
        this.publicKey = null;
    }
    
    public KEYBase(final Name name, final int type, final int dclass, final long ttl, final int flags, final int proto, final int alg, final byte[] key) {
        super(name, type, dclass, ttl);
        this.footprint = -1;
        this.publicKey = null;
        this.flags = Record.checkU16("flags", flags);
        this.proto = Record.checkU8("proto", proto);
        this.alg = Record.checkU8("alg", alg);
        this.key = key;
    }
    
    void rrFromWire(final DNSInput in) throws IOException {
        this.flags = in.readU16();
        this.proto = in.readU8();
        this.alg = in.readU8();
        if (in.remaining() > 0) {
            this.key = in.readByteArray();
        }
    }
    
    String rrToString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(this.flags);
        sb.append(" ");
        sb.append(this.proto);
        sb.append(" ");
        sb.append(this.alg);
        if (this.key != null) {
            if (Options.check("multiline")) {
                sb.append(" (\n");
                sb.append(base64.formatString(this.key, 64, "\t", true));
                sb.append(" ; key_tag = ");
                sb.append(this.getFootprint());
            }
            else {
                sb.append(" ");
                sb.append(base64.toString(this.key));
            }
        }
        return sb.toString();
    }
    
    public int getFlags() {
        return this.flags;
    }
    
    public int getProtocol() {
        return this.proto;
    }
    
    public int getAlgorithm() {
        return this.alg;
    }
    
    public byte[] getKey() {
        return this.key;
    }
    
    public int getFootprint() {
        if (this.footprint >= 0) {
            return this.footprint;
        }
        int foot = 0;
        final DNSOutput out = new DNSOutput();
        this.rrToWire(out, null, false);
        final byte[] rdata = out.toByteArray();
        if (this.alg == 1) {
            final int d1 = rdata[rdata.length - 3] & 0xFF;
            final int d2 = rdata[rdata.length - 2] & 0xFF;
            foot = (d1 << 8) + d2;
        }
        else {
            int i;
            for (i = 0; i < rdata.length - 1; i += 2) {
                final int d3 = rdata[i] & 0xFF;
                final int d4 = rdata[i + 1] & 0xFF;
                foot += (d3 << 8) + d4;
            }
            if (i < rdata.length) {
                final int d3 = rdata[i] & 0xFF;
                foot += d3 << 8;
            }
            foot += (foot >> 16 & 0xFFFF);
        }
        return this.footprint = (foot & 0xFFFF);
    }
    
    public PublicKey getPublicKey() throws DNSSEC.DNSSECException {
        if (this.publicKey != null) {
            return this.publicKey;
        }
        return this.publicKey = DNSSEC.toPublicKey(this);
    }
    
    void rrToWire(final DNSOutput out, final Compression c, final boolean canonical) {
        out.writeU16(this.flags);
        out.writeU8(this.proto);
        out.writeU8(this.alg);
        if (this.key != null) {
            out.writeByteArray(this.key);
        }
    }
}
