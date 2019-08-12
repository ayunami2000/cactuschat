// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.util.concurrent;

import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.Iterator;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.Executor;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import io.netty.util.internal.logging.InternalLogger;

public abstract class SingleThreadEventExecutor extends AbstractScheduledEventExecutor
{
    private static final InternalLogger logger;
    private static final int ST_NOT_STARTED = 1;
    private static final int ST_STARTED = 2;
    private static final int ST_SHUTTING_DOWN = 3;
    private static final int ST_SHUTDOWN = 4;
    private static final int ST_TERMINATED = 5;
    private static final Runnable WAKEUP_TASK;
    private static final AtomicIntegerFieldUpdater<SingleThreadEventExecutor> STATE_UPDATER;
    private static final AtomicReferenceFieldUpdater<SingleThreadEventExecutor, Thread> THREAD_UPDATER;
    private final Queue<Runnable> taskQueue;
    private volatile Thread thread;
    private final Executor executor;
    private final Semaphore threadLock;
    private final Set<Runnable> shutdownHooks;
    private final boolean addTaskWakesUp;
    private long lastExecutionTime;
    private volatile int state;
    private volatile long gracefulShutdownQuietPeriod;
    private volatile long gracefulShutdownTimeout;
    private long gracefulShutdownStartTime;
    private final Promise<?> terminationFuture;
    private boolean firstRun;
    private final Runnable asRunnable;
    private static final long SCHEDULE_PURGE_INTERVAL;
    
    protected SingleThreadEventExecutor(final EventExecutorGroup parent, final Executor executor, final boolean addTaskWakesUp) {
        super(parent);
        this.threadLock = new Semaphore(0);
        this.shutdownHooks = new LinkedHashSet<Runnable>();
        this.state = 1;
        this.terminationFuture = new DefaultPromise<Object>(GlobalEventExecutor.INSTANCE);
        this.firstRun = true;
        this.asRunnable = new Runnable() {
            @Override
            public void run() {
                SingleThreadEventExecutor.this.updateThread(Thread.currentThread());
                if (SingleThreadEventExecutor.this.firstRun) {
                    SingleThreadEventExecutor.this.firstRun = false;
                    SingleThreadEventExecutor.this.updateLastExecutionTime();
                }
                try {
                    SingleThreadEventExecutor.this.run();
                }
                catch (Throwable t) {
                    SingleThreadEventExecutor.logger.warn("Unexpected exception from an event executor: ", t);
                    SingleThreadEventExecutor.this.cleanupAndTerminate(false);
                }
            }
        };
        if (executor == null) {
            throw new NullPointerException("executor");
        }
        this.addTaskWakesUp = addTaskWakesUp;
        this.executor = executor;
        this.taskQueue = this.newTaskQueue();
    }
    
    protected Queue<Runnable> newTaskQueue() {
        return new LinkedBlockingQueue<Runnable>();
    }
    
    protected Runnable pollTask() {
        assert this.inEventLoop();
        Runnable task;
        do {
            task = this.taskQueue.poll();
        } while (task == SingleThreadEventExecutor.WAKEUP_TASK);
        return task;
    }
    
    protected Runnable takeTask() {
        assert this.inEventLoop();
        if (!(this.taskQueue instanceof BlockingQueue)) {
            throw new UnsupportedOperationException();
        }
        final BlockingQueue<Runnable> taskQueue = (BlockingQueue<Runnable>)(BlockingQueue)this.taskQueue;
        while (true) {
            final ScheduledFutureTask<?> scheduledTask = this.peekScheduledTask();
            if (scheduledTask == null) {
                Runnable task = null;
                try {
                    task = taskQueue.take();
                    if (task == SingleThreadEventExecutor.WAKEUP_TASK) {
                        task = null;
                    }
                }
                catch (InterruptedException ex) {}
                return task;
            }
            final long delayNanos = scheduledTask.delayNanos();
            Runnable task2 = null;
            if (delayNanos > 0L) {
                try {
                    task2 = taskQueue.poll(delayNanos, TimeUnit.NANOSECONDS);
                }
                catch (InterruptedException e) {
                    return null;
                }
            }
            if (task2 == null) {
                this.fetchFromScheduledTaskQueue();
                task2 = taskQueue.poll();
            }
            if (task2 != null) {
                return task2;
            }
        }
    }
    
