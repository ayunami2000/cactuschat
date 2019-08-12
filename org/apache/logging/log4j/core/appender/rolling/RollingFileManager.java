// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.appender.rolling;

import java.io.FileNotFoundException;
import java.io.File;
import org.apache.logging.log4j.core.appender.rolling.action.Action;
import org.apache.logging.log4j.core.appender.rolling.action.AbstractAction;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.Log4jThread;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.ManagerFactory;
import org.apache.logging.log4j.core.appender.OutputStreamManager;
import java.io.Serializable;
import org.apache.logging.log4j.core.Layout;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.concurrent.Semaphore;
import org.apache.logging.log4j.core.appender.FileManager;

public class RollingFileManager extends FileManager
{
    private static RollingFileManagerFactory factory;
    private long size;
    private long initialTime;
    private final PatternProcessor patternProcessor;
    private final Semaphore semaphore;
    private volatile TriggeringPolicy triggeringPolicy;
    private volatile RolloverStrategy rolloverStrategy;
    private static final AtomicReferenceFieldUpdater<RollingFileManager, TriggeringPolicy> triggeringPolicyUpdater;
    private static final AtomicReferenceFieldUpdater<RollingFileManager, RolloverStrategy> rolloverStrategyUpdater;
    
    protected RollingFileManager(final String fileName, final String pattern, final OutputStream os, final boolean append, final long size, final long time, final TriggeringPolicy triggeringPolicy, final RolloverStrategy rolloverStrategy, final String advertiseURI, final Layout<? extends Serializable> layout, final int bufferSize, final boolean writeHeader) {
        super(fileName, os, append, false, advertiseURI, layout, bufferSize, writeHeader);
        this.semaphore = new Semaphore(1);
        this.size = size;
        this.initialTime = time;
        this.triggeringPolicy = triggeringPolicy;
        this.rolloverStrategy = rolloverStrategy;
        this.patternProcessor = new PatternProcessor(pattern);
        triggeringPolicy.initialize(this);
    }
    
    public static RollingFileManager getFileManager(final String fileName, final String pattern, final boolean append, final boolean bufferedIO, final TriggeringPolicy policy, final RolloverStrategy strategy, final String advertiseURI, final Layout<? extends Serializable> layout, final int bufferSize) {
        return (RollingFileManager)OutputStreamManager.getManager(fileName, new FactoryData(pattern, append, bufferedIO, policy, strategy, advertiseURI, layout, bufferSize), RollingFileManager.factory);
    }
    
    @Override
    protected synchronized void write(final byte[] bytes, final int offset, final int length) {
        this.size += length;
        super.write(bytes, offset, length);
    }
    
    public long getFileSize() {
        return this.size;
    }
    
    public long getFileTime() {
        return this.initialTime;
    }
    
    public synchronized void checkRollover(final LogEvent event) {
        if (this.triggeringPolicy.isTriggeringEvent(event)) {
            this.rollover();
        }
    }
    
    public synchronized void rollover() {
        if (this.rollover(this.rolloverStrategy)) {
            try {
                this.size = 0L;
                this.initialTime = System.currentTimeMillis();
                this.createFileAfterRollover();
            }
            catch (IOException e) {
                this.logError("failed to create file after rollover", e);
            }
        }
    }
    
    protected void createFileAfterRollover() throws IOException {
        final OutputStream os = new FileOutputStream(this.getFileName(), this.isAppend());
        if (this.getBufferSize() > 0) {
            this.setOutputStream(new BufferedOutputStream(os, this.getBufferSize()));
        }
        else {
            this.setOutputStream(os);
        }
    }
    
    public PatternProcessor getPatternProcessor() {
        return this.patternProcessor;
    }
    
    public void setTriggeringPolicy(final TriggeringPolicy triggeringPolicy) {
        triggeringPolicy.initialize(this);
        RollingFileManager.triggeringPolicyUpdater.compareAndSet(this, this.triggeringPolicy, triggeringPolicy);
    }
    
    public void setRolloverStrategy(final RolloverStrategy rolloverStrategy) {
        RollingFileManager.rolloverStrategyUpdater.compareAndSet(this, this.rolloverStrategy, rolloverStrategy);
    }
    
    public <T extends TriggeringPolicy> T getTriggeringPolicy() {
        return (T)this.triggeringPolicy;
    }
    
    public RolloverStrategy getRolloverStrategy() {
        return this.rolloverStrategy;
    }
    
