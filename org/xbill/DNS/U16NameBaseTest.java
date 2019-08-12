// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.io.IOException;
import junit.framework.Assert;
import java.util.Arrays;
import junit.framework.TestCase;

public class U16NameBaseTest extends TestCase
{
    private void assertEquals(final byte[] exp, final byte[] act) {
        Assert.assertTrue(Arrays.equals(exp, act));
    }
    
    public void test_ctor_0arg() {
        final TestClass tc = new TestClass();
        Assert.assertNull(tc.getName());
        Assert.assertEquals(0, tc.getType());
        Assert.assertEquals(0, tc.getDClass());
        Assert.assertEquals(0L, tc.getTTL());
        Assert.assertEquals(0, tc.getU16Field());
        Assert.assertNull(tc.getNameField());
    }
    
    public void test_ctor_4arg() throws TextParseException {
        final Name n = Name.fromString("My.Name.");
        final TestClass tc = new TestClass(n, 15, 1, 48346L);
        Assert.assertSame(n, tc.getName());
        Assert.assertEquals(15, tc.getType());
        Assert.assertEquals(1, tc.getDClass());
        Assert.assertEquals(48346L, tc.getTTL());
        Assert.assertEquals(0, tc.getU16Field());
        Assert.assertNull(tc.getNameField());
    }
    
    public void test_ctor_8arg() throws TextParseException {
        final Name n = Name.fromString("My.Name.");
        final Name m = Name.fromString("My.Other.Name.");
        final TestClass tc = new TestClass(n, 15, 1, 45359L, 7979, "u16 description", m, "name description");
        Assert.assertSame(n, tc.getName());
        Assert.assertEquals(15, tc.getType());
        Assert.assertEquals(1, tc.getDClass());
        Assert.assertEquals(45359L, tc.getTTL());
        Assert.assertEquals(7979, tc.getU16Field());
        Assert.assertEquals(m, tc.getNameField());
        try {
            new TestClass(n, 15, 1, 45359L, 65536, "u16 description", m, "name description");
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex) {}
        final Name rel = Name.fromString("My.relative.Name");
        try {
            new TestClass(n, 15, 1, 45359L, 7979, "u16 description", rel, "name description");
            Assert.fail("RelativeNameException not thrown");
        }
        catch (RelativeNameException ex2) {}
    }
    
    public void test_rrFromWire() throws IOException {
        final byte[] raw = { -68, 31, 2, 77, 121, 6, 115, 105, 78, 103, 108, 69, 4, 110, 65, 109, 69, 0 };
        final DNSInput in = new DNSInput(raw);
        final TestClass tc = new TestClass();
        tc.rrFromWire(in);
        final Name exp = Name.fromString("My.single.name.");
        Assert.assertEquals(48159L, tc.getU16Field());
        Assert.assertEquals(exp, tc.getNameField());
    }
    
    public void test_rdataFromString() throws IOException {
        final Name exp = Name.fromString("My.Single.Name.");
        Tokenizer t = new Tokenizer("6562 My.Single.Name.");
        TestClass tc = new TestClass();
        tc.rdataFromString(t, null);
        Assert.assertEquals(6562, tc.getU16Field());
        Assert.assertEquals(exp, tc.getNameField());
        t = new Tokenizer("10 My.Relative.Name");
        tc = new TestClass();
        try {
            tc.rdataFromString(t, null);
            Assert.fail("RelativeNameException not thrown");
        }
        catch (RelativeNameException ex) {}
    }
    
    public void test_rrToString() throws IOException, TextParseException {
        final Name n = Name.fromString("My.Name.");
        final Name m = Name.fromString("My.Other.Name.");
        final TestClass tc = new TestClass(n, 15, 1, 45359L, 7979, "u16 description", m, "name description");
        final String out = tc.rrToString();
        final String exp = "7979 My.Other.Name.";
        Assert.assertEquals(exp, out);
    }
    
    public void test_rrToWire() throws IOException, TextParseException {
        final Name n = Name.fromString("My.Name.");
        final Name m = Name.fromString("M.O.n.");
        final TestClass tc = new TestClass(n, 15, 1, 45359L, 7979, "u16 description", m, "name description");
        DNSOutput dout = new DNSOutput();
        tc.rrToWire(dout, null, true);
        byte[] out = dout.toByteArray();
        byte[] exp = { 31, 43, 1, 109, 1, 111, 1, 110, 0 };
        Assert.assertTrue(Arrays.equals(exp, out));
        dout = new DNSOutput();
        tc.rrToWire(dout, null, false);
        out = dout.toByteArray();
        exp = new byte[] { 31, 43, 1, 77, 1, 79, 1, 110, 0 };
        Assert.assertTrue(Arrays.equals(exp, out));
    }
    
    private static class TestClass extends U16NameBase
    {
        public TestClass() {
        }
        
        public TestClass(final Name name, final int type, final int dclass, final long ttl) {
            super(name, type, dclass, ttl);
        }
        
        public TestClass(final Name name, final int type, final int dclass, final long ttl, final int u16Field, final String u16Description, final Name nameField, final String nameDescription) {
            super(name, type, dclass, ttl, u16Field, u16Description, nameField, nameDescription);
        }
        
        public int getU16Field() {
            return super.getU16Field();
        }
        
        public Name getNameField() {
            return super.getNameField();
        }
        
        public Record getObject() {
            return null;
        }
    }
}
