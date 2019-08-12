// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.util.concurrent;

import java.util.concurrent.Executor;

public final class DefaultEventExecutor extends SingleThreadEventExecutor
{
    public DefaultEventExecutor() {
        this(null);
    }
    
    public DefaultEventExecutor(final Executor executor) {
        this(null, executor);
    }
    
    public DefaultEventExecutor(final EventExecutorGroup parent) {
        this(parent, new DefaultExecutorServiceFactory(DefaultEventExecutor.class).newExecutorService(1));
    }
    
    public DefaultEventExecutor(final EventExecutorGroup parent, final Executor executor) {
        super(parent, executor, true);
    }
    
    @Override
    protected void run() {
        final Runnable task = this.takeTask();
        if (task != null) {
            task.run();
            this.updateLastExecutionTime();
        }
        if (this.confirmShutdown()) {
            this.cleanupAndTerminate(true);
        }
        else {
            this.scheduleExecution();
        }
    }
}
