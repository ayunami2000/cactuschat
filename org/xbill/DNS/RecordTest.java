// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.util.Date;
import java.io.IOException;
import java.util.Arrays;
import java.net.UnknownHostException;
import java.net.InetAddress;
import junit.framework.Assert;
import junit.framework.TestCase;

public class RecordTest extends TestCase
{
    public void test_ctor_0arg() {
        final SubRecord sr = new SubRecord();
        Assert.assertNull(sr.getName());
        Assert.assertEquals(0, sr.getType());
        Assert.assertEquals(0L, sr.getTTL());
        Assert.assertEquals(0, sr.getDClass());
    }
    
    public void test_ctor_4arg() throws TextParseException {
        final Name n = Name.fromString("my.name.");
        final int t = 1;
        final int d = 1;
        final long ttl = 703710L;
        final SubRecord r = new SubRecord(n, t, d, ttl);
        Assert.assertEquals(n, r.getName());
        Assert.assertEquals(t, r.getType());
        Assert.assertEquals(d, r.getDClass());
        Assert.assertEquals(ttl, r.getTTL());
    }
    
    public void test_ctor_4arg_invalid() throws TextParseException {
        final Name n = Name.fromString("my.name.");
        final Name r = Name.fromString("my.relative.name");
        final int t = 1;
        final int d = 1;
        final long ttl = 703710L;
        try {
            new SubRecord(r, t, d, ttl);
            Assert.fail("RelativeNameException not thrown");
        }
        catch (RelativeNameException ex) {}
        try {
            new SubRecord(n, -1, d, ttl);
            Assert.fail("InvalidTypeException not thrown");
        }
        catch (InvalidTypeException ex2) {}
        try {
            new SubRecord(n, t, -1, ttl);
            Assert.fail("InvalidDClassException not thrown");
        }
        catch (InvalidDClassException ex3) {}
        try {
            new SubRecord(n, t, d, -1L);
            Assert.fail("InvalidTTLException not thrown");
        }
        catch (InvalidTTLException ex4) {}
    }
    
    public void test_newRecord_3arg() throws TextParseException {
        final Name n = Name.fromString("my.name.");
        final Name r = Name.fromString("my.relative.name");
        final int t = 1;
        final int d = 1;
        final Record rec = Record.newRecord(n, t, d);
        Assert.assertTrue(rec instanceof EmptyRecord);
        Assert.assertEquals(n, rec.getName());
        Assert.assertEquals(t, rec.getType());
        Assert.assertEquals(d, rec.getDClass());
        Assert.assertEquals(0L, rec.getTTL());
        try {
            Record.newRecord(r, t, d);
            Assert.fail("RelativeNameException not thrown");
        }
        catch (RelativeNameException ex) {}
    }
    
    public void test_newRecord_4arg() throws TextParseException {
        final Name n = Name.fromString("my.name.");
        final Name r = Name.fromString("my.relative.name");
        final int t = 1;
        final int d = 1;
        final int ttl = 56296;
        final Record rec = Record.newRecord(n, t, d, ttl);
        Assert.assertTrue(rec instanceof EmptyRecord);
        Assert.assertEquals(n, rec.getName());
        Assert.assertEquals(t, rec.getType());
        Assert.assertEquals(d, rec.getDClass());
        Assert.assertEquals(ttl, rec.getTTL());
        try {
            Record.newRecord(r, t, d, ttl);
            Assert.fail("RelativeNameException not thrown");
        }
        catch (RelativeNameException ex) {}
    }
    
    public void test_newRecord_5arg() throws TextParseException, UnknownHostException {
        final Name n = Name.fromString("my.name.");
        final int t = 1;
        final int d = 1;
        final int ttl = 56296;
        final byte[] data = { 123, -24, 0, -1 };
        final InetAddress exp = InetAddress.getByName("123.232.0.255");
        final Record rec = Record.newRecord(n, t, d, ttl, data);
        Assert.assertTrue(rec instanceof ARecord);
        Assert.assertEquals(n, rec.getName());
        Assert.assertEquals(t, rec.getType());
        Assert.assertEquals(d, rec.getDClass());
        Assert.assertEquals(ttl, rec.getTTL());
        Assert.assertEquals(exp, ((ARecord)rec).getAddress());
    }
    
