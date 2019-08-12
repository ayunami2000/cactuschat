// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.channel.epoll;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.EventLoop;
import java.util.Iterator;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.ExecutorServiceFactory;
import java.util.concurrent.Executor;
import io.netty.channel.MultithreadEventLoopGroup;

public final class EpollEventLoopGroup extends MultithreadEventLoopGroup
{
    public EpollEventLoopGroup() {
        this(0);
    }
    
    public EpollEventLoopGroup(final int nEventLoops) {
        this(nEventLoops, (Executor)null);
    }
    
    public EpollEventLoopGroup(final int nEventLoops, final Executor executor) {
        this(nEventLoops, executor, 0);
    }
    
    public EpollEventLoopGroup(final int nEventLoops, final ExecutorServiceFactory executorServiceFactory) {
        this(nEventLoops, executorServiceFactory, 0);
    }
    
    @Deprecated
    public EpollEventLoopGroup(final int nEventLoops, final Executor executor, final int maxEventsAtOnce) {
        super(nEventLoops, executor, new Object[] { maxEventsAtOnce });
    }
    
    @Deprecated
    public EpollEventLoopGroup(final int nEventLoops, final ExecutorServiceFactory executorServiceFactory, final int maxEventsAtOnce) {
        super(nEventLoops, executorServiceFactory, new Object[] { maxEventsAtOnce });
    }
    
    public void setIoRatio(final int ioRatio) {
        for (final EventExecutor e : this.children()) {
            ((EpollEventLoop)e).setIoRatio(ioRatio);
        }
    }
    
    @Override
    protected EventLoop newChild(final Executor executor, final Object... args) throws Exception {
        return new EpollEventLoop(this, executor, (int)args[0]);
    }
}
