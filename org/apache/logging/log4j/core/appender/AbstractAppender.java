// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.appender;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.util.Integers;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.ErrorHandler;
import java.io.Serializable;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.filter.AbstractFilterable;

public abstract class AbstractAppender extends AbstractFilterable implements Appender
{
    private static final long serialVersionUID = 1L;
    private final String name;
    private final boolean ignoreExceptions;
    private final Layout<? extends Serializable> layout;
    private ErrorHandler handler;
    
    protected AbstractAppender(final String name, final Filter filter, final Layout<? extends Serializable> layout) {
        this(name, filter, layout, true);
    }
    
    protected AbstractAppender(final String name, final Filter filter, final Layout<? extends Serializable> layout, final boolean ignoreExceptions) {
        super(filter);
        this.handler = new DefaultErrorHandler(this);
        this.name = name;
        this.layout = layout;
        this.ignoreExceptions = ignoreExceptions;
    }
    
    public static int parseInt(final String s, final int defaultValue) {
        try {
            return Integers.parseInt(s, defaultValue);
        }
        catch (NumberFormatException e) {
            AbstractAppender.LOGGER.error("Could not parse \"{}\" as an integer,  using default value {}: {}", new Object[] { s, defaultValue, e });
            return defaultValue;
        }
    }
    
    public void error(final String msg) {
        this.handler.error(msg);
    }
    
    public void error(final String msg, final LogEvent event, final Throwable t) {
        this.handler.error(msg, event, t);
    }
    
    public void error(final String msg, final Throwable t) {
        this.handler.error(msg, t);
    }
    
    @Override
    public ErrorHandler getHandler() {
        return this.handler;
    }
    
    @Override
    public Layout<? extends Serializable> getLayout() {
        return this.layout;
    }
    
    @Override
    public String getName() {
        return this.name;
    }
    
    @Override
    public boolean ignoreExceptions() {
        return this.ignoreExceptions;
    }
    
    @Override
    public void setHandler(final ErrorHandler handler) {
        if (handler == null) {
            AbstractAppender.LOGGER.error("The handler cannot be set to null");
        }
        if (this.isStarted()) {
            AbstractAppender.LOGGER.error("The handler cannot be changed once the appender is started");
            return;
        }
        this.handler = handler;
    }
    
    @Override
    public String toString() {
        return this.name;
    }
}
