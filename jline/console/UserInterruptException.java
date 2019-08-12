// 
// Decompiled by Procyon v0.5.36
// 

package jline.console;

public class UserInterruptException extends RuntimeException
{
    private final String partialLine;
    
    public UserInterruptException(final String partialLine) {
        this.partialLine = partialLine;
    }
    
    public String getPartialLine() {
        return this.partialLine;
    }
}
