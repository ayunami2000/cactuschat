// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import junit.framework.Assert;
import java.util.Arrays;
import junit.framework.TestCase;

public class DNSOutputTest extends TestCase
{
    private DNSOutput m_do;
    
    public void setUp() {
        this.m_do = new DNSOutput(1);
    }
    
    private void assertEquals(final byte[] exp, final byte[] act) {
        Assert.assertTrue(Arrays.equals(exp, act));
    }
    
    public void test_default_ctor() {
        this.m_do = new DNSOutput();
        Assert.assertEquals(0, this.m_do.current());
    }
    
    public void test_initial_state() {
        Assert.assertEquals(0, this.m_do.current());
        try {
            this.m_do.restore();
            Assert.fail("IllegalStateException not thrown");
        }
        catch (IllegalStateException ex) {}
        try {
            this.m_do.jump(1);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex2) {}
    }
    
    public void test_writeU8_basic() {
        this.m_do.writeU8(1);
        Assert.assertEquals(1, this.m_do.current());
        final byte[] curr = this.m_do.toByteArray();
        Assert.assertEquals(1, curr.length);
        Assert.assertEquals(1, curr[0]);
    }
    
    public void test_writeU8_expand() {
        this.m_do.writeU8(1);
        this.m_do.writeU8(2);
        Assert.assertEquals(2, this.m_do.current());
        final byte[] curr = this.m_do.toByteArray();
        Assert.assertEquals(2, curr.length);
        Assert.assertEquals(1, curr[0]);
        Assert.assertEquals(2, curr[1]);
    }
    
    public void test_writeU8_max() {
        this.m_do.writeU8(255);
        final byte[] curr = this.m_do.toByteArray();
        Assert.assertEquals((byte)(-1), curr[0]);
    }
    
    public void test_writeU8_toobig() {
        try {
            this.m_do.writeU8(511);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex) {}
    }
    
    public void test_writeU16_basic() {
        this.m_do.writeU16(256);
        Assert.assertEquals(2, this.m_do.current());
        final byte[] curr = this.m_do.toByteArray();
        Assert.assertEquals(2, curr.length);
        Assert.assertEquals(1, curr[0]);
        Assert.assertEquals(0, curr[1]);
    }
    
    public void test_writeU16_max() {
        this.m_do.writeU16(65535);
        final byte[] curr = this.m_do.toByteArray();
        Assert.assertEquals((byte)(-1), curr[0]);
        Assert.assertEquals((byte)(-1), curr[1]);
    }
    
    public void test_writeU16_toobig() {
        try {
            this.m_do.writeU16(131071);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex) {}
    }
    
    public void test_writeU32_basic() {
        this.m_do.writeU32(285216785L);
        Assert.assertEquals(4, this.m_do.current());
        final byte[] curr = this.m_do.toByteArray();
        Assert.assertEquals(4, curr.length);
        Assert.assertEquals(17, curr[0]);
        Assert.assertEquals(0, curr[1]);
        Assert.assertEquals(16, curr[2]);
        Assert.assertEquals(17, curr[3]);
    }
    
    public void test_writeU32_max() {
        this.m_do.writeU32(4294967295L);
        final byte[] curr = this.m_do.toByteArray();
        Assert.assertEquals((byte)(-1), curr[0]);
        Assert.assertEquals((byte)(-1), curr[1]);
        Assert.assertEquals((byte)(-1), curr[2]);
        Assert.assertEquals((byte)(-1), curr[3]);
    }
    
    public void test_writeU32_toobig() {
        try {
            this.m_do.writeU32(8589934591L);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex) {}
    }
    
    public void test_jump_basic() {
        this.m_do.writeU32(287454020L);
        Assert.assertEquals(4, this.m_do.current());
        this.m_do.jump(2);
        Assert.assertEquals(2, this.m_do.current());
        this.m_do.writeU8(153);
        final byte[] curr = this.m_do.toByteArray();
        Assert.assertEquals(3, curr.length);
        Assert.assertEquals(17, curr[0]);
        Assert.assertEquals(34, curr[1]);
        Assert.assertEquals((byte)(-103), curr[2]);
    }
    
    public void test_writeByteArray_1arg() {
        final byte[] in = { -85, -51, -17, 18, 52 };
        this.m_do.writeByteArray(in);
        Assert.assertEquals(5, this.m_do.current());
        final byte[] curr = this.m_do.toByteArray();
        this.assertEquals(in, curr);
    }
    
    public void test_writeByteArray_3arg() {
        final byte[] in = { -85, -51, -17, 18, 52 };
        this.m_do.writeByteArray(in, 2, 3);
        Assert.assertEquals(3, this.m_do.current());
        final byte[] exp = { in[2], in[3], in[4] };
        final byte[] curr = this.m_do.toByteArray();
        this.assertEquals(exp, curr);
    }
    
    public void test_writeCountedString_basic() {
        final byte[] in = { 104, 101, 108, 76, 48 };
        this.m_do.writeCountedString(in);
        Assert.assertEquals(in.length + 1, this.m_do.current());
        final byte[] curr = this.m_do.toByteArray();
        final byte[] exp = { (byte)in.length, in[0], in[1], in[2], in[3], in[4] };
        this.assertEquals(exp, curr);
    }
    
    public void test_writeCountedString_empty() {
        final byte[] in = new byte[0];
        this.m_do.writeCountedString(in);
        Assert.assertEquals(in.length + 1, this.m_do.current());
        final byte[] curr = this.m_do.toByteArray();
        final byte[] exp = { (byte)in.length };
        this.assertEquals(exp, curr);
    }
    
    public void test_writeCountedString_toobig() {
        final byte[] in = new byte[256];
        try {
            this.m_do.writeCountedString(in);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex) {}
    }
    
    public void test_save_restore() {
        this.m_do.writeU32(305419896L);
        Assert.assertEquals(4, this.m_do.current());
        this.m_do.save();
        this.m_do.writeU16(43981);
        Assert.assertEquals(6, this.m_do.current());
        this.m_do.restore();
        Assert.assertEquals(4, this.m_do.current());
        try {
            this.m_do.restore();
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalStateException ex) {}
    }
}