    private void fetchFromScheduledTaskQueue() {
        if (this.hasScheduledTasks()) {
            final long nanoTime = AbstractScheduledEventExecutor.nanoTime();
            while (true) {
                final Runnable scheduledTask = this.pollScheduledTask(nanoTime);
                if (scheduledTask == null) {
                    break;
                }
                this.taskQueue.add(scheduledTask);
            }
        }
    }
    
    protected Runnable peekTask() {
        assert this.inEventLoop();
        return this.taskQueue.peek();
    }
    
    protected boolean hasTasks() {
        assert this.inEventLoop();
        return !this.taskQueue.isEmpty();
    }
    
    public final int pendingTasks() {
        return this.taskQueue.size();
    }
    
    protected void addTask(final Runnable task) {
        if (task == null) {
            throw new NullPointerException("task");
        }
        if (this.isShutdown()) {
            reject();
        }
        this.taskQueue.add(task);
    }
    
    protected boolean removeTask(final Runnable task) {
        if (task == null) {
            throw new NullPointerException("task");
        }
        return this.taskQueue.remove(task);
    }
    
    protected boolean runAllTasks() {
        this.fetchFromScheduledTaskQueue();
        Runnable task = this.pollTask();
        if (task == null) {
            return false;
        }
        do {
            try {
                task.run();
            }
            catch (Throwable t) {
                SingleThreadEventExecutor.logger.warn("A task raised an exception.", t);
            }
            task = this.pollTask();
        } while (task != null);
        this.lastExecutionTime = ScheduledFutureTask.nanoTime();
        return true;
    }
    
    protected boolean runAllTasks(final long timeoutNanos) {
        this.fetchFromScheduledTaskQueue();
        Runnable task = this.pollTask();
        if (task == null) {
            return false;
        }
        final long deadline = ScheduledFutureTask.nanoTime() + timeoutNanos;
        long runTasks = 0L;
        long lastExecutionTime;
        while (true) {
            try {
                task.run();
            }
            catch (Throwable t) {
                SingleThreadEventExecutor.logger.warn("A task raised an exception.", t);
            }
            ++runTasks;
            if ((runTasks & 0x3FL) == 0x0L) {
                lastExecutionTime = ScheduledFutureTask.nanoTime();
                if (lastExecutionTime >= deadline) {
                    break;
                }
            }
            task = this.pollTask();
            if (task == null) {
                lastExecutionTime = ScheduledFutureTask.nanoTime();
                break;
            }
        }
        this.lastExecutionTime = lastExecutionTime;
        return true;
    }
    
    protected long delayNanos(final long currentTimeNanos) {
        final ScheduledFutureTask<?> scheduledTask = this.peekScheduledTask();
        if (scheduledTask == null) {
            return SingleThreadEventExecutor.SCHEDULE_PURGE_INTERVAL;
        }
        return scheduledTask.delayNanos(currentTimeNanos);
    }
    
    protected void updateLastExecutionTime() {
        this.lastExecutionTime = ScheduledFutureTask.nanoTime();
    }
    
    protected abstract void run();
    
    protected void cleanup() {
    }
    
    protected void wakeup(final boolean inEventLoop) {
        if (!inEventLoop || SingleThreadEventExecutor.STATE_UPDATER.get(this) == 3) {
            this.taskQueue.add(SingleThreadEventExecutor.WAKEUP_TASK);
        }
    }
    
    @Override
    public boolean inEventLoop(final Thread thread) {
        return thread == this.thread;
    }
    
    public void addShutdownHook(final Runnable task) {
        if (this.inEventLoop()) {
            this.shutdownHooks.add(task);
        }
        else {
            this.execute(new Runnable() {
                @Override
                public void run() {
                    SingleThreadEventExecutor.this.shutdownHooks.add(task);
                }
            });
        }
    }
    
    public void removeShutdownHook(final Runnable task) {
        if (this.inEventLoop()) {
            this.shutdownHooks.remove(task);
        }
        else {
            this.execute(new Runnable() {
                @Override
                public void run() {
                    SingleThreadEventExecutor.this.shutdownHooks.remove(task);
                }
            });
        }
    }
    
    private boolean runShutdownHooks() {
        boolean ran = false;
        while (!this.shutdownHooks.isEmpty()) {
            final List<Runnable> copy = new ArrayList<Runnable>(this.shutdownHooks);
            this.shutdownHooks.clear();
            for (final Runnable task : copy) {
                try {
                    task.run();
                }
                catch (Throwable t) {
                    SingleThreadEventExecutor.logger.warn("Shutdown hook raised an exception.", t);
                }
                finally {
                    ran = true;
                }
            }
        }
        if (ran) {
            this.lastExecutionTime = ScheduledFutureTask.nanoTime();
        }
        return ran;
    }
    
