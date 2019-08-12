// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

public final class Flags
{
    private static Mnemonic flags;
    public static final byte QR = 0;
    public static final byte AA = 5;
    public static final byte TC = 6;
    public static final byte RD = 7;
    public static final byte RA = 8;
    public static final byte AD = 10;
    public static final byte CD = 11;
    public static final int DO = 32768;
    
    private Flags() {
    }
    
    public static String string(final int i) {
        return Flags.flags.getText(i);
    }
    
    public static int value(final String s) {
        return Flags.flags.getValue(s);
    }
    
    public static boolean isFlag(final int index) {
        Flags.flags.check(index);
        return (index < 1 || index > 4) && index < 12;
    }
    
    static {
        (Flags.flags = new Mnemonic("DNS Header Flag", 3)).setMaximum(15);
        Flags.flags.setPrefix("FLAG");
        Flags.flags.setNumericAllowed(true);
        Flags.flags.add(0, "qr");
        Flags.flags.add(5, "aa");
        Flags.flags.add(6, "tc");
        Flags.flags.add(7, "rd");
        Flags.flags.add(8, "ra");
        Flags.flags.add(10, "ad");
        Flags.flags.add(11, "cd");
    }
}
