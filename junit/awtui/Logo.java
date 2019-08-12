// 
// Decompiled by Procyon v0.5.36
// 

package junit.awtui;

import junit.runner.BaseTestRunner;
import java.awt.Color;
import java.awt.SystemColor;
import java.awt.Graphics;
import java.net.URL;
import java.awt.image.ImageProducer;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;
import java.awt.Component;
import java.awt.MediaTracker;
import java.awt.Image;
import java.awt.Canvas;

public class Logo extends Canvas
{
    private Image fImage;
    private int fWidth;
    private int fHeight;
    
    public Logo() {
        this.fImage = this.loadImage("logo.gif");
        final MediaTracker tracker = new MediaTracker(this);
        tracker.addImage(this.fImage, 0);
        try {
            tracker.waitForAll();
        }
        catch (Exception ex) {}
        if (this.fImage != null) {
            this.fWidth = this.fImage.getWidth(this);
            this.fHeight = this.fImage.getHeight(this);
        }
        else {
            this.fWidth = 20;
            this.fHeight = 20;
        }
        this.setSize(this.fWidth, this.fHeight);
    }
    
    public Image loadImage(final String name) {
        final Toolkit toolkit = Toolkit.getDefaultToolkit();
        try {
            final URL url = BaseTestRunner.class.getResource(name);
            return toolkit.createImage((ImageProducer)url.getContent());
        }
        catch (Exception ex) {
            return null;
        }
    }
    
    public void paint(final Graphics g) {
        this.paintBackground(g);
        if (this.fImage != null) {
            g.drawImage(this.fImage, 0, 0, this.fWidth, this.fHeight, this);
        }
    }
    
    public void paintBackground(final Graphics g) {
        g.setColor(SystemColor.control);
        g.fillRect(0, 0, this.getBounds().width, this.getBounds().height);
    }
}
