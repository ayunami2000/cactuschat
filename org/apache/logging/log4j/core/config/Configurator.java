// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.config;

import org.apache.logging.log4j.status.StatusLogger;
import java.util.Iterator;
import java.util.Map;
import org.apache.logging.log4j.Level;
import java.net.URI;
import org.apache.logging.log4j.core.util.NetUtils;
import org.apache.logging.log4j.util.Strings;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.spi.LoggerContextFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.impl.Log4jContextFactory;
import org.apache.logging.log4j.Logger;

public final class Configurator
{
    private static final String FQCN;
    private static final Logger LOGGER;
    
    private static Log4jContextFactory getFactory() {
        final LoggerContextFactory factory = LogManager.getFactory();
        if (factory instanceof Log4jContextFactory) {
            return (Log4jContextFactory)factory;
        }
        if (factory != null) {
            Configurator.LOGGER.error("LogManager returned an instance of {} which does not implement {}. Unable to initialize Log4j.", new Object[] { factory.getClass().getName(), Log4jContextFactory.class.getName() });
            return null;
        }
        Configurator.LOGGER.fatal("LogManager did not return a LoggerContextFactory. This indicates something has gone terribly wrong!");
        return null;
    }
    
    public static LoggerContext initialize(final ClassLoader loader, final ConfigurationSource source) {
        return initialize(loader, source, null);
    }
    
    public static LoggerContext initialize(final ClassLoader loader, final ConfigurationSource source, final Object externalContext) {
        try {
            final Log4jContextFactory factory = getFactory();
            return (factory == null) ? null : factory.getContext(Configurator.FQCN, loader, externalContext, false, source);
        }
        catch (Exception ex) {
            Configurator.LOGGER.error("There was a problem obtaining a LoggerContext using the configuration source [{}]", new Object[] { source, ex });
            return null;
        }
    }
    
    public static LoggerContext initialize(final String name, final ClassLoader loader, final String configLocation) {
        return initialize(name, loader, configLocation, null);
    }
    
    public static LoggerContext initialize(final String name, final ClassLoader loader, final String configLocation, final Object externalContext) {
        final URI uri = Strings.isBlank(configLocation) ? null : NetUtils.toURI(configLocation);
        return initialize(name, loader, uri, externalContext);
    }
    
    public static LoggerContext initialize(final String name, final ClassLoader loader, final URI configLocation) {
        return initialize(name, loader, configLocation, null);
    }
    
    public static LoggerContext initialize(final String name, final ClassLoader loader, final URI configLocation, final Object externalContext) {
        try {
            final Log4jContextFactory factory = getFactory();
            return (factory == null) ? null : factory.getContext(Configurator.FQCN, loader, externalContext, false, configLocation, name);
        }
        catch (Exception ex) {
            Configurator.LOGGER.error("There was a problem initializing the LoggerContext [{}] using configuration at [{}].", new Object[] { name, configLocation, ex });
            return null;
        }
    }
    
    public static LoggerContext initialize(final String name, final String configLocation) {
        return initialize(name, null, configLocation);
    }
    
    public static LoggerContext initialize(final Configuration configuration) {
        return initialize(null, configuration, null);
    }
    
    public static LoggerContext initialize(final ClassLoader loader, final Configuration configuration) {
        return initialize(loader, configuration, null);
    }
    
    public static LoggerContext initialize(final ClassLoader loader, final Configuration configuration, final Object externalContext) {
        try {
            final Log4jContextFactory factory = getFactory();
            return (factory == null) ? null : factory.getContext(Configurator.FQCN, loader, externalContext, false, configuration);
        }
        catch (Exception ex) {
            Configurator.LOGGER.error("There was a problem initializing the LoggerContext using configuration {}", new Object[] { configuration.getName(), ex });
            return null;
        }
    }
    
    public static void setAllLevels(final String parentLogger, final Level level) {
        final LoggerContext loggerContext = LoggerContext.getContext(false);
        final Configuration config = loggerContext.getConfiguration();
        boolean set = setLevel(parentLogger, level, config);
        for (final Map.Entry<String, LoggerConfig> entry : config.getLoggers().entrySet()) {
            if (entry.getKey().startsWith(parentLogger)) {
                set |= setLevel(entry.getValue(), level);
            }
        }
        if (set) {
            loggerContext.updateLoggers();
        }
    }
    
    private static boolean setLevel(final LoggerConfig loggerConfig, final Level level) {
        final boolean set = !loggerConfig.getLevel().equals(level);
        if (set) {
            loggerConfig.setLevel(level);
        }
        return set;
    }
    
    public static void setLevel(final Map<String, Level> levelMap) {
        final LoggerContext loggerContext = LoggerContext.getContext(false);
        final Configuration config = loggerContext.getConfiguration();
        boolean set = false;
        for (final Map.Entry<String, Level> entry : levelMap.entrySet()) {
            final String loggerName = entry.getKey();
            final Level level = entry.getValue();
            set |= setLevel(loggerName, level, config);
        }
        if (set) {
            loggerContext.updateLoggers();
        }
    }
    
    public static void setLevel(final String loggerName, final Level level) {
        final LoggerContext loggerContext = LoggerContext.getContext(false);
        if (Strings.isEmpty(loggerName)) {
            setRootLevel(level);
        }
        else if (setLevel(loggerName, level, loggerContext.getConfiguration())) {
            loggerContext.updateLoggers();
        }
    }
    
    private static boolean setLevel(final String loggerName, final Level level, final Configuration config) {
        LoggerConfig loggerConfig = config.getLoggerConfig(loggerName);
        boolean set;
        if (!loggerName.equals(loggerConfig.getName())) {
            loggerConfig = new LoggerConfig(loggerName, level, true);
            config.addLogger(loggerName, loggerConfig);
            loggerConfig.setLevel(level);
            set = true;
        }
        else {
            set = setLevel(loggerConfig, level);
        }
        return set;
    }
    
    public static void setRootLevel(final Level level) {
        final LoggerContext loggerContext = LoggerContext.getContext(false);
        final LoggerConfig loggerConfig = loggerContext.getConfiguration().getRootLogger();
        if (!loggerConfig.getLevel().equals(level)) {
            loggerConfig.setLevel(level);
            loggerContext.updateLoggers();
        }
    }
    
    public static void shutdown(final LoggerContext ctx) {
        if (ctx != null) {
            ctx.stop();
        }
    }
    
    private Configurator() {
    }
    
    static {
        FQCN = Configurator.class.getName();
        LOGGER = StatusLogger.getLogger();
    }
}
