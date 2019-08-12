// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import junit.framework.Assert;
import junit.framework.TestCase;

public class TypeTest extends TestCase
{
    public void test_string() {
        Assert.assertEquals("CNAME", Type.string(5));
        Assert.assertTrue(Type.string(256).startsWith("TYPE"));
        try {
            Type.string(-1);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex) {}
    }
    
    public void test_value() {
        Assert.assertEquals(253, Type.value("MAILB"));
        Assert.assertEquals(300, Type.value("TYPE300"));
        Assert.assertEquals(-1, Type.value("THIS IS DEFINITELY UNKNOWN"));
        Assert.assertEquals(-1, Type.value(""));
    }
    
    public void test_value_2arg() {
        Assert.assertEquals(301, Type.value("301", true));
    }
    
    public void test_isRR() {
        Assert.assertTrue(Type.isRR(5));
        Assert.assertFalse(Type.isRR(251));
    }
}
