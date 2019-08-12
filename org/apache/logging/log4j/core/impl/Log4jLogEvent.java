// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.impl;

import org.apache.logging.log4j.core.async.RingBufferLogEvent;
import org.apache.logging.log4j.core.util.Builder;
import org.apache.logging.log4j.core.util.DummyNanoClock;
import org.apache.logging.log4j.core.util.ClockFactory;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import org.apache.logging.log4j.status.StatusLogger;
import java.util.Objects;
import java.util.Iterator;
import java.util.Collections;
import java.util.HashMap;
import org.apache.logging.log4j.message.LoggerNameAwareMessage;
import org.apache.logging.log4j.message.TimestampMessage;
import org.apache.logging.log4j.core.config.Property;
import java.util.List;
import org.apache.logging.log4j.ThreadContext;
import java.util.Map;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.util.NanoClock;
import org.apache.logging.log4j.core.util.Clock;
import org.apache.logging.log4j.core.LogEvent;

public class Log4jLogEvent implements LogEvent
{
    private static final long serialVersionUID = -1351367343806656055L;
    private static final Clock CLOCK;
    private static volatile NanoClock nanoClock;
    private final String loggerFqcn;
    private final Marker marker;
    private final Level level;
    private final String loggerName;
    private final Message message;
    private final long timeMillis;
    private final transient Throwable thrown;
    private ThrowableProxy thrownProxy;
    private final Map<String, String> contextMap;
    private final ThreadContext.ContextStack contextStack;
    private String threadName;
    private StackTraceElement source;
    private boolean includeLocation;
    private boolean endOfBatch;
    private final transient long nanoTime;
    
    public static Builder newBuilder() {
        return new Builder();
    }
    
    public Log4jLogEvent() {
        this("", null, "", null, null, null, null, null, null, null, null, Log4jLogEvent.CLOCK.currentTimeMillis(), Log4jLogEvent.nanoClock.nanoTime());
    }
    
    @Deprecated
    public Log4jLogEvent(final long timestamp) {
        this("", null, "", null, null, null, null, null, null, null, null, timestamp, Log4jLogEvent.nanoClock.nanoTime());
    }
    
    @Deprecated
    public Log4jLogEvent(final String loggerName, final Marker marker, final String loggerFQCN, final Level level, final Message message, final Throwable t) {
        this(loggerName, marker, loggerFQCN, level, message, null, t);
    }
    
    public Log4jLogEvent(final String loggerName, final Marker marker, final String loggerFQCN, final Level level, final Message message, final List<Property> properties, final Throwable t) {
        this(loggerName, marker, loggerFQCN, level, message, t, null, createMap(properties), (ThreadContext.getDepth() == 0) ? null : ThreadContext.cloneStack(), null, null, (message instanceof TimestampMessage) ? ((TimestampMessage)message).getTimestamp() : Log4jLogEvent.CLOCK.currentTimeMillis(), Log4jLogEvent.nanoClock.nanoTime());
    }
    
    @Deprecated
    public Log4jLogEvent(final String loggerName, final Marker marker, final String loggerFQCN, final Level level, final Message message, final Throwable t, final Map<String, String> mdc, final ThreadContext.ContextStack ndc, final String threadName, final StackTraceElement location, final long timestampMillis) {
        this(loggerName, marker, loggerFQCN, level, message, t, null, mdc, ndc, threadName, location, timestampMillis, Log4jLogEvent.nanoClock.nanoTime());
    }
    
    @Deprecated
    public static Log4jLogEvent createEvent(final String loggerName, final Marker marker, final String loggerFQCN, final Level level, final Message message, final Throwable thrown, final ThrowableProxy thrownProxy, final Map<String, String> mdc, final ThreadContext.ContextStack ndc, final String threadName, final StackTraceElement location, final long timestamp) {
        final Log4jLogEvent result = new Log4jLogEvent(loggerName, marker, loggerFQCN, level, message, thrown, thrownProxy, mdc, ndc, threadName, location, timestamp, Log4jLogEvent.nanoClock.nanoTime());
        return result;
    }
    
