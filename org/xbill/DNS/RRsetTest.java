// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.util.Iterator;
import junit.framework.Assert;
import java.net.UnknownHostException;
import java.util.Date;
import java.net.InetAddress;
import junit.framework.TestCase;

public class RRsetTest extends TestCase
{
    private RRset m_rs;
    Name m_name;
    Name m_name2;
    long m_ttl;
    ARecord m_a1;
    ARecord m_a2;
    RRSIGRecord m_s1;
    RRSIGRecord m_s2;
    
    public void setUp() throws TextParseException, UnknownHostException {
        this.m_rs = new RRset();
        this.m_name = Name.fromString("this.is.a.test.");
        this.m_name2 = Name.fromString("this.is.another.test.");
        this.m_ttl = 43981L;
        this.m_a1 = new ARecord(this.m_name, 1, this.m_ttl, InetAddress.getByName("192.169.232.11"));
        this.m_a2 = new ARecord(this.m_name, 1, this.m_ttl + 1L, InetAddress.getByName("192.169.232.12"));
        this.m_s1 = new RRSIGRecord(this.m_name, 1, this.m_ttl, 1, 15, 703710L, new Date(), new Date(), 10, this.m_name, new byte[0]);
        this.m_s2 = new RRSIGRecord(this.m_name, 1, this.m_ttl, 1, 15, 703710L, new Date(), new Date(), 10, this.m_name2, new byte[0]);
    }
    
    public void test_ctor_0arg() {
        Assert.assertEquals(0, this.m_rs.size());
        try {
            this.m_rs.getDClass();
            Assert.fail("IllegalStateException not thrown");
        }
        catch (IllegalStateException ex) {}
        try {
            this.m_rs.getType();
            Assert.fail("IllegalStateException not thrown");
        }
        catch (IllegalStateException ex2) {}
        try {
            this.m_rs.getTTL();
            Assert.fail("IllegalStateException not thrown");
        }
        catch (IllegalStateException ex3) {}
        try {
            this.m_rs.getName();
            Assert.fail("IllegalStateException not thrown");
        }
        catch (IllegalStateException ex4) {}
        try {
            this.m_rs.first();
            Assert.fail("IllegalStateException not thrown");
        }
        catch (IllegalStateException ex5) {}
        try {
            this.m_rs.toString();
            Assert.fail("IllegalStateException not thrown");
        }
        catch (IllegalStateException ex6) {}
        Iterator itr = this.m_rs.rrs();
        Assert.assertNotNull(itr);
        Assert.assertFalse(itr.hasNext());
        itr = this.m_rs.sigs();
        Assert.assertNotNull(itr);
        Assert.assertFalse(itr.hasNext());
    }
    
    public void test_basics() throws TextParseException, UnknownHostException {
        this.m_rs.addRR(this.m_a1);
        Assert.assertEquals(1, this.m_rs.size());
        Assert.assertEquals(1, this.m_rs.getDClass());
        Assert.assertEquals(this.m_a1, this.m_rs.first());
        Assert.assertEquals(this.m_name, this.m_rs.getName());
        Assert.assertEquals(this.m_ttl, this.m_rs.getTTL());
        Assert.assertEquals(1, this.m_rs.getType());
        this.m_rs.addRR(this.m_a1);
        Assert.assertEquals(1, this.m_rs.size());
        Assert.assertEquals(1, this.m_rs.getDClass());
        Assert.assertEquals(this.m_a1, this.m_rs.first());
        Assert.assertEquals(this.m_name, this.m_rs.getName());
        Assert.assertEquals(this.m_ttl, this.m_rs.getTTL());
        Assert.assertEquals(1, this.m_rs.getType());
        this.m_rs.addRR(this.m_a2);
        Assert.assertEquals(2, this.m_rs.size());
        Assert.assertEquals(1, this.m_rs.getDClass());
        final Record r = this.m_rs.first();
        Assert.assertEquals(this.m_a1, r);
        Assert.assertEquals(this.m_name, this.m_rs.getName());
        Assert.assertEquals(this.m_ttl, this.m_rs.getTTL());
        Assert.assertEquals(1, this.m_rs.getType());
        Iterator itr = this.m_rs.rrs();
        Assert.assertEquals(this.m_a1, itr.next());
        Assert.assertEquals(this.m_a2, itr.next());
        itr = this.m_rs.rrs();
        Assert.assertEquals(this.m_a2, itr.next());
        Assert.assertEquals(this.m_a1, itr.next());
        itr = this.m_rs.rrs();
        Assert.assertEquals(this.m_a1, itr.next());
        Assert.assertEquals(this.m_a2, itr.next());
        this.m_rs.deleteRR(this.m_a1);
        Assert.assertEquals(1, this.m_rs.size());
        Assert.assertEquals(1, this.m_rs.getDClass());
        Assert.assertEquals(this.m_a2, this.m_rs.first());
        Assert.assertEquals(this.m_name, this.m_rs.getName());
        Assert.assertEquals(this.m_ttl, this.m_rs.getTTL());
        Assert.assertEquals(1, this.m_rs.getType());
        this.m_rs.addRR(this.m_s1);
        Assert.assertEquals(1, this.m_rs.size());
        itr = this.m_rs.sigs();
        Assert.assertEquals(this.m_s1, itr.next());
        Assert.assertFalse(itr.hasNext());
        this.m_rs.addRR(this.m_s1);
        itr = this.m_rs.sigs();
        Assert.assertEquals(this.m_s1, itr.next());
        Assert.assertFalse(itr.hasNext());
        this.m_rs.addRR(this.m_s2);
        itr = this.m_rs.sigs();
        Assert.assertEquals(this.m_s1, itr.next());
        Assert.assertEquals(this.m_s2, itr.next());
        Assert.assertFalse(itr.hasNext());
        this.m_rs.deleteRR(this.m_s1);
        itr = this.m_rs.sigs();
        Assert.assertEquals(this.m_s2, itr.next());
        Assert.assertFalse(itr.hasNext());
        this.m_rs.clear();
        Assert.assertEquals(0, this.m_rs.size());
        Assert.assertFalse(this.m_rs.rrs().hasNext());
        Assert.assertFalse(this.m_rs.sigs().hasNext());
    }
    
