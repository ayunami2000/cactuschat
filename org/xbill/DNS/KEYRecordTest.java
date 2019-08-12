// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.io.IOException;
import java.util.Arrays;
import java.net.UnknownHostException;
import junit.framework.Assert;
import junit.framework.TestCase;

public class KEYRecordTest extends TestCase
{
    public void test_ctor_0arg() throws UnknownHostException {
        final KEYRecord ar = new KEYRecord();
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
        final KEYRecord ar = new KEYRecord();
        final Record r = ar.getObject();
        Assert.assertTrue(r instanceof KEYRecord);
    }
    
    public void test_ctor_7arg() throws TextParseException {
        final Name n = Name.fromString("My.Absolute.Name.");
        final Name r = Name.fromString("My.Relative.Name");
        final byte[] key = { 0, 1, 3, 5, 7, 9 };
        final KEYRecord kr = new KEYRecord(n, 1, 9388L, 38962, 18, 103, key);
        Assert.assertEquals(n, kr.getName());
        Assert.assertEquals(25, kr.getType());
        Assert.assertEquals(1, kr.getDClass());
        Assert.assertEquals(9388L, kr.getTTL());
        Assert.assertEquals(38962, kr.getFlags());
        Assert.assertEquals(18, kr.getProtocol());
        Assert.assertEquals(103, kr.getAlgorithm());
        Assert.assertTrue(Arrays.equals(key, kr.getKey()));
        try {
            new KEYRecord(r, 1, 9388L, 38962, 18, 103, key);
            Assert.fail("RelativeNameException not thrown");
        }
        catch (RelativeNameException ex) {}
    }
    
    public void test_Protocol_string() {
        Assert.assertEquals("DNSSEC", KEYRecord.Protocol.string(3));
        Assert.assertEquals("254", KEYRecord.Protocol.string(254));
        try {
            KEYRecord.Protocol.string(-1);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex) {}
        try {
            KEYRecord.Protocol.string(256);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex2) {}
    }
    
    public void test_Protocol_value() {
        Assert.assertEquals(4, KEYRecord.Protocol.value("IPSEC"));
        Assert.assertEquals(254, KEYRecord.Protocol.value("254"));
        Assert.assertEquals(-1, KEYRecord.Protocol.value("-2"));
        Assert.assertEquals(-1, KEYRecord.Protocol.value("256"));
    }
    
    public void test_Flags_value() {
        Assert.assertEquals(-1, KEYRecord.Flags.value("-2"));
        Assert.assertEquals(0, KEYRecord.Flags.value("0"));
        Assert.assertEquals(43829, KEYRecord.Flags.value("43829"));
        Assert.assertEquals(65535, KEYRecord.Flags.value("65535"));
        Assert.assertEquals(-1, KEYRecord.Flags.value("65536"));
        Assert.assertEquals(4096, KEYRecord.Flags.value("EXTEND"));
        Assert.assertEquals(-1, KEYRecord.Flags.value("NOT_A_VALID_NAME"));
        Assert.assertEquals(33056, KEYRecord.Flags.value("NOAUTH|ZONE|FLAG10"));
        Assert.assertEquals(-1, KEYRecord.Flags.value("NOAUTH|INVALID_NAME|FLAG10"));
        Assert.assertEquals(0, KEYRecord.Flags.value("|"));
    }
    
    public void test_rdataFromString() throws IOException, TextParseException {
        KEYRecord kr = new KEYRecord();
        Tokenizer st = new Tokenizer("NOAUTH|ZONE|FLAG10 EMAIL RSASHA1 AQIDBAUGBwgJ");
        kr.rdataFromString(st, null);
        Assert.assertEquals(33056, kr.getFlags());
        Assert.assertEquals(2, kr.getProtocol());
        Assert.assertEquals(5, kr.getAlgorithm());
        Assert.assertTrue(Arrays.equals(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 }, kr.getKey()));
        kr = new KEYRecord();
        st = new Tokenizer("NOAUTH|NOKEY|FLAG10 TLS ECC");
        kr.rdataFromString(st, null);
        Assert.assertEquals(49184, kr.getFlags());
        Assert.assertEquals(1, kr.getProtocol());
        Assert.assertEquals(4, kr.getAlgorithm());
        Assert.assertNull(kr.getKey());
        kr = new KEYRecord();
        st = new Tokenizer("NOAUTH|ZONE|JUNK EMAIL RSASHA1 AQIDBAUGBwgJ");
        try {
            kr.rdataFromString(st, null);
            Assert.fail("TextParseException not thrown");
        }
        catch (TextParseException ex) {}
        kr = new KEYRecord();
        st = new Tokenizer("NOAUTH|ZONE RSASHA1 ECC AQIDBAUGBwgJ");
        try {
            kr.rdataFromString(st, null);
            Assert.fail("TextParseException not thrown");
        }
        catch (TextParseException ex2) {}
        kr = new KEYRecord();
        st = new Tokenizer("NOAUTH|ZONE EMAIL ZONE AQIDBAUGBwgJ");
        try {
            kr.rdataFromString(st, null);
            Assert.fail("TextParseException not thrown");
        }
        catch (TextParseException ex3) {}
    }
}
