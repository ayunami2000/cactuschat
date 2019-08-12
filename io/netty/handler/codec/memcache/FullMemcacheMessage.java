// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.memcache;

public interface FullMemcacheMessage extends MemcacheMessage, LastMemcacheContent
{
    FullMemcacheMessage copy();
    
    FullMemcacheMessage retain(final int p0);
    
    FullMemcacheMessage retain();
    
    FullMemcacheMessage touch();
    
    FullMemcacheMessage touch(final Object p0);
    
    FullMemcacheMessage duplicate();
}
