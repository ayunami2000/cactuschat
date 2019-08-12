// 
// Decompiled by Procyon v0.5.36
// 

package junit.swingui;

import javax.swing.JTextField;

public class MacProgressBar extends ProgressBar
{
    private JTextField component;
    
    public MacProgressBar(final JTextField component) {
        this.component = component;
    }
    
    protected void updateBarColor() {
        this.component.setBackground(this.getStatusColor());
    }
}
