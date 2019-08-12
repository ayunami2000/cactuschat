// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.dns;

import java.util.HashMap;
import io.netty.util.collection.IntObjectHashMap;
import java.util.Map;

public final class DnsType implements Comparable<DnsType>
{
    public static final DnsType A;
    public static final DnsType NS;
    public static final DnsType CNAME;
    public static final DnsType SOA;
    public static final DnsType PTR;
    public static final DnsType MX;
    public static final DnsType TXT;
    public static final DnsType RP;
    public static final DnsType AFSDB;
    public static final DnsType SIG;
    public static final DnsType KEY;
    public static final DnsType AAAA;
    public static final DnsType LOC;
    public static final DnsType SRV;
    public static final DnsType NAPTR;
    public static final DnsType KX;
    public static final DnsType CERT;
    public static final DnsType DNAME;
    public static final DnsType OPT;
    public static final DnsType APL;
    public static final DnsType DS;
    public static final DnsType SSHFP;
    public static final DnsType IPSECKEY;
    public static final DnsType RRSIG;
    public static final DnsType NSEC;
    public static final DnsType DNSKEY;
    public static final DnsType DHCID;
    public static final DnsType NSEC3;
    public static final DnsType NSEC3PARAM;
    public static final DnsType TLSA;
    public static final DnsType HIP;
    public static final DnsType SPF;
    public static final DnsType TKEY;
    public static final DnsType TSIG;
    public static final DnsType IXFR;
    public static final DnsType AXFR;
    public static final DnsType ANY;
    public static final DnsType CAA;
    public static final DnsType TA;
    public static final DnsType DLV;
    private static final Map<String, DnsType> BY_NAME;
    private static final IntObjectHashMap<DnsType> BY_TYPE;
    private static final String EXPECTED;
    private final int intValue;
    private final String name;
    
    public static DnsType valueOf(final int intValue) {
        final DnsType result = DnsType.BY_TYPE.get(intValue);
        if (result == null) {
            return new DnsType(intValue, "UNKNOWN");
        }
        return result;
    }
    
    public static DnsType valueOf(final String name) {
        final DnsType result = DnsType.BY_NAME.get(name);
        if (result == null) {
            throw new IllegalArgumentException("name: " + name + DnsType.EXPECTED);
        }
        return result;
    }
    
    public static DnsType valueOf(final int intValue, final String name) {
        return new DnsType(intValue, name);
    }
    
    private DnsType(final int intValue, final String name) {
        if ((intValue & 0xFFFF) != intValue) {
            throw new IllegalArgumentException("intValue: " + intValue + " (expected: 0 ~ 65535)");
        }
        this.intValue = intValue;
        this.name = name;
    }
    
    public String name() {
        return this.name;
    }
    
    public int intValue() {
        return this.intValue;
    }
    
    @Override
    public int hashCode() {
        return this.intValue;
    }
    
    @Override
    public boolean equals(final Object o) {
        return o instanceof DnsType && ((DnsType)o).intValue == this.intValue;
    }
    
    @Override
    public int compareTo(final DnsType o) {
        return this.intValue() - o.intValue();
    }
    
    @Override
    public String toString() {
        return this.name;
    }
    
    static {
        A = new DnsType(1, "A");
        NS = new DnsType(2, "NS");
        CNAME = new DnsType(5, "CNAME");
        SOA = new DnsType(6, "SOA");
        PTR = new DnsType(12, "PTR");
        MX = new DnsType(15, "MX");
        TXT = new DnsType(16, "TXT");
        RP = new DnsType(17, "RP");
        AFSDB = new DnsType(18, "AFSDB");
        SIG = new DnsType(24, "SIG");
        KEY = new DnsType(25, "KEY");
        AAAA = new DnsType(28, "AAAA");
        LOC = new DnsType(29, "LOC");
        SRV = new DnsType(33, "SRV");
        NAPTR = new DnsType(35, "NAPTR");
        KX = new DnsType(36, "KX");
        CERT = new DnsType(37, "CERT");
        DNAME = new DnsType(39, "DNAME");
        OPT = new DnsType(41, "OPT");
        APL = new DnsType(42, "APL");
        DS = new DnsType(43, "DS");
        SSHFP = new DnsType(44, "SSHFP");
        IPSECKEY = new DnsType(45, "IPSECKEY");
        RRSIG = new DnsType(46, "RRSIG");
        NSEC = new DnsType(47, "NSEC");
        DNSKEY = new DnsType(48, "DNSKEY");
        DHCID = new DnsType(49, "DHCID");
        NSEC3 = new DnsType(50, "NSEC3");
        NSEC3PARAM = new DnsType(51, "NSEC3PARAM");
        TLSA = new DnsType(52, "TLSA");
        HIP = new DnsType(55, "HIP");
        SPF = new DnsType(99, "SPF");
        TKEY = new DnsType(249, "TKEY");
        TSIG = new DnsType(250, "TSIG");
        IXFR = new DnsType(251, "IXFR");
        AXFR = new DnsType(252, "AXFR");
        ANY = new DnsType(255, "ANY");
        CAA = new DnsType(257, "CAA");
        TA = new DnsType(32768, "TA");
        DLV = new DnsType(32769, "DLV");
        BY_NAME = new HashMap<String, DnsType>();
        BY_TYPE = new IntObjectHashMap<DnsType>();
        final DnsType[] all = { DnsType.A, DnsType.NS, DnsType.CNAME, DnsType.SOA, DnsType.PTR, DnsType.MX, DnsType.TXT, DnsType.RP, DnsType.AFSDB, DnsType.SIG, DnsType.KEY, DnsType.AAAA, DnsType.LOC, DnsType.SRV, DnsType.NAPTR, DnsType.KX, DnsType.CERT, DnsType.DNAME, DnsType.OPT, DnsType.APL, DnsType.DS, DnsType.SSHFP, DnsType.IPSECKEY, DnsType.RRSIG, DnsType.NSEC, DnsType.DNSKEY, DnsType.DHCID, DnsType.NSEC3, DnsType.NSEC3PARAM, DnsType.TLSA, DnsType.HIP, DnsType.SPF, DnsType.TKEY, DnsType.TSIG, DnsType.IXFR, DnsType.AXFR, DnsType.ANY, DnsType.CAA, DnsType.TA, DnsType.DLV };
        final StringBuilder expected = new StringBuilder(512);
        expected.append(" (expected: ");
        for (final DnsType type : all) {
            DnsType.BY_NAME.put(type.name(), type);
            DnsType.BY_TYPE.put(type.intValue(), type);
            expected.append(type.name());
            expected.append('(');
            expected.append(type.intValue());
            expected.append("), ");
        }
        expected.setLength(expected.length() - 2);
        expected.append(')');
        EXPECTED = expected.toString();
    }
}
