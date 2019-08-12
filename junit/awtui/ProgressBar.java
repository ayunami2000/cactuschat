// 
// Decompiled by Procyon v0.5.36
// 

package junit.awtui;

import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Canvas;

public class ProgressBar extends Canvas
{
    public boolean fError;
    public int fTotal;
    public int fProgress;
    public int fProgressX;
    
    public ProgressBar() {
        this.fError = false;
        this.fTotal = 0;
        this.fProgress = 0;
        this.fProgressX = 0;
        this.setSize(20, 30);
    }
    
    private Color getStatusColor() {
        if (this.fError) {
            return Color.red;
        }
        return Color.green;
    }
    
    public void paint(final Graphics g) {
        this.paintBackground(g);
        this.paintStatus(g);
    }
    
    public void paintBackground(final Graphics g) {
        g.setColor(SystemColor.control);
        final Rectangle r = this.getBounds();
        g.fillRect(0, 0, r.width, r.height);
        g.setColor(Color.darkGray);
        g.drawLine(0, 0, r.width - 1, 0);
        g.drawLine(0, 0, 0, r.height - 1);
        g.setColor(Color.white);
        g.drawLine(r.width - 1, 0, r.width - 1, r.height - 1);
        g.drawLine(0, r.height - 1, r.width - 1, r.height - 1);
    }
    
    public void paintStatus(final Graphics g) {
        g.setColor(this.getStatusColor());
        final Rectangle r = new Rectangle(0, 0, this.fProgressX, this.getBounds().height);
        g.fillRect(1, 1, r.width - 1, r.height - 2);
    }
    
    private void paintStep(final int startX, final int endX) {
        this.repaint(startX, 1, endX - startX, this.getBounds().height - 2);
    }
    
    public void reset() {
        this.fProgressX = 1;
        this.fProgress = 0;
        this.fError = false;
        this.paint(this.getGraphics());
    }
    
    public int scale(final int value) {
        if (this.fTotal > 0) {
            return Math.max(1, value * (this.getBounds().width - 1) / this.fTotal);
        }
        return value;
    }
    
    public void setBounds(final int x, final int y, final int w, final int h) {
        super.setBounds(x, y, w, h);
        this.fProgressX = this.scale(this.fProgress);
    }
    
    public void start(final int total) {
        this.fTotal = total;
        this.reset();
    }
    
    public void step(final boolean successful) {
        ++this.fProgress;
        int x = this.fProgressX;
        this.fProgressX = this.scale(this.fProgress);
        if (!this.fError && !successful) {
            this.fError = true;
            x = 1;
        }
        this.paintStep(x, this.fProgressX);
    }
}
