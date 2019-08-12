// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import junit.framework.Assert;
import junit.framework.TestCase;

public class SectionTest extends TestCase
{
    public void test_string() {
        Assert.assertEquals("au", Section.string(2));
        try {
            Section.string(-1);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex) {}
        try {
            Section.string(4);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex2) {}
    }
    
    public void test_value() {
        Assert.assertEquals(3, Section.value("ad"));
        Assert.assertEquals(-1, Section.value("THIS IS DEFINITELY UNKNOWN"));
        Assert.assertEquals(-1, Section.value(""));
    }
    
    public void test_longString() {
        Assert.assertEquals("ADDITIONAL RECORDS", Section.longString(3));
        try {
            Section.longString(-1);
        }
        catch (IllegalArgumentException ex) {}
        try {
            Section.longString(4);
        }
        catch (IllegalArgumentException ex2) {}
    }
    
    public void test_updString() {
        Assert.assertEquals("ZONE", Section.updString(0));
        try {
            Section.longString(-1);
        }
        catch (IllegalArgumentException ex) {}
        try {
            Section.longString(4);
        }
        catch (IllegalArgumentException ex2) {}
    }
}
