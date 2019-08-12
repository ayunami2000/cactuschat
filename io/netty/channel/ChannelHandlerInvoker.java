// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.channel;

import java.net.SocketAddress;
import io.netty.util.concurrent.EventExecutor;

public interface ChannelHandlerInvoker
{
    EventExecutor executor();
    
    void invokeChannelRegistered(final ChannelHandlerContext p0);
    
    void invokeChannelUnregistered(final ChannelHandlerContext p0);
    
    void invokeChannelActive(final ChannelHandlerContext p0);
    
    void invokeChannelInactive(final ChannelHandlerContext p0);
    
    void invokeExceptionCaught(final ChannelHandlerContext p0, final Throwable p1);
    
    void invokeUserEventTriggered(final ChannelHandlerContext p0, final Object p1);
    
    void invokeChannelRead(final ChannelHandlerContext p0, final Object p1);
    
    void invokeChannelReadComplete(final ChannelHandlerContext p0);
    
    void invokeChannelWritabilityChanged(final ChannelHandlerContext p0);
    
    void invokeBind(final ChannelHandlerContext p0, final SocketAddress p1, final ChannelPromise p2);
    
    void invokeConnect(final ChannelHandlerContext p0, final SocketAddress p1, final SocketAddress p2, final ChannelPromise p3);
    
    void invokeDisconnect(final ChannelHandlerContext p0, final ChannelPromise p1);
    
    void invokeClose(final ChannelHandlerContext p0, final ChannelPromise p1);
    
    void invokeDeregister(final ChannelHandlerContext p0, final ChannelPromise p1);
    
    void invokeRead(final ChannelHandlerContext p0);
    
    void invokeWrite(final ChannelHandlerContext p0, final Object p1, final ChannelPromise p2);
    
    void invokeFlush(final ChannelHandlerContext p0);
}
