// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.appender;

import java.io.Closeable;
import org.apache.logging.log4j.core.util.Closer;
import org.apache.logging.log4j.core.util.NullOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.security.PrivilegedActionException;
import java.security.AccessController;
import java.lang.reflect.Method;
import java.security.PrivilegedExceptionAction;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.io.IOException;
import java.util.Objects;
import java.io.Serializable;
import org.apache.logging.log4j.core.Layout;
import java.io.OutputStream;
import java.nio.MappedByteBuffer;
import java.io.RandomAccessFile;

public class MemoryMappedFileManager extends OutputStreamManager
{
    static final int DEFAULT_REGION_LENGTH = 33554432;
    private static final int MAX_REMAP_COUNT = 10;
    private static final MemoryMappedFileManagerFactory FACTORY;
    private static final double NANOS_PER_MILLISEC = 1000000.0;
    private final boolean isForce;
    private final int regionLength;
    private final String advertiseURI;
    private final RandomAccessFile randomAccessFile;
    private final ThreadLocal<Boolean> isEndOfBatch;
    private MappedByteBuffer mappedBuffer;
    private long mappingOffset;
    
    protected MemoryMappedFileManager(final RandomAccessFile file, final String fileName, final OutputStream os, final boolean force, final long position, final int regionLength, final String advertiseURI, final Layout<? extends Serializable> layout, final boolean writeHeader) throws IOException {
        super(os, fileName, layout, writeHeader);
        this.isEndOfBatch = new ThreadLocal<Boolean>();
        this.isForce = force;
        this.randomAccessFile = Objects.requireNonNull(file, "RandomAccessFile");
        this.regionLength = regionLength;
        this.advertiseURI = advertiseURI;
        this.isEndOfBatch.set(Boolean.FALSE);
        this.mappedBuffer = mmap(this.randomAccessFile.getChannel(), this.getFileName(), position, regionLength);
        this.mappingOffset = position;
    }
    
    public static MemoryMappedFileManager getFileManager(final String fileName, final boolean append, final boolean isForce, final int regionLength, final String advertiseURI, final Layout<? extends Serializable> layout) {
        return (MemoryMappedFileManager)OutputStreamManager.getManager(fileName, new FactoryData(append, isForce, regionLength, advertiseURI, layout), MemoryMappedFileManager.FACTORY);
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
        while (length > this.mappedBuffer.remaining()) {
            final int chunk = this.mappedBuffer.remaining();
            this.mappedBuffer.put(bytes, offset, chunk);
            offset += chunk;
            length -= chunk;
            this.remap();
        }
        this.mappedBuffer.put(bytes, offset, length);
    }
    
    private synchronized void remap() {
        final long offset = this.mappingOffset + this.mappedBuffer.position();
        final int length = this.mappedBuffer.remaining() + this.regionLength;
        try {
            unsafeUnmap(this.mappedBuffer);
            final long fileLength = this.randomAccessFile.length() + this.regionLength;
            MemoryMappedFileManager.LOGGER.debug("{} {} extending {} by {} bytes to {}", new Object[] { this.getClass().getSimpleName(), this.getName(), this.getFileName(), this.regionLength, fileLength });
            final long startNanos = System.nanoTime();
            this.randomAccessFile.setLength(fileLength);
            final float millis = (float)((System.nanoTime() - startNanos) / 1000000.0);
            MemoryMappedFileManager.LOGGER.debug("{} {} extended {} OK in {} millis", new Object[] { this.getClass().getSimpleName(), this.getName(), this.getFileName(), millis });
            this.mappedBuffer = mmap(this.randomAccessFile.getChannel(), this.getFileName(), offset, length);
            this.mappingOffset = offset;
        }
        catch (Exception ex) {
            this.logError("unable to remap", ex);
        }
    }
    
    @Override
    public synchronized void flush() {
        this.mappedBuffer.force();
    }
    
    public synchronized void close() {
        final long position = this.mappedBuffer.position();
        final long length = this.mappingOffset + position;
        try {
            unsafeUnmap(this.mappedBuffer);
        }
        catch (Exception ex) {
            this.logError("unable to unmap MappedBuffer", ex);
        }
        try {
            MemoryMappedFileManager.LOGGER.debug("MMapAppender closing. Setting {} length to {} (offset {} + position {})", new Object[] { this.getFileName(), length, this.mappingOffset, position });
            this.randomAccessFile.setLength(length);
            this.randomAccessFile.close();
        }
        catch (IOException ex2) {
            this.logError("unable to close MemoryMappedFile", ex2);
        }
    }
    
