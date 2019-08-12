// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.io.IOException;
import junit.framework.Assert;
import java.util.Arrays;
import junit.framework.TestCase;

public class SingleCompressedNameBaseTest extends TestCase
{
    private void assertEquals(final byte[] exp, final byte[] act) {
        Assert.assertTrue(Arrays.equals(exp, act));
    }
    
    public void test_ctor() throws TextParseException {
        TestClass tc = new TestClass();
        Assert.assertNull(tc.getSingleName());
        final Name n = Name.fromString("my.name.");
        final Name sn = Name.fromString("my.single.name.");
        tc = new TestClass(n, 1, 1, 100L, sn, "The Description");
        Assert.assertSame(n, tc.getName());
        Assert.assertEquals(1, tc.getType());
        Assert.assertEquals(1, tc.getDClass());
        Assert.assertEquals(100L, tc.getTTL());
        Assert.assertSame(sn, tc.getSingleName());
    }
    
    public void test_rrToWire() throws IOException, TextParseException {
        final Name n = Name.fromString("my.name.");
        final Name sn = Name.fromString("My.Single.Name.");
        TestClass tc = new TestClass(n, 1, 1, 100L, sn, "The Description");
        byte[] exp = { 2, 77, 121, 6, 83, 105, 110, 103, 108, 101, 4, 78, 97, 109, 101, 0 };
        DNSOutput dout = new DNSOutput();
        tc.rrToWire(dout, null, false);
        byte[] out = dout.toByteArray();
        this.assertEquals(exp, out);
        tc = new TestClass(n, 1, 1, 100L, sn, "The Description");
        exp = new byte[] { 2, 109, 121, 6, 115, 105, 110, 103, 108, 101, 4, 110, 97, 109, 101, 0 };
        dout = new DNSOutput();
        tc.rrToWire(dout, null, true);
        out = dout.toByteArray();
        this.assertEquals(exp, out);
    }
    
    private static class TestClass extends SingleCompressedNameBase
    {
        public TestClass() {
        }
        
        public TestClass(final Name name, final int type, final int dclass, final long ttl, final Name singleName, final String desc) {
            super(name, type, dclass, ttl, singleName, desc);
        }
        
        public Name getSingleName() {
            return super.getSingleName();
        }
        
        public Record getObject() {
            return null;
        }
    }
}
