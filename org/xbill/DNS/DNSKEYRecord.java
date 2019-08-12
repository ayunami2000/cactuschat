// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.io.IOException;
import java.security.PublicKey;

public class DNSKEYRecord extends KEYBase
{
    private static final long serialVersionUID = -8679800040426675002L;
    
    DNSKEYRecord() {
    }
    
    Record getObject() {
        return new DNSKEYRecord();
    }
    
    public DNSKEYRecord(final Name name, final int dclass, final long ttl, final int flags, final int proto, final int alg, final byte[] key) {
        super(name, 48, dclass, ttl, flags, proto, alg, key);
    }
    
    public DNSKEYRecord(final Name name, final int dclass, final long ttl, final int flags, final int proto, final int alg, final PublicKey key) throws DNSSEC.DNSSECException {
        super(name, 48, dclass, ttl, flags, proto, alg, DNSSEC.fromPublicKey(key, alg));
        this.publicKey = key;
    }
    
    void rdataFromString(final Tokenizer st, final Name origin) throws IOException {
        this.flags = st.getUInt16();
        this.proto = st.getUInt8();
        final String algString = st.getString();
        this.alg = DNSSEC.Algorithm.value(algString);
        if (this.alg < 0) {
            throw st.exception("Invalid algorithm: " + algString);
        }
        this.key = st.getBase64();
    }
    
    public static class Protocol
    {
        public static final int DNSSEC = 3;
        
        private Protocol() {
        }
    }
    
    public static class Flags
    {
        public static final int ZONE_KEY = 256;
        public static final int SEP_KEY = 1;
        public static final int REVOKE = 128;
        
        private Flags() {
        }
    }
}
