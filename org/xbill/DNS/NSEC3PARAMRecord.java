// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.security.NoSuchAlgorithmException;
import org.xbill.DNS.utils.base16;
import java.io.IOException;

public class NSEC3PARAMRecord extends Record
{
    private static final long serialVersionUID = -8689038598776316533L;
    private int hashAlg;
    private int flags;
    private int iterations;
    private byte[] salt;
    
    NSEC3PARAMRecord() {
    }
    
    Record getObject() {
        return new NSEC3PARAMRecord();
    }
    
    public NSEC3PARAMRecord(final Name name, final int dclass, final long ttl, final int hashAlg, final int flags, final int iterations, final byte[] salt) {
        super(name, 51, dclass, ttl);
        this.hashAlg = Record.checkU8("hashAlg", hashAlg);
        this.flags = Record.checkU8("flags", flags);
        this.iterations = Record.checkU16("iterations", iterations);
        if (salt != null) {
            if (salt.length > 255) {
                throw new IllegalArgumentException("Invalid salt length");
            }
            if (salt.length > 0) {
                System.arraycopy(salt, 0, this.salt = new byte[salt.length], 0, salt.length);
            }
        }
    }
    
    void rrFromWire(final DNSInput in) throws IOException {
        this.hashAlg = in.readU8();
        this.flags = in.readU8();
        this.iterations = in.readU16();
        final int salt_length = in.readU8();
        if (salt_length > 0) {
            this.salt = in.readByteArray(salt_length);
        }
        else {
            this.salt = null;
        }
    }
    
    void rrToWire(final DNSOutput out, final Compression c, final boolean canonical) {
        out.writeU8(this.hashAlg);
        out.writeU8(this.flags);
        out.writeU16(this.iterations);
        if (this.salt != null) {
            out.writeU8(this.salt.length);
            out.writeByteArray(this.salt);
        }
        else {
            out.writeU8(0);
        }
    }
    
    void rdataFromString(final Tokenizer st, final Name origin) throws IOException {
        this.hashAlg = st.getUInt8();
        this.flags = st.getUInt8();
        this.iterations = st.getUInt16();
        final String s = st.getString();
        if (s.equals("-")) {
            this.salt = null;
        }
        else {
            st.unget();
            this.salt = st.getHexString();
            if (this.salt.length > 255) {
                throw st.exception("salt value too long");
            }
        }
    }
    
    String rrToString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(this.hashAlg);
        sb.append(' ');
        sb.append(this.flags);
        sb.append(' ');
        sb.append(this.iterations);
        sb.append(' ');
        if (this.salt == null) {
            sb.append('-');
        }
        else {
            sb.append(base16.toString(this.salt));
        }
        return sb.toString();
    }
    
    public int getHashAlgorithm() {
        return this.hashAlg;
    }
    
    public int getFlags() {
        return this.flags;
    }
    
    public int getIterations() {
        return this.iterations;
    }
    
    public byte[] getSalt() {
        return this.salt;
    }
    
    public byte[] hashName(final Name name) throws NoSuchAlgorithmException {
        return NSEC3Record.hashName(name, this.hashAlg, this.iterations, this.salt);
    }
}
