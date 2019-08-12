// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http2;

import io.netty.buffer.ByteBuf;

public interface Http2HeadersEncoder
{
    void encodeHeaders(final Http2Headers p0, final ByteBuf p1) throws Http2Exception;
    
    Configuration configuration();
    
    public interface Configuration
    {
        Http2HeaderTable headerTable();
    }
}
