// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.io.IOException;
import java.net.UnknownHostException;
import junit.framework.Assert;
import junit.framework.TestCase;

public class EmptyRecordTest extends TestCase
{
    public void test_ctor() throws UnknownHostException {
        final EmptyRecord ar = new EmptyRecord();
        Assert.assertNull(ar.getName());
        Assert.assertEquals(0, ar.getType());
        Assert.assertEquals(0, ar.getDClass());
        Assert.assertEquals(0L, ar.getTTL());
    }
    
    public void test_getObject() {
        final EmptyRecord ar = new EmptyRecord();
        final Record r = ar.getObject();
        Assert.assertTrue(r instanceof EmptyRecord);
    }
    
    public void test_rrFromWire() throws IOException {
        final DNSInput i = new DNSInput(new byte[] { 1, 2, 3, 4, 5 });
        i.jump(3);
        final EmptyRecord er = new EmptyRecord();
        er.rrFromWire(i);
        Assert.assertEquals(3, i.current());
        Assert.assertNull(er.getName());
        Assert.assertEquals(0, er.getType());
        Assert.assertEquals(0, er.getDClass());
        Assert.assertEquals(0L, er.getTTL());
    }
    
    public void test_rdataFromString() throws IOException {
        final Tokenizer t = new Tokenizer("these are the tokens");
        final EmptyRecord er = new EmptyRecord();
        er.rdataFromString(t, null);
        Assert.assertNull(er.getName());
        Assert.assertEquals(0, er.getType());
        Assert.assertEquals(0, er.getDClass());
        Assert.assertEquals(0L, er.getTTL());
        Assert.assertEquals("these", t.getString());
    }
    
    public void test_rrToString() {
        final EmptyRecord er = new EmptyRecord();
        Assert.assertEquals("", er.rrToString());
    }
    
    public void test_rrToWire() {
        final EmptyRecord er = new EmptyRecord();
        final DNSOutput out = new DNSOutput();
        er.rrToWire(out, null, true);
        Assert.assertEquals(0, out.toByteArray().length);
    }
}
