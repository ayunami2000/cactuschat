// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.io.IOException;
import junit.framework.TestSuite;
import junit.framework.Test;
import junit.framework.Assert;
import junit.framework.TestCase;

public class GPOSRecordTest extends TestCase
{
    public void test_ctor_0arg() {
        final GPOSRecord gr = new GPOSRecord();
        Assert.assertNull(gr.getName());
        Assert.assertEquals(0, gr.getType());
        Assert.assertEquals(0, gr.getDClass());
        Assert.assertEquals(0L, gr.getTTL());
    }
    
    public void test_getObject() {
        final GPOSRecord gr = new GPOSRecord();
        final Record r = gr.getObject();
        Assert.assertTrue(r instanceof GPOSRecord);
    }
    
    public void test_rrToString() throws TextParseException {
        final String exp = "\"10.45\" \"171.121212\" \"1010787.0\"";
        final GPOSRecord gr = new GPOSRecord(Name.fromString("The.Name."), 1, 291L, 10.45, 171.121212, 1010787.0);
        Assert.assertEquals(exp, gr.rrToString());
    }
    
    public void test_rrToWire() throws TextParseException {
        final GPOSRecord gr = new GPOSRecord(Name.fromString("The.Name."), 1, 291L, -10.45, 120.0, 111.0);
        final byte[] exp = { 6, 45, 49, 48, 46, 52, 53, 5, 49, 50, 48, 46, 48, 5, 49, 49, 49, 46, 48 };
        final DNSOutput out = new DNSOutput();
        gr.rrToWire(out, null, true);
        final byte[] bar = out.toByteArray();
        Assert.assertEquals(exp.length, bar.length);
        for (int i = 0; i < exp.length; ++i) {
            Assert.assertEquals("i=" + i, exp[i], bar[i]);
        }
    }
    
    public static Test suite() {
        final TestSuite s = new TestSuite();
        s.addTestSuite(Test_Ctor_6arg_doubles.class);
        s.addTestSuite(Test_Ctor_6arg_Strings.class);
        s.addTestSuite(Test_rrFromWire.class);
        s.addTestSuite(Test_rdataFromString.class);
        s.addTestSuite(GPOSRecordTest.class);
        return s;
    }
    
    public static class Test_Ctor_6arg_doubles extends TestCase
    {
        private Name m_n;
        private long m_ttl;
        private double m_lat;
        private double m_long;
        private double m_alt;
        
        protected void setUp() throws TextParseException {
            this.m_n = Name.fromString("The.Name.");
            this.m_ttl = 43981L;
            this.m_lat = -10.43;
            this.m_long = 76.12;
            this.m_alt = 100.101;
        }
        
        public void test_basic() throws TextParseException {
            final GPOSRecord gr = new GPOSRecord(this.m_n, 1, this.m_ttl, this.m_long, this.m_lat, this.m_alt);
            Assert.assertEquals(this.m_n, gr.getName());
            Assert.assertEquals(1, gr.getDClass());
            Assert.assertEquals(27, gr.getType());
            Assert.assertEquals(this.m_ttl, gr.getTTL());
            Assert.assertEquals(new Double(this.m_long), new Double(gr.getLongitude()));
            Assert.assertEquals(new Double(this.m_lat), new Double(gr.getLatitude()));
            Assert.assertEquals(new Double(this.m_alt), new Double(gr.getAltitude()));
            Assert.assertEquals(new Double(this.m_long).toString(), gr.getLongitudeString());
            Assert.assertEquals(new Double(this.m_lat).toString(), gr.getLatitudeString());
            Assert.assertEquals(new Double(this.m_alt).toString(), gr.getAltitudeString());
        }
        
        public void test_toosmall_longitude() throws TextParseException {
            try {
                new GPOSRecord(this.m_n, 1, this.m_ttl, -90.001, this.m_lat, this.m_alt);
                Assert.fail("IllegalArgumentException not thrown");
            }
            catch (IllegalArgumentException ex) {}
        }
        
        public void test_toobig_longitude() throws TextParseException {
            try {
                new GPOSRecord(this.m_n, 1, this.m_ttl, 90.001, this.m_lat, this.m_alt);
                Assert.fail("IllegalArgumentException not thrown");
            }
            catch (IllegalArgumentException ex) {}
        }
        