    public void test_newRecord_6arg() throws TextParseException, UnknownHostException {
        final Name n = Name.fromString("my.name.");
        final int t = 1;
        final int d = 1;
        final int ttl = 56296;
        final byte[] data = { 123, -24, 0, -1 };
        final InetAddress exp = InetAddress.getByName("123.232.0.255");
        Record rec = Record.newRecord(n, t, d, ttl, 0, (byte[])null);
        Assert.assertTrue(rec instanceof EmptyRecord);
        Assert.assertEquals(n, rec.getName());
        Assert.assertEquals(t, rec.getType());
        Assert.assertEquals(d, rec.getDClass());
        Assert.assertEquals(ttl, rec.getTTL());
        rec = Record.newRecord(n, t, d, ttl, data.length, data);
        Assert.assertTrue(rec instanceof ARecord);
        Assert.assertEquals(n, rec.getName());
        Assert.assertEquals(t, rec.getType());
        Assert.assertEquals(d, rec.getDClass());
        Assert.assertEquals(ttl, rec.getTTL());
        Assert.assertEquals(exp, ((ARecord)rec).getAddress());
        rec = Record.newRecord(n, 32, d, ttl, data.length, data);
        Assert.assertTrue(rec instanceof UNKRecord);
        Assert.assertEquals(n, rec.getName());
        Assert.assertEquals(32, rec.getType());
        Assert.assertEquals(d, rec.getDClass());
        Assert.assertEquals(ttl, rec.getTTL());
        Assert.assertTrue(Arrays.equals(data, ((UNKRecord)rec).getData()));
    }
    
    public void test_newRecord_6arg_invalid() throws TextParseException {
        final Name n = Name.fromString("my.name.");
        final Name r = Name.fromString("my.relative.name");
        final int t = 1;
        final int d = 1;
        final int ttl = 56296;
        final byte[] data = { 123, -24, 0, -1 };
        Assert.assertNull(Record.newRecord(n, t, d, ttl, 0, new byte[0]));
        Assert.assertNull(Record.newRecord(n, t, d, ttl, 1, new byte[0]));
        Assert.assertNull(Record.newRecord(n, t, d, ttl, data.length + 1, data));
        Assert.assertNull(Record.newRecord(n, t, d, ttl, 5, new byte[] { data[0], data[1], data[2], data[3], 0 }));
        try {
            Record.newRecord(r, t, d, ttl, 0, (byte[])null);
            Assert.fail("RelativeNameException not thrown");
        }
        catch (RelativeNameException ex) {}
    }
    
