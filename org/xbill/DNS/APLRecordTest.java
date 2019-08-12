// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.util.Arrays;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import junit.framework.Assert;
import java.net.UnknownHostException;
import java.net.InetAddress;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;

public class APLRecordTest
{
    public static Test suite() {
        final TestSuite s = new TestSuite();
        s.addTestSuite(Test_Element_init.class);
        s.addTestSuite(Test_init.class);
        s.addTestSuite(Test_rrFromWire.class);
        s.addTestSuite(Test_rdataFromString.class);
        s.addTestSuite(Test_rrToString.class);
        s.addTestSuite(Test_rrToWire.class);
        return s;
    }
    
    public static class Test_Element_init extends TestCase
    {
        InetAddress m_addr4;
        InetAddress m_addr6;
        
        protected void setUp() throws TextParseException, UnknownHostException {
            this.m_addr4 = InetAddress.getByName("193.160.232.5");
            this.m_addr6 = InetAddress.getByName("2001:db8:85a3:8d3:1319:8a2e:370:7334");
        }
        
        public void test_valid_IPv4() {
            final APLRecord.Element el = new APLRecord.Element(true, this.m_addr4, 16);
            Assert.assertEquals(1, el.family);
            Assert.assertEquals(true, el.negative);
            Assert.assertEquals(this.m_addr4, el.address);
            Assert.assertEquals(16, el.prefixLength);
        }
        
        public void test_invalid_IPv4() {
            try {
                new APLRecord.Element(true, this.m_addr4, 33);
                Assert.fail("IllegalArgumentException not thrown");
            }
            catch (IllegalArgumentException ex) {}
        }
        
        public void test_valid_IPv6() {
            final APLRecord.Element el = new APLRecord.Element(false, this.m_addr6, 74);
            Assert.assertEquals(2, el.family);
            Assert.assertEquals(false, el.negative);
            Assert.assertEquals(this.m_addr6, el.address);
            Assert.assertEquals(74, el.prefixLength);
        }
        
        public void test_invalid_IPv6() {
            try {
                new APLRecord.Element(true, this.m_addr6, 129);
                Assert.fail("IllegalArgumentException not thrown");
            }
            catch (IllegalArgumentException ex) {}
        }
    }
    
    public static class Test_init extends TestCase
    {
        Name m_an;
        Name m_rn;
        long m_ttl;
        ArrayList m_elements;
        InetAddress m_addr4;
        String m_addr4_string;
        byte[] m_addr4_bytes;
        InetAddress m_addr6;
        String m_addr6_string;
        byte[] m_addr6_bytes;
        
        protected void setUp() throws TextParseException, UnknownHostException {
            this.m_an = Name.fromString("My.Absolute.Name.");
            this.m_rn = Name.fromString("My.Relative.Name");
            this.m_ttl = 79225L;
            this.m_addr4_string = "193.160.232.5";
            this.m_addr4 = InetAddress.getByName(this.m_addr4_string);
            this.m_addr4_bytes = this.m_addr4.getAddress();
            this.m_addr6_string = "2001:db8:85a3:8d3:1319:8a2e:370:7334";
            this.m_addr6 = InetAddress.getByName(this.m_addr6_string);
            this.m_addr6_bytes = this.m_addr6.getAddress();
            this.m_elements = new ArrayList(2);
            APLRecord.Element e = new APLRecord.Element(true, this.m_addr4, 12);
            this.m_elements.add(e);
            e = new APLRecord.Element(false, this.m_addr6, 64);
            this.m_elements.add(e);
        }
        
        public void test_0arg() throws UnknownHostException {
            final APLRecord ar = new APLRecord();
            Assert.assertNull(ar.getName());
            Assert.assertEquals(0, ar.getType());
            Assert.assertEquals(0, ar.getDClass());
            Assert.assertEquals(0L, ar.getTTL());
            Assert.assertNull(ar.getElements());
        }
        
        public void test_getObject() {
            final APLRecord ar = new APLRecord();
            final Record r = ar.getObject();
            Assert.assertTrue(r instanceof APLRecord);
        }
        
        public void test_4arg_basic() {
            final APLRecord ar = new APLRecord(this.m_an, 1, this.m_ttl, this.m_elements);
            Assert.assertEquals(this.m_an, ar.getName());
            Assert.assertEquals(42, ar.getType());
            Assert.assertEquals(1, ar.getDClass());
            Assert.assertEquals(this.m_ttl, ar.getTTL());
            Assert.assertEquals(this.m_elements, ar.getElements());
        }
        
