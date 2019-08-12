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

public class AAAARecordTest extends TestCase
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
        this.m_addr_string = "2001:db8:85a3:8d3:1319:8a2e:370:7334";
        this.m_addr = InetAddress.getByName(this.m_addr_string);
        this.m_addr_bytes = this.m_addr.getAddress();
        this.m_ttl = 79225L;
    }
    
    public void test_ctor_0arg() throws UnknownHostException {
        final AAAARecord ar = new AAAARecord();
        Assert.assertNull(ar.getName());
        Assert.assertEquals(0, ar.getType());
        Assert.assertEquals(0, ar.getDClass());
        Assert.assertEquals(0L, ar.getTTL());
        Assert.assertNull(ar.getAddress());
    }
    
    public void test_getObject() {
        final AAAARecord ar = new AAAARecord();
        final Record r = ar.getObject();
        Assert.assertTrue(r instanceof AAAARecord);
    }
    
    public void test_ctor_4arg() {
        final AAAARecord ar = new AAAARecord(this.m_an, 1, this.m_ttl, this.m_addr);
        Assert.assertEquals(this.m_an, ar.getName());
        Assert.assertEquals(28, ar.getType());
        Assert.assertEquals(1, ar.getDClass());
        Assert.assertEquals(this.m_ttl, ar.getTTL());
        Assert.assertEquals(this.m_addr, ar.getAddress());
        try {
            new AAAARecord(this.m_rn, 1, this.m_ttl, this.m_addr);
            Assert.fail("RelativeNameException not thrown");
        }
        catch (RelativeNameException ex) {}
        try {
            new AAAARecord(this.m_an, 1, this.m_ttl, InetAddress.getByName("192.168.0.1"));
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException e2) {}
        catch (UnknownHostException e) {
            Assert.fail(e.getMessage());
        }
    }
    
    public void test_rrFromWire() throws IOException {
        final DNSInput di = new DNSInput(this.m_addr_bytes);
        final AAAARecord ar = new AAAARecord();
        ar.rrFromWire(di);
        Assert.assertEquals(this.m_addr, ar.getAddress());
    }
    
    public void test_rdataFromString() throws IOException {
        Tokenizer t = new Tokenizer(this.m_addr_string);
        AAAARecord ar = new AAAARecord();
        ar.rdataFromString(t, null);
        Assert.assertEquals(this.m_addr, ar.getAddress());
        t = new Tokenizer("193.160.232.1");
        ar = new AAAARecord();
        try {
            ar.rdataFromString(t, null);
            Assert.fail("TextParseException not thrown");
        }
        catch (TextParseException ex) {}
    }
    
    public void test_rrToString() {
        final AAAARecord ar = new AAAARecord(this.m_an, 1, this.m_ttl, this.m_addr);
        Assert.assertEquals(this.m_addr_string, ar.rrToString());
    }
    
    public void test_rrToWire() {
        final AAAARecord ar = new AAAARecord(this.m_an, 1, this.m_ttl, this.m_addr);
        DNSOutput dout = new DNSOutput();
        ar.rrToWire(dout, null, true);
        Assert.assertTrue(Arrays.equals(this.m_addr_bytes, dout.toByteArray()));
        dout = new DNSOutput();
        ar.rrToWire(dout, null, false);
        Assert.assertTrue(Arrays.equals(this.m_addr_bytes, dout.toByteArray()));
    }
}
