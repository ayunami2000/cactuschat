// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http2;

import io.netty.buffer.ByteBuf;

public interface Http2HeadersDecoder
{
    Http2Headers decodeHeaders(final ByteBuf p0) throws Http2Exception;
    
    Configuration configuration();
    
    public interface Configuration
    {
        Http2HeaderTable headerTable();
    }
}
