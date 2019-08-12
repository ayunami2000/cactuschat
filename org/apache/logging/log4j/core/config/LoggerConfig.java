// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.config;

import org.apache.logging.log4j.core.impl.DefaultLogEventFactory;
import org.apache.logging.log4j.core.util.Loader;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.apache.logging.log4j.core.async.AsyncLoggerContextSelector;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.util.Booleans;
import java.util.Arrays;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.util.Strings;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.Marker;
import java.util.Collections;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import org.apache.logging.log4j.core.Appender;
import java.util.HashMap;
import org.apache.logging.log4j.core.Filter;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.ArrayList;
import java.util.Map;
import org.apache.logging.log4j.Level;
import java.util.Set;
import java.util.List;
import org.apache.logging.log4j.core.impl.LogEventFactory;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.filter.AbstractFilterable;

@Plugin(name = "logger", category = "Core", printObject = true)
public class LoggerConfig extends AbstractFilterable
{
    public static final String ROOT = "root";
    private static final long serialVersionUID = 1L;
    private static LogEventFactory LOG_EVENT_FACTORY;
    private List<AppenderRef> appenderRefs;
    private final Set<AppenderControl> appenders;
    private final String name;
    private LogEventFactory logEventFactory;
    private Level level;
    private boolean additive;
    private boolean includeLocation;
    private LoggerConfig parent;
    private final Map<Property, Boolean> properties;
    private final Configuration config;
    private final ReliabilityStrategy reliabilityStrategy;
    
    public LoggerConfig() {
        this.appenderRefs = new ArrayList<AppenderRef>();
        this.appenders = new CopyOnWriteArraySet<AppenderControl>();
        this.additive = true;
        this.includeLocation = true;
        this.logEventFactory = LoggerConfig.LOG_EVENT_FACTORY;
        this.level = Level.ERROR;
        this.name = "";
        this.properties = null;
        this.config = null;
        this.reliabilityStrategy = new DefaultReliabilityStrategy(this);
    }
    
    public LoggerConfig(final String name, final Level level, final boolean additive) {
        this.appenderRefs = new ArrayList<AppenderRef>();
        this.appenders = new CopyOnWriteArraySet<AppenderControl>();
        this.additive = true;
        this.includeLocation = true;
        this.logEventFactory = LoggerConfig.LOG_EVENT_FACTORY;
        this.name = name;
        this.level = level;
        this.additive = additive;
        this.properties = null;
        this.config = null;
        this.reliabilityStrategy = new DefaultReliabilityStrategy(this);
    }
    
    protected LoggerConfig(final String name, final List<AppenderRef> appenders, final Filter filter, final Level level, final boolean additive, final Property[] properties, final Configuration config, final boolean includeLocation) {
        super(filter);
        this.appenderRefs = new ArrayList<AppenderRef>();
        this.appenders = new CopyOnWriteArraySet<AppenderControl>();
        this.additive = true;
        this.includeLocation = true;
        this.logEventFactory = LoggerConfig.LOG_EVENT_FACTORY;
        this.name = name;
        this.appenderRefs = appenders;
        this.level = level;
        this.additive = additive;
        this.includeLocation = includeLocation;
        this.config = config;
        if (properties != null && properties.length > 0) {
            this.properties = new HashMap<Property, Boolean>(properties.length);
            for (final Property prop : properties) {
                final boolean interpolate = prop.getValue().contains("${");
                this.properties.put(prop, interpolate);
            }
        }
        else {
            this.properties = null;
        }
        this.reliabilityStrategy = config.getReliabilityStrategy(this);
    }
    
