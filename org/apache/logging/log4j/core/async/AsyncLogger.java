// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.async;

import org.apache.logging.log4j.core.util.ClockFactory;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import java.util.Map;
import com.lmax.disruptor.dsl.Disruptor;
import org.apache.logging.log4j.message.TimestampMessage;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.util.Constants;
import org.apache.logging.log4j.core.config.ReliabilityStrategy;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.util.Supplier;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.util.NanoClockFactory;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.util.NanoClock;
import org.apache.logging.log4j.core.util.Clock;
import org.apache.logging.log4j.status.StatusLogger;
import com.lmax.disruptor.EventTranslatorVararg;
import org.apache.logging.log4j.core.Logger;

public class AsyncLogger extends Logger implements EventTranslatorVararg<RingBufferLogEvent>
{
    private static final long serialVersionUID = 1L;
    private static final StatusLogger LOGGER;
    private static final Clock CLOCK;
    private static final ThreadNameCachingStrategy THREAD_NAME_CACHING_STRATEGY;
    private final ThreadLocal<RingBufferLogEventTranslator> threadLocalTranslator;
    private final AsyncLoggerDisruptor loggerDisruptor;
    private volatile NanoClock nanoClock;
    
    public AsyncLogger(final LoggerContext context, final String name, final MessageFactory messageFactory, final AsyncLoggerDisruptor loggerDisruptor) {
        super(context, name, messageFactory);
        this.threadLocalTranslator = new ThreadLocal<RingBufferLogEventTranslator>();
        this.loggerDisruptor = loggerDisruptor;
        this.nanoClock = NanoClockFactory.createNanoClock();
    }
    
    @Override
    protected void updateConfiguration(final Configuration newConfig) {
        super.updateConfiguration(newConfig);
        this.nanoClock = NanoClockFactory.createNanoClock();
        AsyncLogger.LOGGER.trace("[{}] AsyncLogger {} uses {}.", new Object[] { this.getContext().getName(), this.getName(), this.nanoClock });
    }
    
    NanoClock getNanoClock() {
        return this.nanoClock;
    }
    
    private RingBufferLogEventTranslator getCachedTranslator() {
        RingBufferLogEventTranslator result = this.threadLocalTranslator.get();
        if (result == null) {
            result = new RingBufferLogEventTranslator();
            this.threadLocalTranslator.set(result);
        }
        return result;
    }
    
    @Override
    public void logMessage(final String fqcn, final Level level, final Marker marker, final Message message, final Throwable thrown) {
        if (this.loggerDisruptor.shouldLogInCurrentThread()) {
            this.logMessageInCurrentThread(fqcn, level, marker, message, thrown);
        }
        else {
            this.logMessageInBackgroundThread(fqcn, level, marker, message, thrown);
        }
    }
    
    private void logMessageInCurrentThread(final String fqcn, final Level level, final Marker marker, final Message message, final Throwable thrown) {
        final ReliabilityStrategy strategy = this.privateConfig.loggerConfig.getReliabilityStrategy();
        strategy.log(this, this.getName(), fqcn, marker, level, message, thrown);
    }
    
    private void logMessageInBackgroundThread(final String fqcn, final Level level, final Marker marker, final Message message, final Throwable thrown) {
        if (!Constants.FORMAT_MESSAGES_IN_BACKGROUND) {
            message.getFormattedMessage();
        }
        this.logInBackground(fqcn, level, marker, message, thrown);
    }
    
    private void logInBackground(final String fqcn, final Level level, final Marker marker, final Message message, final Throwable thrown) {
        if (this.loggerDisruptor.isUseThreadLocals()) {
            this.logWithThreadLocalTranslator(fqcn, level, marker, message, thrown);
        }
        else {
            this.logWithVarargTranslator(fqcn, level, marker, message, thrown);
        }
    }
    
