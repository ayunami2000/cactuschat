// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.util.HashMap;

public final class Type
{
    public static final int A = 1;
    public static final int NS = 2;
    public static final int MD = 3;
    public static final int MF = 4;
    public static final int CNAME = 5;
    public static final int SOA = 6;
    public static final int MB = 7;
    public static final int MG = 8;
    public static final int MR = 9;
    public static final int NULL = 10;
    public static final int WKS = 11;
    public static final int PTR = 12;
    public static final int HINFO = 13;
    public static final int MINFO = 14;
    public static final int MX = 15;
    public static final int TXT = 16;
    public static final int RP = 17;
    public static final int AFSDB = 18;
    public static final int X25 = 19;
    public static final int ISDN = 20;
    public static final int RT = 21;
    public static final int NSAP = 22;
    public static final int NSAP_PTR = 23;
    public static final int SIG = 24;
    public static final int KEY = 25;
    public static final int PX = 26;
    public static final int GPOS = 27;
    public static final int AAAA = 28;
    public static final int LOC = 29;
    public static final int NXT = 30;
    public static final int EID = 31;
    public static final int NIMLOC = 32;
    public static final int SRV = 33;
    public static final int ATMA = 34;
    public static final int NAPTR = 35;
    public static final int KX = 36;
    public static final int CERT = 37;
    public static final int A6 = 38;
    public static final int DNAME = 39;
    public static final int OPT = 41;
    public static final int APL = 42;
    public static final int DS = 43;
    public static final int SSHFP = 44;
    public static final int IPSECKEY = 45;
    public static final int RRSIG = 46;
    public static final int NSEC = 47;
    public static final int DNSKEY = 48;
    public static final int DHCID = 49;
    public static final int NSEC3 = 50;
    public static final int NSEC3PARAM = 51;
    public static final int SPF = 99;
    public static final int TKEY = 249;
    public static final int TSIG = 250;
    public static final int IXFR = 251;
    public static final int AXFR = 252;
    public static final int MAILB = 253;
    public static final int MAILA = 254;
    public static final int ANY = 255;
    public static final int DLV = 32769;
    private static TypeMnemonic types;
    
    private Type() {
    }
    
    public static void check(final int val) {
        if (val < 0 || val > 65535) {
            throw new InvalidTypeException(val);
        }
    }
    
    public static String string(final int val) {
        return Type.types.getText(val);
    }
    
    public static int value(final String s, final boolean numberok) {
        int val = Type.types.getValue(s);
        if (val == -1 && numberok) {
            val = Type.types.getValue("TYPE" + s);
        }
        return val;
    }
    
    public static int value(final String s) {
        return value(s, false);
    }
    
    static Record getProto(final int val) {
        return Type.types.getProto(val);
    }
    
    public static boolean isRR(final int type) {
        switch (type) {
            case 41:
            case 249:
            case 250:
            case 251:
            case 252:
            case 253:
            case 254:
            case 255: {
                return false;
            }
            default: {
                return true;
            }
        }
    }
    
    static {
        (Type.types = new TypeMnemonic()).add(1, "A", new ARecord());
        Type.types.add(2, "NS", new NSRecord());
        Type.types.add(3, "MD", new MDRecord());
        Type.types.add(4, "MF", new MFRecord());
        Type.types.add(5, "CNAME", new CNAMERecord());
        Type.types.add(6, "SOA", new SOARecord());
        Type.types.add(7, "MB", new MBRecord());
        Type.types.add(8, "MG", new MGRecord());
        Type.types.add(9, "MR", new MRRecord());
        Type.types.add(10, "NULL", new NULLRecord());
        Type.types.add(11, "WKS", new WKSRecord());
        Type.types.add(12, "PTR", new PTRRecord());
        Type.types.add(13, "HINFO", new HINFORecord());
        Type.types.add(14, "MINFO", new MINFORecord());
        Type.types.add(15, "MX", new MXRecord());
        Type.types.add(16, "TXT", new TXTRecord());
        Type.types.add(17, "RP", new RPRecord());
        Type.types.add(18, "AFSDB", new AFSDBRecord());
        Type.types.add(19, "X25", new X25Record());
        Type.types.add(20, "ISDN", new ISDNRecord());
        Type.types.add(21, "RT", new RTRecord());
        Type.types.add(22, "NSAP", new NSAPRecord());
        Type.types.add(23, "NSAP-PTR", new NSAP_PTRRecord());
        Type.types.add(24, "SIG", new SIGRecord());
        Type.types.add(25, "KEY", new KEYRecord());
        Type.types.add(26, "PX", new PXRecord());
        Type.types.add(27, "GPOS", new GPOSRecord());
        Type.types.add(28, "AAAA", new AAAARecord());
        Type.types.add(29, "LOC", new LOCRecord());
        Type.types.add(30, "NXT", new NXTRecord());
        Type.types.add(31, "EID");
        Type.types.add(32, "NIMLOC");
        Type.types.add(33, "SRV", new SRVRecord());
        Type.types.add(34, "ATMA");
        Type.types.add(35, "NAPTR", new NAPTRRecord());
        Type.types.add(36, "KX", new KXRecord());
        Type.types.add(37, "CERT", new CERTRecord());
        Type.types.add(38, "A6", new A6Record());
        Type.types.add(39, "DNAME", new DNAMERecord());
        Type.types.add(41, "OPT", new OPTRecord());
        Type.types.add(42, "APL", new APLRecord());
        Type.types.add(43, "DS", new DSRecord());
        Type.types.add(44, "SSHFP", new SSHFPRecord());
        Type.types.add(45, "IPSECKEY", new IPSECKEYRecord());
        Type.types.add(46, "RRSIG", new RRSIGRecord());
        Type.types.add(47, "NSEC", new NSECRecord());
        Type.types.add(48, "DNSKEY", new DNSKEYRecord());
        Type.types.add(49, "DHCID", new DHCIDRecord());
        Type.types.add(50, "NSEC3", new NSEC3Record());
        Type.types.add(51, "NSEC3PARAM", new NSEC3PARAMRecord());
        Type.types.add(99, "SPF", new SPFRecord());
        Type.types.add(249, "TKEY", new TKEYRecord());
        Type.types.add(250, "TSIG", new TSIGRecord());
        Type.types.add(251, "IXFR");
        Type.types.add(252, "AXFR");
        Type.types.add(253, "MAILB");
        Type.types.add(254, "MAILA");
        Type.types.add(255, "ANY");
        Type.types.add(32769, "DLV", new DLVRecord());
    }
    
    private static class TypeMnemonic extends Mnemonic
    {
        private HashMap objects;
        
        public TypeMnemonic() {
            super("Type", 2);
            this.setPrefix("TYPE");
            this.objects = new HashMap();
        }
        
        public void add(final int val, final String str, final Record proto) {
            super.add(val, str);
            this.objects.put(Mnemonic.toInteger(val), proto);
        }
        
        public void check(final int val) {
            Type.check(val);
        }
        
        public Record getProto(final int val) {
            this.check(val);
            return this.objects.get(Mnemonic.toInteger(val));
        }
    }
}