    private boolean rollover(final RolloverStrategy strategy) {
        try {
            this.semaphore.acquire();
        }
        catch (InterruptedException e) {
            this.logError("Thread interrupted while attempting to check rollover", e);
            return false;
        }
        boolean success = false;
        Thread thread = null;
        try {
            final RolloverDescription descriptor = strategy.rollover(this);
            if (descriptor != null) {
                this.writeFooter();
                this.close();
                if (descriptor.getSynchronous() != null) {
                    RollingFileManager.LOGGER.debug("RollingFileManager executing synchronous {}", new Object[] { descriptor.getSynchronous() });
                    try {
                        success = descriptor.getSynchronous().execute();
                    }
                    catch (Exception ex) {
                        this.logError("caught error in synchronous task", ex);
                    }
                }
                if (success && descriptor.getAsynchronous() != null) {
                    RollingFileManager.LOGGER.debug("RollingFileManager executing async {}", new Object[] { descriptor.getAsynchronous() });
                    thread = new Log4jThread(new AsyncAction(descriptor.getAsynchronous(), this));
                    thread.start();
                }
                return true;
            }
            return false;
        }
        finally {
            if (thread == null || !thread.isAlive()) {
                this.semaphore.release();
            }
        }
    }
    
    @Override
    public void updateData(final Object data) {
        final FactoryData factoryData = (FactoryData)data;
        this.setRolloverStrategy(factoryData.getRolloverStrategy());
        this.setTriggeringPolicy(factoryData.getTriggeringPolicy());
    }
    
    static {
        RollingFileManager.factory = new RollingFileManagerFactory();
        triggeringPolicyUpdater = AtomicReferenceFieldUpdater.newUpdater(RollingFileManager.class, TriggeringPolicy.class, "triggeringPolicy");
        rolloverStrategyUpdater = AtomicReferenceFieldUpdater.newUpdater(RollingFileManager.class, RolloverStrategy.class, "rolloverStrategy");
    }
    
    private static class AsyncAction extends AbstractAction
    {
        private final Action action;
        private final RollingFileManager manager;
        
        public AsyncAction(final Action act, final RollingFileManager manager) {
            this.action = act;
            this.manager = manager;
        }
        
        @Override
        public boolean execute() throws IOException {
            try {
                return this.action.execute();
            }
            finally {
                this.manager.semaphore.release();
            }
        }
        
        @Override
        public void close() {
            this.action.close();
        }
        
        @Override
        public boolean isComplete() {
            return this.action.isComplete();
        }
    }
    
    private static class FactoryData
    {
        private final String pattern;
        private final boolean append;
        private final boolean bufferedIO;
        private final int bufferSize;
        private final TriggeringPolicy policy;
        private final RolloverStrategy strategy;
        private final String advertiseURI;
        private final Layout<? extends Serializable> layout;
        
        public FactoryData(final String pattern, final boolean append, final boolean bufferedIO, final TriggeringPolicy policy, final RolloverStrategy strategy, final String advertiseURI, final Layout<? extends Serializable> layout, final int bufferSize) {
            this.pattern = pattern;
            this.append = append;
            this.bufferedIO = bufferedIO;
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
    
    private static class RollingFileManagerFactory implements ManagerFactory<RollingFileManager, FactoryData>
    {
        @Override
        public RollingFileManager createManager(final String name, final FactoryData data) {
            final File file = new File(name);
            final File parent = file.getParentFile();
            if (null != parent && !parent.exists()) {
                parent.mkdirs();
            }
            final boolean writeHeader = !data.append || !file.exists();
            try {
                file.createNewFile();
            }
            catch (IOException ioe) {
                RollingFileManager.LOGGER.error("Unable to create file " + name, ioe);
                return null;
            }
            final long size = data.append ? file.length() : 0L;
            try {
                OutputStream os = new FileOutputStream(name, data.append);
                int bufferSize = data.bufferSize;
                if (data.bufferedIO) {
                    os = new BufferedOutputStream(os, bufferSize);
                }
                else {
                    bufferSize = -1;
                }
                final long time = file.lastModified();
                return new RollingFileManager(name, data.pattern, os, data.append, size, time, data.policy, data.strategy, data.advertiseURI, data.layout, bufferSize, writeHeader);
            }
            catch (FileNotFoundException ex) {
                RollingFileManager.LOGGER.error("FileManager (" + name + ") " + ex);
                return null;
            }
        }
    }
}
