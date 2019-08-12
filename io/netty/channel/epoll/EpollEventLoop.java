// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.channel.epoll;

import io.netty.util.internal.logging.InternalLoggerFactory;
import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;
import java.io.IOException;
import io.netty.util.internal.PlatformDependent;
import java.util.Queue;
import io.netty.util.collection.IntObjectHashMap;
import java.util.concurrent.Executor;
import io.netty.channel.EventLoopGroup;
import io.netty.util.collection.IntObjectMap;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.channel.SingleThreadEventLoop;

final class EpollEventLoop extends SingleThreadEventLoop
{
    private static final InternalLogger logger;
    private static final AtomicIntegerFieldUpdater<EpollEventLoop> WAKEN_UP_UPDATER;
    private final int epollFd;
    private final int eventFd;
    private final IntObjectMap<AbstractEpollChannel> channels;
    private final boolean allowGrowing;
    private final EpollEventArray events;
    private volatile int wakenUp;
    private volatile int ioRatio;
    
    EpollEventLoop(final EventLoopGroup parent, final Executor executor, final int maxEvents) {
        super(parent, executor, false);
        this.channels = new IntObjectHashMap<AbstractEpollChannel>(4096);
        this.ioRatio = 50;
        if (maxEvents == 0) {
            this.allowGrowing = true;
            this.events = new EpollEventArray(4096);
        }
        else {
            this.allowGrowing = false;
            this.events = new EpollEventArray(maxEvents);
        }
        boolean success = false;
        int epollFd = -1;
        int eventFd = -1;
        try {
            epollFd = (this.epollFd = Native.epollCreate());
            eventFd = (this.eventFd = Native.eventFd());
            Native.epollCtlAdd(epollFd, eventFd, Native.EPOLLIN);
            success = true;
        }
        finally {
            if (!success) {
                if (epollFd != -1) {
                    try {
                        Native.close(epollFd);
                    }
                    catch (Exception ex) {}
                }
                if (eventFd != -1) {
                    try {
                        Native.close(eventFd);
                    }
                    catch (Exception ex2) {}
                }
            }
        }
    }
    
    @Override
    protected void wakeup(final boolean inEventLoop) {
        if (!inEventLoop && EpollEventLoop.WAKEN_UP_UPDATER.compareAndSet(this, 0, 1)) {
            Native.eventFdWrite(this.eventFd, 1L);
        }
    }
    
    void add(final AbstractEpollChannel ch) {
        assert this.inEventLoop();
        final int fd = ch.fd().intValue();
        Native.epollCtlAdd(this.epollFd, fd, ch.flags);
        this.channels.put(fd, ch);
    }
    
    void modify(final AbstractEpollChannel ch) {
        assert this.inEventLoop();
        Native.epollCtlMod(this.epollFd, ch.fd().intValue(), ch.flags);
    }
    
    void remove(final AbstractEpollChannel ch) {
        assert this.inEventLoop();
        if (ch.isOpen()) {
            final int fd = ch.fd().intValue();
            if (this.channels.remove(fd) != null) {
                Native.epollCtlDel(this.epollFd, ch.fd().intValue());
            }
        }
    }
    
    @Override
    protected Queue<Runnable> newTaskQueue() {
        return PlatformDependent.newMpscQueue();
    }
    
    public int getIoRatio() {
        return this.ioRatio;
    }
    
    public void setIoRatio(final int ioRatio) {
        if (ioRatio <= 0 || ioRatio > 100) {
            throw new IllegalArgumentException("ioRatio: " + ioRatio + " (expected: 0 < ioRatio <= 100)");
        }
        this.ioRatio = ioRatio;
    }
    
    private int epollWait(final boolean oldWakenUp) throws IOException {
        int selectCnt = 0;
        long currentTimeNanos = System.nanoTime();
        final long selectDeadLineNanos = currentTimeNanos + this.delayNanos(currentTimeNanos);
        while (true) {
            final long timeoutMillis = (selectDeadLineNanos - currentTimeNanos + 500000L) / 1000000L;
            if (timeoutMillis <= 0L) {
                if (selectCnt == 0) {
                    final int ready = Native.epollWait(this.epollFd, this.events, 0);
                    if (ready > 0) {
                        return ready;
                    }
                }
                return 0;
            }
            final int selectedKeys = Native.epollWait(this.epollFd, this.events, (int)timeoutMillis);
            ++selectCnt;
            if (selectedKeys != 0 || oldWakenUp || this.wakenUp == 1 || this.hasTasks() || this.hasScheduledTasks()) {
                return selectedKeys;
            }
            currentTimeNanos = System.nanoTime();
        }
    }
    
