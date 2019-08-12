// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.io.IOException;
import java.util.Arrays;
import junit.framework.TestSuite;
import junit.framework.Test;
import junit.framework.Assert;
import junit.framework.TestCase;

public class NameTest extends TestCase
{
    public void test_init_from_name() throws TextParseException {
        final Name n = new Name("A.B.c.d.");
        final Name e = new Name("B.c.d.");
        final Name o = new Name(n, 1);
        Assert.assertEquals(e, o);
    }
    
    public void test_init_from_name_root() throws TextParseException {
        final Name n = new Name("A.B.c.d.");
        final Name o = new Name(n, 4);
        Assert.assertEquals(Name.root, o);
    }
    
    public void test_init_from_name_empty() throws TextParseException {
        final Name n = new Name("A.B.c.d.");
        final Name n2 = new Name(n, 5);
        Assert.assertFalse(n2.isAbsolute());
        Assert.assertFalse(n2.isWild());
        Assert.assertEquals(0, n2.labels());
        Assert.assertEquals(0, n2.length());
    }
    
    public void test_concatenate_basic() throws NameTooLongException, TextParseException {
        final Name p = Name.fromString("A.B");
        final Name s = Name.fromString("c.d.");
        final Name e = Name.fromString("A.B.c.d.");
        final Name n = Name.concatenate(p, s);
        Assert.assertEquals(e, n);
    }
    
    public void test_concatenate_abs_prefix() throws NameTooLongException, TextParseException {
        final Name p = Name.fromString("A.B.");
        final Name s = Name.fromString("c.d.");
        final Name e = Name.fromString("A.B.");
        final Name n = Name.concatenate(p, s);
        Assert.assertEquals(e, n);
    }
    
    public void test_concatenate_too_long() throws TextParseException {
        final Name p = Name.fromString("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa.bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb");
        final Name s = Name.fromString("ccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc.ddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd.");
        try {
            Name.concatenate(p, s);
            Assert.fail("NameTooLongException not thrown");
        }
        catch (NameTooLongException ex) {}
    }
    
    public void test_relativize() throws TextParseException {
        final Name sub = Name.fromString("a.b.c.");
        final Name dom = Name.fromString("c.");
        final Name exp = Name.fromString("a.b");
        final Name n = sub.relativize(dom);
        Assert.assertEquals(exp, n);
    }
    
    public void test_relativize_null_origin() throws TextParseException {
        final Name sub = Name.fromString("a.b.c.");
        final Name dom = null;
        final Name n = sub.relativize(dom);
        Assert.assertEquals(sub, n);
    }
    
    public void test_relativize_disjoint() throws TextParseException {
        final Name sub = Name.fromString("a.b.c.");
        final Name dom = Name.fromString("e.f.");
        final Name n = sub.relativize(dom);
        Assert.assertEquals(sub, n);
    }
    
    public void test_relativize_root() throws TextParseException {
        final Name sub = Name.fromString("a.b.c.");
        final Name dom = Name.fromString(".");
        final Name exp = Name.fromString("a.b.c");
        final Name n = sub.relativize(dom);
        Assert.assertEquals(exp, n);
    }
    
    public void test_wild() throws TextParseException {
        final Name sub = Name.fromString("a.b.c.");
        final Name exp = Name.fromString("*.b.c.");
        final Name n = sub.wild(1);
        Assert.assertEquals(exp, n);
    }
    
    public void test_wild_abs() throws TextParseException {
        final Name sub = Name.fromString("a.b.c.");
        final Name exp = Name.fromString("*.");
        final Name n = sub.wild(3);
        Assert.assertEquals(exp, n);
    }
    
    public void test_wild_toobig() throws TextParseException {
        final Name sub = Name.fromString("a.b.c.");
        try {
            sub.wild(4);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex) {}
    }
    
    public void test_wild_toosmall() throws TextParseException {
        final Name sub = Name.fromString("a.b.c.");
        try {
            sub.wild(0);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex) {}
    }
    
    public void test_fromDNAME() throws NameTooLongException, TextParseException {
        final Name own = new Name("the.owner.");
        final Name alias = new Name("the.alias.");
        final DNAMERecord dnr = new DNAMERecord(own, 1, 43981L, alias);
        final Name sub = new Name("sub.the.owner.");
        final Name exp = new Name("sub.the.alias.");
        final Name n = sub.fromDNAME(dnr);
        Assert.assertEquals(exp, n);
    }
    
    public void test_fromDNAME_toobig() throws NameTooLongException, TextParseException {
        final Name own = new Name("the.owner.");
        final Name alias = new Name("the.aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa.bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb.ccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc.");
        final DNAMERecord dnr = new DNAMERecord(own, 1, 43981L, alias);
        final Name sub = new Name("ddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd.the.owner.");
        try {
            sub.fromDNAME(dnr);
            Assert.fail("NameTooLongException not thrown");
        }
        catch (NameTooLongException ex) {}
    }
    
