// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.memcache;

import io.netty.buffer.ByteBufHolder;

public interface MemcacheContent extends MemcacheObject, ByteBufHolder
{
    MemcacheContent copy();
    
    MemcacheContent duplicate();
    
    MemcacheContent retain();
    
    MemcacheContent retain(final int p0);
    
    MemcacheContent touch();
    
    MemcacheContent touch(final Object p0);
}
