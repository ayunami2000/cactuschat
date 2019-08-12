// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import junit.framework.Assert;
import junit.framework.TestCase;

public class RcodeTest extends TestCase
{
    public void test_string() {
        Assert.assertEquals("NXDOMAIN", Rcode.string(3));
        Assert.assertEquals("NOTIMP", Rcode.string(4));
        Assert.assertTrue(Rcode.string(20).startsWith("RESERVED"));
        try {
            Rcode.string(-1);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex) {}
        try {
            Rcode.string(4096);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex2) {}
    }
    
    public void test_TSIGstring() {
        Assert.assertEquals("BADSIG", Rcode.TSIGstring(16));
        Assert.assertTrue(Rcode.TSIGstring(20).startsWith("RESERVED"));
        try {
            Rcode.TSIGstring(-1);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex) {}
        try {
            Rcode.string(65536);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex2) {}
    }
    
    public void test_value() {
        Assert.assertEquals(1, Rcode.value("FORMERR"));
        Assert.assertEquals(4, Rcode.value("NOTIMP"));
        Assert.assertEquals(4, Rcode.value("NOTIMPL"));
        Assert.assertEquals(35, Rcode.value("RESERVED35"));
        Assert.assertEquals(-1, Rcode.value("RESERVED4096"));
        Assert.assertEquals(-1, Rcode.value("THIS IS DEFINITELY UNKNOWN"));
        Assert.assertEquals(-1, Rcode.value(""));
    }
}
