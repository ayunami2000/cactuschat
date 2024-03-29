// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j;

import java.util.Iterator;
import java.util.SortedMap;
import java.util.Map;
import org.apache.logging.log4j.simple.SimpleLoggerContextFactory;
import org.apache.logging.log4j.spi.Provider;
import org.apache.logging.log4j.util.ProviderUtil;
import java.util.TreeMap;
import org.apache.logging.log4j.util.LoaderUtil;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.message.StringFormatterMessageFactory;
import org.apache.logging.log4j.util.ReflectionUtil;
import java.net.URI;
import org.apache.logging.log4j.spi.LoggerContext;
import org.apache.logging.log4j.spi.LoggerContextFactory;

public class LogManager
{
    public static final String FACTORY_PROPERTY_NAME = "log4j2.loggerContextFactory";
    public static final String ROOT_LOGGER_NAME = "";
    private static final Logger LOGGER;
    private static final String FQCN;
    private static volatile LoggerContextFactory factory;
    
    protected LogManager() {
    }
    
    public static boolean exists(final String name) {
        return getContext().hasLogger(name);
    }
    
    public static LoggerContext getContext() {
        return LogManager.factory.getContext(LogManager.FQCN, null, null, true);
    }
    
    public static LoggerContext getContext(final boolean currentContext) {
        return LogManager.factory.getContext(LogManager.FQCN, null, null, currentContext, null, null);
    }
    
    public static LoggerContext getContext(final ClassLoader loader, final boolean currentContext) {
        return LogManager.factory.getContext(LogManager.FQCN, loader, null, currentContext);
    }
    
    public static LoggerContext getContext(final ClassLoader loader, final boolean currentContext, final Object externalContext) {
        return LogManager.factory.getContext(LogManager.FQCN, loader, externalContext, currentContext);
    }
    
    public static LoggerContext getContext(final ClassLoader loader, final boolean currentContext, final URI configLocation) {
        return LogManager.factory.getContext(LogManager.FQCN, loader, null, currentContext, configLocation, null);
    }
    
    public static LoggerContext getContext(final ClassLoader loader, final boolean currentContext, final Object externalContext, final URI configLocation) {
        return LogManager.factory.getContext(LogManager.FQCN, loader, externalContext, currentContext, configLocation, null);
    }
    
    public static LoggerContext getContext(final ClassLoader loader, final boolean currentContext, final Object externalContext, final URI configLocation, final String name) {
        return LogManager.factory.getContext(LogManager.FQCN, loader, externalContext, currentContext, configLocation, name);
    }
    
    protected static LoggerContext getContext(final String fqcn, final boolean currentContext) {
        return LogManager.factory.getContext(fqcn, null, null, currentContext);
    }
    
    protected static LoggerContext getContext(final String fqcn, final ClassLoader loader, final boolean currentContext) {
        return LogManager.factory.getContext(fqcn, loader, null, currentContext);
    }
    
    public static LoggerContextFactory getFactory() {
        return LogManager.factory;
    }
    
    public static void setFactory(final LoggerContextFactory factory) {
        LogManager.factory = factory;
    }
    
    public static Logger getFormatterLogger() {
        return getFormatterLogger(ReflectionUtil.getCallerClass(2));
    }
    
    public static Logger getFormatterLogger(final Class<?> clazz) {
        return getLogger((clazz != null) ? clazz : ReflectionUtil.getCallerClass(2), StringFormatterMessageFactory.INSTANCE);
    }
    
    public static Logger getFormatterLogger(final Object value) {
        return getLogger((value != null) ? value.getClass() : ReflectionUtil.getCallerClass(2), StringFormatterMessageFactory.INSTANCE);
    }
    
    public static Logger getFormatterLogger(final String name) {
        return (name == null) ? getFormatterLogger(ReflectionUtil.getCallerClass(2)) : getLogger(name, StringFormatterMessageFactory.INSTANCE);
    }
    
    private static Class<?> callerClass(final Class<?> clazz) {
        if (clazz != null) {
            return clazz;
        }
        final Class<?> candidate = ReflectionUtil.getCallerClass(3);
        if (candidate == null) {
            throw new UnsupportedOperationException("No class provided, and an appropriate one cannot be found.");
        }
        return candidate;
    }
    
    public static Logger getLogger() {
        return getLogger(ReflectionUtil.getCallerClass(2));
    }
    
