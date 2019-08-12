// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.memcache.binary;

import io.netty.handler.codec.memcache.FullMemcacheMessage;

public interface FullBinaryMemcacheResponse extends BinaryMemcacheResponse, FullMemcacheMessage
{
    FullBinaryMemcacheResponse copy();
    
    FullBinaryMemcacheResponse retain(final int p0);
    
    FullBinaryMemcacheResponse retain();
    
    FullBinaryMemcacheResponse touch();
    
    FullBinaryMemcacheResponse touch(final Object p0);
    
    FullBinaryMemcacheResponse duplicate();
}
