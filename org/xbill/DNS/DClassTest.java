// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import junit.framework.Assert;
import junit.framework.TestCase;

public class DClassTest extends TestCase
{
    public void test_string() {
        Assert.assertEquals("IN", DClass.string(1));
        Assert.assertEquals("CH", DClass.string(3));
        Assert.assertTrue(DClass.string(20).startsWith("CLASS"));
        try {
            DClass.string(-1);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex) {}
        try {
            DClass.string(65536);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex2) {}
    }
    
    public void test_value() {
        Assert.assertEquals(254, DClass.value("NONE"));
        Assert.assertEquals(4, DClass.value("HS"));
        Assert.assertEquals(4, DClass.value("HESIOD"));
        Assert.assertEquals(21, DClass.value("CLASS21"));
        Assert.assertEquals(-1, DClass.value("THIS IS DEFINITELY UNKNOWN"));
        Assert.assertEquals(-1, DClass.value(""));
    }
}
