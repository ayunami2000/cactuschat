// 
// Decompiled by Procyon v0.5.36
// 

package junit.swingui;

import javax.swing.Icon;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.SwingUtilities;
import java.util.Vector;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.ToolTipManager;
import javax.swing.tree.TreeModel;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.BorderLayout;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import junit.framework.TestListener;
import javax.swing.JPanel;

class TestSuitePanel extends JPanel implements TestListener
{
    private JTree fTree;
    private JScrollPane fScrollTree;
    private TestTreeModel fModel;
    
    public TestSuitePanel() {
        super(new BorderLayout());
        this.setPreferredSize(new Dimension(300, 100));
        (this.fTree = new JTree()).setModel(null);
        this.fTree.setRowHeight(20);
        ToolTipManager.sharedInstance().registerComponent(this.fTree);
        this.fTree.putClientProperty("JTree.lineStyle", "Angled");
        this.add(this.fScrollTree = new JScrollPane(this.fTree), "Center");
    }
    
    public void addError(final Test test, final Throwable t) {
        this.fModel.addError(test);
        this.fireTestChanged(test, true);
    }
    
    public void addFailure(final Test test, final AssertionFailedError t) {
        this.fModel.addFailure(test);
        this.fireTestChanged(test, true);
    }
    
    public void endTest(final Test test) {
        this.fModel.addRunTest(test);
        this.fireTestChanged(test, false);
    }
    
    public void startTest(final Test test) {
    }
    
    public Test getSelectedTest() {
        final TreePath[] paths = this.fTree.getSelectionPaths();
        if (paths != null && paths.length == 1) {
            return (Test)paths[0].getLastPathComponent();
        }
        return null;
    }
    
    public JTree getTree() {
        return this.fTree;
    }
    
    public void showTestTree(final Test root) {
        this.fModel = new TestTreeModel(root);
        this.fTree.setModel(this.fModel);
        this.fTree.setCellRenderer(new TestTreeCellRenderer());
    }
    
    private void fireTestChanged(final Test test, final boolean expand) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                final Vector vpath = new Vector();
                final int index = TestSuitePanel.this.fModel.findTest(test, (Test)TestSuitePanel.this.fModel.getRoot(), vpath);
                if (index >= 0) {
                    final Object[] path = new Object[vpath.size()];
                    vpath.copyInto(path);
                    final TreePath treePath = new TreePath(path);
                    TestSuitePanel.this.fModel.fireNodeChanged(treePath, index);
                    if (expand) {
                        final Object[] fullPath = new Object[vpath.size() + 1];
                        vpath.copyInto(fullPath);
                        fullPath[vpath.size()] = TestSuitePanel.this.fModel.getChild(treePath.getLastPathComponent(), index);
                        final TreePath fullTreePath = new TreePath(fullPath);
                        TestSuitePanel.this.fTree.scrollPathToVisible(fullTreePath);
                    }
                }
            }
        });
    }
    
    static class TestTreeCellRenderer extends DefaultTreeCellRenderer
    {
        private Icon fErrorIcon;
        private Icon fOkIcon;
        private Icon fFailureIcon;
        
        TestTreeCellRenderer() {
            this.loadIcons();
        }
        
        void loadIcons() {
            this.fErrorIcon = TestRunner.getIconResource(this.getClass(), "icons/error.gif");
            this.fOkIcon = TestRunner.getIconResource(this.getClass(), "icons/ok.gif");
            this.fFailureIcon = TestRunner.getIconResource(this.getClass(), "icons/failure.gif");
        }
        
        String stripParenthesis(final Object o) {
            final String text = o.toString();
            final int pos = text.indexOf(40);
            if (pos < 1) {
                return text;
            }
            return text.substring(0, pos);
        }
        
        public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean sel, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
            final Component c = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            final TreeModel model = tree.getModel();
            if (model instanceof TestTreeModel) {
                final TestTreeModel testModel = (TestTreeModel)model;
                final Test t = (Test)value;
                String s = "";
                if (testModel.isFailure(t)) {
                    if (this.fFailureIcon != null) {
                        this.setIcon(this.fFailureIcon);
                    }
                    s = " - Failed";
                }
                else if (testModel.isError(t)) {
                    if (this.fErrorIcon != null) {
                        this.setIcon(this.fErrorIcon);
                    }
                    s = " - Error";
                }
                else if (testModel.wasRun(t)) {
                    if (this.fOkIcon != null) {
                        this.setIcon(this.fOkIcon);
                    }
                    s = " - Passed";
                }
                if (c instanceof JComponent) {
                    ((JComponent)c).setToolTipText(this.getText() + s);
                }
            }
            this.setText(this.stripParenthesis(value));
            return c;
        }
    }
}
