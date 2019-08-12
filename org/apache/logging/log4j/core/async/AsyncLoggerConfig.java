// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.async;

import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.util.Booleans;
import java.util.Arrays;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.jmx.RingBufferAdmin;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.config.AppenderRef;
import java.util.List;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.LoggerConfig;

@Plugin(name = "asyncLogger", category = "Core", printObject = true)
public class AsyncLoggerConfig extends LoggerConfig
{
    private static final long serialVersionUID = 1L;
    private AsyncLoggerConfigDelegate delegate;
    
    protected AsyncLoggerConfig(final String name, final List<AppenderRef> appenders, final Filter filter, final Level level, final boolean additive, final Property[] properties, final Configuration config, final boolean includeLocation) {
        super(name, appenders, filter, level, additive, properties, config, includeLocation);
        this.delegate = config.getAsyncLoggerConfigDelegate();
    }
    
    @Override
    protected void callAppenders(final LogEvent event) {
        event.getSource();
        event.getThreadName();
        if (!this.delegate.tryCallAppendersInBackground(event, this)) {
            super.callAppenders(event);
        }
    }
    
    void asyncCallAppenders(final LogEvent event) {
        super.callAppenders(event);
    }
    
    private String displayName() {
        return "".equals(this.getName()) ? "root" : this.getName();
    }
    
    @Override
    public void start() {
        AsyncLoggerConfig.LOGGER.trace("AsyncLoggerConfig[{}] starting...", new Object[] { this.displayName() });
        super.start();
    }
    
    @Override
    public void stop() {
        AsyncLoggerConfig.LOGGER.trace("AsyncLoggerConfig[{}] stopping...", new Object[] { this.displayName() });
        super.stop();
    }
    
    public RingBufferAdmin createRingBufferAdmin(final String contextName) {
        return this.delegate.createRingBufferAdmin(contextName, this.getName());
    }
    
    @PluginFactory
    public static LoggerConfig createLogger(@PluginAttribute("additivity") final String additivity, @PluginAttribute("level") final String levelName, @PluginAttribute("name") final String loggerName, @PluginAttribute("includeLocation") final String includeLocation, @PluginElement("AppenderRef") final AppenderRef[] refs, @PluginElement("Properties") final Property[] properties, @PluginConfiguration final Configuration config, @PluginElement("Filter") final Filter filter) {
        if (loggerName == null) {
            AsyncLoggerConfig.LOGGER.error("Loggers cannot be configured without a name");
            return null;
        }
        final List<AppenderRef> appenderRefs = Arrays.asList(refs);
        Level level;
        try {
            level = Level.toLevel(levelName, Level.ERROR);
        }
        catch (Exception ex) {
            AsyncLoggerConfig.LOGGER.error("Invalid Log level specified: {}. Defaulting to Error", new Object[] { levelName });
            level = Level.ERROR;
        }
        final String name = loggerName.equals("root") ? "" : loggerName;
        final boolean additive = Booleans.parseBoolean(additivity, true);
        return new AsyncLoggerConfig(name, appenderRefs, filter, level, additive, properties, config, includeLocation(includeLocation));
    }
    
    protected static boolean includeLocation(final String includeLocationConfigValue) {
        return Boolean.parseBoolean(includeLocationConfigValue);
    }
    
    @Plugin(name = "asyncRoot", category = "Core", printObject = true)
    public static class RootLogger extends LoggerConfig
    {
        private static final long serialVersionUID = 1L;
        
        @PluginFactory
        public static LoggerConfig createLogger(@PluginAttribute("additivity") final String additivity, @PluginAttribute("level") final String levelName, @PluginAttribute("includeLocation") final String includeLocation, @PluginElement("AppenderRef") final AppenderRef[] refs, @PluginElement("Properties") final Property[] properties, @PluginConfiguration final Configuration config, @PluginElement("Filter") final Filter filter) {
            final List<AppenderRef> appenderRefs = Arrays.asList(refs);
            Level level;
            try {
                level = Level.toLevel(levelName, Level.ERROR);
            }
            catch (Exception ex) {
                RootLogger.LOGGER.error("Invalid Log level specified: {}. Defaulting to Error", new Object[] { levelName });
                level = Level.ERROR;
            }
            final boolean additive = Booleans.parseBoolean(additivity, true);
            return new AsyncLoggerConfig("", appenderRefs, filter, level, additive, properties, config, AsyncLoggerConfig.includeLocation(includeLocation));
        }
    }
}
