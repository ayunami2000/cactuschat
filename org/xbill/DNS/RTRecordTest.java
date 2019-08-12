// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import junit.framework.Assert;
import junit.framework.TestCase;

public class RTRecordTest extends TestCase
{
    public void test_getObject() {
        final RTRecord d = new RTRecord();
        final Record r = d.getObject();
        Assert.assertTrue(r instanceof RTRecord);
    }
    
    public void test_ctor_5arg() throws TextParseException {
        final Name n = Name.fromString("My.Name.");
        final Name m = Name.fromString("My.OtherName.");
        final RTRecord d = new RTRecord(n, 1, 703710L, 241, m);
        Assert.assertEquals(n, d.getName());
        Assert.assertEquals(21, d.getType());
        Assert.assertEquals(1, d.getDClass());
        Assert.assertEquals(703710L, d.getTTL());
        Assert.assertEquals(241, d.getPreference());
        Assert.assertEquals(m, d.getIntermediateHost());
    }
}