        public void test_4arg_empty_elements() {
            final APLRecord ar = new APLRecord(this.m_an, 1, this.m_ttl, new ArrayList());
            Assert.assertEquals(new ArrayList(), ar.getElements());
        }
        
        public void test_4arg_relative_name() {
            try {
                new APLRecord(this.m_rn, 1, this.m_ttl, this.m_elements);
                Assert.fail("RelativeNameException not thrown");
            }
            catch (RelativeNameException ex) {}
        }
        
        public void test_4arg_invalid_elements() {
            (this.m_elements = new ArrayList()).add(new Object());
            try {
                new APLRecord(this.m_an, 1, this.m_ttl, this.m_elements);
                Assert.fail("IllegalArgumentException not thrown");
            }
            catch (IllegalArgumentException ex) {}
        }
    }
    
    public static class Test_rrFromWire extends TestCase
    {
        InetAddress m_addr4;
        byte[] m_addr4_bytes;
        InetAddress m_addr6;
        byte[] m_addr6_bytes;
        
        protected void setUp() throws TextParseException, UnknownHostException {
            this.m_addr4 = InetAddress.getByName("193.160.232.5");
            this.m_addr4_bytes = this.m_addr4.getAddress();
            this.m_addr6 = InetAddress.getByName("2001:db8:85a3:8d3:1319:8a2e:370:7334");
            this.m_addr6_bytes = this.m_addr6.getAddress();
        }
        
        public void test_validIPv4() throws IOException {
            final byte[] raw = { 0, 1, 8, -124, this.m_addr4_bytes[0], this.m_addr4_bytes[1], this.m_addr4_bytes[2], this.m_addr4_bytes[3] };
            final DNSInput di = new DNSInput(raw);
            final APLRecord ar = new APLRecord();
            ar.rrFromWire(di);
            final ArrayList exp = new ArrayList();
            exp.add(new APLRecord.Element(true, this.m_addr4, 8));
            Assert.assertEquals(exp, ar.getElements());
        }
        
        public void test_validIPv4_short_address() throws IOException {
            final byte[] raw = { 0, 1, 20, -125, this.m_addr4_bytes[0], this.m_addr4_bytes[1], this.m_addr4_bytes[2] };
            final DNSInput di = new DNSInput(raw);
            final APLRecord ar = new APLRecord();
            ar.rrFromWire(di);
            final InetAddress a = InetAddress.getByName("193.160.232.0");
            final ArrayList exp = new ArrayList();
            exp.add(new APLRecord.Element(true, a, 20));
            Assert.assertEquals(exp, ar.getElements());
        }
        
        public void test_invalid_IPv4_prefix() throws IOException {
            final byte[] raw = { 0, 1, 33, -124, this.m_addr4_bytes[0], this.m_addr4_bytes[1], this.m_addr4_bytes[2], this.m_addr4_bytes[3] };
            final DNSInput di = new DNSInput(raw);
            final APLRecord ar = new APLRecord();
            try {
                ar.rrFromWire(di);
                Assert.fail("WireParseException not thrown");
            }
            catch (WireParseException ex) {}
        }
        
        public void test_invalid_IPv4_length() throws IOException {
            final byte[] raw = { 0, 1, 8, -123, this.m_addr4_bytes[0], this.m_addr4_bytes[1], this.m_addr4_bytes[2], this.m_addr4_bytes[3], 10 };
            final DNSInput di = new DNSInput(raw);
            final APLRecord ar = new APLRecord();
            try {
                ar.rrFromWire(di);
                Assert.fail("WireParseException not thrown");
            }
            catch (WireParseException ex) {}
        }
        
        public void test_multiple_validIPv4() throws IOException {
            final byte[] raw = { 0, 1, 8, -124, this.m_addr4_bytes[0], this.m_addr4_bytes[1], this.m_addr4_bytes[2], this.m_addr4_bytes[3], 0, 1, 30, 4, this.m_addr4_bytes[0], this.m_addr4_bytes[1], this.m_addr4_bytes[2], this.m_addr4_bytes[3] };
            final DNSInput di = new DNSInput(raw);
            final APLRecord ar = new APLRecord();
            ar.rrFromWire(di);
            final ArrayList exp = new ArrayList();
            exp.add(new APLRecord.Element(true, this.m_addr4, 8));
            exp.add(new APLRecord.Element(false, this.m_addr4, 30));
            Assert.assertEquals(exp, ar.getElements());
        }
        
