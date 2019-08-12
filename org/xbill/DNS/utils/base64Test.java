// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS.utils;

import junit.framework.Assert;
import junit.framework.TestCase;

public class base64Test extends TestCase
{
    public base64Test(final String name) {
        super(name);
    }
    
    public void test_toString_empty() {
        final byte[] data = new byte[0];
        final String out = base64.toString(data);
        Assert.assertEquals("", out);
    }
    
    public void test_toString_basic1() {
        final byte[] data = { 0 };
        final String out = base64.toString(data);
        Assert.assertEquals("AA==", out);
    }
    
    public void test_toString_basic2() {
        final byte[] data = { 0, 0 };
        final String out = base64.toString(data);
        Assert.assertEquals("AAA=", out);
    }
    
    public void test_toString_basic3() {
        final byte[] data = { 0, 0, 1 };
        final String out = base64.toString(data);
        Assert.assertEquals("AAAB", out);
    }
    
    public void test_toString_basic4() {
        final byte[] data = { -4, 0, 0 };
        final String out = base64.toString(data);
        Assert.assertEquals("/AAA", out);
    }
    
    public void test_toString_basic5() {
        final byte[] data = { -1, -1, -1 };
        final String out = base64.toString(data);
        Assert.assertEquals("////", out);
    }
    
    public void test_toString_basic6() {
        final byte[] data = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        final String out = base64.toString(data);
        Assert.assertEquals("AQIDBAUGBwgJ", out);
    }
    
    public void test_formatString_empty1() {
        final String out = base64.formatString(new byte[0], 5, "", false);
        Assert.assertEquals("", out);
    }
    
    public void test_formatString_shorter() {
        final byte[] in = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        final String out = base64.formatString(in, 13, "", false);
        Assert.assertEquals("AQIDBAUGBwgJ", out);
    }
    
    public void test_formatString_sameLength() {
        final byte[] in = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        final String out = base64.formatString(in, 12, "", false);
        Assert.assertEquals("AQIDBAUGBwgJ", out);
    }
    
    public void test_formatString_oneBreak() {
        final byte[] in = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        final String out = base64.formatString(in, 10, "", false);
        Assert.assertEquals("AQIDBAUGBw\ngJ", out);
    }
    
    public void test_formatString_twoBreaks1() {
        final byte[] in = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        final String out = base64.formatString(in, 5, "", false);
        Assert.assertEquals("AQIDB\nAUGBw\ngJ", out);
    }
    
    public void test_formatString_twoBreaks2() {
        final byte[] in = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        final String out = base64.formatString(in, 4, "", false);
        Assert.assertEquals("AQID\nBAUG\nBwgJ", out);
    }
    
    public void test_formatString_shorterWithPrefix() {
        final byte[] in = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        final String out = base64.formatString(in, 13, "!_", false);
        Assert.assertEquals("!_AQIDBAUGBwgJ", out);
    }
    
    public void test_formatString_sameLengthWithPrefix() {
        final byte[] in = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        final String out = base64.formatString(in, 12, "!_", false);
        Assert.assertEquals("!_AQIDBAUGBwgJ", out);
    }
    
    public void test_formatString_oneBreakWithPrefix() {
        final byte[] in = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        final String out = base64.formatString(in, 10, "!_", false);
        Assert.assertEquals("!_AQIDBAUGBw\n!_gJ", out);
    }
    
    public void test_formatString_twoBreaks1WithPrefix() {
        final byte[] in = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        final String out = base64.formatString(in, 5, "!_", false);
        Assert.assertEquals("!_AQIDB\n!_AUGBw\n!_gJ", out);
    }
    
    public void test_formatString_twoBreaks2WithPrefix() {
        final byte[] in = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        final String out = base64.formatString(in, 4, "!_", false);
        Assert.assertEquals("!_AQID\n!_BAUG\n!_BwgJ", out);
    }
    
    public void test_formatString_shorterWithPrefixAndClose() {
        final byte[] in = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        final String out = base64.formatString(in, 13, "!_", true);
        Assert.assertEquals("!_AQIDBAUGBwgJ )", out);
    }
    
    public void test_formatString_sameLengthWithPrefixAndClose() {
        final byte[] in = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        final String out = base64.formatString(in, 12, "!_", true);
        Assert.assertEquals("!_AQIDBAUGBwgJ )", out);
    }
    
    public void test_formatString_oneBreakWithPrefixAndClose() {
        final byte[] in = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        final String out = base64.formatString(in, 10, "!_", true);
        Assert.assertEquals("!_AQIDBAUGBw\n!_gJ )", out);
    }
    
    public void test_formatString_twoBreaks1WithPrefixAndClose() {
        final byte[] in = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        final String out = base64.formatString(in, 5, "!_", true);
        Assert.assertEquals("!_AQIDB\n!_AUGBw\n!_gJ )", out);
    }
    
    public void test_formatString_twoBreaks2WithPrefixAndClose() {
        final byte[] in = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        final String out = base64.formatString(in, 4, "!_", true);
        Assert.assertEquals("!_AQID\n!_BAUG\n!_BwgJ )", out);
    }
    
    private void assertEquals(final byte[] exp, final byte[] act) {
        Assert.assertEquals(exp.length, act.length);
        for (int i = 0; i < exp.length; ++i) {
            Assert.assertEquals(exp[i], act[i]);
        }
    }
    
    public void test_fromString_empty1() {
        final byte[] data = new byte[0];
        final byte[] out = base64.fromString("");
        this.assertEquals(new byte[0], out);
    }
    
    public void test_fromString_basic1() {
        final byte[] exp = { 0 };
        final byte[] out = base64.fromString("AA==");
        this.assertEquals(exp, out);
    }
    
    public void test_fromString_basic2() {
        final byte[] exp = { 0, 0 };
        final byte[] out = base64.fromString("AAA=");
        this.assertEquals(exp, out);
    }
    
    public void test_fromString_basic3() {
        final byte[] exp = { 0, 0, 1 };
        final byte[] out = base64.fromString("AAAB");
        this.assertEquals(exp, out);
    }
    
    public void test_fromString_basic4() {
        final byte[] exp = { -4, 0, 0 };
        final byte[] out = base64.fromString("/AAA");
        this.assertEquals(exp, out);
    }
    
    public void test_fromString_basic5() {
        final byte[] exp = { -1, -1, -1 };
        final byte[] out = base64.fromString("////");
        this.assertEquals(exp, out);
    }
    
    public void test_fromString_basic6() {
        final byte[] exp = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        final byte[] out = base64.fromString("AQIDBAUGBwgJ");
        this.assertEquals(exp, out);
    }
    
    public void test_fromString_invalid1() {
        final byte[] out = base64.fromString("AAA");
        Assert.assertNull(out);
    }
    
    public void test_fromString_invalid2() {
        final byte[] out = base64.fromString("AA");
        Assert.assertNull(out);
    }
    
    public void test_fromString_invalid3() {
        final byte[] out = base64.fromString("A");
        Assert.assertNull(out);
    }
    
    public void test_fromString_invalid4() {
        final byte[] out = base64.fromString("BB==");
        Assert.assertNull(out);
    }
    
    public void test_fromString_invalid5() {
        final byte[] out = base64.fromString("BBB=");
        Assert.assertNull(out);
    }
}
