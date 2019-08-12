// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.io.IOException;
import junit.framework.Assert;
import junit.framework.TestCase;

public class HeaderTest extends TestCase
{
    private Header m_h;
    
    public void setUp() {
        this.m_h = new Header(43981);
    }
    
    public void test_fixture_state() {
        Assert.assertEquals(43981, this.m_h.getID());
        final boolean[] flags = this.m_h.getFlags();
        for (int i = 0; i < flags.length; ++i) {
            Assert.assertFalse(flags[i]);
        }
        Assert.assertEquals(0, this.m_h.getRcode());
        Assert.assertEquals(0, this.m_h.getOpcode());
        Assert.assertEquals(0, this.m_h.getCount(0));
        Assert.assertEquals(0, this.m_h.getCount(1));
        Assert.assertEquals(0, this.m_h.getCount(2));
        Assert.assertEquals(0, this.m_h.getCount(3));
    }
    
    public void test_ctor_0arg() {
        this.m_h = new Header();
        Assert.assertTrue(0 <= this.m_h.getID() && this.m_h.getID() < 65535);
        final boolean[] flags = this.m_h.getFlags();
        for (int i = 0; i < flags.length; ++i) {
            Assert.assertFalse(flags[i]);
        }
        Assert.assertEquals(0, this.m_h.getRcode());
        Assert.assertEquals(0, this.m_h.getOpcode());
        Assert.assertEquals(0, this.m_h.getCount(0));
        Assert.assertEquals(0, this.m_h.getCount(1));
        Assert.assertEquals(0, this.m_h.getCount(2));
        Assert.assertEquals(0, this.m_h.getCount(3));
    }
    
    public void test_ctor_DNSInput() throws IOException {
        final byte[] raw = { 18, -85, -113, -67, 101, 28, 16, -16, -104, -70, 113, -112 };
        this.m_h = new Header(new DNSInput(raw));
        Assert.assertEquals(4779, this.m_h.getID());
        final boolean[] flags = this.m_h.getFlags();
        Assert.assertTrue(flags[0]);
        Assert.assertEquals(1, this.m_h.getOpcode());
        Assert.assertTrue(flags[5]);
        Assert.assertTrue(flags[6]);
        Assert.assertTrue(flags[7]);
        Assert.assertTrue(flags[8]);
        Assert.assertFalse(flags[9]);
        Assert.assertTrue(flags[10]);
        Assert.assertTrue(flags[11]);
        Assert.assertEquals(13, this.m_h.getRcode());
        Assert.assertEquals(25884, this.m_h.getCount(0));
        Assert.assertEquals(4336, this.m_h.getCount(1));
        Assert.assertEquals(39098, this.m_h.getCount(2));
        Assert.assertEquals(29072, this.m_h.getCount(3));
    }
    
    public void test_toWire() throws IOException {
        final byte[] raw = { 18, -85, -113, -67, 101, 28, 16, -16, -104, -70, 113, -112 };
        this.m_h = new Header(raw);
        final DNSOutput dout = new DNSOutput();
        this.m_h.toWire(dout);
        byte[] out = dout.toByteArray();
        Assert.assertEquals(12, out.length);
        for (int i = 0; i < out.length; ++i) {
            Assert.assertEquals(raw[i], out[i]);
        }
        this.m_h.setOpcode(10);
        Assert.assertEquals(10, this.m_h.getOpcode());
        this.m_h.setRcode(7);
        raw[2] = -41;
        raw[3] = -73;
        out = this.m_h.toWire();
        Assert.assertEquals(12, out.length);
        for (int i = 0; i < out.length; ++i) {
            Assert.assertEquals("i=" + i, raw[i], out[i]);
        }
    }
    
