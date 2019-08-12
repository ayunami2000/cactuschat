// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import junit.framework.Assert;
import junit.framework.TestCase;

public class SerialTest extends TestCase
{
    public void test_compare_NegativeArg1() {
        final long arg1 = -1L;
        final long arg2 = 1L;
        try {
            Serial.compare(arg1, arg2);
            Assert.fail("compare accepted negative argument 1");
        }
        catch (IllegalArgumentException ex) {}
    }
    
    public void test_compare_OOBArg1() {
        final long arg1 = 4294967296L;
        final long arg2 = 1L;
        try {
            Serial.compare(arg1, arg2);
            Assert.fail("compare accepted out-of-bounds argument 1");
        }
        catch (IllegalArgumentException ex) {}
    }
    
    public void test_compare_NegativeArg2() {
        final long arg1 = 1L;
        final long arg2 = -1L;
        try {
            Serial.compare(arg1, arg2);
            Assert.fail("compare accepted negative argument 2");
        }
        catch (IllegalArgumentException ex) {}
    }
    
    public void test_compare_OOBArg2() {
        final long arg1 = 1L;
        final long arg2 = 4294967296L;
        try {
            Serial.compare(arg1, arg2);
            Assert.fail("compare accepted out-of-bounds argument 1");
        }
        catch (IllegalArgumentException ex) {}
    }
    
    public void test_compare_Arg1Greater() {
        final long arg1 = 10L;
        final long arg2 = 9L;
        final int ret = Serial.compare(arg1, arg2);
        Assert.assertTrue(ret > 0);
    }
    
    public void test_compare_Arg2Greater() {
        final long arg1 = 9L;
        final long arg2 = 10L;
        final int ret = Serial.compare(arg1, arg2);
        Assert.assertTrue(ret < 0);
    }
    
    public void test_compare_ArgsEqual() {
        final long arg1 = 10L;
        final long arg2 = 10L;
        final int ret = Serial.compare(arg1, arg2);
        Assert.assertEquals(ret, 0);
    }
    
    public void test_compare_boundary() {
        final long arg1 = 4294967295L;
        final long arg2 = 0L;
        int ret = Serial.compare(arg1, arg2);
        Assert.assertEquals(-1, ret);
        ret = Serial.compare(arg2, arg1);
        Assert.assertEquals(1, ret);
    }
    
    public void test_increment_NegativeArg() {
        final long arg = -1L;
        try {
            Serial.increment(arg);
            Assert.fail("increment accepted negative argument");
        }
        catch (IllegalArgumentException ex) {}
    }
    
    public void test_increment_OOBArg() {
        final long arg = 4294967296L;
        try {
            Serial.increment(arg);
            Assert.fail("increment accepted out-of-bounds argument");
        }
        catch (IllegalArgumentException ex) {}
    }
    
    public void test_increment_reset() {
        final long arg = 4294967295L;
        final long ret = Serial.increment(arg);
        Assert.assertEquals(0L, ret);
    }
    
    public void test_increment_normal() {
        final long arg = 10L;
        final long ret = Serial.increment(arg);
        Assert.assertEquals(arg + 1L, ret);
    }
}
