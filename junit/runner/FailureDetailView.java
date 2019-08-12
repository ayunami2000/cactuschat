// 
// Decompiled by Procyon v0.5.36
// 

package junit.runner;

import junit.framework.TestFailure;
import java.awt.Component;

public interface FailureDetailView
{
    Component getComponent();
    
    void showFailure(final TestFailure p0);
    
    void clear();
}