    @Override
    public Future<?> shutdownGracefully(final long quietPeriod, final long timeout, final TimeUnit unit) {
        if (quietPeriod < 0L) {
            throw new IllegalArgumentException("quietPeriod: " + quietPeriod + " (expected >= 0)");
        }
        if (timeout < quietPeriod) {
            throw new IllegalArgumentException("timeout: " + timeout + " (expected >= quietPeriod (" + quietPeriod + "))");
        }
        if (unit == null) {
            throw new NullPointerException("unit");
        }
        if (this.isShuttingDown()) {
            return this.terminationFuture();
        }
        final boolean inEventLoop = this.inEventLoop();
        while (!this.isShuttingDown()) {
            boolean wakeup = true;
            final int oldState = SingleThreadEventExecutor.STATE_UPDATER.get(this);
            int newState = 0;
            if (inEventLoop) {
                newState = 3;
            }
            else {
                switch (oldState) {
                    case 1:
                    case 2: {
                        newState = 3;
                        break;
                    }
                    default: {
                        newState = oldState;
                        wakeup = false;
                        break;
                    }
                }
            }
            if (SingleThreadEventExecutor.STATE_UPDATER.compareAndSet(this, oldState, newState)) {
                this.gracefulShutdownQuietPeriod = unit.toNanos(quietPeriod);
                this.gracefulShutdownTimeout = unit.toNanos(timeout);
                if (oldState == 1) {
                    this.scheduleExecution();
                }
                if (wakeup) {
                    this.wakeup(inEventLoop);
                }
                return this.terminationFuture();
            }
        }
        return this.terminationFuture();
    }
    
    @Override
    public Future<?> terminationFuture() {
        return this.terminationFuture;
    }
    
    @Deprecated
    @Override
    public void shutdown() {
        if (this.isShutdown()) {
            return;
        }
        final boolean inEventLoop = this.inEventLoop();
        while (!this.isShuttingDown()) {
            boolean wakeup = true;
            final int oldState = SingleThreadEventExecutor.STATE_UPDATER.get(this);
            int newState = 0;
            if (inEventLoop) {
                newState = 4;
            }
            else {
                switch (oldState) {
                    case 1:
                    case 2:
                    case 3: {
                        newState = 4;
                        break;
                    }
                    default: {
                        newState = oldState;
                        wakeup = false;
                        break;
                    }
                }
            }
            if (SingleThreadEventExecutor.STATE_UPDATER.compareAndSet(this, oldState, newState)) {
                if (oldState == 1) {
                    this.scheduleExecution();
                }
                if (wakeup) {
                    this.wakeup(inEventLoop);
                }
            }
        }
    }
    
    @Override
    public boolean isShuttingDown() {
        return SingleThreadEventExecutor.STATE_UPDATER.get(this) >= 3;
    }
    
    @Override
    public boolean isShutdown() {
        return SingleThreadEventExecutor.STATE_UPDATER.get(this) >= 4;
    }
    
    @Override
    public boolean isTerminated() {
        return SingleThreadEventExecutor.STATE_UPDATER.get(this) == 5;
    }
    
    protected boolean confirmShutdown() {
        if (!this.isShuttingDown()) {
            return false;
        }
        if (!this.inEventLoop()) {
            throw new IllegalStateException("must be invoked from an event loop");
        }
        this.cancelScheduledTasks();
        if (this.gracefulShutdownStartTime == 0L) {
            this.gracefulShutdownStartTime = ScheduledFutureTask.nanoTime();
        }
        if (this.runAllTasks() || this.runShutdownHooks()) {
            if (this.isShutdown()) {
                return true;
            }
            this.wakeup(true);
            return false;
        }
        else {
            final long nanoTime = ScheduledFutureTask.nanoTime();
            if (this.isShutdown() || nanoTime - this.gracefulShutdownStartTime > this.gracefulShutdownTimeout) {
                return true;
            }
            if (nanoTime - this.lastExecutionTime <= this.gracefulShutdownQuietPeriod) {
                this.wakeup(true);
                try {
                    Thread.sleep(100L);
                }
                catch (InterruptedException ex) {}
                return false;
            }
            return true;
        }
    }
    
