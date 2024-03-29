// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.filter;

import java.util.Iterator;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.ThreadContext;
import java.util.Objects;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import java.util.HashMap;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.util.KeyValuePair;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import java.util.Map;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.plugins.Plugin;

@Plugin(name = "DynamicThresholdFilter", category = "Core", elementType = "filter", printObject = true)
public final class DynamicThresholdFilter extends AbstractFilter
{
    private static final long serialVersionUID = 1L;
    private Level defaultThreshold;
    private final String key;
    private Map<String, Level> levelMap;
    
    @PluginFactory
    public static DynamicThresholdFilter createFilter(@PluginAttribute("key") final String key, @PluginElement("Pairs") final KeyValuePair[] pairs, @PluginAttribute("defaultThreshold") final Level defaultThreshold, @PluginAttribute("onMatch") final Filter.Result onMatch, @PluginAttribute("onMismatch") final Filter.Result onMismatch) {
        final Map<String, Level> map = new HashMap<String, Level>();
        for (final KeyValuePair pair : pairs) {
            map.put(pair.getKey(), Level.toLevel(pair.getValue()));
        }
        final Level level = (defaultThreshold == null) ? Level.ERROR : defaultThreshold;
        return new DynamicThresholdFilter(key, map, level, onMatch, onMismatch);
    }
    
    private DynamicThresholdFilter(final String key, final Map<String, Level> pairs, final Level defaultLevel, final Filter.Result onMatch, final Filter.Result onMismatch) {
        super(onMatch, onMismatch);
        this.defaultThreshold = Level.ERROR;
        this.levelMap = new HashMap<String, Level>();
        Objects.requireNonNull(key, "key cannot be null");
        this.key = key;
        this.levelMap = pairs;
        this.defaultThreshold = defaultLevel;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equalsImpl(obj)) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final DynamicThresholdFilter other = (DynamicThresholdFilter)obj;
        if (this.defaultThreshold == null) {
            if (other.defaultThreshold != null) {
                return false;
            }
        }
        else if (!this.defaultThreshold.equals(other.defaultThreshold)) {
            return false;
        }
        if (this.key == null) {
            if (other.key != null) {
                return false;
            }
        }
        else if (!this.key.equals(other.key)) {
            return false;
        }
        if (this.levelMap == null) {
            if (other.levelMap != null) {
                return false;
            }
        }
        else if (!this.levelMap.equals(other.levelMap)) {
            return false;
        }
        return true;
    }
    
    private Filter.Result filter(final Level level) {
        final Object value = ThreadContext.get(this.key);
        if (value != null) {
            Level ctxLevel = this.levelMap.get(value);
            if (ctxLevel == null) {
                ctxLevel = this.defaultThreshold;
            }
            return level.isMoreSpecificThan(ctxLevel) ? this.onMatch : this.onMismatch;
        }
        return Filter.Result.NEUTRAL;
    }
    
    @Override
    public Filter.Result filter(final LogEvent event) {
        return this.filter(event.getLevel());
    }
    
    @Override
    public Filter.Result filter(final Logger logger, final Level level, final Marker marker, final Message msg, final Throwable t) {
        return this.filter(level);
    }
    
    @Override
    public Filter.Result filter(final Logger logger, final Level level, final Marker marker, final Object msg, final Throwable t) {
        return this.filter(level);
    }
    
    @Override
    public Filter.Result filter(final Logger logger, final Level level, final Marker marker, final String msg, final Object... params) {
        return this.filter(level);
    }
    
    public String getKey() {
        return this.key;
    }
    
    public Map<String, Level> getLevelMap() {
        return this.levelMap;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCodeImpl();
        result = 31 * result + ((this.defaultThreshold == null) ? 0 : this.defaultThreshold.hashCode());
        result = 31 * result + ((this.key == null) ? 0 : this.key.hashCode());
        result = 31 * result + ((this.levelMap == null) ? 0 : this.levelMap.hashCode());
        return result;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("key=").append(this.key);
        sb.append(", default=").append(this.defaultThreshold);
        if (this.levelMap.size() > 0) {
            sb.append('{');
            boolean first = true;
            for (final Map.Entry<String, Level> entry : this.levelMap.entrySet()) {
                if (!first) {
                    sb.append(", ");
                    first = false;
                }
                sb.append(entry.getKey()).append('=').append(entry.getValue());
            }
            sb.append('}');
        }
        return sb.toString();
    }
}
