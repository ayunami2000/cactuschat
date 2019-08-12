// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

public final class Rcode
{
    private static Mnemonic rcodes;
    private static Mnemonic tsigrcodes;
    public static final int NOERROR = 0;
    public static final int FORMERR = 1;
    public static final int SERVFAIL = 2;
    public static final int NXDOMAIN = 3;
    public static final int NOTIMP = 4;
    public static final int NOTIMPL = 4;
    public static final int REFUSED = 5;
    public static final int YXDOMAIN = 6;
    public static final int YXRRSET = 7;
    public static final int NXRRSET = 8;
    public static final int NOTAUTH = 9;
    public static final int NOTZONE = 10;
    public static final int BADVERS = 16;
    public static final int BADSIG = 16;
    public static final int BADKEY = 17;
    public static final int BADTIME = 18;
    public static final int BADMODE = 19;
    
    private Rcode() {
    }
    
    public static String string(final int i) {
        return Rcode.rcodes.getText(i);
    }
    
    public static String TSIGstring(final int i) {
        return Rcode.tsigrcodes.getText(i);
    }
    
    public static int value(final String s) {
        return Rcode.rcodes.getValue(s);
    }
    
    static {
        Rcode.rcodes = new Mnemonic("DNS Rcode", 2);
        Rcode.tsigrcodes = new Mnemonic("TSIG rcode", 2);
        Rcode.rcodes.setMaximum(4095);
        Rcode.rcodes.setPrefix("RESERVED");
        Rcode.rcodes.setNumericAllowed(true);
        Rcode.rcodes.add(0, "NOERROR");
        Rcode.rcodes.add(1, "FORMERR");
        Rcode.rcodes.add(2, "SERVFAIL");
        Rcode.rcodes.add(3, "NXDOMAIN");
        Rcode.rcodes.add(4, "NOTIMP");
        Rcode.rcodes.addAlias(4, "NOTIMPL");
        Rcode.rcodes.add(5, "REFUSED");
        Rcode.rcodes.add(6, "YXDOMAIN");
        Rcode.rcodes.add(7, "YXRRSET");
        Rcode.rcodes.add(8, "NXRRSET");
        Rcode.rcodes.add(9, "NOTAUTH");
        Rcode.rcodes.add(10, "NOTZONE");
        Rcode.rcodes.add(16, "BADVERS");
        Rcode.tsigrcodes.setMaximum(65535);
        Rcode.tsigrcodes.setPrefix("RESERVED");
        Rcode.tsigrcodes.setNumericAllowed(true);
        Rcode.tsigrcodes.addAll(Rcode.rcodes);
        Rcode.tsigrcodes.add(16, "BADSIG");
        Rcode.tsigrcodes.add(17, "BADKEY");
        Rcode.tsigrcodes.add(18, "BADTIME");
        Rcode.tsigrcodes.add(19, "BADMODE");
    }
}
