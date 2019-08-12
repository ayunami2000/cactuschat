// 
// Decompiled by Procyon v0.5.36
// 

package junit.extensions;

import junit.framework.Assert;
import junit.framework.TestCase;

public class ExceptionTestCase extends TestCase
{
    Class fExpected;
    
    public ExceptionTestCase(final String name, final Class exception) {
        super(name);
        this.fExpected = exception;
    }
    
    protected void runTest() throws Throwable {
        try {
            super.runTest();
        }
        catch (Exception e) {
            if (this.fExpected.isAssignableFrom(e.getClass())) {
                return;
            }
            throw e;
        }
        Assert.fail("Expected exception " + this.fExpected);
    }
}