        public void test_toosmall_latitude() throws TextParseException {
            try {
                new GPOSRecord(this.m_n, 1, this.m_ttl, this.m_long, -180.001, this.m_alt);
                Assert.fail("IllegalArgumentException not thrown");
            }
            catch (IllegalArgumentException ex) {}
        }
        
        public void test_toobig_latitude() throws TextParseException {
            try {
                new GPOSRecord(this.m_n, 1, this.m_ttl, this.m_long, 180.001, this.m_alt);
                Assert.fail("IllegalArgumentException not thrown");
            }
            catch (IllegalArgumentException ex) {}
        }
        
        public void test_invalid_string() {
            try {
                final GPOSRecord gposRecord = new GPOSRecord(this.m_n, 1, this.m_ttl, new Double(this.m_long).toString(), "120.\\00ABC", new Double(this.m_alt).toString());
                Assert.fail("IllegalArgumentException not thrown");
            }
            catch (IllegalArgumentException ex) {}
        }
    }
    
    public static class Test_Ctor_6arg_Strings extends TestCase
    {
        private Name m_n;
        private long m_ttl;
        private double m_lat;
        private double m_long;
        private double m_alt;
        
        protected void setUp() throws TextParseException {
            this.m_n = Name.fromString("The.Name.");
            this.m_ttl = 43981L;
            this.m_lat = -10.43;
            this.m_long = 76.12;
            this.m_alt = 100.101;
        }
        
        public void test_basic() throws TextParseException {
            final GPOSRecord gr = new GPOSRecord(this.m_n, 1, this.m_ttl, new Double(this.m_long).toString(), new Double(this.m_lat).toString(), new Double(this.m_alt).toString());
            Assert.assertEquals(this.m_n, gr.getName());
            Assert.assertEquals(1, gr.getDClass());
            Assert.assertEquals(27, gr.getType());
            Assert.assertEquals(this.m_ttl, gr.getTTL());
            Assert.assertEquals(new Double(this.m_long), new Double(gr.getLongitude()));
            Assert.assertEquals(new Double(this.m_lat), new Double(gr.getLatitude()));
            Assert.assertEquals(new Double(this.m_alt), new Double(gr.getAltitude()));
            Assert.assertEquals(new Double(this.m_long).toString(), gr.getLongitudeString());
            Assert.assertEquals(new Double(this.m_lat).toString(), gr.getLatitudeString());
            Assert.assertEquals(new Double(this.m_alt).toString(), gr.getAltitudeString());
        }
        
        public void test_toosmall_longitude() throws TextParseException {
            try {
                final GPOSRecord gposRecord = new GPOSRecord(this.m_n, 1, this.m_ttl, "-90.001", new Double(this.m_lat).toString(), new Double(this.m_alt).toString());
                Assert.fail("IllegalArgumentException not thrown");
            }
            catch (IllegalArgumentException ex) {}
        }
        
        public void test_toobig_longitude() throws TextParseException {
            try {
                final GPOSRecord gposRecord = new GPOSRecord(this.m_n, 1, this.m_ttl, "90.001", new Double(this.m_lat).toString(), new Double(this.m_alt).toString());
                Assert.fail("IllegalArgumentException not thrown");
            }
            catch (IllegalArgumentException ex) {}
        }
        
        public void test_toosmall_latitude() throws TextParseException {
            try {
                final GPOSRecord gposRecord = new GPOSRecord(this.m_n, 1, this.m_ttl, new Double(this.m_long).toString(), "-180.001", new Double(this.m_alt).toString());
                Assert.fail("IllegalArgumentException not thrown");
            }
            catch (IllegalArgumentException ex) {}
        }
        
        public void test_toobig_latitude() throws TextParseException {
            try {
                final GPOSRecord gposRecord = new GPOSRecord(this.m_n, 1, this.m_ttl, new Double(this.m_long).toString(), "180.001", new Double(this.m_alt).toString());
                Assert.fail("IllegalArgumentException not thrown");
            }
            catch (IllegalArgumentException ex) {}
        }
    }
    
    public static class Test_rrFromWire extends TestCase
    {
        public void test_basic() throws IOException {
            final byte[] raw = { 5, 45, 56, 46, 49, 50, 6, 49, 50, 51, 46, 48, 55, 3, 48, 46, 48 };
            final DNSInput in = new DNSInput(raw);
            final GPOSRecord gr = new GPOSRecord();
            gr.rrFromWire(in);
            Assert.assertEquals(new Double(-8.12), new Double(gr.getLongitude()));
            Assert.assertEquals(new Double(123.07), new Double(gr.getLatitude()));
            Assert.assertEquals(new Double(0.0), new Double(gr.getAltitude()));
        }
        
