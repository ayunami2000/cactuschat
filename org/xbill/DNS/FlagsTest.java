// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import junit.framework.Assert;
import junit.framework.TestCase;

public class FlagsTest extends TestCase
{
    public void test_string() {
        Assert.assertEquals("aa", Flags.string(5));
        Assert.assertTrue(Flags.string(12).startsWith("flag"));
        try {
            Flags.string(-1);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex) {}
        try {
            Flags.string(16);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex2) {}
    }
    
    public void test_value() {
        Assert.assertEquals(11, Flags.value("cd"));
        Assert.assertEquals(13, Flags.value("FLAG13"));
        Assert.assertEquals(-1, Flags.value("FLAG16"));
        Assert.assertEquals(-1, Flags.value("THIS IS DEFINITELY UNKNOWN"));
        Assert.assertEquals(-1, Flags.value(""));
    }
    
    public void test_isFlag() {
        try {
            Flags.isFlag(-1);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex) {}
        Assert.assertTrue(Flags.isFlag(0));
        Assert.assertFalse(Flags.isFlag(1));
        Assert.assertFalse(Flags.isFlag(2));
        Assert.assertFalse(Flags.isFlag(3));
        Assert.assertFalse(Flags.isFlag(4));
        Assert.assertTrue(Flags.isFlag(5));
        Assert.assertTrue(Flags.isFlag(6));
        Assert.assertTrue(Flags.isFlag(7));
        Assert.assertTrue(Flags.isFlag(8));
        Assert.assertTrue(Flags.isFlag(9));
        Assert.assertTrue(Flags.isFlag(10));
        Assert.assertTrue(Flags.isFlag(11));
        Assert.assertFalse(Flags.isFlag(12));
        Assert.assertFalse(Flags.isFlag(13));
        Assert.assertFalse(Flags.isFlag(14));
        Assert.assertFalse(Flags.isFlag(14));
        try {
            Flags.isFlag(16);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex2) {}
    }
}
