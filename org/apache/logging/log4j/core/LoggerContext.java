// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core;

import org.apache.logging.log4j.core.config.NullConfiguration;
import org.apache.logging.log4j.core.config.Reconfigurable;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import java.util.Iterator;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.util.NanoClockFactory;
import java.beans.PropertyChangeEvent;
import org.apache.logging.log4j.core.util.NetUtils;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.apache.logging.log4j.spi.AbstractLogger;
import org.apache.logging.log4j.spi.LoggerContextKey;
import java.util.Collection;
import org.apache.logging.log4j.message.MessageFactory;
import java.util.Objects;
import org.apache.logging.log4j.core.jmx.Server;
import org.apache.logging.log4j.spi.LoggerContextFactory;
import org.apache.logging.log4j.core.util.ShutdownCallbackRegistry;
import org.apache.logging.log4j.LogManager;
import java.io.File;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import org.apache.logging.log4j.core.util.Cancellable;
import java.net.URI;
import java.beans.PropertyChangeListener;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentMap;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationListener;

public class LoggerContext extends AbstractLifeCycle implements org.apache.logging.log4j.spi.LoggerContext, ConfigurationListener
{
    public static final String PROPERTY_CONFIG = "config";
    private static final long serialVersionUID = 1L;
    private static final Configuration NULL_CONFIGURATION;
    private final ConcurrentMap<String, Logger> loggers;
    private final CopyOnWriteArrayList<PropertyChangeListener> propertyChangeListeners;
    private volatile Configuration configuration;
    private Object externalContext;
    private String contextName;
    private volatile URI configLocation;
    private Cancellable shutdownCallback;
    private final Lock configLock;
    
    public LoggerContext(final String name) {
        this(name, null, (URI)null);
    }
    
    public LoggerContext(final String name, final Object externalContext) {
        this(name, externalContext, (URI)null);
    }
    
    public LoggerContext(final String name, final Object externalContext, final URI configLocn) {
        this.loggers = new ConcurrentHashMap<String, Logger>();
        this.propertyChangeListeners = new CopyOnWriteArrayList<PropertyChangeListener>();
        this.configuration = new DefaultConfiguration();
        this.configLock = new ReentrantLock();
        this.contextName = name;
        this.externalContext = externalContext;
        this.configLocation = configLocn;
    }
    
    public LoggerContext(final String name, final Object externalContext, final String configLocn) {
        this.loggers = new ConcurrentHashMap<String, Logger>();
        this.propertyChangeListeners = new CopyOnWriteArrayList<PropertyChangeListener>();
        this.configuration = new DefaultConfiguration();
        this.configLock = new ReentrantLock();
        this.contextName = name;
        this.externalContext = externalContext;
        if (configLocn != null) {
            URI uri;
            try {
                uri = new File(configLocn).toURI();
            }
            catch (Exception ex) {
                uri = null;
            }
            this.configLocation = uri;
        }
        else {
            this.configLocation = null;
        }
    }
    
    public static LoggerContext getContext() {
        return (LoggerContext)LogManager.getContext();
    }
    
    public static LoggerContext getContext(final boolean currentContext) {
        return (LoggerContext)LogManager.getContext(currentContext);
    }
    
    public static LoggerContext getContext(final ClassLoader loader, final boolean currentContext, final URI configLocation) {
        return (LoggerContext)LogManager.getContext(loader, currentContext, configLocation);
    }
    
    @Override
    public void start() {
        LoggerContext.LOGGER.debug("Starting LoggerContext[name={}, {}]...", new Object[] { this.getName(), this });
        if (this.configLock.tryLock()) {
            try {
                if (this.isInitialized() || this.isStopped()) {
                    this.setStarting();
                    this.reconfigure();
                    if (this.configuration.isShutdownHookEnabled()) {
                        this.setUpShutdownHook();
                    }
                    this.setStarted();
                }
            }
            finally {
                this.configLock.unlock();
            }
        }
        LoggerContext.LOGGER.debug("LoggerContext[name={}, {}] started OK.", new Object[] { this.getName(), this });
    }
    
