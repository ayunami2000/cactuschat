// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.appender;

import org.apache.logging.log4j.core.LogEvent;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.logging.log4j.core.Filter;
import java.io.Serializable;
import org.apache.logging.log4j.core.Layout;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

public abstract class AbstractOutputStreamAppender<M extends OutputStreamManager> extends AbstractAppender
{
    private static final long serialVersionUID = 1L;
    private final boolean immediateFlush;
    private final M manager;
    private final ReadWriteLock rwLock;
    private final Lock readLock;
    
    protected AbstractOutputStreamAppender(final String name, final Layout<? extends Serializable> layout, final Filter filter, final boolean ignoreExceptions, final boolean immediateFlush, final M manager) {
        super(name, filter, layout, ignoreExceptions);
        this.rwLock = new ReentrantReadWriteLock();
        this.readLock = this.rwLock.readLock();
        this.manager = manager;
        this.immediateFlush = immediateFlush;
    }
    
    public boolean getImmediateFlush() {
        return this.immediateFlush;
    }
    
    public M getManager() {
        return this.manager;
    }
    
    @Override
    public void start() {
        if (this.getLayout() == null) {
            AbstractOutputStreamAppender.LOGGER.error("No layout set for the appender named [" + this.getName() + "].");
        }
        if (this.manager == null) {
            AbstractOutputStreamAppender.LOGGER.error("No OutputStreamManager set for the appender named [" + this.getName() + "].");
        }
        super.start();
    }
    
    @Override
    public void stop() {
        super.stop();
        this.manager.release();
    }
    
    @Override
    public void append(final LogEvent event) {
        this.readLock.lock();
        try {
            final byte[] bytes = this.getLayout().toByteArray(event);
            if (bytes.length > 0) {
                this.manager.write(bytes);
                if (this.immediateFlush || event.isEndOfBatch()) {
                    this.manager.flush();
                }
            }
        }
        catch (AppenderLoggingException ex) {
            this.error("Unable to write to stream " + this.manager.getName() + " for appender " + this.getName());
            throw ex;
        }
        finally {
            this.readLock.unlock();
        }
    }
}
