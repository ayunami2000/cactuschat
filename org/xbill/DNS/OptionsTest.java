// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import junit.framework.Assert;
import junit.framework.TestCase;

public class OptionsTest extends TestCase
{
    public void setUp() {
        Options.clear();
    }
    
    public void test_set_1arg() {
        Options.set("Option1");
        Assert.assertEquals("true", Options.value("option1"));
        Options.set("OPTION2");
        Assert.assertEquals("true", Options.value("option1"));
        Assert.assertEquals("true", Options.value("OpTIOn2"));
        Options.set("option2");
        Assert.assertEquals("true", Options.value("option2"));
    }
    
    public void test_set_2arg() {
        Options.set("OPTION1", "Value1");
        Assert.assertEquals("value1", Options.value("Option1"));
        Options.set("option2", "value2");
        Assert.assertEquals("value1", Options.value("Option1"));
        Assert.assertEquals("value2", Options.value("OPTION2"));
        Options.set("OPTION2", "value2b");
        Assert.assertEquals("value1", Options.value("Option1"));
        Assert.assertEquals("value2b", Options.value("option2"));
    }
    
    public void test_check() {
        Assert.assertFalse(Options.check("No Options yet"));
        Options.set("First Option");
        Assert.assertFalse(Options.check("Not a valid option name"));
        Assert.assertTrue(Options.check("First Option"));
        Assert.assertTrue(Options.check("FIRST option"));
    }
    
    public void test_unset() {
        Options.unset("Not an option Name");
        Options.set("Temporary Option");
        Assert.assertTrue(Options.check("Temporary Option"));
        Options.unset("Temporary Option");
        Assert.assertFalse(Options.check("Temporary Option"));
        Options.set("Temporary Option");
        Assert.assertTrue(Options.check("Temporary Option"));
        Options.unset("temporary option");
        Assert.assertFalse(Options.check("Temporary Option"));
        Options.unset("Still Not an Option Name");
    }
    
    public void test_value() {
        Assert.assertNull(Options.value("Table is Null"));
        Options.set("Testing Option");
        Assert.assertNull(Options.value("Not an Option Name"));
        Assert.assertEquals("true", Options.value("Testing OPTION"));
    }
    
    public void test_intValue() {
        Assert.assertEquals(-1, Options.intValue("Table is Null"));
        Options.set("A Boolean Option");
        Options.set("An Int Option", "13");
        Options.set("Not An Int Option", "NotAnInt");
        Options.set("A Negative Int Value", "-1000");
        Assert.assertEquals(-1, Options.intValue("A Boolean Option"));
        Assert.assertEquals(-1, Options.intValue("Not an Option NAME"));
        Assert.assertEquals(13, Options.intValue("an int option"));
        Assert.assertEquals(-1, Options.intValue("NOT an INT option"));
        Assert.assertEquals(-1, Options.intValue("A negative int Value"));
    }
    
    public void test_systemProperty() {
        System.setProperty("dnsjava.options", "booleanOption,valuedOption1=10,valuedOption2=NotAnInteger");
        Options.refresh();
        Assert.assertTrue(Options.check("booleanOPTION"));
        Assert.assertTrue(Options.check("booleanOption"));
        Assert.assertTrue(Options.check("valuedOption1"));
        Assert.assertTrue(Options.check("ValuedOption2"));
        Assert.assertEquals("true", Options.value("booleanOption"));
        Assert.assertEquals(-1, Options.intValue("BOOLEANOPTION"));
        Assert.assertEquals("10", Options.value("valuedOption1"));
        Assert.assertEquals(10, Options.intValue("valuedOption1"));
        Assert.assertEquals("notaninteger", Options.value("VALUEDOPTION2"));
        Assert.assertEquals(-1, Options.intValue("valuedOption2"));
    }
}
