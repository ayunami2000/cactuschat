// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.config.properties;

import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.RootLoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.LoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.AppenderRefComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.FilterComponentBuilder;
import org.apache.logging.log4j.util.Strings;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ScriptFileComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ScriptComponentBuilder;
import java.util.Iterator;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import java.io.InputStream;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import java.io.IOException;
import org.apache.logging.log4j.core.config.ConfigurationException;
import java.util.Properties;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Order;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.ConfigurationFactory;

@Plugin(name = "PropertiesConfigurationFactory", category = "ConfigurationFactory")
@Order(8)
public class PropertiesConfigurationFactory extends ConfigurationFactory
{
    private static final String ADVERTISER_KEY = "advertiser";
    private static final String STATUS_KEY = "status";
    private static final String SHUTDOWN_HOOK = "shutdownHook";
    private static final String VERBOSE = "verbose";
    private static final String PACKAGES = "packages";
    private static final String CONFIG_NAME = "name";
    private static final String MONITOR_INTERVAL = "monitorInterval";
    private static final String CONFIG_TYPE = "type";
    
    @Override
    protected String[] getSupportedTypes() {
        return new String[] { ".properties" };
    }
    
    @Override
    public PropertiesConfiguration getConfiguration(final ConfigurationSource source) {
        final InputStream configStream = source.getInputStream();
        final Properties properties = new Properties();
        try {
            properties.load(configStream);
        }
        catch (IOException ioe) {
            throw new ConfigurationException("Unable to load " + source.toString(), ioe);
        }
        final ConfigurationBuilder<PropertiesConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder(PropertiesConfiguration.class);
        String value = properties.getProperty("status");
        if (value != null) {
            builder.setStatusLevel(Level.toLevel(value, Level.ERROR));
        }
        else {
            builder.setStatusLevel(Level.ERROR);
        }
        value = properties.getProperty("shutdownHook");
        if (value != null) {
            builder.setShutdownHook(value);
        }
        value = properties.getProperty("verbose");
        if (value != null) {
            builder.setVerbosity(value);
        }
        value = properties.getProperty("packages");
        if (value != null) {
            builder.setPackages(value);
        }
        value = properties.getProperty("name");
        if (value != null) {
            builder.setConfigurationName(value);
        }
        value = properties.getProperty("monitorInterval");
        if (value != null) {
            builder.setMonitorInterval(value);
        }
        value = properties.getProperty("advertiser");
        if (value != null) {
            builder.setAdvertiser(value);
        }
        Properties props = PropertiesUtil.extractSubset(properties, "property");
        for (final String key : props.stringPropertyNames()) {
            builder.addProperty(key, props.getProperty(key));
        }
        final String scriptProp = properties.getProperty("scripts");
        if (scriptProp != null) {
            final String[] arr$;
            final String[] scriptNames = arr$ = scriptProp.split(",");
            for (final String scriptName : arr$) {
                final String name = scriptName.trim();
                final Properties scriptProps = PropertiesUtil.extractSubset(properties, "script." + name);
                final String type = scriptProps.getProperty("type");
                if (type == null) {
                    throw new ConfigurationException("No type provided for script - must be Script or ScriptFile");
                }
                scriptProps.remove("type");
                if (type.equalsIgnoreCase("script")) {
                    builder.add(this.createScript(builder, name, scriptProps));
                }
                else {
                    builder.add(this.createScriptFile(builder, name, scriptProps));
                }
            }
        }
        final Properties levelProps = PropertiesUtil.extractSubset(properties, "customLevel");
        if (levelProps.size() > 0) {
            for (final String key2 : levelProps.stringPropertyNames()) {
                builder.add(builder.newCustomLevel(key2, Integer.parseInt(props.getProperty(key2))));
            }
        }
        final String filterProp = properties.getProperty("filters");
        if (filterProp != null) {
            final String[] arr$2;
            final String[] filterNames = arr$2 = filterProp.split(",");
            for (final String filterName : arr$2) {
                final String name2 = filterName.trim();
                builder.add(this.createFilter(builder, name2, PropertiesUtil.extractSubset(properties, "filter." + name2)));
            }
        }
        final String appenderProp = properties.getProperty("appenders");
        if (appenderProp != null) {
            final String[] arr$3;
            final String[] appenderNames = arr$3 = appenderProp.split(",");
            for (final String appenderName : arr$3) {
                final String name3 = appenderName.trim();
                builder.add(this.createAppender(builder, name3, PropertiesUtil.extractSubset(properties, "appender." + name3)));
            }
        }
        final String loggerProp = properties.getProperty("loggers");
        if (loggerProp != null) {
            final String[] arr$4;
            final String[] loggerNames = arr$4 = loggerProp.split(",");
            for (final String loggerName : arr$4) {
                final String name4 = loggerName.trim();
                if (!name4.equals("root")) {
                    builder.add(this.createLogger(builder, name4, PropertiesUtil.extractSubset(properties, "logger." + name4)));
                }
            }
        }
        props = PropertiesUtil.extractSubset(properties, "rootLogger");
        if (props.size() > 0) {
            builder.add(this.createRootLogger(builder, props));
        }
        return builder.build();
    }
    
