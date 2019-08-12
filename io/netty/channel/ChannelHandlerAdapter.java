// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.channel;

import java.net.SocketAddress;
import java.util.Map;
import java.lang.annotation.Annotation;
import io.netty.util.internal.InternalThreadLocalMap;

public class ChannelHandlerAdapter implements ChannelHandler
{
    boolean added;
    
    public boolean isSharable() {
        final Class<?> clazz = this.getClass();
        final Map<Class<?>, Boolean> cache = InternalThreadLocalMap.get().handlerSharableCache();
        Boolean sharable = cache.get(clazz);
        if (sharable == null) {
            sharable = clazz.isAnnotationPresent(Sharable.class);
            cache.put(clazz, sharable);
        }
        return sharable;
    }
    
    @Skip
    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) throws Exception {
    }
    
    @Skip
    @Override
    public void handlerRemoved(final ChannelHandlerContext ctx) throws Exception {
    }
    
    @Skip
    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        ctx.fireExceptionCaught(cause);
    }
    
    @Skip
    @Override
    public void channelRegistered(final ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelRegistered();
    }
    
    @Skip
    @Override
    public void channelUnregistered(final ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelUnregistered();
    }
    
    @Skip
    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelActive();
    }
    
    @Skip
    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelInactive();
    }
    
    @Skip
    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        ctx.fireChannelRead(msg);
    }
    
    @Skip
    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelReadComplete();
    }
    
    @Skip
    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) throws Exception {
        ctx.fireUserEventTriggered(evt);
    }
    
    @Skip
    @Override
    public void channelWritabilityChanged(final ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelWritabilityChanged();
    }
    
    @Skip
    @Override
    public void bind(final ChannelHandlerContext ctx, final SocketAddress localAddress, final ChannelPromise promise) throws Exception {
        ctx.bind(localAddress, promise);
    }
    
    @Skip
    @Override
    public void connect(final ChannelHandlerContext ctx, final SocketAddress remoteAddress, final SocketAddress localAddress, final ChannelPromise promise) throws Exception {
        ctx.connect(remoteAddress, localAddress, promise);
    }
    
    @Skip
    @Override
    public void disconnect(final ChannelHandlerContext ctx, final ChannelPromise promise) throws Exception {
        ctx.disconnect(promise);
    }
    
    @Skip
    @Override
    public void close(final ChannelHandlerContext ctx, final ChannelPromise promise) throws Exception {
        ctx.close(promise);
    }
    
    @Skip
    @Override
    public void deregister(final ChannelHandlerContext ctx, final ChannelPromise promise) throws Exception {
        ctx.deregister(promise);
    }
    
    @Skip
    @Override
    public void read(final ChannelHandlerContext ctx) throws Exception {
        ctx.read();
    }
    
    @Skip
    @Override
    public void write(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise promise) throws Exception {
        ctx.write(msg, promise);
    }
    
    @Skip
    @Override
    public void flush(final ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}
