// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import junit.framework.Assert;
import junit.framework.TestCase;

public class MnemonicTest extends TestCase
{
    private Mnemonic m_mn;
    
    public MnemonicTest(final String name) {
        super(name);
    }
    
    public void setUp() {
        this.m_mn = new Mnemonic(MnemonicTest.class.getName() + " UPPER", 2);
    }
    
    public void test_toInteger() {
        Integer i = Mnemonic.toInteger(64);
        Assert.assertEquals(new Integer(64), i);
        Integer i2 = Mnemonic.toInteger(64);
        Assert.assertEquals(i, i2);
        Assert.assertNotSame(i, i2);
        i = Mnemonic.toInteger(-1);
        Assert.assertEquals(new Integer(-1), i);
        i2 = Mnemonic.toInteger(-1);
        Assert.assertEquals(i, i2);
        Assert.assertNotSame(i, i2);
        i = Mnemonic.toInteger(0);
        Assert.assertEquals(new Integer(0), i);
        i2 = Mnemonic.toInteger(0);
        Assert.assertEquals(i, i2);
        Assert.assertSame(i, i2);
        i = Mnemonic.toInteger(63);
        Assert.assertEquals(new Integer(63), i);
        i2 = Mnemonic.toInteger(63);
        Assert.assertEquals(i, i2);
        Assert.assertSame(i, i2);
    }
    
    public void test_no_maximum() {
        try {
            this.m_mn.check(-1);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex) {}
        try {
            this.m_mn.check(0);
        }
        catch (IllegalArgumentException e) {
            Assert.fail(e.getMessage());
        }
        try {
            this.m_mn.check(Integer.MAX_VALUE);
        }
        catch (IllegalArgumentException e) {
            Assert.fail(e.getMessage());
        }
        this.m_mn.setNumericAllowed(true);
        int val = this.m_mn.getValue("-2");
        Assert.assertEquals(-1, val);
        val = this.m_mn.getValue("0");
        Assert.assertEquals(0, val);
        val = this.m_mn.getValue("2147483647");
        Assert.assertEquals(Integer.MAX_VALUE, val);
    }
    
    public void test_setMaximum() {
        this.m_mn.setMaximum(15);
        try {
            this.m_mn.check(-1);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex) {}
        try {
            this.m_mn.check(0);
        }
        catch (IllegalArgumentException e) {
            Assert.fail(e.getMessage());
        }
        try {
            this.m_mn.check(15);
        }
        catch (IllegalArgumentException e) {
            Assert.fail(e.getMessage());
        }
        try {
            this.m_mn.check(16);
            Assert.fail("IllegalArgumentException not thrown");
        }
        catch (IllegalArgumentException ex2) {}
        this.m_mn.setNumericAllowed(true);
        int val = this.m_mn.getValue("-2");
        Assert.assertEquals(-1, val);
        val = this.m_mn.getValue("0");
        Assert.assertEquals(0, val);
        val = this.m_mn.getValue("15");
        Assert.assertEquals(15, val);
        val = this.m_mn.getValue("16");
        Assert.assertEquals(-1, val);
    }
    
    public void test_setPrefix() {
        final String prefix = "A mixed CASE Prefix".toUpperCase();
        this.m_mn.setPrefix(prefix);
        final String out = this.m_mn.getText(10);
        Assert.assertEquals(prefix + "10", out);
        final int i = this.m_mn.getValue(out);
        Assert.assertEquals(10, i);
    }
    
    public void test_basic_operation() {
        this.m_mn.add(10, "Ten");
        this.m_mn.add(20, "Twenty");
        this.m_mn.addAlias(20, "Veinte");
        this.m_mn.add(30, "Thirty");
        String text = this.m_mn.getText(10);
        Assert.assertEquals("TEN", text);
        text = this.m_mn.getText(20);
        Assert.assertEquals("TWENTY", text);
        text = this.m_mn.getText(30);
        Assert.assertEquals("THIRTY", text);
        text = this.m_mn.getText(40);
        Assert.assertEquals("40", text);
        int value = this.m_mn.getValue("tEn");
        Assert.assertEquals(10, value);
        value = this.m_mn.getValue("twenty");
        Assert.assertEquals(20, value);
        value = this.m_mn.getValue("VeiNTe");
        Assert.assertEquals(20, value);
        value = this.m_mn.getValue("THIRTY");
        Assert.assertEquals(30, value);
    }
    