    private Log4jLogEvent(final String loggerName, final Marker marker, final String loggerFQCN, final Level level, final Message message, final Throwable thrown, final ThrowableProxy thrownProxy, final Map<String, String> contextMap, final ThreadContext.ContextStack contextStack, final String threadName, final StackTraceElement source, final long timestampMillis, final long nanoTime) {
        this.endOfBatch = false;
        this.loggerName = loggerName;
        this.marker = marker;
        this.loggerFqcn = loggerFQCN;
        this.level = ((level == null) ? Level.OFF : level);
        this.message = message;
        this.thrown = thrown;
        this.thrownProxy = thrownProxy;
        this.contextMap = ((contextMap == null) ? ThreadContext.EMPTY_MAP : contextMap);
        this.contextStack = ((contextStack == null) ? ThreadContext.EMPTY_STACK : contextStack);
        this.timeMillis = ((message instanceof TimestampMessage) ? ((TimestampMessage)message).getTimestamp() : timestampMillis);
        this.threadName = threadName;
        this.source = source;
        if (message != null && message instanceof LoggerNameAwareMessage) {
            ((LoggerNameAwareMessage)message).setLoggerName(loggerName);
        }
        this.nanoTime = nanoTime;
    }
    
    private static Map<String, String> createMap(final List<Property> properties) {
        final Map<String, String> contextMap = ThreadContext.getImmutableContext();
        if (properties == null || properties.isEmpty()) {
            return contextMap;
        }
        final Map<String, String> map = new HashMap<String, String>(contextMap);
        for (final Property prop : properties) {
            if (!map.containsKey(prop.getName())) {
                map.put(prop.getName(), prop.getValue());
            }
        }
        return Collections.unmodifiableMap((Map<? extends String, ? extends String>)map);
    }
    
    public static NanoClock getNanoClock() {
        return Log4jLogEvent.nanoClock;
    }
    
    public static void setNanoClock(final NanoClock nanoClock) {
        Log4jLogEvent.nanoClock = Objects.requireNonNull(nanoClock, "NanoClock must be non-null");
        StatusLogger.getLogger().trace("Using {} for nanosecond timestamps.", new Object[] { nanoClock.getClass().getSimpleName() });
    }
    
    public Builder asBuilder() {
        return new Builder(this);
    }
    
    @Override
    public Level getLevel() {
        return this.level;
    }
    
    @Override
    public String getLoggerName() {
        return this.loggerName;
    }
    
    @Override
    public Message getMessage() {
        return this.message;
    }
    
    @Override
    public String getThreadName() {
        if (this.threadName == null) {
            this.threadName = Thread.currentThread().getName();
        }
        return this.threadName;
    }
    
    @Override
    public long getTimeMillis() {
        return this.timeMillis;
    }
    
    @Override
    public Throwable getThrown() {
        return this.thrown;
    }
    
    @Override
    public ThrowableProxy getThrownProxy() {
        if (this.thrownProxy == null && this.thrown != null) {
            this.thrownProxy = new ThrowableProxy(this.thrown);
        }
        return this.thrownProxy;
    }
    
    @Override
    public Marker getMarker() {
        return this.marker;
    }
    
    @Override
    public String getLoggerFqcn() {
        return this.loggerFqcn;
    }
    
    @Override
    public Map<String, String> getContextMap() {
        return this.contextMap;
    }
    
    @Override
    public ThreadContext.ContextStack getContextStack() {
        return this.contextStack;
    }
    
    @Override
    public StackTraceElement getSource() {
        if (this.source != null) {
            return this.source;
        }
        if (this.loggerFqcn == null || !this.includeLocation) {
            return null;
        }
        return this.source = calcLocation(this.loggerFqcn);
    }
    
    public static StackTraceElement calcLocation(final String fqcnOfLogger) {
        if (fqcnOfLogger == null) {
            return null;
        }
        final StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        StackTraceElement last = null;
        for (int i = stackTrace.length - 1; i > 0; --i) {
            final String className = stackTrace[i].getClassName();
            if (fqcnOfLogger.equals(className)) {
                return last;
            }
            last = stackTrace[i];
        }
        return null;
    }
    
    @Override
    public boolean isIncludeLocation() {
        return this.includeLocation;
    }
    
