// 
// Decompiled by Procyon v0.5.36
// 

package junit.framework;

import java.util.Enumeration;
import java.util.Vector;

public class TestResult
{
    protected Vector fFailures;
    protected Vector fErrors;
    protected Vector fListeners;
    protected int fRunTests;
    private boolean fStop;
    
    public TestResult() {
        this.fFailures = new Vector();
        this.fErrors = new Vector();
        this.fListeners = new Vector();
        this.fRunTests = 0;
        this.fStop = false;
    }
    
    public synchronized void addError(final Test test, final Throwable t) {
        this.fErrors.addElement(new TestFailure(test, t));
        final Enumeration e = this.cloneListeners().elements();
        while (e.hasMoreElements()) {
            e.nextElement().addError(test, t);
        }
    }
    
    public synchronized void addFailure(final Test test, final AssertionFailedError t) {
        this.fFailures.addElement(new TestFailure(test, t));
        final Enumeration e = this.cloneListeners().elements();
        while (e.hasMoreElements()) {
            e.nextElement().addFailure(test, t);
        }
    }
    
    public synchronized void addListener(final TestListener listener) {
        this.fListeners.addElement(listener);
    }
    
    public synchronized void removeListener(final TestListener listener) {
        this.fListeners.removeElement(listener);
    }
    
    private synchronized Vector cloneListeners() {
        return (Vector)this.fListeners.clone();
    }
    
    public void endTest(final Test test) {
        final Enumeration e = this.cloneListeners().elements();
        while (e.hasMoreElements()) {
            e.nextElement().endTest(test);
        }
    }
    
    public synchronized int errorCount() {
        return this.fErrors.size();
    }
    
    public synchronized Enumeration errors() {
        return this.fErrors.elements();
    }
    
    public synchronized int failureCount() {
        return this.fFailures.size();
    }
    
    public synchronized Enumeration failures() {
        return this.fFailures.elements();
    }
    
    protected void run(final TestCase test) {
        this.startTest(test);
        final Protectable p = new Protectable() {
            public void protect() throws Throwable {
                test.runBare();
            }
        };
        this.runProtected(test, p);
        this.endTest(test);
    }
    
    public synchronized int runCount() {
        return this.fRunTests;
    }
    
    public void runProtected(final Test test, final Protectable p) {
        try {
            p.protect();
        }
        catch (AssertionFailedError e) {
            this.addFailure(test, e);
        }
        catch (ThreadDeath e2) {
            throw e2;
        }
        catch (Throwable e3) {
            this.addError(test, e3);
        }
    }
    
    public synchronized boolean shouldStop() {
        return this.fStop;
    }
    
    public void startTest(final Test test) {
        final int count = test.countTestCases();
        synchronized (this) {
            this.fRunTests += count;
        }
        final Enumeration e = this.cloneListeners().elements();
        while (e.hasMoreElements()) {
            e.nextElement().startTest(test);
        }
    }
    
    public synchronized void stop() {
        this.fStop = true;
    }
    
    public synchronized boolean wasSuccessful() {
        return this.failureCount() == 0 && this.errorCount() == 0;
    }
}