    public void test_ctor_1arg() {
        this.m_rs.addRR(this.m_a1);
        this.m_rs.addRR(this.m_a2);
        this.m_rs.addRR(this.m_s1);
        this.m_rs.addRR(this.m_s2);
        final RRset rs2 = new RRset(this.m_rs);
        Assert.assertEquals(2, rs2.size());
        Assert.assertEquals(this.m_a1, rs2.first());
        Iterator itr = rs2.rrs();
        Assert.assertEquals(this.m_a1, itr.next());
        Assert.assertEquals(this.m_a2, itr.next());
        Assert.assertFalse(itr.hasNext());
        itr = rs2.sigs();
        Assert.assertTrue(itr.hasNext());
        Assert.assertEquals(this.m_s1, itr.next());
        Assert.assertTrue(itr.hasNext());
        Assert.assertEquals(this.m_s2, itr.next());
        Assert.assertFalse(itr.hasNext());
    }
    
    public void test_toString() {
        this.m_rs.addRR(this.m_a1);
        this.m_rs.addRR(this.m_a2);
        this.m_rs.addRR(this.m_s1);
        this.m_rs.addRR(this.m_s2);
        final String out = this.m_rs.toString();
        Assert.assertTrue(out.indexOf(this.m_name.toString()) != -1);
        Assert.assertTrue(out.indexOf(" IN A ") != -1);
        Assert.assertTrue(out.indexOf("[192.169.232.11]") != -1);
        Assert.assertTrue(out.indexOf("[192.169.232.12]") != -1);
    }
    
    public void test_addRR_invalidType() throws TextParseException {
        this.m_rs.addRR(this.m_a1);
        final CNAMERecord c = new CNAMERecord(this.m_name, 1, this.m_ttl, Name.fromString("an.alias."));
        try {
            this.m_rs.addRR(c);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex) {}
    }
    
    public void test_addRR_invalidName() throws TextParseException, UnknownHostException {
        this.m_rs.addRR(this.m_a1);
        this.m_a2 = new ARecord(this.m_name2, 1, this.m_ttl, InetAddress.getByName("192.169.232.11"));
        try {
            this.m_rs.addRR(this.m_a2);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex) {}
    }
    
    public void test_addRR_invalidDClass() throws TextParseException, UnknownHostException {
        this.m_rs.addRR(this.m_a1);
        this.m_a2 = new ARecord(this.m_name, 3, this.m_ttl, InetAddress.getByName("192.169.232.11"));
        try {
            this.m_rs.addRR(this.m_a2);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex) {}
    }
    
    public void test_TTLcalculation() {
        this.m_rs.addRR(this.m_a2);
        Assert.assertEquals(this.m_a2.getTTL(), this.m_rs.getTTL());
        this.m_rs.addRR(this.m_a1);
        Assert.assertEquals(this.m_a1.getTTL(), this.m_rs.getTTL());
        final Iterator itr = this.m_rs.rrs();
        while (itr.hasNext()) {
            final Record r = itr.next();
            Assert.assertEquals(this.m_a1.getTTL(), r.getTTL());
        }
    }
    
    public void test_Record_placement() {
        this.m_rs.addRR(this.m_a1);
        this.m_rs.addRR(this.m_s1);
        this.m_rs.addRR(this.m_a2);
        Iterator itr = this.m_rs.rrs();
        Assert.assertTrue(itr.hasNext());
        Assert.assertEquals(this.m_a1, itr.next());
        Assert.assertTrue(itr.hasNext());
        Assert.assertEquals(this.m_a2, itr.next());
        Assert.assertFalse(itr.hasNext());
        itr = this.m_rs.sigs();
        Assert.assertTrue(itr.hasNext());
        Assert.assertEquals(this.m_s1, itr.next());
        Assert.assertFalse(itr.hasNext());
    }
    
    public void test_noncycling_iterator() {
        this.m_rs.addRR(this.m_a1);
        this.m_rs.addRR(this.m_a2);
        Iterator itr = this.m_rs.rrs(false);
        Assert.assertTrue(itr.hasNext());
        Assert.assertEquals(this.m_a1, itr.next());
        Assert.assertTrue(itr.hasNext());
        Assert.assertEquals(this.m_a2, itr.next());
        itr = this.m_rs.rrs(false);
        Assert.assertTrue(itr.hasNext());
        Assert.assertEquals(this.m_a1, itr.next());
        Assert.assertTrue(itr.hasNext());
        Assert.assertEquals(this.m_a2, itr.next());
    }
}
