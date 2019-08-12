// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.util.concurrent;

public interface PausableEventExecutor extends EventExecutor, WrappedEventExecutor
{
    void rejectNewTasks();
    
    void acceptNewTasks();
    
    boolean isAcceptingNewTasks();
}
