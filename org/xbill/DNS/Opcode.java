// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

public final class Opcode
{
    public static final int QUERY = 0;
    public static final int IQUERY = 1;
    public static final int STATUS = 2;
    public static final int NOTIFY = 4;
    public static final int UPDATE = 5;
    private static Mnemonic opcodes;
    
    private Opcode() {
    }
    
    public static String string(final int i) {
        return Opcode.opcodes.getText(i);
    }
    
    public static int value(final String s) {
        return Opcode.opcodes.getValue(s);
    }
    
    static {
        (Opcode.opcodes = new Mnemonic("DNS Opcode", 2)).setMaximum(15);
        Opcode.opcodes.setPrefix("RESERVED");
        Opcode.opcodes.setNumericAllowed(true);
        Opcode.opcodes.add(0, "QUERY");
        Opcode.opcodes.add(1, "IQUERY");
        Opcode.opcodes.add(2, "STATUS");
        Opcode.opcodes.add(4, "NOTIFY");
        Opcode.opcodes.add(5, "UPDATE");
    }
}
