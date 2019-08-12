// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import org.xbill.DNS.utils.base64;
import java.io.IOException;
import java.util.Date;

abstract class SIGBase extends Record
{
    private static final long serialVersionUID = -3738444391533812369L;
    protected int covered;
    protected int alg;
    protected int labels;
    protected long origttl;
    protected Date expire;
    protected Date timeSigned;
    protected int footprint;
    protected Name signer;
    protected byte[] signature;
    
    protected SIGBase() {
    }
    
    public SIGBase(final Name name, final int type, final int dclass, final long ttl, final int covered, final int alg, final long origttl, final Date expire, final Date timeSigned, final int footprint, final Name signer, final byte[] signature) {
        super(name, type, dclass, ttl);
        Type.check(covered);
        TTL.check(origttl);
        this.covered = covered;
        this.alg = Record.checkU8("alg", alg);
        this.labels = name.labels() - 1;
        if (name.isWild()) {
            --this.labels;
        }
        this.origttl = origttl;
        this.expire = expire;
        this.timeSigned = timeSigned;
        this.footprint = Record.checkU16("footprint", footprint);
        this.signer = Record.checkName("signer", signer);
        this.signature = signature;
    }
    
    void rrFromWire(final DNSInput in) throws IOException {
        this.covered = in.readU16();
        this.alg = in.readU8();
        this.labels = in.readU8();
        this.origttl = in.readU32();
        this.expire = new Date(1000L * in.readU32());
        this.timeSigned = new Date(1000L * in.readU32());
        this.footprint = in.readU16();
        this.signer = new Name(in);
        this.signature = in.readByteArray();
    }
    
    void rdataFromString(final Tokenizer st, final Name origin) throws IOException {
        final String typeString = st.getString();
        this.covered = Type.value(typeString);
        if (this.covered < 0) {
            throw st.exception("Invalid type: " + typeString);
        }
        final String algString = st.getString();
        this.alg = DNSSEC.Algorithm.value(algString);
        if (this.alg < 0) {
            throw st.exception("Invalid algorithm: " + algString);
        }
        this.labels = st.getUInt8();
        this.origttl = st.getTTL();
        this.expire = FormattedTime.parse(st.getString());
        this.timeSigned = FormattedTime.parse(st.getString());
        this.footprint = st.getUInt16();
        this.signer = st.getName(origin);
        this.signature = st.getBase64();
    }
    
    String rrToString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(Type.string(this.covered));
        sb.append(" ");
        sb.append(this.alg);
        sb.append(" ");
        sb.append(this.labels);
        sb.append(" ");
        sb.append(this.origttl);
        sb.append(" ");
        if (Options.check("multiline")) {
            sb.append("(\n\t");
        }
        sb.append(FormattedTime.format(this.expire));
        sb.append(" ");
        sb.append(FormattedTime.format(this.timeSigned));
        sb.append(" ");
        sb.append(this.footprint);
        sb.append(" ");
        sb.append(this.signer);
        if (Options.check("multiline")) {
            sb.append("\n");
            sb.append(base64.formatString(this.signature, 64, "\t", true));
        }
        else {
            sb.append(" ");
            sb.append(base64.toString(this.signature));
        }
        return sb.toString();
    }
    
    public int getTypeCovered() {
        return this.covered;
    }
    
    public int getAlgorithm() {
        return this.alg;
    }
    
    public int getLabels() {
        return this.labels;
    }
    
    public long getOrigTTL() {
        return this.origttl;
    }
    
    public Date getExpire() {
        return this.expire;
    }
    
    public Date getTimeSigned() {
        return this.timeSigned;
    }
    
    public int getFootprint() {
        return this.footprint;
    }
    
    public Name getSigner() {
        return this.signer;
    }
    
    public byte[] getSignature() {
        return this.signature;
    }
    
    void setSignature(final byte[] signature) {
        this.signature = signature;
    }
    
    void rrToWire(final DNSOutput out, final Compression c, final boolean canonical) {
        out.writeU16(this.covered);
        out.writeU8(this.alg);
        out.writeU8(this.labels);
        out.writeU32(this.origttl);
        out.writeU32(this.expire.getTime() / 1000L);
        out.writeU32(this.timeSigned.getTime() / 1000L);
        out.writeU16(this.footprint);
        this.signer.toWire(out, null, canonical);
        out.writeByteArray(this.signature);
    }
}
