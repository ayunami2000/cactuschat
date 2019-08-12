// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.filter;

import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import java.util.Arrays;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;
import java.util.Iterator;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.AbstractLifeCycle;

@Plugin(name = "filters", category = "Core", printObject = true)
public final class CompositeFilter extends AbstractLifeCycle implements Iterable<Filter>, Filter
{
    private static final long serialVersionUID = 1L;
    private final List<Filter> filters;
    
    private CompositeFilter() {
        this.filters = new ArrayList<Filter>();
    }
    
    private CompositeFilter(final List<Filter> filters) {
        if (filters == null) {
            this.filters = Collections.unmodifiableList((List<? extends Filter>)new ArrayList<Filter>());
            return;
        }
        this.filters = Collections.unmodifiableList((List<? extends Filter>)filters);
    }
    
    public CompositeFilter addFilter(final Filter filter) {
        if (filter == null) {
            return this;
        }
        final List<Filter> filterList = new ArrayList<Filter>(this.filters);
        filterList.add(filter);
        return new CompositeFilter(Collections.unmodifiableList((List<? extends Filter>)filterList));
    }
    
    public CompositeFilter removeFilter(final Filter filter) {
        if (filter == null) {
            return this;
        }
        final List<Filter> filterList = new ArrayList<Filter>(this.filters);
        filterList.remove(filter);
        return new CompositeFilter(Collections.unmodifiableList((List<? extends Filter>)filterList));
    }
    
    @Override
    public Iterator<Filter> iterator() {
        return this.filters.iterator();
    }
    
    public List<Filter> getFilters() {
        return this.filters;
    }
    
    public boolean isEmpty() {
        return this.filters.isEmpty();
    }
    
    public int size() {
        return this.filters.size();
    }
    
    @Override
    public void start() {
        this.setStarting();
        for (final Filter filter : this.filters) {
            filter.start();
        }
        this.setStarted();
    }
    
    @Override
    public void stop() {
        this.setStopping();
        for (final Filter filter : this.filters) {
            filter.stop();
        }
        this.setStopped();
    }
    
    @Override
    public Result getOnMismatch() {
        return Result.NEUTRAL;
    }
    
    @Override
    public Result getOnMatch() {
        return Result.NEUTRAL;
    }
    
    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final String msg, final Object... params) {
        Result result = Result.NEUTRAL;
        for (final Filter filter : this.filters) {
            result = filter.filter(logger, level, marker, msg, params);
            if (result == Result.ACCEPT || result == Result.DENY) {
                return result;
            }
        }
        return result;
    }
    
    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final Object msg, final Throwable t) {
        Result result = Result.NEUTRAL;
        for (final Filter filter : this.filters) {
            result = filter.filter(logger, level, marker, msg, t);
            if (result == Result.ACCEPT || result == Result.DENY) {
                return result;
            }
        }
        return result;
    }
    
    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final Message msg, final Throwable t) {
        Result result = Result.NEUTRAL;
        for (final Filter filter : this.filters) {
            result = filter.filter(logger, level, marker, msg, t);
            if (result == Result.ACCEPT || result == Result.DENY) {
                return result;
            }
        }
        return result;
    }
    
    @Override
    public Result filter(final LogEvent event) {
        Result result = Result.NEUTRAL;
        for (final Filter filter : this.filters) {
            result = filter.filter(event);
            if (result == Result.ACCEPT || result == Result.DENY) {
                return result;
            }
        }
        return result;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (final Filter filter : this.filters) {
            if (sb.length() == 0) {
                sb.append('{');
            }
            else {
                sb.append(", ");
            }
            sb.append(filter.toString());
        }
        if (sb.length() > 0) {
            sb.append('}');
        }
        return sb.toString();
    }
    
    @PluginFactory
    public static CompositeFilter createFilters(@PluginElement("Filters") final Filter[] filters) {
        final List<Filter> filterList = (filters == null || filters.length == 0) ? new ArrayList<Filter>() : Arrays.asList(filters);
        return new CompositeFilter(filterList);
    }
}
