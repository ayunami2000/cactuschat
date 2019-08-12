// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.stomp;

import io.netty.buffer.ByteBufHolder;

public interface StompContentSubframe extends ByteBufHolder, StompSubframe
{
    StompContentSubframe copy();
    
    StompContentSubframe duplicate();
    
    StompContentSubframe retain();
    
    StompContentSubframe retain(final int p0);
    
    StompContentSubframe touch();
    
    StompContentSubframe touch(final Object p0);
}
