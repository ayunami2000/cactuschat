// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.util.Arrays;
import java.io.IOException;
import junit.framework.Assert;
import java.net.UnknownHostException;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;
import java.util.Random;

public class SOARecordTest
{
    private static final Random m_random;
    
    private static long randomU16() {
        return SOARecordTest.m_random.nextLong() >>> 48;
    }
    
    private static long randomU32() {
        return SOARecordTest.m_random.nextLong() >>> 32;
    }
    
    public static Test suite() {
        final TestSuite s = new TestSuite();
        s.addTestSuite(Test_init.class);
        s.addTestSuite(Test_rrFromWire.class);
        s.addTestSuite(Test_rdataFromString.class);
        s.addTestSuite(Test_rrToString.class);
        s.addTestSuite(Test_rrToWire.class);
        return s;
    }
    
    static {
        m_random = new Random();
    }
    
    public static class Test_init extends TestCase
    {
        private Name m_an;
        private Name m_rn;
        private Name m_host;
        private Name m_admin;
        private long m_ttl;
        private long m_serial;
        private long m_refresh;
        private long m_retry;
        private long m_expire;
        private long m_minimum;
        
        protected void setUp() throws TextParseException, UnknownHostException {
            this.m_an = Name.fromString("My.Absolute.Name.");
            this.m_rn = Name.fromString("My.Relative.Name");
            this.m_host = Name.fromString("My.Host.Name.");
            this.m_admin = Name.fromString("My.Administrative.Name.");
            this.m_ttl = randomU16();
            this.m_serial = randomU32();
            this.m_refresh = randomU32();
            this.m_retry = randomU32();
            this.m_expire = randomU32();
            this.m_minimum = randomU32();
        }
        
        public void test_0arg() throws UnknownHostException {
            final SOARecord ar = new SOARecord();
            Assert.assertNull(ar.getName());
            Assert.assertEquals(0, ar.getType());
            Assert.assertEquals(0, ar.getDClass());
            Assert.assertEquals(0L, ar.getTTL());
            Assert.assertNull(ar.getHost());
            Assert.assertNull(ar.getAdmin());
            Assert.assertEquals(0L, ar.getSerial());
            Assert.assertEquals(0L, ar.getRefresh());
            Assert.assertEquals(0L, ar.getRetry());
            Assert.assertEquals(0L, ar.getExpire());
            Assert.assertEquals(0L, ar.getMinimum());
        }
        
        public void test_getObject() {
            final SOARecord ar = new SOARecord();
            final Record r = ar.getObject();
            Assert.assertTrue(r instanceof SOARecord);
        }
        
        public void test_10arg() {
            final SOARecord ar = new SOARecord(this.m_an, 1, this.m_ttl, this.m_host, this.m_admin, this.m_serial, this.m_refresh, this.m_retry, this.m_expire, this.m_minimum);
            Assert.assertEquals(this.m_an, ar.getName());
            Assert.assertEquals(6, ar.getType());
            Assert.assertEquals(1, ar.getDClass());
            Assert.assertEquals(this.m_ttl, ar.getTTL());
            Assert.assertEquals(this.m_host, ar.getHost());
            Assert.assertEquals(this.m_admin, ar.getAdmin());
            Assert.assertEquals(this.m_serial, ar.getSerial());
            Assert.assertEquals(this.m_refresh, ar.getRefresh());
            Assert.assertEquals(this.m_retry, ar.getRetry());
            Assert.assertEquals(this.m_expire, ar.getExpire());
            Assert.assertEquals(this.m_minimum, ar.getMinimum());
        }
        
        public void test_10arg_relative_name() {
            try {
                new SOARecord(this.m_rn, 1, this.m_ttl, this.m_host, this.m_admin, this.m_serial, this.m_refresh, this.m_retry, this.m_expire, this.m_minimum);
                Assert.fail("RelativeNameException not thrown");
            }
            catch (RelativeNameException ex) {}
        }
        
