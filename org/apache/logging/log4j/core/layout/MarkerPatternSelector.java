// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.layout;

import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import java.util.Iterator;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import java.util.List;
import org.apache.logging.log4j.core.pattern.PatternParser;
import java.util.HashMap;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.pattern.PatternFormatter;
import java.util.Map;
import org.apache.logging.log4j.core.config.plugins.Plugin;

@Plugin(name = "MarkerPatternSelector", category = "Core", elementType = "patternSelector", printObject = true)
public class MarkerPatternSelector implements PatternSelector
{
    private final Map<String, PatternFormatter[]> formatterMap;
    private final Map<String, String> patternMap;
    private final PatternFormatter[] defaultFormatters;
    private final String defaultPattern;
    private static Logger LOGGER;
    
    public MarkerPatternSelector(final PatternMatch[] properties, final String defaultPattern, final boolean alwaysWriteExceptions, final boolean noConsoleNoAnsi, final Configuration config) {
        this.formatterMap = new HashMap<String, PatternFormatter[]>();
        this.patternMap = new HashMap<String, String>();
        final PatternParser parser = PatternLayout.createPatternParser(config);
        for (final PatternMatch property : properties) {
            try {
                final List<PatternFormatter> list = parser.parse(property.getPattern(), alwaysWriteExceptions, noConsoleNoAnsi);
                this.formatterMap.put(property.getKey(), list.toArray(new PatternFormatter[list.size()]));
                this.patternMap.put(property.getKey(), property.getPattern());
            }
            catch (RuntimeException ex) {
                throw new IllegalArgumentException("Cannot parse pattern '" + property.getPattern() + "'", ex);
            }
        }
        try {
            final List<PatternFormatter> list2 = parser.parse(defaultPattern, alwaysWriteExceptions, noConsoleNoAnsi);
            this.defaultFormatters = list2.toArray(new PatternFormatter[list2.size()]);
            this.defaultPattern = defaultPattern;
        }
        catch (RuntimeException ex2) {
            throw new IllegalArgumentException("Cannot parse pattern '" + defaultPattern + "'", ex2);
        }
    }
    
    @Override
    public PatternFormatter[] getFormatters(final LogEvent event) {
        final Marker marker = event.getMarker();
        if (marker == null) {
            return this.defaultFormatters;
        }
        for (final String key : this.formatterMap.keySet()) {
            if (marker.isInstanceOf(key)) {
                return this.formatterMap.get(key);
            }
        }
        return this.defaultFormatters;
    }
    
    @PluginFactory
    public static MarkerPatternSelector createSelector(@PluginElement("PatternMatch") final PatternMatch[] properties, @PluginAttribute("defaultPattern") String defaultPattern, @PluginAttribute(value = "alwaysWriteExceptions", defaultBoolean = true) final boolean alwaysWriteExceptions, @PluginAttribute(value = "noConsoleNoAnsi", defaultBoolean = false) final boolean noConsoleNoAnsi, @PluginConfiguration final Configuration config) {
        if (defaultPattern == null) {
            defaultPattern = "%m%n";
        }
        if (properties == null || properties.length == 0) {
            MarkerPatternSelector.LOGGER.warn("No marker patterns were provided");
        }
        return new MarkerPatternSelector(properties, defaultPattern, alwaysWriteExceptions, noConsoleNoAnsi, config);
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (final Map.Entry<String, String> entry : this.patternMap.entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            sb.append("key=\"").append(entry.getKey()).append("\", pattern=\"").append(entry.getValue()).append("\"");
            first = false;
        }
        if (!first) {
            sb.append(", ");
        }
        sb.append("default=\"").append(this.defaultPattern).append("\"");
        return sb.toString();
    }
    
    static {
        MarkerPatternSelector.LOGGER = StatusLogger.getLogger();
    }
}
