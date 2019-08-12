// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.util;

public interface ResourceLeak
{
    void record();
    
    void record(final Object p0);
    
    boolean close();
}
