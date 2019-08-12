// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.channel;

import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.ExecutorServiceFactory;
import java.util.concurrent.Executor;

public class DefaultEventLoopGroup extends MultithreadEventLoopGroup
{
    public DefaultEventLoopGroup() {
        this(0);
    }
    
    public DefaultEventLoopGroup(final int nEventLoops) {
        this(nEventLoops, (Executor)null);
    }
    
    public DefaultEventLoopGroup(final int nEventLoops, final Executor executor) {
        super(nEventLoops, executor, new Object[0]);
    }
    
    public DefaultEventLoopGroup(final int nEventLoops, final ExecutorServiceFactory executorServiceFactory) {
        super(nEventLoops, executorServiceFactory, new Object[0]);
    }
    
    @Override
    protected EventLoop newChild(final Executor executor, final Object... args) throws Exception {
        return new DefaultEventLoop(this, executor);
    }
}
