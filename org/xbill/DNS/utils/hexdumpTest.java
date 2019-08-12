// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS.utils;

import junit.framework.Assert;
import junit.framework.TestCase;

public class hexdumpTest extends TestCase
{
    public hexdumpTest(final String name) {
        super(name);
    }
    
    public void test_shortform() {
        final byte[] data = { 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3 };
        final String desc = "This Is My Description";
        final String long_out = hexdump.dump(desc, data, 0, data.length);
        final String short_out = hexdump.dump(desc, data);
        Assert.assertEquals(long_out, short_out);
    }
    
    public void test_0() {
        final byte[] data = { 1, 0, 2 };
        final String out = hexdump.dump(null, data, 1, 1);
        Assert.assertEquals("1b:\t00 \n", out);
    }
    
    public void test_1() {
        final byte[] data = { 2, 1, 3 };
        final String out = hexdump.dump(null, data, 1, 1);
        Assert.assertEquals("1b:\t01 \n", out);
    }
    
    public void test_2() {
        final byte[] data = { 1, 2, 3 };
        final String out = hexdump.dump(null, data, 1, 1);
        Assert.assertEquals("1b:\t02 \n", out);
    }
    
    public void test_3() {
        final byte[] data = { 1, 3, 2 };
        final String out = hexdump.dump(null, data, 1, 1);
        Assert.assertEquals("1b:\t03 \n", out);
    }
    
    public void test_4() {
        final byte[] data = { 1, 4, 2 };
        final String out = hexdump.dump(null, data, 1, 1);
        Assert.assertEquals("1b:\t04 \n", out);
    }
    
    public void test_5() {
        final byte[] data = { 1, 5, 2 };
        final String out = hexdump.dump(null, data, 1, 1);
        Assert.assertEquals("1b:\t05 \n", out);
    }
    
    public void test_6() {
        final byte[] data = { 1, 6, 2 };
        final String out = hexdump.dump(null, data, 1, 1);
        Assert.assertEquals("1b:\t06 \n", out);
    }
    
    public void test_7() {
        final byte[] data = { 1, 7, 2 };
        final String out = hexdump.dump(null, data, 1, 1);
        Assert.assertEquals("1b:\t07 \n", out);
    }
    
    public void test_8() {
        final byte[] data = { 1, 8, 2 };
        final String out = hexdump.dump(null, data, 1, 1);
        Assert.assertEquals("1b:\t08 \n", out);
    }
    
    public void test_9() {
        final byte[] data = { 1, 9, 2 };
        final String out = hexdump.dump(null, data, 1, 1);
        Assert.assertEquals("1b:\t09 \n", out);
    }
    
    public void test_10() {
        final byte[] data = { 1, 10, 2 };
        final String out = hexdump.dump(null, data, 1, 1);
        Assert.assertEquals("1b:\t0A \n", out);
    }
    
    public void test_11() {
        final byte[] data = { 1, 11, 2 };
        final String out = hexdump.dump(null, data, 1, 1);
        Assert.assertEquals("1b:\t0B \n", out);
    }
    
    public void test_12() {
        final byte[] data = { 1, 12, 2 };
        final String out = hexdump.dump(null, data, 1, 1);
        Assert.assertEquals("1b:\t0C \n", out);
    }
    
    public void test_13() {
        final byte[] data = { 1, 13, 2 };
        final String out = hexdump.dump(null, data, 1, 1);
        Assert.assertEquals("1b:\t0D \n", out);
    }
    
    public void test_14() {
        final byte[] data = { 1, 14, 2 };
        final String out = hexdump.dump(null, data, 1, 1);
        Assert.assertEquals("1b:\t0E \n", out);
    }
    
    public void test_15() {
        final byte[] data = { 1, 15, 2 };
        final String out = hexdump.dump(null, data, 1, 1);
        Assert.assertEquals("1b:\t0F \n", out);
    }
    
    public void test_default_constructor() {
        new hexdump();
    }
}