    public void test_fromDNAME_disjoint() throws NameTooLongException, TextParseException {
        final Name own = new Name("the.owner.");
        final Name alias = new Name("the.alias.");
        final DNAMERecord dnr = new DNAMERecord(own, 1, 43981L, alias);
        final Name sub = new Name("sub.the.other");
        Assert.assertNull(sub.fromDNAME(dnr));
    }
    
    public void test_subdomain_abs() throws TextParseException {
        final Name dom = new Name("the.domain.");
        final Name sub = new Name("sub.of.the.domain.");
        Assert.assertTrue(sub.subdomain(dom));
        Assert.assertFalse(dom.subdomain(sub));
    }
    
    public void test_subdomain_rel() throws TextParseException {
        final Name dom = new Name("the.domain");
        final Name sub = new Name("sub.of.the.domain");
        Assert.assertTrue(sub.subdomain(dom));
        Assert.assertFalse(dom.subdomain(sub));
    }
    
    public void test_subdomain_equal() throws TextParseException {
        final Name dom = new Name("the.domain");
        final Name sub = new Name("the.domain");
        Assert.assertTrue(sub.subdomain(dom));
        Assert.assertTrue(dom.subdomain(sub));
    }
    
    public void test_toString_abs() throws TextParseException {
        final String in = "This.Is.My.Absolute.Name.";
        final Name n = new Name(in);
        Assert.assertEquals(in, n.toString());
    }
    
    public void test_toString_rel() throws TextParseException {
        final String in = "This.Is.My.Relative.Name";
        final Name n = new Name(in);
        Assert.assertEquals(in, n.toString());
    }
    
    public void test_toString_at() throws TextParseException {
        final Name n = new Name("@", null);
        Assert.assertEquals("@", n.toString());
    }
    
    public void test_toString_root() throws TextParseException {
        Assert.assertEquals(".", Name.root.toString());
    }
    
    public void test_toString_wild() throws TextParseException {
        final String in = "*.A.b.c.e";
        final Name n = new Name(in);
        Assert.assertEquals(in, n.toString());
    }
    
    public void test_toString_escaped() throws TextParseException {
        final String in = "my.escaped.junk\\128.label.";
        final Name n = new Name(in);
        Assert.assertEquals(in, n.toString());
    }
    
    public void test_toString_special_char() throws TextParseException, WireParseException {
        final byte[] raw = { 1, 34, 1, 40, 1, 41, 1, 46, 1, 59, 1, 92, 1, 64, 1, 36, 0 };
        final String exp = "\\\".\\(.\\).\\..\\;.\\\\.\\@.\\$.";
        final Name n = new Name(new DNSInput(raw));
        Assert.assertEquals(exp, n.toString());
    }
    
    public static Test suite() {
        final TestSuite s = new TestSuite();
        s.addTestSuite(Test_String_init.class);
        s.addTestSuite(Test_DNSInput_init.class);
        s.addTestSuite(NameTest.class);
        s.addTestSuite(Test_toWire.class);
        s.addTestSuite(Test_toWireCanonical.class);
        s.addTestSuite(Test_equals.class);
        s.addTestSuite(Test_compareTo.class);
        return s;
    }
    
    public static class Test_String_init extends TestCase
    {
        private final String m_abs = "WWW.DnsJava.org.";
        private Name m_abs_origin;
        private final String m_rel = "WWW.DnsJava";
        private Name m_rel_origin;
        
        protected void setUp() throws TextParseException {
            this.m_abs_origin = Name.fromString("Orig.");
            this.m_rel_origin = Name.fromString("Orig");
        }
        
        public void test_ctor_empty() {
            try {
                new Name("");
                Assert.fail("TextParseException not thrown");
            }
            catch (TextParseException ex) {}
        }
        
        public void test_ctor_at_null_origin() throws TextParseException {
            final Name n = new Name("@");
            Assert.assertFalse(n.isAbsolute());
            Assert.assertFalse(n.isWild());
            Assert.assertEquals(0, n.labels());
            Assert.assertEquals(0, n.length());
        }
        
        public void test_ctor_at_abs_origin() throws TextParseException {
            final Name n = new Name("@", this.m_abs_origin);
            Assert.assertEquals(this.m_abs_origin, n);
        }
        
        public void test_ctor_at_rel_origin() throws TextParseException {
            final Name n = new Name("@", this.m_rel_origin);
            Assert.assertEquals(this.m_rel_origin, n);
        }
        
        public void test_ctor_dot() throws TextParseException {
            final Name n = new Name(".");
            Assert.assertEquals(Name.root, n);
            Assert.assertNotSame(Name.root, n);
            Assert.assertEquals(1, n.labels());
            Assert.assertEquals(1, n.length());
        }
        
        public void test_ctor_wildcard() throws TextParseException {
            final Name n = new Name("*");
            Assert.assertFalse(n.isAbsolute());
            Assert.assertTrue(n.isWild());
            Assert.assertEquals(1, n.labels());
            Assert.assertEquals(2, n.length());
            Assert.assertTrue(Arrays.equals(new byte[] { 1, 42 }, n.getLabel(0)));
            Assert.assertEquals("*", n.getLabelString(0));
        }
        
