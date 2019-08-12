// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.spi;

import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.message.MessageFactory;

public class ExtendedLoggerWrapper extends AbstractLogger
{
    private static final long serialVersionUID = 1L;
    protected final ExtendedLogger logger;
    
    public ExtendedLoggerWrapper(final ExtendedLogger logger, final String name, final MessageFactory messageFactory) {
        super(name, messageFactory);
        this.logger = logger;
    }
    
    @Override
    public Level getLevel() {
        return this.logger.getLevel();
    }
    
    @Override
    public boolean isEnabled(final Level level, final Marker marker, final Message message, final Throwable t) {
        return this.logger.isEnabled(level, marker, message, t);
    }
    
    @Override
    public boolean isEnabled(final Level level, final Marker marker, final Object message, final Throwable t) {
        return this.logger.isEnabled(level, marker, message, t);
    }
    
    @Override
    public boolean isEnabled(final Level level, final Marker marker, final String message) {
        return this.logger.isEnabled(level, marker, message);
    }
    
    @Override
    public boolean isEnabled(final Level level, final Marker marker, final String message, final Object... params) {
        return this.logger.isEnabled(level, marker, message, params);
    }
    
    @Override
    public boolean isEnabled(final Level level, final Marker marker, final String message, final Throwable t) {
        return this.logger.isEnabled(level, marker, message, t);
    }
    
    @Override
    public void logMessage(final String fqcn, final Level level, final Marker marker, final Message message, final Throwable t) {
        this.logger.logMessage(fqcn, level, marker, message, t);
    }
}
