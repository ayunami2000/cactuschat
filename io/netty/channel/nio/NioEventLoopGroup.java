// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.channel.nio;

import io.netty.channel.EventLoop;
import java.util.Iterator;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.ExecutorServiceFactory;
import java.nio.channels.spi.SelectorProvider;
import java.util.concurrent.Executor;
import io.netty.channel.MultithreadEventLoopGroup;

public class NioEventLoopGroup extends MultithreadEventLoopGroup
{
    public NioEventLoopGroup() {
        this(0);
    }
    
    public NioEventLoopGroup(final int nEventLoops) {
        this(nEventLoops, (Executor)null);
    }
    
    public NioEventLoopGroup(final int nEventLoops, final Executor executor) {
        this(nEventLoops, executor, SelectorProvider.provider());
    }
    
    public NioEventLoopGroup(final int nEventLoops, final ExecutorServiceFactory executorServiceFactory) {
        this(nEventLoops, executorServiceFactory, SelectorProvider.provider());
    }
    
    public NioEventLoopGroup(final int nEventLoops, final Executor executor, final SelectorProvider selectorProvider) {
        super(nEventLoops, executor, new Object[] { selectorProvider });
    }
    
    public NioEventLoopGroup(final int nEventLoops, final ExecutorServiceFactory executorServiceFactory, final SelectorProvider selectorProvider) {
        super(nEventLoops, executorServiceFactory, new Object[] { selectorProvider });
    }
    
    public void setIoRatio(final int ioRatio) {
        for (final EventExecutor e : this.children()) {
            ((NioEventLoop)e).setIoRatio(ioRatio);
        }
    }
    
    public void rebuildSelectors() {
        for (final EventExecutor e : this.children()) {
            ((NioEventLoop)e).rebuildSelector();
        }
    }
    
    @Override
    protected EventLoop newChild(final Executor executor, final Object... args) throws Exception {
        return new NioEventLoop(this, executor, (SelectorProvider)args[0]);
    }
}
