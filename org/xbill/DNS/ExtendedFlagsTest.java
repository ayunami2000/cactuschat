// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import junit.framework.Assert;
import junit.framework.TestCase;

public class ExtendedFlagsTest extends TestCase
{
    public void test_string() {
        Assert.assertEquals("do", ExtendedFlags.string(32768));
        Assert.assertTrue(ExtendedFlags.string(1).startsWith("flag"));
        try {
            ExtendedFlags.string(-1);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex) {}
        try {
            ExtendedFlags.string(65536);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex2) {}
    }
    
    public void test_value() {
        Assert.assertEquals(32768, ExtendedFlags.value("do"));
        Assert.assertEquals(16, ExtendedFlags.value("FLAG16"));
        Assert.assertEquals(-1, ExtendedFlags.value("FLAG65536"));
        Assert.assertEquals(-1, ExtendedFlags.value("THIS IS DEFINITELY UNKNOWN"));
        Assert.assertEquals(-1, ExtendedFlags.value(""));
    }
}