    @Override
    public void setIncludeLocation(final boolean includeLocation) {
        this.includeLocation = includeLocation;
    }
    
    @Override
    public boolean isEndOfBatch() {
        return this.endOfBatch;
    }
    
    @Override
    public void setEndOfBatch(final boolean endOfBatch) {
        this.endOfBatch = endOfBatch;
    }
    
    @Override
    public long getNanoTime() {
        return this.nanoTime;
    }
    
    protected Object writeReplace() {
        this.getThrownProxy();
        return new LogEventProxy(this, this.includeLocation);
    }
    
    public static Serializable serialize(final Log4jLogEvent event, final boolean includeLocation) {
        event.getThrownProxy();
        return new LogEventProxy(event, includeLocation);
    }
    
    public static boolean canDeserialize(final Serializable event) {
        return event instanceof LogEventProxy;
    }
    
    public static Log4jLogEvent deserialize(final Serializable event) {
        Objects.requireNonNull(event, "Event cannot be null");
        if (event instanceof LogEventProxy) {
            final LogEventProxy proxy = (LogEventProxy)event;
            final Log4jLogEvent result = new Log4jLogEvent(proxy.loggerName, proxy.marker, proxy.loggerFQCN, proxy.level, proxy.message, proxy.thrown, proxy.thrownProxy, proxy.contextMap, proxy.contextStack, proxy.threadName, proxy.source, proxy.timeMillis, proxy.nanoTime);
            result.setEndOfBatch(proxy.isEndOfBatch);
            result.setIncludeLocation(proxy.isLocationRequired);
            return result;
        }
        throw new IllegalArgumentException("Event is not a serialized LogEvent: " + event.toString());
    }
    
