// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.appender;

import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.Level;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.Map;
import org.apache.logging.log4j.Logger;

public abstract class AbstractManager
{
    protected static final Logger LOGGER;
    private static final Map<String, AbstractManager> MAP;
    private static final Lock LOCK;
    protected int count;
    private final String name;
    
    protected AbstractManager(final String name) {
        this.name = name;
        AbstractManager.LOGGER.debug("Starting {} {}", new Object[] { this.getClass().getSimpleName(), name });
    }
    
    public static <M extends AbstractManager, T> M getManager(final String name, final ManagerFactory<M, T> factory, final T data) {
        AbstractManager.LOCK.lock();
        try {
            M manager = (M)AbstractManager.MAP.get(name);
            if (manager == null) {
                manager = factory.createManager(name, data);
                if (manager == null) {
                    throw new IllegalStateException("ManagerFactory [" + factory + "] unable to create manager for [" + name + "] with data [" + data + "]");
                }
                AbstractManager.MAP.put(name, manager);
            }
            else {
                manager.updateData(data);
            }
            final AbstractManager abstractManager = manager;
            ++abstractManager.count;
            return manager;
        }
        finally {
            AbstractManager.LOCK.unlock();
        }
    }
    
    public void updateData(final Object data) {
    }
    
    public static boolean hasManager(final String name) {
        AbstractManager.LOCK.lock();
        try {
            return AbstractManager.MAP.containsKey(name);
        }
        finally {
            AbstractManager.LOCK.unlock();
        }
    }
    
    protected void releaseSub() {
    }
    
    protected int getCount() {
        return this.count;
    }
    
    public void release() {
        AbstractManager.LOCK.lock();
        try {
            --this.count;
            if (this.count <= 0) {
                AbstractManager.MAP.remove(this.name);
                AbstractManager.LOGGER.debug("Shutting down {} {}", new Object[] { this.getClass().getSimpleName(), this.getName() });
                this.releaseSub();
            }
        }
        finally {
            AbstractManager.LOCK.unlock();
        }
    }
    
    public String getName() {
        return this.name;
    }
    
    public Map<String, String> getContentFormat() {
        return new HashMap<String, String>();
    }
    
    protected void log(final Level level, final String message, final Throwable throwable) {
        final Message m = AbstractManager.LOGGER.getMessageFactory().newMessage("{} {} {}: {}", this.getClass().getSimpleName(), this.getName(), message, throwable);
        AbstractManager.LOGGER.log(level, m, throwable);
    }
    
    protected void logDebug(final String message, final Throwable throwable) {
        this.log(Level.DEBUG, message, throwable);
    }
    
    protected void logError(final String message, final Throwable throwable) {
        this.log(Level.ERROR, message, throwable);
    }
    
    protected void logWarn(final String message, final Throwable throwable) {
        this.log(Level.WARN, message, throwable);
    }
    
    static {
        LOGGER = StatusLogger.getLogger();
        MAP = new HashMap<String, AbstractManager>();
        LOCK = new ReentrantLock();
    }
}
