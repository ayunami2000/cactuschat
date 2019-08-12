// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.appender;

import org.apache.logging.log4j.core.util.NullOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.io.Serializable;
import org.apache.logging.log4j.core.Layout;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.io.RandomAccessFile;

public class RandomAccessFileManager extends OutputStreamManager
{
    static final int DEFAULT_BUFFER_SIZE = 262144;
    private static final RandomAccessFileManagerFactory FACTORY;
    private final boolean isImmediateFlush;
    private final String advertiseURI;
    private final RandomAccessFile randomAccessFile;
    private final ByteBuffer buffer;
    private final ThreadLocal<Boolean> isEndOfBatch;
    
    protected RandomAccessFileManager(final RandomAccessFile file, final String fileName, final OutputStream os, final boolean immediateFlush, final int bufferSize, final String advertiseURI, final Layout<? extends Serializable> layout, final boolean writeHeader) {
        super(os, fileName, layout, writeHeader);
        this.isEndOfBatch = new ThreadLocal<Boolean>();
        this.isImmediateFlush = immediateFlush;
        this.randomAccessFile = file;
        this.advertiseURI = advertiseURI;
        this.isEndOfBatch.set(Boolean.FALSE);
        this.buffer = ByteBuffer.allocate(bufferSize);
    }
    
    public static RandomAccessFileManager getFileManager(final String fileName, final boolean append, final boolean isFlush, final int bufferSize, final String advertiseURI, final Layout<? extends Serializable> layout) {
        return (RandomAccessFileManager)OutputStreamManager.getManager(fileName, new FactoryData(append, isFlush, bufferSize, advertiseURI, layout), RandomAccessFileManager.FACTORY);
    }
    
    public Boolean isEndOfBatch() {
        return this.isEndOfBatch.get();
    }
    
    public void setEndOfBatch(final boolean endOfBatch) {
        this.isEndOfBatch.set(endOfBatch);
    }
    
    @Override
    protected synchronized void write(final byte[] bytes, int offset, int length) {
        super.write(bytes, offset, length);
        int chunk = 0;
        do {
            if (length > this.buffer.remaining()) {
                this.flush();
            }
            chunk = Math.min(length, this.buffer.remaining());
            this.buffer.put(bytes, offset, chunk);
            offset += chunk;
            length -= chunk;
        } while (length > 0);
        if (this.isImmediateFlush || this.isEndOfBatch.get() == Boolean.TRUE) {
            this.flush();
        }
    }
    
    @Override
    public synchronized void flush() {
        this.buffer.flip();
        try {
            this.randomAccessFile.write(this.buffer.array(), 0, this.buffer.limit());
        }
        catch (IOException ex) {
            final String msg = "Error writing to RandomAccessFile " + this.getName();
            throw new AppenderLoggingException(msg, ex);
        }
        this.buffer.clear();
    }
    
    public synchronized void close() {
        this.flush();
        try {
            this.randomAccessFile.close();
        }
        catch (IOException ex) {
            this.logError("unable to close RandomAccessFile", ex);
        }
    }
    
    public String getFileName() {
        return this.getName();
    }
    
    public int getBufferSize() {
        return this.buffer.capacity();
    }
    
    @Override
    public Map<String, String> getContentFormat() {
        final Map<String, String> result = new HashMap<String, String>(super.getContentFormat());
        result.put("fileURI", this.advertiseURI);
        return result;
    }
    
    static {
        FACTORY = new RandomAccessFileManagerFactory();
    }
    
    private static class FactoryData
    {
        private final boolean append;
        private final boolean immediateFlush;
        private final int bufferSize;
        private final String advertiseURI;
        private final Layout<? extends Serializable> layout;
        
        public FactoryData(final boolean append, final boolean immediateFlush, final int bufferSize, final String advertiseURI, final Layout<? extends Serializable> layout) {
            this.append = append;
            this.immediateFlush = immediateFlush;
            this.bufferSize = bufferSize;
            this.advertiseURI = advertiseURI;
            this.layout = layout;
        }
    }
    
    private static class RandomAccessFileManagerFactory implements ManagerFactory<RandomAccessFileManager, FactoryData>
    {
        @Override
        public RandomAccessFileManager createManager(final String name, final FactoryData data) {
            final File file = new File(name);
            final File parent = file.getParentFile();
            if (null != parent && !parent.exists()) {
                parent.mkdirs();
            }
            if (!data.append) {
                file.delete();
            }
            final boolean writeHeader = !data.append || !file.exists();
            final OutputStream os = NullOutputStream.NULL_OUTPUT_STREAM;
            try {
                final RandomAccessFile raf = new RandomAccessFile(name, "rw");
                if (data.append) {
                    raf.seek(raf.length());
                }
                else {
                    raf.setLength(0L);
                }
                return new RandomAccessFileManager(raf, name, os, data.immediateFlush, data.bufferSize, data.advertiseURI, data.layout, writeHeader);
            }
            catch (Exception ex) {
                AbstractManager.LOGGER.error("RandomAccessFileManager (" + name + ") " + ex, ex);
                return null;
            }
        }
    }
}