    private void readObject(final ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        final String n = this.loggerName.isEmpty() ? "root" : this.loggerName;
        sb.append("Logger=").append(n);
        sb.append(" Level=").append(this.level.name());
        sb.append(" Message=").append(this.message.getFormattedMessage());
        return sb.toString();
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final Log4jLogEvent that = (Log4jLogEvent)o;
        if (this.endOfBatch != that.endOfBatch) {
            return false;
        }
        if (this.includeLocation != that.includeLocation) {
            return false;
        }
        if (this.timeMillis != that.timeMillis) {
            return false;
        }
        if (this.nanoTime != that.nanoTime) {
            return false;
        }
        Label_0116: {
            if (this.loggerFqcn != null) {
                if (this.loggerFqcn.equals(that.loggerFqcn)) {
                    break Label_0116;
                }
            }
            else if (that.loggerFqcn == null) {
                break Label_0116;
            }
            return false;
        }
        Label_0149: {
            if (this.level != null) {
                if (this.level.equals(that.level)) {
                    break Label_0149;
                }
            }
            else if (that.level == null) {
                break Label_0149;
            }
            return false;
        }
        Label_0182: {
            if (this.source != null) {
                if (this.source.equals(that.source)) {
                    break Label_0182;
                }
            }
            else if (that.source == null) {
                break Label_0182;
            }
            return false;
        }
        Label_0217: {
            if (this.marker != null) {
                if (this.marker.equals(that.marker)) {
                    break Label_0217;
                }
            }
            else if (that.marker == null) {
                break Label_0217;
            }
            return false;
        }
        Label_0252: {
            if (this.contextMap != null) {
                if (this.contextMap.equals(that.contextMap)) {
                    break Label_0252;
                }
            }
            else if (that.contextMap == null) {
                break Label_0252;
            }
            return false;
        }
        if (!this.message.equals(that.message)) {
            return false;
        }
        if (!this.loggerName.equals(that.loggerName)) {
            return false;
        }
        Label_0317: {
            if (this.contextStack != null) {
                if (this.contextStack.equals(that.contextStack)) {
                    break Label_0317;
                }
            }
            else if (that.contextStack == null) {
                break Label_0317;
            }
            return false;
        }
        Label_0350: {
            if (this.threadName != null) {
                if (this.threadName.equals(that.threadName)) {
                    break Label_0350;
                }
            }
            else if (that.threadName == null) {
                break Label_0350;
            }
            return false;
        }
        Label_0383: {
            if (this.thrown != null) {
                if (this.thrown.equals(that.thrown)) {
                    break Label_0383;
                }
            }
            else if (that.thrown == null) {
                break Label_0383;
            }
            return false;
        }
        if (this.thrownProxy != null) {
            if (this.thrownProxy.equals(that.thrownProxy)) {
                return true;
            }
        }
        else if (that.thrownProxy == null) {
            return true;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int result = (this.loggerFqcn != null) ? this.loggerFqcn.hashCode() : 0;
        result = 31 * result + ((this.marker != null) ? this.marker.hashCode() : 0);
        result = 31 * result + ((this.level != null) ? this.level.hashCode() : 0);
        result = 31 * result + this.loggerName.hashCode();
        result = 31 * result + this.message.hashCode();
        result = 31 * result + (int)(this.timeMillis ^ this.timeMillis >>> 32);
        result = 31 * result + (int)(this.nanoTime ^ this.nanoTime >>> 32);
        result = 31 * result + ((this.thrown != null) ? this.thrown.hashCode() : 0);
        result = 31 * result + ((this.thrownProxy != null) ? this.thrownProxy.hashCode() : 0);
        result = 31 * result + ((this.contextMap != null) ? this.contextMap.hashCode() : 0);
        result = 31 * result + ((this.contextStack != null) ? this.contextStack.hashCode() : 0);
        result = 31 * result + ((this.threadName != null) ? this.threadName.hashCode() : 0);
        result = 31 * result + ((this.source != null) ? this.source.hashCode() : 0);
        result = 31 * result + (this.includeLocation ? 1 : 0);
        result = 31 * result + (this.endOfBatch ? 1 : 0);
        return result;
    }
    
    static {
        CLOCK = ClockFactory.getClock();
        Log4jLogEvent.nanoClock = new DummyNanoClock();
    }
    
    public static class Builder implements org.apache.logging.log4j.core.util.Builder<LogEvent>
    {
        private String loggerFqcn;
        private Marker marker;
        private Level level;
        private String loggerName;
        private Message message;
        private Throwable thrown;
        private long timeMillis;
        private ThrowableProxy thrownProxy;
        private Map<String, String> contextMap;
        private ThreadContext.ContextStack contextStack;
        private String threadName;
        private StackTraceElement source;
        private boolean includeLocation;
        private boolean endOfBatch;
        private long nanoTime;
        
        public Builder() {
            this.timeMillis = Log4jLogEvent.CLOCK.currentTimeMillis();
            this.contextMap = ThreadContext.getImmutableContext();
            this.contextStack = ThreadContext.getImmutableStack();
            this.threadName = null;
            this.endOfBatch = false;
        }
        
        public Builder(final LogEvent other) {
            this.timeMillis = Log4jLogEvent.CLOCK.currentTimeMillis();
            this.contextMap = ThreadContext.getImmutableContext();
            this.contextStack = ThreadContext.getImmutableStack();
            this.threadName = null;
            this.endOfBatch = false;
            Objects.requireNonNull(other);
            if (other instanceof RingBufferLogEvent) {
                final RingBufferLogEvent evt = (RingBufferLogEvent)other;
                evt.initializeBuilder(this);
                return;
            }
            this.loggerFqcn = other.getLoggerFqcn();
            this.marker = other.getMarker();
            this.level = other.getLevel();
            this.loggerName = other.getLoggerName();
            this.message = other.getMessage();
            this.timeMillis = other.getTimeMillis();
            this.thrown = other.getThrown();
            this.contextMap = other.getContextMap();
            this.contextStack = other.getContextStack();
            this.includeLocation = other.isIncludeLocation();
            this.endOfBatch = other.isEndOfBatch();
            this.nanoTime = other.getNanoTime();
            if (other instanceof Log4jLogEvent) {
                final Log4jLogEvent evt2 = (Log4jLogEvent)other;
                this.thrownProxy = evt2.thrownProxy;
                this.source = evt2.source;
                this.threadName = evt2.threadName;
            }
            else {
                this.thrownProxy = other.getThrownProxy();
                this.source = other.getSource();
                this.threadName = other.getThreadName();
            }
        }
        
        public Builder setLevel(final Level level) {
            this.level = level;
            return this;
        }
        
        public Builder setLoggerFqcn(final String loggerFqcn) {
            this.loggerFqcn = loggerFqcn;
            return this;
        }
        
        public Builder setLoggerName(final String loggerName) {
            this.loggerName = loggerName;
            return this;
        }
        
        public Builder setMarker(final Marker marker) {
            this.marker = marker;
            return this;
        }
        
        public Builder setMessage(final Message message) {
            this.message = message;
            return this;
        }
        
        public Builder setThrown(final Throwable thrown) {
            this.thrown = thrown;
            return this;
        }
        
        public Builder setTimeMillis(final long timeMillis) {
            this.timeMillis = timeMillis;
            return this;
        }
        
        public Builder setThrownProxy(final ThrowableProxy thrownProxy) {
            this.thrownProxy = thrownProxy;
            return this;
        }
        
        public Builder setContextMap(final Map<String, String> contextMap) {
            this.contextMap = contextMap;
            return this;
        }
        
        public Builder setContextStack(final ThreadContext.ContextStack contextStack) {
            this.contextStack = contextStack;
            return this;
        }
        
        public Builder setThreadName(final String threadName) {
            this.threadName = threadName;
            return this;
        }
        
        public Builder setSource(final StackTraceElement source) {
            this.source = source;
            return this;
        }
        
        public Builder setIncludeLocation(final boolean includeLocation) {
            this.includeLocation = includeLocation;
            return this;
        }
        
        public Builder setEndOfBatch(final boolean endOfBatch) {
            this.endOfBatch = endOfBatch;
            return this;
        }
        
        public Builder setNanoTime(final long nanoTime) {
            this.nanoTime = nanoTime;
            return this;
        }
        
        @Override
        public Log4jLogEvent build() {
            final Log4jLogEvent result = new Log4jLogEvent(this.loggerName, this.marker, this.loggerFqcn, this.level, this.message, this.thrown, this.thrownProxy, this.contextMap, this.contextStack, this.threadName, this.source, this.timeMillis, this.nanoTime, null);
            result.setIncludeLocation(this.includeLocation);
            result.setEndOfBatch(this.endOfBatch);
            return result;
        }
    }
    
    private static class LogEventProxy implements Serializable
    {
        private static final long serialVersionUID = -7139032940312647146L;
        private final String loggerFQCN;
        private final Marker marker;
        private final Level level;
        private final String loggerName;
        private final Message message;
        private final long timeMillis;
        private final transient Throwable thrown;
        private final ThrowableProxy thrownProxy;
        private final Map<String, String> contextMap;
        private final ThreadContext.ContextStack contextStack;
        private final String threadName;
        private final StackTraceElement source;
        private final boolean isLocationRequired;
        private final boolean isEndOfBatch;
        private final transient long nanoTime;
        
        public LogEventProxy(final Log4jLogEvent event, final boolean includeLocation) {
            this.loggerFQCN = event.loggerFqcn;
            this.marker = event.marker;
            this.level = event.level;
            this.loggerName = event.loggerName;
            this.message = event.message;
            this.timeMillis = event.timeMillis;
            this.thrown = event.thrown;
            this.thrownProxy = event.thrownProxy;
            this.contextMap = event.contextMap;
            this.contextStack = event.contextStack;
            this.source = (includeLocation ? event.getSource() : null);
            this.threadName = event.getThreadName();
            this.isLocationRequired = includeLocation;
            this.isEndOfBatch = event.endOfBatch;
            this.nanoTime = event.nanoTime;
        }
        
        protected Object readResolve() {
            final Log4jLogEvent result = new Log4jLogEvent(this.loggerName, this.marker, this.loggerFQCN, this.level, this.message, this.thrown, this.thrownProxy, this.contextMap, this.contextStack, this.threadName, this.source, this.timeMillis, this.nanoTime, null);
            result.setEndOfBatch(this.isEndOfBatch);
            result.setIncludeLocation(this.isLocationRequired);
            return result;
        }
    }
}
