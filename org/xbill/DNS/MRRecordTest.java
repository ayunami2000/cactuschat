// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import junit.framework.Assert;
import junit.framework.TestCase;

public class MRRecordTest extends TestCase
{
    public void test_ctor_0arg() {
        final MRRecord d = new MRRecord();
        Assert.assertNull(d.getName());
        Assert.assertNull(d.getNewName());
    }
    
    public void test_ctor_4arg() throws TextParseException {
        final Name n = Name.fromString("my.name.");
        final Name a = Name.fromString("my.alias.");
        final MRRecord d = new MRRecord(n, 1, 703710L, a);
        Assert.assertEquals(n, d.getName());
        Assert.assertEquals(9, d.getType());
        Assert.assertEquals(1, d.getDClass());
        Assert.assertEquals(703710L, d.getTTL());
        Assert.assertEquals(a, d.getNewName());
    }
    
    public void test_getObject() {
        final MRRecord d = new MRRecord();
        final Record r = d.getObject();
        Assert.assertTrue(r instanceof MRRecord);
    }
}
