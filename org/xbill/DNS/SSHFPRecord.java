// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import org.xbill.DNS.utils.base16;
import java.io.IOException;

public class SSHFPRecord extends Record
{
    private static final long serialVersionUID = -8104701402654687025L;
    private int alg;
    private int digestType;
    private byte[] fingerprint;
    
    SSHFPRecord() {
    }
    
    Record getObject() {
        return new SSHFPRecord();
    }
    
    public SSHFPRecord(final Name name, final int dclass, final long ttl, final int alg, final int digestType, final byte[] fingerprint) {
        super(name, 44, dclass, ttl);
        this.alg = Record.checkU8("alg", alg);
        this.digestType = Record.checkU8("digestType", digestType);
        this.fingerprint = fingerprint;
    }
    
    void rrFromWire(final DNSInput in) throws IOException {
        this.alg = in.readU8();
        this.digestType = in.readU8();
        this.fingerprint = in.readByteArray();
    }
    
    void rdataFromString(final Tokenizer st, final Name origin) throws IOException {
        this.alg = st.getUInt8();
        this.digestType = st.getUInt8();
        this.fingerprint = st.getHex(true);
    }
    
    String rrToString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(this.alg);
        sb.append(" ");
        sb.append(this.digestType);
        sb.append(" ");
        sb.append(base16.toString(this.fingerprint));
        return sb.toString();
    }
    
    public int getAlgorithm() {
        return this.alg;
    }
    
    public int getDigestType() {
        return this.digestType;
    }
    
    public byte[] getFingerPrint() {
        return this.fingerprint;
    }
    
    void rrToWire(final DNSOutput out, final Compression c, final boolean canonical) {
        out.writeU8(this.alg);
        out.writeU8(this.digestType);
        out.writeByteArray(this.fingerprint);
    }
    
    public static class Algorithm
    {
        public static final int RSA = 1;
        public static final int DSS = 2;
        
        private Algorithm() {
        }
    }
    
    public static class Digest
    {
        public static final int SHA1 = 1;
        
        private Digest() {
        }
    }
}
