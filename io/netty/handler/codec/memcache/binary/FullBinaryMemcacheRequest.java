// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.memcache.binary;

import io.netty.handler.codec.memcache.FullMemcacheMessage;

public interface FullBinaryMemcacheRequest extends BinaryMemcacheRequest, FullMemcacheMessage
{
    FullBinaryMemcacheRequest copy();
    
    FullBinaryMemcacheRequest retain(final int p0);
    
    FullBinaryMemcacheRequest retain();
    
    FullBinaryMemcacheRequest touch();
    
    FullBinaryMemcacheRequest touch(final Object p0);
    
    FullBinaryMemcacheRequest duplicate();
}
