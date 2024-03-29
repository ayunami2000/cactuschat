// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.util.concurrent;

import io.netty.util.internal.CallableEventExecutorAdapter;
import java.util.concurrent.Executors;
import io.netty.util.internal.RunnableEventExecutorAdapter;
import java.util.Iterator;
import java.util.concurrent.Callable;
import io.netty.util.internal.ObjectUtil;
import java.util.concurrent.TimeUnit;
import java.util.PriorityQueue;
import java.util.Queue;

public abstract class AbstractScheduledEventExecutor extends AbstractEventExecutor
{
    Queue<ScheduledFutureTask<?>> scheduledTaskQueue;
    
    protected AbstractScheduledEventExecutor() {
    }
    
    protected AbstractScheduledEventExecutor(final EventExecutorGroup parent) {
        super(parent);
    }
    
    protected static long nanoTime() {
        return ScheduledFutureTask.nanoTime();
    }
    
    Queue<ScheduledFutureTask<?>> scheduledTaskQueue() {
        if (this.scheduledTaskQueue == null) {
            this.scheduledTaskQueue = new PriorityQueue<ScheduledFutureTask<?>>();
        }
        return this.scheduledTaskQueue;
    }
    
    private static boolean isNullOrEmpty(final Queue<ScheduledFutureTask<?>> queue) {
        return queue == null || queue.isEmpty();
    }
    
    protected void cancelScheduledTasks() {
        assert this.inEventLoop();
        final Queue<ScheduledFutureTask<?>> scheduledTaskQueue = this.scheduledTaskQueue;
        if (isNullOrEmpty(scheduledTaskQueue)) {
            return;
        }
        final ScheduledFutureTask[] arr$;
        final ScheduledFutureTask<?>[] scheduledTasks = (ScheduledFutureTask<?>[])(arr$ = scheduledTaskQueue.toArray(new ScheduledFutureTask[scheduledTaskQueue.size()]));
        for (final ScheduledFutureTask<?> task : arr$) {
            task.cancel(false);
        }
        scheduledTaskQueue.clear();
    }
    
    protected final Runnable pollScheduledTask() {
        return this.pollScheduledTask(nanoTime());
    }
    
    protected final Runnable pollScheduledTask(final long nanoTime) {
        assert this.inEventLoop();
        final Queue<ScheduledFutureTask<?>> scheduledTaskQueue = this.scheduledTaskQueue;
        final ScheduledFutureTask<?> scheduledTask = (scheduledTaskQueue == null) ? null : scheduledTaskQueue.peek();
        if (scheduledTask == null) {
            return null;
        }
        if (scheduledTask.deadlineNanos() <= nanoTime) {
            scheduledTaskQueue.remove();
            return scheduledTask;
        }
        return null;
    }
    
    protected final long nextScheduledTaskNano() {
        final Queue<ScheduledFutureTask<?>> scheduledTaskQueue = this.scheduledTaskQueue;
        final ScheduledFutureTask<?> scheduledTask = (scheduledTaskQueue == null) ? null : scheduledTaskQueue.peek();
        if (scheduledTask == null) {
            return -1L;
        }
        return Math.max(0L, scheduledTask.deadlineNanos() - nanoTime());
    }
    
    final ScheduledFutureTask<?> peekScheduledTask() {
        final Queue<ScheduledFutureTask<?>> scheduledTaskQueue = this.scheduledTaskQueue;
        if (scheduledTaskQueue == null) {
            return null;
        }
        return scheduledTaskQueue.peek();
    }
    
    protected final boolean hasScheduledTasks() {
        final Queue<ScheduledFutureTask<?>> scheduledTaskQueue = this.scheduledTaskQueue;
        final ScheduledFutureTask<?> scheduledTask = (scheduledTaskQueue == null) ? null : scheduledTaskQueue.peek();
        return scheduledTask != null && scheduledTask.deadlineNanos() <= nanoTime();
    }
    
