// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http;

import io.netty.buffer.ByteBuf;

public interface FullHttpRequest extends HttpRequest, FullHttpMessage
{
    FullHttpRequest copy(final ByteBuf p0);
    
    FullHttpRequest copy();
    
    FullHttpRequest retain(final int p0);
    
    FullHttpRequest retain();
    
    FullHttpRequest touch();
    
    FullHttpRequest touch(final Object p0);
    
    FullHttpRequest duplicate();
    
    FullHttpRequest setProtocolVersion(final HttpVersion p0);
    
    FullHttpRequest setMethod(final HttpMethod p0);
    
    FullHttpRequest setUri(final String p0);
}
