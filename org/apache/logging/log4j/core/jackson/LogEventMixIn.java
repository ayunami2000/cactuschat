// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.jackson;

import org.apache.logging.log4j.core.impl.ThrowableProxy;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.Level;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import org.apache.logging.log4j.ThreadContext;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.apache.logging.log4j.core.LogEvent;

@JsonRootName("Event")
@JacksonXmlRootElement(namespace = "http://logging.apache.org/log4j/2.0/events", localName = "Event")
@JsonFilter("org.apache.logging.log4j.core.impl.Log4jLogEvent")
@JsonPropertyOrder({ "timeMillis", "threadName", "level", "loggerName", "marker", "message", "thrown", "ContextMap", "contextStack", "loggerFQCN", "Source", "endOfBatch" })
abstract class LogEventMixIn implements LogEvent
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("contextMap")
    @JacksonXmlProperty(namespace = "http://logging.apache.org/log4j/2.0/events", localName = "ContextMap")
    @JsonSerialize(using = ListOfMapEntrySerializer.class)
    @JsonDeserialize(using = ListOfMapEntryDeserializer.class)
    @Override
    public abstract Map<String, String> getContextMap();
    
    @JsonProperty("contextStack")
    @JacksonXmlElementWrapper(namespace = "http://logging.apache.org/log4j/2.0/events", localName = "ContextStack")
    @JacksonXmlProperty(namespace = "http://logging.apache.org/log4j/2.0/events", localName = "ContextStackItem")
    @Override
    public abstract ThreadContext.ContextStack getContextStack();
    
    @JsonProperty
    @JacksonXmlProperty(isAttribute = true)
    @Override
    public abstract Level getLevel();
    
    @JsonProperty
    @JacksonXmlProperty(isAttribute = true)
    @Override
    public abstract String getLoggerFqcn();
    
    @JsonProperty
    @JacksonXmlProperty(isAttribute = true)
    @Override
    public abstract String getLoggerName();
    
    @JsonProperty("marker")
    @JacksonXmlProperty(namespace = "http://logging.apache.org/log4j/2.0/events", localName = "Marker")
    @Override
    public abstract Marker getMarker();
    
    @JsonProperty("message")
    @JsonSerialize(using = MessageSerializer.class)
    @JsonDeserialize(using = SimpleMessageDeserializer.class)
    @JacksonXmlProperty(namespace = "http://logging.apache.org/log4j/2.0/events", localName = "Message")
    @Override
    public abstract Message getMessage();
    
    @JsonProperty("source")
    @JsonDeserialize(using = Log4jStackTraceElementDeserializer.class)
    @JacksonXmlProperty(namespace = "http://logging.apache.org/log4j/2.0/events", localName = "Source")
    @Override
    public abstract StackTraceElement getSource();
    
    @JsonProperty("thread")
    @JacksonXmlProperty(isAttribute = true, localName = "thread")
    @Override
    public abstract String getThreadName();
    
    @JsonIgnore
    @Override
    public abstract Throwable getThrown();
    
    @JsonProperty("thrown")
    @JacksonXmlProperty(namespace = "http://logging.apache.org/log4j/2.0/events", localName = "Thrown")
    @Override
    public abstract ThrowableProxy getThrownProxy();
    
    @JsonProperty
    @JacksonXmlProperty(isAttribute = true)
    @Override
    public abstract long getTimeMillis();
    
    @JsonProperty
    @JacksonXmlProperty(isAttribute = true)
    @Override
    public abstract boolean isEndOfBatch();
    
    @JsonIgnore
    @Override
    public abstract boolean isIncludeLocation();
    
    @Override
    public abstract void setEndOfBatch(final boolean p0);
    
    @Override
    public abstract void setIncludeLocation(final boolean p0);
}
