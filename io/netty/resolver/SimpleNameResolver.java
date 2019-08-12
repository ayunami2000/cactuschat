// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.resolver;

import io.netty.util.concurrent.Promise;
import java.net.InetSocketAddress;
import io.netty.util.concurrent.Future;
import java.nio.channels.UnsupportedAddressTypeException;
import io.netty.util.internal.TypeParameterMatcher;
import io.netty.util.concurrent.EventExecutor;
import java.net.SocketAddress;

public abstract class SimpleNameResolver<T extends SocketAddress> implements NameResolver<T>
{
    private final EventExecutor executor;
    private final TypeParameterMatcher matcher;
    
    protected SimpleNameResolver(final EventExecutor executor) {
        if (executor == null) {
            throw new NullPointerException("executor");
        }
        this.executor = executor;
        this.matcher = TypeParameterMatcher.find(this, SimpleNameResolver.class, "T");
    }
    
    protected SimpleNameResolver(final EventExecutor executor, final Class<? extends T> addressType) {
        if (executor == null) {
            throw new NullPointerException("executor");
        }
        this.executor = executor;
        this.matcher = TypeParameterMatcher.get(addressType);
    }
    
    protected EventExecutor executor() {
        return this.executor;
    }
    
    @Override
    public boolean isSupported(final SocketAddress address) {
        return this.matcher.match(address);
    }
    
    @Override
    public final boolean isResolved(final SocketAddress address) {
        if (!this.isSupported(address)) {
            throw new UnsupportedAddressTypeException();
        }
        final T castAddress = (T)address;
        return this.doIsResolved(castAddress);
    }
    
    protected abstract boolean doIsResolved(final T p0);
    
    @Override
    public final Future<T> resolve(final String inetHost, final int inetPort) {
        if (inetHost == null) {
            throw new NullPointerException("inetHost");
        }
        return this.resolve(InetSocketAddress.createUnresolved(inetHost, inetPort));
    }
    
    @Override
    public Future<T> resolve(final String inetHost, final int inetPort, final Promise<T> promise) {
        if (inetHost == null) {
            throw new NullPointerException("inetHost");
        }
        return this.resolve(InetSocketAddress.createUnresolved(inetHost, inetPort), promise);
    }
    
    @Override
    public final Future<T> resolve(final SocketAddress address) {
        if (address == null) {
            throw new NullPointerException("unresolvedAddress");
        }
        if (!this.isSupported(address)) {
            return this.executor().newFailedFuture(new UnsupportedAddressTypeException());
        }
        if (this.isResolved(address)) {
            final T cast = (T)address;
            return this.executor.newSucceededFuture(cast);
        }
        try {
            final T cast = (T)address;
            final Promise<T> promise = this.executor().newPromise();
            this.doResolve(cast, promise);
            return promise;
        }
        catch (Exception e) {
            return this.executor().newFailedFuture(e);
        }
    }
    
    @Override
    public final Future<T> resolve(final SocketAddress address, final Promise<T> promise) {
        if (address == null) {
            throw new NullPointerException("unresolvedAddress");
        }
        if (promise == null) {
            throw new NullPointerException("promise");
        }
        if (!this.isSupported(address)) {
            return promise.setFailure(new UnsupportedAddressTypeException());
        }
        if (this.isResolved(address)) {
            final T cast = (T)address;
            return promise.setSuccess(cast);
        }
        try {
            final T cast = (T)address;
            this.doResolve(cast, promise);
            return promise;
        }
        catch (Exception e) {
            return promise.setFailure(e);
        }
    }
    
    protected abstract void doResolve(final T p0, final Promise<T> p1) throws Exception;
    
    @Override
    public void close() {
    }
}
