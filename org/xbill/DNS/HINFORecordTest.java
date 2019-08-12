// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.util.Arrays;
import java.io.IOException;
import junit.framework.Assert;
import junit.framework.TestCase;

public class HINFORecordTest extends TestCase
{
    public void test_ctor_0arg() {
        final HINFORecord dr = new HINFORecord();
        Assert.assertNull(dr.getName());
        Assert.assertEquals(0, dr.getType());
        Assert.assertEquals(0, dr.getDClass());
        Assert.assertEquals(0L, dr.getTTL());
    }
    
    public void test_getObject() {
        final HINFORecord dr = new HINFORecord();
        final Record r = dr.getObject();
        Assert.assertTrue(r instanceof HINFORecord);
    }
    
    public void test_ctor_5arg() throws TextParseException {
        final Name n = Name.fromString("The.Name.");
        final long ttl = 43981L;
        final String cpu = "i686 Intel(R) Pentium(R) M processor 1.70GHz GenuineIntel GNU/Linux";
        final String os = "Linux troy 2.6.10-gentoo-r6 #8 Wed Apr 6 21:25:04 MDT 2005";
        final HINFORecord dr = new HINFORecord(n, 1, ttl, cpu, os);
        Assert.assertEquals(n, dr.getName());
        Assert.assertEquals(1, dr.getDClass());
        Assert.assertEquals(13, dr.getType());
        Assert.assertEquals(ttl, dr.getTTL());
        Assert.assertEquals(cpu, dr.getCPU());
        Assert.assertEquals(os, dr.getOS());
    }
    
    public void test_ctor_5arg_invalid_CPU() throws TextParseException {
        final Name n = Name.fromString("The.Name.");
        final long ttl = 43981L;
        final String cpu = "i686 Intel(R) Pentium(R) M \\256 processor 1.70GHz GenuineIntel GNU/Linux";
        final String os = "Linux troy 2.6.10-gentoo-r6 #8 Wed Apr 6 21:25:04 MDT 2005";
        try {
            new HINFORecord(n, 1, ttl, cpu, os);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex) {}
    }
    
    public void test_ctor_5arg_invalid_OS() throws TextParseException {
        final Name n = Name.fromString("The.Name.");
        final long ttl = 43981L;
        final String cpu = "i686 Intel(R) Pentium(R) M processor 1.70GHz GenuineIntel GNU/Linux";
        final String os = "Linux troy 2.6.10-gentoo-r6 \\1 #8 Wed Apr 6 21:25:04 MDT 2005";
        try {
            new HINFORecord(n, 1, ttl, cpu, os);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex) {}
    }
    
    public void test_rrFromWire() throws IOException {
        final String cpu = "Intel(R) Pentium(R) M processor 1.70GHz";
        final String os = "Linux troy 2.6.10-gentoo-r6";
        final byte[] raw = { 39, 73, 110, 116, 101, 108, 40, 82, 41, 32, 80, 101, 110, 116, 105, 117, 109, 40, 82, 41, 32, 77, 32, 112, 114, 111, 99, 101, 115, 115, 111, 114, 32, 49, 46, 55, 48, 71, 72, 122, 27, 76, 105, 110, 117, 120, 32, 116, 114, 111, 121, 32, 50, 46, 54, 46, 49, 48, 45, 103, 101, 110, 116, 111, 111, 45, 114, 54 };
        final DNSInput in = new DNSInput(raw);
        final HINFORecord dr = new HINFORecord();
        dr.rrFromWire(in);
        Assert.assertEquals(cpu, dr.getCPU());
        Assert.assertEquals(os, dr.getOS());
    }
    
    public void test_rdataFromString() throws IOException {
        final String cpu = "Intel(R) Pentium(R) M processor 1.70GHz";
        final String os = "Linux troy 2.6.10-gentoo-r6";
        final Tokenizer t = new Tokenizer("\"" + cpu + "\" \"" + os + "\"");
        final HINFORecord dr = new HINFORecord();
        dr.rdataFromString(t, null);
        Assert.assertEquals(cpu, dr.getCPU());
        Assert.assertEquals(os, dr.getOS());
    }
    
    public void test_rdataFromString_invalid_CPU() throws IOException {
        final String cpu = "Intel(R) Pentium(R) \\388 M processor 1.70GHz";
        final String os = "Linux troy 2.6.10-gentoo-r6";
        final Tokenizer t = new Tokenizer("\"" + cpu + "\" \"" + os + "\"");
        final HINFORecord dr = new HINFORecord();
        try {
            dr.rdataFromString(t, null);
            Assert.fail("TextParseException not thrown");
        }
        catch (TextParseException ex) {}
    }
    
    public void test_rdataFromString_invalid_OS() throws IOException {
        final String cpu = "Intel(R) Pentium(R) M processor 1.70GHz";
        final Tokenizer t = new Tokenizer("\"" + cpu + "\"");
        final HINFORecord dr = new HINFORecord();
        try {
            dr.rdataFromString(t, null);
            Assert.fail("TextParseException not thrown");
        }
        catch (TextParseException ex) {}
    }
    
    public void test_rrToString() throws TextParseException {
        final String cpu = "Intel(R) Pentium(R) M processor 1.70GHz";
        final String os = "Linux troy 2.6.10-gentoo-r6";
        final String exp = "\"" + cpu + "\" \"" + os + "\"";
        final HINFORecord dr = new HINFORecord(Name.fromString("The.Name."), 1, 291L, cpu, os);
        Assert.assertEquals(exp, dr.rrToString());
    }
    
    public void test_rrToWire() throws TextParseException {
        final String cpu = "Intel(R) Pentium(R) M processor 1.70GHz";
        final String os = "Linux troy 2.6.10-gentoo-r6";
        final byte[] raw = { 39, 73, 110, 116, 101, 108, 40, 82, 41, 32, 80, 101, 110, 116, 105, 117, 109, 40, 82, 41, 32, 77, 32, 112, 114, 111, 99, 101, 115, 115, 111, 114, 32, 49, 46, 55, 48, 71, 72, 122, 27, 76, 105, 110, 117, 120, 32, 116, 114, 111, 121, 32, 50, 46, 54, 46, 49, 48, 45, 103, 101, 110, 116, 111, 111, 45, 114, 54 };
        final HINFORecord dr = new HINFORecord(Name.fromString("The.Name."), 1, 291L, cpu, os);
        final DNSOutput out = new DNSOutput();
        dr.rrToWire(out, null, true);
        Assert.assertTrue(Arrays.equals(raw, out.toByteArray()));
    }
}
