// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.appender;

import java.io.FileNotFoundException;
import java.io.BufferedOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.nio.channels.FileLock;
import java.nio.channels.FileChannel;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.Serializable;
import org.apache.logging.log4j.core.Layout;
import java.io.OutputStream;

public class FileManager extends OutputStreamManager
{
    private static final FileManagerFactory FACTORY;
    private final boolean isAppend;
    private final boolean isLocking;
    private final String advertiseURI;
    private final int bufferSize;
    
    protected FileManager(final String fileName, final OutputStream os, final boolean append, final boolean locking, final String advertiseURI, final Layout<? extends Serializable> layout, final int bufferSize, final boolean writeHeader) {
        super(os, fileName, layout, writeHeader);
        this.isAppend = append;
        this.isLocking = locking;
        this.advertiseURI = advertiseURI;
        this.bufferSize = bufferSize;
    }
    
    public static FileManager getFileManager(final String fileName, final boolean append, boolean locking, final boolean bufferedIo, final String advertiseUri, final Layout<? extends Serializable> layout, final int bufferSize) {
        if (locking && bufferedIo) {
            locking = false;
        }
        return (FileManager)OutputStreamManager.getManager(fileName, new FactoryData(append, locking, bufferedIo, bufferSize, advertiseUri, layout), FileManager.FACTORY);
    }
    
    @Override
    protected synchronized void write(final byte[] bytes, final int offset, final int length) {
        if (this.isLocking) {
            final FileChannel channel = ((FileOutputStream)this.getOutputStream()).getChannel();
            try {
                final FileLock lock = channel.lock(0L, Long.MAX_VALUE, false);
                try {
                    super.write(bytes, offset, length);
                }
                finally {
                    lock.release();
                }
            }
            catch (IOException ex) {
                throw new AppenderLoggingException("Unable to obtain lock on " + this.getName(), ex);
            }
        }
        else {
            super.write(bytes, offset, length);
        }
    }
    
    public String getFileName() {
        return this.getName();
    }
    
    public boolean isAppend() {
        return this.isAppend;
    }
    
    public boolean isLocking() {
        return this.isLocking;
    }
    
    public int getBufferSize() {
        return this.bufferSize;
    }
    
    @Override
    public Map<String, String> getContentFormat() {
        final Map<String, String> result = new HashMap<String, String>(super.getContentFormat());
        result.put("fileURI", this.advertiseURI);
        return result;
    }
    
    static {
        FACTORY = new FileManagerFactory();
    }
    
    private static class FactoryData
    {
        private final boolean append;
        private final boolean locking;
        private final boolean bufferedIO;
        private final int bufferSize;
        private final String advertiseURI;
        private final Layout<? extends Serializable> layout;
        
        public FactoryData(final boolean append, final boolean locking, final boolean bufferedIO, final int bufferSize, final String advertiseURI, final Layout<? extends Serializable> layout) {
            this.append = append;
            this.locking = locking;
            this.bufferedIO = bufferedIO;
            this.bufferSize = bufferSize;
            this.advertiseURI = advertiseURI;
            this.layout = layout;
        }
    }
    
    private static class FileManagerFactory implements ManagerFactory<FileManager, FactoryData>
    {
        @Override
        public FileManager createManager(final String name, final FactoryData data) {
            final File file = new File(name);
            final File parent = file.getParentFile();
            if (null != parent && !parent.exists()) {
                parent.mkdirs();
            }
            final boolean writeHeader = !data.append || !file.exists();
            try {
                OutputStream os = new FileOutputStream(name, data.append);
                int bufferSize = data.bufferSize;
                if (data.bufferedIO) {
                    os = new BufferedOutputStream(os, bufferSize);
                }
                else {
                    bufferSize = -1;
                }
                return new FileManager(name, os, data.append, data.locking, data.advertiseURI, data.layout, bufferSize, writeHeader);
            }
            catch (FileNotFoundException ex) {
                AbstractManager.LOGGER.error("FileManager (" + name + ") " + ex, ex);
                return null;
            }
        }
    }
}
