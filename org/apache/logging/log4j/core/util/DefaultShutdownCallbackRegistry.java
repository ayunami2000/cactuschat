// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.util;

import org.apache.logging.log4j.status.StatusLogger;
import java.lang.ref.WeakReference;
import java.lang.ref.SoftReference;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.lang.ref.Reference;
import java.util.Collection;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.logging.log4j.Logger;
import java.io.Serializable;
import org.apache.logging.log4j.core.LifeCycle;

public class DefaultShutdownCallbackRegistry implements ShutdownCallbackRegistry, LifeCycle, Runnable, Serializable
{
    protected static final Logger LOGGER;
    private static final long serialVersionUID = 1L;
    private final AtomicReference<State> state;
    private final ThreadFactory threadFactory;
    private final Collection<Cancellable> hooks;
    private Reference<Thread> shutdownHookRef;
    
    public DefaultShutdownCallbackRegistry() {
        this(Executors.defaultThreadFactory());
    }
    
    protected DefaultShutdownCallbackRegistry(final ThreadFactory threadFactory) {
        this.state = new AtomicReference<State>(State.INITIALIZED);
        this.hooks = new CopyOnWriteArrayList<Cancellable>();
        this.threadFactory = threadFactory;
    }
    
    @Override
    public void run() {
        if (this.state.compareAndSet(State.STARTED, State.STOPPING)) {
            for (final Runnable hook : this.hooks) {
                try {
                    hook.run();
                }
                catch (Throwable t) {
                    DefaultShutdownCallbackRegistry.LOGGER.error(DefaultShutdownCallbackRegistry.SHUTDOWN_HOOK_MARKER, "Caught exception executing shutdown hook {}", new Object[] { hook, t });
                }
            }
            this.state.set(State.STOPPED);
        }
    }
    
    @Override
    public Cancellable addShutdownCallback(final Runnable callback) {
        if (this.isStarted()) {
            final Cancellable receipt = new Cancellable() {
                private final Reference<Runnable> hook = new SoftReference<Runnable>(callback);
                
                @Override
                public void cancel() {
                    this.hook.clear();
                    DefaultShutdownCallbackRegistry.this.hooks.remove(this);
                }
                
                @Override
                public void run() {
                    final Runnable runnableHook = this.hook.get();
                    if (runnableHook != null) {
                        runnableHook.run();
                        this.hook.clear();
                    }
                }
                
                @Override
                public String toString() {
                    return String.valueOf(this.hook.get());
                }
            };
            this.hooks.add(receipt);
            return receipt;
        }
        throw new IllegalStateException("Cannot add new shutdown hook as this is not started. Current state: " + this.state.get().name());
    }
    
    @Override
    public void initialize() {
    }
    
    @Override
    public void start() {
        if (this.state.compareAndSet(State.INITIALIZED, State.STARTING)) {
            try {
                this.addShutdownHook(this.threadFactory.newThread(this));
                this.state.set(State.STARTED);
            }
            catch (Exception e) {
                DefaultShutdownCallbackRegistry.LOGGER.catching(e);
                this.state.set(State.STOPPED);
            }
        }
    }
    
    private void addShutdownHook(final Thread thread) {
        this.shutdownHookRef = new WeakReference<Thread>(thread);
        Runtime.getRuntime().addShutdownHook(thread);
    }
    
    @Override
    public void stop() {
        if (this.state.compareAndSet(State.STARTED, State.STOPPING)) {
            try {
                this.removeShutdownHook();
            }
            finally {
                this.state.set(State.STOPPED);
            }
        }
    }
    
    private void removeShutdownHook() {
        final Thread shutdownThread = this.shutdownHookRef.get();
        if (shutdownThread != null) {
            Runtime.getRuntime().removeShutdownHook(shutdownThread);
            this.shutdownHookRef.enqueue();
        }
    }
    
    @Override
    public State getState() {
        return this.state.get();
    }
    
    @Override
    public boolean isStarted() {
        return this.state.get() == State.STARTED;
    }
    
    @Override
    public boolean isStopped() {
        return this.state.get() == State.STOPPED;
    }
    
    static {
        LOGGER = StatusLogger.getLogger();
    }
}
