// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import junit.framework.TestSuite;
import junit.framework.Test;
import java.io.IOException;
import java.util.Arrays;
import junit.framework.Assert;
import junit.framework.TestCase;

public class DSRecordTest extends TestCase
{
    public void test_ctor_0arg() {
        final DSRecord dr = new DSRecord();
        Assert.assertNull(dr.getName());
        Assert.assertEquals(0, dr.getType());
        Assert.assertEquals(0, dr.getDClass());
        Assert.assertEquals(0L, dr.getTTL());
        Assert.assertEquals(0, dr.getAlgorithm());
        Assert.assertEquals(0, dr.getDigestID());
        Assert.assertNull(dr.getDigest());
        Assert.assertEquals(0, dr.getFootprint());
    }
    
    public void test_getObject() {
        final DSRecord dr = new DSRecord();
        final Record r = dr.getObject();
        Assert.assertTrue(r instanceof DSRecord);
    }
    
    public void test_rrFromWire() throws IOException {
        final byte[] raw = { -85, -51, -17, 1, 35, 69, 103, -119 };
        final DNSInput in = new DNSInput(raw);
        final DSRecord dr = new DSRecord();
        dr.rrFromWire(in);
        Assert.assertEquals(43981, dr.getFootprint());
        Assert.assertEquals(239, dr.getAlgorithm());
        Assert.assertEquals(1, dr.getDigestID());
        Assert.assertTrue(Arrays.equals(new byte[] { 35, 69, 103, -119 }, dr.getDigest()));
    }
    
    public void test_rdataFromString() throws IOException {
        final byte[] raw = { -85, -51, -17, 1, 35, 69, 103, -119 };
        final Tokenizer t = new Tokenizer("43981 239 1 23456789AB");
        final DSRecord dr = new DSRecord();
        dr.rdataFromString(t, null);
        Assert.assertEquals(43981, dr.getFootprint());
        Assert.assertEquals(239, dr.getAlgorithm());
        Assert.assertEquals(1, dr.getDigestID());
        Assert.assertTrue(Arrays.equals(new byte[] { 35, 69, 103, -119, -85 }, dr.getDigest()));
    }
    
    public void test_rrToString() throws TextParseException {
        final String exp = "43981 239 1 23456789AB";
        final DSRecord dr = new DSRecord(Name.fromString("The.Name."), 1, 291L, 43981, 239, 1, new byte[] { 35, 69, 103, -119, -85 });
        Assert.assertEquals(exp, dr.rrToString());
    }
    
    public void test_rrToWire() throws TextParseException {
        final DSRecord dr = new DSRecord(Name.fromString("The.Name."), 1, 291L, 43981, 239, 1, new byte[] { 35, 69, 103, -119, -85 });
        final byte[] exp = { -85, -51, -17, 1, 35, 69, 103, -119, -85 };
        final DNSOutput out = new DNSOutput();
        dr.rrToWire(out, null, true);
        Assert.assertTrue(Arrays.equals(exp, out.toByteArray()));
    }
    
    public static Test suite() {
        final TestSuite s = new TestSuite();
        s.addTestSuite(Test_Ctor_7arg.class);
        s.addTestSuite(DSRecordTest.class);
        return s;
    }
    
    public static class Test_Ctor_7arg extends TestCase
    {
        private Name m_n;
        private long m_ttl;
        private int m_footprint;
        private int m_algorithm;
        private int m_digestid;
        private byte[] m_digest;
        
        protected void setUp() throws TextParseException {
            this.m_n = Name.fromString("The.Name.");
            this.m_ttl = 43981L;
            this.m_footprint = 61185;
            this.m_algorithm = 35;
            this.m_digestid = 69;
            this.m_digest = new byte[] { 103, -119, -85, -51, -17 };
        }
        
        public void test_basic() throws TextParseException {
            final DSRecord dr = new DSRecord(this.m_n, 1, this.m_ttl, this.m_footprint, this.m_algorithm, this.m_digestid, this.m_digest);
            Assert.assertEquals(this.m_n, dr.getName());
            Assert.assertEquals(1, dr.getDClass());
            Assert.assertEquals(43, dr.getType());
            Assert.assertEquals(this.m_ttl, dr.getTTL());
            Assert.assertEquals(this.m_footprint, dr.getFootprint());
            Assert.assertEquals(this.m_algorithm, dr.getAlgorithm());
            Assert.assertEquals(this.m_digestid, dr.getDigestID());
            Assert.assertTrue(Arrays.equals(this.m_digest, dr.getDigest()));
        }
        
        public void test_toosmall_footprint() throws TextParseException {
            try {
                new DSRecord(this.m_n, 1, this.m_ttl, -1, this.m_algorithm, this.m_digestid, this.m_digest);
                Assert.fail("IllegalArgumentException not thrown");
            }
            catch (IllegalArgumentException ex) {}
        }
        
        public void test_toobig_footprint() throws TextParseException {
            try {
                new DSRecord(this.m_n, 1, this.m_ttl, 65536, this.m_algorithm, this.m_digestid, this.m_digest);
                Assert.fail("IllegalArgumentException not thrown");
            }
            catch (IllegalArgumentException ex) {}
        }
        
        public void test_toosmall_algorithm() throws TextParseException {
            try {
                new DSRecord(this.m_n, 1, this.m_ttl, this.m_footprint, -1, this.m_digestid, this.m_digest);
                Assert.fail("IllegalArgumentException not thrown");
            }
            catch (IllegalArgumentException ex) {}
        }
        
        public void test_toobig_algorithm() throws TextParseException {
            try {
                new DSRecord(this.m_n, 1, this.m_ttl, this.m_footprint, 65536, this.m_digestid, this.m_digest);
                Assert.fail("IllegalArgumentException not thrown");
            }
            catch (IllegalArgumentException ex) {}
        }
        
        public void test_toosmall_digestid() throws TextParseException {
            try {
                new DSRecord(this.m_n, 1, this.m_ttl, this.m_footprint, this.m_algorithm, -1, this.m_digest);
                Assert.fail("IllegalArgumentException not thrown");
            }
            catch (IllegalArgumentException ex) {}
        }
        
        public void test_toobig_digestid() throws TextParseException {
            try {
                new DSRecord(this.m_n, 1, this.m_ttl, this.m_footprint, this.m_algorithm, 65536, this.m_digest);
                Assert.fail("IllegalArgumentException not thrown");
            }
            catch (IllegalArgumentException ex) {}
        }
        
        public void test_null_digest() {
            final DSRecord dr = new DSRecord(this.m_n, 1, this.m_ttl, this.m_footprint, this.m_algorithm, this.m_digestid, null);
            Assert.assertEquals(this.m_n, dr.getName());
            Assert.assertEquals(1, dr.getDClass());
            Assert.assertEquals(43, dr.getType());
            Assert.assertEquals(this.m_ttl, dr.getTTL());
            Assert.assertEquals(this.m_footprint, dr.getFootprint());
            Assert.assertEquals(this.m_algorithm, dr.getAlgorithm());
            Assert.assertEquals(this.m_digestid, dr.getDigestID());
            Assert.assertNull(dr.getDigest());
        }
    }
}
