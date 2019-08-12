// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.util.internal;

import io.netty.util.concurrent.EventExecutor;

public interface RunnableEventExecutorAdapter extends Runnable
{
    EventExecutor executor();
    
    Runnable unwrap();
}