        public void test_ctor_abs() throws TextParseException {
            final Name n = new Name("WWW.DnsJava.org.");
            Assert.assertTrue(n.isAbsolute());
            Assert.assertFalse(n.isWild());
            Assert.assertEquals(4, n.labels());
            Assert.assertEquals(17, n.length());
            Assert.assertTrue(Arrays.equals(new byte[] { 3, 87, 87, 87 }, n.getLabel(0)));
            Assert.assertEquals("WWW", n.getLabelString(0));
            Assert.assertTrue(Arrays.equals(new byte[] { 7, 68, 110, 115, 74, 97, 118, 97 }, n.getLabel(1)));
            Assert.assertEquals("DnsJava", n.getLabelString(1));
            Assert.assertTrue(Arrays.equals(new byte[] { 3, 111, 114, 103 }, n.getLabel(2)));
            Assert.assertEquals("org", n.getLabelString(2));
            Assert.assertTrue(Arrays.equals(new byte[] { 0 }, n.getLabel(3)));
            Assert.assertEquals("", n.getLabelString(3));
        }
        
        public void test_ctor_rel() throws TextParseException {
            final Name n = new Name("WWW.DnsJava");
            Assert.assertFalse(n.isAbsolute());
            Assert.assertFalse(n.isWild());
            Assert.assertEquals(2, n.labels());
            Assert.assertEquals(12, n.length());
            Assert.assertTrue(Arrays.equals(new byte[] { 3, 87, 87, 87 }, n.getLabel(0)));
            Assert.assertEquals("WWW", n.getLabelString(0));
            Assert.assertTrue(Arrays.equals(new byte[] { 7, 68, 110, 115, 74, 97, 118, 97 }, n.getLabel(1)));
            Assert.assertEquals("DnsJava", n.getLabelString(1));
        }
        
        public void test_ctor_7label() throws TextParseException {
            final Name n = new Name("a.b.c.d.e.f.");
            Assert.assertTrue(n.isAbsolute());
            Assert.assertFalse(n.isWild());
            Assert.assertEquals(7, n.labels());
            Assert.assertEquals(13, n.length());
            Assert.assertTrue(Arrays.equals(new byte[] { 1, 97 }, n.getLabel(0)));
            Assert.assertEquals("a", n.getLabelString(0));
            Assert.assertTrue(Arrays.equals(new byte[] { 1, 98 }, n.getLabel(1)));
            Assert.assertEquals("b", n.getLabelString(1));
            Assert.assertTrue(Arrays.equals(new byte[] { 1, 99 }, n.getLabel(2)));
            Assert.assertEquals("c", n.getLabelString(2));
            Assert.assertTrue(Arrays.equals(new byte[] { 1, 100 }, n.getLabel(3)));
            Assert.assertEquals("d", n.getLabelString(3));
            Assert.assertTrue(Arrays.equals(new byte[] { 1, 101 }, n.getLabel(4)));
            Assert.assertEquals("e", n.getLabelString(4));
            Assert.assertTrue(Arrays.equals(new byte[] { 1, 102 }, n.getLabel(5)));
            Assert.assertEquals("f", n.getLabelString(5));
            Assert.assertTrue(Arrays.equals(new byte[] { 0 }, n.getLabel(6)));
            Assert.assertEquals("", n.getLabelString(6));
        }
        
        public void test_ctor_8label() throws TextParseException {
            final Name n = new Name("a.b.c.d.e.f.g.");
            Assert.assertTrue(n.isAbsolute());
            Assert.assertFalse(n.isWild());
            Assert.assertEquals(8, n.labels());
            Assert.assertEquals(15, n.length());
            Assert.assertTrue(Arrays.equals(new byte[] { 1, 97 }, n.getLabel(0)));
            Assert.assertEquals("a", n.getLabelString(0));
            Assert.assertTrue(Arrays.equals(new byte[] { 1, 98 }, n.getLabel(1)));
            Assert.assertEquals("b", n.getLabelString(1));
            Assert.assertTrue(Arrays.equals(new byte[] { 1, 99 }, n.getLabel(2)));
            Assert.assertEquals("c", n.getLabelString(2));
            Assert.assertTrue(Arrays.equals(new byte[] { 1, 100 }, n.getLabel(3)));
            Assert.assertEquals("d", n.getLabelString(3));
            Assert.assertTrue(Arrays.equals(new byte[] { 1, 101 }, n.getLabel(4)));
            Assert.assertEquals("e", n.getLabelString(4));
            Assert.assertTrue(Arrays.equals(new byte[] { 1, 102 }, n.getLabel(5)));
            Assert.assertEquals("f", n.getLabelString(5));
            Assert.assertTrue(Arrays.equals(new byte[] { 1, 103 }, n.getLabel(6)));
            Assert.assertEquals("g", n.getLabelString(6));
            Assert.assertTrue(Arrays.equals(new byte[] { 0 }, n.getLabel(7)));
            Assert.assertEquals("", n.getLabelString(7));
        }
        
