// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.util.concurrent;

import java.util.Set;

public interface EventExecutor extends EventExecutorGroup
{
    EventExecutor next();
    
     <E extends EventExecutor> Set<E> children();
    
    EventExecutorGroup parent();
    
    boolean inEventLoop();
    
    boolean inEventLoop(final Thread p0);
    
    EventExecutor unwrap();
    
     <V> Promise<V> newPromise();
    
     <V> ProgressivePromise<V> newProgressivePromise();
    
     <V> Future<V> newSucceededFuture(final V p0);
    
     <V> Future<V> newFailedFuture(final Throwable p0);
}
