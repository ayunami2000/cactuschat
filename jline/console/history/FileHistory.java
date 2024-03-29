// 
// Decompiled by Procyon v0.5.36
// 

package jline.console.history;

import java.util.Iterator;
import java.io.PrintStream;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.Reader;
import java.io.FileReader;
import jline.internal.Log;
import java.io.IOException;
import jline.internal.Preconditions;
import java.io.File;
import java.io.Flushable;

public class FileHistory extends MemoryHistory implements PersistentHistory, Flushable
{
    private final File file;
    
    public FileHistory(final File file) throws IOException {
        this.file = Preconditions.checkNotNull(file);
        this.load(file);
    }
    
    public File getFile() {
        return this.file;
    }
    
    public void load(final File file) throws IOException {
        Preconditions.checkNotNull(file);
        if (file.exists()) {
            Log.trace("Loading history from: ", file);
            this.load(new FileReader(file));
        }
    }
    
    public void load(final InputStream input) throws IOException {
        Preconditions.checkNotNull(input);
        this.load(new InputStreamReader(input));
    }
    
    public void load(final Reader reader) throws IOException {
        Preconditions.checkNotNull(reader);
        final BufferedReader input = new BufferedReader(reader);
        String item;
        while ((item = input.readLine()) != null) {
            this.internalAdd(item);
        }
    }
    
    public void flush() throws IOException {
        Log.trace("Flushing history");
        if (!this.file.exists()) {
            final File dir = this.file.getParentFile();
            if (!dir.exists() && !dir.mkdirs()) {
                Log.warn("Failed to create directory: ", dir);
            }
            if (!this.file.createNewFile()) {
                Log.warn("Failed to create file: ", this.file);
            }
        }
        final PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream(this.file)));
        try {
            for (final History.Entry entry : this) {
                out.println(entry.value());
            }
        }
        finally {
            out.close();
        }
    }
    
    public void purge() throws IOException {
        Log.trace("Purging history");
        this.clear();
        if (!this.file.delete()) {
            Log.warn("Failed to delete history file: ", this.file);
        }
    }
}
