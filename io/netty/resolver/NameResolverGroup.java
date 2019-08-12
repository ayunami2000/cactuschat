// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.resolver;

import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import java.util.IdentityHashMap;
import io.netty.util.concurrent.EventExecutor;
import java.util.Map;
import io.netty.util.internal.logging.InternalLogger;
import java.io.Closeable;
import java.net.SocketAddress;

public abstract class NameResolverGroup<T extends SocketAddress> implements Closeable
{
    private static final InternalLogger logger;
    private final Map<EventExecutor, NameResolver<T>> resolvers;
    
    protected NameResolverGroup() {
        this.resolvers = new IdentityHashMap<EventExecutor, NameResolver<T>>();
    }
    
    public NameResolver<T> getResolver(final EventExecutor executor) {
        if (executor == null) {
            throw new NullPointerException("executor");
        }
        if (executor.isShuttingDown()) {
            throw new IllegalStateException("executor not accepting a task");
        }
        return this.getResolver0(executor.unwrap());
    }
    
    private NameResolver<T> getResolver0(final EventExecutor executor) {
        NameResolver<T> r;
        synchronized (this.resolvers) {
            r = this.resolvers.get(executor);
            if (r == null) {
                NameResolver<T> newResolver;
                try {
                    newResolver = this.newResolver(executor);
                }
                catch (Exception e) {
                    throw new IllegalStateException("failed to create a new resolver", e);
                }
                this.resolvers.put(executor, newResolver);
                executor.terminationFuture().addListener(new FutureListener<Object>() {
                    @Override
                    public void operationComplete(final Future<Object> future) throws Exception {
                        NameResolverGroup.this.resolvers.remove(executor);
                        newResolver.close();
                    }
                });
                r = newResolver;
            }
        }
        return r;
    }
    
    protected abstract NameResolver<T> newResolver(final EventExecutor p0) throws Exception;
    
    @Override
    public void close() {
        final NameResolver<T>[] rArray;
        synchronized (this.resolvers) {
            rArray = this.resolvers.values().toArray(new NameResolver[this.resolvers.size()]);
            this.resolvers.clear();
        }
        for (final NameResolver<T> r : rArray) {
            try {
                r.close();
            }
            catch (Throwable t) {
                NameResolverGroup.logger.warn("Failed to close a resolver:", t);
            }
        }
    }
    
    static {
        logger = InternalLoggerFactory.getInstance(NameResolverGroup.class);
    }
}
