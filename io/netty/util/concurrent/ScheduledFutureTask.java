// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.util.concurrent;

import io.netty.util.internal.OneTimeTask;
import io.netty.util.internal.CallableEventExecutorAdapter;
import java.util.Queue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

final class ScheduledFutureTask<V> extends PromiseTask<V> implements ScheduledFuture<V>
{
    private static final AtomicLong nextTaskId;
    private static final long START_TIME;
    private final long id;
    private long deadlineNanos;
    private final long periodNanos;
    
    static long nanoTime() {
        return System.nanoTime() - ScheduledFutureTask.START_TIME;
    }
    
    static long deadlineNanos(final long delay) {
        return nanoTime() + delay;
    }
    
    ScheduledFutureTask(final EventExecutor executor, final Callable<V> callable, final long nanoTime, final long period) {
        super(executor.unwrap(), callable);
        this.id = ScheduledFutureTask.nextTaskId.getAndIncrement();
        if (period == 0L) {
            throw new IllegalArgumentException("period: 0 (expected: != 0)");
        }
        this.deadlineNanos = nanoTime;
        this.periodNanos = period;
    }
    
    ScheduledFutureTask(final EventExecutor executor, final Callable<V> callable, final long nanoTime) {
        super(executor.unwrap(), callable);
        this.id = ScheduledFutureTask.nextTaskId.getAndIncrement();
        this.deadlineNanos = nanoTime;
        this.periodNanos = 0L;
    }
    
    public long deadlineNanos() {
        return this.deadlineNanos;
    }
    
    public long delayNanos() {
        return Math.max(0L, this.deadlineNanos() - nanoTime());
    }
    
    public long delayNanos(final long currentTimeNanos) {
        return Math.max(0L, this.deadlineNanos() - (currentTimeNanos - ScheduledFutureTask.START_TIME));
    }
    
    @Override
    public long getDelay(final TimeUnit unit) {
        return unit.convert(this.delayNanos(), TimeUnit.NANOSECONDS);
    }
    
    @Override
    public int compareTo(final Delayed o) {
        if (this == o) {
            return 0;
        }
        final ScheduledFutureTask<?> that = (ScheduledFutureTask<?>)o;
        final long d = this.deadlineNanos() - that.deadlineNanos();
        if (d < 0L) {
            return -1;
        }
        if (d > 0L) {
            return 1;
        }
        if (this.id < that.id) {
            return -1;
        }
        if (this.id == that.id) {
            throw new Error();
        }
        return 1;
    }
    
    @Override
    public void run() {
        assert this.executor().inEventLoop();
        try {
            if (this.isMigrationPending()) {
                this.scheduleWithNewExecutor();
            }
            else if (this.needsLaterExecution()) {
                if (!this.executor().isShutdown()) {
                    this.deadlineNanos = nanoTime() + TimeUnit.MICROSECONDS.toNanos(10L);
                    if (!this.isCancelled()) {
                        final Queue<ScheduledFutureTask<?>> scheduledTaskQueue = ((AbstractScheduledEventExecutor)this.executor()).scheduledTaskQueue;
                        assert scheduledTaskQueue != null;
                        scheduledTaskQueue.add(this);
                    }
                }
            }
            else if (this.periodNanos == 0L) {
                if (this.setUncancellableInternal()) {
                    final V result = this.task.call();
                    this.setSuccessInternal(result);
                }
            }
            else if (!this.isCancelled()) {
                this.task.call();
                if (!this.executor().isShutdown()) {
                    final long p = this.periodNanos;
                    if (p > 0L) {
                        this.deadlineNanos += p;
                    }
                    else {
                        this.deadlineNanos = nanoTime() - p;
                    }
                    if (!this.isCancelled()) {
                        final Queue<ScheduledFutureTask<?>> scheduledTaskQueue2 = ((AbstractScheduledEventExecutor)this.executor()).scheduledTaskQueue;
                        assert scheduledTaskQueue2 != null;
                        scheduledTaskQueue2.add(this);
                    }
                }
            }
        }
        catch (Throwable cause) {
            this.setFailureInternal(cause);
        }
    }
    
    @Override
    protected StringBuilder toStringBuilder() {
        final StringBuilder buf = super.toStringBuilder();
        buf.setCharAt(buf.length() - 1, ',');
        return buf.append(" id: ").append(this.id).append(", deadline: ").append(this.deadlineNanos).append(", period: ").append(this.periodNanos).append(')');
    }
    
    private boolean needsLaterExecution() {
        return this.task instanceof CallableEventExecutorAdapter && ((CallableEventExecutorAdapter)this.task).executor() instanceof PausableEventExecutor && !((PausableEventExecutor)((CallableEventExecutorAdapter)this.task).executor()).isAcceptingNewTasks();
    }
    
    private boolean isMigrationPending() {
        return !this.isCancelled() && this.task instanceof CallableEventExecutorAdapter && this.executor() != ((CallableEventExecutorAdapter)this.task).executor().unwrap();
    }
    
    private void scheduleWithNewExecutor() {
        final EventExecutor newExecutor = ((CallableEventExecutorAdapter)this.task).executor().unwrap();
        if (newExecutor instanceof SingleThreadEventExecutor) {
            if (!newExecutor.isShutdown()) {
                this.executor = newExecutor;
                final Queue<ScheduledFutureTask<?>> scheduledTaskQueue = ((SingleThreadEventExecutor)newExecutor).scheduledTaskQueue();
                this.executor.execute(new OneTimeTask() {
                    @Override
                    public void run() {
                        ScheduledFutureTask.this.deadlineNanos = ScheduledFutureTask.nanoTime();
                        if (!ScheduledFutureTask.this.isCancelled()) {
                            scheduledTaskQueue.add(ScheduledFutureTask.this);
                        }
                    }
                });
            }
            return;
        }
        throw new UnsupportedOperationException("task migration unsupported");
    }
    
    static {
        nextTaskId = new AtomicLong();
        START_TIME = System.nanoTime();
    }
}