        public void test_longitude_toosmall() throws IOException {
            final byte[] raw = { 5, 45, 57, 53, 46, 48, 6, 49, 50, 51, 46, 48, 55, 3, 48, 46, 48 };
            final DNSInput in = new DNSInput(raw);
            final GPOSRecord gr = new GPOSRecord();
            try {
                gr.rrFromWire(in);
                Assert.fail("WireParseException not thrown");
            }
            catch (WireParseException ex) {}
        }
        
        public void test_longitude_toobig() throws IOException {
            final byte[] raw = { 5, 49, 56, 53, 46, 48, 6, 49, 50, 51, 46, 48, 55, 3, 48, 46, 48 };
            final DNSInput in = new DNSInput(raw);
            final GPOSRecord gr = new GPOSRecord();
            try {
                gr.rrFromWire(in);
                Assert.fail("WireParseException not thrown");
            }
            catch (WireParseException ex) {}
        }
        
        public void test_latitude_toosmall() throws IOException {
            final byte[] raw = { 5, 45, 56, 53, 46, 48, 6, 45, 49, 57, 48, 46, 48, 3, 48, 46, 48 };
            final DNSInput in = new DNSInput(raw);
            final GPOSRecord gr = new GPOSRecord();
            try {
                gr.rrFromWire(in);
                Assert.fail("WireParseException not thrown");
            }
            catch (WireParseException ex) {}
        }
        
        public void test_latitude_toobig() throws IOException {
            final byte[] raw = { 5, 45, 56, 53, 46, 48, 6, 50, 49, 57, 48, 46, 48, 3, 48, 46, 48 };
            final DNSInput in = new DNSInput(raw);
            final GPOSRecord gr = new GPOSRecord();
            try {
                gr.rrFromWire(in);
                Assert.fail("WireParseException not thrown");
            }
            catch (WireParseException ex) {}
        }
    }
    
    public static class Test_rdataFromString extends TestCase
    {
        public void test_basic() throws IOException {
            final Tokenizer t = new Tokenizer("10.45 171.121212 1010787");
            final GPOSRecord gr = new GPOSRecord();
            gr.rdataFromString(t, null);
            Assert.assertEquals(new Double(10.45), new Double(gr.getLongitude()));
            Assert.assertEquals(new Double(171.121212), new Double(gr.getLatitude()));
            Assert.assertEquals(new Double(1010787.0), new Double(gr.getAltitude()));
        }
        
        public void test_longitude_toosmall() throws IOException {
            final Tokenizer t = new Tokenizer("-100.390 171.121212 1010787");
            final GPOSRecord gr = new GPOSRecord();
            try {
                gr.rdataFromString(t, null);
                Assert.fail("IOException not thrown");
            }
            catch (IOException ex) {}
        }
        
        public void test_longitude_toobig() throws IOException {
            final Tokenizer t = new Tokenizer("90.00001 171.121212 1010787");
            final GPOSRecord gr = new GPOSRecord();
            try {
                gr.rdataFromString(t, null);
                Assert.fail("IOException not thrown");
            }
            catch (IOException ex) {}
        }
        
        public void test_latitude_toosmall() throws IOException {
            final Tokenizer t = new Tokenizer("0.0 -180.01 1010787");
            final GPOSRecord gr = new GPOSRecord();
            try {
                gr.rdataFromString(t, null);
                Assert.fail("IOException not thrown");
            }
            catch (IOException ex) {}
        }
        
        public void test_latitude_toobig() throws IOException {
            final Tokenizer t = new Tokenizer("0.0 180.01 1010787");
            final GPOSRecord gr = new GPOSRecord();
            try {
                gr.rdataFromString(t, null);
                Assert.fail("IOException not thrown");
            }
            catch (IOException ex) {}
        }
        
        public void test_invalid_string() throws IOException {
            final Tokenizer t = new Tokenizer("1.0 2.0 \\435");
            try {
                final GPOSRecord gr = new GPOSRecord();
                gr.rdataFromString(t, null);
            }
            catch (TextParseException ex) {}
        }
    }
}
