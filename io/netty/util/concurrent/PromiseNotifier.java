// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.util.concurrent;

public class PromiseNotifier<V, F extends Future<V>> implements GenericFutureListener<F>
{
    private final Promise<? super V>[] promises;
    
    @SafeVarargs
    public PromiseNotifier(final Promise<? super V>... promises) {
        if (promises == null) {
            throw new NullPointerException("promises");
        }
        for (final Promise<? super V> promise : promises) {
            if (promise == null) {
                throw new IllegalArgumentException("promises contains null Promise");
            }
        }
        this.promises = (Promise<? super V>[])promises.clone();
    }
    
    @Override
    public void operationComplete(final F future) throws Exception {
        if (future.isSuccess()) {
            final V result = future.get();
            for (final Promise<? super V> p : this.promises) {
                p.setSuccess((Object)result);
            }
            return;
        }
        final Throwable cause = future.cause();
        for (final Promise<? super V> p : this.promises) {
            p.setFailure(cause);
        }
    }
}
