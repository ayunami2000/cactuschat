// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.async;

import com.lmax.disruptor.EventFactory;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.HashMap;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.logging.log4j.ThreadContext;
import java.util.Map;
import org.apache.logging.log4j.core.impl.ThrowableProxy;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;

public class RingBufferLogEvent implements LogEvent
{
    public static final Factory FACTORY;
    private static final long serialVersionUID = 8462119088943934758L;
    private transient AsyncLogger asyncLogger;
    private String loggerName;
    private Marker marker;
    private String fqcn;
    private Level level;
    private Message message;
    private transient Throwable thrown;
    private ThrowableProxy thrownProxy;
    private Map<String, String> contextMap;
    private ThreadContext.ContextStack contextStack;
    private String threadName;
    private StackTraceElement location;
    private long currentTimeMillis;
    private boolean endOfBatch;
    private boolean includeLocation;
    private long nanoTime;
    
    public void setValues(final AsyncLogger anAsyncLogger, final String aLoggerName, final Marker aMarker, final String theFqcn, final Level aLevel, final Message msg, final Throwable aThrowable, final Map<String, String> aMap, final ThreadContext.ContextStack aContextStack, final String aThreadName, final StackTraceElement aLocation, final long aCurrentTimeMillis, final long aNanoTime) {
        this.asyncLogger = anAsyncLogger;
        this.loggerName = aLoggerName;
        this.marker = aMarker;
        this.fqcn = theFqcn;
        this.level = aLevel;
        this.message = msg;
        this.thrown = aThrowable;
        this.thrownProxy = null;
        this.contextMap = aMap;
        this.contextStack = aContextStack;
        this.threadName = aThreadName;
        this.location = aLocation;
        this.currentTimeMillis = aCurrentTimeMillis;
        this.nanoTime = aNanoTime;
    }
    
    public void execute(final boolean endOfBatch) {
        this.endOfBatch = endOfBatch;
        this.asyncLogger.actualAsyncLog(this);
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
    public boolean isIncludeLocation() {
        return this.includeLocation;
    }
    
    @Override
    public void setIncludeLocation(final boolean includeLocation) {
        this.includeLocation = includeLocation;
    }
    
    @Override
    public String getLoggerName() {
        return this.loggerName;
    }
    
    @Override
    public Marker getMarker() {
        return this.marker;
    }
    
    @Override
    public String getLoggerFqcn() {
        return this.fqcn;
    }
    
    @Override
    public Level getLevel() {
        if (this.level == null) {
            this.level = Level.OFF;
        }
        return this.level;
    }
    
    @Override
    public Message getMessage() {
        if (this.message == null) {
            this.message = new SimpleMessage("");
        }
        return this.message;
    }
    
    @Override
    public Throwable getThrown() {
        if (this.thrown == null && this.thrownProxy != null) {
            this.thrown = this.thrownProxy.getThrowable();
        }
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
    public Map<String, String> getContextMap() {
        return this.contextMap;
    }
    
    @Override
    public ThreadContext.ContextStack getContextStack() {
        return this.contextStack;
    }
    
    @Override
    public String getThreadName() {
        return this.threadName;
    }
    
    @Override
    public StackTraceElement getSource() {
        return this.location;
    }
    
    @Override
    public long getTimeMillis() {
        return this.currentTimeMillis;
    }
    
    @Override
    public long getNanoTime() {
        return this.nanoTime;
    }
    
    public void mergePropertiesIntoContextMap(final Map<Property, Boolean> properties, final StrSubstitutor strSubstitutor) {
        if (properties == null) {
            return;
        }
        final Map<String, String> map = (this.contextMap == null) ? new HashMap<String, String>() : new HashMap<String, String>(this.contextMap);
        for (final Map.Entry<Property, Boolean> entry : properties.entrySet()) {
            final Property prop = entry.getKey();
            if (map.containsKey(prop.getName())) {
                continue;
            }
            final String value = entry.getValue() ? strSubstitutor.replace(prop.getValue()) : prop.getValue();
            map.put(prop.getName(), value);
        }
        this.contextMap = map;
    }
    
    public void clear() {
        this.setValues(null, null, null, null, null, null, null, null, null, null, null, 0L, 0L);
    }
    
    private void writeObject(final ObjectOutputStream out) throws IOException {
        this.getThrownProxy();
        out.defaultWriteObject();
    }
    
    public LogEvent createMemento() {
        final LogEvent result = new Log4jLogEvent.Builder(this).build();
        return result;
    }
    
    public void initializeBuilder(final Log4jLogEvent.Builder builder) {
        builder.setContextMap(this.contextMap).setContextStack(this.contextStack).setEndOfBatch(this.endOfBatch).setIncludeLocation(this.includeLocation).setLevel(this.getLevel()).setLoggerFqcn(this.fqcn).setLoggerName(this.loggerName).setMarker(this.marker).setMessage(this.getMessage()).setNanoTime(this.nanoTime).setSource(this.location).setThreadName(this.threadName).setThrown(this.getThrown()).setThrownProxy(this.thrownProxy).setTimeMillis(this.currentTimeMillis);
    }
    
    static {
        FACTORY = new Factory();
    }
    
    private static class Factory implements EventFactory<RingBufferLogEvent>
    {
        public RingBufferLogEvent newInstance() {
            return new RingBufferLogEvent();
        }
    }
}