    public void test_fromWire() throws IOException, TextParseException, UnknownHostException {
        final Name n = Name.fromString("my.name.");
        final int t = 1;
        final int d = 1;
        final int ttl = 56296;
        final byte[] data = { 123, -24, 0, -1 };
        final InetAddress exp = InetAddress.getByName("123.232.0.255");
        DNSOutput out = new DNSOutput();
        n.toWire(out, null);
        out.writeU16(t);
        out.writeU16(d);
        out.writeU32(ttl);
        out.writeU16(data.length);
        out.writeByteArray(data);
        DNSInput in = new DNSInput(out.toByteArray());
        Record rec = Record.fromWire(in, 1, false);
        Assert.assertTrue(rec instanceof ARecord);
        Assert.assertEquals(n, rec.getName());
        Assert.assertEquals(t, rec.getType());
        Assert.assertEquals(d, rec.getDClass());
        Assert.assertEquals(ttl, rec.getTTL());
        Assert.assertEquals(exp, ((ARecord)rec).getAddress());
        in = new DNSInput(out.toByteArray());
        rec = Record.fromWire(in, 0, false);
        Assert.assertTrue(rec instanceof EmptyRecord);
        Assert.assertEquals(n, rec.getName());
        Assert.assertEquals(t, rec.getType());
        Assert.assertEquals(d, rec.getDClass());
        Assert.assertEquals(0L, rec.getTTL());
        in = new DNSInput(out.toByteArray());
        rec = Record.fromWire(in, 0);
        Assert.assertTrue(rec instanceof EmptyRecord);
        Assert.assertEquals(n, rec.getName());
        Assert.assertEquals(t, rec.getType());
        Assert.assertEquals(d, rec.getDClass());
        Assert.assertEquals(0L, rec.getTTL());
        rec = Record.fromWire(out.toByteArray(), 0);
        Assert.assertTrue(rec instanceof EmptyRecord);
        Assert.assertEquals(n, rec.getName());
        Assert.assertEquals(t, rec.getType());
        Assert.assertEquals(d, rec.getDClass());
        Assert.assertEquals(0L, rec.getTTL());
        out = new DNSOutput();
        n.toWire(out, null);
        out.writeU16(t);
        out.writeU16(d);
        out.writeU32(ttl);
        out.writeU16(0);
        in = new DNSInput(out.toByteArray());
        rec = Record.fromWire(in, 1, true);
        Assert.assertTrue(rec instanceof EmptyRecord);
        Assert.assertEquals(n, rec.getName());
        Assert.assertEquals(t, rec.getType());
        Assert.assertEquals(d, rec.getDClass());
        Assert.assertEquals(ttl, rec.getTTL());
    }
    
    public void test_toWire() throws IOException, TextParseException, UnknownHostException {
        final Name n = Name.fromString("my.name.");
        final int t = 1;
        final int d = 1;
        final int ttl = 56296;
        final byte[] data = { 123, -24, 0, -1 };
        DNSOutput out = new DNSOutput();
        n.toWire(out, null);
        out.writeU16(t);
        out.writeU16(d);
        out.writeU32(ttl);
        out.writeU16(data.length);
        out.writeByteArray(data);
        byte[] exp = out.toByteArray();
        final Record rec = Record.newRecord(n, t, d, ttl, data.length, data);
        out = new DNSOutput();
        rec.toWire(out, 1, null);
        byte[] after = out.toByteArray();
        Assert.assertTrue(Arrays.equals(exp, after));
        after = rec.toWire(1);
        Assert.assertTrue(Arrays.equals(exp, after));
        out = new DNSOutput();
        n.toWire(out, null);
        out.writeU16(t);
        out.writeU16(d);
        exp = out.toByteArray();
        out = new DNSOutput();
        rec.toWire(out, 0, null);
        after = out.toByteArray();
        Assert.assertTrue(Arrays.equals(exp, after));
    }
    
    public void test_toWireCanonical() throws IOException, TextParseException, UnknownHostException {
        final Name n = Name.fromString("My.Name.");
        final int t = 1;
        final int d = 1;
        final int ttl = 56296;
        final byte[] data = { 123, -24, 0, -1 };
        final DNSOutput out = new DNSOutput();
        n.toWireCanonical(out);
        out.writeU16(t);
        out.writeU16(d);
        out.writeU32(ttl);
        out.writeU16(data.length);
        out.writeByteArray(data);
        final byte[] exp = out.toByteArray();
        final Record rec = Record.newRecord(n, t, d, ttl, data.length, data);
        final byte[] after = rec.toWireCanonical();
        Assert.assertTrue(Arrays.equals(exp, after));
    }
    
    public void test_rdataToWireCanonical() throws IOException, TextParseException, UnknownHostException {
        final Name n = Name.fromString("My.Name.");
        final Name n2 = Name.fromString("My.Second.Name.");
        final int t = 2;
        final int d = 1;
        final int ttl = 704153;
        DNSOutput out = new DNSOutput();
        n2.toWire(out, null);
        final byte[] data = out.toByteArray();
        out = new DNSOutput();
        n2.toWireCanonical(out);
        final byte[] exp = out.toByteArray();
        final Record rec = Record.newRecord(n, t, d, ttl, data.length, data);
        Assert.assertTrue(rec instanceof NSRecord);
        final byte[] after = rec.rdataToWireCanonical();
        Assert.assertTrue(Arrays.equals(exp, after));
    }
    