    public void test_flags() {
        this.m_h.setFlag(0);
        this.m_h.setFlag(5);
        Assert.assertTrue(this.m_h.getFlag(0));
        Assert.assertTrue(this.m_h.getFlags()[0]);
        Assert.assertTrue(this.m_h.getFlag(5));
        Assert.assertTrue(this.m_h.getFlags()[5]);
        this.m_h.unsetFlag(0);
        Assert.assertFalse(this.m_h.getFlag(0));
        Assert.assertFalse(this.m_h.getFlags()[0]);
        Assert.assertTrue(this.m_h.getFlag(5));
        Assert.assertTrue(this.m_h.getFlags()[5]);
        this.m_h.unsetFlag(5);
        Assert.assertFalse(this.m_h.getFlag(0));
        Assert.assertFalse(this.m_h.getFlags()[0]);
        Assert.assertFalse(this.m_h.getFlag(5));
        Assert.assertFalse(this.m_h.getFlags()[5]);
        final boolean[] flags = this.m_h.getFlags();
        for (int i = 0; i < flags.length; ++i) {
            if (i <= 0 || i >= 5) {
                if (i <= 11) {
                    Assert.assertFalse(flags[i]);
                }
            }
        }
    }
    
    public void test_flags_invalid() {
        try {
            this.m_h.setFlag(-1);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex) {}
        try {
            this.m_h.setFlag(1);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex2) {}
        try {
            this.m_h.setFlag(16);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex3) {}
        try {
            this.m_h.unsetFlag(-1);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex4) {}
        try {
            this.m_h.unsetFlag(13);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex5) {}
        try {
            this.m_h.unsetFlag(16);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex6) {}
        try {
            this.m_h.getFlag(-1);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex7) {}
        try {
            this.m_h.getFlag(4);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex8) {}
        try {
            this.m_h.getFlag(16);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex9) {}
    }
    
    public void test_ID() {
        Assert.assertEquals(43981, this.m_h.getID());
        this.m_h = new Header();
        final int id = this.m_h.getID();
        Assert.assertEquals(id, this.m_h.getID());
        Assert.assertTrue(id >= 0 && id < 65535);
        this.m_h.setID(56506);
        Assert.assertEquals(56506, this.m_h.getID());
    }
    
    public void test_setID_invalid() {
        try {
            this.m_h.setID(65536);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex) {}
        try {
            this.m_h.setID(-1);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex2) {}
    }
    
    public void test_Rcode() {
        Assert.assertEquals(0, this.m_h.getRcode());
        this.m_h.setRcode(10);
        Assert.assertEquals(10, this.m_h.getRcode());
        for (int i = 0; i < 12; ++i) {
            if (i <= 0 || i >= 5) {
                if (i <= 11) {
                    Assert.assertFalse(this.m_h.getFlag(i));
                }
            }
        }
    }
    
    public void test_setRcode_invalid() {
        try {
            this.m_h.setRcode(-1);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex) {}
        try {
            this.m_h.setRcode(256);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex2) {}
    }
    
    public void test_Opcode() {
        Assert.assertEquals(0, this.m_h.getOpcode());
        this.m_h.setOpcode(14);
        Assert.assertEquals(14, this.m_h.getOpcode());
        Assert.assertFalse(this.m_h.getFlag(0));
        for (int i = 5; i < 12; ++i) {
            Assert.assertFalse(this.m_h.getFlag(i));
        }
        Assert.assertEquals(0, this.m_h.getRcode());
    }
    
    public void test_setOpcode_invalid() {
        try {
            this.m_h.setOpcode(-1);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex) {}
        try {
            this.m_h.setOpcode(256);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex2) {}
    }
    
    public void test_Count() {
        this.m_h.setCount(2, 30);
        Assert.assertEquals(0, this.m_h.getCount(0));
        Assert.assertEquals(0, this.m_h.getCount(1));
        Assert.assertEquals(30, this.m_h.getCount(2));
        Assert.assertEquals(0, this.m_h.getCount(3));
        this.m_h.incCount(0);
        Assert.assertEquals(1, this.m_h.getCount(0));
        this.m_h.decCount(2);
        Assert.assertEquals(29, this.m_h.getCount(2));
    }
    
