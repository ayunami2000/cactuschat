// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.appender;

import java.util.Iterator;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginAliases;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.util.Constants;
import org.apache.logging.log4j.core.async.RingBufferLogEvent;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.LogEvent;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.core.config.ConfigurationException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.config.AppenderControl;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.logging.log4j.core.config.plugins.Plugin;

@Plugin(name = "Async", category = "Core", elementType = "appender", printObject = true)
public final class AsyncAppender extends AbstractAppender
{
    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_QUEUE_SIZE = 128;
    private static final String SHUTDOWN = "Shutdown";
    private static final AtomicLong THREAD_SEQUENCE;
    private static ThreadLocal<Boolean> isAppenderThread;
    private final BlockingQueue<Serializable> queue;
    private final int queueSize;
    private final boolean blocking;
    private final long shutdownTimeout;
    private final Configuration config;
    private final AppenderRef[] appenderRefs;
    private final String errorRef;
    private final boolean includeLocation;
    private AppenderControl errorAppender;
    private AsyncThread thread;
    
    private AsyncAppender(final String name, final Filter filter, final AppenderRef[] appenderRefs, final String errorRef, final int queueSize, final boolean blocking, final boolean ignoreExceptions, final long shutdownTimeout, final Configuration config, final boolean includeLocation) {
        super(name, filter, null, ignoreExceptions);
        this.queue = new ArrayBlockingQueue<Serializable>(queueSize);
        this.queueSize = queueSize;
        this.blocking = blocking;
        this.shutdownTimeout = shutdownTimeout;
        this.config = config;
        this.appenderRefs = appenderRefs;
        this.errorRef = errorRef;
        this.includeLocation = includeLocation;
    }
    
    @Override
    public void start() {
        final Map<String, Appender> map = this.config.getAppenders();
        final List<AppenderControl> appenders = new ArrayList<AppenderControl>();
        for (final AppenderRef appenderRef : this.appenderRefs) {
            final Appender appender = map.get(appenderRef.getRef());
            if (appender != null) {
                appenders.add(new AppenderControl(appender, appenderRef.getLevel(), appenderRef.getFilter()));
            }
            else {
                AsyncAppender.LOGGER.error("No appender named {} was configured", new Object[] { appenderRef });
            }
        }
        if (this.errorRef != null) {
            final Appender appender2 = map.get(this.errorRef);
            if (appender2 != null) {
                this.errorAppender = new AppenderControl(appender2, null, null);
            }
            else {
                AsyncAppender.LOGGER.error("Unable to set up error Appender. No appender named {} was configured", new Object[] { this.errorRef });
            }
        }
        if (appenders.size() > 0) {
            (this.thread = new AsyncThread(appenders, this.queue)).setName("AsyncAppender-" + this.getName());
        }
        else if (this.errorRef == null) {
            throw new ConfigurationException("No appenders are available for AsyncAppender " + this.getName());
        }
        this.thread.start();
        super.start();
    }
    
    @Override
    public void stop() {
        super.stop();
        AsyncAppender.LOGGER.trace("AsyncAppender stopping. Queue still has {} events.", new Object[] { this.queue.size() });
        this.thread.shutdown();
        try {
            this.thread.join(this.shutdownTimeout);
        }
        catch (InterruptedException ex) {
            AsyncAppender.LOGGER.warn("Interrupted while stopping AsyncAppender {}", new Object[] { this.getName() });
        }
        AsyncAppender.LOGGER.trace("AsyncAppender stopped. Queue has {} events.", new Object[] { this.queue.size() });
    }
    
    @Override
    public void append(LogEvent logEvent) {
        if (!this.isStarted()) {
            throw new IllegalStateException("AsyncAppender " + this.getName() + " is not active");
        }
        if (!(logEvent instanceof Log4jLogEvent)) {
            if (!(logEvent instanceof RingBufferLogEvent)) {
                return;
            }
            logEvent = ((RingBufferLogEvent)logEvent).createMemento();
        }
        if (!Constants.FORMAT_MESSAGES_IN_BACKGROUND) {
            logEvent.getMessage().getFormattedMessage();
        }
        final Log4jLogEvent coreEvent = (Log4jLogEvent)logEvent;
        boolean appendSuccessful = false;
        if (this.blocking) {
            if (AsyncAppender.isAppenderThread.get() == Boolean.TRUE && this.queue.remainingCapacity() == 0) {
                coreEvent.setEndOfBatch(false);
                appendSuccessful = this.thread.callAppenders(coreEvent);
            }
            else {
                final Serializable serialized = Log4jLogEvent.serialize(coreEvent, this.includeLocation);
                try {
                    this.queue.put(serialized);
                    appendSuccessful = true;
                }
                catch (InterruptedException e) {
                    appendSuccessful = this.queue.offer(serialized);
                    if (!appendSuccessful) {
                        AsyncAppender.LOGGER.warn("Interrupted while waiting for a free slot in the AsyncAppender LogEvent-queue {}", new Object[] { this.getName() });
                    }
                    Thread.currentThread().interrupt();
                }
            }
        }
        else {
            appendSuccessful = this.queue.offer(Log4jLogEvent.serialize(coreEvent, this.includeLocation));
            if (!appendSuccessful) {
                this.error("Appender " + this.getName() + " is unable to write primary appenders. queue is full");
            }
        }
        if (!appendSuccessful && this.errorAppender != null) {
            this.errorAppender.callAppender(coreEvent);
        }
    }
    
