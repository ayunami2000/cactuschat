// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.dns;

public final class DnsResponseCode implements Comparable<DnsResponseCode>
{
    public static final DnsResponseCode NOERROR;
    public static final DnsResponseCode FORMERROR;
    public static final DnsResponseCode SERVFAIL;
    public static final DnsResponseCode NXDOMAIN;
    public static final DnsResponseCode NOTIMPL;
    public static final DnsResponseCode REFUSED;
    public static final DnsResponseCode YXDOMAIN;
    public static final DnsResponseCode YXRRSET;
    public static final DnsResponseCode NXRRSET;
    public static final DnsResponseCode NOTAUTH;
    public static final DnsResponseCode NOTZONE;
    public static final DnsResponseCode BADVERS;
    public static final DnsResponseCode BADSIG;
    public static final DnsResponseCode BADKEY;
    public static final DnsResponseCode BADTIME;
    private final int errorCode;
    private final String message;
    
    public static DnsResponseCode valueOf(final int responseCode) {
        switch (responseCode) {
            case 0: {
                return DnsResponseCode.NOERROR;
            }
            case 1: {
                return DnsResponseCode.FORMERROR;
            }
            case 2: {
                return DnsResponseCode.SERVFAIL;
            }
            case 3: {
                return DnsResponseCode.NXDOMAIN;
            }
            case 4: {
                return DnsResponseCode.NOTIMPL;
            }
            case 5: {
                return DnsResponseCode.REFUSED;
            }
            case 6: {
                return DnsResponseCode.YXDOMAIN;
            }
            case 7: {
                return DnsResponseCode.YXRRSET;
            }
            case 8: {
                return DnsResponseCode.NXRRSET;
            }
            case 9: {
                return DnsResponseCode.NOTAUTH;
            }
            case 10: {
                return DnsResponseCode.NOTZONE;
            }
            case 11: {
                return DnsResponseCode.BADVERS;
            }
            case 12: {
                return DnsResponseCode.BADSIG;
            }
            case 13: {
                return DnsResponseCode.BADKEY;
            }
            case 14: {
                return DnsResponseCode.BADTIME;
            }
            default: {
                return new DnsResponseCode(responseCode, null);
            }
        }
    }
    
    public DnsResponseCode(final int errorCode, final String message) {
        this.errorCode = errorCode;
        this.message = message;
    }
    
    public int code() {
        return this.errorCode;
    }
    
    @Override
    public int compareTo(final DnsResponseCode o) {
        return this.code() - o.code();
    }
    
    @Override
    public int hashCode() {
        return this.code();
    }
    
    @Override
    public boolean equals(final Object o) {
        return o instanceof DnsResponseCode && this.code() == ((DnsResponseCode)o).code();
    }
    
    @Override
    public String toString() {
        if (this.message == null) {
            return "DnsResponseCode(" + this.errorCode + ')';
        }
        return "DnsResponseCode(" + this.errorCode + ", " + this.message + ')';
    }
    
    static {
        NOERROR = new DnsResponseCode(0, "no error");
        FORMERROR = new DnsResponseCode(1, "format error");
        SERVFAIL = new DnsResponseCode(2, "server failure");
        NXDOMAIN = new DnsResponseCode(3, "name error");
        NOTIMPL = new DnsResponseCode(4, "not implemented");
        REFUSED = new DnsResponseCode(5, "operation refused");
        YXDOMAIN = new DnsResponseCode(6, "domain name should not exist");
        YXRRSET = new DnsResponseCode(7, "resource record set should not exist");
        NXRRSET = new DnsResponseCode(8, "rrset does not exist");
        NOTAUTH = new DnsResponseCode(9, "not authoritative for zone");
        NOTZONE = new DnsResponseCode(10, "name not in zone");
        BADVERS = new DnsResponseCode(11, "bad extension mechanism for version");
        BADSIG = new DnsResponseCode(12, "bad signature");
        BADKEY = new DnsResponseCode(13, "bad key");
        BADTIME = new DnsResponseCode(14, "bad timestamp");
    }
}
