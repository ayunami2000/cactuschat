// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.channel;

import io.netty.util.concurrent.DefaultExecutorServiceFactory;
import java.util.concurrent.Executor;

public class DefaultEventLoop extends SingleThreadEventLoop
{
    public DefaultEventLoop() {
        this(null);
    }
    
    public DefaultEventLoop(final Executor executor) {
        this(null, executor);
    }
    
    public DefaultEventLoop(final EventLoopGroup parent) {
        this(parent, new DefaultExecutorServiceFactory(DefaultEventLoop.class).newExecutorService(1));
    }
    
    public DefaultEventLoop(final EventLoopGroup parent, final Executor executor) {
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