    public static MappedByteBuffer mmap(final FileChannel fileChannel, final String fileName, final long start, final int size) throws IOException {
        int i = 1;
        while (true) {
            try {
                MemoryMappedFileManager.LOGGER.debug("MMapAppender remapping {} start={}, size={}", new Object[] { fileName, start, size });
                final long startNanos = System.nanoTime();
                final MappedByteBuffer map = fileChannel.map(FileChannel.MapMode.READ_WRITE, start, size);
                map.order(ByteOrder.nativeOrder());
                final float millis = (float)((System.nanoTime() - startNanos) / 1000000.0);
                MemoryMappedFileManager.LOGGER.debug("MMapAppender remapped {} OK in {} millis", new Object[] { fileName, millis });
                return map;
            }
            catch (IOException e) {
                if (e.getMessage() == null || !e.getMessage().endsWith("user-mapped section open")) {
                    throw e;
                }
                MemoryMappedFileManager.LOGGER.debug("Remap attempt {}/{} failed. Retrying...", new Object[] { i, 10, e });
                if (i < 10) {
                    Thread.yield();
                }
                else {
                    try {
                        Thread.sleep(1L);
                    }
                    catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                        throw e;
                    }
                }
                ++i;
                continue;
            }
            break;
        }
    }
    
    private static void unsafeUnmap(final MappedByteBuffer mbb) throws PrivilegedActionException {
        MemoryMappedFileManager.LOGGER.debug("MMapAppender unmapping old buffer...");
        final long startNanos = System.nanoTime();
        AccessController.doPrivileged((PrivilegedExceptionAction<Object>)new PrivilegedExceptionAction<Object>() {
            @Override
            public Object run() throws Exception {
                final Method getCleanerMethod = mbb.getClass().getMethod("cleaner", (Class<?>[])new Class[0]);
                getCleanerMethod.setAccessible(true);
                final Object cleaner = getCleanerMethod.invoke(mbb, new Object[0]);
                final Method cleanMethod = cleaner.getClass().getMethod("clean", (Class<?>[])new Class[0]);
                cleanMethod.invoke(cleaner, new Object[0]);
                return null;
            }
        });
        final float millis = (float)((System.nanoTime() - startNanos) / 1000000.0);
        MemoryMappedFileManager.LOGGER.debug("MMapAppender unmapped buffer OK in {} millis", new Object[] { millis });
    }
    
    public String getFileName() {
        return this.getName();
    }
    
    public int getRegionLength() {
        return this.regionLength;
    }
    
    public boolean isImmediateFlush() {
        return this.isForce;
    }
    
    @Override
    public Map<String, String> getContentFormat() {
        final Map<String, String> result = new HashMap<String, String>(super.getContentFormat());
        result.put("fileURI", this.advertiseURI);
        return result;
    }
    
    static {
        FACTORY = new MemoryMappedFileManagerFactory();
    }
    
    private static class FactoryData
    {
        private final boolean append;
        private final boolean force;
        private final int regionLength;
        private final String advertiseURI;
        private final Layout<? extends Serializable> layout;
        
        public FactoryData(final boolean append, final boolean force, final int regionLength, final String advertiseURI, final Layout<? extends Serializable> layout) {
            this.append = append;
            this.force = force;
            this.regionLength = regionLength;
            this.advertiseURI = advertiseURI;
            this.layout = layout;
        }
    }
    
    private static class MemoryMappedFileManagerFactory implements ManagerFactory<MemoryMappedFileManager, FactoryData>
    {
        @Override
        public MemoryMappedFileManager createManager(final String name, final FactoryData data) {
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
            RandomAccessFile raf = null;
            try {
                raf = new RandomAccessFile(name, "rw");
                final long position = data.append ? raf.length() : 0L;
                raf.setLength(position + data.regionLength);
                return new MemoryMappedFileManager(raf, name, os, data.force, position, data.regionLength, data.advertiseURI, data.layout, writeHeader);
            }
            catch (Exception ex) {
                AbstractManager.LOGGER.error("MemoryMappedFileManager (" + name + ") " + ex, ex);
                Closer.closeSilently(raf);
                return null;
            }
        }
    }
}
