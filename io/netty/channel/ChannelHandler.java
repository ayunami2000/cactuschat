// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.channel;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.Annotation;
import java.net.SocketAddress;

public interface ChannelHandler
{
    void handlerAdded(final ChannelHandlerContext p0) throws Exception;
    
    void handlerRemoved(final ChannelHandlerContext p0) throws Exception;
    
    void exceptionCaught(final ChannelHandlerContext p0, final Throwable p1) throws Exception;
    
    void channelRegistered(final ChannelHandlerContext p0) throws Exception;
    
    void channelUnregistered(final ChannelHandlerContext p0) throws Exception;
    
    void channelActive(final ChannelHandlerContext p0) throws Exception;
    
    void channelInactive(final ChannelHandlerContext p0) throws Exception;
    
    void channelRead(final ChannelHandlerContext p0, final Object p1) throws Exception;
    
    void channelReadComplete(final ChannelHandlerContext p0) throws Exception;
    
    void userEventTriggered(final ChannelHandlerContext p0, final Object p1) throws Exception;
    
    void channelWritabilityChanged(final ChannelHandlerContext p0) throws Exception;
    
    void bind(final ChannelHandlerContext p0, final SocketAddress p1, final ChannelPromise p2) throws Exception;
    
    void connect(final ChannelHandlerContext p0, final SocketAddress p1, final SocketAddress p2, final ChannelPromise p3) throws Exception;
    
    void disconnect(final ChannelHandlerContext p0, final ChannelPromise p1) throws Exception;
    
    void close(final ChannelHandlerContext p0, final ChannelPromise p1) throws Exception;
    
    void deregister(final ChannelHandlerContext p0, final ChannelPromise p1) throws Exception;
    
    void read(final ChannelHandlerContext p0) throws Exception;
    
    void write(final ChannelHandlerContext p0, final Object p1, final ChannelPromise p2) throws Exception;
    
    void flush(final ChannelHandlerContext p0) throws Exception;
    
    @Target({ ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Skip {
    }
    
    @Inherited
    @Documented
    @Target({ ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Sharable {
    }
}