    public void start(final Configuration config) {
        LoggerContext.LOGGER.debug("Starting LoggerContext[name={}, {}] with configuration {}...", new Object[] { this.getName(), this, config });
        if (this.configLock.tryLock()) {
            try {
                if (this.isInitialized() || this.isStopped()) {
                    if (this.configuration.isShutdownHookEnabled()) {
                        this.setUpShutdownHook();
                    }
                    this.setStarted();
                }
            }
            finally {
                this.configLock.unlock();
            }
        }
        this.setConfiguration(config);
        LoggerContext.LOGGER.debug("LoggerContext[name={}, {}] started OK with configuration {}.", new Object[] { this.getName(), this, config });
    }
    
    private void setUpShutdownHook() {
        if (this.shutdownCallback == null) {
            final LoggerContextFactory factory = LogManager.getFactory();
            if (factory instanceof ShutdownCallbackRegistry) {
                LoggerContext.LOGGER.debug(ShutdownCallbackRegistry.SHUTDOWN_HOOK_MARKER, "Shutdown hook enabled. Registering a new one.");
                try {
                    this.shutdownCallback = ((ShutdownCallbackRegistry)factory).addShutdownCallback(new Runnable() {
                        @Override
                        public void run() {
                            final LoggerContext context = LoggerContext.this;
                            AbstractLifeCycle.LOGGER.debug(ShutdownCallbackRegistry.SHUTDOWN_HOOK_MARKER, "Stopping LoggerContext[name={}, {}]", new Object[] { context.getName(), context });
                            context.stop();
                        }
                        
                        @Override
                        public String toString() {
                            return "Shutdown callback for LoggerContext[name=" + LoggerContext.this.getName() + ']';
                        }
                    });
                }
                catch (IllegalStateException e) {
                    LoggerContext.LOGGER.error(ShutdownCallbackRegistry.SHUTDOWN_HOOK_MARKER, "Unable to register shutdown hook because JVM is shutting down.", e);
                }
                catch (SecurityException e2) {
                    LoggerContext.LOGGER.error(ShutdownCallbackRegistry.SHUTDOWN_HOOK_MARKER, "Unable to register shutdown hook due to security restrictions", e2);
                }
            }
        }
    }
    
    @Override
    public void stop() {
        LoggerContext.LOGGER.debug("Stopping LoggerContext[name={}, {}]...", new Object[] { this.getName(), this });
        this.configLock.lock();
        try {
            if (this.isStopped()) {
                return;
            }
            this.setStopping();
            try {
                Server.unregisterLoggerContext(this.getName());
            }
            catch (Exception ex) {
                LoggerContext.LOGGER.error("Unable to unregister MBeans", ex);
            }
            if (this.shutdownCallback != null) {
                this.shutdownCallback.cancel();
                this.shutdownCallback = null;
            }
            final Configuration prev = this.configuration;
            this.configuration = LoggerContext.NULL_CONFIGURATION;
            this.updateLoggers();
            prev.stop();
            this.externalContext = null;
            LogManager.getFactory().removeContext(this);
            this.setStopped();
        }
        finally {
            this.configLock.unlock();
        }
        LoggerContext.LOGGER.debug("Stopped LoggerContext[name={}, {}]...", new Object[] { this.getName(), this });
    }
    
    public String getName() {
        return this.contextName;
    }
    
    public Logger getRootLogger() {
        return this.getLogger("");
    }
    
    public void setName(final String name) {
        this.contextName = Objects.requireNonNull(name);
    }
    
    public void setExternalContext(final Object context) {
        this.externalContext = context;
    }
    
    @Override
    public Object getExternalContext() {
        return this.externalContext;
    }
    
    @Override
    public Logger getLogger(final String name) {
        return this.getLogger(name, null);
    }
    
    public Collection<Logger> getLoggers() {
        return this.loggers.values();
    }
    
    @Override
    public Logger getLogger(final String name, final MessageFactory messageFactory) {
        String key = LoggerContextKey.create(name, messageFactory);
        Logger logger = this.loggers.get(key);
        if (logger != null) {
            AbstractLogger.checkMessageFactory(logger, messageFactory);
            return logger;
        }
        logger = this.newInstance(this, name, messageFactory);
        key = LoggerContextKey.create(name, logger.getMessageFactory());
        final Logger prev = this.loggers.putIfAbsent(key, logger);
        return (prev == null) ? logger : prev;
    }
    
    @Override
    public boolean hasLogger(final String name) {
        return this.loggers.containsKey(LoggerContextKey.create(name));
    }
    
    @Override
    public boolean hasLogger(final String name, final MessageFactory messageFactory) {
        return this.loggers.containsKey(LoggerContextKey.create(name, messageFactory));
    }
    
