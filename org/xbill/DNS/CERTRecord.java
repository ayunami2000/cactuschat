// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import org.xbill.DNS.utils.base64;
import java.io.IOException;

public class CERTRecord extends Record
{
    public static final int PKIX = 1;
    public static final int SPKI = 2;
    public static final int PGP = 3;
    public static final int URI = 253;
    public static final int OID = 254;
    private static final long serialVersionUID = 4763014646517016835L;
    private int certType;
    private int keyTag;
    private int alg;
    private byte[] cert;
    
    CERTRecord() {
    }
    
    Record getObject() {
        return new CERTRecord();
    }
    
    public CERTRecord(final Name name, final int dclass, final long ttl, final int certType, final int keyTag, final int alg, final byte[] cert) {
        super(name, 37, dclass, ttl);
        this.certType = Record.checkU16("certType", certType);
        this.keyTag = Record.checkU16("keyTag", keyTag);
        this.alg = Record.checkU8("alg", alg);
        this.cert = cert;
    }
    
    void rrFromWire(final DNSInput in) throws IOException {
        this.certType = in.readU16();
        this.keyTag = in.readU16();
        this.alg = in.readU8();
        this.cert = in.readByteArray();
    }
    
    void rdataFromString(final Tokenizer st, final Name origin) throws IOException {
        final String certTypeString = st.getString();
        this.certType = CertificateType.value(certTypeString);
        if (this.certType < 0) {
            throw st.exception("Invalid certificate type: " + certTypeString);
        }
        this.keyTag = st.getUInt16();
        final String algString = st.getString();
        this.alg = DNSSEC.Algorithm.value(algString);
        if (this.alg < 0) {
            throw st.exception("Invalid algorithm: " + algString);
        }
        this.cert = st.getBase64();
    }
    
    String rrToString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(this.certType);
        sb.append(" ");
        sb.append(this.keyTag);
        sb.append(" ");
        sb.append(this.alg);
        if (this.cert != null) {
            if (Options.check("multiline")) {
                sb.append(" (\n");
                sb.append(base64.formatString(this.cert, 64, "\t", true));
            }
            else {
                sb.append(" ");
                sb.append(base64.toString(this.cert));
            }
        }
        return sb.toString();
    }
    
    public int getCertType() {
        return this.certType;
    }
    
    public int getKeyTag() {
        return this.keyTag;
    }
    
    public int getAlgorithm() {
        return this.alg;
    }
    
    public byte[] getCert() {
        return this.cert;
    }
    
    void rrToWire(final DNSOutput out, final Compression c, final boolean canonical) {
        out.writeU16(this.certType);
        out.writeU16(this.keyTag);
        out.writeU8(this.alg);
        out.writeByteArray(this.cert);
    }
    
    public static class CertificateType
    {
        public static final int PKIX = 1;
        public static final int SPKI = 2;
        public static final int PGP = 3;
        public static final int IPKIX = 4;
        public static final int ISPKI = 5;
        public static final int IPGP = 6;
        public static final int ACPKIX = 7;
        public static final int IACPKIX = 8;
        public static final int URI = 253;
        public static final int OID = 254;
        private static Mnemonic types;
        
        private CertificateType() {
        }
        
        public static String string(final int type) {
            return CertificateType.types.getText(type);
        }
        
        public static int value(final String s) {
            return CertificateType.types.getValue(s);
        }
        
        static {
            (CertificateType.types = new Mnemonic("Certificate type", 2)).setMaximum(65535);
            CertificateType.types.setNumericAllowed(true);
            CertificateType.types.add(1, "PKIX");
            CertificateType.types.add(2, "SPKI");
            CertificateType.types.add(3, "PGP");
            CertificateType.types.add(1, "IPKIX");
            CertificateType.types.add(2, "ISPKI");
            CertificateType.types.add(3, "IPGP");
            CertificateType.types.add(3, "ACPKIX");
            CertificateType.types.add(3, "IACPKIX");
            CertificateType.types.add(253, "URI");
            CertificateType.types.add(254, "OID");
        }
    }
}