    private void logWithThreadLocalTranslator(final String fqcn, final Level level, final Marker marker, final Message message, final Throwable thrown) {
        final RingBufferLogEventTranslator translator = this.getCachedTranslator();
        this.initTranslator(translator, fqcn, level, marker, message, thrown);
        this.loggerDisruptor.enqueueLogMessageInfo(translator);
    }
    
    private void initTranslator(final RingBufferLogEventTranslator translator, final String fqcn, final Level level, final Marker marker, final Message message, final Throwable thrown) {
        this.initTranslatorPart1(translator, fqcn, level, marker, message, thrown);
        this.initTranslatorPart2(translator, fqcn, message);
    }
    
    private void initTranslatorPart1(final RingBufferLogEventTranslator translator, final String fqcn, final Level level, final Marker marker, final Message message, final Throwable thrown) {
        translator.setValuesPart1(this, this.getName(), marker, fqcn, level, message, thrown);
    }
    
    private void initTranslatorPart2(final RingBufferLogEventTranslator translator, final String fqcn, final Message message) {
        translator.setValuesPart2(ThreadContext.getImmutableContext(), ThreadContext.getImmutableStack(), AsyncLogger.THREAD_NAME_CACHING_STRATEGY.getThreadName(), this.calcLocationIfRequested(fqcn), this.eventTimeMillis(message), this.nanoClock.nanoTime());
    }
    
    private long eventTimeMillis(final Message message) {
        return (message instanceof TimestampMessage) ? ((TimestampMessage)message).getTimestamp() : AsyncLogger.CLOCK.currentTimeMillis();
    }
    
    private void logWithVarargTranslator(final String fqcn, final Level level, final Marker marker, final Message message, final Throwable thrown) {
        final Disruptor<RingBufferLogEvent> disruptor = this.loggerDisruptor.getDisruptor();
        if (disruptor == null) {
            AsyncLogger.LOGGER.error("Ignoring log event after Log4j has been shut down.");
            return;
        }
        disruptor.getRingBuffer().publishEvent((EventTranslatorVararg)this, new Object[] { this, this.calcLocationIfRequested(fqcn), fqcn, level, marker, message, thrown });
    }
    
    public void translateTo(final RingBufferLogEvent event, final long sequence, final Object... args) {
        final AsyncLogger asyncLogger = (AsyncLogger)args[0];
        final StackTraceElement location = (StackTraceElement)args[1];
        final String fqcn = (String)args[2];
        final Level level = (Level)args[3];
        final Marker marker = (Marker)args[4];
        final Message message = (Message)args[5];
        final Throwable thrown = (Throwable)args[6];
        final Map<String, String> contextMap = ThreadContext.getImmutableContext();
        final ThreadContext.ContextStack contextStack = ThreadContext.getImmutableStack();
        final String threadName = AsyncLogger.THREAD_NAME_CACHING_STRATEGY.getThreadName();
        event.setValues(asyncLogger, asyncLogger.getName(), marker, fqcn, level, message, thrown, contextMap, contextStack, threadName, location, this.eventTimeMillis(message), this.nanoClock.nanoTime());
    }
    
    private StackTraceElement calcLocationIfRequested(final String fqcn) {
        final boolean includeLocation = this.privateConfig.loggerConfig.isIncludeLocation();
        return includeLocation ? Log4jLogEvent.calcLocation(fqcn) : null;
    }
    
    public void actualAsyncLog(final RingBufferLogEvent event) {
        final Map<Property, Boolean> properties = this.privateConfig.loggerConfig.getProperties();
        event.mergePropertiesIntoContextMap(properties, this.privateConfig.config.getStrSubstitutor());
        final ReliabilityStrategy strategy = this.privateConfig.loggerConfig.getReliabilityStrategy();
        strategy.log(this, event);
    }
    
    static {
        LOGGER = StatusLogger.getLogger();
        CLOCK = ClockFactory.getClock();
        THREAD_NAME_CACHING_STRATEGY = ThreadNameCachingStrategy.create();
    }
}
