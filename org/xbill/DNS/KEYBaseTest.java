// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import org.xbill.DNS.utils.base64;
import java.io.IOException;
import java.util.Arrays;
import junit.framework.Assert;
import junit.framework.TestCase;

public class KEYBaseTest extends TestCase
{
    public void test_ctor() throws TextParseException {
        TestClass tc = new TestClass();
        Assert.assertEquals(0, tc.getFlags());
        Assert.assertEquals(0, tc.getProtocol());
        Assert.assertEquals(0, tc.getAlgorithm());
        Assert.assertNull(tc.getKey());
        final Name n = Name.fromString("my.name.");
        final byte[] key = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 };
        tc = new TestClass(n, 25, 1, 100L, 255, 15, 14, key);
        Assert.assertSame(n, tc.getName());
        Assert.assertEquals(25, tc.getType());
        Assert.assertEquals(1, tc.getDClass());
        Assert.assertEquals(100L, tc.getTTL());
        Assert.assertEquals(255, tc.getFlags());
        Assert.assertEquals(15, tc.getProtocol());
        Assert.assertEquals(14, tc.getAlgorithm());
        Assert.assertTrue(Arrays.equals(key, tc.getKey()));
    }
    
    public void test_rrFromWire() throws IOException {
        byte[] raw = { -85, -51, -17, 25, 1, 2, 3, 4, 5 };
        DNSInput in = new DNSInput(raw);
        TestClass tc = new TestClass();
        tc.rrFromWire(in);
        Assert.assertEquals(43981, tc.getFlags());
        Assert.assertEquals(239, tc.getProtocol());
        Assert.assertEquals(25, tc.getAlgorithm());
        Assert.assertTrue(Arrays.equals(new byte[] { 1, 2, 3, 4, 5 }, tc.getKey()));
        raw = new byte[] { -70, -38, -1, 40 };
        in = new DNSInput(raw);
        tc = new TestClass();
        tc.rrFromWire(in);
        Assert.assertEquals(47834, tc.getFlags());
        Assert.assertEquals(255, tc.getProtocol());
        Assert.assertEquals(40, tc.getAlgorithm());
        Assert.assertNull(tc.getKey());
    }
    
    public void test_rrToString() throws IOException, TextParseException {
        final Name n = Name.fromString("my.name.");
        final byte[] key = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 };
        TestClass tc = new TestClass(n, 25, 1, 100L, 255, 15, 14, null);
        String out = tc.rrToString();
        Assert.assertEquals("255 15 14", out);
        tc = new TestClass(n, 25, 1, 100L, 255, 15, 14, key);
        out = tc.rrToString();
        Assert.assertEquals("255 15 14 " + base64.toString(key), out);
        Options.set("multiline");
        out = tc.rrToString();
        Assert.assertEquals("255 15 14 (\n\t" + base64.toString(key) + " ) ; key_tag = 18509", out);
        Options.unset("multiline");
    }
    
    public void test_getFootprint() throws TextParseException {
        final Name n = Name.fromString("my.name.");
        final byte[] key = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 };
        TestClass tc = new TestClass(n, 25, 1, 100L, 255, 15, 1, key);
        int foot = tc.getFootprint();
        Assert.assertEquals(3342, foot);
        Assert.assertEquals(foot, tc.getFootprint());
        tc = new TestClass(n, 25, 1, 100L, 35243, 205, 239, new byte[] { 18, 52, 86 });
        foot = tc.getFootprint();
        Assert.assertEquals(49103, foot);
        Assert.assertEquals(foot, tc.getFootprint());
        tc = new TestClass();
        Assert.assertEquals(0, tc.getFootprint());
    }
    
    public void test_rrToWire() throws IOException, TextParseException {
        final Name n = Name.fromString("my.name.");
        final byte[] key = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 };
        final TestClass tc = new TestClass(n, 25, 1, 100L, 30345, 171, 205, key);
        final byte[] exp = { 118, -119, -85, -51, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 };
        DNSOutput o = new DNSOutput();
        tc.rrToWire(o, null, true);
        Assert.assertTrue(Arrays.equals(exp, o.toByteArray()));
        o = new DNSOutput();
        tc.rrToWire(o, null, false);
        Assert.assertTrue(Arrays.equals(exp, o.toByteArray()));
    }
    
    private static class TestClass extends KEYBase
    {
        public TestClass() {
        }
        
        public TestClass(final Name name, final int type, final int dclass, final long ttl, final int flags, final int proto, final int alg, final byte[] key) {
            super(name, type, dclass, ttl, flags, proto, alg, key);
        }
        
        public Record getObject() {
            return null;
        }
        
        void rdataFromString(final Tokenizer st, final Name origin) throws IOException {
        }
    }
}
