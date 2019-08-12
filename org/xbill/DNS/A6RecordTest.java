// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.util.Arrays;
import java.io.IOException;
import junit.framework.Assert;
import java.net.UnknownHostException;
import java.net.InetAddress;
import junit.framework.TestCase;

public class A6RecordTest extends TestCase
{
    Name m_an;
    Name m_an2;
    Name m_rn;
    InetAddress m_addr;
    String m_addr_string;
    String m_addr_string_canonical;
    byte[] m_addr_bytes;
    int m_prefix_bits;
    long m_ttl;
    
    protected void setUp() throws TextParseException, UnknownHostException {
        this.m_an = Name.fromString("My.Absolute.Name.");
        this.m_an2 = Name.fromString("My.Second.Absolute.Name.");
        this.m_rn = Name.fromString("My.Relative.Name");
        this.m_addr_string = "2001:0db8:85a3:08d3:1319:8a2e:0370:7334";
        this.m_addr_string_canonical = "2001:db8:85a3:8d3:1319:8a2e:370:7334";
        this.m_addr = InetAddress.getByName(this.m_addr_string);
        this.m_addr_bytes = this.m_addr.getAddress();
        this.m_ttl = 79225L;
        this.m_prefix_bits = 9;
    }
    
    public void test_ctor_0arg() {
        final A6Record ar = new A6Record();
        Assert.assertNull(ar.getName());
        Assert.assertEquals(0, ar.getType());
        Assert.assertEquals(0, ar.getDClass());
        Assert.assertEquals(0L, ar.getTTL());
    }
    
    public void test_getObject() {
        final A6Record ar = new A6Record();
        final Record r = ar.getObject();
        Assert.assertTrue(r instanceof A6Record);
    }
    
