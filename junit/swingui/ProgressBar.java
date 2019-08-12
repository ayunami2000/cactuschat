// 
// Decompiled by Procyon v0.5.36
// 

package junit.swingui;

import java.awt.Color;
import javax.swing.JProgressBar;

class ProgressBar extends JProgressBar
{
    boolean fError;
    
    public ProgressBar() {
        this.fError = false;
        this.setForeground(this.getStatusColor());
    }
    
    protected Color getStatusColor() {
        if (this.fError) {
            return Color.red;
        }
        return Color.green;
    }
    
    public void reset() {
        this.fError = false;
        this.updateBarColor();
        this.setValue(0);
    }
    
    public void start(final int total) {
        this.setMaximum(total);
        this.reset();
    }
    
    public void step(final int value, final boolean successful) {
        this.setValue(value);
        if (!this.fError && !successful) {
            this.fError = true;
            this.updateBarColor();
        }
    }
    
    protected void updateBarColor() {
        this.setForeground(this.getStatusColor());
    }
}