        public void test_10arg_relative_host() {
            try {
                new SOARecord(this.m_an, 1, this.m_ttl, this.m_rn, this.m_admin, this.m_serial, this.m_refresh, this.m_retry, this.m_expire, this.m_minimum);
                Assert.fail("RelativeNameException not thrown");
            }
            catch (RelativeNameException ex) {}
        }
        
        public void test_10arg_relative_admin() {
            try {
                new SOARecord(this.m_an, 1, this.m_ttl, this.m_host, this.m_rn, this.m_serial, this.m_refresh, this.m_retry, this.m_expire, this.m_minimum);
                Assert.fail("RelativeNameException not thrown");
            }
            catch (RelativeNameException ex) {}
        }
        
        public void test_10arg_negative_serial() {
            try {
                new SOARecord(this.m_an, 1, this.m_ttl, this.m_host, this.m_admin, -1L, this.m_refresh, this.m_retry, this.m_expire, this.m_minimum);
                Assert.fail("IllegalArgumentException not thrown");
            }
            catch (IllegalArgumentException ex) {}
        }
        
        public void test_10arg_toobig_serial() {
            try {
                new SOARecord(this.m_an, 1, this.m_ttl, this.m_host, this.m_admin, 4294967296L, this.m_refresh, this.m_retry, this.m_expire, this.m_minimum);
                Assert.fail("IllegalArgumentException not thrown");
            }
            catch (IllegalArgumentException ex) {}
        }
        
        public void test_10arg_negative_refresh() {
            try {
                new SOARecord(this.m_an, 1, this.m_ttl, this.m_host, this.m_admin, this.m_serial, -1L, this.m_retry, this.m_expire, this.m_minimum);
                Assert.fail("IllegalArgumentException not thrown");
            }
            catch (IllegalArgumentException ex) {}
        }
        
        public void test_10arg_toobig_refresh() {
            try {
                new SOARecord(this.m_an, 1, this.m_ttl, this.m_host, this.m_admin, this.m_serial, 4294967296L, this.m_retry, this.m_expire, this.m_minimum);
                Assert.fail("IllegalArgumentException not thrown");
            }
            catch (IllegalArgumentException ex) {}
        }
        
        public void test_10arg_negative_retry() {
            try {
                new SOARecord(this.m_an, 1, this.m_ttl, this.m_host, this.m_admin, this.m_serial, this.m_refresh, -1L, this.m_expire, this.m_minimum);
                Assert.fail("IllegalArgumentException not thrown");
            }
            catch (IllegalArgumentException ex) {}
        }
        
        public void test_10arg_toobig_retry() {
            try {
                new SOARecord(this.m_an, 1, this.m_ttl, this.m_host, this.m_admin, this.m_serial, this.m_refresh, 4294967296L, this.m_expire, this.m_minimum);
                Assert.fail("IllegalArgumentException not thrown");
            }
            catch (IllegalArgumentException ex) {}
        }
        
        public void test_10arg_negative_expire() {
            try {
                new SOARecord(this.m_an, 1, this.m_ttl, this.m_host, this.m_admin, this.m_serial, this.m_refresh, this.m_retry, -1L, this.m_minimum);
                Assert.fail("IllegalArgumentException not thrown");
            }
            catch (IllegalArgumentException ex) {}
        }
        
        public void test_10arg_toobig_expire() {
            try {
                new SOARecord(this.m_an, 1, this.m_ttl, this.m_host, this.m_admin, this.m_serial, this.m_refresh, this.m_retry, 4294967296L, this.m_minimum);
                Assert.fail("IllegalArgumentException not thrown");
            }
            catch (IllegalArgumentException ex) {}
        }
        
        public void test_10arg_negative_minimun() {
            try {
                new SOARecord(this.m_an, 1, this.m_ttl, this.m_host, this.m_admin, this.m_serial, this.m_refresh, this.m_retry, this.m_expire, -1L);
                Assert.fail("IllegalArgumentException not thrown");
            }
            catch (IllegalArgumentException ex) {}
        }
        
