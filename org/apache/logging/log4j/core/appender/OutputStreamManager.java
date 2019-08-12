// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.appender;

import java.io.IOException;
import org.apache.logging.log4j.core.Layout;
import java.io.OutputStream;

public class OutputStreamManager extends AbstractManager
{
    private volatile OutputStream os;
    protected final Layout<?> layout;
    
    protected OutputStreamManager(final OutputStream os, final String streamName, final Layout<?> layout, final boolean writeHeader) {
        super(streamName);
        this.os = os;
        this.layout = layout;
        if (writeHeader && layout != null) {
            final byte[] header = layout.getHeader();
            if (header != null) {
                try {
                    this.os.write(header, 0, header.length);
                }
                catch (IOException e) {
                    this.logError("unable to write header", e);
                }
            }
        }
    }
    
    public static <T> OutputStreamManager getManager(final String name, final T data, final ManagerFactory<? extends OutputStreamManager, T> factory) {
        return AbstractManager.getManager(name, factory, data);
    }
    
    public void releaseSub() {
        this.writeFooter();
        this.close();
    }
    
    protected void writeFooter() {
        if (this.layout == null) {
            return;
        }
        final byte[] footer = this.layout.getFooter();
        if (footer != null) {
            this.write(footer);
        }
    }
    
    public boolean isOpen() {
        return this.getCount() > 0;
    }
    
    protected OutputStream getOutputStream() {
        return this.os;
    }
    
    protected void setOutputStream(final OutputStream os) {
        final byte[] header = this.layout.getHeader();
        if (header != null) {
            try {
                os.write(header, 0, header.length);
                this.os = os;
            }
            catch (IOException ioe) {
                this.logError("unable to write header", ioe);
            }
        }
        else {
            this.os = os;
        }
    }
    
    protected synchronized void write(final byte[] bytes, final int offset, final int length) {
        try {
            this.os.write(bytes, offset, length);
        }
        catch (IOException ex) {
            final String msg = "Error writing to stream " + this.getName();
            throw new AppenderLoggingException(msg, ex);
        }
    }
    
    protected void write(final byte[] bytes) {
        this.write(bytes, 0, bytes.length);
    }
    
    protected synchronized void close() {
        final OutputStream stream = this.os;
        if (stream == System.out || stream == System.err) {
            return;
        }
        try {
            stream.close();
        }
        catch (IOException ex) {
            this.logError("unable to close stream", ex);
        }
    }
    
    public synchronized void flush() {
        try {
            this.os.flush();
        }
        catch (IOException ex) {
            final String msg = "Error flushing stream " + this.getName();
            throw new AppenderLoggingException(msg, ex);
        }
    }
}
