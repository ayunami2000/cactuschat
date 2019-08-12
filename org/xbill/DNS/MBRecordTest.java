// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import junit.framework.Assert;
import junit.framework.TestCase;

public class MBRecordTest extends TestCase
{
    public void test_ctor_0arg() {
        final MBRecord d = new MBRecord();
        Assert.assertNull(d.getName());
        Assert.assertNull(d.getAdditionalName());
        Assert.assertNull(d.getMailbox());
    }
    
    public void test_ctor_4arg() throws TextParseException {
        final Name n = Name.fromString("my.name.");
        final Name a = Name.fromString("my.alias.");
        final MBRecord d = new MBRecord(n, 1, 703710L, a);
        Assert.assertEquals(n, d.getName());
        Assert.assertEquals(7, d.getType());
        Assert.assertEquals(1, d.getDClass());
        Assert.assertEquals(703710L, d.getTTL());
        Assert.assertEquals(a, d.getAdditionalName());
        Assert.assertEquals(a, d.getMailbox());
    }
    
    public void test_getObject() {
        final MBRecord d = new MBRecord();
        final Record r = d.getObject();
        Assert.assertTrue(r instanceof MBRecord);
    }
}