        public void test_validIPv6() throws IOException {
            final byte[] raw = { 0, 2, 115, 16, this.m_addr6_bytes[0], this.m_addr6_bytes[1], this.m_addr6_bytes[2], this.m_addr6_bytes[3], this.m_addr6_bytes[4], this.m_addr6_bytes[5], this.m_addr6_bytes[6], this.m_addr6_bytes[7], this.m_addr6_bytes[8], this.m_addr6_bytes[9], this.m_addr6_bytes[10], this.m_addr6_bytes[11], this.m_addr6_bytes[12], this.m_addr6_bytes[13], this.m_addr6_bytes[14], this.m_addr6_bytes[15] };
            final DNSInput di = new DNSInput(raw);
            final APLRecord ar = new APLRecord();
            ar.rrFromWire(di);
            final ArrayList exp = new ArrayList();
            exp.add(new APLRecord.Element(false, this.m_addr6, 115));
            Assert.assertEquals(exp, ar.getElements());
        }
        
        public void test_valid_nonIP() throws IOException {
            final byte[] raw = { 0, 3, -126, -123, 1, 2, 3, 4, 5 };
            final DNSInput di = new DNSInput(raw);
            final APLRecord ar = new APLRecord();
            ar.rrFromWire(di);
            final List l = ar.getElements();
            Assert.assertEquals(1, l.size());
            final APLRecord.Element el = l.get(0);
            Assert.assertEquals(3, el.family);
            Assert.assertEquals(true, el.negative);
            Assert.assertEquals(130, el.prefixLength);
            Assert.assertTrue(Arrays.equals(new byte[] { 1, 2, 3, 4, 5 }, (byte[])el.address));
        }
    }
    
    public static class Test_rdataFromString extends TestCase
    {
        InetAddress m_addr4;
        String m_addr4_string;
        byte[] m_addr4_bytes;
        InetAddress m_addr6;
        String m_addr6_string;
        byte[] m_addr6_bytes;
        
        protected void setUp() throws TextParseException, UnknownHostException {
            this.m_addr4_string = "193.160.232.5";
            this.m_addr4 = InetAddress.getByName(this.m_addr4_string);
            this.m_addr4_bytes = this.m_addr4.getAddress();
            this.m_addr6_string = "2001:db8:85a3:8d3:1319:8a2e:370:7334";
            this.m_addr6 = InetAddress.getByName(this.m_addr6_string);
            this.m_addr6_bytes = this.m_addr6.getAddress();
        }
        
        public void test_validIPv4() throws IOException {
            final Tokenizer t = new Tokenizer("1:" + this.m_addr4_string + "/11\n");
            final APLRecord ar = new APLRecord();
            ar.rdataFromString(t, null);
            final ArrayList exp = new ArrayList();
            exp.add(new APLRecord.Element(false, this.m_addr4, 11));
            Assert.assertEquals(exp, ar.getElements());
            Assert.assertEquals(1, t.get().type);
        }
        
        public void test_valid_multi() throws IOException {
            final Tokenizer t = new Tokenizer("1:" + this.m_addr4_string + "/11 !2:" + this.m_addr6_string + "/100");
            final APLRecord ar = new APLRecord();
            ar.rdataFromString(t, null);
            final ArrayList exp = new ArrayList();
            exp.add(new APLRecord.Element(false, this.m_addr4, 11));
            exp.add(new APLRecord.Element(true, this.m_addr6, 100));
            Assert.assertEquals(exp, ar.getElements());
        }
        
        public void test_validIPv6() throws IOException {
            final Tokenizer t = new Tokenizer("!2:" + this.m_addr6_string + "/36\n");
            final APLRecord ar = new APLRecord();
            ar.rdataFromString(t, null);
            final ArrayList exp = new ArrayList();
            exp.add(new APLRecord.Element(true, this.m_addr6, 36));
            Assert.assertEquals(exp, ar.getElements());
            Assert.assertEquals(1, t.get().type);
        }
        
        public void test_no_colon() throws IOException {
            final Tokenizer t = new Tokenizer("!1192.68.0.1/20");
            final APLRecord ar = new APLRecord();
            try {
                ar.rdataFromString(t, null);
                Assert.fail("TextParseException not thrown");
            }
            catch (TextParseException ex) {}
        }
        
        public void test_colon_and_slash_swapped() throws IOException {
            final Tokenizer t = new Tokenizer("!1/192.68.0.1:20");
            final APLRecord ar = new APLRecord();
            try {
                ar.rdataFromString(t, null);
                Assert.fail("TextParseException not thrown");
            }
            catch (TextParseException ex) {}
        }
        
        public void test_no_slash() throws IOException {
            final Tokenizer t = new Tokenizer("!1:192.68.0.1|20");
            final APLRecord ar = new APLRecord();
            try {
                ar.rdataFromString(t, null);
                Assert.fail("TextParseException not thrown");
            }
            catch (TextParseException ex) {}
        }
        
