// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.impl;

import org.apache.logging.log4j.util.PropertiesUtil;
import org.apache.logging.log4j.core.util.Cancellable;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import java.net.URI;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.LifeCycle;
import org.apache.logging.log4j.core.util.DefaultShutdownCallbackRegistry;
import org.apache.logging.log4j.core.selector.ClassLoaderContextSelector;
import org.apache.logging.log4j.util.LoaderUtil;
import java.util.Objects;
import org.apache.logging.log4j.core.selector.ContextSelector;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.core.util.ShutdownCallbackRegistry;
import org.apache.logging.log4j.spi.LoggerContextFactory;

public class Log4jContextFactory implements LoggerContextFactory, ShutdownCallbackRegistry
{
    private static final StatusLogger LOGGER;
    private static final boolean SHUTDOWN_HOOK_ENABLED;
    private final ContextSelector selector;
    private final ShutdownCallbackRegistry shutdownCallbackRegistry;
    
    public Log4jContextFactory() {
        this(createContextSelector(), createShutdownCallbackRegistry());
    }
    
    public Log4jContextFactory(final ContextSelector selector) {
        this(selector, createShutdownCallbackRegistry());
    }
    
    public Log4jContextFactory(final ShutdownCallbackRegistry shutdownCallbackRegistry) {
        this(createContextSelector(), shutdownCallbackRegistry);
    }
    
    public Log4jContextFactory(final ContextSelector selector, final ShutdownCallbackRegistry shutdownCallbackRegistry) {
        this.selector = Objects.requireNonNull(selector, "No ContextSelector provided");
        this.shutdownCallbackRegistry = Objects.requireNonNull(shutdownCallbackRegistry, "No ShutdownCallbackRegistry provided");
        Log4jContextFactory.LOGGER.debug("Using ShutdownCallbackRegistry {}", new Object[] { shutdownCallbackRegistry.getClass() });
        this.initializeShutdownCallbackRegistry();
    }
    
    private static ContextSelector createContextSelector() {
        try {
            final ContextSelector selector = LoaderUtil.newCheckedInstanceOfProperty("Log4jContextSelector", ContextSelector.class);
            if (selector != null) {
                return selector;
            }
        }
        catch (Exception e) {
            Log4jContextFactory.LOGGER.error("Unable to create custom ContextSelector. Falling back to default.", e);
        }
        return new ClassLoaderContextSelector();
    }
    
    private static ShutdownCallbackRegistry createShutdownCallbackRegistry() {
        try {
            final ShutdownCallbackRegistry registry = LoaderUtil.newCheckedInstanceOfProperty("log4j.shutdownCallbackRegistry", ShutdownCallbackRegistry.class);
            if (registry != null) {
                return registry;
            }
        }
        catch (Exception e) {
            Log4jContextFactory.LOGGER.error("Unable to create custom ShutdownCallbackRegistry. Falling back to default.", e);
        }
        return new DefaultShutdownCallbackRegistry();
    }
    
    private void initializeShutdownCallbackRegistry() {
        if (Log4jContextFactory.SHUTDOWN_HOOK_ENABLED && this.shutdownCallbackRegistry instanceof LifeCycle) {
            try {
                ((LifeCycle)this.shutdownCallbackRegistry).start();
            }
            catch (Exception e) {
                Log4jContextFactory.LOGGER.error("There was an error starting the ShutdownCallbackRegistry.", e);
            }
        }
    }
    
    @Override
    public LoggerContext getContext(final String fqcn, final ClassLoader loader, final Object externalContext, final boolean currentContext) {
        final LoggerContext ctx = this.selector.getContext(fqcn, loader, currentContext);
        if (externalContext != null && ctx.getExternalContext() == null) {
            ctx.setExternalContext(externalContext);
        }
        if (ctx.getState() == LifeCycle.State.INITIALIZED) {
            ctx.start();
        }
        return ctx;
    }
    
    public LoggerContext getContext(final String fqcn, final ClassLoader loader, final Object externalContext, final boolean currentContext, final ConfigurationSource source) {
        final LoggerContext ctx = this.selector.getContext(fqcn, loader, currentContext, null);
        if (externalContext != null && ctx.getExternalContext() == null) {
            ctx.setExternalContext(externalContext);
        }
        if (ctx.getState() == LifeCycle.State.INITIALIZED) {
            if (source != null) {
                ContextAnchor.THREAD_CONTEXT.set(ctx);
                final Configuration config = ConfigurationFactory.getInstance().getConfiguration(source);
                Log4jContextFactory.LOGGER.debug("Starting LoggerContext[name={}] from configuration {}", new Object[] { ctx.getName(), source });
                ctx.start(config);
                ContextAnchor.THREAD_CONTEXT.remove();
            }
            else {
                ctx.start();
            }
        }
        return ctx;
    }
    
    public LoggerContext getContext(final String fqcn, final ClassLoader loader, final Object externalContext, final boolean currentContext, final Configuration configuration) {
        final LoggerContext ctx = this.selector.getContext(fqcn, loader, currentContext, null);
        if (externalContext != null && ctx.getExternalContext() == null) {
            ctx.setExternalContext(externalContext);
        }
        if (ctx.getState() == LifeCycle.State.INITIALIZED) {
            ContextAnchor.THREAD_CONTEXT.set(ctx);
            try {
                ctx.start(configuration);
            }
            finally {
                ContextAnchor.THREAD_CONTEXT.remove();
            }
        }
        return ctx;
    }
    
    @Override
    public LoggerContext getContext(final String fqcn, final ClassLoader loader, final Object externalContext, final boolean currentContext, final URI configLocation, final String name) {
        final LoggerContext ctx = this.selector.getContext(fqcn, loader, currentContext, configLocation);
        if (externalContext != null && ctx.getExternalContext() == null) {
            ctx.setExternalContext(externalContext);
        }
        if (name != null) {
            ctx.setName(name);
        }
        if (ctx.getState() == LifeCycle.State.INITIALIZED) {
            if (configLocation != null || name != null) {
                ContextAnchor.THREAD_CONTEXT.set(ctx);
                final Configuration config = ConfigurationFactory.getInstance().getConfiguration(name, configLocation);
                Log4jContextFactory.LOGGER.debug("Starting LoggerContext[name={}] from configuration at {}", new Object[] { ctx.getName(), configLocation });
                ctx.start(config);
                ContextAnchor.THREAD_CONTEXT.remove();
            }
            else {
                ctx.start();
            }
        }
        return ctx;
    }
    
    public ContextSelector getSelector() {
        return this.selector;
    }
    
    public ShutdownCallbackRegistry getShutdownCallbackRegistry() {
        return this.shutdownCallbackRegistry;
    }
    
    @Override
    public void removeContext(final org.apache.logging.log4j.spi.LoggerContext context) {
        if (context instanceof LoggerContext) {
            this.selector.removeContext((LoggerContext)context);
        }
    }
    
    @Override
    public Cancellable addShutdownCallback(final Runnable callback) {
        return Log4jContextFactory.SHUTDOWN_HOOK_ENABLED ? this.shutdownCallbackRegistry.addShutdownCallback(callback) : null;
    }
    
    static {
        LOGGER = StatusLogger.getLogger();
        SHUTDOWN_HOOK_ENABLED = PropertiesUtil.getProperties().getBooleanProperty("log4j.shutdownHookEnabled", true);
    }
}