        public void test_ctor_abs_abs_origin() throws TextParseException {
            final Name n = new Name("WWW.DnsJava.org.", this.m_abs_origin);
            Assert.assertTrue(n.isAbsolute());
            Assert.assertFalse(n.isWild());
            Assert.assertEquals(4, n.labels());
            Assert.assertEquals(17, n.length());
            Assert.assertTrue(Arrays.equals(new byte[] { 3, 87, 87, 87 }, n.getLabel(0)));
            Assert.assertEquals("WWW", n.getLabelString(0));
            Assert.assertTrue(Arrays.equals(new byte[] { 7, 68, 110, 115, 74, 97, 118, 97 }, n.getLabel(1)));
            Assert.assertEquals("DnsJava", n.getLabelString(1));
            Assert.assertTrue(Arrays.equals(new byte[] { 3, 111, 114, 103 }, n.getLabel(2)));
            Assert.assertEquals("org", n.getLabelString(2));
            Assert.assertTrue(Arrays.equals(new byte[] { 0 }, n.getLabel(3)));
            Assert.assertEquals("", n.getLabelString(3));
        }
        
        public void test_ctor_abs_rel_origin() throws TextParseException {
            final Name n = new Name("WWW.DnsJava.org.", this.m_rel_origin);
            Assert.assertTrue(n.isAbsolute());
            Assert.assertFalse(n.isWild());
            Assert.assertEquals(4, n.labels());
            Assert.assertEquals(17, n.length());
            Assert.assertTrue(Arrays.equals(new byte[] { 3, 87, 87, 87 }, n.getLabel(0)));
            Assert.assertEquals("WWW", n.getLabelString(0));
            Assert.assertTrue(Arrays.equals(new byte[] { 7, 68, 110, 115, 74, 97, 118, 97 }, n.getLabel(1)));
            Assert.assertEquals("DnsJava", n.getLabelString(1));
            Assert.assertTrue(Arrays.equals(new byte[] { 3, 111, 114, 103 }, n.getLabel(2)));
            Assert.assertEquals("org", n.getLabelString(2));
            Assert.assertTrue(Arrays.equals(new byte[] { 0 }, n.getLabel(3)));
            Assert.assertEquals("", n.getLabelString(3));
        }
        
        public void test_ctor_rel_abs_origin() throws TextParseException {
            final Name n = new Name("WWW.DnsJava", this.m_abs_origin);
            Assert.assertTrue(n.isAbsolute());
            Assert.assertFalse(n.isWild());
            Assert.assertEquals(4, n.labels());
            Assert.assertEquals(18, n.length());
            Assert.assertTrue(Arrays.equals(new byte[] { 3, 87, 87, 87 }, n.getLabel(0)));
            Assert.assertEquals("WWW", n.getLabelString(0));
            Assert.assertTrue(Arrays.equals(new byte[] { 7, 68, 110, 115, 74, 97, 118, 97 }, n.getLabel(1)));
            Assert.assertEquals("DnsJava", n.getLabelString(1));
            Assert.assertTrue(Arrays.equals(new byte[] { 4, 79, 114, 105, 103 }, n.getLabel(2)));
            Assert.assertEquals("Orig", n.getLabelString(2));
            Assert.assertTrue(Arrays.equals(new byte[] { 0 }, n.getLabel(3)));
            Assert.assertEquals("", n.getLabelString(3));
        }
        
        public void test_ctor_invalid_label() {
            try {
                new Name("junk..junk.");
                Assert.fail("TextParseException not thrown");
            }
            catch (TextParseException ex) {}
        }
        
        public void test_ctor_max_label() throws TextParseException {
            final Name n = new Name("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa.b.");
            Assert.assertTrue(n.isAbsolute());
            Assert.assertFalse(n.isWild());
            Assert.assertEquals(3, n.labels());
            Assert.assertEquals(67, n.length());
            Assert.assertTrue(Arrays.equals(new byte[] { 63, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97 }, n.getLabel(0)));
            Assert.assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", n.getLabelString(0));
            Assert.assertTrue(Arrays.equals(new byte[] { 1, 98 }, n.getLabel(1)));
            Assert.assertEquals("b", n.getLabelString(1));
            Assert.assertTrue(Arrays.equals(new byte[] { 0 }, n.getLabel(2)));
            Assert.assertEquals("", n.getLabelString(2));
        }
        
        public void test_ctor_toobig_label() {
            try {
                new Name("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa.b.");
                Assert.fail("TextParseException not thrown");
            }
            catch (TextParseException ex) {}
        }
        
        public void test_ctor_max_length_rel() throws TextParseException {
            final Name n = new Name("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa.bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb.ccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc.dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd");
            Assert.assertFalse(n.isAbsolute());
            Assert.assertFalse(n.isWild());
            Assert.assertEquals(4, n.labels());
            Assert.assertEquals(255, n.length());
        }
        