    public void test_rdataToString() throws IOException, TextParseException, UnknownHostException {
        final Name n = Name.fromString("My.Name.");
        final Name n2 = Name.fromString("My.Second.Name.");
        final int t = 2;
        final int d = 1;
        final int ttl = 704153;
        final DNSOutput out = new DNSOutput();
        n2.toWire(out, null);
        final byte[] data = out.toByteArray();
        final Record rec = Record.newRecord(n, t, d, ttl, data.length, data);
        Assert.assertTrue(rec instanceof NSRecord);
        Assert.assertEquals(rec.rrToString(), rec.rdataToString());
    }
    
    public void test_toString() throws TextParseException {
        final Name n = Name.fromString("My.N.");
        final Name n2 = Name.fromString("My.Second.Name.");
        final int t = 2;
        final int d = 1;
        final int ttl = 704153;
        final DNSOutput o = new DNSOutput();
        n2.toWire(o, null);
        final byte[] data = o.toByteArray();
        final Record rec = Record.newRecord(n, t, d, ttl, data.length, data);
        String out = rec.toString();
        Assert.assertFalse(out.indexOf(n.toString()) == -1);
        Assert.assertFalse(out.indexOf(n2.toString()) == -1);
        Assert.assertFalse(out.indexOf("NS") == -1);
        Assert.assertFalse(out.indexOf("IN") == -1);
        Assert.assertFalse(out.indexOf(ttl + "") == -1);
        Options.set("BINDTTL");
        out = rec.toString();
        Assert.assertFalse(out.indexOf(n.toString()) == -1);
        Assert.assertFalse(out.indexOf(n2.toString()) == -1);
        Assert.assertFalse(out.indexOf("NS") == -1);
        Assert.assertFalse(out.indexOf("IN") == -1);
        Assert.assertFalse(out.indexOf(TTL.format(ttl)) == -1);
        Options.set("noPrintIN");
        out = rec.toString();
        Assert.assertFalse(out.indexOf(n.toString()) == -1);
        Assert.assertFalse(out.indexOf(n2.toString()) == -1);
        Assert.assertFalse(out.indexOf("NS") == -1);
        Assert.assertTrue(out.indexOf("IN") == -1);
        Assert.assertFalse(out.indexOf(TTL.format(ttl)) == -1);
    }
    
    public void test_byteArrayFromString() throws TextParseException {
        String in = "the 98 \" ' quick 0xAB brown";
        byte[] out = SubRecord.byteArrayFromString(in);
        Assert.assertTrue(Arrays.equals(in.getBytes(), out));
        in = " \\031Aa\\;\\\"\\\\~\\127\\255";
        final byte[] exp = { 32, 31, 65, 97, 59, 34, 92, 126, 127, -1 };
        out = SubRecord.byteArrayFromString(in);
        Assert.assertTrue(Arrays.equals(exp, out));
    }
    
    public void test_byteArrayFromString_invalid() {
        final StringBuffer b = new StringBuffer();
        for (int i = 0; i < 257; ++i) {
            b.append('A');
        }
        try {
            SubRecord.byteArrayFromString(b.toString());
            Assert.fail("TextParseException not thrown");
        }
        catch (TextParseException ex) {}
        try {
            SubRecord.byteArrayFromString("\\256");
            Assert.fail("TextParseException not thrown");
        }
        catch (TextParseException ex2) {}
        try {
            SubRecord.byteArrayFromString("\\25a");
            Assert.fail("TextParseException not thrown");
        }
        catch (TextParseException ex3) {}
        try {
            SubRecord.byteArrayFromString("\\25");
            Assert.fail("TextParseException not thrown");
        }
        catch (TextParseException ex4) {}
        b.append("\\233");
        try {
            SubRecord.byteArrayFromString(b.toString());
            Assert.fail("TextParseException not thrown");
        }
        catch (TextParseException ex5) {}
    }
    
