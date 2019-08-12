// 
// Decompiled by Procyon v0.5.36
// 

package jline;

import org.fusesource.jansi.AnsiConsole;
import java.io.ByteArrayOutputStream;
import org.fusesource.jansi.AnsiOutputStream;
import org.fusesource.jansi.WindowsAnsiOutputStream;
import jline.internal.Configuration;
import java.io.OutputStream;

public class AnsiWindowsTerminal extends WindowsTerminal
{
    private final boolean ansiSupported;
    
    @Override
    public OutputStream wrapOutIfNeeded(final OutputStream out) {
        return wrapOutputStream(out);
    }
    
    private static OutputStream wrapOutputStream(final OutputStream stream) {
        if (Configuration.isWindows()) {
            try {
                return new WindowsAnsiOutputStream(stream);
            }
            catch (Throwable ignore) {
                return new AnsiOutputStream(stream);
            }
        }
        return stream;
    }
    
    private static boolean detectAnsiSupport() {
        final OutputStream out = AnsiConsole.wrapOutputStream(new ByteArrayOutputStream());
        try {
            out.close();
        }
        catch (Exception ex) {}
        return out instanceof WindowsAnsiOutputStream;
    }
    
    public AnsiWindowsTerminal() throws Exception {
        this.ansiSupported = detectAnsiSupport();
    }
    
    @Override
    public boolean isAnsiSupported() {
        return this.ansiSupported;
    }
    
    @Override
    public boolean hasWeirdWrap() {
        return false;
    }
}