        public void test_ctor_max_length_abs() throws TextParseException {
            final Name n = new Name("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa.bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb.ccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc.ddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd.");
            Assert.assertTrue(n.isAbsolute());
            Assert.assertFalse(n.isWild());
            Assert.assertEquals(5, n.labels());
            Assert.assertEquals(255, n.length());
        }
        
        public void test_ctor_escaped() throws TextParseException {
            final Name n = new Name("ab\\123cd");
            Assert.assertFalse(n.isAbsolute());
            Assert.assertFalse(n.isWild());
            Assert.assertEquals(1, n.labels());
            Assert.assertEquals(6, n.length());
            Assert.assertTrue(Arrays.equals(new byte[] { 5, 97, 98, 123, 99, 100 }, n.getLabel(0)));
        }
        
        public void test_ctor_escaped_end() throws TextParseException {
            final Name n = new Name("abcd\\123");
            Assert.assertFalse(n.isAbsolute());
            Assert.assertFalse(n.isWild());
            Assert.assertEquals(1, n.labels());
            Assert.assertEquals(6, n.length());
            Assert.assertTrue(Arrays.equals(new byte[] { 5, 97, 98, 99, 100, 123 }, n.getLabel(0)));
        }
        
        public void test_ctor_short_escaped() throws TextParseException {
            try {
                new Name("ab\\12cd");
                Assert.fail("TextParseException not throw");
            }
            catch (TextParseException ex) {}
        }
        
        public void test_ctor_short_escaped_end() throws TextParseException {
            try {
                new Name("ab\\12");
                Assert.fail("TextParseException not throw");
            }
            catch (TextParseException ex) {}
        }
        
        public void test_ctor_empty_escaped_end() throws TextParseException {
            try {
                new Name("ab\\");
                Assert.fail("TextParseException not throw");
            }
            catch (TextParseException ex) {}
        }
        
        public void test_ctor_toobig_escaped() throws TextParseException {
            try {
                new Name("ab\\256cd");
                Assert.fail("TextParseException not throw");
            }
            catch (TextParseException ex) {}
        }
        
        public void test_ctor_toobig_escaped_end() throws TextParseException {
            try {
                new Name("ab\\256");
                Assert.fail("TextParseException not throw");
            }
            catch (TextParseException ex) {}
        }
        
        public void test_ctor_max_label_escaped() throws TextParseException {
            final Name n = new Name("aaaa\\100aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa.b.");
            Assert.assertTrue(n.isAbsolute());
            Assert.assertFalse(n.isWild());
            Assert.assertEquals(3, n.labels());
            Assert.assertEquals(67, n.length());
            Assert.assertTrue(Arrays.equals(new byte[] { 63, 97, 97, 97, 97, 100, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97 }, n.getLabel(0)));
            Assert.assertTrue(Arrays.equals(new byte[] { 1, 98 }, n.getLabel(1)));
            Assert.assertEquals("b", n.getLabelString(1));
            Assert.assertTrue(Arrays.equals(new byte[] { 0 }, n.getLabel(2)));
            Assert.assertEquals("", n.getLabelString(2));
        }
        
        public void test_ctor_max_labels() throws TextParseException {
            final StringBuffer sb = new StringBuffer();
            for (int i = 0; i < 127; ++i) {
                sb.append("a.");
            }
            final Name n = new Name(sb.toString());
            Assert.assertTrue(n.isAbsolute());
            Assert.assertFalse(n.isWild());
            Assert.assertEquals(128, n.labels());
            Assert.assertEquals(255, n.length());
            for (int j = 0; j < 127; ++j) {
                Assert.assertTrue(Arrays.equals(new byte[] { 1, 97 }, n.getLabel(j)));
                Assert.assertEquals("a", n.getLabelString(j));
            }
            Assert.assertTrue(Arrays.equals(new byte[] { 0 }, n.getLabel(127)));
            Assert.assertEquals("", n.getLabelString(127));
        }
        
        public void test_ctor_toobig_label_escaped_end() throws TextParseException {
            try {
                new Name("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\\090.b.");
                Assert.fail("TextParseException not thrown");
            }
            catch (TextParseException ex) {}
        }
        
        public void test_ctor_toobig_label_escaped() throws TextParseException {
            try {
                new Name("aaaaaaaaaaaaaaaaaaaaaaaaaaaa\\001aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa.b.");
                Assert.fail("TextParseException not thrown");
            }
            catch (TextParseException ex) {}
        }
        
        public void test_fromString() throws TextParseException {
            final Name n = new Name("WWW.DnsJava", this.m_abs_origin);
            final Name n2 = Name.fromString("WWW.DnsJava", this.m_abs_origin);
            Assert.assertEquals(n, n2);
        }
        
        public void test_fromString_at() throws TextParseException {
            final Name n = Name.fromString("@", this.m_rel_origin);
            Assert.assertSame(this.m_rel_origin, n);
        }
        
