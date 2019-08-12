// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import junit.framework.Assert;
import junit.framework.TestCase;

public class OpcodeTest extends TestCase
{
    public void test_string() {
        Assert.assertEquals("IQUERY", Opcode.string(1));
        Assert.assertTrue(Opcode.string(6).startsWith("RESERVED"));
        try {
            Opcode.string(-1);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex) {}
        try {
            Opcode.string(16);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex2) {}
    }
    
    public void test_value() {
        Assert.assertEquals(2, Opcode.value("STATUS"));
        Assert.assertEquals(6, Opcode.value("RESERVED6"));
        Assert.assertEquals(-1, Opcode.value("RESERVED16"));
        Assert.assertEquals(-1, Opcode.value("THIS IS DEFINITELY UNKNOWN"));
        Assert.assertEquals(-1, Opcode.value(""));
    }
}
