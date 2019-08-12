// 
// Decompiled by Procyon v0.5.36
// 

package junit.swingui;

import junit.framework.TestResult;
import javax.swing.JTabbedPane;
import junit.framework.Test;

interface TestRunView
{
    Test getSelectedTest();
    
    void activate();
    
    void revealFailure(final Test p0);
    
    void addTab(final JTabbedPane p0);
    
    void aboutToStart(final Test p0, final TestResult p1);
    
    void runFinished(final Test p0, final TestResult p1);
}