        public void test_fromString_dot() throws TextParseException {
            final Name n = Name.fromString(".");
            Assert.assertSame(Name.root, n);
        }
        
        public void test_fromConstantString() throws TextParseException {
            final Name n = new Name("WWW.DnsJava.org.");
            final Name n2 = Name.fromConstantString("WWW.DnsJava.org.");
            Assert.assertEquals(n, n2);
        }
        
        public void test_fromConstantString_invalid() {
            try {
                Name.fromConstantString("junk..junk");
                Assert.fail("IllegalArgumentException not thrown");
            }
            catch (IllegalArgumentException ex) {}
        }
    }
    
    public static class Test_DNSInput_init extends TestCase
    {
        public void test_basic() throws IOException, TextParseException, WireParseException {
            final byte[] raw = { 3, 87, 119, 119, 7, 68, 110, 115, 74, 97, 118, 97, 3, 111, 114, 103, 0 };
            final Name e = Name.fromString("Www.DnsJava.org.");
            final Name n = new Name(raw);
            Assert.assertEquals(e, n);
        }
        
        public void test_incomplete() throws IOException {
            try {
                final Name name = new Name(new byte[] { 3, 87, 119, 119 });
                Assert.fail("WireParseException not thrown");
            }
            catch (WireParseException ex) {}
        }
        
        public void test_root() throws WireParseException {
            final byte[] raw = { 0 };
            final Name n = new Name(new DNSInput(raw));
            Assert.assertEquals(Name.root, n);
        }
        
        public void test_invalid_length() throws IOException {
            try {
                final Name name = new Name(new byte[] { 4, 87, 119, 119 });
                Assert.fail("WireParseException not thrown");
            }
            catch (WireParseException ex) {}
        }
        
        public void test_max_label_length() throws TextParseException, WireParseException {
            final byte[] raw = { 63, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 0 };
            final Name e = Name.fromString("bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb.");
            final Name n = new Name(new DNSInput(raw));
            Assert.assertEquals(e, n);
        }
        
        public void test_max_name() throws TextParseException, WireParseException {
            final Name e = new Name("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa.bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb.ccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc.ddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd.");
            final byte[] raw = { 63, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 63, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 63, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 61, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 0 };
            final Name n = new Name(new DNSInput(raw));
            Assert.assertEquals(e, n);
        }
        
        public void test_toolong_name() throws TextParseException, WireParseException {
            final byte[] raw = { 63, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 63, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 98, 63, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 62, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 0 };
            try {
                final Name name = new Name(new DNSInput(raw));
                Assert.fail("WireParseException not thrown");
            }
            catch (WireParseException ex) {}
        }
        
        public void test_max_labels() throws TextParseException, WireParseException {
            final byte[] raw = { 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 0 };
            final Name e = Name.fromString("a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.a.");
            final Name n = new Name(new DNSInput(raw));
            Assert.assertEquals(128, n.labels());
            Assert.assertEquals(e, n);
        }
        
        public void test_toomany_labels() throws TextParseException, WireParseException {
            final byte[] raw = { 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 1, 97, 0 };
            try {
                final Name name = new Name(new DNSInput(raw));
                Assert.fail("WireParseException not thrown");
            }
            catch (WireParseException ex) {}
        }
        
        public void test_basic_compression() throws TextParseException, WireParseException {
            final byte[] raw = { 10, 3, 97, 98, 99, 0, -64, 1 };
            final Name e = Name.fromString("abc.");
            final DNSInput in = new DNSInput(raw);
            in.jump(6);
            Options.set("verbosecompression");
            final Name n = new Name(in);
            Options.unset("verbosecompression");
            Assert.assertEquals(e, n);
        }
        
        public void test_two_pointer_compression() throws TextParseException, WireParseException {
            final byte[] raw = { 10, 3, 97, 98, 99, 0, -64, 1, -64, 6 };
            final Name e = Name.fromString("abc.");
            final DNSInput in = new DNSInput(raw);
            in.jump(8);
            final Name n = new Name(in);
            Assert.assertEquals(e, n);
        }
        
        public void test_two_part_compression() throws TextParseException, WireParseException {
            final byte[] raw = { 10, 3, 97, 98, 99, 0, 1, 66, -64, 1 };
            final Name e = Name.fromString("B.abc.");
            final DNSInput in = new DNSInput(raw);
            in.jump(6);
            final Name n = new Name(in);
            Assert.assertEquals(e, n);
        }
        
        public void test_long_jump_compression() throws TextParseException, WireParseException {
            final byte[] raw = { 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 3, 97, 98, 99, 0, -63, 0 };
            final Name e = Name.fromString("abc.");
            final DNSInput in = new DNSInput(raw);
            in.jump(261);
            final Name n = new Name(in);
            Assert.assertEquals(e, n);
        }
        
        public void test_bad_compression() throws TextParseException, WireParseException {
            final byte[] raw = { -64, 2, 0 };
            try {
                final Name name = new Name(new DNSInput(raw));
                Assert.fail("WireParseException not thrown");
            }
            catch (WireParseException ex) {}
        }
        
