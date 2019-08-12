// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS.utils;

import junit.framework.Assert;
import junit.framework.TestCase;

public class base16Test extends TestCase
{
    public base16Test(final String name) {
        super(name);
    }
    
    public void test_toString_emptyArray() {
        final String out = base16.toString(new byte[0]);
        Assert.assertEquals("", out);
    }
    
    public void test_toString_singleByte1() {
        final byte[] data = { 1 };
        final String out = base16.toString(data);
        Assert.assertEquals("01", out);
    }
    
    public void test_toString_singleByte2() {
        final byte[] data = { 16 };
        final String out = base16.toString(data);
        Assert.assertEquals("10", out);
    }
    
    public void test_toString_singleByte3() {
        final byte[] data = { -1 };
        final String out = base16.toString(data);
        Assert.assertEquals("FF", out);
    }
    
    public void test_toString_array1() {
        final byte[] data = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 };
        final String out = base16.toString(data);
        Assert.assertEquals("0102030405060708090A0B0C0D0E0F", out);
    }
    
    public void test_fromString_emptyString() {
        final String data = "";
        final byte[] out = base16.fromString(data);
        Assert.assertEquals(0, out.length);
    }
    
    public void test_fromString_invalidStringLength() {
        final String data = "1";
        final byte[] out = base16.fromString(data);
        Assert.assertNull(out);
    }
    
    public void test_fromString_nonHexChars() {
        final String data = "GG";
        final byte[] out = base16.fromString(data);
    }
    
    public void test_fromString_normal() {
        final String data = "0102030405060708090A0B0C0D0E0F";
        final byte[] out = base16.fromString(data);
        final byte[] exp = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 };
        Assert.assertEquals(exp.length, out.length);
        for (int i = 0; i < exp.length; ++i) {
            Assert.assertEquals(exp[i], out[i]);
        }
    }
}
