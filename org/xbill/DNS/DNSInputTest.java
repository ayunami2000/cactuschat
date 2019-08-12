// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import junit.framework.Assert;
import java.util.Arrays;
import junit.framework.TestCase;

public class DNSInputTest extends TestCase
{
    private byte[] m_raw;
    private DNSInput m_di;
    
    private void assertEquals(final byte[] exp, final byte[] act) {
        Assert.assertTrue(Arrays.equals(exp, act));
    }
    
    public void setUp() {
        this.m_raw = new byte[] { 0, 1, 2, 3, 4, 5, -1, -1, -1, -1 };
        this.m_di = new DNSInput(this.m_raw);
    }
    
    public void test_initial_state() {
        Assert.assertEquals(0, this.m_di.current());
        Assert.assertEquals(10, this.m_di.remaining());
    }
    
    public void test_jump1() {
        this.m_di.jump(1);
        Assert.assertEquals(1, this.m_di.current());
        Assert.assertEquals(9, this.m_di.remaining());
    }
    
    public void test_jump2() {
        this.m_di.jump(9);
        Assert.assertEquals(9, this.m_di.current());
        Assert.assertEquals(1, this.m_di.remaining());
    }
    
    public void test_jump_invalid() {
        try {
            this.m_di.jump(10);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex) {}
    }
    
    public void test_setActive() {
        this.m_di.setActive(5);
        Assert.assertEquals(0, this.m_di.current());
        Assert.assertEquals(5, this.m_di.remaining());
    }
    
    public void test_setActive_boundary1() {
        this.m_di.setActive(10);
        Assert.assertEquals(0, this.m_di.current());
        Assert.assertEquals(10, this.m_di.remaining());
    }
    
    public void test_setActive_boundary2() {
        this.m_di.setActive(0);
        Assert.assertEquals(0, this.m_di.current());
        Assert.assertEquals(0, this.m_di.remaining());
    }
    
    public void test_setActive_invalid() {
        try {
            this.m_di.setActive(11);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex) {}
    }
    
    public void test_clearActive() {
        this.m_di.clearActive();
        Assert.assertEquals(0, this.m_di.current());
        Assert.assertEquals(10, this.m_di.remaining());
        this.m_di.setActive(5);
        this.m_di.clearActive();
        Assert.assertEquals(0, this.m_di.current());
        Assert.assertEquals(10, this.m_di.remaining());
    }
    
    public void test_restore_invalid() {
        try {
            this.m_di.restore();
            Assert.fail("IllegalStateException not thrown");
        }
        catch (IllegalStateException ex) {}
    }
    
    public void test_save_restore() {
        this.m_di.jump(4);
        Assert.assertEquals(4, this.m_di.current());
        Assert.assertEquals(6, this.m_di.remaining());
        this.m_di.save();
        this.m_di.jump(0);
        Assert.assertEquals(0, this.m_di.current());
        Assert.assertEquals(10, this.m_di.remaining());
        this.m_di.restore();
        Assert.assertEquals(4, this.m_di.current());
        Assert.assertEquals(6, this.m_di.remaining());
    }
    
    public void test_readU8_basic() throws WireParseException {
        final int v1 = this.m_di.readU8();
        Assert.assertEquals(1, this.m_di.current());
        Assert.assertEquals(9, this.m_di.remaining());
        Assert.assertEquals(0, v1);
    }
    
    public void test_readU8_maxval() throws WireParseException {
        this.m_di.jump(9);
        int v1 = this.m_di.readU8();
        Assert.assertEquals(10, this.m_di.current());
        Assert.assertEquals(0, this.m_di.remaining());
        Assert.assertEquals(255, v1);
        try {
            v1 = this.m_di.readU8();
            Assert.fail("WireParseException not thrown");
        }
        catch (WireParseException ex) {}
    }
    
