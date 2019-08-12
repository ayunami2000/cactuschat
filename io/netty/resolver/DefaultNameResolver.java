// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.resolver;

import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.net.InetAddress;
import io.netty.util.concurrent.Promise;
import io.netty.util.concurrent.EventExecutor;
import java.net.InetSocketAddress;

public class DefaultNameResolver extends SimpleNameResolver<InetSocketAddress>
{
    public DefaultNameResolver(final EventExecutor executor) {
        super(executor);
    }
    
    @Override
    protected boolean doIsResolved(final InetSocketAddress address) {
        return !address.isUnresolved();
    }
    
    @Override
    protected void doResolve(final InetSocketAddress unresolvedAddress, final Promise<InetSocketAddress> promise) throws Exception {
        try {
            promise.setSuccess(new InetSocketAddress(InetAddress.getByName(unresolvedAddress.getHostString()), unresolvedAddress.getPort()));
        }
        catch (UnknownHostException e) {
            promise.setFailure(e);
        }
    }
}
