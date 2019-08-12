// 
// Decompiled by Procyon v0.5.36
// 

package jline;

import jline.internal.Log;
import jline.internal.TerminalLineSettings;

public class UnixTerminal extends TerminalSupport
{
    private final TerminalLineSettings settings;
    
    public UnixTerminal() throws Exception {
        super(true);
        this.settings = new TerminalLineSettings();
    }
    
    protected TerminalLineSettings getSettings() {
        return this.settings;
    }
    
    @Override
    public void init() throws Exception {
        super.init();
        this.setAnsiSupported(true);
        this.settings.set("-icanon min 1 -icrnl -inlcr -ixon");
        this.settings.set("dsusp undef");
        this.setEchoEnabled(false);
    }
    
    @Override
    public void restore() throws Exception {
        this.settings.restore();
        super.restore();
    }
    
    @Override
    public int getWidth() {
        final int w = this.settings.getProperty("columns");
        return (w < 1) ? 80 : w;
    }
    
    @Override
    public int getHeight() {
        final int h = this.settings.getProperty("rows");
        return (h < 1) ? 24 : h;
    }
    
    @Override
    public synchronized void setEchoEnabled(final boolean enabled) {
        try {
            if (enabled) {
                this.settings.set("echo");
            }
            else {
                this.settings.set("-echo");
            }
            super.setEchoEnabled(enabled);
        }
        catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            Log.error("Failed to ", enabled ? "enable" : "disable", " echo", e);
        }
    }
    
    public void disableInterruptCharacter() {
        try {
            this.settings.set("intr undef");
        }
        catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            Log.error("Failed to disable interrupt character", e);
        }
    }
    
    public void enableInterruptCharacter() {
        try {
            this.settings.set("intr ^C");
        }
        catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            Log.error("Failed to enable interrupt character", e);
        }
    }
}