        public void test_empty_family() throws IOException {
            final Tokenizer t = new Tokenizer("!:192.68.0.1/20");
            final APLRecord ar = new APLRecord();
            try {
                ar.rdataFromString(t, null);
                Assert.fail("TextParseException not thrown");
            }
            catch (TextParseException ex) {}
        }
        
        public void test_malformed_family() throws IOException {
            final Tokenizer t = new Tokenizer("family:192.68.0.1/20");
            final APLRecord ar = new APLRecord();
            try {
                ar.rdataFromString(t, null);
                Assert.fail("TextParseException not thrown");
            }
            catch (TextParseException ex) {}
        }
        
        public void test_invalid_family() throws IOException {
            final Tokenizer t = new Tokenizer("3:192.68.0.1/20");
            final APLRecord ar = new APLRecord();
            try {
                ar.rdataFromString(t, null);
                Assert.fail("TextParseException not thrown");
            }
            catch (TextParseException ex) {}
        }
        
        public void test_empty_prefix() throws IOException {
            final Tokenizer t = new Tokenizer("1:192.68.0.1/");
            final APLRecord ar = new APLRecord();
            try {
                ar.rdataFromString(t, null);
                Assert.fail("TextParseException not thrown");
            }
            catch (TextParseException ex) {}
        }
        
        public void test_malformed_prefix() throws IOException {
            final Tokenizer t = new Tokenizer("1:192.68.0.1/prefix");
            final APLRecord ar = new APLRecord();
            try {
                ar.rdataFromString(t, null);
                Assert.fail("TextParseException not thrown");
            }
            catch (TextParseException ex) {}
        }
        
        public void test_invalid_prefix() throws IOException {
            final Tokenizer t = new Tokenizer("1:192.68.0.1/33");
            final APLRecord ar = new APLRecord();
            try {
                ar.rdataFromString(t, null);
                Assert.fail("TextParseException not thrown");
            }
            catch (TextParseException ex) {}
        }
        
        public void test_empty_address() throws IOException {
            final Tokenizer t = new Tokenizer("1:/33");
            final APLRecord ar = new APLRecord();
            try {
                ar.rdataFromString(t, null);
                Assert.fail("TextParseException not thrown");
            }
            catch (TextParseException ex) {}
        }
        
        public void test_malformed_address() throws IOException {
            final Tokenizer t = new Tokenizer("1:A.B.C.D/33");
            final APLRecord ar = new APLRecord();
            try {
                ar.rdataFromString(t, null);
                Assert.fail("TextParseException not thrown");
            }
            catch (TextParseException ex) {}
        }
    }
    
    public static class Test_rrToString extends TestCase
    {
        Name m_an;
        Name m_rn;
        long m_ttl;
        ArrayList m_elements;
        InetAddress m_addr4;
        String m_addr4_string;
        byte[] m_addr4_bytes;
        InetAddress m_addr6;
        String m_addr6_string;
        byte[] m_addr6_bytes;
        
        protected void setUp() throws TextParseException, UnknownHostException {
            this.m_an = Name.fromString("My.Absolute.Name.");
            this.m_rn = Name.fromString("My.Relative.Name");
            this.m_ttl = 79225L;
            this.m_addr4_string = "193.160.232.5";
            this.m_addr4 = InetAddress.getByName(this.m_addr4_string);
            this.m_addr4_bytes = this.m_addr4.getAddress();
            this.m_addr6_string = "2001:db8:85a3:8d3:1319:8a2e:370:7334";
            this.m_addr6 = InetAddress.getByName(this.m_addr6_string);
            this.m_addr6_bytes = this.m_addr6.getAddress();
            this.m_elements = new ArrayList(2);
            APLRecord.Element e = new APLRecord.Element(true, this.m_addr4, 12);
            this.m_elements.add(e);
            e = new APLRecord.Element(false, this.m_addr6, 64);
            this.m_elements.add(e);
        }
        
        public void test() {
            final APLRecord ar = new APLRecord(this.m_an, 1, this.m_ttl, this.m_elements);
            Assert.assertEquals("!1:" + this.m_addr4_string + "/12 2:" + this.m_addr6_string + "/64", ar.rrToString());
        }
    }
    
    public static class Test_rrToWire extends TestCase
    {
        Name m_an;
        Name m_rn;
        long m_ttl;
        ArrayList m_elements;
        InetAddress m_addr4;
        String m_addr4_string;
        byte[] m_addr4_bytes;
        InetAddress m_addr6;
        String m_addr6_string;
        byte[] m_addr6_bytes;
        
