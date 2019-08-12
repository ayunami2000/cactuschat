// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import junit.framework.Assert;
import junit.framework.TestCase;

public class NSAP_PTRRecordTest extends TestCase
{
    public void test_ctor_0arg() {
        final NSAP_PTRRecord d = new NSAP_PTRRecord();
        Assert.assertNull(d.getName());
        Assert.assertNull(d.getTarget());
    }
    
    public void test_ctor_4arg() throws TextParseException {
        final Name n = Name.fromString("my.name.");
        final Name a = Name.fromString("my.alias.");
        final NSAP_PTRRecord d = new NSAP_PTRRecord(n, 1, 703710L, a);
        Assert.assertEquals(n, d.getName());
        Assert.assertEquals(23, d.getType());
        Assert.assertEquals(1, d.getDClass());
        Assert.assertEquals(703710L, d.getTTL());
        Assert.assertEquals(a, d.getTarget());
    }
    
    public void test_getObject() {
        final NSAP_PTRRecord d = new NSAP_PTRRecord();
        final Record r = d.getObject();
        Assert.assertTrue(r instanceof NSAP_PTRRecord);
    }
}
