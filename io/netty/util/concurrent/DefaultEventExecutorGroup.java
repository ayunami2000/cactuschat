// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.util.concurrent;

import java.util.concurrent.Executor;

public class DefaultEventExecutorGroup extends MultithreadEventExecutorGroup
{
    public DefaultEventExecutorGroup(final int nEventExecutors) {
        this(nEventExecutors, (Executor)null);
    }
    
    public DefaultEventExecutorGroup(final int nEventExecutors, final Executor executor) {
        super(nEventExecutors, executor, new Object[0]);
    }
    
    public DefaultEventExecutorGroup(final int nEventExecutors, final ExecutorServiceFactory executorServiceFactory) {
        super(nEventExecutors, executorServiceFactory, new Object[0]);
    }
    
    @Override
    protected EventExecutor newChild(final Executor executor, final Object... args) throws Exception {
        return new DefaultEventExecutor(this, executor);
    }
}
