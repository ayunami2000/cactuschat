// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.spi;

import org.apache.logging.log4j.util.Supplier;
import org.apache.logging.log4j.util.MessageSupplier;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

public interface ExtendedLogger extends Logger
{
    boolean isEnabled(final Level p0, final Marker p1, final Message p2, final Throwable p3);
    
    boolean isEnabled(final Level p0, final Marker p1, final Object p2, final Throwable p3);
    
    boolean isEnabled(final Level p0, final Marker p1, final String p2, final Throwable p3);
    
    boolean isEnabled(final Level p0, final Marker p1, final String p2);
    
    boolean isEnabled(final Level p0, final Marker p1, final String p2, final Object... p3);
    
    void logIfEnabled(final String p0, final Level p1, final Marker p2, final Message p3, final Throwable p4);
    
    void logIfEnabled(final String p0, final Level p1, final Marker p2, final Object p3, final Throwable p4);
    
    void logIfEnabled(final String p0, final Level p1, final Marker p2, final String p3, final Throwable p4);
    
    void logIfEnabled(final String p0, final Level p1, final Marker p2, final String p3);
    
    void logIfEnabled(final String p0, final Level p1, final Marker p2, final String p3, final Object... p4);
    
    void logMessage(final String p0, final Level p1, final Marker p2, final Message p3, final Throwable p4);
    
    void logIfEnabled(final String p0, final Level p1, final Marker p2, final MessageSupplier p3, final Throwable p4);
    
    void logIfEnabled(final String p0, final Level p1, final Marker p2, final String p3, final Supplier<?>... p4);
    
    void logIfEnabled(final String p0, final Level p1, final Marker p2, final Supplier<?> p3, final Throwable p4);
}
