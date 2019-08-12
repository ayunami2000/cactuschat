// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.net.UnknownHostException;
import java.net.InetAddress;
import junit.framework.Assert;
import junit.framework.TestCase;

public class ReverseMapTest extends TestCase
{
    public void test_fromAddress_ipv4() throws UnknownHostException, TextParseException {
        final Name exp = Name.fromString("1.0.168.192.in-addr.arpa.");
        final String addr = "192.168.0.1";
        Assert.assertEquals(exp, ReverseMap.fromAddress(addr));
        Assert.assertEquals(exp, ReverseMap.fromAddress(addr, 1));
        Assert.assertEquals(exp, ReverseMap.fromAddress(InetAddress.getByName(addr)));
        Assert.assertEquals(exp, ReverseMap.fromAddress(new byte[] { -64, -88, 0, 1 }));
        Assert.assertEquals(exp, ReverseMap.fromAddress(new int[] { 192, 168, 0, 1 }));
    }
    
    public void test_fromAddress_ipv6() throws UnknownHostException, TextParseException {
        final Name exp = Name.fromString("4.3.3.7.0.7.3.0.E.2.A.8.9.1.3.1.3.D.8.0.3.A.5.8.8.B.D.0.1.0.0.2.ip6.arpa.");
        final String addr = "2001:0db8:85a3:08d3:1319:8a2e:0370:7334";
        final byte[] dat = { 32, 1, 13, -72, -123, -93, 8, -45, 19, 25, -118, 46, 3, 112, 115, 52 };
        final int[] idat = { 32, 1, 13, 184, 133, 163, 8, 211, 19, 25, 138, 46, 3, 112, 115, 52 };
        Assert.assertEquals(exp, ReverseMap.fromAddress(addr, 2));
        Assert.assertEquals(exp, ReverseMap.fromAddress(InetAddress.getByName(addr)));
        Assert.assertEquals(exp, ReverseMap.fromAddress(dat));
        Assert.assertEquals(exp, ReverseMap.fromAddress(idat));
    }
    
    public void test_fromAddress_invalid() {
        try {
            ReverseMap.fromAddress("A.B.C.D", 1);
            Assert.fail("UnknownHostException not thrown");
        }
        catch (UnknownHostException ex) {}
        try {
            ReverseMap.fromAddress(new byte[0]);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex2) {}
        try {
            ReverseMap.fromAddress(new byte[3]);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex3) {}
        try {
            ReverseMap.fromAddress(new byte[5]);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex4) {}
        try {
            ReverseMap.fromAddress(new byte[15]);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex5) {}
        try {
            ReverseMap.fromAddress(new byte[17]);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex6) {}
        try {
            final int[] dat = { 0, 1, 2, 256 };
            ReverseMap.fromAddress(dat);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex7) {}
    }
}
