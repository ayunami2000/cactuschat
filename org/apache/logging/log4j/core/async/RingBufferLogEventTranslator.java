// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.async;

import org.apache.logging.log4j.ThreadContext;
import java.util.Map;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import com.lmax.disruptor.EventTranslator;

public class RingBufferLogEventTranslator implements EventTranslator<RingBufferLogEvent>
{
    private AsyncLogger asyncLogger;
    private String loggerName;
    private Marker marker;
    private String fqcn;
    private Level level;
    private Message message;
    private Throwable thrown;
    private Map<String, String> contextMap;
    private ThreadContext.ContextStack contextStack;
    private String threadName;
    private StackTraceElement location;
    private long currentTimeMillis;
    private long nanoTime;
    
    public void translateTo(final RingBufferLogEvent event, final long sequence) {
        event.setValues(this.asyncLogger, this.loggerName, this.marker, this.fqcn, this.level, this.message, this.thrown, this.contextMap, this.contextStack, this.threadName, this.location, this.currentTimeMillis, this.nanoTime);
        this.clear();
    }
    
    private void clear() {
        this.setValues(null, null, null, null, null, null, null, null, null, null, null, 0L, 0L);
    }
    
    public void setValues(final AsyncLogger anAsyncLogger, final String aLoggerName, final Marker aMarker, final String theFqcn, final Level aLevel, final Message msg, final Throwable aThrowable, final Map<String, String> aMap, final ThreadContext.ContextStack aContextStack, final String aThreadName, final StackTraceElement aLocation, final long aCurrentTimeMillis, final long aNanoTime) {
        this.asyncLogger = anAsyncLogger;
        this.loggerName = aLoggerName;
        this.marker = aMarker;
        this.fqcn = theFqcn;
        this.level = aLevel;
        this.message = msg;
        this.thrown = aThrowable;
        this.contextMap = aMap;
        this.contextStack = aContextStack;
        this.threadName = aThreadName;
        this.location = aLocation;
        this.currentTimeMillis = aCurrentTimeMillis;
        this.nanoTime = aNanoTime;
    }
    
    public void setValuesPart1(final AsyncLogger anAsyncLogger, final String aLoggerName, final Marker aMarker, final String theFqcn, final Level aLevel, final Message msg, final Throwable aThrowable) {
        this.asyncLogger = anAsyncLogger;
        this.loggerName = aLoggerName;
        this.marker = aMarker;
        this.fqcn = theFqcn;
        this.level = aLevel;
        this.message = msg;
        this.thrown = aThrowable;
    }
    
    public void setValuesPart2(final Map<String, String> aMap, final ThreadContext.ContextStack aContextStack, final String aThreadName, final StackTraceElement aLocation, final long aCurrentTimeMillis, final long aNanoTime) {
        this.contextMap = aMap;
        this.contextStack = aContextStack;
        this.threadName = aThreadName;
        this.location = aLocation;
        this.currentTimeMillis = aCurrentTimeMillis;
        this.nanoTime = aNanoTime;
    }
}