    public static Logger getLogger(final Class<?> clazz) {
        final Class<?> cls = callerClass(clazz);
        return getContext(cls.getClassLoader(), false).getLogger(cls.getName());
    }
    
    public static Logger getLogger(final Class<?> clazz, final MessageFactory messageFactory) {
        final Class<?> cls = callerClass(clazz);
        return getContext(cls.getClassLoader(), false).getLogger(cls.getName(), messageFactory);
    }
    
    public static Logger getLogger(final MessageFactory messageFactory) {
        return getLogger(ReflectionUtil.getCallerClass(2), messageFactory);
    }
    
    public static Logger getLogger(final Object value) {
        return getLogger((value != null) ? value.getClass() : ReflectionUtil.getCallerClass(2));
    }
    
    public static Logger getLogger(final Object value, final MessageFactory messageFactory) {
        return getLogger((value != null) ? value.getClass() : ReflectionUtil.getCallerClass(2), messageFactory);
    }
    
    public static Logger getLogger(final String name) {
        return (name != null) ? getContext(false).getLogger(name) : getLogger(ReflectionUtil.getCallerClass(2));
    }
    
    public static Logger getLogger(final String name, final MessageFactory messageFactory) {
        return (name != null) ? getContext(false).getLogger(name, messageFactory) : getLogger(ReflectionUtil.getCallerClass(2), messageFactory);
    }
    
    protected static Logger getLogger(final String fqcn, final String name) {
        return LogManager.factory.getContext(fqcn, null, null, false).getLogger(name);
    }
    
    public static Logger getRootLogger() {
        return getLogger("");
    }
    
    static {
        LOGGER = StatusLogger.getLogger();
        FQCN = LogManager.class.getName();
        final PropertiesUtil managerProps = PropertiesUtil.getProperties();
        final String factoryClassName = managerProps.getStringProperty("log4j2.loggerContextFactory");
        if (factoryClassName != null) {
            try {
                LogManager.factory = LoaderUtil.newCheckedInstanceOf(factoryClassName, LoggerContextFactory.class);
            }
            catch (ClassNotFoundException cnfe) {
                LogManager.LOGGER.error("Unable to locate configured LoggerContextFactory {}", new Object[] { factoryClassName });
            }
            catch (Exception ex) {
                LogManager.LOGGER.error("Unable to create configured LoggerContextFactory {}", new Object[] { factoryClassName, ex });
            }
        }
        if (LogManager.factory == null) {
            final SortedMap<Integer, LoggerContextFactory> factories = new TreeMap<Integer, LoggerContextFactory>();
            if (ProviderUtil.hasProviders()) {
                for (final Provider provider : ProviderUtil.getProviders()) {
                    final Class<? extends LoggerContextFactory> factoryClass = provider.loadLoggerContextFactory();
                    if (factoryClass != null) {
                        try {
                            factories.put(provider.getPriority(), (LoggerContextFactory)factoryClass.newInstance());
                        }
                        catch (Exception e) {
                            LogManager.LOGGER.error("Unable to create class {} specified in {}", new Object[] { factoryClass.getName(), provider.getUrl().toString(), e });
                        }
                    }
                }
                if (factories.isEmpty()) {
                    LogManager.LOGGER.error("Log4j2 could not find a logging implementation. Please add log4j-core to the classpath. Using SimpleLogger to log to the console...");
                    LogManager.factory = new SimpleLoggerContextFactory();
                }
                else if (factories.size() == 1) {
                    LogManager.factory = factories.get(factories.lastKey());
                }
                else {
                    final StringBuilder sb = new StringBuilder("Multiple logging implementations found: \n");
                    for (final Map.Entry<Integer, LoggerContextFactory> entry : factories.entrySet()) {
                        sb.append("Factory: ").append(entry.getValue().getClass().getName());
                        sb.append(", Weighting: ").append(entry.getKey()).append('\n');
                    }
                    LogManager.factory = factories.get(factories.lastKey());
                    sb.append("Using factory: ").append(LogManager.factory.getClass().getName());
                    LogManager.LOGGER.warn(sb.toString());
                }
            }
            else {
                LogManager.LOGGER.error("Log4j2 could not find a logging implementation. Please add log4j-core to the classpath. Using SimpleLogger to log to the console...");
                LogManager.factory = new SimpleLoggerContextFactory();
            }
        }
    }
}
