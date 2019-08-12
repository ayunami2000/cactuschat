// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.channel;

import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.AbstractEventExecutorGroup;

public abstract class AbstractEventLoopGroup extends AbstractEventExecutorGroup implements EventLoopGroup
{
    @Override
    public abstract EventLoop next();
}