    @Override
    protected void run() {
        final boolean oldWakenUp = EpollEventLoop.WAKEN_UP_UPDATER.getAndSet(this, 0) == 1;
        try {
            int ready;
            if (this.hasTasks()) {
                ready = Native.epollWait(this.epollFd, this.events, 0);
            }
            else {
                ready = this.epollWait(oldWakenUp);
                if (this.wakenUp == 1) {
                    Native.eventFdWrite(this.eventFd, 1L);
                }
            }
            final int ioRatio = this.ioRatio;
            if (ioRatio == 100) {
                if (ready > 0) {
                    this.processReady(this.events, ready);
                }
                this.runAllTasks();
            }
            else {
                final long ioStartTime = System.nanoTime();
                if (ready > 0) {
                    this.processReady(this.events, ready);
                }
                final long ioTime = System.nanoTime() - ioStartTime;
                this.runAllTasks(ioTime * (100 - ioRatio) / ioRatio);
            }
            if (this.allowGrowing && ready == this.events.length()) {
                this.events.increase();
            }
            if (this.isShuttingDown()) {
                this.closeAll();
                if (this.confirmShutdown()) {
                    this.cleanupAndTerminate(true);
                    return;
                }
            }
        }
        catch (Throwable t) {
            EpollEventLoop.logger.warn("Unexpected exception in the selector loop.", t);
            try {
                Thread.sleep(1000L);
            }
            catch (InterruptedException ex) {}
        }
        this.scheduleExecution();
    }
    
    private void closeAll() {
        try {
            Native.epollWait(this.epollFd, this.events, 0);
        }
        catch (IOException ex) {}
        final Collection<AbstractEpollChannel> array = new ArrayList<AbstractEpollChannel>(this.channels.size());
        for (final IntObjectMap.Entry<AbstractEpollChannel> entry : this.channels.entries()) {
            array.add(entry.value());
        }
        for (final AbstractEpollChannel ch : array) {
            ch.unsafe().close(ch.unsafe().voidPromise());
        }
    }
    
    private void processReady(final EpollEventArray events, final int ready) {
        for (int i = 0; i < ready; ++i) {
            final int fd = events.fd(i);
            if (fd == this.eventFd) {
                Native.eventFdRead(this.eventFd);
            }
            else {
                final long ev = events.events(i);
                final AbstractEpollChannel ch = this.channels.get(fd);
                if (ch != null && ch.isOpen()) {
                    final boolean close = (ev & (long)Native.EPOLLRDHUP) != 0x0L;
                    final boolean read = (ev & (long)Native.EPOLLIN) != 0x0L;
                    final boolean write = (ev & (long)Native.EPOLLOUT) != 0x0L;
                    final AbstractEpollChannel.AbstractEpollUnsafe unsafe = (AbstractEpollChannel.AbstractEpollUnsafe)ch.unsafe();
                    if (close) {
                        unsafe.epollRdHupReady();
                    }
                    if (write && ch.isOpen()) {
                        unsafe.epollOutReady();
                    }
                    if (read && ch.isOpen()) {
                        unsafe.epollInReady();
                    }
                }
                else {
                    Native.epollCtlDel(this.epollFd, fd);
                }
            }
        }
    }
    
    @Override
    protected void cleanup() {
        try {
            try {
                Native.close(this.epollFd);
            }
            catch (IOException e) {
                EpollEventLoop.logger.warn("Failed to close the epoll fd.", e);
            }
            try {
                Native.close(this.eventFd);
            }
            catch (IOException e) {
                EpollEventLoop.logger.warn("Failed to close the event fd.", e);
            }
        }
        finally {
            this.events.free();
        }
    }
    
    static {
        logger = InternalLoggerFactory.getInstance(EpollEventLoop.class);
        AtomicIntegerFieldUpdater<EpollEventLoop> updater = PlatformDependent.newAtomicIntegerFieldUpdater(EpollEventLoop.class, "wakenUp");
        if (updater == null) {
            updater = AtomicIntegerFieldUpdater.newUpdater(EpollEventLoop.class, "wakenUp");
        }
        WAKEN_UP_UPDATER = updater;
    }
}
