// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core;

import java.io.Serializable;
import org.apache.logging.log4j.core.config.Configuration;
import java.util.List;
import org.apache.logging.log4j.core.filter.CompositeFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import org.apache.logging.log4j.core.config.ReliabilityStrategy;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.Level;
import java.io.ObjectStreamException;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.util.Supplier;
import org.apache.logging.log4j.spi.AbstractLogger;

public class Logger extends AbstractLogger implements Supplier<LoggerConfig>
{
    private static final long serialVersionUID = 1L;
    protected volatile PrivateConfig privateConfig;
    private final LoggerContext context;
    
    protected Logger(final LoggerContext context, final String name, final MessageFactory messageFactory) {
        super(name, messageFactory);
        this.context = context;
        this.privateConfig = new PrivateConfig(context.getConfiguration(), this);
    }
    
    protected Object writeReplace() throws ObjectStreamException {
        return new LoggerProxy(this.getName(), this.getMessageFactory());
    }
    
    public Logger getParent() {
        final LoggerConfig lc = this.privateConfig.loggerConfig.getName().equals(this.getName()) ? this.privateConfig.loggerConfig.getParent() : this.privateConfig.loggerConfig;
        if (lc == null) {
            return null;
        }
        final String lcName = lc.getName();
        final MessageFactory messageFactory = this.getMessageFactory();
        if (this.context.hasLogger(lcName, messageFactory)) {
            return this.context.getLogger(lcName, messageFactory);
        }
        return new Logger(this.context, lcName, messageFactory);
    }
    
    public LoggerContext getContext() {
        return this.context;
    }
    
    public synchronized void setLevel(final Level level) {
        if (level == this.getLevel()) {
            return;
        }
        Level actualLevel;
        if (level != null) {
            actualLevel = level;
        }
        else {
            final Logger parent = this.getParent();
            actualLevel = ((parent != null) ? parent.getLevel() : this.privateConfig.loggerConfigLevel);
        }
        this.privateConfig = new PrivateConfig(this.privateConfig, actualLevel);
    }
    
    @Override
    public LoggerConfig get() {
        return this.privateConfig.loggerConfig;
    }
    
    @Override
    public void logMessage(final String fqcn, final Level level, final Marker marker, final Message message, final Throwable t) {
        final Message msg = (message == null) ? new SimpleMessage("") : message;
        final ReliabilityStrategy strategy = this.privateConfig.loggerConfig.getReliabilityStrategy();
        strategy.log(this, this.getName(), fqcn, marker, level, msg, t);
    }
    
    @Override
    public boolean isEnabled(final Level level, final Marker marker, final String message, final Throwable t) {
        return this.privateConfig.filter(level, marker, message, t);
    }
    
    @Override
    public boolean isEnabled(final Level level, final Marker marker, final String message) {
        return this.privateConfig.filter(level, marker, message);
    }
    
    @Override
    public boolean isEnabled(final Level level, final Marker marker, final String message, final Object... params) {
        return this.privateConfig.filter(level, marker, message, params);
    }
    
    @Override
    public boolean isEnabled(final Level level, final Marker marker, final Object message, final Throwable t) {
        return this.privateConfig.filter(level, marker, message, t);
    }
    
    @Override
    public boolean isEnabled(final Level level, final Marker marker, final Message message, final Throwable t) {
        return this.privateConfig.filter(level, marker, message, t);
    }
    
    public void addAppender(final Appender appender) {
        this.privateConfig.config.addLoggerAppender(this, appender);
    }
    
    public void removeAppender(final Appender appender) {
        this.privateConfig.loggerConfig.removeAppender(appender.getName());
    }
    
    public Map<String, Appender> getAppenders() {
        return this.privateConfig.loggerConfig.getAppenders();
    }
    
    public Iterator<Filter> getFilters() {
        final Filter filter = this.privateConfig.loggerConfig.getFilter();
        if (filter == null) {
            return new ArrayList<Filter>().iterator();
        }
        if (filter instanceof CompositeFilter) {
            return ((CompositeFilter)filter).iterator();
        }
        final List<Filter> filters = new ArrayList<Filter>();
        filters.add(filter);
        return filters.iterator();
    }
    
    @Override
    public Level getLevel() {
        return this.privateConfig.loggerConfigLevel;
    }
    
    public int filterCount() {
        final Filter filter = this.privateConfig.loggerConfig.getFilter();
        if (filter == null) {
            return 0;
        }
        if (filter instanceof CompositeFilter) {
            return ((CompositeFilter)filter).size();
        }
        return 1;
    }
    
    public void addFilter(final Filter filter) {
        this.privateConfig.config.addLoggerFilter(this, filter);
    }
    
