// 
// Decompiled by Procyon v0.5.36
// 

package junit.swingui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import javax.swing.UIManager;
import javax.swing.Icon;
import javax.swing.DefaultListCellRenderer;
import java.util.Enumeration;
import junit.runner.Sorter;
import javax.swing.ListModel;
import java.awt.Toolkit;
import java.awt.Insets;
import java.awt.GridBagConstraints;
import java.awt.LayoutManager;
import java.awt.GridBagLayout;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Dimension;
import javax.swing.ListCellRenderer;
import java.util.Vector;
import java.awt.Cursor;
import java.awt.Component;
import junit.runner.TestCollector;
import java.awt.Frame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JButton;
import javax.swing.JDialog;

public class TestSelector extends JDialog
{
    private JButton fCancel;
    private JButton fOk;
    private JList fList;
    private JScrollPane fScrolledList;
    private JLabel fDescription;
    private String fSelectedItem;
    
    public TestSelector(final Frame parent, final TestCollector testCollector) {
        super(parent, true);
        this.setSize(350, 300);
        this.setResizable(false);
        try {
            this.setLocationRelativeTo(parent);
        }
        catch (NoSuchMethodError e) {
            centerWindow(this);
        }
        this.setTitle("Test Selector");
        Vector list = null;
        try {
            parent.setCursor(Cursor.getPredefinedCursor(3));
            list = this.createTestList(testCollector);
        }
        finally {
            parent.setCursor(Cursor.getDefaultCursor());
        }
        (this.fList = new JList(list)).setSelectionMode(0);
        this.fList.setCellRenderer(new TestCellRenderer());
        this.fScrolledList = new JScrollPane(this.fList);
        this.fCancel = new JButton("Cancel");
        this.fDescription = new JLabel("Select the Test class:");
        (this.fOk = new JButton("OK")).setEnabled(false);
        this.getRootPane().setDefaultButton(this.fOk);
        this.defineLayout();
        this.addListeners();
    }
    
    public static void centerWindow(final Component c) {
        final Dimension paneSize = c.getSize();
        final Dimension screenSize = c.getToolkit().getScreenSize();
        c.setLocation((screenSize.width - paneSize.width) / 2, (screenSize.height - paneSize.height) / 2);
    }
    
