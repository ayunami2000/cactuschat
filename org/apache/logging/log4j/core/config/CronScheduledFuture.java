// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.config;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledFuture;

public class CronScheduledFuture<V> implements ScheduledFuture<V>
{
    private volatile ScheduledFuture<?> scheduledFuture;
    
    public CronScheduledFuture(final ScheduledFuture<V> future) {
        this.scheduledFuture = future;
    }
    
    void setScheduledFuture(final ScheduledFuture<?> future) {
        this.scheduledFuture = future;
    }
    
    @Override
    public long getDelay(final TimeUnit unit) {
        return this.scheduledFuture.getDelay(unit);
    }
    
    @Override
    public int compareTo(final Delayed delayed) {
        return this.scheduledFuture.compareTo(delayed);
    }
    
    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        return this.scheduledFuture.cancel(mayInterruptIfRunning);
    }
    
    @Override
    public boolean isCancelled() {
        return this.scheduledFuture.isCancelled();
    }
    
    @Override
    public boolean isDone() {
        return this.scheduledFuture.isDone();
    }
    
    @Override
    public V get() throws InterruptedException, ExecutionException {
        return (V)this.scheduledFuture.get();
    }
    
    @Override
    public V get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return (V)this.scheduledFuture.get(timeout, unit);
    }
}
