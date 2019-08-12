// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.util.EventListener;

public interface ResolverListener extends EventListener
{
    void receiveMessage(final Object p0, final Message p1);
    
    void handleException(final Object p0, final Exception p1);
}
