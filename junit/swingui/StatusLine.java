// 
// Decompiled by Procyon v0.5.36
// 

package junit.swingui;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import java.awt.Font;
import javax.swing.JTextField;

public class StatusLine extends JTextField
{
    public static final Font PLAIN_FONT;
    public static final Font BOLD_FONT;
    
    public StatusLine(final int preferredWidth) {
        this.setFont(StatusLine.BOLD_FONT);
        this.setEditable(false);
        this.setBorder(BorderFactory.createBevelBorder(1));
        final Dimension d = this.getPreferredSize();
        d.width = preferredWidth;
        this.setPreferredSize(d);
    }
    
    public void showInfo(final String message) {
        this.setFont(StatusLine.PLAIN_FONT);
        this.setForeground(Color.black);
        this.setText(message);
    }
    
    public void showError(final String status) {
        this.setFont(StatusLine.BOLD_FONT);
        this.setForeground(Color.red);
        this.setText(status);
        this.setToolTipText(status);
    }
    
    public void clear() {
        this.setText("");
        this.setToolTipText(null);
    }
    
    static {
        PLAIN_FONT = new Font("dialog", 0, 12);
        BOLD_FONT = new Font("dialog", 1, 12);
    }
}
