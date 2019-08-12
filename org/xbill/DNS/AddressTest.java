// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.net.UnknownHostException;
import java.net.InetAddress;
import junit.framework.Assert;
import java.util.Arrays;
import junit.framework.TestCase;

public class AddressTest extends TestCase
{
    private void assertEquals(final byte[] exp, final byte[] act) {
        Assert.assertTrue(Arrays.equals(exp, act));
    }
    
    private void assertEquals(final int[] exp, final int[] act) {
        Assert.assertEquals(exp.length, act.length);
        for (int i = 0; i < exp.length; ++i) {
            Assert.assertEquals("i=" + i, exp[i], act[i]);
        }
    }
    
    public void test_toByteArray_invalid() {
        try {
            Address.toByteArray("doesn't matter", 3);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex) {}
    }
    
    public void test_toByteArray_IPv4() {
        byte[] exp = { -58, 121, 10, -22 };
        byte[] ret = Address.toByteArray("198.121.10.234", 1);
        this.assertEquals(exp, ret);
        exp = new byte[] { 0, 0, 0, 0 };
        ret = Address.toByteArray("0.0.0.0", 1);
        this.assertEquals(exp, ret);
        exp = new byte[] { -1, -1, -1, -1 };
        ret = Address.toByteArray("255.255.255.255", 1);
    }
    
    public void test_toByteArray_IPv4_invalid() {
        Assert.assertNull(Address.toByteArray("A.B.C.D", 1));
        Assert.assertNull(Address.toByteArray("128...", 1));
        Assert.assertNull(Address.toByteArray("128.121", 1));
        Assert.assertNull(Address.toByteArray("128.111.8", 1));
        Assert.assertNull(Address.toByteArray("128.198.10.", 1));
        Assert.assertNull(Address.toByteArray("128.121.90..10", 1));
        Assert.assertNull(Address.toByteArray("128.121..90.10", 1));
        Assert.assertNull(Address.toByteArray("128..121.90.10", 1));
        Assert.assertNull(Address.toByteArray(".128.121.90.10", 1));
        Assert.assertNull(Address.toByteArray("128.121.90.256", 1));
        Assert.assertNull(Address.toByteArray("128.121.256.10", 1));
        Assert.assertNull(Address.toByteArray("128.256.90.10", 1));
        Assert.assertNull(Address.toByteArray("256.121.90.10", 1));
        Assert.assertNull(Address.toByteArray("128.121.90.-1", 1));
        Assert.assertNull(Address.toByteArray("128.121.-1.10", 1));
        Assert.assertNull(Address.toByteArray("128.-1.90.10", 1));
        Assert.assertNull(Address.toByteArray("-1.121.90.10", 1));
        Assert.assertNull(Address.toByteArray("120.121.90.10.10", 1));
        Assert.assertNull(Address.toByteArray("120.121.90.010", 1));
        Assert.assertNull(Address.toByteArray("120.121.090.10", 1));
        Assert.assertNull(Address.toByteArray("120.021.90.10", 1));
        Assert.assertNull(Address.toByteArray("020.121.90.10", 1));
        Assert.assertNull(Address.toByteArray("1120.121.90.10", 1));
        Assert.assertNull(Address.toByteArray("120.2121.90.10", 1));
        Assert.assertNull(Address.toByteArray("120.121.4190.10", 1));
        Assert.assertNull(Address.toByteArray("120.121.190.1000", 1));
        Assert.assertNull(Address.toByteArray("", 1));
    }
    
