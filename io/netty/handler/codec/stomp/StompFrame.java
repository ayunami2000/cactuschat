// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.stomp;

public interface StompFrame extends StompHeadersSubframe, LastStompContentSubframe
{
    StompFrame copy();
    
    StompFrame duplicate();
    
    StompFrame retain();
    
    StompFrame retain(final int p0);
    
    StompFrame touch();
    
    StompFrame touch(final Object p0);
}
