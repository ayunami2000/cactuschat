// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.appender.db;

import org.apache.logging.log4j.core.appender.ManagerFactory;
import java.util.Iterator;
import org.apache.logging.log4j.core.LogEvent;
import java.util.ArrayList;
import java.io.Flushable;
import org.apache.logging.log4j.core.appender.AbstractManager;

public abstract class AbstractDatabaseManager extends AbstractManager implements Flushable
{
    private final ArrayList<LogEvent> buffer;
    private final int bufferSize;
    private boolean running;
    
    protected AbstractDatabaseManager(final String name, final int bufferSize) {
        super(name);
        this.running = false;
        this.bufferSize = bufferSize;
        this.buffer = new ArrayList<LogEvent>(bufferSize + 1);
    }
    
    protected abstract void startupInternal() throws Exception;
    
    public final synchronized void startup() {
        if (!this.isRunning()) {
            try {
                this.startupInternal();
                this.running = true;
            }
            catch (Exception e) {
                this.logError("could not perform database startup operations", e);
            }
        }
    }
    
    protected abstract void shutdownInternal() throws Exception;
    
    public final synchronized void shutdown() {
        this.flush();
        if (this.isRunning()) {
            try {
                this.shutdownInternal();
            }
            catch (Exception e) {
                this.logWarn("caught exception while performing database shutdown operations", e);
            }
            finally {
                this.running = false;
            }
        }
    }
    
    public final boolean isRunning() {
        return this.running;
    }
    
    protected abstract void connectAndStart();
    
    protected abstract void writeInternal(final LogEvent p0);
    
    protected abstract void commitAndClose();
    
    @Override
    public final synchronized void flush() {
        if (this.isRunning() && this.buffer.size() > 0) {
            this.connectAndStart();
            try {
                for (final LogEvent event : this.buffer) {
                    this.writeInternal(event);
                }
            }
            finally {
                this.commitAndClose();
                this.buffer.clear();
            }
        }
    }
    
    public final synchronized void write(final LogEvent event) {
        if (this.bufferSize > 0) {
            this.buffer.add(event);
            if (this.buffer.size() >= this.bufferSize || event.isEndOfBatch()) {
                this.flush();
            }
        }
        else {
            this.connectAndStart();
            try {
                this.writeInternal(event);
            }
            finally {
                this.commitAndClose();
            }
        }
    }
    
    public final void releaseSub() {
        this.shutdown();
    }
    
    @Override
    public final String toString() {
        return this.getName();
    }
    
    protected static <M extends AbstractDatabaseManager, T extends AbstractFactoryData> M getManager(final String name, final T data, final ManagerFactory<M, T> factory) {
        return AbstractManager.getManager(name, factory, data);
    }
    
    protected abstract static class AbstractFactoryData
    {
        private final int bufferSize;
        
        protected AbstractFactoryData(final int bufferSize) {
            this.bufferSize = bufferSize;
        }
        
        public int getBufferSize() {
            return this.bufferSize;
        }
    }
}