    @Override
    public boolean hasLogger(final String name, final Class<? extends MessageFactory> messageFactoryClass) {
        return this.loggers.containsKey(LoggerContextKey.create(name, messageFactoryClass));
    }
    
    public Configuration getConfiguration() {
        return this.configuration;
    }
    
    public void addFilter(final Filter filter) {
        this.configuration.addFilter(filter);
    }
    
    public void removeFilter(final Filter filter) {
        this.configuration.removeFilter(filter);
    }
    
    private Configuration setConfiguration(final Configuration config) {
        Objects.requireNonNull(config, "No Configuration was provided");
        this.configLock.lock();
        try {
            final Configuration prev = this.configuration;
            config.addListener(this);
            final ConcurrentMap<String, String> map = config.getComponent("ContextProperties");
            try {
                map.putIfAbsent("hostName", NetUtils.getLocalHostname());
            }
            catch (Exception ex) {
                LoggerContext.LOGGER.debug("Ignoring {}, setting hostName to 'unknown'", new Object[] { ex.toString() });
                map.putIfAbsent("hostName", "unknown");
            }
            map.putIfAbsent("contextName", this.contextName);
            config.start();
            this.configuration = config;
            this.updateLoggers();
            if (prev != null) {
                prev.removeListener(this);
                prev.stop();
            }
            this.firePropertyChangeEvent(new PropertyChangeEvent(this, "config", prev, config));
            try {
                Server.reregisterMBeansAfterReconfigure();
            }
            catch (Throwable t) {
                LoggerContext.LOGGER.error("Could not reconfigure JMX", t);
            }
            Log4jLogEvent.setNanoClock(NanoClockFactory.createNanoClock());
            return prev;
        }
        finally {
            this.configLock.unlock();
        }
    }
    
    private void firePropertyChangeEvent(final PropertyChangeEvent event) {
        for (final PropertyChangeListener listener : this.propertyChangeListeners) {
            listener.propertyChange(event);
        }
    }
    
    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        this.propertyChangeListeners.add(Objects.requireNonNull(listener, "listener"));
    }
    
    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        this.propertyChangeListeners.remove(listener);
    }
    
    public URI getConfigLocation() {
        return this.configLocation;
    }
    
    public void setConfigLocation(final URI configLocation) {
        this.reconfigure(this.configLocation = configLocation);
    }
    
    private void reconfigure(final URI configURI) {
        final ClassLoader cl = ClassLoader.class.isInstance(this.externalContext) ? ((ClassLoader)this.externalContext) : null;
        LoggerContext.LOGGER.debug("Reconfiguration started for context[name={}] at URI {} ({}) with optional ClassLoader: {}", new Object[] { this.contextName, configURI, this, cl });
        final Configuration instance = ConfigurationFactory.getInstance().getConfiguration(this.contextName, configURI, cl);
        this.setConfiguration(instance);
        final String location = (this.configuration == null) ? "?" : String.valueOf(this.configuration.getConfigurationSource());
        LoggerContext.LOGGER.debug("Reconfiguration complete for context[name={}] at URI {} ({}) with optional ClassLoader: {}", new Object[] { this.contextName, location, this, cl });
    }
    
    public void reconfigure() {
        this.reconfigure(this.configLocation);
    }
    
    public void updateLoggers() {
        this.updateLoggers(this.configuration);
    }
    
    public void updateLoggers(final Configuration config) {
        for (final Logger logger : this.loggers.values()) {
            logger.updateConfiguration(config);
        }
    }
    
    @Override
    public synchronized void onChange(final Reconfigurable reconfigurable) {
        LoggerContext.LOGGER.debug("Reconfiguration started for context {} ({})", new Object[] { this.contextName, this });
        final Configuration newConfig = reconfigurable.reconfigure();
        if (newConfig != null) {
            this.setConfiguration(newConfig);
            LoggerContext.LOGGER.debug("Reconfiguration completed for {} ({})", new Object[] { this.contextName, this });
        }
        else {
            LoggerContext.LOGGER.debug("Reconfiguration failed for {} ({})", new Object[] { this.contextName, this });
        }
    }
    
    protected Logger newInstance(final LoggerContext ctx, final String name, final MessageFactory messageFactory) {
        return new Logger(ctx, name, messageFactory);
    }
    
    static {
        NULL_CONFIGURATION = new NullConfiguration();
    }
}