    public void test_byteArrayToString() {
        final byte[] in = { 32, 31, 65, 97, 59, 34, 92, 126, 127, -1 };
        final String exp = "\" \\031Aa;\\\"\\\\~\\127\\255\"";
        Assert.assertEquals(exp, SubRecord.byteArrayToString(in, true));
    }
    
    public void test_unknownToString() {
        final byte[] data = { 18, 52, 86, 120, -102, -68, -34, -1 };
        final String out = SubRecord.unknownToString(data);
        Assert.assertFalse(out.indexOf("" + data.length) == -1);
        Assert.assertFalse(out.indexOf("123456789ABCDEFF") == -1);
    }
    
    public void test_fromString() throws IOException, TextParseException {
        final Name n = Name.fromString("My.N.");
        final Name n2 = Name.fromString("My.Second.Name.");
        final int t = 1;
        final int d = 1;
        final int ttl = 704153;
        final String sa = "191.234.43.10";
        final InetAddress addr = InetAddress.getByName(sa);
        final byte[] b = { -65, -22, 43, 10 };
        Tokenizer st = new Tokenizer(sa);
        Record rec = Record.fromString(n, t, d, ttl, st, n2);
        Assert.assertTrue(rec instanceof ARecord);
        Assert.assertEquals(n, rec.getName());
        Assert.assertEquals(t, rec.getType());
        Assert.assertEquals(d, rec.getDClass());
        Assert.assertEquals(ttl, rec.getTTL());
        Assert.assertEquals(addr, ((ARecord)rec).getAddress());
        final String unkData = SubRecord.unknownToString(b);
        st = new Tokenizer(unkData);
        rec = Record.fromString(n, t, d, ttl, st, n2);
        Assert.assertTrue(rec instanceof ARecord);
        Assert.assertEquals(n, rec.getName());
        Assert.assertEquals(t, rec.getType());
        Assert.assertEquals(d, rec.getDClass());
        Assert.assertEquals(ttl, rec.getTTL());
        Assert.assertEquals(addr, ((ARecord)rec).getAddress());
    }
    
    public void test_fromString_invalid() throws IOException, TextParseException {
        final Name n = Name.fromString("My.N.");
        final Name rel = Name.fromString("My.R");
        final Name n2 = Name.fromString("My.Second.Name.");
        final int t = 1;
        final int d = 1;
        final int ttl = 704153;
        final InetAddress addr = InetAddress.getByName("191.234.43.10");
        Tokenizer st = new Tokenizer("191.234.43.10");
        try {
            Record.fromString(rel, t, d, ttl, st, n2);
            Assert.fail("RelativeNameException not thrown");
        }
        catch (RelativeNameException ex) {}
        st = new Tokenizer("191.234.43.10 another_token");
        try {
            Record.fromString(n, t, d, ttl, st, n2);
            Assert.fail("TextParseException not thrown");
        }
        catch (TextParseException ex2) {}
        st = new Tokenizer("\\# 100 ABCDE");
        try {
            Record.fromString(n, t, d, ttl, st, n2);
            Assert.fail("TextParseException not thrown");
        }
        catch (TextParseException ex3) {}
        try {
            Record.fromString(n, t, d, ttl, "\\# 100", n2);
            Assert.fail("TextParseException not thrown");
        }
        catch (TextParseException ex4) {}
    }
    
    public void test_getRRsetType() throws TextParseException {
        final Name n = Name.fromString("My.N.");
        Record r = Record.newRecord(n, 1, 1, 0L);
        Assert.assertEquals(1, r.getRRsetType());
        r = new RRSIGRecord(n, 1, 0L, 1, 1, 0L, new Date(), new Date(), 10, n, new byte[0]);
        Assert.assertEquals(1, r.getRRsetType());
    }
    
