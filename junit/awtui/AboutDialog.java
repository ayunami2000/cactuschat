// 
// Decompiled by Procyon v0.5.36
// 

package junit.awtui;

import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.Insets;
import java.awt.Component;
import java.awt.GridBagConstraints;
import junit.runner.Version;
import java.awt.Font;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Button;
import java.awt.LayoutManager;
import java.awt.GridBagLayout;
import java.awt.Frame;
import java.awt.Dialog;

class AboutDialog extends Dialog
{
    public AboutDialog(final Frame parent) {
        super(parent);
        this.setResizable(false);
        this.setLayout(new GridBagLayout());
        this.setSize(330, 138);
        this.setTitle("About");
        final Button button = new Button("Close");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                AboutDialog.this.dispose();
            }
        });
        final Label label1 = new Label("JUnit");
        label1.setFont(new Font("dialog", 0, 36));
        final Label label2 = new Label("JUnit " + Version.id() + " by Kent Beck and Erich Gamma");
        label2.setFont(new Font("dialog", 0, 14));
        final Logo logo = new Logo();
        final GridBagConstraints constraintsLabel1 = new GridBagConstraints();
        constraintsLabel1.gridx = 3;
        constraintsLabel1.gridy = 0;
        constraintsLabel1.gridwidth = 1;
        constraintsLabel1.gridheight = 1;
        constraintsLabel1.anchor = 10;
        this.add(label1, constraintsLabel1);
        final GridBagConstraints constraintsLabel2 = new GridBagConstraints();
        constraintsLabel2.gridx = 2;
        constraintsLabel2.gridy = 1;
        constraintsLabel2.gridwidth = 2;
        constraintsLabel2.gridheight = 1;
        constraintsLabel2.anchor = 10;
        this.add(label2, constraintsLabel2);
        final GridBagConstraints constraintsButton1 = new GridBagConstraints();
        constraintsButton1.gridx = 2;
        constraintsButton1.gridy = 2;
        constraintsButton1.gridwidth = 2;
        constraintsButton1.gridheight = 1;
        constraintsButton1.anchor = 10;
        constraintsButton1.insets = new Insets(8, 0, 8, 0);
        this.add(button, constraintsButton1);
        final GridBagConstraints constraintsLogo1 = new GridBagConstraints();
        constraintsLogo1.gridx = 2;
        constraintsLogo1.gridy = 0;
        constraintsLogo1.gridwidth = 1;
        constraintsLogo1.gridheight = 1;
        constraintsLogo1.anchor = 10;
        this.add(logo, constraintsLogo1);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(final WindowEvent e) {
                AboutDialog.this.dispose();
            }
        });
    }
}