    @Override
    public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
        if (unit == null) {
            throw new NullPointerException("unit");
        }
        if (this.inEventLoop()) {
            throw new IllegalStateException("cannot await termination of the current thread");
        }
        if (this.threadLock.tryAcquire(timeout, unit)) {
            this.threadLock.release();
        }
        return this.isTerminated();
    }
    
    @Override
    public void execute(final Runnable task) {
        if (task == null) {
            throw new NullPointerException("task");
        }
        final boolean inEventLoop = this.inEventLoop();
        if (inEventLoop) {
            this.addTask(task);
        }
        else {
            this.startExecution();
            this.addTask(task);
            if (this.isShutdown() && this.removeTask(task)) {
                reject();
            }
        }
        if (!this.addTaskWakesUp && this.wakesUpForTask(task)) {
            this.wakeup(inEventLoop);
        }
    }
    
    protected boolean wakesUpForTask(final Runnable task) {
        return true;
    }
    
    protected static void reject() {
        throw new RejectedExecutionException("event executor terminated");
    }
    
    protected void cleanupAndTerminate(final boolean success) {
        int oldState;
        do {
            oldState = SingleThreadEventExecutor.STATE_UPDATER.get(this);
        } while (oldState < 3 && !SingleThreadEventExecutor.STATE_UPDATER.compareAndSet(this, oldState, 3));
        if (success && this.gracefulShutdownStartTime == 0L) {
            SingleThreadEventExecutor.logger.error("Buggy " + EventExecutor.class.getSimpleName() + " implementation; " + SingleThreadEventExecutor.class.getSimpleName() + ".confirmShutdown() must be called " + "before run() implementation terminates.");
        }
        try {
            while (!this.confirmShutdown()) {}
        }
        finally {
            try {
                this.cleanup();
            }
            finally {
                SingleThreadEventExecutor.STATE_UPDATER.set(this, 5);
                this.threadLock.release();
                if (!this.taskQueue.isEmpty()) {
                    SingleThreadEventExecutor.logger.warn("An event executor terminated with non-empty task queue (" + this.taskQueue.size() + ')');
                }
                this.firstRun = true;
                this.terminationFuture.setSuccess(null);
            }
        }
    }
    
    private void startExecution() {
        if (SingleThreadEventExecutor.STATE_UPDATER.get(this) == 1 && SingleThreadEventExecutor.STATE_UPDATER.compareAndSet(this, 1, 2)) {
            this.schedule(new ScheduledFutureTask<Object>(this, (Callable<Object>)Executors.callable(new PurgeTask(), (V)null), ScheduledFutureTask.deadlineNanos(SingleThreadEventExecutor.SCHEDULE_PURGE_INTERVAL), -SingleThreadEventExecutor.SCHEDULE_PURGE_INTERVAL));
            this.scheduleExecution();
        }
    }
    
    protected final void scheduleExecution() {
        this.updateThread(null);
        this.executor.execute(this.asRunnable);
    }
    
    private void updateThread(final Thread t) {
        SingleThreadEventExecutor.THREAD_UPDATER.lazySet(this, t);
    }
    
    static {
        logger = InternalLoggerFactory.getInstance(SingleThreadEventExecutor.class);
        WAKEUP_TASK = new Runnable() {
            @Override
            public void run() {
            }
        };
        AtomicIntegerFieldUpdater<SingleThreadEventExecutor> updater = PlatformDependent.newAtomicIntegerFieldUpdater(SingleThreadEventExecutor.class, "state");
        if (updater == null) {
            updater = AtomicIntegerFieldUpdater.newUpdater(SingleThreadEventExecutor.class, "state");
        }
        STATE_UPDATER = updater;
        AtomicReferenceFieldUpdater<SingleThreadEventExecutor, Thread> refUpdater = PlatformDependent.newAtomicReferenceFieldUpdater(SingleThreadEventExecutor.class, "thread");
        if (refUpdater == null) {
            refUpdater = AtomicReferenceFieldUpdater.newUpdater(SingleThreadEventExecutor.class, Thread.class, "thread");
        }
        THREAD_UPDATER = refUpdater;
        SCHEDULE_PURGE_INTERVAL = TimeUnit.SECONDS.toNanos(1L);
    }
    
    private final class PurgeTask implements Runnable
    {
        @Override
        public void run() {
            SingleThreadEventExecutor.this.purgeCancelledScheduledTasks();
        }
    }
}
