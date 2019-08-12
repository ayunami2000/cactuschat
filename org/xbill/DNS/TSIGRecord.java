// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import org.xbill.DNS.utils.base64;
import java.io.IOException;
import java.util.Date;

public class TSIGRecord extends Record
{
    private static final long serialVersionUID = -88820909016649306L;
    private Name alg;
    private Date timeSigned;
    private int fudge;
    private byte[] signature;
    private int originalID;
    private int error;
    private byte[] other;
    
    TSIGRecord() {
    }
    
    Record getObject() {
        return new TSIGRecord();
    }
    
    public TSIGRecord(final Name name, final int dclass, final long ttl, final Name alg, final Date timeSigned, final int fudge, final byte[] signature, final int originalID, final int error, final byte[] other) {
        super(name, 250, dclass, ttl);
        this.alg = Record.checkName("alg", alg);
        this.timeSigned = timeSigned;
        this.fudge = Record.checkU16("fudge", fudge);
        this.signature = signature;
        this.originalID = Record.checkU16("originalID", originalID);
        this.error = Record.checkU16("error", error);
        this.other = other;
    }
    
    void rrFromWire(final DNSInput in) throws IOException {
        this.alg = new Name(in);
        final long timeHigh = in.readU16();
        final long timeLow = in.readU32();
        final long time = (timeHigh << 32) + timeLow;
        this.timeSigned = new Date(time * 1000L);
        this.fudge = in.readU16();
        final int sigLen = in.readU16();
        this.signature = in.readByteArray(sigLen);
        this.originalID = in.readU16();
        this.error = in.readU16();
        final int otherLen = in.readU16();
        if (otherLen > 0) {
            this.other = in.readByteArray(otherLen);
        }
        else {
            this.other = null;
        }
    }
    
    void rdataFromString(final Tokenizer st, final Name origin) throws IOException {
        throw st.exception("no text format defined for TSIG");
    }
    
    String rrToString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(this.alg);
        sb.append(" ");
        if (Options.check("multiline")) {
            sb.append("(\n\t");
        }
        sb.append(this.timeSigned.getTime() / 1000L);
        sb.append(" ");
        sb.append(this.fudge);
        sb.append(" ");
        sb.append(this.signature.length);
        if (Options.check("multiline")) {
            sb.append("\n");
            sb.append(base64.formatString(this.signature, 64, "\t", false));
        }
        else {
            sb.append(" ");
            sb.append(base64.toString(this.signature));
        }
        sb.append(" ");
        sb.append(Rcode.TSIGstring(this.error));
        sb.append(" ");
        if (this.other == null) {
            sb.append(0);
        }
        else {
            sb.append(this.other.length);
            if (Options.check("multiline")) {
                sb.append("\n\n\n\t");
            }
            else {
                sb.append(" ");
            }
            if (this.error == 18) {
                if (this.other.length != 6) {
                    sb.append("<invalid BADTIME other data>");
                }
                else {
                    final long time = ((long)(this.other[0] & 0xFF) << 40) + ((long)(this.other[1] & 0xFF) << 32) + ((this.other[2] & 0xFF) << 24) + ((this.other[3] & 0xFF) << 16) + ((this.other[4] & 0xFF) << 8) + (this.other[5] & 0xFF);
                    sb.append("<server time: ");
                    sb.append(new Date(time * 1000L));
                    sb.append(">");
                }
            }
            else {
                sb.append("<");
                sb.append(base64.toString(this.other));
                sb.append(">");
            }
        }
        if (Options.check("multiline")) {
            sb.append(" )");
        }
        return sb.toString();
    }
    
    public Name getAlgorithm() {
        return this.alg;
    }
    
    public Date getTimeSigned() {
        return this.timeSigned;
    }
    
    public int getFudge() {
        return this.fudge;
    }
    
    public byte[] getSignature() {
        return this.signature;
    }
    
    public int getOriginalID() {
        return this.originalID;
    }
    
    public int getError() {
        return this.error;
    }
    
    public byte[] getOther() {
        return this.other;
    }
    
    void rrToWire(final DNSOutput out, final Compression c, final boolean canonical) {
        this.alg.toWire(out, null, canonical);
        final long time = this.timeSigned.getTime() / 1000L;
        final int timeHigh = (int)(time >> 32);
        final long timeLow = time & 0xFFFFFFFFL;
        out.writeU16(timeHigh);
        out.writeU32(timeLow);
        out.writeU16(this.fudge);
        out.writeU16(this.signature.length);
        out.writeByteArray(this.signature);
        out.writeU16(this.originalID);
        out.writeU16(this.error);
        if (this.other != null) {
            out.writeU16(this.other.length);
            out.writeByteArray(this.other);
        }
        else {
            out.writeU16(0);
        }
    }
}
