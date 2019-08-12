// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http;

import io.netty.buffer.ByteBuf;

public interface FullHttpResponse extends HttpResponse, FullHttpMessage
{
    FullHttpResponse copy(final ByteBuf p0);
    
    FullHttpResponse copy();
    
    FullHttpResponse retain(final int p0);
    
    FullHttpResponse retain();
    
    FullHttpResponse touch();
    
    FullHttpResponse touch(final Object p0);
    
    FullHttpResponse duplicate();
    
    FullHttpResponse setProtocolVersion(final HttpVersion p0);
    
    FullHttpResponse setStatus(final HttpResponseStatus p0);
}
