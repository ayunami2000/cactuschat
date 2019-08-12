// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.net.InetAddress;
import junit.framework.Assert;
import junit.framework.TestCase;

public class SetResponseTest extends TestCase
{
    public void test_ctor_1arg() {
        final int[] types = { 0, 1, 2, 3, 4, 5, 6 };
        for (int i = 0; i < types.length; ++i) {
            final SetResponse sr = new SetResponse(types[i]);
            Assert.assertNull(sr.getNS());
            Assert.assertEquals(types[i] == 0, sr.isUnknown());
            Assert.assertEquals(types[i] == 1, sr.isNXDOMAIN());
            Assert.assertEquals(types[i] == 2, sr.isNXRRSET());
            Assert.assertEquals(types[i] == 3, sr.isDelegation());
            Assert.assertEquals(types[i] == 4, sr.isCNAME());
            Assert.assertEquals(types[i] == 5, sr.isDNAME());
            Assert.assertEquals(types[i] == 6, sr.isSuccessful());
        }
    }
    
    public void test_ctor_1arg_toosmall() {
        try {
            new SetResponse(-1);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex) {}
    }
    
    public void test_ctor_1arg_toobig() {
        try {
            new SetResponse(7);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex) {}
    }
    
    public void test_ctor_2arg() {
        final int[] types = { 0, 1, 2, 3, 4, 5, 6 };
        for (int i = 0; i < types.length; ++i) {
            final RRset rs = new RRset();
            final SetResponse sr = new SetResponse(types[i], rs);
            Assert.assertSame(rs, sr.getNS());
            Assert.assertEquals(types[i] == 0, sr.isUnknown());
            Assert.assertEquals(types[i] == 1, sr.isNXDOMAIN());
            Assert.assertEquals(types[i] == 2, sr.isNXRRSET());
            Assert.assertEquals(types[i] == 3, sr.isDelegation());
            Assert.assertEquals(types[i] == 4, sr.isCNAME());
            Assert.assertEquals(types[i] == 5, sr.isDNAME());
            Assert.assertEquals(types[i] == 6, sr.isSuccessful());
        }
    }
    
    public void test_ctor_2arg_toosmall() {
        try {
            final SetResponse setResponse = new SetResponse(-1, new RRset());
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex) {}
    }
    
    public void test_ctor_2arg_toobig() {
        try {
            final SetResponse setResponse = new SetResponse(7, new RRset());
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex) {}
    }
    
    public void test_ofType_basic() {
        final int[] types = { 3, 4, 5, 6 };
        for (int i = 0; i < types.length; ++i) {
            final SetResponse sr = SetResponse.ofType(types[i]);
            Assert.assertNull(sr.getNS());
            Assert.assertEquals(types[i] == 0, sr.isUnknown());
            Assert.assertEquals(types[i] == 1, sr.isNXDOMAIN());
            Assert.assertEquals(types[i] == 2, sr.isNXRRSET());
            Assert.assertEquals(types[i] == 3, sr.isDelegation());
            Assert.assertEquals(types[i] == 4, sr.isCNAME());
            Assert.assertEquals(types[i] == 5, sr.isDNAME());
            Assert.assertEquals(types[i] == 6, sr.isSuccessful());
            final SetResponse sr2 = SetResponse.ofType(types[i]);
            Assert.assertNotSame(sr, sr2);
        }
    }
    
    public void test_ofType_singleton() {
        final int[] types = { 0, 1, 2 };
        for (int i = 0; i < types.length; ++i) {
            final SetResponse sr = SetResponse.ofType(types[i]);
            Assert.assertNull(sr.getNS());
            Assert.assertEquals(types[i] == 0, sr.isUnknown());
            Assert.assertEquals(types[i] == 1, sr.isNXDOMAIN());
            Assert.assertEquals(types[i] == 2, sr.isNXRRSET());
            Assert.assertEquals(types[i] == 3, sr.isDelegation());
            Assert.assertEquals(types[i] == 4, sr.isCNAME());
            Assert.assertEquals(types[i] == 5, sr.isDNAME());
            Assert.assertEquals(types[i] == 6, sr.isSuccessful());
            final SetResponse sr2 = SetResponse.ofType(types[i]);
            Assert.assertSame(sr, sr2);
        }
    }
    
