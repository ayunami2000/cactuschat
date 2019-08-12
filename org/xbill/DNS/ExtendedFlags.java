// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

public final class ExtendedFlags
{
    private static Mnemonic extflags;
    public static final int DO = 32768;
    
    private ExtendedFlags() {
    }
    
    public static String string(final int i) {
        return ExtendedFlags.extflags.getText(i);
    }
    
    public static int value(final String s) {
        return ExtendedFlags.extflags.getValue(s);
    }
    
    static {
        (ExtendedFlags.extflags = new Mnemonic("EDNS Flag", 3)).setMaximum(65535);
        ExtendedFlags.extflags.setPrefix("FLAG");
        ExtendedFlags.extflags.setNumericAllowed(true);
        ExtendedFlags.extflags.add(32768, "do");
    }
}