    public void test_sameRRset() throws TextParseException {
        final Name n = Name.fromString("My.N.");
        final Name m = Name.fromString("My.M.");
        Record r1 = Record.newRecord(n, 1, 1, 0L);
        Record r2 = new RRSIGRecord(n, 1, 0L, 1, 1, 0L, new Date(), new Date(), 10, n, new byte[0]);
        Assert.assertTrue(r1.sameRRset(r2));
        Assert.assertTrue(r2.sameRRset(r1));
        r1 = Record.newRecord(n, 1, 4, 0L);
        r2 = new RRSIGRecord(n, 1, 0L, 1, 1, 0L, new Date(), new Date(), 10, n, new byte[0]);
        Assert.assertFalse(r1.sameRRset(r2));
        Assert.assertFalse(r2.sameRRset(r1));
        r1 = Record.newRecord(n, 1, 1, 0L);
        r2 = new RRSIGRecord(m, 1, 0L, 1, 1, 0L, new Date(), new Date(), 10, n, new byte[0]);
        Assert.assertFalse(r1.sameRRset(r2));
        Assert.assertFalse(r2.sameRRset(r1));
    }
    
    public void test_equals() throws TextParseException {
        final Name n = Name.fromString("My.N.");
        final Name n2 = Name.fromString("my.n.");
        final Name m = Name.fromString("My.M.");
        Record r1 = Record.newRecord(n, 1, 1, 0L);
        Assert.assertFalse(r1.equals(null));
        Assert.assertFalse(r1.equals(new Object()));
        Record r2 = Record.newRecord(n, 1, 1, 0L);
        Assert.assertEquals(r1, r2);
        Assert.assertEquals(r2, r1);
        r2 = Record.newRecord(n2, 1, 1, 0L);
        Assert.assertEquals(r1, r2);
        Assert.assertEquals(r2, r1);
        r2 = Record.newRecord(n2, 1, 1, 703710L);
        Assert.assertEquals(r1, r2);
        Assert.assertEquals(r2, r1);
        r2 = Record.newRecord(m, 1, 1, 703710L);
        Assert.assertFalse(r1.equals(r2));
        Assert.assertFalse(r2.equals(r1));
        r2 = Record.newRecord(n2, 15, 1, 703710L);
        Assert.assertFalse(r1.equals(r2));
        Assert.assertFalse(r2.equals(r1));
        r2 = Record.newRecord(n2, 1, 3, 703710L);
        Assert.assertFalse(r1.equals(r2));
        Assert.assertFalse(r2.equals(r1));
        final byte[] d1 = { 23, 12, 9, -127 };
        final byte[] d2 = { -36, 1, -125, -44 };
        r1 = Record.newRecord(n, 1, 1, 11259369L, d1);
        r2 = Record.newRecord(n, 1, 1, 11259369L, d1);
        Assert.assertEquals(r1, r2);
        Assert.assertEquals(r2, r1);
        r2 = Record.newRecord(n, 1, 1, 11259369L, d2);
        Assert.assertFalse(r1.equals(r2));
        Assert.assertFalse(r2.equals(r1));
    }
    
    public void test_hashCode() throws TextParseException {
        final Name n = Name.fromString("My.N.");
        final Name n2 = Name.fromString("my.n.");
        final Name m = Name.fromString("My.M.");
        final byte[] d1 = { 23, 12, 9, -127 };
        final byte[] d2 = { -36, 1, -125, -44 };
        final Record r1 = Record.newRecord(n, 1, 1, 11259369L, d1);
        Record r2 = Record.newRecord(n, 1, 1, 11259369L, d1);
        Assert.assertEquals(r1.hashCode(), r2.hashCode());
        r2 = Record.newRecord(n2, 1, 1, 11259369L, d1);
        Assert.assertEquals(r1.hashCode(), r2.hashCode());
        r2 = Record.newRecord(m, 1, 1, 11259369L, d1);
        Assert.assertFalse(r1.hashCode() == r2.hashCode());
        r2 = Record.newRecord(n, 1, 3, 11259369L, d1);
        Assert.assertFalse(r1.hashCode() == r2.hashCode());
        r2 = Record.newRecord(n, 1, 1, 703710L, d1);
        Assert.assertEquals(r1.hashCode(), r2.hashCode());
        r2 = Record.newRecord(n, 1, 1, 11259369L, d2);
        Assert.assertFalse(r1.hashCode() == r2.hashCode());
    }
    