    public void test_setCount_invalid() {
        try {
            this.m_h.setCount(-1, 0);
            Assert.fail("ArrayIndexOutOfBoundsException not thrown");
        }
        catch (ArrayIndexOutOfBoundsException ex) {}
        try {
            this.m_h.setCount(4, 0);
            Assert.fail("ArrayIndexOutOfBoundsException not thrown");
        }
        catch (ArrayIndexOutOfBoundsException ex2) {}
        try {
            this.m_h.setCount(0, -1);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex3) {}
        try {
            this.m_h.setCount(3, 65536);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex4) {}
    }
    
    public void test_getCount_invalid() {
        try {
            this.m_h.getCount(-1);
            Assert.fail("ArrayIndexOutOfBoundsException not thrown");
        }
        catch (ArrayIndexOutOfBoundsException ex) {}
        try {
            this.m_h.getCount(4);
            Assert.fail("ArrayIndexOutOfBoundsException not thrown");
        }
        catch (ArrayIndexOutOfBoundsException ex2) {}
    }
    
    public void test_incCount_invalid() {
        this.m_h.setCount(1, 65535);
        try {
            this.m_h.incCount(1);
            Assert.fail("IllegalStateException not thrown");
        }
        catch (IllegalStateException ex) {}
    }
    
    public void test_decCount_invalid() {
        this.m_h.setCount(2, 0);
        try {
            this.m_h.decCount(2);
            Assert.fail("IllegalStateException not thrown");
        }
        catch (IllegalStateException ex) {}
    }
    
    public void test_toString() {
        this.m_h.setOpcode(Opcode.value("STATUS"));
        this.m_h.setRcode(Rcode.value("NXDOMAIN"));
        this.m_h.setFlag(0);
        this.m_h.setFlag(7);
        this.m_h.setFlag(8);
        this.m_h.setFlag(11);
        this.m_h.setCount(1, 255);
        this.m_h.setCount(2, 10);
        final String text = this.m_h.toString();
        Assert.assertFalse(text.indexOf("id: 43981") == -1);
        Assert.assertFalse(text.indexOf("opcode: STATUS") == -1);
        Assert.assertFalse(text.indexOf("status: NXDOMAIN") == -1);
        Assert.assertFalse(text.indexOf(" qr ") == -1);
        Assert.assertFalse(text.indexOf(" rd ") == -1);
        Assert.assertFalse(text.indexOf(" ra ") == -1);
        Assert.assertFalse(text.indexOf(" cd ") == -1);
        Assert.assertFalse(text.indexOf("qd: 0 ") == -1);
        Assert.assertFalse(text.indexOf("an: 255 ") == -1);
        Assert.assertFalse(text.indexOf("au: 10 ") == -1);
        Assert.assertFalse(text.indexOf("ad: 0 ") == -1);
    }
    
    public void test_clone() {
        this.m_h.setOpcode(Opcode.value("IQUERY"));
        this.m_h.setRcode(Rcode.value("SERVFAIL"));
        this.m_h.setFlag(0);
        this.m_h.setFlag(7);
        this.m_h.setFlag(8);
        this.m_h.setFlag(11);
        this.m_h.setCount(1, 255);
        this.m_h.setCount(2, 10);
        final Header h2 = (Header)this.m_h.clone();
        Assert.assertNotSame(this.m_h, h2);
        Assert.assertEquals(this.m_h.getID(), h2.getID());
        for (int i = 0; i < 16; ++i) {
            if (i <= 0 || i >= 5) {
                if (i <= 11) {
                    Assert.assertEquals(this.m_h.getFlag(i), h2.getFlag(i));
                }
            }
        }
        for (int i = 0; i < 4; ++i) {
            Assert.assertEquals(this.m_h.getCount(i), h2.getCount(i));
        }
    }
}
