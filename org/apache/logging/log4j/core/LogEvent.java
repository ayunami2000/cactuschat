// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core;

import org.apache.logging.log4j.core.impl.ThrowableProxy;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.ThreadContext;
import java.util.Map;
import java.io.Serializable;

public interface LogEvent extends Serializable
{
    Map<String, String> getContextMap();
    
    ThreadContext.ContextStack getContextStack();
    
    String getLoggerFqcn();
    
    Level getLevel();
    
    String getLoggerName();
    
    Marker getMarker();
    
    Message getMessage();
    
    long getTimeMillis();
    
    StackTraceElement getSource();
    
    String getThreadName();
    
    Throwable getThrown();
    
    ThrowableProxy getThrownProxy();
    
    boolean isEndOfBatch();
    
    boolean isIncludeLocation();
    
    void setEndOfBatch(final boolean p0);
    
    void setIncludeLocation(final boolean p0);
    
    long getNanoTime();
}
