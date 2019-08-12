// 
// Decompiled by Procyon v0.5.36
// 

package junit.swingui;

import junit.framework.TestListener;
import junit.framework.TestResult;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import java.util.Vector;
import junit.framework.Test;
import javax.swing.Icon;
import java.awt.Component;
import javax.swing.JTabbedPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

public class TestHierarchyRunView implements TestRunView
{
    TestSuitePanel fTreeBrowser;
    TestRunContext fTestContext;
    
    public TestHierarchyRunView(final TestRunContext context) {
        this.fTestContext = context;
        this.fTreeBrowser = new TestSuitePanel();
        this.fTreeBrowser.getTree().addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(final TreeSelectionEvent e) {
                TestHierarchyRunView.this.testSelected();
            }
        });
    }
    
    public void addTab(final JTabbedPane pane) {
        final Icon treeIcon = TestRunner.getIconResource(this.getClass(), "icons/hierarchy.gif");
        pane.addTab("Test Hierarchy", treeIcon, this.fTreeBrowser, "The test hierarchy");
    }
    
    public Test getSelectedTest() {
        return this.fTreeBrowser.getSelectedTest();
    }
    
    public void activate() {
        this.testSelected();
    }
    
    public void revealFailure(final Test failure) {
        final JTree tree = this.fTreeBrowser.getTree();
        final TestTreeModel model = (TestTreeModel)tree.getModel();
        final Vector vpath = new Vector();
        final int index = model.findTest(failure, (Test)model.getRoot(), vpath);
        if (index >= 0) {
            final Object[] path = new Object[vpath.size() + 1];
            vpath.copyInto(path);
            final Object last = path[vpath.size() - 1];
            path[vpath.size()] = model.getChild(last, index);
            final TreePath selectionPath = new TreePath(path);
            tree.setSelectionPath(selectionPath);
            tree.makeVisible(selectionPath);
        }
    }
    
    public void aboutToStart(final Test suite, final TestResult result) {
        this.fTreeBrowser.showTestTree(suite);
        result.addListener(this.fTreeBrowser);
    }
    
    public void runFinished(final Test suite, final TestResult result) {
        result.removeListener(this.fTreeBrowser);
    }
    
    protected void testSelected() {
        this.fTestContext.handleTestSelected(this.getSelectedTest());
    }
}
