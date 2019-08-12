// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.resolver;

import io.netty.util.concurrent.EventExecutor;
import java.net.SocketAddress;

public final class NoopNameResolverGroup extends NameResolverGroup<SocketAddress>
{
    public static final NoopNameResolverGroup INSTANCE;
    
    private NoopNameResolverGroup() {
    }
    
    @Override
    protected NameResolver<SocketAddress> newResolver(final EventExecutor executor) throws Exception {
        return new NoopNameResolver(executor);
    }
    
    static {
        INSTANCE = new NoopNameResolverGroup();
    }
}