        public void test_10arg_toobig_minimum() {
            try {
                new SOARecord(this.m_an, 1, this.m_ttl, this.m_host, this.m_admin, this.m_serial, this.m_refresh, this.m_retry, this.m_expire, 4294967296L);
                Assert.fail("IllegalArgumentException not thrown");
            }
            catch (IllegalArgumentException ex) {}
        }
    }
    
    public static class Test_rrFromWire extends TestCase
    {
        private Name m_host;
        private Name m_admin;
        private long m_serial;
        private long m_refresh;
        private long m_retry;
        private long m_expire;
        private long m_minimum;
        
        protected void setUp() throws TextParseException, UnknownHostException {
            this.m_host = Name.fromString("M.h.N.");
            this.m_admin = Name.fromString("M.a.n.");
            this.m_serial = 2882400018L;
            this.m_refresh = 3454997044L;
            this.m_retry = 4010947670L;
            this.m_expire = 305419896L;
            this.m_minimum = 878082202L;
        }
        
        public void test() throws IOException {
            final byte[] raw = { 1, 109, 1, 104, 1, 110, 0, 1, 109, 1, 97, 1, 110, 0, -85, -51, -17, 18, -51, -17, 18, 52, -17, 18, 52, 86, 18, 52, 86, 120, 52, 86, 120, -102 };
            final DNSInput di = new DNSInput(raw);
            final SOARecord ar = new SOARecord();
            ar.rrFromWire(di);
            Assert.assertEquals(this.m_host, ar.getHost());
            Assert.assertEquals(this.m_admin, ar.getAdmin());
            Assert.assertEquals(this.m_serial, ar.getSerial());
            Assert.assertEquals(this.m_refresh, ar.getRefresh());
            Assert.assertEquals(this.m_retry, ar.getRetry());
            Assert.assertEquals(this.m_expire, ar.getExpire());
            Assert.assertEquals(this.m_minimum, ar.getMinimum());
        }
    }
    
    public static class Test_rdataFromString extends TestCase
    {
        private Name m_host;
        private Name m_admin;
        private Name m_origin;
        private long m_serial;
        private long m_refresh;
        private long m_retry;
        private long m_expire;
        private long m_minimum;
        
        protected void setUp() throws TextParseException, UnknownHostException {
            this.m_origin = Name.fromString("O.");
            this.m_host = Name.fromString("M.h", this.m_origin);
            this.m_admin = Name.fromString("M.a.n.");
            this.m_serial = 2882400018L;
            this.m_refresh = 3454997044L;
            this.m_retry = 4010947670L;
            this.m_expire = 305419896L;
            this.m_minimum = 878082202L;
        }
        
        public void test_valid() throws IOException {
            final Tokenizer t = new Tokenizer("M.h " + this.m_admin + " " + this.m_serial + " " + this.m_refresh + " " + this.m_retry + " " + this.m_expire + " " + this.m_minimum);
            final SOARecord ar = new SOARecord();
            ar.rdataFromString(t, this.m_origin);
            Assert.assertEquals(this.m_host, ar.getHost());
            Assert.assertEquals(this.m_admin, ar.getAdmin());
            Assert.assertEquals(this.m_serial, ar.getSerial());
            Assert.assertEquals(this.m_refresh, ar.getRefresh());
            Assert.assertEquals(this.m_retry, ar.getRetry());
            Assert.assertEquals(this.m_expire, ar.getExpire());
            Assert.assertEquals(this.m_minimum, ar.getMinimum());
        }
        
        public void test_relative_name() throws IOException {
            final Tokenizer t = new Tokenizer("M.h " + this.m_admin + " " + this.m_serial + " " + this.m_refresh + " " + this.m_retry + " " + this.m_expire + " " + this.m_minimum);
            final SOARecord ar = new SOARecord();
            try {
                ar.rdataFromString(t, null);
                Assert.fail("RelativeNameException not thrown");
            }
            catch (RelativeNameException ex) {}
        }
    }
    
    public static class Test_rrToString extends TestCase
    {
        private Name m_an;
        private Name m_host;
        private Name m_admin;
        private long m_ttl;
        private long m_serial;
        private long m_refresh;
        private long m_retry;
        private long m_expire;
        private long m_minimum;
        
