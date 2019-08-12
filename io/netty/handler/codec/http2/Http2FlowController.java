// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http2;

import io.netty.channel.ChannelHandlerContext;

public interface Http2FlowController
{
    void initialWindowSize(final int p0) throws Http2Exception;
    
    int initialWindowSize();
    
    int windowSize(final Http2Stream p0);
    
    void incrementWindowSize(final ChannelHandlerContext p0, final Http2Stream p1, final int p2) throws Http2Exception;
}