    @Override
    public ScheduledFuture<?> schedule(final Runnable command, final long delay, final TimeUnit unit) {
        ObjectUtil.checkNotNull(command, "command");
        ObjectUtil.checkNotNull(unit, "unit");
        if (delay < 0L) {
            throw new IllegalArgumentException(String.format("delay: %d (expected: >= 0)", delay));
        }
        return this.schedule((ScheduledFutureTask<?>)new ScheduledFutureTask<Object>(this, toCallable(command), ScheduledFutureTask.deadlineNanos(unit.toNanos(delay))));
    }
    
    @Override
    public <V> ScheduledFuture<V> schedule(final Callable<V> callable, final long delay, final TimeUnit unit) {
        ObjectUtil.checkNotNull(callable, "callable");
        ObjectUtil.checkNotNull(unit, "unit");
        if (delay < 0L) {
            throw new IllegalArgumentException(String.format("delay: %d (expected: >= 0)", delay));
        }
        return this.schedule(new ScheduledFutureTask<V>(this, callable, ScheduledFutureTask.deadlineNanos(unit.toNanos(delay))));
    }
    
    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(final Runnable command, final long initialDelay, final long period, final TimeUnit unit) {
        ObjectUtil.checkNotNull(command, "command");
        ObjectUtil.checkNotNull(unit, "unit");
        if (initialDelay < 0L) {
            throw new IllegalArgumentException(String.format("initialDelay: %d (expected: >= 0)", initialDelay));
        }
        if (period <= 0L) {
            throw new IllegalArgumentException(String.format("period: %d (expected: > 0)", period));
        }
        return this.schedule((ScheduledFutureTask<?>)new ScheduledFutureTask<Object>(this, toCallable(command), ScheduledFutureTask.deadlineNanos(unit.toNanos(initialDelay)), unit.toNanos(period)));
    }
    
    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(final Runnable command, final long initialDelay, final long delay, final TimeUnit unit) {
        ObjectUtil.checkNotNull(command, "command");
        ObjectUtil.checkNotNull(unit, "unit");
        if (initialDelay < 0L) {
            throw new IllegalArgumentException(String.format("initialDelay: %d (expected: >= 0)", initialDelay));
        }
        if (delay <= 0L) {
            throw new IllegalArgumentException(String.format("delay: %d (expected: > 0)", delay));
        }
        return this.schedule((ScheduledFutureTask<?>)new ScheduledFutureTask<Object>(this, toCallable(command), ScheduledFutureTask.deadlineNanos(unit.toNanos(initialDelay)), -unit.toNanos(delay)));
    }
    
     <V> ScheduledFuture<V> schedule(final ScheduledFutureTask<V> task) {
        if (this.inEventLoop()) {
            this.scheduledTaskQueue().add(task);
        }
        else {
            this.execute(new Runnable() {
                @Override
                public void run() {
                    AbstractScheduledEventExecutor.this.scheduledTaskQueue().add(task);
                }
            });
        }
        return task;
    }
    
    void purgeCancelledScheduledTasks() {
        final Queue<ScheduledFutureTask<?>> scheduledTaskQueue = this.scheduledTaskQueue;
        if (isNullOrEmpty(scheduledTaskQueue)) {
            return;
        }
        final Iterator<ScheduledFutureTask<?>> i = scheduledTaskQueue.iterator();
        while (i.hasNext()) {
            final ScheduledFutureTask<?> task = i.next();
            if (task.isCancelled()) {
                i.remove();
            }
        }
    }
    
    private static Callable<Void> toCallable(final Runnable command) {
        if (command instanceof RunnableEventExecutorAdapter) {
            return new RunnableToCallableAdapter((RunnableEventExecutorAdapter)command);
        }
        return Executors.callable(command, (Void)null);
    }
    
    private static class RunnableToCallableAdapter implements CallableEventExecutorAdapter<Void>
    {
        final RunnableEventExecutorAdapter runnable;
        
        RunnableToCallableAdapter(final RunnableEventExecutorAdapter runnable) {
            this.runnable = runnable;
        }
        
        @Override
        public EventExecutor executor() {
            return this.runnable.executor();
        }
        
        @Override
        public Callable<Void> unwrap() {
            return null;
        }
        
        @Override
        public Void call() throws Exception {
            this.runnable.run();
            return null;
        }
    }
}
