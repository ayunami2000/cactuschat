// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.config.builder.impl;

import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.config.builder.api.AppenderRefComponentBuilder;
import org.apache.logging.log4j.core.config.Configuration;
import java.lang.reflect.Constructor;
import java.util.Iterator;
import org.apache.logging.log4j.core.config.ConfigurationException;
import org.apache.logging.log4j.core.config.builder.api.RootLoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.LoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ScriptFileComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ScriptComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.FilterComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.CustomLevelComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ComponentBuilder;
import java.util.List;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.builder.api.Component;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;

public class DefaultConfigurationBuilder<T extends BuiltConfiguration> implements ConfigurationBuilder<T>
{
    private final Component root;
    private Component loggers;
    private Component appenders;
    private Component filters;
    private Component properties;
    private Component customLevels;
    private Component scripts;
    private final Class<T> clazz;
    private ConfigurationSource source;
    private int monitorInterval;
    private Level level;
    private String verbosity;
    private String packages;
    private String shutdownFlag;
    private String advertiser;
    private String name;
    
    public DefaultConfigurationBuilder() {
        this(BuiltConfiguration.class);
        this.root.addAttribute("name", "Built");
    }
    
    public DefaultConfigurationBuilder(final Class<T> clazz) {
        this.root = new Component();
        this.monitorInterval = 0;
        this.level = null;
        this.verbosity = null;
        this.packages = null;
        this.shutdownFlag = null;
        this.advertiser = null;
        this.name = null;
        if (clazz == null) {
            throw new IllegalArgumentException("A Configuration class must be provided");
        }
        this.clazz = clazz;
        final List<Component> components = this.root.getComponents();
        components.add(this.properties = new Component("Properties"));
        components.add(this.scripts = new Component("Scripts"));
        components.add(this.customLevels = new Component("CustomLevels"));
        components.add(this.filters = new Component("Filters"));
        components.add(this.appenders = new Component("Appenders"));
        components.add(this.loggers = new Component("Loggers"));
    }
    
    protected ConfigurationBuilder<T> add(final Component parent, final ComponentBuilder<?> builder) {
        parent.getComponents().add(builder.build());
        return this;
    }
    
    @Override
    public ConfigurationBuilder<T> add(final AppenderComponentBuilder builder) {
        return this.add(this.appenders, builder);
    }
    
    @Override
    public ConfigurationBuilder<T> add(final CustomLevelComponentBuilder builder) {
        return this.add(this.customLevels, builder);
    }
    
    @Override
    public ConfigurationBuilder<T> add(final FilterComponentBuilder builder) {
        return this.add(this.filters, builder);
    }
    
    @Override
    public ConfigurationBuilder<T> add(final ScriptComponentBuilder builder) {
        return this.add(this.scripts, builder);
    }
    
    @Override
    public ConfigurationBuilder<T> add(final ScriptFileComponentBuilder builder) {
        return this.add(this.scripts, builder);
    }
    
    @Override
    public ConfigurationBuilder<T> add(final LoggerComponentBuilder builder) {
        return this.add(this.loggers, builder);
    }
    
    @Override
    public ConfigurationBuilder<T> add(final RootLoggerComponentBuilder builder) {
        for (final Component c : this.loggers.getComponents()) {
            if (c.getPluginType().equals("root")) {
                throw new ConfigurationException("Root Logger was previously defined");
            }
        }
        return this.add(this.loggers, builder);
    }
    
    @Override
    public ConfigurationBuilder<T> addProperty(final String key, final String value) {
        this.properties.addComponent(this.newComponent(key, "Property", value).build());
        return this;
    }
    
    @Override
    public T build() {
        T configuration;
        try {
            if (this.source == null) {
                this.source = ConfigurationSource.NULL_SOURCE;
            }
            final Constructor<T> constructor = this.clazz.getConstructor(ConfigurationSource.class, Component.class);
            configuration = constructor.newInstance(this.source, this.root);
            configuration.setMonitorInterval(this.monitorInterval);
            if (this.name != null) {
                configuration.setName(this.name);
            }
            if (this.level != null) {
                configuration.getStatusConfiguration().withStatus(this.level);
            }
            if (this.verbosity != null) {
                configuration.getStatusConfiguration().withVerbosity(this.verbosity);
            }
            if (this.packages != null) {
                configuration.setPluginPackages(this.packages);
            }
            if (this.shutdownFlag != null) {
                configuration.setShutdownHook(this.shutdownFlag);
            }
            if (this.advertiser != null) {
                configuration.createAdvertiser(this.advertiser, this.source);
            }
        }
        catch (Exception ex) {
            throw new IllegalArgumentException("Invalid Configuration class specified", ex);
        }
        configuration.getStatusConfiguration().initialize();
        configuration.initialize();
        return configuration;
    }
    