    public void test_readU16_basic() throws WireParseException {
        int v1 = this.m_di.readU16();
        Assert.assertEquals(2, this.m_di.current());
        Assert.assertEquals(8, this.m_di.remaining());
        Assert.assertEquals(1, v1);
        this.m_di.jump(1);
        v1 = this.m_di.readU16();
        Assert.assertEquals(258, v1);
    }
    
    public void test_readU16_maxval() throws WireParseException {
        this.m_di.jump(8);
        final int v = this.m_di.readU16();
        Assert.assertEquals(10, this.m_di.current());
        Assert.assertEquals(0, this.m_di.remaining());
        Assert.assertEquals(65535, v);
        try {
            this.m_di.jump(9);
            this.m_di.readU16();
            Assert.fail("WireParseException not thrown");
        }
        catch (WireParseException ex) {}
    }
    
    public void test_readU32_basic() throws WireParseException {
        final long v1 = this.m_di.readU32();
        Assert.assertEquals(4, this.m_di.current());
        Assert.assertEquals(6, this.m_di.remaining());
        Assert.assertEquals(66051L, v1);
    }
    
    public void test_readU32_maxval() throws WireParseException {
        this.m_di.jump(6);
        final long v = this.m_di.readU32();
        Assert.assertEquals(10, this.m_di.current());
        Assert.assertEquals(0, this.m_di.remaining());
        Assert.assertEquals(4294967295L, v);
        try {
            this.m_di.jump(7);
            this.m_di.readU32();
            Assert.fail("WireParseException not thrown");
        }
        catch (WireParseException ex) {}
    }
    
    public void test_readByteArray_0arg() throws WireParseException {
        this.m_di.jump(1);
        final byte[] out = this.m_di.readByteArray();
        Assert.assertEquals(10, this.m_di.current());
        Assert.assertEquals(0, this.m_di.remaining());
        Assert.assertEquals(9, out.length);
        for (int i = 0; i < 9; ++i) {
            Assert.assertEquals(this.m_raw[i + 1], out[i]);
        }
    }
    
    public void test_readByteArray_0arg_boundary() throws WireParseException {
        this.m_di.jump(9);
        this.m_di.readU8();
        final byte[] out = this.m_di.readByteArray();
        Assert.assertEquals(0, out.length);
    }
    
    public void test_readByteArray_1arg() throws WireParseException {
        final byte[] out = this.m_di.readByteArray(2);
        Assert.assertEquals(2, this.m_di.current());
        Assert.assertEquals(8, this.m_di.remaining());
        Assert.assertEquals(2, out.length);
        Assert.assertEquals(0, out[0]);
        Assert.assertEquals(1, out[1]);
    }
    
    public void test_readByteArray_1arg_boundary() throws WireParseException {
        final byte[] out = this.m_di.readByteArray(10);
        Assert.assertEquals(10, this.m_di.current());
        Assert.assertEquals(0, this.m_di.remaining());
        this.assertEquals(this.m_raw, out);
    }
    
    public void test_readByteArray_1arg_invalid() {
        try {
            this.m_di.readByteArray(11);
            Assert.fail("WireParseException not thrown");
        }
        catch (WireParseException ex) {}
    }
    
    public void test_readByteArray_3arg() throws WireParseException {
        final byte[] data = new byte[5];
        this.m_di.jump(4);
        this.m_di.readByteArray(data, 1, 4);
        Assert.assertEquals(8, this.m_di.current());
        Assert.assertEquals(0, data[0]);
        for (int i = 0; i < 4; ++i) {
            Assert.assertEquals(this.m_raw[i + 4], data[i + 1]);
        }
    }
    
    public void test_readCountedSting() throws WireParseException {
        this.m_di.jump(1);
        final byte[] out = this.m_di.readCountedString();
        Assert.assertEquals(1, out.length);
        Assert.assertEquals(3, this.m_di.current());
        Assert.assertEquals(out[0], 2);
    }
}