        public void test_basic_compression_state_restore() throws TextParseException, WireParseException {
            final byte[] raw = { 10, 3, 97, 98, 99, 0, -64, 1, 3, 100, 101, 102, 0 };
            final Name e = Name.fromString("abc.");
            final Name e2 = Name.fromString("def.");
            final DNSInput in = new DNSInput(raw);
            in.jump(6);
            Name n = new Name(in);
            Assert.assertEquals(e, n);
            n = new Name(in);
            Assert.assertEquals(e2, n);
        }
        
        public void test_two_part_compression_state_restore() throws TextParseException, WireParseException {
            final byte[] raw = { 10, 3, 97, 98, 99, 0, 1, 66, -64, 1, 3, 100, 101, 102, 0 };
            final Name e = Name.fromString("B.abc.");
            final Name e2 = Name.fromString("def.");
            final DNSInput in = new DNSInput(raw);
            in.jump(6);
            Name n = new Name(in);
            Assert.assertEquals(e, n);
            n = new Name(in);
            Assert.assertEquals(e2, n);
        }
    }
    
    public static class Test_toWire extends TestCase
    {
        public void test_rel() throws TextParseException {
            final Name n = new Name("A.Relative.Name");
            try {
                n.toWire(new DNSOutput(), null);
                Assert.fail("IllegalArgumentException not thrown");
            }
            catch (IllegalArgumentException ex) {}
        }
        
        public void test_null_Compression() throws TextParseException {
            final byte[] raw = { 1, 65, 5, 66, 97, 115, 105, 99, 4, 78, 97, 109, 101, 0 };
            final Name n = new Name("A.Basic.Name.");
            final DNSOutput o = new DNSOutput();
            n.toWire(o, null);
            Assert.assertTrue(Arrays.equals(raw, o.toByteArray()));
        }
        
        public void test_empty_Compression() throws TextParseException {
            final byte[] raw = { 1, 65, 5, 66, 97, 115, 105, 99, 4, 78, 97, 109, 101, 0 };
            final Name n = new Name("A.Basic.Name.");
            final Compression c = new Compression();
            final DNSOutput o = new DNSOutput();
            n.toWire(o, c);
            Assert.assertTrue(Arrays.equals(raw, o.toByteArray()));
            Assert.assertEquals(0, c.get(n));
        }
        
        public void test_with_exact_Compression() throws TextParseException {
            final Name n = new Name("A.Basic.Name.");
            final Compression c = new Compression();
            c.add(256, n);
            final byte[] exp = { -63, 0 };
            final DNSOutput o = new DNSOutput();
            n.toWire(o, c);
            Assert.assertTrue(Arrays.equals(exp, o.toByteArray()));
            Assert.assertEquals(256, c.get(n));
        }
        
        public void test_with_partial_Compression() throws TextParseException {
            final Name d = new Name("Basic.Name.");
            final Name n = new Name("A.Basic.Name.");
            final Compression c = new Compression();
            c.add(257, d);
            final byte[] exp = { 1, 65, -63, 1 };
            final DNSOutput o = new DNSOutput();
            n.toWire(o, c);
            Assert.assertTrue(Arrays.equals(exp, o.toByteArray()));
            Assert.assertEquals(257, c.get(d));
            Assert.assertEquals(0, c.get(n));
        }
        
        public void test_0arg_rel() throws TextParseException {
            final Name n = new Name("A.Relative.Name");
            try {
                n.toWire();
                Assert.fail("IllegalArgumentException not thrown");
            }
            catch (IllegalArgumentException ex) {}
        }
        
        public void test_0arg() throws TextParseException {
            final byte[] raw = { 1, 65, 5, 66, 97, 115, 105, 99, 4, 78, 97, 109, 101, 0 };
            final Name n = new Name("A.Basic.Name.");
            final byte[] out = n.toWire();
            Assert.assertTrue(Arrays.equals(raw, out));
        }
        
        public void test_root() {
            final byte[] out = Name.root.toWire();
            Assert.assertTrue(Arrays.equals(new byte[] { 0 }, out));
        }
        
        public void test_3arg() throws TextParseException {
            final Name d = new Name("Basic.Name.");
            final Name n = new Name("A.Basic.Name.");
            final Compression c = new Compression();
            c.add(257, d);
            final byte[] exp = { 1, 65, -63, 1 };
            final DNSOutput o = new DNSOutput();
            n.toWire(o, c, false);
            Assert.assertTrue(Arrays.equals(exp, o.toByteArray()));
            Assert.assertEquals(257, c.get(d));
            Assert.assertEquals(0, c.get(n));
        }
    }
    
    public static class Test_toWireCanonical extends TestCase
    {
        public void test_basic() throws TextParseException {
            final byte[] raw = { 1, 97, 5, 98, 97, 115, 105, 99, 4, 110, 97, 109, 101, 0 };
            final Name n = new Name("A.Basic.Name.");
            final DNSOutput o = new DNSOutput();
            n.toWireCanonical(o);
            Assert.assertTrue(Arrays.equals(raw, o.toByteArray()));
        }
        
