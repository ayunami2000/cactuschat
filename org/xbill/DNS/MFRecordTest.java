// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import junit.framework.Assert;
import junit.framework.TestCase;

public class MFRecordTest extends TestCase
{
    public void test_ctor_0arg() {
        final MFRecord d = new MFRecord();
        Assert.assertNull(d.getName());
        Assert.assertNull(d.getAdditionalName());
        Assert.assertNull(d.getMailAgent());
    }
    
    public void test_ctor_4arg() throws TextParseException {
        final Name n = Name.fromString("my.name.");
        final Name a = Name.fromString("my.alias.");
        final MFRecord d = new MFRecord(n, 1, 703710L, a);
        Assert.assertEquals(n, d.getName());
        Assert.assertEquals(4, d.getType());
        Assert.assertEquals(1, d.getDClass());
        Assert.assertEquals(703710L, d.getTTL());
        Assert.assertEquals(a, d.getAdditionalName());
        Assert.assertEquals(a, d.getMailAgent());
    }
    
    public void test_getObject() {
        final MFRecord d = new MFRecord();
        final Record r = d.getObject();
        Assert.assertTrue(r instanceof MFRecord);
    }
}
