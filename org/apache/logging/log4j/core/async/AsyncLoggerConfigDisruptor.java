// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.async;

import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.SequenceReportingEventHandler;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.core.jmx.RingBufferAdmin;
import org.apache.logging.log4j.core.util.Constants;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.ExceptionHandler;
import java.util.concurrent.Executor;
import com.lmax.disruptor.dsl.ProducerType;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import com.lmax.disruptor.dsl.Disruptor;
import java.util.concurrent.ThreadFactory;
import org.apache.logging.log4j.core.LogEvent;
import com.lmax.disruptor.EventTranslatorTwoArg;
import com.lmax.disruptor.EventFactory;
import org.apache.logging.log4j.Logger;

public class AsyncLoggerConfigDisruptor implements AsyncLoggerConfigDelegate
{
    private static final int MAX_DRAIN_ATTEMPTS_BEFORE_SHUTDOWN = 200;
    private static final int SLEEP_MILLIS_BETWEEN_DRAIN_ATTEMPTS = 50;
    private static final Logger LOGGER;
    private static final EventFactory<Log4jEventWrapper> FACTORY;
    private static final EventTranslatorTwoArg<Log4jEventWrapper, LogEvent, AsyncLoggerConfig> TRANSLATOR;
    private static final ThreadFactory THREAD_FACTORY;
    private volatile Disruptor<Log4jEventWrapper> disruptor;
    private ExecutorService executor;
    private long backgroundThreadId;
    
    public synchronized void start() {
        if (this.disruptor != null) {
            AsyncLoggerConfigDisruptor.LOGGER.trace("AsyncLoggerConfigHelper not starting new disruptor for this configuration, using existing object.");
            return;
        }
        AsyncLoggerConfigDisruptor.LOGGER.trace("AsyncLoggerConfigHelper creating new disruptor for this configuration.");
        final int ringBufferSize = DisruptorUtil.calculateRingBufferSize("AsyncLoggerConfig.RingBufferSize");
        final WaitStrategy waitStrategy = DisruptorUtil.createWaitStrategy("AsyncLoggerConfig.WaitStrategy");
        this.executor = Executors.newSingleThreadExecutor(AsyncLoggerConfigDisruptor.THREAD_FACTORY);
        this.backgroundThreadId = DisruptorUtil.getExecutorThreadId(this.executor);
        this.disruptor = (Disruptor<Log4jEventWrapper>)new Disruptor((EventFactory)AsyncLoggerConfigDisruptor.FACTORY, ringBufferSize, (Executor)this.executor, ProducerType.MULTI, waitStrategy);
        final ExceptionHandler<Log4jEventWrapper> errorHandler = DisruptorUtil.getExceptionHandler("AsyncLoggerConfig.ExceptionHandler", Log4jEventWrapper.class);
        this.disruptor.handleExceptionsWith((ExceptionHandler)errorHandler);
        final Log4jEventWrapperHandler[] handlers = { new Log4jEventWrapperHandler() };
        this.disruptor.handleEventsWith((EventHandler[])handlers);
        AsyncLoggerConfigDisruptor.LOGGER.debug("Starting AsyncLoggerConfig disruptor for this configuration with ringbufferSize={}, waitStrategy={}, exceptionHandler={}...", new Object[] { this.disruptor.getRingBuffer().getBufferSize(), waitStrategy.getClass().getSimpleName(), errorHandler });
        this.disruptor.start();
    }
    
    public synchronized void stop() {
        final Disruptor<Log4jEventWrapper> temp = this.disruptor;
        if (temp == null) {
            AsyncLoggerConfigDisruptor.LOGGER.trace("AsyncLoggerConfigHelper: disruptor for this configuration already shut down.");
            return;
        }
        AsyncLoggerConfigDisruptor.LOGGER.trace("AsyncLoggerConfigHelper: shutting down disruptor for this configuration.");
        this.disruptor = null;
        for (int i = 0; hasBacklog(temp) && i < 200; ++i) {
            try {
                Thread.sleep(50L);
            }
            catch (InterruptedException ex) {}
        }
        temp.shutdown();
        AsyncLoggerConfigDisruptor.LOGGER.trace("AsyncLoggerConfigHelper: shutting down disruptor executor for this configuration.");
        this.executor.shutdown();
        this.executor = null;
    }
    
    private static boolean hasBacklog(final Disruptor<?> theDisruptor) {
        final RingBuffer<?> ringBuffer = (RingBuffer<?>)theDisruptor.getRingBuffer();
        return !ringBuffer.hasAvailableCapacity(ringBuffer.getBufferSize());
    }
    
