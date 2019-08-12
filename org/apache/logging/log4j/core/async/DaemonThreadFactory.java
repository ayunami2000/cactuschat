// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.async;

import org.apache.logging.log4j.core.util.Log4jThread;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ThreadFactory;

public class DaemonThreadFactory implements ThreadFactory
{
    private static final AtomicInteger THREAD_NUMBER;
    private final ThreadGroup group;
    private final String threadNamePrefix;
    
    public DaemonThreadFactory(final String threadNamePrefix) {
        this.threadNamePrefix = threadNamePrefix;
        final SecurityManager securityManager = System.getSecurityManager();
        this.group = ((securityManager != null) ? securityManager.getThreadGroup() : Thread.currentThread().getThreadGroup());
    }
    
    @Override
    public Thread newThread(final Runnable runnable) {
        final Thread thread = new Log4jThread(this.group, runnable, this.threadNamePrefix + DaemonThreadFactory.THREAD_NUMBER.getAndIncrement(), 0L);
        if (!thread.isDaemon()) {
            thread.setDaemon(true);
        }
        if (thread.getPriority() != 5) {
            thread.setPriority(5);
        }
        return thread;
    }
    
    static {
        THREAD_NUMBER = new AtomicInteger(1);
    }
}
