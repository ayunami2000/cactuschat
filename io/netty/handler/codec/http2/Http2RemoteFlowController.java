// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http2;

import io.netty.channel.ChannelHandlerContext;

public interface Http2RemoteFlowController extends Http2FlowController
{
    void sendFlowControlled(final ChannelHandlerContext p0, final Http2Stream p1, final FlowControlled p2);
    
    public interface FlowControlled
    {
        int size();
        
        void error(final Throwable p0);
        
        boolean write(final int p0);
    }
}
