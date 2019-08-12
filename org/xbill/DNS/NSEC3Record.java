// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;
import org.xbill.DNS.utils.base16;
import java.io.IOException;
import org.xbill.DNS.utils.base32;

public class NSEC3Record extends Record
{
    public static final int SHA1_DIGEST_ID = 1;
    private static final long serialVersionUID = -7123504635968932855L;
    private int hashAlg;
    private int flags;
    private int iterations;
    private byte[] salt;
    private byte[] next;
    private TypeBitmap types;
    private static final base32 b32;
    
    NSEC3Record() {
    }
    
    Record getObject() {
        return new NSEC3Record();
    }
    
    public NSEC3Record(final Name name, final int dclass, final long ttl, final int hashAlg, final int flags, final int iterations, final byte[] salt, final byte[] next, final int[] types) {
        super(name, 50, dclass, ttl);
        this.hashAlg = Record.checkU8("hashAlg", hashAlg);
        this.flags = Record.checkU8("flags", flags);
        this.iterations = Record.checkU16("iterations", iterations);
        if (salt != null) {
            if (salt.length > 255) {
                throw new IllegalArgumentException("Invalid salt");
            }
            if (salt.length > 0) {
                System.arraycopy(salt, 0, this.salt = new byte[salt.length], 0, salt.length);
            }
        }
        if (next.length > 255) {
            throw new IllegalArgumentException("Invalid next hash");
        }
        System.arraycopy(next, 0, this.next = new byte[next.length], 0, next.length);
        this.types = new TypeBitmap(types);
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
        final int next_length = in.readU8();
        this.next = in.readByteArray(next_length);
        this.types = new TypeBitmap(in);
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
        out.writeU8(this.next.length);
        out.writeByteArray(this.next);
        this.types.toWire(out);
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
        this.next = st.getBase32String(NSEC3Record.b32);
        this.types = new TypeBitmap(st);
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
        sb.append(' ');
        sb.append(NSEC3Record.b32.toString(this.next));
        if (!this.types.empty()) {
            sb.append(' ');
            sb.append(this.types.toString());
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
    
    public byte[] getNext() {
        return this.next;
    }
    
    public int[] getTypes() {
        return this.types.toArray();
    }
    
    public boolean hasType(final int type) {
        return this.types.contains(type);
    }
    
    static byte[] hashName(final Name name, final int hashAlg, final int iterations, final byte[] salt) throws NoSuchAlgorithmException {
        switch (hashAlg) {
            case 1: {
                final MessageDigest digest = MessageDigest.getInstance("sha-1");
                byte[] hash = null;
                for (int i = 0; i <= iterations; ++i) {
                    digest.reset();
                    if (i == 0) {
                        digest.update(name.toWireCanonical());
                    }
                    else {
                        digest.update(hash);
                    }
                    if (salt != null) {
                        digest.update(salt);
                    }
                    hash = digest.digest();
                }
                return hash;
            }
            default: {
                throw new NoSuchAlgorithmException("Unknown NSEC3 algorithmidentifier: " + hashAlg);
            }
        }
    }
    
    public byte[] hashName(final Name name) throws NoSuchAlgorithmException {
        return hashName(name, this.hashAlg, this.iterations, this.salt);
    }
    
    static {
        b32 = new base32("0123456789ABCDEFGHIJKLMNOPQRSTUV=", false, false);
    }
    
    public static class Flags
    {
        public static final int OPT_OUT = 1;
        
        private Flags() {
        }
    }
    
    public static class Digest
    {
        public static final int SHA1 = 1;
        
        private Digest() {
        }
    }
}
