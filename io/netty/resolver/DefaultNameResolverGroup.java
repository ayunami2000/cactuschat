// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.resolver;

import io.netty.util.concurrent.EventExecutor;
import java.net.InetSocketAddress;

public final class DefaultNameResolverGroup extends NameResolverGroup<InetSocketAddress>
{
    public static final DefaultNameResolverGroup INSTANCE;
    
    private DefaultNameResolverGroup() {
    }
    
    @Override
    protected NameResolver<InetSocketAddress> newResolver(final EventExecutor executor) throws Exception {
        return new DefaultNameResolver(executor);
    }
    
    static {
        INSTANCE = new DefaultNameResolverGroup();
    }
}
