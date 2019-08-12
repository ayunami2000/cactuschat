// 
// Decompiled by Procyon v0.5.36
// 

package jline;

public class UnsupportedTerminal extends TerminalSupport
{
    public UnsupportedTerminal() {
        super(false);
        this.setAnsiSupported(false);
        this.setEchoEnabled(true);
    }
}
