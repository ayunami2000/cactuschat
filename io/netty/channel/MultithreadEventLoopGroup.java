// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.channel;

import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.ExecutorServiceFactory;
import java.util.concurrent.Executor;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.concurrent.MultithreadEventExecutorGroup;

public abstract class MultithreadEventLoopGroup extends MultithreadEventExecutorGroup implements EventLoopGroup
{
    private static final InternalLogger logger;
    private static final int DEFAULT_EVENT_LOOP_THREADS;
    
    protected MultithreadEventLoopGroup(final int nEventLoops, final Executor executor, final Object... args) {
        super((nEventLoops == 0) ? MultithreadEventLoopGroup.DEFAULT_EVENT_LOOP_THREADS : nEventLoops, executor, args);
    }
    
    protected MultithreadEventLoopGroup(final int nEventLoops, final ExecutorServiceFactory executorServiceFactory, final Object... args) {
        super((nEventLoops == 0) ? MultithreadEventLoopGroup.DEFAULT_EVENT_LOOP_THREADS : nEventLoops, executorServiceFactory, args);
    }
    
    @Override
    public EventLoop next() {
        return (EventLoop)super.next();
    }
    
    @Override
    protected abstract EventLoop newChild(final Executor p0, final Object... p1) throws Exception;
    
    @Override
    public ChannelFuture register(final Channel channel) {
        return this.next().register(channel);
    }
    
    @Override
    public ChannelFuture register(final Channel channel, final ChannelPromise promise) {
        return this.next().register(channel, promise);
    }
    
    static {
        logger = InternalLoggerFactory.getInstance(MultithreadEventLoopGroup.class);
        DEFAULT_EVENT_LOOP_THREADS = Math.max(1, SystemPropertyUtil.getInt("io.netty.eventLoopThreads", Runtime.getRuntime().availableProcessors() * 2));
        if (MultithreadEventLoopGroup.logger.isDebugEnabled()) {
            MultithreadEventLoopGroup.logger.debug("-Dio.netty.eventLoopThreads: {}", (Object)MultithreadEventLoopGroup.DEFAULT_EVENT_LOOP_THREADS);
        }
    }
}
