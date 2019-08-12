// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import junit.framework.Assert;
import junit.framework.TestCase;

public class MGRecordTest extends TestCase
{
    public void test_ctor_0arg() {
        final MGRecord d = new MGRecord();
        Assert.assertNull(d.getName());
        Assert.assertNull(d.getMailbox());
    }
    
    public void test_ctor_4arg() throws TextParseException {
        final Name n = Name.fromString("my.name.");
        final Name a = Name.fromString("my.alias.");
        final MGRecord d = new MGRecord(n, 1, 703710L, a);
        Assert.assertEquals(n, d.getName());
        Assert.assertEquals(8, d.getType());
        Assert.assertEquals(1, d.getDClass());
        Assert.assertEquals(703710L, d.getTTL());
        Assert.assertEquals(a, d.getMailbox());
    }
    
    public void test_getObject() {
        final MGRecord d = new MGRecord();
        final Record r = d.getObject();
        Assert.assertTrue(r instanceof MGRecord);
    }
}
