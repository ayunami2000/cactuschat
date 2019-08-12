// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.util.Arrays;
import junit.framework.Assert;
import junit.framework.TestCase;

public class MXRecordTest extends TestCase
{
    public void test_getObject() {
        final MXRecord d = new MXRecord();
        final Record r = d.getObject();
        Assert.assertTrue(r instanceof MXRecord);
    }
    
    public void test_ctor_5arg() throws TextParseException {
        final Name n = Name.fromString("My.Name.");
        final Name m = Name.fromString("My.OtherName.");
        final MXRecord d = new MXRecord(n, 1, 703710L, 241, m);
        Assert.assertEquals(n, d.getName());
        Assert.assertEquals(15, d.getType());
        Assert.assertEquals(1, d.getDClass());
        Assert.assertEquals(703710L, d.getTTL());
        Assert.assertEquals(241, d.getPriority());
        Assert.assertEquals(m, d.getTarget());
        Assert.assertEquals(m, d.getAdditionalName());
    }
    
    public void test_rrToWire() throws TextParseException {
        final Name n = Name.fromString("My.Name.");
        final Name m = Name.fromString("M.O.n.");
        final MXRecord mr = new MXRecord(n, 1, 45359L, 7979, m);
        DNSOutput dout = new DNSOutput();
        mr.rrToWire(dout, null, true);
        byte[] out = dout.toByteArray();
        byte[] exp = { 31, 43, 1, 109, 1, 111, 1, 110, 0 };
        Assert.assertTrue(Arrays.equals(exp, out));
        dout = new DNSOutput();
        mr.rrToWire(dout, null, false);
        out = dout.toByteArray();
        exp = new byte[] { 31, 43, 1, 77, 1, 79, 1, 110, 0 };
        Assert.assertTrue(Arrays.equals(exp, out));
    }
}
