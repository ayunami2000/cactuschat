// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.lookup;

import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.util.Loader;
import java.util.Iterator;
import org.apache.logging.log4j.core.util.ReflectionUtil;
import org.apache.logging.log4j.core.config.plugins.util.PluginType;
import org.apache.logging.log4j.core.config.plugins.util.PluginManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.Logger;

public class Interpolator extends AbstractLookup
{
    private static final Logger LOGGER;
    private static final char PREFIX_SEPARATOR = ':';
    private final Map<String, StrLookup> lookups;
    private final StrLookup defaultLookup;
    
    public Interpolator(final StrLookup defaultLookup) {
        this(defaultLookup, null);
    }
    
    public Interpolator(final StrLookup defaultLookup, final List<String> pluginPackages) {
        this.lookups = new HashMap<String, StrLookup>();
        this.defaultLookup = ((defaultLookup == null) ? new MapLookup(new HashMap<String, String>()) : defaultLookup);
        final PluginManager manager = new PluginManager("Lookup");
        manager.collectPlugins(pluginPackages);
        final Map<String, PluginType<?>> plugins = manager.getPlugins();
        for (final Map.Entry<String, PluginType<?>> entry : plugins.entrySet()) {
            try {
                final Class<? extends StrLookup> clazz = entry.getValue().getPluginClass().asSubclass(StrLookup.class);
                this.lookups.put(entry.getKey(), ReflectionUtil.instantiate(clazz));
            }
            catch (Exception ex) {
                Interpolator.LOGGER.error("Unable to create Lookup for {}", new Object[] { entry.getKey(), ex });
            }
        }
    }
    
    public Interpolator() {
        this((Map<String, String>)null);
    }
    
    public Interpolator(final Map<String, String> properties) {
        this.lookups = new HashMap<String, StrLookup>();
        this.defaultLookup = new MapLookup((properties == null) ? new HashMap<String, String>() : properties);
        this.lookups.put("log4j", new Log4jLookup());
        this.lookups.put("sys", new SystemPropertiesLookup());
        this.lookups.put("env", new EnvironmentLookup());
        this.lookups.put("main", MainMapLookup.MAIN_SINGLETON);
        this.lookups.put("marker", new MarkerLookup());
        this.lookups.put("java", new JavaLookup());
        try {
            this.lookups.put("jndi", Loader.newCheckedInstanceOf("org.apache.logging.log4j.core.lookup.JndiLookup", StrLookup.class));
        }
        catch (Throwable e) {
            Interpolator.LOGGER.warn("JNDI lookup class is not available because this JRE does not support JNDI. JNDI string lookups will not be available, continuing configuration.", e);
        }
        try {
            this.lookups.put("jvmrunargs", Loader.newCheckedInstanceOf("org.apache.logging.log4j.core.lookup.JmxRuntimeInputArgumentsLookup", StrLookup.class));
        }
        catch (Throwable e) {
            Interpolator.LOGGER.warn("JMX runtime input lookup class is not available because this JRE does not support JMX. JMX lookups will not be available, continuing configuration.", e);
        }
        this.lookups.put("date", new DateLookup());
        this.lookups.put("ctx", new ContextMapLookup());
        if (Loader.isClassAvailable("javax.servlet.ServletContext")) {
            try {
                this.lookups.put("web", Loader.newCheckedInstanceOf("org.apache.logging.log4j.web.WebLookup", StrLookup.class));
            }
            catch (Exception ignored) {
                Interpolator.LOGGER.info("Log4j appears to be running in a Servlet environment, but there's no log4j-web module available. If you want better web container support, please add the log4j-web JAR to your web archive or server lib directory.");
            }
        }
        else {
            Interpolator.LOGGER.debug("Not in a ServletContext environment, thus not loading WebLookup plugin.");
        }
    }
    
    @Override
    public String lookup(final LogEvent event, String var) {
        if (var == null) {
            return null;
        }
        final int prefixPos = var.indexOf(58);
        if (prefixPos >= 0) {
            final String prefix = var.substring(0, prefixPos);
            final String name = var.substring(prefixPos + 1);
            final StrLookup lookup = this.lookups.get(prefix);
            String value = null;
            if (lookup != null) {
                value = ((event == null) ? lookup.lookup(name) : lookup.lookup(event, name));
            }
            if (value != null) {
                return value;
            }
            var = var.substring(prefixPos + 1);
        }
        if (this.defaultLookup != null) {
            return (event == null) ? this.defaultLookup.lookup(var) : this.defaultLookup.lookup(event, var);
        }
        return null;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (final String name : this.lookups.keySet()) {
            if (sb.length() == 0) {
                sb.append('{');
            }
            else {
                sb.append(", ");
            }
            sb.append(name);
        }
        if (sb.length() > 0) {
            sb.append('}');
        }
        return sb.toString();
    }
    
    static {
        LOGGER = StatusLogger.getLogger();
    }
}
