// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http;

import io.netty.buffer.ByteBuf;

public interface FullHttpMessage extends HttpMessage, LastHttpContent
{
    FullHttpMessage copy(final ByteBuf p0);
    
    FullHttpMessage copy();
    
    FullHttpMessage retain(final int p0);
    
    FullHttpMessage retain();
    
    FullHttpMessage touch();
    
    FullHttpMessage touch(final Object p0);
    
    FullHttpMessage duplicate();
}