    @PluginFactory
    public static AsyncAppender createAppender(@PluginElement("AppenderRef") final AppenderRef[] appenderRefs, @PluginAttribute("errorRef") @PluginAliases({ "error-ref" }) final String errorRef, @PluginAttribute(value = "blocking", defaultBoolean = true) final boolean blocking, @PluginAttribute(value = "shutdownTimeout", defaultLong = 0L) final long shutdownTimeout, @PluginAttribute(value = "bufferSize", defaultInt = 128) final int size, @PluginAttribute("name") final String name, @PluginAttribute(value = "includeLocation", defaultBoolean = false) final boolean includeLocation, @PluginElement("Filter") final Filter filter, @PluginConfiguration final Configuration config, @PluginAttribute(value = "ignoreExceptions", defaultBoolean = true) final boolean ignoreExceptions) {
        if (name == null) {
            AsyncAppender.LOGGER.error("No name provided for AsyncAppender");
            return null;
        }
        if (appenderRefs == null) {
            AsyncAppender.LOGGER.error("No appender references provided to AsyncAppender {}", new Object[] { name });
        }
        return new AsyncAppender(name, filter, appenderRefs, errorRef, size, blocking, ignoreExceptions, shutdownTimeout, config, includeLocation);
    }
    
    public String[] getAppenderRefStrings() {
        final String[] result = new String[this.appenderRefs.length];
        for (int i = 0; i < result.length; ++i) {
            result[i] = this.appenderRefs[i].getRef();
        }
        return result;
    }
    
    public boolean isIncludeLocation() {
        return this.includeLocation;
    }
    
    public boolean isBlocking() {
        return this.blocking;
    }
    
    public String getErrorRef() {
        return this.errorRef;
    }
    
    public int getQueueCapacity() {
        return this.queueSize;
    }
    
    public int getQueueRemainingCapacity() {
        return this.queue.remainingCapacity();
    }
    
    static {
        THREAD_SEQUENCE = new AtomicLong(1L);
        AsyncAppender.isAppenderThread = new ThreadLocal<Boolean>();
    }
    
    private class AsyncThread extends Thread
    {
        private volatile boolean shutdown;
        private final List<AppenderControl> appenders;
        private final BlockingQueue<Serializable> queue;
        
        public AsyncThread(final List<AppenderControl> appenders, final BlockingQueue<Serializable> queue) {
            this.shutdown = false;
            this.appenders = appenders;
            this.queue = queue;
            this.setDaemon(true);
            this.setName("AsyncAppenderThread" + AsyncAppender.THREAD_SEQUENCE.getAndIncrement());
        }
        
        @Override
        public void run() {
            AsyncAppender.isAppenderThread.set(Boolean.TRUE);
            while (!this.shutdown) {
                Serializable s;
                try {
                    s = this.queue.take();
                    if (s != null && s instanceof String && "Shutdown".equals(s.toString())) {
                        this.shutdown = true;
                        continue;
                    }
                }
                catch (InterruptedException ex) {
                    break;
                }
                final Log4jLogEvent event = Log4jLogEvent.deserialize(s);
                event.setEndOfBatch(this.queue.isEmpty());
                final boolean success = this.callAppenders(event);
                if (!success && AsyncAppender.this.errorAppender != null) {
                    try {
                        AsyncAppender.this.errorAppender.callAppender(event);
                    }
                    catch (Exception ex2) {}
                }
            }
            AsyncAppender.LOGGER.trace("AsyncAppender.AsyncThread shutting down. Processing remaining {} queue events.", new Object[] { this.queue.size() });
            int count = 0;
            int ignored = 0;
            while (!this.queue.isEmpty()) {
                try {
                    final Serializable s2 = this.queue.take();
                    if (Log4jLogEvent.canDeserialize(s2)) {
                        final Log4jLogEvent event2 = Log4jLogEvent.deserialize(s2);
                        event2.setEndOfBatch(this.queue.isEmpty());
                        this.callAppenders(event2);
                        ++count;
                    }
                    else {
                        ++ignored;
                        AsyncAppender.LOGGER.trace("Ignoring event of class {}", new Object[] { s2.getClass().getName() });
                    }
                }
                catch (InterruptedException ex3) {}
            }
            AsyncAppender.LOGGER.trace("AsyncAppender.AsyncThread stopped. Queue has {} events remaining. Processed {} and ignored {} events since shutdown started.", new Object[] { this.queue.size(), count, ignored });
        }
        
        boolean callAppenders(final Log4jLogEvent event) {
            boolean success = false;
            for (final AppenderControl control : this.appenders) {
                try {
                    control.callAppender(event);
                    success = true;
                }
                catch (Exception ex) {}
            }
            return success;
        }
        
        public void shutdown() {
            this.shutdown = true;
            if (this.queue.isEmpty()) {
                this.queue.offer("Shutdown");
            }
        }
    }
}
