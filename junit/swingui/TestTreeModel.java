// 
// Decompiled by Procyon v0.5.36
// 

package junit.swingui;

import junit.extensions.TestDecorator;
import java.util.Enumeration;
import javax.swing.event.TreeModelEvent;
import javax.swing.tree.TreePath;
import junit.framework.TestSuite;
import javax.swing.event.TreeModelListener;
import java.util.Hashtable;
import java.util.Vector;
import junit.framework.Test;
import javax.swing.tree.TreeModel;

class TestTreeModel implements TreeModel
{
    private Test fRoot;
    private Vector fModelListeners;
    private Hashtable fFailures;
    private Hashtable fErrors;
    private Hashtable fRunTests;
    
    public TestTreeModel(final Test root) {
        this.fModelListeners = new Vector();
        this.fFailures = new Hashtable();
        this.fErrors = new Hashtable();
        this.fRunTests = new Hashtable();
        this.fRoot = root;
    }
    
    public void addTreeModelListener(final TreeModelListener l) {
        if (!this.fModelListeners.contains(l)) {
            this.fModelListeners.addElement(l);
        }
    }
    
    public void removeTreeModelListener(final TreeModelListener l) {
        this.fModelListeners.removeElement(l);
    }
    
    public int findTest(final Test target, final Test node, final Vector path) {
        if (target.equals(node)) {
            return 0;
        }
        final TestSuite suite = this.isTestSuite(node);
        int i = 0;
        while (i < this.getChildCount(node)) {
            final Test t = suite.testAt(i);
            final int index = this.findTest(target, t, path);
            if (index >= 0) {
                path.insertElementAt(node, 0);
                if (path.size() == 1) {
                    return i;
                }
                return index;
            }
            else {
                ++i;
            }
        }
        return -1;
    }
    
    public void fireNodeChanged(final TreePath path, final int index) {
        final int[] indices = { index };
        final Object[] changedChildren = { this.getChild(path.getLastPathComponent(), index) };
        final TreeModelEvent event = new TreeModelEvent(this, path, indices, changedChildren);
        final Enumeration e = this.fModelListeners.elements();
        while (e.hasMoreElements()) {
            final TreeModelListener l = e.nextElement();
            l.treeNodesChanged(event);
        }
    }
    
    public Object getChild(final Object parent, final int index) {
        final TestSuite suite = this.isTestSuite(parent);
        if (suite != null) {
            return suite.testAt(index);
        }
        return null;
    }
    
    public int getChildCount(final Object parent) {
        final TestSuite suite = this.isTestSuite(parent);
        if (suite != null) {
            return suite.testCount();
        }
        return 0;
    }
    
    public int getIndexOfChild(final Object parent, final Object child) {
        final TestSuite suite = this.isTestSuite(parent);
        if (suite != null) {
            int i = 0;
            final Enumeration e = suite.tests();
            while (e.hasMoreElements()) {
                if (child.equals(e.nextElement())) {
                    return i;
                }
                ++i;
            }
        }
        return -1;
    }
    
    public Object getRoot() {
        return this.fRoot;
    }
    
    public boolean isLeaf(final Object node) {
        return this.isTestSuite(node) == null;
    }
    
    TestSuite isTestSuite(final Object node) {
        if (node instanceof TestSuite) {
            return (TestSuite)node;
        }
        if (node instanceof TestDecorator) {
            final Test baseTest = ((TestDecorator)node).getTest();
            return this.isTestSuite(baseTest);
        }
        return null;
    }
    
    public void valueForPathChanged(final TreePath path, final Object newValue) {
        System.out.println("TreeModel.valueForPathChanged: not implemented");
    }
    
    void addFailure(final Test t) {
        this.fFailures.put(t, t);
    }
    
    void addError(final Test t) {
        this.fErrors.put(t, t);
    }
    
    void addRunTest(final Test t) {
        this.fRunTests.put(t, t);
    }
    
    boolean wasRun(final Test t) {
        return this.fRunTests.get(t) != null;
    }
    
    boolean isError(final Test t) {
        return this.fErrors != null && this.fErrors.get(t) != null;
    }
    
    boolean isFailure(final Test t) {
        return this.fFailures != null && this.fFailures.get(t) != null;
    }
    
    void resetResults() {
        this.fFailures = new Hashtable();
        this.fRunTests = new Hashtable();
        this.fErrors = new Hashtable();
    }
}