    public void test_basic_operation_lower() {
        (this.m_mn = new Mnemonic(MnemonicTest.class.getName() + " LOWER", 3)).add(10, "Ten");
        this.m_mn.add(20, "Twenty");
        this.m_mn.addAlias(20, "Veinte");
        this.m_mn.add(30, "Thirty");
        String text = this.m_mn.getText(10);
        Assert.assertEquals("ten", text);
        text = this.m_mn.getText(20);
        Assert.assertEquals("twenty", text);
        text = this.m_mn.getText(30);
        Assert.assertEquals("thirty", text);
        text = this.m_mn.getText(40);
        Assert.assertEquals("40", text);
        int value = this.m_mn.getValue("tEn");
        Assert.assertEquals(10, value);
        value = this.m_mn.getValue("twenty");
        Assert.assertEquals(20, value);
        value = this.m_mn.getValue("VeiNTe");
        Assert.assertEquals(20, value);
        value = this.m_mn.getValue("THIRTY");
        Assert.assertEquals(30, value);
    }
    
    public void test_basic_operation_sensitive() {
        (this.m_mn = new Mnemonic(MnemonicTest.class.getName() + " SENSITIVE", 1)).add(10, "Ten");
        this.m_mn.add(20, "Twenty");
        this.m_mn.addAlias(20, "Veinte");
        this.m_mn.add(30, "Thirty");
        String text = this.m_mn.getText(10);
        Assert.assertEquals("Ten", text);
        text = this.m_mn.getText(20);
        Assert.assertEquals("Twenty", text);
        text = this.m_mn.getText(30);
        Assert.assertEquals("Thirty", text);
        text = this.m_mn.getText(40);
        Assert.assertEquals("40", text);
        int value = this.m_mn.getValue("Ten");
        Assert.assertEquals(10, value);
        value = this.m_mn.getValue("twenty");
        Assert.assertEquals(-1, value);
        value = this.m_mn.getValue("Twenty");
        Assert.assertEquals(20, value);
        value = this.m_mn.getValue("VEINTE");
        Assert.assertEquals(-1, value);
        value = this.m_mn.getValue("Veinte");
        Assert.assertEquals(20, value);
        value = this.m_mn.getValue("Thirty");
        Assert.assertEquals(30, value);
    }
    
    public void test_invalid_numeric() {
        this.m_mn.setNumericAllowed(true);
        final int value = this.m_mn.getValue("Not-A-Number");
        Assert.assertEquals(-1, value);
    }
    
    public void test_addAll() {
        this.m_mn.add(10, "Ten");
        this.m_mn.add(20, "Twenty");
        final Mnemonic mn2 = new Mnemonic("second test Mnemonic", 2);
        mn2.add(20, "Twenty");
        mn2.addAlias(20, "Veinte");
        mn2.add(30, "Thirty");
        this.m_mn.addAll(mn2);
        String text = this.m_mn.getText(10);
        Assert.assertEquals("TEN", text);
        text = this.m_mn.getText(20);
        Assert.assertEquals("TWENTY", text);
        text = this.m_mn.getText(30);
        Assert.assertEquals("THIRTY", text);
        text = this.m_mn.getText(40);
        Assert.assertEquals("40", text);
        int value = this.m_mn.getValue("tEn");
        Assert.assertEquals(10, value);
        value = this.m_mn.getValue("twenty");
        Assert.assertEquals(20, value);
        value = this.m_mn.getValue("VeiNTe");
        Assert.assertEquals(20, value);
        value = this.m_mn.getValue("THIRTY");
        Assert.assertEquals(30, value);
    }
}
