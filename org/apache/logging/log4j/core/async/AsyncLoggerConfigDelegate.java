// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.async;

import org.apache.logging.log4j.core.jmx.RingBufferAdmin;
import org.apache.logging.log4j.core.LogEvent;

public interface AsyncLoggerConfigDelegate
{
    boolean tryCallAppendersInBackground(final LogEvent p0, final AsyncLoggerConfig p1);
    
    RingBufferAdmin createRingBufferAdmin(final String p0, final String p1);
}
