// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import junit.framework.Assert;
import junit.framework.TestCase;

public class CNAMERecordTest extends TestCase
{
    public void test_ctor_0arg() {
        final CNAMERecord d = new CNAMERecord();
        Assert.assertNull(d.getName());
        Assert.assertNull(d.getTarget());
        Assert.assertNull(d.getAlias());
    }
    
    public void test_ctor_4arg() throws TextParseException {
        final Name n = Name.fromString("my.name.");
        final Name a = Name.fromString("my.alias.");
        final CNAMERecord d = new CNAMERecord(n, 1, 703710L, a);
        Assert.assertEquals(n, d.getName());
        Assert.assertEquals(5, d.getType());
        Assert.assertEquals(1, d.getDClass());
        Assert.assertEquals(703710L, d.getTTL());
        Assert.assertEquals(a, d.getTarget());
        Assert.assertEquals(a, d.getAlias());
    }
    
    public void test_getObject() {
        final CNAMERecord d = new CNAMERecord();
        final Record r = d.getObject();
        Assert.assertTrue(r instanceof CNAMERecord);
    }
}