    private ScriptComponentBuilder createScript(final ConfigurationBuilder<PropertiesConfiguration> builder, final String key, final Properties properties) {
        final String name = properties.getProperty("name");
        if (name != null) {
            properties.remove("name");
        }
        final String language = properties.getProperty("language");
        if (language != null) {
            properties.remove("language");
        }
        final String text = properties.getProperty("text");
        if (text != null) {
            properties.remove("text");
        }
        final ScriptComponentBuilder scriptBuilder = builder.newScript(name, language, text);
        this.processRemainingProperties(scriptBuilder, key, properties);
        return scriptBuilder;
    }
    
    private ScriptFileComponentBuilder createScriptFile(final ConfigurationBuilder<PropertiesConfiguration> builder, final String key, final Properties properties) {
        final String name = properties.getProperty("name");
        if (name != null) {
            properties.remove("name");
        }
        final String path = properties.getProperty("path");
        if (path != null) {
            properties.remove("path");
        }
        final ScriptFileComponentBuilder scriptFileBuilder = builder.newScriptFile(name, path);
        this.processRemainingProperties(scriptFileBuilder, key, properties);
        return scriptFileBuilder;
    }
    
    private AppenderComponentBuilder createAppender(final ConfigurationBuilder<PropertiesConfiguration> builder, final String key, final Properties properties) {
        final String name = properties.getProperty("name");
        if (Strings.isEmpty(name)) {
            throw new ConfigurationException("No name attribute provided for Appender " + key);
        }
        properties.remove("name");
        final String type = properties.getProperty("type");
        if (Strings.isEmpty(type)) {
            throw new ConfigurationException("No type attribute provided for Appender " + key);
        }
        properties.remove("type");
        final AppenderComponentBuilder appenderBuilder = builder.newAppender(name, type);
        final String filters = properties.getProperty("filters");
        if (filters != null) {
            properties.remove("filters");
            final String[] arr$;
            final String[] filterNames = arr$ = filters.split(",");
            for (String filterName : arr$) {
                filterName = filterName.trim();
                final Properties filterProps = PropertiesUtil.extractSubset(properties, "filter." + filterName);
                appenderBuilder.add(this.createFilter(builder, filterName, filterProps));
            }
        }
        final Properties layoutProps = PropertiesUtil.extractSubset(properties, "layout");
        if (layoutProps.size() > 0) {
            appenderBuilder.add(this.createLayout(builder, name, layoutProps));
        }
        this.processRemainingProperties(appenderBuilder, name, properties);
        return appenderBuilder;
    }
    
    private FilterComponentBuilder createFilter(final ConfigurationBuilder<PropertiesConfiguration> builder, final String key, final Properties properties) {
        final String type = properties.getProperty("type");
        if (Strings.isEmpty(type)) {
            throw new ConfigurationException("No type attribute provided for Appender " + key);
        }
        properties.remove("type");
        final String onMatch = properties.getProperty("onMatch");
        if (onMatch != null) {
            properties.remove("onMatch");
        }
        final String onMisMatch = properties.getProperty("onMisMatch");
        if (onMisMatch != null) {
            properties.remove("onMisMatch");
        }
        final FilterComponentBuilder filterBuilder = builder.newFilter(type, onMatch, onMisMatch);
        this.processRemainingProperties(filterBuilder, key, properties);
        return filterBuilder;
    }
    
    private AppenderRefComponentBuilder createAppenderRef(final ConfigurationBuilder<PropertiesConfiguration> builder, final String key, final Properties properties) {
        final String ref = properties.getProperty("ref");
        if (Strings.isEmpty(ref)) {
            throw new ConfigurationException("No ref attribute provided for AppenderRef " + key);
        }
        properties.remove("ref");
        final AppenderRefComponentBuilder appenderRefBuilder = builder.newAppenderRef(ref);
        final String level = properties.getProperty("level");
        if (!Strings.isEmpty(level)) {
            appenderRefBuilder.addAttribute("level", level);
        }
        final String filters = properties.getProperty("filters");
        if (filters != null) {
            properties.remove("filters");
            final String[] arr$;
            final String[] filterNames = arr$ = filters.split(",");
            for (String filterName : arr$) {
                filterName = filterName.trim();
                final Properties filterProps = PropertiesUtil.extractSubset(properties, "filter." + filterName);
                appenderRefBuilder.add(this.createFilter(builder, filterName, filterProps));
            }
        }
        return appenderRefBuilder;
    }
    
