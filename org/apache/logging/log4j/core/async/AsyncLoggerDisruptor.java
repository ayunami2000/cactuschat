// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.async;

import com.lmax.disruptor.EventTranslator;
import org.apache.logging.log4j.core.jmx.RingBufferAdmin;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.ExceptionHandler;
import java.util.concurrent.Executor;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.dsl.ProducerType;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import com.lmax.disruptor.dsl.Disruptor;
import org.apache.logging.log4j.status.StatusLogger;

class AsyncLoggerDisruptor
{
    private static final int SLEEP_MILLIS_BETWEEN_DRAIN_ATTEMPTS = 50;
    private static final int MAX_DRAIN_ATTEMPTS_BEFORE_SHUTDOWN = 200;
    private static final StatusLogger LOGGER;
    private volatile Disruptor<RingBufferLogEvent> disruptor;
    private ExecutorService executor;
    private String contextName;
    private boolean useThreadLocalTranslator;
    private long backgroundThreadId;
    
    AsyncLoggerDisruptor(final String contextName) {
        this.contextName = contextName;
    }
    
    public String getContextName() {
        return this.contextName;
    }
    
    public void setContextName(final String name) {
        this.contextName = name;
    }
    
    Disruptor<RingBufferLogEvent> getDisruptor() {
        return this.disruptor;
    }
    
    synchronized void start() {
        if (this.disruptor != null) {
            AsyncLoggerDisruptor.LOGGER.trace("[{}] AsyncLoggerDisruptor not starting new disruptor for this context, using existing object.", new Object[] { this.contextName });
            return;
        }
        AsyncLoggerDisruptor.LOGGER.trace("[{}] AsyncLoggerDisruptor creating new disruptor for this context.", new Object[] { this.contextName });
        final int ringBufferSize = DisruptorUtil.calculateRingBufferSize("AsyncLogger.RingBufferSize");
        final WaitStrategy waitStrategy = DisruptorUtil.createWaitStrategy("AsyncLogger.WaitStrategy");
        this.executor = Executors.newSingleThreadExecutor(new DaemonThreadFactory("AsyncLogger[" + this.contextName + "]"));
        this.backgroundThreadId = DisruptorUtil.getExecutorThreadId(this.executor);
        this.disruptor = (Disruptor<RingBufferLogEvent>)new Disruptor((EventFactory)RingBufferLogEvent.FACTORY, ringBufferSize, (Executor)this.executor, ProducerType.MULTI, waitStrategy);
        final ExceptionHandler<RingBufferLogEvent> errorHandler = DisruptorUtil.getExceptionHandler("AsyncLogger.ExceptionHandler", RingBufferLogEvent.class);
        this.disruptor.handleExceptionsWith((ExceptionHandler)errorHandler);
        final RingBufferLogEventHandler[] handlers = { new RingBufferLogEventHandler() };
        this.disruptor.handleEventsWith((EventHandler[])handlers);
        AsyncLoggerDisruptor.LOGGER.debug("[{}] Starting AsyncLogger disruptor for this context with ringbufferSize={}, waitStrategy={}, exceptionHandler={}...", new Object[] { this.contextName, this.disruptor.getRingBuffer().getBufferSize(), waitStrategy.getClass().getSimpleName(), errorHandler });
        this.disruptor.start();
        AsyncLoggerDisruptor.LOGGER.trace("[{}] AsyncLoggers use a {} translator", new Object[] { this.contextName, this.useThreadLocalTranslator ? "threadlocal" : "vararg" });
    }
    
    synchronized void stop() {
        final Disruptor<RingBufferLogEvent> temp = this.getDisruptor();
        if (temp == null) {
            AsyncLoggerDisruptor.LOGGER.trace("[{}] AsyncLoggerDisruptor: disruptor for this context already shut down.", new Object[] { this.contextName });
            return;
        }
        AsyncLoggerDisruptor.LOGGER.debug("[{}] AsyncLoggerDisruptor: shutting down disruptor for this context.", new Object[] { this.contextName });
        this.disruptor = null;
        for (int i = 0; hasBacklog(temp) && i < 200; ++i) {
            try {
                Thread.sleep(50L);
            }
            catch (InterruptedException ex) {}
        }
        temp.shutdown();
        AsyncLoggerDisruptor.LOGGER.trace("[{}] AsyncLoggerDisruptor: shutting down disruptor executor.", new Object[] { this.contextName });
        this.executor.shutdown();
        this.executor = null;
    }
    
    private static boolean hasBacklog(final Disruptor<?> theDisruptor) {
        final RingBuffer<?> ringBuffer = (RingBuffer<?>)theDisruptor.getRingBuffer();
        return !ringBuffer.hasAvailableCapacity(ringBuffer.getBufferSize());
    }
    
    public RingBufferAdmin createRingBufferAdmin(final String jmxContextName) {
        final RingBuffer<RingBufferLogEvent> ring = (RingBuffer<RingBufferLogEvent>)((this.disruptor == null) ? null : this.disruptor.getRingBuffer());
        return RingBufferAdmin.forAsyncLogger(ring, jmxContextName);
    }
    
    boolean shouldLogInCurrentThread() {
        return this.currentThreadIsAppenderThread() && this.isRingBufferFull();
    }
    
    private boolean currentThreadIsAppenderThread() {
        return Thread.currentThread().getId() == this.backgroundThreadId;
    }
    
    private boolean isRingBufferFull() {
        final Disruptor<RingBufferLogEvent> theDisruptor = this.disruptor;
        return theDisruptor == null || theDisruptor.getRingBuffer().remainingCapacity() == 0L;
    }
    
    void enqueueLogMessageInfo(final RingBufferLogEventTranslator translator) {
        try {
            this.disruptor.publishEvent((EventTranslator)translator);
        }
        catch (NullPointerException npe) {
            AsyncLoggerDisruptor.LOGGER.fatal("[{}] Ignoring log event after log4j was shut down.", new Object[] { this.contextName });
        }
    }
    
    public boolean isUseThreadLocals() {
        return this.useThreadLocalTranslator;
    }
    
    public void setUseThreadLocals(final boolean allow) {
        this.useThreadLocalTranslator = allow;
    }
    
    static {
        LOGGER = StatusLogger.getLogger();
    }
}
