// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.filter;

import org.apache.logging.log4j.core.LogEvent;
import java.util.Iterator;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.AbstractLifeCycle;

public abstract class AbstractFilterable extends AbstractLifeCycle implements Filterable
{
    private static final long serialVersionUID = 1L;
    private volatile Filter filter;
    
    protected AbstractFilterable(final Filter filter) {
        this.filter = filter;
    }
    
    protected AbstractFilterable() {
    }
    
    @Override
    public Filter getFilter() {
        return this.filter;
    }
    
    @Override
    public synchronized void addFilter(final Filter filter) {
        if (this.filter == null) {
            this.filter = filter;
        }
        else if (filter instanceof CompositeFilter) {
            this.filter = ((CompositeFilter)this.filter).addFilter(filter);
        }
        else {
            final Filter[] filters = { this.filter, filter };
            this.filter = CompositeFilter.createFilters(filters);
        }
    }
    
    @Override
    public synchronized void removeFilter(final Filter filter) {
        if (this.filter == filter) {
            this.filter = null;
        }
        else if (filter instanceof CompositeFilter) {
            CompositeFilter composite = (CompositeFilter)filter;
            composite = composite.removeFilter(filter);
            if (composite.size() > 1) {
                this.filter = composite;
            }
            else if (composite.size() == 1) {
                final Iterator<Filter> iter = composite.iterator();
                this.filter = iter.next();
            }
            else {
                this.filter = null;
            }
        }
    }
    
    @Override
    public boolean hasFilter() {
        return this.filter != null;
    }
    
    @Override
    public void start() {
        this.setStarting();
        if (this.filter != null) {
            this.filter.start();
        }
        this.setStarted();
    }
    
    @Override
    public void stop() {
        this.setStopping();
        if (this.filter != null) {
            this.filter.stop();
        }
        this.setStopped();
    }
    
    @Override
    public boolean isFiltered(final LogEvent event) {
        return this.filter != null && this.filter.filter(event) == Filter.Result.DENY;
    }
}