    private LoggerComponentBuilder createLogger(final ConfigurationBuilder<PropertiesConfiguration> builder, final String key, final Properties properties) {
        final String name = properties.getProperty("name");
        if (Strings.isEmpty(name)) {
            throw new ConfigurationException("No name attribute provided for Logger " + key);
        }
        properties.remove("name");
        final String level = properties.getProperty("level");
        if (level != null) {
            properties.remove("level");
        }
        final String type = properties.getProperty("type");
        LoggerComponentBuilder loggerBuilder;
        if (type != null) {
            if (!type.equalsIgnoreCase("asyncLogger")) {
                throw new ConfigurationException("Unknown Logger type " + type + " for Logger " + name);
            }
            loggerBuilder = builder.newAsyncLogger(name, level);
        }
        else {
            loggerBuilder = builder.newLogger(name, level);
        }
        final String appenderRefs = properties.getProperty("appenderRefs");
        if (appenderRefs != null) {
            properties.remove("appenderRefs");
            final String[] arr$;
            final String[] refNames = arr$ = appenderRefs.split(",");
            for (String appenderRef : arr$) {
                appenderRef = appenderRef.trim();
                final Properties refProps = PropertiesUtil.extractSubset(properties, "appenderRef." + appenderRef);
                loggerBuilder.add(this.createAppenderRef(builder, appenderRef, refProps));
            }
        }
        final String filters = properties.getProperty("filters");
        if (filters != null) {
            properties.remove("filters");
            final String[] arr$2;
            final String[] filterNames = arr$2 = filters.split(",");
            for (String filterName : arr$2) {
                filterName = filterName.trim();
                final Properties filterProps = PropertiesUtil.extractSubset(properties, "filter." + filterName);
                loggerBuilder.add(this.createFilter(builder, filterName, filterProps));
            }
        }
        final String additivity = properties.getProperty("additivity");
        if (!Strings.isEmpty(additivity)) {
            loggerBuilder.addAttribute("additivity", additivity);
        }
        return loggerBuilder;
    }
    
    private RootLoggerComponentBuilder createRootLogger(final ConfigurationBuilder<PropertiesConfiguration> builder, final Properties properties) {
        final String level = properties.getProperty("level");
        if (level != null) {
            properties.remove("level");
        }
        final String type = properties.getProperty("type");
        RootLoggerComponentBuilder loggerBuilder;
        if (type != null) {
            if (!type.equalsIgnoreCase("asyncRoot")) {
                throw new ConfigurationException("Unknown Logger type for root logger" + type);
            }
            loggerBuilder = builder.newAsyncRootLogger(level);
        }
        else {
            loggerBuilder = builder.newRootLogger(level);
        }
        final String appenderRefs = properties.getProperty("appenderRefs");
        if (appenderRefs != null) {
            properties.remove("appenderRefs");
            final String[] arr$;
            final String[] refNames = arr$ = appenderRefs.split(",");
            for (String appenderRef : arr$) {
                appenderRef = appenderRef.trim();
                final Properties refProps = PropertiesUtil.extractSubset(properties, "appenderRef." + appenderRef);
                loggerBuilder.add(this.createAppenderRef(builder, appenderRef, refProps));
            }
        }
        final String filters = properties.getProperty("filters");
        if (filters != null) {
            properties.remove("filters");
            final String[] arr$2;
            final String[] filterNames = arr$2 = filters.split(",");
            for (String filterName : arr$2) {
                filterName = filterName.trim();
                final Properties filterProps = PropertiesUtil.extractSubset(properties, "filter." + filterName);
                loggerBuilder.add(this.createFilter(builder, filterName, filterProps));
            }
        }
        return loggerBuilder;
    }
    
    private LayoutComponentBuilder createLayout(final ConfigurationBuilder<PropertiesConfiguration> builder, final String appenderName, final Properties properties) {
        final String type = properties.getProperty("type");
        if (Strings.isEmpty(type)) {
            throw new ConfigurationException("No type attribute provided for Layout on Appender " + appenderName);
        }
        properties.remove("type");
        final LayoutComponentBuilder layoutBuilder = builder.newLayout(type);
        this.processRemainingProperties(layoutBuilder, appenderName, properties);
        return layoutBuilder;
    }
    
    private <B extends ComponentBuilder<B>> ComponentBuilder<B> createComponent(final ComponentBuilder<?> parent, final String key, final Properties properties) {
        final String name = properties.getProperty("name");
        if (name != null) {
            properties.remove("name");
        }
        final String type = properties.getProperty("type");
        if (Strings.isEmpty(type)) {
            throw new ConfigurationException("No type attribute provided for component " + key);
        }
        properties.remove("type");
        final ComponentBuilder<B> componentBuilder = parent.getBuilder().newComponent(name, type);
        this.processRemainingProperties(componentBuilder, name, properties);
        return componentBuilder;
    }
    
    private void processRemainingProperties(final ComponentBuilder<?> builder, final String name, final Properties properties) {
        while (properties.size() > 0) {
            final String propertyName = properties.stringPropertyNames().iterator().next();
            final int index = propertyName.indexOf(46);
            if (index > 0) {
                final String prefix = propertyName.substring(0, index);
                final Properties componentProperties = PropertiesUtil.extractSubset(properties, prefix);
                builder.addComponent(this.createComponent(builder, prefix, componentProperties));
            }
            else {
                builder.addAttribute(propertyName, properties.getProperty(propertyName));
                properties.remove(propertyName);
            }
        }
    }
}
