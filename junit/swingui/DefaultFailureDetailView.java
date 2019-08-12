// 
// Decompiled by Procyon v0.5.36
// 

package junit.swingui;

import javax.swing.DefaultListCellRenderer;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.AbstractListModel;
import junit.runner.BaseTestRunner;
import junit.framework.TestFailure;
import javax.swing.ListCellRenderer;
import java.awt.Font;
import javax.swing.ListModel;
import java.awt.Component;
import javax.swing.JList;
import junit.runner.FailureDetailView;

public class DefaultFailureDetailView implements FailureDetailView
{
    JList fList;
    
    public Component getComponent() {
        if (this.fList == null) {
            (this.fList = new JList(new StackTraceListModel())).setFont(new Font("Dialog", 0, 12));
            this.fList.setSelectionMode(0);
            this.fList.setVisibleRowCount(5);
            this.fList.setCellRenderer(new StackEntryRenderer());
        }
        return this.fList;
    }
    
    public void showFailure(final TestFailure failure) {
        this.getModel().setTrace(BaseTestRunner.getFilteredTrace(failure.trace()));
    }
    
    public void clear() {
        this.getModel().clear();
    }
    
    private StackTraceListModel getModel() {
        return (StackTraceListModel)this.fList.getModel();
    }
    
    static class StackTraceListModel extends AbstractListModel
    {
        private Vector fLines;
        
        StackTraceListModel() {
            this.fLines = new Vector(20);
        }
        
        public Object getElementAt(final int index) {
            return this.fLines.elementAt(index);
        }
        
        public int getSize() {
            return this.fLines.size();
        }
        
        public void setTrace(final String trace) {
            this.scan(trace);
            this.fireContentsChanged(this, 0, this.fLines.size());
        }
        
        public void clear() {
            this.fLines.removeAllElements();
            this.fireContentsChanged(this, 0, this.fLines.size());
        }
        
        private void scan(final String trace) {
            this.fLines.removeAllElements();
            final StringTokenizer st = new StringTokenizer(trace, "\n\r", false);
            while (st.hasMoreTokens()) {
                this.fLines.addElement(st.nextToken());
            }
        }
    }
    
    static class StackEntryRenderer extends DefaultListCellRenderer
    {
        public Component getListCellRendererComponent(final JList list, final Object value, final int modelIndex, final boolean isSelected, final boolean cellHasFocus) {
            final String text = ((String)value).replace('\t', ' ');
            final Component c = super.getListCellRendererComponent(list, text, modelIndex, isSelected, cellHasFocus);
            this.setText(text);
            this.setToolTipText(text);
            return c;
        }
    }
}