    public void test_ofType_toosmall() {
        try {
            SetResponse.ofType(-1);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex) {}
    }
    
    public void test_ofType_toobig() {
        try {
            SetResponse.ofType(7);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex) {}
    }
    
    public void test_addRRset() throws TextParseException, UnknownHostException {
        final RRset rrs = new RRset();
        rrs.addRR(new ARecord(Name.fromString("The.Name."), 1, 43981L, InetAddress.getByName("192.168.0.1")));
        rrs.addRR(new ARecord(Name.fromString("The.Name."), 1, 43981L, InetAddress.getByName("192.168.0.2")));
        final SetResponse sr = new SetResponse(6);
        sr.addRRset(rrs);
        final RRset[] exp = { rrs };
        Assert.assertTrue(Arrays.equals(exp, sr.answers()));
    }
    
    public void test_addRRset_multiple() throws TextParseException, UnknownHostException {
        final RRset rrs = new RRset();
        rrs.addRR(new ARecord(Name.fromString("The.Name."), 1, 43981L, InetAddress.getByName("192.168.0.1")));
        rrs.addRR(new ARecord(Name.fromString("The.Name."), 1, 43981L, InetAddress.getByName("192.168.0.2")));
        final RRset rrs2 = new RRset();
        rrs2.addRR(new ARecord(Name.fromString("The.Other.Name."), 1, 43982L, InetAddress.getByName("192.168.1.1")));
        rrs2.addRR(new ARecord(Name.fromString("The.Other.Name."), 1, 43982L, InetAddress.getByName("192.168.1.2")));
        final SetResponse sr = new SetResponse(6);
        sr.addRRset(rrs);
        sr.addRRset(rrs2);
        final RRset[] exp = { rrs, rrs2 };
        Assert.assertTrue(Arrays.equals(exp, sr.answers()));
    }
    
    public void test_answers_nonSUCCESSFUL() {
        final SetResponse sr = new SetResponse(0, new RRset());
        Assert.assertNull(sr.answers());
    }
    
    public void test_getCNAME() throws TextParseException, UnknownHostException {
        final RRset rrs = new RRset();
        final CNAMERecord cr = new CNAMERecord(Name.fromString("The.Name."), 1, 43981L, Name.fromString("The.Alias."));
        rrs.addRR(cr);
        final SetResponse sr = new SetResponse(4, rrs);
        Assert.assertEquals(cr, sr.getCNAME());
    }
    
    public void test_getDNAME() throws TextParseException, UnknownHostException {
        final RRset rrs = new RRset();
        final DNAMERecord dr = new DNAMERecord(Name.fromString("The.Name."), 1, 43981L, Name.fromString("The.Alias."));
        rrs.addRR(dr);
        final SetResponse sr = new SetResponse(5, rrs);
        Assert.assertEquals(dr, sr.getDNAME());
    }
    
    public void test_toString() throws TextParseException, UnknownHostException {
        final int[] types = { 0, 1, 2, 3, 4, 5, 6 };
        final RRset rrs = new RRset();
        rrs.addRR(new ARecord(Name.fromString("The.Name."), 1, 43981L, InetAddress.getByName("192.168.0.1")));
        final String[] labels = { "unknown", "NXDOMAIN", "NXRRSET", "delegation: " + rrs, "CNAME: " + rrs, "DNAME: " + rrs, "successful" };
        for (int i = 0; i < types.length; ++i) {
            final SetResponse sr = new SetResponse(types[i], rrs);
            Assert.assertEquals(labels[i], sr.toString());
        }
    }
}
