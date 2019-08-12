// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.io.IOException;
import java.util.Arrays;
import java.net.UnknownHostException;
import junit.framework.Assert;
import junit.framework.TestCase;

public class DNSKEYRecordTest extends TestCase
{
    public void test_ctor_0arg() throws UnknownHostException {
        final DNSKEYRecord ar = new DNSKEYRecord();
        Assert.assertNull(ar.getName());
        Assert.assertEquals(0, ar.getType());
        Assert.assertEquals(0, ar.getDClass());
        Assert.assertEquals(0L, ar.getTTL());
        Assert.assertEquals(0, ar.getAlgorithm());
        Assert.assertEquals(0, ar.getFlags());
        Assert.assertEquals(0, ar.getFootprint());
        Assert.assertEquals(0, ar.getProtocol());
        Assert.assertNull(ar.getKey());
    }
    
    public void test_getObject() {
        final DNSKEYRecord ar = new DNSKEYRecord();
        final Record r = ar.getObject();
        Assert.assertTrue(r instanceof DNSKEYRecord);
    }
    
    public void test_ctor_7arg() throws TextParseException {
        final Name n = Name.fromString("My.Absolute.Name.");
        final Name r = Name.fromString("My.Relative.Name");
        final byte[] key = { 0, 1, 3, 5, 7, 9 };
        final DNSKEYRecord kr = new DNSKEYRecord(n, 1, 9388L, 38962, 18, 103, key);
        Assert.assertEquals(n, kr.getName());
        Assert.assertEquals(48, kr.getType());
        Assert.assertEquals(1, kr.getDClass());
        Assert.assertEquals(9388L, kr.getTTL());
        Assert.assertEquals(38962, kr.getFlags());
        Assert.assertEquals(18, kr.getProtocol());
        Assert.assertEquals(103, kr.getAlgorithm());
        Assert.assertTrue(Arrays.equals(key, kr.getKey()));
        try {
            new DNSKEYRecord(r, 1, 9388L, 38962, 18, 103, key);
            Assert.fail("RelativeNameException not thrown");
        }
        catch (RelativeNameException ex) {}
    }
    
    public void test_rdataFromString() throws IOException, TextParseException {
        DNSKEYRecord kr = new DNSKEYRecord();
        Tokenizer st = new Tokenizer("43981 129 RSASHA1 AQIDBAUGBwgJ");
        kr.rdataFromString(st, null);
        Assert.assertEquals(43981, kr.getFlags());
        Assert.assertEquals(129, kr.getProtocol());
        Assert.assertEquals(5, kr.getAlgorithm());
        Assert.assertTrue(Arrays.equals(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 }, kr.getKey()));
        kr = new DNSKEYRecord();
        st = new Tokenizer("4626 170 ZONE AQIDBAUGBwgJ");
        try {
            kr.rdataFromString(st, null);
            Assert.fail("TextParseException not thrown");
        }
        catch (TextParseException ex) {}
    }
}
