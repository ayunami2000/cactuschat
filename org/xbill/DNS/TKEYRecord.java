// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import org.xbill.DNS.utils.base64;
import java.io.IOException;
import java.util.Date;

public class TKEYRecord extends Record
{
    private static final long serialVersionUID = 8828458121926391756L;
    private Name alg;
    private Date timeInception;
    private Date timeExpire;
    private int mode;
    private int error;
    private byte[] key;
    private byte[] other;
    public static final int SERVERASSIGNED = 1;
    public static final int DIFFIEHELLMAN = 2;
    public static final int GSSAPI = 3;
    public static final int RESOLVERASSIGNED = 4;
    public static final int DELETE = 5;
    
    TKEYRecord() {
    }
    
    Record getObject() {
        return new TKEYRecord();
    }
    
    public TKEYRecord(final Name name, final int dclass, final long ttl, final Name alg, final Date timeInception, final Date timeExpire, final int mode, final int error, final byte[] key, final byte[] other) {
        super(name, 249, dclass, ttl);
        this.alg = Record.checkName("alg", alg);
        this.timeInception = timeInception;
        this.timeExpire = timeExpire;
        this.mode = Record.checkU16("mode", mode);
        this.error = Record.checkU16("error", error);
        this.key = key;
        this.other = other;
    }
    
    void rrFromWire(final DNSInput in) throws IOException {
        this.alg = new Name(in);
        this.timeInception = new Date(1000L * in.readU32());
        this.timeExpire = new Date(1000L * in.readU32());
        this.mode = in.readU16();
        this.error = in.readU16();
        final int keylen = in.readU16();
        if (keylen > 0) {
            this.key = in.readByteArray(keylen);
        }
        else {
            this.key = null;
        }
        final int otherlen = in.readU16();
        if (otherlen > 0) {
            this.other = in.readByteArray(otherlen);
        }
        else {
            this.other = null;
        }
    }
    
    void rdataFromString(final Tokenizer st, final Name origin) throws IOException {
        throw st.exception("no text format defined for TKEY");
    }
    
    protected String modeString() {
        switch (this.mode) {
            case 1: {
                return "SERVERASSIGNED";
            }
            case 2: {
                return "DIFFIEHELLMAN";
            }
            case 3: {
                return "GSSAPI";
            }
            case 4: {
                return "RESOLVERASSIGNED";
            }
            case 5: {
                return "DELETE";
            }
            default: {
                return Integer.toString(this.mode);
            }
        }
    }
    
    String rrToString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(this.alg);
        sb.append(" ");
        if (Options.check("multiline")) {
            sb.append("(\n\t");
        }
        sb.append(FormattedTime.format(this.timeInception));
        sb.append(" ");
        sb.append(FormattedTime.format(this.timeExpire));
        sb.append(" ");
        sb.append(this.modeString());
        sb.append(" ");
        sb.append(Rcode.TSIGstring(this.error));
        if (Options.check("multiline")) {
            sb.append("\n");
            if (this.key != null) {
                sb.append(base64.formatString(this.key, 64, "\t", false));
                sb.append("\n");
            }
            if (this.other != null) {
                sb.append(base64.formatString(this.other, 64, "\t", false));
            }
            sb.append(" )");
        }
        else {
            sb.append(" ");
            if (this.key != null) {
                sb.append(base64.toString(this.key));
                sb.append(" ");
            }
            if (this.other != null) {
                sb.append(base64.toString(this.other));
            }
        }
        return sb.toString();
    }
    
    public Name getAlgorithm() {
        return this.alg;
    }
    
    public Date getTimeInception() {
        return this.timeInception;
    }
    
    public Date getTimeExpire() {
        return this.timeExpire;
    }
    
    public int getMode() {
        return this.mode;
    }
    
    public int getError() {
        return this.error;
    }
    
    public byte[] getKey() {
        return this.key;
    }
    
    public byte[] getOther() {
        return this.other;
    }
    
    void rrToWire(final DNSOutput out, final Compression c, final boolean canonical) {
        this.alg.toWire(out, null, canonical);
        out.writeU32(this.timeInception.getTime() / 1000L);
        out.writeU32(this.timeExpire.getTime() / 1000L);
        out.writeU16(this.mode);
        out.writeU16(this.error);
        if (this.key != null) {
            out.writeU16(this.key.length);
            out.writeByteArray(this.key);
        }
        else {
            out.writeU16(0);
        }
        if (this.other != null) {
            out.writeU16(this.other.length);
            out.writeByteArray(this.other);
        }
        else {
            out.writeU16(0);
        }
    }
}