        public void test_0arg() throws TextParseException {
            final byte[] raw = { 1, 97, 5, 98, 97, 115, 105, 99, 4, 110, 97, 109, 101, 0 };
            final Name n = new Name("A.Basic.Name.");
            final byte[] out = n.toWireCanonical();
            Assert.assertTrue(Arrays.equals(raw, out));
        }
        
        public void test_root() {
            final byte[] out = Name.root.toWireCanonical();
            Assert.assertTrue(Arrays.equals(new byte[] { 0 }, out));
        }
        
        public void test_empty() throws TextParseException {
            final Name n = new Name("@", null);
            final byte[] out = n.toWireCanonical();
            Assert.assertTrue(Arrays.equals(new byte[0], out));
        }
        
        public void test_3arg() throws TextParseException {
            final Name d = new Name("Basic.Name.");
            final Name n = new Name("A.Basic.Name.");
            final Compression c = new Compression();
            c.add(257, d);
            final byte[] exp = { 1, 97, 5, 98, 97, 115, 105, 99, 4, 110, 97, 109, 101, 0 };
            final DNSOutput o = new DNSOutput();
            n.toWire(o, c, true);
            Assert.assertTrue(Arrays.equals(exp, o.toByteArray()));
            Assert.assertEquals(257, c.get(d));
            Assert.assertEquals(-1, c.get(n));
        }
    }
    
    public static class Test_equals extends TestCase
    {
        public void test_same() throws TextParseException {
            final Name n = new Name("A.Name.");
            Assert.assertTrue(n.equals(n));
        }
        
        public void test_null() throws TextParseException {
            final Name n = new Name("A.Name.");
            Assert.assertFalse(n.equals(null));
        }
        
        public void test_notName() throws TextParseException {
            final Name n = new Name("A.Name.");
            Assert.assertFalse(n.equals(new Object()));
        }
        
        public void test_abs() throws TextParseException {
            final Name n = new Name("A.Name.");
            final Name n2 = new Name("a.name.");
            Assert.assertTrue(n.equals(n2));
            Assert.assertTrue(n2.equals(n));
        }
        
        public void test_rel() throws TextParseException {
            final Name n1 = new Name("A.Relative.Name");
            final Name n2 = new Name("a.relative.name");
            Assert.assertTrue(n1.equals(n2));
            Assert.assertTrue(n2.equals(n1));
        }
        
        public void test_mixed() throws TextParseException {
            final Name n1 = new Name("A.Name");
            final Name n2 = new Name("a.name.");
            Assert.assertFalse(n1.equals(n2));
            Assert.assertFalse(n2.equals(n1));
        }
        
        public void test_weird() throws TextParseException {
            final Name n1 = new Name("ab.c");
            final Name n2 = new Name("abc.");
            Assert.assertFalse(n1.equals(n2));
            Assert.assertFalse(n2.equals(n1));
        }
    }
    
    public static class Test_compareTo extends TestCase
    {
        public void test_notName() throws TextParseException {
            final Name n = new Name("A.Name");
            try {
                n.compareTo(new Object());
                Assert.fail("ClassCastException not thrown");
            }
            catch (ClassCastException ex) {}
        }
        
        public void test_same() throws TextParseException {
            final Name n = new Name("A.Name");
            Assert.assertEquals(0, n.compareTo(n));
        }
        
        public void test_equal() throws TextParseException {
            final Name n1 = new Name("A.Name.");
            final Name n2 = new Name("a.name.");
            Assert.assertEquals(0, n1.compareTo(n2));
            Assert.assertEquals(0, n2.compareTo(n1));
        }
        
        public void test_close() throws TextParseException {
            final Name n1 = new Name("a.name");
            final Name n2 = new Name("a.name.");
            Assert.assertTrue(n1.compareTo(n2) > 0);
            Assert.assertTrue(n2.compareTo(n1) < 0);
        }
        
        public void test_disjoint() throws TextParseException {
            final Name n1 = new Name("b");
            final Name n2 = new Name("c");
            Assert.assertTrue(n1.compareTo(n2) < 0);
            Assert.assertTrue(n2.compareTo(n1) > 0);
        }
        
        public void test_label_prefix() throws TextParseException {
            final Name n1 = new Name("thisIs.a.");
            final Name n2 = new Name("thisIsGreater.a.");
            Assert.assertTrue(n1.compareTo(n2) < 0);
            Assert.assertTrue(n2.compareTo(n1) > 0);
        }
        
        public void test_more_labels() throws TextParseException {
            final Name n1 = new Name("c.b.a.");
            final Name n2 = new Name("d.c.b.a.");
            Assert.assertTrue(n1.compareTo(n2) < 0);
            Assert.assertTrue(n2.compareTo(n1) > 0);
        }
    }
}