    @Override
    public ScriptComponentBuilder newScript(final String name, final String language, final String text) {
        return new DefaultScriptComponentBuilder(this, name, language, text);
    }
    
    @Override
    public ScriptFileComponentBuilder newScriptFile(final String path) {
        return new DefaultScriptFileComponentBuilder(this, path, path);
    }
    
    @Override
    public ScriptFileComponentBuilder newScriptFile(final String name, final String path) {
        return new DefaultScriptFileComponentBuilder(this, name, path);
    }
    
    @Override
    public AppenderComponentBuilder newAppender(final String name, final String type) {
        return new DefaultAppenderComponentBuilder(this, name, type);
    }
    
    @Override
    public AppenderRefComponentBuilder newAppenderRef(final String ref) {
        return new DefaultAppenderRefComponentBuilder(this, ref);
    }
    
    @Override
    public LoggerComponentBuilder newAsyncLogger(final String name, final Level level) {
        return new DefaultLoggerComponentBuilder(this, name, level.toString(), "AsyncLogger");
    }
    
    @Override
    public LoggerComponentBuilder newAsyncLogger(final String name, final String level) {
        return new DefaultLoggerComponentBuilder(this, name, level, "AsyncLogger");
    }
    
    @Override
    public RootLoggerComponentBuilder newAsyncRootLogger(final Level level) {
        return new DefaultRootLoggerComponentBuilder(this, level.toString(), "AsyncRoot");
    }
    
    @Override
    public RootLoggerComponentBuilder newAsyncRootLogger(final String level) {
        return new DefaultRootLoggerComponentBuilder(this, level, "AsyncRoot");
    }
    
    @Override
    public <B extends ComponentBuilder<B>> ComponentBuilder<B> newComponent(final String type) {
        return new DefaultComponentBuilder<B, Object>(this, type);
    }
    
    @Override
    public <B extends ComponentBuilder<B>> ComponentBuilder<B> newComponent(final String name, final String type) {
        return new DefaultComponentBuilder<B, Object>(this, name, type);
    }
    
    @Override
    public <B extends ComponentBuilder<B>> ComponentBuilder<B> newComponent(final String name, final String type, final String value) {
        return new DefaultComponentBuilder<B, Object>(this, name, type, value);
    }
    
    @Override
    public CustomLevelComponentBuilder newCustomLevel(final String name, final int level) {
        return new DefaultCustomLevelComponentBuilder(this, name, level);
    }
    
    @Override
    public FilterComponentBuilder newFilter(final String type, final Filter.Result onMatch, final Filter.Result onMisMatch) {
        return new DefaultFilterComponentBuilder(this, type, onMatch.name(), onMisMatch.name());
    }
    
    @Override
    public FilterComponentBuilder newFilter(final String type, final String onMatch, final String onMisMatch) {
        return new DefaultFilterComponentBuilder(this, type, onMatch, onMisMatch);
    }
    
    @Override
    public LayoutComponentBuilder newLayout(final String type) {
        return new DefaultLayoutComponentBuilder(this, type);
    }
    
    @Override
    public LoggerComponentBuilder newLogger(final String name, final Level level) {
        return new DefaultLoggerComponentBuilder(this, name, level.toString());
    }
    
    @Override
    public LoggerComponentBuilder newLogger(final String name, final String level) {
        return new DefaultLoggerComponentBuilder(this, name, level);
    }
    
    @Override
    public RootLoggerComponentBuilder newRootLogger(final Level level) {
        return new DefaultRootLoggerComponentBuilder(this, level.toString());
    }
    
    @Override
    public RootLoggerComponentBuilder newRootLogger(final String level) {
        return new DefaultRootLoggerComponentBuilder(this, level);
    }
    
    @Override
    public ConfigurationBuilder<T> setAdvertiser(final String advertiser) {
        this.advertiser = advertiser;
        return this;
    }
    
    @Override
    public ConfigurationBuilder<T> setConfigurationName(final String name) {
        this.name = name;
        return this;
    }
    
    @Override
    public ConfigurationBuilder<T> setConfigurationSource(final ConfigurationSource configurationSource) {
        this.source = configurationSource;
        return this;
    }
    
    @Override
    public ConfigurationBuilder<T> setMonitorInterval(final String intervalSeconds) {
        this.monitorInterval = Integer.parseInt(intervalSeconds);
        return this;
    }
    
    @Override
    public ConfigurationBuilder<T> setPackages(final String packages) {
        this.packages = packages;
        return this;
    }
    
    @Override
    public ConfigurationBuilder<T> setShutdownHook(final String flag) {
        this.shutdownFlag = flag;
        return this;
    }
    
    @Override
    public ConfigurationBuilder<T> setStatusLevel(final Level level) {
        this.level = level;
        return this;
    }
    
    @Override
    public ConfigurationBuilder<T> setVerbosity(final String verbosity) {
        this.verbosity = verbosity;
        return this;
    }
}
