// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http2;

public interface Http2HeaderTable
{
    void maxHeaderTableSize(final int p0) throws Http2Exception;
    
    int maxHeaderTableSize();
    
    void maxHeaderListSize(final int p0) throws Http2Exception;
    
    int maxHeaderListSize();
}
