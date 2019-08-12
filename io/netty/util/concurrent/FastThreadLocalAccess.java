// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.util.concurrent;

import io.netty.util.internal.InternalThreadLocalMap;

public interface FastThreadLocalAccess
{
    InternalThreadLocalMap threadLocalMap();
    
    void setThreadLocalMap(final InternalThreadLocalMap p0);
}