    public boolean isAdditive() {
        return this.privateConfig.loggerConfig.isAdditive();
    }
    
    public void setAdditive(final boolean additive) {
        this.privateConfig.config.setLoggerAdditive(this, additive);
    }
    
    protected void updateConfiguration(final Configuration newConfig) {
        this.privateConfig = new PrivateConfig(newConfig, this);
    }
    
    @Override
    public String toString() {
        final String nameLevel = "" + this.getName() + ':' + this.getLevel();
        if (this.context == null) {
            return nameLevel;
        }
        final String contextName = this.context.getName();
        return (contextName == null) ? nameLevel : (nameLevel + " in " + contextName);
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final Logger that = (Logger)o;
        return this.getName().equals(that.getName());
    }
    
    @Override
    public int hashCode() {
        return this.getName().hashCode();
    }
    
    protected class PrivateConfig
    {
        public final LoggerConfig loggerConfig;
        public final Configuration config;
        private final Level loggerConfigLevel;
        private final int intLevel;
        private final Logger logger;
        
        public PrivateConfig(final Configuration config, final Logger logger) {
            this.config = config;
            this.loggerConfig = config.getLoggerConfig(Logger.this.getName());
            this.loggerConfigLevel = this.loggerConfig.getLevel();
            this.intLevel = this.loggerConfigLevel.intLevel();
            this.logger = logger;
        }
        
        public PrivateConfig(final PrivateConfig pc, final Level level) {
            this.config = pc.config;
            this.loggerConfig = pc.loggerConfig;
            this.loggerConfigLevel = level;
            this.intLevel = this.loggerConfigLevel.intLevel();
            this.logger = pc.logger;
        }
        
        public PrivateConfig(final PrivateConfig pc, final LoggerConfig lc) {
            this.config = pc.config;
            this.loggerConfig = lc;
            this.loggerConfigLevel = lc.getLevel();
            this.intLevel = this.loggerConfigLevel.intLevel();
            this.logger = pc.logger;
        }
        
        public void logEvent(final LogEvent event) {
            this.loggerConfig.log(event);
        }
        
        boolean filter(final Level level, final Marker marker, final String msg) {
            final Filter filter = this.config.getFilter();
            if (filter != null) {
                final Filter.Result r = filter.filter(this.logger, level, marker, msg, new Object[0]);
                if (r != Filter.Result.NEUTRAL) {
                    return r == Filter.Result.ACCEPT;
                }
            }
            return level != null && this.intLevel >= level.intLevel();
        }
        
        boolean filter(final Level level, final Marker marker, final String msg, final Throwable t) {
            final Filter filter = this.config.getFilter();
            if (filter != null) {
                final Filter.Result r = filter.filter(this.logger, level, marker, (Object)msg, t);
                if (r != Filter.Result.NEUTRAL) {
                    return r == Filter.Result.ACCEPT;
                }
            }
            return level != null && this.intLevel >= level.intLevel();
        }
        
        boolean filter(final Level level, final Marker marker, final String msg, final Object... p1) {
            final Filter filter = this.config.getFilter();
            if (filter != null) {
                final Filter.Result r = filter.filter(this.logger, level, marker, msg, p1);
                if (r != Filter.Result.NEUTRAL) {
                    return r == Filter.Result.ACCEPT;
                }
            }
            return level != null && this.intLevel >= level.intLevel();
        }
        
        boolean filter(final Level level, final Marker marker, final Object msg, final Throwable t) {
            final Filter filter = this.config.getFilter();
            if (filter != null) {
                final Filter.Result r = filter.filter(this.logger, level, marker, msg, t);
                if (r != Filter.Result.NEUTRAL) {
                    return r == Filter.Result.ACCEPT;
                }
            }
            return level != null && this.intLevel >= level.intLevel();
        }
        
        boolean filter(final Level level, final Marker marker, final Message msg, final Throwable t) {
            final Filter filter = this.config.getFilter();
            if (filter != null) {
                final Filter.Result r = filter.filter(this.logger, level, marker, msg, t);
                if (r != Filter.Result.NEUTRAL) {
                    return r == Filter.Result.ACCEPT;
                }
            }
            return level != null && this.intLevel >= level.intLevel();
        }
    }
    
    protected static class LoggerProxy implements Serializable
    {
        private static final long serialVersionUID = 1L;
        private final String name;
        private final MessageFactory messageFactory;
        
        public LoggerProxy(final String name, final MessageFactory messageFactory) {
            this.name = name;
            this.messageFactory = messageFactory;
        }
        
        protected Object readResolve() throws ObjectStreamException {
            return new Logger(LoggerContext.getContext(), this.name, this.messageFactory);
        }
    }
}
