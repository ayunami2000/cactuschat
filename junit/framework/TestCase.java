// 
// Decompiled by Procyon v0.5.36
// 

package junit.framework;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

public abstract class TestCase extends Assert implements Test
{
    private String fName;
    
    public TestCase() {
        this.fName = null;
    }
    
    public TestCase(final String name) {
        this.fName = name;
    }
    
    public int countTestCases() {
        return 1;
    }
    
    protected TestResult createResult() {
        return new TestResult();
    }
    
    public TestResult run() {
        final TestResult result = this.createResult();
        this.run(result);
        return result;
    }
    
    public void run(final TestResult result) {
        result.run(this);
    }
    
    public void runBare() throws Throwable {
        Throwable exception = null;
        this.setUp();
        try {
            this.runTest();
        }
        catch (Throwable running) {
            exception = running;
        }
        finally {
            try {
                this.tearDown();
            }
            catch (Throwable tearingDown) {
                if (exception == null) {
                    exception = tearingDown;
                }
            }
        }
        if (exception != null) {
            throw exception;
        }
    }
    
    protected void runTest() throws Throwable {
        Assert.assertNotNull(this.fName);
        Method runMethod = null;
        try {
            runMethod = this.getClass().getMethod(this.fName, (Class<?>[])null);
        }
        catch (NoSuchMethodException e3) {
            Assert.fail("Method \"" + this.fName + "\" not found");
        }
        if (!Modifier.isPublic(runMethod.getModifiers())) {
            Assert.fail("Method \"" + this.fName + "\" should be public");
        }
        try {
            runMethod.invoke(this, (Object[])new Class[0]);
        }
        catch (InvocationTargetException e) {
            e.fillInStackTrace();
            throw e.getTargetException();
        }
        catch (IllegalAccessException e2) {
            e2.fillInStackTrace();
            throw e2;
        }
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    public String toString() {
        return this.getName() + "(" + this.getClass().getName() + ")";
    }
    
    public String getName() {
        return this.fName;
    }
    
    public void setName(final String name) {
        this.fName = name;
    }
}
