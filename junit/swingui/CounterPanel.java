// 
// Decompiled by Procyon v0.5.36
// 

package junit.swingui;

import java.awt.GridBagConstraints;
import javax.swing.BorderFactory;
import java.awt.Component;
import java.awt.Insets;
import javax.swing.JLabel;
import java.awt.LayoutManager;
import java.awt.GridBagLayout;
import javax.swing.Icon;
import javax.swing.JTextField;
import javax.swing.JPanel;

public class CounterPanel extends JPanel
{
    private JTextField fNumberOfErrors;
    private JTextField fNumberOfFailures;
    private JTextField fNumberOfRuns;
    private Icon fFailureIcon;
    private Icon fErrorIcon;
    private int fTotal;
    
    public CounterPanel() {
        super(new GridBagLayout());
        this.fFailureIcon = TestRunner.getIconResource(this.getClass(), "icons/failure.gif");
        this.fErrorIcon = TestRunner.getIconResource(this.getClass(), "icons/error.gif");
        this.fNumberOfErrors = this.createOutputField(5);
        this.fNumberOfFailures = this.createOutputField(5);
        this.fNumberOfRuns = this.createOutputField(9);
        this.addToGrid(new JLabel("Runs:", 0), 0, 0, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 0, 0));
        this.addToGrid(this.fNumberOfRuns, 1, 0, 1, 1, 0.33, 0.0, 10, 2, new Insets(0, 8, 0, 0));
        this.addToGrid(new JLabel("Errors:", this.fErrorIcon, 2), 2, 0, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 8, 0, 0));
        this.addToGrid(this.fNumberOfErrors, 3, 0, 1, 1, 0.33, 0.0, 10, 2, new Insets(0, 8, 0, 0));
        this.addToGrid(new JLabel("Failures:", this.fFailureIcon, 2), 4, 0, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 8, 0, 0));
        this.addToGrid(this.fNumberOfFailures, 5, 0, 1, 1, 0.33, 0.0, 10, 2, new Insets(0, 8, 0, 0));
    }
    
    private JTextField createOutputField(final int width) {
        final JTextField field = new JTextField("0", width);
        field.setMinimumSize(field.getPreferredSize());
        field.setMaximumSize(field.getPreferredSize());
        field.setHorizontalAlignment(2);
        field.setFont(StatusLine.BOLD_FONT);
        field.setEditable(false);
        field.setBorder(BorderFactory.createEmptyBorder());
        return field;
    }
    
    public void addToGrid(final Component comp, final int gridx, final int gridy, final int gridwidth, final int gridheight, final double weightx, final double weighty, final int anchor, final int fill, final Insets insets) {
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = gridx;
        constraints.gridy = gridy;
        constraints.gridwidth = gridwidth;
        constraints.gridheight = gridheight;
        constraints.weightx = weightx;
        constraints.weighty = weighty;
        constraints.anchor = anchor;
        constraints.fill = fill;
        constraints.insets = insets;
        this.add(comp, constraints);
    }
    
    public void reset() {
        this.setLabelValue(this.fNumberOfErrors, 0);
        this.setLabelValue(this.fNumberOfFailures, 0);
        this.setLabelValue(this.fNumberOfRuns, 0);
        this.fTotal = 0;
    }
    
    public void setTotal(final int value) {
        this.fTotal = value;
    }
    
    public void setRunValue(final int value) {
        this.fNumberOfRuns.setText(Integer.toString(value) + "/" + this.fTotal);
    }
    
    public void setErrorValue(final int value) {
        this.setLabelValue(this.fNumberOfErrors, value);
    }
    
    public void setFailureValue(final int value) {
        this.setLabelValue(this.fNumberOfFailures, value);
    }
    
    private void setLabelValue(final JTextField label, final int value) {
        label.setText(Integer.toString(value));
    }
}
