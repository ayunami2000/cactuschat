// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.resolver;

import io.netty.util.concurrent.Promise;
import io.netty.util.concurrent.EventExecutor;
import java.net.SocketAddress;

public class NoopNameResolver extends SimpleNameResolver<SocketAddress>
{
    public NoopNameResolver(final EventExecutor executor) {
        super(executor);
    }
    
    @Override
    protected boolean doIsResolved(final SocketAddress address) {
        return true;
    }
    
    @Override
    protected void doResolve(final SocketAddress unresolvedAddress, final Promise<SocketAddress> promise) throws Exception {
        promise.setSuccess(unresolvedAddress);
    }
}
