// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http2;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public interface Http2LocalFlowController extends Http2FlowController
{
    void receiveFlowControlledFrame(final ChannelHandlerContext p0, final Http2Stream p1, final ByteBuf p2, final int p3, final boolean p4) throws Http2Exception;
    
    void consumeBytes(final ChannelHandlerContext p0, final Http2Stream p1, final int p2) throws Http2Exception;
    
    int unconsumedBytes(final Http2Stream p0);
}
