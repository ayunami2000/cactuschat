// 
// Decompiled by Procyon v0.5.36
// 

package jline.console.history;

import java.io.IOException;

public interface PersistentHistory extends History
{
    void flush() throws IOException;
    
    void purge() throws IOException;
}