        protected void setUp() throws TextParseException, UnknownHostException {
            this.m_an = Name.fromString("My.Absolute.Name.");
            this.m_rn = Name.fromString("My.Relative.Name");
            this.m_ttl = 79225L;
            this.m_addr4_string = "193.160.232.5";
            this.m_addr4 = InetAddress.getByName(this.m_addr4_string);
            this.m_addr4_bytes = this.m_addr4.getAddress();
            this.m_addr6_string = "2001:db8:85a3:8d3:1319:8a2e:370:7334";
            this.m_addr6 = InetAddress.getByName(this.m_addr6_string);
            this.m_addr6_bytes = this.m_addr6.getAddress();
            this.m_elements = new ArrayList(2);
            APLRecord.Element e = new APLRecord.Element(true, this.m_addr4, 12);
            this.m_elements.add(e);
            e = new APLRecord.Element(false, this.m_addr6, 64);
            this.m_elements.add(e);
        }
        
        public void test_empty() {
            final APLRecord ar = new APLRecord(this.m_an, 1, this.m_ttl, new ArrayList());
            final DNSOutput dout = new DNSOutput();
            ar.rrToWire(dout, null, true);
            Assert.assertTrue(Arrays.equals(new byte[0], dout.toByteArray()));
        }
        
        public void test_basic() {
            final APLRecord ar = new APLRecord(this.m_an, 1, this.m_ttl, this.m_elements);
            final byte[] exp = { 0, 1, 12, -124, this.m_addr4_bytes[0], this.m_addr4_bytes[1], this.m_addr4_bytes[2], this.m_addr4_bytes[3], 0, 2, 64, 16, this.m_addr6_bytes[0], this.m_addr6_bytes[1], this.m_addr6_bytes[2], this.m_addr6_bytes[3], this.m_addr6_bytes[4], this.m_addr6_bytes[5], this.m_addr6_bytes[6], this.m_addr6_bytes[7], this.m_addr6_bytes[8], this.m_addr6_bytes[9], this.m_addr6_bytes[10], this.m_addr6_bytes[11], this.m_addr6_bytes[12], this.m_addr6_bytes[13], this.m_addr6_bytes[14], this.m_addr6_bytes[15] };
            final DNSOutput dout = new DNSOutput();
            ar.rrToWire(dout, null, true);
            Assert.assertTrue(Arrays.equals(exp, dout.toByteArray()));
        }
        
        public void test_non_IP() throws IOException {
            final byte[] exp = { 0, 3, -126, -123, 1, 2, 3, 4, 5 };
            final DNSInput di = new DNSInput(exp);
            final APLRecord ar = new APLRecord();
            ar.rrFromWire(di);
            final DNSOutput dout = new DNSOutput();
            ar.rrToWire(dout, null, true);
            Assert.assertTrue(Arrays.equals(exp, dout.toByteArray()));
        }
        
        public void test_address_with_embedded_zero() throws UnknownHostException {
            final InetAddress a = InetAddress.getByName("232.0.11.1");
            final ArrayList elements = new ArrayList();
            elements.add(new APLRecord.Element(true, a, 31));
            final APLRecord ar = new APLRecord(this.m_an, 1, this.m_ttl, elements);
            final byte[] exp = { 0, 1, 31, -124, -24, 0, 11, 1 };
            final DNSOutput dout = new DNSOutput();
            ar.rrToWire(dout, null, true);
            Assert.assertTrue(Arrays.equals(exp, dout.toByteArray()));
        }
        
        public void test_short_address() throws UnknownHostException {
            final InetAddress a = InetAddress.getByName("232.0.11.0");
            final ArrayList elements = new ArrayList();
            elements.add(new APLRecord.Element(true, a, 31));
            final APLRecord ar = new APLRecord(this.m_an, 1, this.m_ttl, elements);
            final byte[] exp = { 0, 1, 31, -125, -24, 0, 11 };
            final DNSOutput dout = new DNSOutput();
            ar.rrToWire(dout, null, true);
            Assert.assertTrue(Arrays.equals(exp, dout.toByteArray()));
        }
        
        public void test_wildcard_address() throws UnknownHostException {
            final InetAddress a = InetAddress.getByName("0.0.0.0");
            final ArrayList elements = new ArrayList();
            elements.add(new APLRecord.Element(true, a, 31));
            final APLRecord ar = new APLRecord(this.m_an, 1, this.m_ttl, elements);
            final byte[] exp = { 0, 1, 31, -128 };
            final DNSOutput dout = new DNSOutput();
            ar.rrToWire(dout, null, true);
            Assert.assertTrue(Arrays.equals(exp, dout.toByteArray()));
        }
    }
}
