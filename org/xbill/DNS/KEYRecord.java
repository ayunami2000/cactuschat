// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.util.StringTokenizer;
import java.io.IOException;
import java.security.PublicKey;

public class KEYRecord extends KEYBase
{
    private static final long serialVersionUID = 6385613447571488906L;
    public static final int FLAG_NOCONF = 16384;
    public static final int FLAG_NOAUTH = 32768;
    public static final int FLAG_NOKEY = 49152;
    public static final int OWNER_ZONE = 256;
    public static final int OWNER_HOST = 512;
    public static final int OWNER_USER = 0;
    public static final int PROTOCOL_TLS = 1;
    public static final int PROTOCOL_EMAIL = 2;
    public static final int PROTOCOL_DNSSEC = 3;
    public static final int PROTOCOL_IPSEC = 4;
    public static final int PROTOCOL_ANY = 255;
    
    KEYRecord() {
    }
    
    Record getObject() {
        return new KEYRecord();
    }
    
    public KEYRecord(final Name name, final int dclass, final long ttl, final int flags, final int proto, final int alg, final byte[] key) {
        super(name, 25, dclass, ttl, flags, proto, alg, key);
    }
    
    public KEYRecord(final Name name, final int dclass, final long ttl, final int flags, final int proto, final int alg, final PublicKey key) throws DNSSEC.DNSSECException {
        super(name, 25, dclass, ttl, flags, proto, alg, DNSSEC.fromPublicKey(key, alg));
        this.publicKey = key;
    }
    
    void rdataFromString(final Tokenizer st, final Name origin) throws IOException {
        final String flagString = st.getIdentifier();
        this.flags = Flags.value(flagString);
        if (this.flags < 0) {
            throw st.exception("Invalid flags: " + flagString);
        }
        final String protoString = st.getIdentifier();
        this.proto = Protocol.value(protoString);
        if (this.proto < 0) {
            throw st.exception("Invalid protocol: " + protoString);
        }
        final String algString = st.getIdentifier();
        this.alg = DNSSEC.Algorithm.value(algString);
        if (this.alg < 0) {
            throw st.exception("Invalid algorithm: " + algString);
        }
        if ((this.flags & 0xC000) == 0xC000) {
            this.key = null;
        }
        else {
            this.key = st.getBase64();
        }
    }
    
    public static class Protocol
    {
        public static final int NONE = 0;
        public static final int TLS = 1;
        public static final int EMAIL = 2;
        public static final int DNSSEC = 3;
        public static final int IPSEC = 4;
        public static final int ANY = 255;
        private static Mnemonic protocols;
        
        private Protocol() {
        }
        
        public static String string(final int type) {
            return Protocol.protocols.getText(type);
        }
        
        public static int value(final String s) {
            return Protocol.protocols.getValue(s);
        }
        
        static {
            (Protocol.protocols = new Mnemonic("KEY protocol", 2)).setMaximum(255);
            Protocol.protocols.setNumericAllowed(true);
            Protocol.protocols.add(0, "NONE");
            Protocol.protocols.add(1, "TLS");
            Protocol.protocols.add(2, "EMAIL");
            Protocol.protocols.add(3, "DNSSEC");
            Protocol.protocols.add(4, "IPSEC");
            Protocol.protocols.add(255, "ANY");
        }
    }
    
    public static class Flags
    {
        public static final int NOCONF = 16384;
        public static final int NOAUTH = 32768;
        public static final int NOKEY = 49152;
        public static final int USE_MASK = 49152;
        public static final int FLAG2 = 8192;
        public static final int EXTEND = 4096;
        public static final int FLAG4 = 2048;
        public static final int FLAG5 = 1024;
        public static final int USER = 0;
        public static final int ZONE = 256;
        public static final int HOST = 512;
        public static final int NTYP3 = 768;
        public static final int OWNER_MASK = 768;
        public static final int FLAG8 = 128;
        public static final int FLAG9 = 64;
        public static final int FLAG10 = 32;
        public static final int FLAG11 = 16;
        public static final int SIG0 = 0;
        public static final int SIG1 = 1;
        public static final int SIG2 = 2;
        public static final int SIG3 = 3;
        public static final int SIG4 = 4;
        public static final int SIG5 = 5;
        public static final int SIG6 = 6;
        public static final int SIG7 = 7;
        public static final int SIG8 = 8;
        public static final int SIG9 = 9;
        public static final int SIG10 = 10;
        public static final int SIG11 = 11;
        public static final int SIG12 = 12;
        public static final int SIG13 = 13;
        public static final int SIG14 = 14;
        public static final int SIG15 = 15;
        private static Mnemonic flags;
        
        private Flags() {
        }
        
        public static int value(final String s) {
            try {
                final int value = Integer.parseInt(s);
                if (value >= 0 && value <= 65535) {
                    return value;
                }
                return -1;
            }
            catch (NumberFormatException e) {
                final StringTokenizer st = new StringTokenizer(s, "|");
                int value = 0;
                while (st.hasMoreTokens()) {
                    final int val = Flags.flags.getValue(st.nextToken());
                    if (val < 0) {
                        return -1;
                    }
                    value |= val;
                }
                return value;
            }
        }
        
        static {
            (Flags.flags = new Mnemonic("KEY flags", 2)).setMaximum(65535);
            Flags.flags.setNumericAllowed(false);
            Flags.flags.add(16384, "NOCONF");
            Flags.flags.add(32768, "NOAUTH");
            Flags.flags.add(49152, "NOKEY");
            Flags.flags.add(8192, "FLAG2");
            Flags.flags.add(4096, "EXTEND");
            Flags.flags.add(2048, "FLAG4");
            Flags.flags.add(1024, "FLAG5");
            Flags.flags.add(0, "USER");
            Flags.flags.add(256, "ZONE");
            Flags.flags.add(512, "HOST");
            Flags.flags.add(768, "NTYP3");
            Flags.flags.add(128, "FLAG8");
            Flags.flags.add(64, "FLAG9");
            Flags.flags.add(32, "FLAG10");
            Flags.flags.add(16, "FLAG11");
            Flags.flags.add(0, "SIG0");
            Flags.flags.add(1, "SIG1");
            Flags.flags.add(2, "SIG2");
            Flags.flags.add(3, "SIG3");
            Flags.flags.add(4, "SIG4");
            Flags.flags.add(5, "SIG5");
            Flags.flags.add(6, "SIG6");
            Flags.flags.add(7, "SIG7");
            Flags.flags.add(8, "SIG8");
            Flags.flags.add(9, "SIG9");
            Flags.flags.add(10, "SIG10");
            Flags.flags.add(11, "SIG11");
            Flags.flags.add(12, "SIG12");
            Flags.flags.add(13, "SIG13");
            Flags.flags.add(14, "SIG14");
            Flags.flags.add(15, "SIG15");
        }
    }
}
