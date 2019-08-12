// 
// Decompiled by Procyon v0.5.36
// 

package jline;

public class NoInterruptUnixTerminal extends UnixTerminal
{
    public NoInterruptUnixTerminal() throws Exception {
    }
    
    @Override
    public void init() throws Exception {
        super.init();
        this.getSettings().set("intr undef");
    }
    
    @Override
    public void restore() throws Exception {
        this.getSettings().set("intr ^C");
        super.restore();
    }
}