    @Override
    public boolean tryCallAppendersInBackground(final LogEvent event, final AsyncLoggerConfig asyncLoggerConfig) {
        final Disruptor<Log4jEventWrapper> temp = this.disruptor;
        if (!this.hasLog4jBeenShutDown(temp)) {
            if (this.isCalledFromAppenderThreadAndBufferFull(temp)) {
                return false;
            }
            this.enqueueEvent(event, asyncLoggerConfig);
        }
        return true;
    }
    
    private boolean hasLog4jBeenShutDown(final Disruptor<Log4jEventWrapper> aDisruptor) {
        if (aDisruptor == null) {
            AsyncLoggerConfigDisruptor.LOGGER.fatal("Ignoring log event after log4j was shut down");
            return true;
        }
        return false;
    }
    
    private void enqueueEvent(final LogEvent event, final AsyncLoggerConfig asyncLoggerConfig) {
        try {
            final LogEvent logEvent = this.prepareEvent(event);
            this.enqueue(logEvent, asyncLoggerConfig);
        }
        catch (NullPointerException npe) {
            AsyncLoggerConfigDisruptor.LOGGER.fatal("Ignoring log event after log4j was shut down.");
        }
    }
    
    private LogEvent prepareEvent(final LogEvent event) {
        final LogEvent logEvent = this.ensureImmutable(event);
        if (!Constants.FORMAT_MESSAGES_IN_BACKGROUND) {
            logEvent.getMessage().getFormattedMessage();
        }
        return logEvent;
    }
    
    private void enqueue(final LogEvent logEvent, final AsyncLoggerConfig asyncLoggerConfig) {
        this.disruptor.getRingBuffer().publishEvent((EventTranslatorTwoArg)AsyncLoggerConfigDisruptor.TRANSLATOR, (Object)logEvent, (Object)asyncLoggerConfig);
    }
    
    private LogEvent ensureImmutable(final LogEvent event) {
        LogEvent result = event;
        if (event instanceof RingBufferLogEvent) {
            result = ((RingBufferLogEvent)event).createMemento();
        }
        return result;
    }
    
    private boolean isCalledFromAppenderThreadAndBufferFull(final Disruptor<Log4jEventWrapper> theDisruptor) {
        return this.currentThreadIsAppenderThread() && theDisruptor.getRingBuffer().remainingCapacity() == 0L;
    }
    
    private boolean currentThreadIsAppenderThread() {
        return Thread.currentThread().getId() == this.backgroundThreadId;
    }
    
    @Override
    public RingBufferAdmin createRingBufferAdmin(final String contextName, final String loggerConfigName) {
        return RingBufferAdmin.forAsyncLoggerConfig((RingBuffer<?>)this.disruptor.getRingBuffer(), contextName, loggerConfigName);
    }
    
    static {
        LOGGER = StatusLogger.getLogger();
        FACTORY = (EventFactory)new EventFactory<Log4jEventWrapper>() {
            public Log4jEventWrapper newInstance() {
                return new Log4jEventWrapper();
            }
        };
        TRANSLATOR = (EventTranslatorTwoArg)new EventTranslatorTwoArg<Log4jEventWrapper, LogEvent, AsyncLoggerConfig>() {
            public void translateTo(final Log4jEventWrapper ringBufferElement, final long sequence, final LogEvent logEvent, final AsyncLoggerConfig loggerConfig) {
                ringBufferElement.event = logEvent;
                ringBufferElement.loggerConfig = loggerConfig;
            }
        };
        THREAD_FACTORY = new DaemonThreadFactory("AsyncLoggerConfig-");
    }
    
    private static class Log4jEventWrapper
    {
        private AsyncLoggerConfig loggerConfig;
        private LogEvent event;
        
        public void clear() {
            this.loggerConfig = null;
            this.event = null;
        }
    }
    
    private static class Log4jEventWrapperHandler implements SequenceReportingEventHandler<Log4jEventWrapper>
    {
        private static final int NOTIFY_PROGRESS_THRESHOLD = 50;
        private Sequence sequenceCallback;
        private int counter;
        
        public void setSequenceCallback(final Sequence sequenceCallback) {
            this.sequenceCallback = sequenceCallback;
        }
        
        public void onEvent(final Log4jEventWrapper event, final long sequence, final boolean endOfBatch) throws Exception {
            event.event.setEndOfBatch(endOfBatch);
            event.loggerConfig.asyncCallAppenders(event.event);
            event.clear();
            this.notifyIntermediateProgress(sequence);
        }
        
        private void notifyIntermediateProgress(final long sequence) {
            if (++this.counter > 50) {
                this.sequenceCallback.set(sequence);
                this.counter = 0;
            }
        }
    }
}