    public void test_ctor_6arg() {
        A6Record ar = new A6Record(this.m_an, 1, this.m_ttl, this.m_prefix_bits, this.m_addr, null);
        Assert.assertEquals(this.m_an, ar.getName());
        Assert.assertEquals(38, ar.getType());
        Assert.assertEquals(1, ar.getDClass());
        Assert.assertEquals(this.m_ttl, ar.getTTL());
        Assert.assertEquals(this.m_prefix_bits, ar.getPrefixBits());
        Assert.assertEquals(this.m_addr, ar.getSuffix());
        Assert.assertNull(ar.getPrefix());
        ar = new A6Record(this.m_an, 1, this.m_ttl, this.m_prefix_bits, this.m_addr, this.m_an2);
        Assert.assertEquals(this.m_an, ar.getName());
        Assert.assertEquals(38, ar.getType());
        Assert.assertEquals(1, ar.getDClass());
        Assert.assertEquals(this.m_ttl, ar.getTTL());
        Assert.assertEquals(this.m_prefix_bits, ar.getPrefixBits());
        Assert.assertEquals(this.m_addr, ar.getSuffix());
        Assert.assertEquals(this.m_an2, ar.getPrefix());
        try {
            new A6Record(this.m_rn, 1, this.m_ttl, this.m_prefix_bits, this.m_addr, null);
            Assert.fail("RelativeNameException not thrown");
        }
        catch (RelativeNameException ex) {}
        try {
            new A6Record(this.m_an, 1, this.m_ttl, this.m_prefix_bits, this.m_addr, this.m_rn);
            Assert.fail("RelativeNameException not thrown");
        }
        catch (RelativeNameException ex2) {}
        try {
            new A6Record(this.m_rn, 1, this.m_ttl, 256, this.m_addr, null);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (RelativeNameException ex3) {}
        try {
            new A6Record(this.m_an, 1, this.m_ttl, this.m_prefix_bits, InetAddress.getByName("192.168.0.1"), null);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException e2) {}
        catch (UnknownHostException e) {
            Assert.fail(e.getMessage());
        }
    }
    
    public void test_rrFromWire() throws CloneNotSupportedException, IOException, UnknownHostException {
        DNSOutput dout = new DNSOutput();
        dout.writeU8(0);
        dout.writeByteArray(this.m_addr_bytes);
        DNSInput din = new DNSInput(dout.toByteArray());
        A6Record ar = new A6Record();
        ar.rrFromWire(din);
        Assert.assertEquals(0, ar.getPrefixBits());
        Assert.assertEquals(this.m_addr, ar.getSuffix());
        Assert.assertNull(ar.getPrefix());
        dout = new DNSOutput();
        dout.writeU8(9);
        dout.writeByteArray(this.m_addr_bytes, 1, 15);
        dout.writeByteArray(this.m_an2.toWire());
        din = new DNSInput(dout.toByteArray());
        ar = new A6Record();
        ar.rrFromWire(din);
        Assert.assertEquals(9, ar.getPrefixBits());
        final byte[] addr_bytes = this.m_addr_bytes.clone();
        addr_bytes[0] = 0;
        final InetAddress exp = InetAddress.getByAddress(addr_bytes);
        Assert.assertEquals(exp, ar.getSuffix());
        Assert.assertEquals(this.m_an2, ar.getPrefix());
    }
    
    public void test_rdataFromString() throws CloneNotSupportedException, IOException, UnknownHostException {
        Tokenizer t = new Tokenizer("0 " + this.m_addr_string);
        A6Record ar = new A6Record();
        ar.rdataFromString(t, null);
        Assert.assertEquals(0, ar.getPrefixBits());
        Assert.assertEquals(this.m_addr, ar.getSuffix());
        Assert.assertNull(ar.getPrefix());
        t = new Tokenizer("9 " + this.m_addr_string + " " + this.m_an2);
        ar = new A6Record();
        ar.rdataFromString(t, null);
        Assert.assertEquals(9, ar.getPrefixBits());
        Assert.assertEquals(this.m_addr, ar.getSuffix());
        Assert.assertEquals(this.m_an2, ar.getPrefix());
        t = new Tokenizer("129");
        ar = new A6Record();
        try {
            ar.rdataFromString(t, null);
            Assert.fail("TextParseException not thrown");
        }
        catch (TextParseException ex) {}
        t = new Tokenizer("0 " + this.m_addr_string.substring(4));
        ar = new A6Record();
        try {
            ar.rdataFromString(t, null);
            Assert.fail("TextParseException not thrown");
        }
        catch (TextParseException ex2) {}
    }
    
    public void test_rrToString() {
        final A6Record ar = new A6Record(this.m_an, 1, this.m_ttl, this.m_prefix_bits, this.m_addr, this.m_an2);
        final String exp = "" + this.m_prefix_bits + " " + this.m_addr_string_canonical + " " + this.m_an2;
        final String out = ar.rrToString();
        Assert.assertEquals(exp, out);
    }
    
    public void test_rrToWire() {
        final A6Record ar = new A6Record(this.m_an, 1, this.m_ttl, this.m_prefix_bits, this.m_addr, this.m_an2);
        DNSOutput dout = new DNSOutput();
        dout.writeU8(this.m_prefix_bits);
        dout.writeByteArray(this.m_addr_bytes, 1, 15);
        dout.writeByteArray(this.m_an2.toWireCanonical());
        byte[] exp = dout.toByteArray();
        dout = new DNSOutput();
        ar.rrToWire(dout, null, true);
        Assert.assertTrue(Arrays.equals(exp, dout.toByteArray()));
        dout = new DNSOutput();
        dout.writeU8(this.m_prefix_bits);
        dout.writeByteArray(this.m_addr_bytes, 1, 15);
        dout.writeByteArray(this.m_an2.toWire());
        exp = dout.toByteArray();
        dout = new DNSOutput();
        ar.rrToWire(dout, null, false);
        Assert.assertTrue(Arrays.equals(exp, dout.toByteArray()));
    }
}
