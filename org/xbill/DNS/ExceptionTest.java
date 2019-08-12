// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.io.IOException;
import junit.framework.Assert;
import junit.framework.TestCase;

public class ExceptionTest extends TestCase
{
    public void test_InvalidDClassException() {
        final IllegalArgumentException e = new InvalidDClassException(10);
        Assert.assertEquals("Invalid DNS class: 10", e.getMessage());
    }
    
    public void test_InvalidTTLException() {
        final IllegalArgumentException e = new InvalidTTLException(32345L);
        Assert.assertEquals("Invalid DNS TTL: 32345", e.getMessage());
    }
    
    public void test_InvalidTypeException() {
        final IllegalArgumentException e = new InvalidTypeException(32345);
        Assert.assertEquals("Invalid DNS type: 32345", e.getMessage());
    }
    
    public void test_NameTooLongException() {
        WireParseException e = new NameTooLongException();
        Assert.assertNull(e.getMessage());
        e = new NameTooLongException("This is my too long name");
        Assert.assertEquals("This is my too long name", e.getMessage());
    }
    
    public void test_RelativeNameException() throws TextParseException {
        IllegalArgumentException e = new RelativeNameException("This is my relative name");
        Assert.assertEquals("This is my relative name", e.getMessage());
        e = new RelativeNameException(Name.fromString("relative"));
        Assert.assertEquals("'relative' is not an absolute name", e.getMessage());
    }
    
    public void test_TextParseException() {
        IOException e = new TextParseException();
        Assert.assertNull(e.getMessage());
        e = new TextParseException("This is my message");
        Assert.assertEquals("This is my message", e.getMessage());
    }
    
    public void test_WireParseException() {
        IOException e = new WireParseException();
        Assert.assertNull(e.getMessage());
        e = new WireParseException("This is my message");
        Assert.assertEquals("This is my message", e.getMessage());
    }
    
    public void test_ZoneTransferException() {
        Exception e = new ZoneTransferException();
        Assert.assertNull(e.getMessage());
        e = new ZoneTransferException("This is my message");
        Assert.assertEquals("This is my message", e.getMessage());
    }
}
