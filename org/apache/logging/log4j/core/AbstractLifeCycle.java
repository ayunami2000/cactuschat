// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core;

import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.Logger;
import java.io.Serializable;

public class AbstractLifeCycle implements LifeCycle, Serializable
{
    protected static final Logger LOGGER;
    private static final long serialVersionUID = 1L;
    private volatile State state;
    
    public AbstractLifeCycle() {
        this.state = State.INITIALIZED;
    }
    
    protected boolean equalsImpl(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final LifeCycle other = (LifeCycle)obj;
        return this.state == other.getState();
    }
    
    @Override
    public State getState() {
        return this.state;
    }
    
    protected int hashCodeImpl() {
        final int prime = 31;
        int result = 1;
        result = 31 * result + ((this.state == null) ? 0 : this.state.hashCode());
        return result;
    }
    
    public boolean isInitialized() {
        return this.state == State.INITIALIZED;
    }
    
    @Override
    public boolean isStarted() {
        return this.state == State.STARTED;
    }
    
    public boolean isStarting() {
        return this.state == State.STARTING;
    }
    
    @Override
    public boolean isStopped() {
        return this.state == State.STOPPED;
    }
    
    public boolean isStopping() {
        return this.state == State.STOPPING;
    }
    
    protected void setStarted() {
        this.setState(State.STARTED);
    }
    
    protected void setStarting() {
        this.setState(State.STARTING);
    }
    
    protected void setState(final State newState) {
        this.state = newState;
    }
    
    protected void setStopped() {
        this.setState(State.STOPPED);
    }
    
    protected void setStopping() {
        this.setState(State.STOPPING);
    }
    
    @Override
    public void initialize() {
        this.state = State.INITIALIZED;
    }
    
    @Override
    public void start() {
        this.setStarted();
    }
    
    @Override
    public void stop() {
        this.state = State.STOPPED;
    }
    
    static {
        LOGGER = StatusLogger.getLogger();
    }
}