    public void test_cloneRecord() throws TextParseException {
        final Name n = Name.fromString("My.N.");
        final byte[] d = { 23, 12, 9, -127 };
        Record r = Record.newRecord(n, 1, 1, 11259369L, d);
        final Record r2 = r.cloneRecord();
        Assert.assertNotSame(r, r2);
        Assert.assertEquals(r, r2);
        r = new SubRecord(n, 1, 1, 11259369L);
        try {
            r.cloneRecord();
            Assert.fail("IllegalStateException not thrown");
        }
        catch (IllegalStateException ex) {}
    }
    
    public void test_withName() throws TextParseException {
        final Name n = Name.fromString("My.N.");
        final Name m = Name.fromString("My.M.Name.");
        final Name rel = Name.fromString("My.Relative.Name");
        final byte[] d = { 23, 12, 9, -127 };
        final Record r = Record.newRecord(n, 1, 1, 11259369L, d);
        final Record r2 = r.withName(m);
        Assert.assertEquals(m, r2.getName());
        Assert.assertEquals(1, r2.getType());
        Assert.assertEquals(1, r2.getDClass());
        Assert.assertEquals(11259369L, r2.getTTL());
        Assert.assertEquals(((ARecord)r).getAddress(), ((ARecord)r2).getAddress());
        try {
            r.withName(rel);
            Assert.fail("RelativeNameException not thrown");
        }
        catch (RelativeNameException ex) {}
    }
    
    public void test_withDClass() throws TextParseException {
        final Name n = Name.fromString("My.N.");
        final byte[] d = { 23, 12, 9, -127 };
        final Record r = Record.newRecord(n, 1, 1, 11259369L, d);
        final Record r2 = r.withDClass(4, 39030L);
        Assert.assertEquals(n, r2.getName());
        Assert.assertEquals(1, r2.getType());
        Assert.assertEquals(4, r2.getDClass());
        Assert.assertEquals(39030L, r2.getTTL());
        Assert.assertEquals(((ARecord)r).getAddress(), ((ARecord)r2).getAddress());
    }
    
    public void test_setTTL() throws TextParseException, UnknownHostException {
        final Name n = Name.fromString("My.N.");
        final byte[] d = { 23, 12, 9, -127 };
        final InetAddress exp = InetAddress.getByName("23.12.9.129");
        final Record r = Record.newRecord(n, 1, 1, 11259369L, d);
        Assert.assertEquals(11259369L, r.getTTL());
        r.setTTL(39030L);
        Assert.assertEquals(n, r.getName());
        Assert.assertEquals(1, r.getType());
        Assert.assertEquals(1, r.getDClass());
        Assert.assertEquals(39030L, r.getTTL());
        Assert.assertEquals(exp, ((ARecord)r).getAddress());
    }
    
