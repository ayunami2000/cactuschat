// 
// Decompiled by Procyon v0.5.36
// 

package junit.swingui;

import junit.runner.BaseTestRunner;
import javax.swing.DefaultListCellRenderer;
import junit.framework.TestResult;
import javax.swing.Icon;
import java.awt.Component;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import junit.framework.TestFailure;
import junit.framework.Test;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.ListCellRenderer;
import java.awt.Font;
import javax.swing.ListModel;
import javax.swing.JList;

public class FailureRunView implements TestRunView
{
    JList fFailureList;
    TestRunContext fRunContext;
    
    public FailureRunView(final TestRunContext context) {
        this.fRunContext = context;
        (this.fFailureList = new JList(this.fRunContext.getFailures())).setFont(new Font("Dialog", 0, 12));
        this.fFailureList.setSelectionMode(0);
        this.fFailureList.setCellRenderer(new FailureListCellRenderer());
        this.fFailureList.setVisibleRowCount(5);
        this.fFailureList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(final ListSelectionEvent e) {
                FailureRunView.this.testSelected();
            }
        });
    }
    
    public Test getSelectedTest() {
        final int index = this.fFailureList.getSelectedIndex();
        if (index == -1) {
            return null;
        }
        final ListModel model = this.fFailureList.getModel();
        final TestFailure failure = model.getElementAt(index);
        return failure.failedTest();
    }
    
    public void activate() {
        this.testSelected();
    }
    
    public void addTab(final JTabbedPane pane) {
        final JScrollPane scrollPane = new JScrollPane(this.fFailureList, 22, 32);
        final Icon errorIcon = TestRunner.getIconResource(this.getClass(), "icons/error.gif");
        pane.addTab("Failures", errorIcon, scrollPane, "The list of failed tests");
    }
    
    public void revealFailure(final Test failure) {
        this.fFailureList.setSelectedIndex(0);
    }
    
    public void aboutToStart(final Test suite, final TestResult result) {
    }
    
    public void runFinished(final Test suite, final TestResult result) {
    }
    
    protected void testSelected() {
        this.fRunContext.handleTestSelected(this.getSelectedTest());
    }
    
    static class FailureListCellRenderer extends DefaultListCellRenderer
    {
        private Icon fFailureIcon;
        private Icon fErrorIcon;
        
        FailureListCellRenderer() {
            this.loadIcons();
        }
        
        void loadIcons() {
            this.fFailureIcon = TestRunner.getIconResource(this.getClass(), "icons/failure.gif");
            this.fErrorIcon = TestRunner.getIconResource(this.getClass(), "icons/error.gif");
        }
        
        public Component getListCellRendererComponent(final JList list, final Object value, final int modelIndex, final boolean isSelected, final boolean cellHasFocus) {
            final Component c = super.getListCellRendererComponent(list, value, modelIndex, isSelected, cellHasFocus);
            final TestFailure failure = (TestFailure)value;
            String text = failure.failedTest().toString();
            final String msg = failure.exceptionMessage();
            if (msg != null) {
                text = text + ":" + BaseTestRunner.truncate(msg);
            }
            if (failure.isFailure()) {
                if (this.fFailureIcon != null) {
                    this.setIcon(this.fFailureIcon);
                }
            }
            else if (this.fErrorIcon != null) {
                this.setIcon(this.fErrorIcon);
            }
            this.setText(text);
            this.setToolTipText(text);
            return c;
        }
    }
}
