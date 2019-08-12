// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

public final class DClass
{
    public static final int IN = 1;
    public static final int CH = 3;
    public static final int CHAOS = 3;
    public static final int HS = 4;
    public static final int HESIOD = 4;
    public static final int NONE = 254;
    public static final int ANY = 255;
    private static Mnemonic classes;
    
    private DClass() {
    }
    
    public static void check(final int i) {
        if (i < 0 || i > 65535) {
            throw new InvalidDClassException(i);
        }
    }
    
    public static String string(final int i) {
        return DClass.classes.getText(i);
    }
    
    public static int value(final String s) {
        return DClass.classes.getValue(s);
    }
    
    static {
        (DClass.classes = new DClassMnemonic()).add(1, "IN");
        DClass.classes.add(3, "CH");
        DClass.classes.addAlias(3, "CHAOS");
        DClass.classes.add(4, "HS");
        DClass.classes.addAlias(4, "HESIOD");
        DClass.classes.add(254, "NONE");
        DClass.classes.add(255, "ANY");
    }
    
    private static class DClassMnemonic extends Mnemonic
    {
        public DClassMnemonic() {
            super("DClass", 2);
            this.setPrefix("CLASS");
        }
        
        public void check(final int val) {
            DClass.check(val);
        }
    }
}