    public void test_compareTo() throws TextParseException {
        final Name n = Name.fromString("My.N.");
        final Name n2 = Name.fromString("my.n.");
        Name m = Name.fromString("My.M.");
        final byte[] d = { 23, 12, 9, -127 };
        final byte[] d2 = { 23, 12, 9, -128 };
        Record r1 = Record.newRecord(n, 1, 1, 11259369L, d);
        Record r2 = Record.newRecord(n, 1, 1, 11259369L, d);
        Assert.assertEquals(0, r1.compareTo(r1));
        Assert.assertEquals(0, r1.compareTo(r2));
        Assert.assertEquals(0, r2.compareTo(r1));
        r2 = Record.newRecord(n2, 1, 1, 11259369L, d);
        Assert.assertEquals(0, r1.compareTo(r2));
        Assert.assertEquals(0, r2.compareTo(r1));
        r2 = Record.newRecord(m, 1, 1, 11259369L, d);
        Assert.assertEquals(n.compareTo(m), r1.compareTo(r2));
        Assert.assertEquals(m.compareTo(n), r2.compareTo(r1));
        r2 = Record.newRecord(n, 1, 3, 11259369L, d);
        Assert.assertEquals(-2, r1.compareTo(r2));
        Assert.assertEquals(2, r2.compareTo(r1));
        r2 = Record.newRecord(n, 2, 1, 11259369L, m.toWire());
        Assert.assertEquals(-1, r1.compareTo(r2));
        Assert.assertEquals(1, r2.compareTo(r1));
        r2 = Record.newRecord(n, 1, 1, 11259369L, d2);
        Assert.assertEquals(1, r1.compareTo(r2));
        Assert.assertEquals(-1, r2.compareTo(r1));
        m = Name.fromString("My.N.L.");
        r1 = Record.newRecord(n, 2, 1, 11259369L, n.toWire());
        r2 = Record.newRecord(n, 2, 1, 11259369L, m.toWire());
        Assert.assertEquals(-1, r1.compareTo(r2));
        Assert.assertEquals(1, r2.compareTo(r1));
    }
    
    public void test_getAdditionalName() throws TextParseException {
        final Name n = Name.fromString("My.N.");
        final Record r = new SubRecord(n, 1, 1, 11259369L);
        Assert.assertNull(r.getAdditionalName());
    }
    
    public void test_checkU8() {
        try {
            Record.checkU8("field", -1);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex) {}
        Assert.assertEquals(0, Record.checkU8("field", 0));
        Assert.assertEquals(157, Record.checkU8("field", 157));
        Assert.assertEquals(255, Record.checkU8("field", 255));
        try {
            Record.checkU8("field", 256);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex2) {}
    }
    
    public void test_checkU16() {
        try {
            Record.checkU16("field", -1);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex) {}
        Assert.assertEquals(0, Record.checkU16("field", 0));
        Assert.assertEquals(40353, Record.checkU16("field", 40353));
        Assert.assertEquals(65535, Record.checkU16("field", 65535));
        try {
            Record.checkU16("field", 65536);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex2) {}
    }
    
    public void test_checkU32() {
        try {
            Record.checkU32("field", -1L);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex) {}
        Assert.assertEquals(0L, Record.checkU32("field", 0L));
        Assert.assertEquals(2644635693L, Record.checkU32("field", 2644635693L));
        Assert.assertEquals(4294967295L, Record.checkU32("field", 4294967295L));
        try {
            Record.checkU32("field", 4294967296L);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex2) {}
    }
    
    public void test_checkName() throws TextParseException {
        final Name n = Name.fromString("My.N.");
        final Name m = Name.fromString("My.m");
        Assert.assertEquals(n, Record.checkName("field", n));
        try {
            Record.checkName("field", m);
            Assert.fail("RelativeNameException not thrown");
        }
        catch (RelativeNameException ex) {}
    }
    
    private static class SubRecord extends Record
    {
        public SubRecord() {
        }
        
        public SubRecord(final Name name, final int type, final int dclass, final long ttl) {
            super(name, type, dclass, ttl);
        }
        
        public Record getObject() {
            return null;
        }
        
        public void rrFromWire(final DNSInput in) throws IOException {
        }
        
        public String rrToString() {
            return "{SubRecord: rrToString}";
        }
        
        public void rdataFromString(final Tokenizer t, final Name origin) throws IOException {
        }
        
        public void rrToWire(final DNSOutput out, final Compression c, final boolean canonical) {
        }
        
        public static byte[] byteArrayFromString(final String in) throws TextParseException {
            return Record.byteArrayFromString(in);
        }
        
        public static String byteArrayToString(final byte[] in, final boolean quote) {
            return Record.byteArrayToString(in, quote);
        }
        
        public static String unknownToString(final byte[] in) {
            return Record.unknownToString(in);
        }
        
        public Object clone() throws CloneNotSupportedException {
            throw new CloneNotSupportedException();
        }
    }
}