        protected void setUp() throws TextParseException {
            this.m_an = Name.fromString("My.absolute.name.");
            this.m_ttl = 5032L;
            this.m_host = Name.fromString("M.h.N.");
            this.m_admin = Name.fromString("M.a.n.");
            this.m_serial = 2882400018L;
            this.m_refresh = 3454997044L;
            this.m_retry = 4010947670L;
            this.m_expire = 305419896L;
            this.m_minimum = 878082202L;
        }
        
        public void test_singleLine() {
            final SOARecord ar = new SOARecord(this.m_an, 1, this.m_ttl, this.m_host, this.m_admin, this.m_serial, this.m_refresh, this.m_retry, this.m_expire, this.m_minimum);
            final String exp = this.m_host + " " + this.m_admin + " " + this.m_serial + " " + this.m_refresh + " " + this.m_retry + " " + this.m_expire + " " + this.m_minimum;
            final String out = ar.rrToString();
            Assert.assertEquals(exp, out);
        }
        
        public void test_multiLine() {
            final SOARecord ar = new SOARecord(this.m_an, 1, this.m_ttl, this.m_host, this.m_admin, this.m_serial, this.m_refresh, this.m_retry, this.m_expire, this.m_minimum);
            final String re = "^.*\\(\\n\\s*" + this.m_serial + "\\s*;\\s*serial\\n" + "\\s*" + this.m_refresh + "\\s*;\\s*refresh\\n" + "\\s*" + this.m_retry + "\\s*;\\s*retry\\n" + "\\s*" + this.m_expire + "\\s*;\\s*expire\\n" + "\\s*" + this.m_minimum + "\\s*\\)\\s*;\\s*minimum$";
            Options.set("multiline");
            final String out = ar.rrToString();
            Options.unset("multiline");
            Assert.assertTrue(out.matches(re));
        }
    }
    
    public static class Test_rrToWire extends TestCase
    {
        private Name m_an;
        private Name m_host;
        private Name m_admin;
        private long m_ttl;
        private long m_serial;
        private long m_refresh;
        private long m_retry;
        private long m_expire;
        private long m_minimum;
        
        protected void setUp() throws TextParseException {
            this.m_an = Name.fromString("My.Abs.Name.");
            this.m_ttl = 5032L;
            this.m_host = Name.fromString("M.h.N.");
            this.m_admin = Name.fromString("M.a.n.");
            this.m_serial = 2882400018L;
            this.m_refresh = 3454997044L;
            this.m_retry = 4010947670L;
            this.m_expire = 305419896L;
            this.m_minimum = 878082202L;
        }
        
        public void test_canonical() {
            final byte[] exp = { 1, 109, 1, 104, 1, 110, 0, 1, 109, 1, 97, 1, 110, 0, -85, -51, -17, 18, -51, -17, 18, 52, -17, 18, 52, 86, 18, 52, 86, 120, 52, 86, 120, -102 };
            final SOARecord ar = new SOARecord(this.m_an, 1, this.m_ttl, this.m_host, this.m_admin, this.m_serial, this.m_refresh, this.m_retry, this.m_expire, this.m_minimum);
            final DNSOutput o = new DNSOutput();
            ar.rrToWire(o, null, true);
            Assert.assertTrue(Arrays.equals(exp, o.toByteArray()));
        }
        
        public void test_case_sensitive() {
            final byte[] exp = { 1, 77, 1, 104, 1, 78, 0, 1, 77, 1, 97, 1, 110, 0, -85, -51, -17, 18, -51, -17, 18, 52, -17, 18, 52, 86, 18, 52, 86, 120, 52, 86, 120, -102 };
            final SOARecord ar = new SOARecord(this.m_an, 1, this.m_ttl, this.m_host, this.m_admin, this.m_serial, this.m_refresh, this.m_retry, this.m_expire, this.m_minimum);
            final DNSOutput o = new DNSOutput();
            ar.rrToWire(o, null, false);
            Assert.assertTrue(Arrays.equals(exp, o.toByteArray()));
        }
    }
}
