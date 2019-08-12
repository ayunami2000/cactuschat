// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.appender.rolling;

import org.apache.logging.log4j.core.util.NullOutputStream;
import java.io.File;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.appender.ManagerFactory;
import org.apache.logging.log4j.core.appender.OutputStreamManager;
import java.io.IOException;
import java.io.Serializable;
import org.apache.logging.log4j.core.Layout;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.io.RandomAccessFile;

public class RollingRandomAccessFileManager extends RollingFileManager
{
    public static final int DEFAULT_BUFFER_SIZE = 262144;
    private static final RollingRandomAccessFileManagerFactory FACTORY;
    private final boolean isImmediateFlush;
    private RandomAccessFile randomAccessFile;
    private final ByteBuffer buffer;
    private final ThreadLocal<Boolean> isEndOfBatch;
    
    public RollingRandomAccessFileManager(final RandomAccessFile raf, final String fileName, final String pattern, final OutputStream os, final boolean append, final boolean immediateFlush, final int bufferSize, final long size, final long time, final TriggeringPolicy policy, final RolloverStrategy strategy, final String advertiseURI, final Layout<? extends Serializable> layout, final boolean writeHeader) {
        super(fileName, pattern, os, append, size, time, policy, strategy, advertiseURI, layout, bufferSize, writeHeader);
        this.isEndOfBatch = new ThreadLocal<Boolean>();
        this.isImmediateFlush = immediateFlush;
        this.randomAccessFile = raf;
        this.isEndOfBatch.set(Boolean.FALSE);
        this.buffer = ByteBuffer.allocate(bufferSize);
        this.writeHeader();
    }
    
    private void writeHeader() {
        if (this.layout == null) {
            return;
        }
        final byte[] header = this.layout.getHeader();
        if (header == null) {
            return;
        }
        try {
            this.randomAccessFile.write(header, 0, header.length);
        }
        catch (IOException e) {
            this.logError("unable to write header", e);
        }
    }
    
    public static RollingRandomAccessFileManager getRollingRandomAccessFileManager(final String fileName, final String filePattern, final boolean isAppend, final boolean immediateFlush, final int bufferSize, final TriggeringPolicy policy, final RolloverStrategy strategy, final String advertiseURI, final Layout<? extends Serializable> layout) {
        return (RollingRandomAccessFileManager)OutputStreamManager.getManager(fileName, new FactoryData(filePattern, isAppend, immediateFlush, bufferSize, policy, strategy, advertiseURI, layout), RollingRandomAccessFileManager.FACTORY);
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
    protected void createFileAfterRollover() throws IOException {
        this.randomAccessFile = new RandomAccessFile(this.getFileName(), "rw");
        if (this.isAppend()) {
            this.randomAccessFile.seek(this.randomAccessFile.length());
        }
        this.writeHeader();
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
        catch (IOException e) {
            this.logError("unable to close RandomAccessFile", e);
        }
    }
    
    @Override
    public int getBufferSize() {
        return this.buffer.capacity();
    }
    
    @Override
    public void updateData(final Object data) {
        final FactoryData factoryData = (FactoryData)data;
        this.setRolloverStrategy(factoryData.getRolloverStrategy());
        this.setTriggeringPolicy(factoryData.getTriggeringPolicy());
    }
    
    static {
        FACTORY = new RollingRandomAccessFileManagerFactory();
    }
    
    private static class RollingRandomAccessFileManagerFactory implements ManagerFactory<RollingRandomAccessFileManager, FactoryData>
    {
        @Override
        public RollingRandomAccessFileManager createManager(final String name, final FactoryData data) {
            final File file = new File(name);
            final File parent = file.getParentFile();
            if (null != parent && !parent.exists()) {
                parent.mkdirs();
            }
            if (!data.append) {
                file.delete();
            }
            final long size = data.append ? file.length() : 0L;
            final long time = file.exists() ? file.lastModified() : System.currentTimeMillis();
            final boolean writeHeader = !data.append || !file.exists();
            RandomAccessFile raf = null;
            try {
                raf = new RandomAccessFile(name, "rw");
                if (data.append) {
                    final long length = raf.length();
                    RollingRandomAccessFileManager.LOGGER.trace("RandomAccessFile {} seek to {}", new Object[] { name, length });
                    raf.seek(length);
                }
                else {
                    RollingRandomAccessFileManager.LOGGER.trace("RandomAccessFile {} set length to 0", new Object[] { name });
                    raf.setLength(0L);
                }
                return new RollingRandomAccessFileManager(raf, name, data.pattern, NullOutputStream.NULL_OUTPUT_STREAM, data.append, data.immediateFlush, data.bufferSize, size, time, data.policy, data.strategy, data.advertiseURI, data.layout, writeHeader);
            }
            catch (IOException ex) {
                RollingRandomAccessFileManager.LOGGER.error("Cannot access RandomAccessFile {}) " + ex);
                if (raf != null) {
                    try {
                        raf.close();
                    }
                    catch (IOException e) {
                        RollingRandomAccessFileManager.LOGGER.error("Cannot close RandomAccessFile {}", new Object[] { name, e });
                    }
                }
                return null;
            }
        }
    }
    
    private static class FactoryData
    {
        private final String pattern;
        private final boolean append;
        private final boolean immediateFlush;
        private final int bufferSize;
        private final TriggeringPolicy policy;
        private final RolloverStrategy strategy;
        private final String advertiseURI;
        private final Layout<? extends Serializable> layout;
        
        public FactoryData(final String pattern, final boolean append, final boolean immediateFlush, final int bufferSize, final TriggeringPolicy policy, final RolloverStrategy strategy, final String advertiseURI, final Layout<? extends Serializable> layout) {
            this.pattern = pattern;
            this.append = append;
            this.immediateFlush = immediateFlush;
            this.bufferSize = bufferSize;
            this.policy = policy;
            this.strategy = strategy;
            this.advertiseURI = advertiseURI;
            this.layout = layout;
        }
        
        public TriggeringPolicy getTriggeringPolicy() {
            return this.policy;
        }
        
        public RolloverStrategy getRolloverStrategy() {
            return this.strategy;
        }
    }
}
