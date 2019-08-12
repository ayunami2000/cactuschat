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

public class ARecordTest extends TestCase
{
    Name m_an;
    Name m_rn;
    InetAddress m_addr;
    String m_addr_string;
    byte[] m_addr_bytes;
    long m_ttl;
    
    protected void setUp() throws TextParseException, UnknownHostException {
        this.m_an = Name.fromString("My.Absolute.Name.");
        this.m_rn = Name.fromString("My.Relative.Name");
        this.m_addr_string = "193.160.232.5";
        this.m_addr = InetAddress.getByName(this.m_addr_string);
        this.m_addr_bytes = this.m_addr.getAddress();
        this.m_ttl = 79225L;
    }
    
    public void test_ctor_0arg() throws UnknownHostException {
        final ARecord ar = new ARecord();
        Assert.assertNull(ar.getName());
        Assert.assertEquals(0, ar.getType());
        Assert.assertEquals(0, ar.getDClass());
        Assert.assertEquals(0L, ar.getTTL());
        Assert.assertEquals(InetAddress.getByName("0.0.0.0"), ar.getAddress());
    }
    
    public void test_getObject() {
        final ARecord ar = new ARecord();
        final Record r = ar.getObject();
        Assert.assertTrue(r instanceof ARecord);
    }
    
    public void test_ctor_4arg() {
        final ARecord ar = new ARecord(this.m_an, 1, this.m_ttl, this.m_addr);
        Assert.assertEquals(this.m_an, ar.getName());
        Assert.assertEquals(1, ar.getType());
        Assert.assertEquals(1, ar.getDClass());
        Assert.assertEquals(this.m_ttl, ar.getTTL());
        Assert.assertEquals(this.m_addr, ar.getAddress());
        try {
            new ARecord(this.m_rn, 1, this.m_ttl, this.m_addr);
            Assert.fail("RelativeNameException not thrown");
        }
        catch (RelativeNameException ex) {}
        try {
            new ARecord(this.m_an, 1, this.m_ttl, InetAddress.getByName("2001:0db8:85a3:08d3:1319:8a2e:0370:7334"));
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException e2) {}
        catch (UnknownHostException e) {
            Assert.fail(e.getMessage());
        }
    }
    
    public void test_rrFromWire() throws IOException {
        final DNSInput di = new DNSInput(this.m_addr_bytes);
        final ARecord ar = new ARecord();
        ar.rrFromWire(di);
        Assert.assertEquals(this.m_addr, ar.getAddress());
    }
    
    public void test_rdataFromString() throws IOException {
        Tokenizer t = new Tokenizer(this.m_addr_string);
        ARecord ar = new ARecord();
        ar.rdataFromString(t, null);
        Assert.assertEquals(this.m_addr, ar.getAddress());
        t = new Tokenizer("193.160.232");
        ar = new ARecord();
        try {
            ar.rdataFromString(t, null);
            Assert.fail("TextParseException not thrown");
        }
        catch (TextParseException ex) {}
    }
    
    public void test_rrToString() {
        final ARecord ar = new ARecord(this.m_an, 1, this.m_ttl, this.m_addr);
        Assert.assertEquals(this.m_addr_string, ar.rrToString());
    }
    
    public void test_rrToWire() {
        final ARecord ar = new ARecord(this.m_an, 1, this.m_ttl, this.m_addr);
        DNSOutput dout = new DNSOutput();
        ar.rrToWire(dout, null, true);
        Assert.assertTrue(Arrays.equals(this.m_addr_bytes, dout.toByteArray()));
        dout = new DNSOutput();
        ar.rrToWire(dout, null, false);
        Assert.assertTrue(Arrays.equals(this.m_addr_bytes, dout.toByteArray()));
    }
}