    public void test_toByteArray_IPv6() {
        byte[] exp = { 32, 1, 13, -72, -123, -93, 8, -45, 19, 25, -118, 46, 3, 112, 115, 52 };
        byte[] ret = Address.toByteArray("2001:0db8:85a3:08d3:1319:8a2e:0370:7334", 2);
        this.assertEquals(exp, ret);
        ret = Address.toByteArray("2001:db8:85a3:8d3:1319:8a2e:370:7334", 2);
        this.assertEquals(exp, ret);
        ret = Address.toByteArray("2001:DB8:85A3:8D3:1319:8A2E:370:7334", 2);
        this.assertEquals(exp, ret);
        exp = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        ret = Address.toByteArray("0:0:0:0:0:0:0:0", 2);
        this.assertEquals(exp, ret);
        exp = new byte[] { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 };
        ret = Address.toByteArray("FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF", 2);
        this.assertEquals(exp, ret);
        exp = new byte[] { 32, 1, 13, -72, 0, 0, 8, -45, 19, 25, -118, 46, 3, 112, 115, 52 };
        ret = Address.toByteArray("2001:0db8:0000:08d3:1319:8a2e:0370:7334", 2);
        this.assertEquals(exp, ret);
        ret = Address.toByteArray("2001:0db8::08d3:1319:8a2e:0370:7334", 2);
        this.assertEquals(exp, ret);
        exp = new byte[] { 0, 0, 0, 0, -123, -93, 8, -45, 19, 25, -118, 46, 3, 112, 115, 52 };
        ret = Address.toByteArray("0000:0000:85a3:08d3:1319:8a2e:0370:7334", 2);
        this.assertEquals(exp, ret);
        ret = Address.toByteArray("::85a3:08d3:1319:8a2e:0370:7334", 2);
        this.assertEquals(exp, ret);
        exp = new byte[] { 32, 1, 13, -72, -123, -93, 8, -45, 19, 25, -118, 46, 0, 0, 0, 0 };
        ret = Address.toByteArray("2001:0db8:85a3:08d3:1319:8a2e:0:0", 2);
        this.assertEquals(exp, ret);
        ret = Address.toByteArray("2001:0db8:85a3:08d3:1319:8a2e::", 2);
        this.assertEquals(exp, ret);
        exp = new byte[] { 32, 1, 13, -72, 0, 0, 0, 0, 0, 0, 0, 0, 3, 112, 115, 52 };
        ret = Address.toByteArray("2001:0db8:0000:0000:0000:0000:0370:7334", 2);
        this.assertEquals(exp, ret);
        ret = Address.toByteArray("2001:0db8:0:0:0:0:0370:7334", 2);
        this.assertEquals(exp, ret);
        ret = Address.toByteArray("2001:0db8::0:0370:7334", 2);
        this.assertEquals(exp, ret);
        ret = Address.toByteArray("2001:db8::370:7334", 2);
        this.assertEquals(exp, ret);
        exp = new byte[] { 32, 1, 13, -72, -123, -93, 8, -45, 19, 25, -118, 46, -64, -88, 89, 9 };
        ret = Address.toByteArray("2001:0db8:85a3:08d3:1319:8a2e:192.168.89.9", 2);
        this.assertEquals(exp, ret);
        exp = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -64, -88, 89, 9 };
        ret = Address.toByteArray("::192.168.89.9", 2);
        this.assertEquals(exp, ret);
    }
    
    public void test_toByteArray_IPv6_invalid() {
        Assert.assertNull(Address.toByteArray("2001:0db8:85a3:08d3:1319:8a2e:0370", 2));
        Assert.assertNull(Address.toByteArray("2001:0db8:85a3:08d3:1319:8a2e:0370:193A:BCdE", 2));
        Assert.assertNull(Address.toByteArray("2001:0gb8:85a3:08d3:1319:8a2e:0370:9819", 2));
        Assert.assertNull(Address.toByteArray("lmno:0bb8:85a3:08d3:1319:8a2e:0370:9819", 2));
        Assert.assertNull(Address.toByteArray("11ab:0ab8:85a3:08d3:1319:8a2e:0370:qrst", 2));
        Assert.assertNull(Address.toByteArray("11ab:0ab8:85a3:08d3:::", 2));
        Assert.assertNull(Address.toByteArray("2001:0ab8:192.168.0.1:1319:8a2e:0370:9819", 2));
        Assert.assertNull(Address.toByteArray("2001:0ab8:1212:AbAb:8a2e:345.12.22.1", 2));
        Assert.assertNull(Address.toByteArray("2001:0ab8:85a3:128d3:1319:8a2e:0370:9819", 2));
    }
    
    public void test_toArray() {
        int[] exp = { 1, 2, 3, 4 };
        int[] ret = Address.toArray("1.2.3.4", 1);
        this.assertEquals(exp, ret);
        exp = new int[] { 0, 0, 0, 0 };
        ret = Address.toArray("0.0.0.0", 1);
        this.assertEquals(exp, ret);
        exp = new int[] { 255, 255, 255, 255 };
        ret = Address.toArray("255.255.255.255", 1);
        this.assertEquals(exp, ret);
    }
    
    public void test_toArray_invalid() {
        Assert.assertNull(Address.toArray("128.121.1", 1));
        Assert.assertNull(Address.toArray(""));
    }
    
    public void test_isDottedQuad() {
        Assert.assertTrue(Address.isDottedQuad("1.2.3.4"));
        Assert.assertFalse(Address.isDottedQuad("256.2.3.4"));
    }
    
    public void test_toDottedQuad() {
        Assert.assertEquals("128.176.201.1", Address.toDottedQuad(new byte[] { -128, -80, -55, 1 }));
        Assert.assertEquals("200.1.255.128", Address.toDottedQuad(new int[] { 200, 1, 255, 128 }));
    }
    
    public void test_addressLength() {
        Assert.assertEquals(4, Address.addressLength(1));
        Assert.assertEquals(16, Address.addressLength(2));
        try {
            Address.addressLength(3);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex) {}
    }
    
    public void test_getByName() throws UnknownHostException {
        InetAddress out = Address.getByName("128.145.198.231");
        Assert.assertEquals("128.145.198.231", out.getHostAddress());
        out = Address.getByName("serl.cs.colorado.edu");
        Assert.assertEquals("serl.cs.colorado.edu", out.getCanonicalHostName());
        Assert.assertEquals("128.138.207.163", out.getHostAddress());
    }
    
    public void test_getByName_invalid() throws UnknownHostException {
        try {
            Address.getByName("bogushost.com");
            Assert.fail("UnknownHostException not thrown");
        }
        catch (UnknownHostException ex) {}
        try {
            Address.getByName("");
            Assert.fail("UnknownHostException not thrown");
        }
        catch (UnknownHostException ex2) {}
    }
    
    public void test_getAllByName() throws UnknownHostException {
        InetAddress[] out = Address.getAllByName("128.145.198.231");
        Assert.assertEquals(1, out.length);
        Assert.assertEquals("128.145.198.231", out[0].getHostAddress());
        out = Address.getAllByName("serl.cs.colorado.edu");
        Assert.assertEquals(1, out.length);
        Assert.assertEquals("serl.cs.colorado.edu", out[0].getCanonicalHostName());
        Assert.assertEquals("128.138.207.163", out[0].getHostAddress());
        out = Address.getAllByName("cnn.com");
        Assert.assertTrue(out.length > 1);
        for (int i = 0; i < out.length; ++i) {
            Assert.assertTrue(out[i].getHostName().endsWith("cnn.com"));
        }
    }
    
    public void test_getAllByName_invalid() throws UnknownHostException {
        try {
            Address.getAllByName("bogushost.com");
            Assert.fail("UnknownHostException not thrown");
        }
        catch (UnknownHostException ex) {}
        try {
            Address.getAllByName("");
            Assert.fail("UnknownHostException not thrown");
        }
        catch (UnknownHostException ex2) {}
    }
    
    public void test_familyOf() throws UnknownHostException {
        Assert.assertEquals(1, Address.familyOf(InetAddress.getByName("192.168.0.1")));
        Assert.assertEquals(2, Address.familyOf(InetAddress.getByName("1:2:3:4:5:6:7:8")));
        try {
            Address.familyOf(null);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex) {}
    }
    
    public void test_getHostName() throws UnknownHostException {
        final String out = Address.getHostName(InetAddress.getByName("128.138.207.163"));
        Assert.assertEquals("serl.cs.colorado.edu.", out);
        try {
            Address.getHostName(InetAddress.getByName("192.168.1.1"));
            Assert.fail("UnknownHostException not thrown");
        }
        catch (UnknownHostException ex) {}
    }
}