    @Override
    public Filter getFilter() {
        return super.getFilter();
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setParent(final LoggerConfig parent) {
        this.parent = parent;
    }
    
    public LoggerConfig getParent() {
        return this.parent;
    }
    
    public void addAppender(final Appender appender, final Level level, final Filter filter) {
        this.appenders.add(new AppenderControl(appender, level, filter));
    }
    
    public void removeAppender(final String name) {
        for (final AppenderControl appenderControl : this.appenders) {
            if (Objects.equals(name, appenderControl.getAppenderName()) && this.appenders.remove(appenderControl)) {
                this.cleanupFilter(appenderControl);
            }
        }
    }
    
    public Map<String, Appender> getAppenders() {
        final Map<String, Appender> map = new HashMap<String, Appender>();
        for (final AppenderControl appenderControl : this.appenders) {
            map.put(appenderControl.getAppenderName(), appenderControl.getAppender());
        }
        return map;
    }
    
    protected void clearAppenders() {
        for (List<AppenderControl> copy = new ArrayList<AppenderControl>(this.appenders); !copy.isEmpty(); copy = new ArrayList<AppenderControl>(this.appenders)) {
            this.appenders.removeAll(copy);
            for (final AppenderControl ctl : copy) {
                this.cleanupFilter(ctl);
            }
        }
    }
    
    private void cleanupFilter(final AppenderControl ctl) {
        final Filter filter = ctl.getFilter();
        if (filter != null) {
            ctl.removeFilter(filter);
            filter.stop();
        }
    }
    
    public List<AppenderRef> getAppenderRefs() {
        return this.appenderRefs;
    }
    
    public void setLevel(final Level level) {
        this.level = level;
    }
    
    public Level getLevel() {
        return (this.level == null) ? this.parent.getLevel() : this.level;
    }
    
    public LogEventFactory getLogEventFactory() {
        return this.logEventFactory;
    }
    
    public void setLogEventFactory(final LogEventFactory logEventFactory) {
        this.logEventFactory = logEventFactory;
    }
    
    public boolean isAdditive() {
        return this.additive;
    }
    
    public void setAdditive(final boolean additive) {
        this.additive = additive;
    }
    
    public boolean isIncludeLocation() {
        return this.includeLocation;
    }
    
    public Map<Property, Boolean> getProperties() {
        return (this.properties == null) ? null : Collections.unmodifiableMap((Map<? extends Property, ? extends Boolean>)this.properties);
    }
    
    public void log(final String loggerName, final String fqcn, final Marker marker, final Level level, final Message data, final Throwable t) {
        List<Property> props = null;
        if (this.properties != null) {
            props = new ArrayList<Property>(this.properties.size());
            final Log4jLogEvent.Builder builder = new Log4jLogEvent.Builder();
            builder.setMessage(data).setMarker(marker).setLevel(level).setLoggerName(loggerName);
            builder.setLoggerFqcn(fqcn).setThrown(t);
            final LogEvent event = builder.build();
            for (final Map.Entry<Property, Boolean> entry : this.properties.entrySet()) {
                final Property prop = entry.getKey();
                final String value = entry.getValue() ? this.config.getStrSubstitutor().replace(event, prop.getValue()) : prop.getValue();
                props.add(Property.createProperty(prop.getName(), value));
            }
        }
        this.log(this.logEventFactory.createEvent(loggerName, marker, fqcn, level, data, props, t));
    }
    
    public void log(final LogEvent event) {
        if (!this.isFiltered(event)) {
            this.processLogEvent(event);
        }
    }
    
    public ReliabilityStrategy getReliabilityStrategy() {
        return this.reliabilityStrategy;
    }
    
    private void processLogEvent(final LogEvent event) {
        event.setIncludeLocation(this.isIncludeLocation());
        this.callAppenders(event);
        this.logParent(event);
    }
    
    private void logParent(final LogEvent event) {
        if (this.additive && this.parent != null) {
            this.parent.log(event);
        }
    }
    
    protected void callAppenders(final LogEvent event) {
        for (final AppenderControl control : this.appenders) {
            control.callAppender(event);
        }
    }
    
    @Override
    public String toString() {
        return Strings.isEmpty(this.name) ? "root" : this.name;
    }
    
    @PluginFactory
    public static LoggerConfig createLogger(@PluginAttribute("additivity") final String additivity, @PluginAttribute("level") final Level level, @PluginAttribute("name") final String loggerName, @PluginAttribute("includeLocation") final String includeLocation, @PluginElement("AppenderRef") final AppenderRef[] refs, @PluginElement("Properties") final Property[] properties, @PluginConfiguration final Configuration config, @PluginElement("Filter") final Filter filter) {
        if (loggerName == null) {
            LoggerConfig.LOGGER.error("Loggers cannot be configured without a name");
            return null;
        }
        final List<AppenderRef> appenderRefs = Arrays.asList(refs);
        final String name = loggerName.equals("root") ? "" : loggerName;
        final boolean additive = Booleans.parseBoolean(additivity, true);
        return new LoggerConfig(name, appenderRefs, filter, level, additive, properties, config, includeLocation(includeLocation));
    }
    
    protected static boolean includeLocation(final String includeLocationConfigValue) {
        if (includeLocationConfigValue == null) {
            final boolean sync = !AsyncLoggerContextSelector.isSelected();
            return sync;
        }
        return Boolean.parseBoolean(includeLocationConfigValue);
    }
    
    static {
        LoggerConfig.LOG_EVENT_FACTORY = null;
        final String factory = PropertiesUtil.getProperties().getStringProperty("Log4jLogEventFactory");
        if (factory != null) {
            try {
                final Class<?> clazz = Loader.loadClass(factory);
                if (clazz != null && LogEventFactory.class.isAssignableFrom(clazz)) {
                    LoggerConfig.LOG_EVENT_FACTORY = (LogEventFactory)clazz.newInstance();
                }
            }
            catch (Exception ex) {
                LoggerConfig.LOGGER.error("Unable to create LogEventFactory {}", new Object[] { factory, ex });
            }
        }
        if (LoggerConfig.LOG_EVENT_FACTORY == null) {
            LoggerConfig.LOG_EVENT_FACTORY = new DefaultLogEventFactory();
        }
    }
    
    @Plugin(name = "root", category = "Core", printObject = true)
    public static class RootLogger extends LoggerConfig
    {
        private static final long serialVersionUID = 1L;
        
        @PluginFactory
        public static LoggerConfig createLogger(@PluginAttribute("additivity") final String additivity, @PluginAttribute("level") final Level level, @PluginAttribute("includeLocation") final String includeLocation, @PluginElement("AppenderRef") final AppenderRef[] refs, @PluginElement("Properties") final Property[] properties, @PluginConfiguration final Configuration config, @PluginElement("Filter") final Filter filter) {
            final List<AppenderRef> appenderRefs = Arrays.asList(refs);
            final Level actualLevel = (level == null) ? Level.ERROR : level;
            final boolean additive = Booleans.parseBoolean(additivity, true);
            return new LoggerConfig("", appenderRefs, filter, actualLevel, additive, properties, config, LoggerConfig.includeLocation(includeLocation));
        }
    }
}