    private void addListeners() {
        this.fCancel.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                TestSelector.this.dispose();
            }
        });
        this.fOk.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                TestSelector.this.okSelected();
            }
        });
        this.fList.addMouseListener(new DoubleClickListener());
        this.fList.addKeyListener(new KeySelectListener());
        this.fList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(final ListSelectionEvent e) {
                TestSelector.this.checkEnableOK(e);
            }
        });
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(final WindowEvent e) {
                TestSelector.this.dispose();
            }
        });
    }
    
    private void defineLayout() {
        this.getContentPane().setLayout(new GridBagLayout());
        final GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = 0;
        labelConstraints.gridwidth = 1;
        labelConstraints.gridheight = 1;
        labelConstraints.fill = 1;
        labelConstraints.anchor = 17;
        labelConstraints.weightx = 1.0;
        labelConstraints.weighty = 0.0;
        labelConstraints.insets = new Insets(8, 8, 0, 8);
        this.getContentPane().add(this.fDescription, labelConstraints);
        final GridBagConstraints listConstraints = new GridBagConstraints();
        listConstraints.gridx = 0;
        listConstraints.gridy = 1;
        listConstraints.gridwidth = 4;
        listConstraints.gridheight = 1;
        listConstraints.fill = 1;
        listConstraints.anchor = 10;
        listConstraints.weightx = 1.0;
        listConstraints.weighty = 1.0;
        listConstraints.insets = new Insets(8, 8, 8, 8);
        this.getContentPane().add(this.fScrolledList, listConstraints);
        final GridBagConstraints okConstraints = new GridBagConstraints();
        okConstraints.gridx = 2;
        okConstraints.gridy = 2;
        okConstraints.gridwidth = 1;
        okConstraints.gridheight = 1;
        okConstraints.anchor = 13;
        okConstraints.insets = new Insets(0, 8, 8, 8);
        this.getContentPane().add(this.fOk, okConstraints);
        final GridBagConstraints cancelConstraints = new GridBagConstraints();
        cancelConstraints.gridx = 3;
        cancelConstraints.gridy = 2;
        cancelConstraints.gridwidth = 1;
        cancelConstraints.gridheight = 1;
        cancelConstraints.anchor = 13;
        cancelConstraints.insets = new Insets(0, 8, 8, 8);
        this.getContentPane().add(this.fCancel, cancelConstraints);
    }
    
    public void checkEnableOK(final ListSelectionEvent e) {
        this.fOk.setEnabled(this.fList.getSelectedIndex() != -1);
    }
    
    public void okSelected() {
        this.fSelectedItem = this.fList.getSelectedValue();
        this.dispose();
    }
    
    public boolean isEmpty() {
        return this.fList.getModel().getSize() == 0;
    }
    
    public void keySelectTestClass(final char ch) {
        final ListModel model = this.fList.getModel();
        if (!Character.isJavaIdentifierStart(ch)) {
            return;
        }
        for (int i = 0; i < model.getSize(); ++i) {
            final String s = model.getElementAt(i);
            if (TestCellRenderer.matchesKey(s, Character.toUpperCase(ch))) {
                this.fList.setSelectedIndex(i);
                this.fList.ensureIndexIsVisible(i);
                return;
            }
        }
        Toolkit.getDefaultToolkit().beep();
    }
    
    public String getSelectedItem() {
        return this.fSelectedItem;
    }
    
    private Vector createTestList(final TestCollector collector) {
        final Enumeration each = collector.collectTests();
        final Vector v = new Vector(200);
        final Vector displayVector = new Vector(v.size());
        while (each.hasMoreElements()) {
            final String s = each.nextElement();
            v.addElement(s);
            displayVector.addElement(TestCellRenderer.displayString(s));
        }
        if (v.size() > 0) {
            Sorter.sortStrings(displayVector, 0, displayVector.size() - 1, new ParallelSwapper(v));
        }
        return v;
    }
    
    static class TestCellRenderer extends DefaultListCellRenderer
    {
        Icon fLeafIcon;
        Icon fSuiteIcon;
        
        public TestCellRenderer() {
            this.fLeafIcon = UIManager.getIcon("Tree.leafIcon");
            this.fSuiteIcon = UIManager.getIcon("Tree.closedIcon");
        }
        
        public Component getListCellRendererComponent(final JList list, final Object value, final int modelIndex, final boolean isSelected, final boolean cellHasFocus) {
            final Component c = super.getListCellRendererComponent(list, value, modelIndex, isSelected, cellHasFocus);
            final String displayString = displayString((String)value);
            if (displayString.startsWith("AllTests")) {
                this.setIcon(this.fSuiteIcon);
            }
            else {
                this.setIcon(this.fLeafIcon);
            }
            this.setText(displayString);
            return c;
        }
        
        public static String displayString(final String className) {
            final int typeIndex = className.lastIndexOf(46);
            if (typeIndex < 0) {
                return className;
            }
            return className.substring(typeIndex + 1) + " - " + className.substring(0, typeIndex);
        }
        
        public static boolean matchesKey(final String s, final char ch) {
            return ch == Character.toUpperCase(s.charAt(typeIndex(s)));
        }
        
        private static int typeIndex(final String s) {
            final int typeIndex = s.lastIndexOf(46);
            int i = 0;
            if (typeIndex > 0) {
                i = typeIndex + 1;
            }
            return i;
        }
    }
    
    protected class DoubleClickListener extends MouseAdapter
    {
        public void mouseClicked(final MouseEvent e) {
            if (e.getClickCount() == 2) {
                TestSelector.this.okSelected();
            }
        }
    }
    
    protected class KeySelectListener extends KeyAdapter
    {
        public void keyTyped(final KeyEvent e) {
            TestSelector.this.keySelectTestClass(e.getKeyChar());
        }
    }
    
    private class ParallelSwapper implements Sorter.Swapper
    {
        Vector fOther;
        
        ParallelSwapper(final Vector other) {
            this.fOther = other;
        }
        
        public void swap(final Vector values, final int left, final int right) {
            final Object tmp = values.elementAt(left);
            values.setElementAt(values.elementAt(right), left);
            values.setElementAt(tmp, right);
            final Object tmp2 = this.fOther.elementAt(left);
            this.fOther.setElementAt(this.fOther.elementAt(right), left);
            this.fOther.setElementAt(tmp2, right);
        }
    }
}
