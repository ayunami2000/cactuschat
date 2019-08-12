// 
// Decompiled by Procyon v0.5.36
// 

package junit.swingui;

import javax.swing.ListModel;
import junit.framework.Test;

public interface TestRunContext
{
    void handleTestSelected(final Test p0);
    
    ListModel getFailures();
}
