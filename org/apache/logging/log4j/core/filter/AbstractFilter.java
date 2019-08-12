// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.filter;

import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.AbstractLifeCycle;

public abstract class AbstractFilter extends AbstractLifeCycle implements Filter
{
    private static final long serialVersionUID = 1L;
    protected final Result onMatch;
    protected final Result onMismatch;
    
    protected AbstractFilter() {
        this(null, null);
    }
    
    protected AbstractFilter(final Result onMatch, final Result onMismatch) {
        this.onMatch = ((onMatch == null) ? Result.NEUTRAL : onMatch);
        this.onMismatch = ((onMismatch == null) ? Result.DENY : onMismatch);
    }
    
    @Override
    protected boolean equalsImpl(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equalsImpl(obj)) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final AbstractFilter other = (AbstractFilter)obj;
        return this.onMatch == other.onMatch && this.onMismatch == other.onMismatch;
    }
    
    @Override
    public Result filter(final LogEvent event) {
        return Result.NEUTRAL;
    }
    
    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final Message msg, final Throwable t) {
        return Result.NEUTRAL;
    }
    
    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final Object msg, final Throwable t) {
        return Result.NEUTRAL;
    }
    
    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final String msg, final Object... params) {
        return Result.NEUTRAL;
    }
    
    @Override
    public final Result getOnMatch() {
        return this.onMatch;
    }
    
    @Override
    public final Result getOnMismatch() {
        return this.onMismatch;
    }
    
    @Override
    protected int hashCodeImpl() {
        final int prime = 31;
        int result = super.hashCodeImpl();
        result = 31 * result + ((this.onMatch == null) ? 0 : this.onMatch.hashCode());
        result = 31 * result + ((this.onMismatch == null) ? 0 : this.onMismatch.hashCode());
        return result;
    }
    
    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
