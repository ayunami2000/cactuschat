// 
// Decompiled by Procyon v0.5.36
// 

package junit.framework;

import java.io.Writer;
import java.io.PrintWriter;
import java.io.StringWriter;

public class TestFailure
{
    protected Test fFailedTest;
    protected Throwable fThrownException;
    
    public TestFailure(final Test failedTest, final Throwable thrownException) {
        this.fFailedTest = failedTest;
        this.fThrownException = thrownException;
    }
    
    public Test failedTest() {
        return this.fFailedTest;
    }
    
    public Throwable thrownException() {
        return this.fThrownException;
    }
    
    public String toString() {
        final StringBuffer buffer = new StringBuffer();
        buffer.append(this.fFailedTest + ": " + this.fThrownException.getMessage());
        return buffer.toString();
    }
    
    public String trace() {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter);
        this.thrownException().printStackTrace(writer);
        final StringBuffer buffer = stringWriter.getBuffer();
        return buffer.toString();
    }
    
    public String exceptionMessage() {
        return this.thrownException().getMessage();
    }
    
    public boolean isFailure() {
        return this.thrownException() instanceof AssertionFailedError;
    }
}
