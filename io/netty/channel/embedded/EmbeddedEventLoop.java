// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.channel.embedded;

import io.netty.util.concurrent.EventExecutorGroup;
import java.net.SocketAddress;
import io.netty.channel.ChannelHandlerInvokerUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.concurrent.EventExecutor;
import io.netty.channel.DefaultChannelPromise;
import io.netty.channel.ChannelFuture;
import io.netty.channel.Channel;
import io.netty.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import io.netty.channel.EventLoopGroup;
import java.util.ArrayDeque;
import java.util.Queue;
import io.netty.channel.EventLoop;
import io.netty.channel.ChannelHandlerInvoker;
import io.netty.util.concurrent.AbstractScheduledEventExecutor;

final class EmbeddedEventLoop extends AbstractScheduledEventExecutor implements ChannelHandlerInvoker, EventLoop
{
    private final Queue<Runnable> tasks;
    
    EmbeddedEventLoop() {
        this.tasks = new ArrayDeque<Runnable>(2);
    }
    
    @Override
    public EventLoop unwrap() {
        return this;
    }
    
    @Override
    public EventLoopGroup parent() {
        return (EventLoopGroup)super.parent();
    }
    
    @Override
    public EventLoop next() {
        return (EventLoop)super.next();
    }
    
    @Override
    public void execute(final Runnable command) {
        if (command == null) {
            throw new NullPointerException("command");
        }
        this.tasks.add(command);
    }
    
    void runTasks() {
        while (true) {
            final Runnable task = this.tasks.poll();
            if (task == null) {
                break;
            }
            task.run();
        }
    }
    
    long runScheduledTasks() {
        final long time = AbstractScheduledEventExecutor.nanoTime();
        while (true) {
            final Runnable task = this.pollScheduledTask(time);
            if (task == null) {
                break;
            }
            task.run();
        }
        return this.nextScheduledTaskNano();
    }
    
    long nextScheduledTask() {
        return this.nextScheduledTaskNano();
    }
    
    @Override
    protected void cancelScheduledTasks() {
        super.cancelScheduledTasks();
    }
    
    @Override
    public Future<?> shutdownGracefully(final long quietPeriod, final long timeout, final TimeUnit unit) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Future<?> terminationFuture() {
        throw new UnsupportedOperationException();
    }
    
    @Deprecated
    @Override
    public void shutdown() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean isShuttingDown() {
        return false;
    }
    
    @Override
    public boolean isShutdown() {
        return false;
    }
    
    @Override
    public boolean isTerminated() {
        return false;
    }
    
    @Override
    public boolean awaitTermination(final long timeout, final TimeUnit unit) {
        return false;
    }
    
    @Override
    public ChannelFuture register(final Channel channel) {
        return this.register(channel, new DefaultChannelPromise(channel, this));
    }
    
    @Override
    public ChannelFuture register(final Channel channel, final ChannelPromise promise) {
        channel.unsafe().register(this, promise);
        return promise;
    }
    
    @Override
    public boolean inEventLoop() {
        return true;
    }
    
    @Override
    public boolean inEventLoop(final Thread thread) {
        return true;
    }
    
    @Override
    public ChannelHandlerInvoker asInvoker() {
        return this;
    }
    
    @Override
    public EventExecutor executor() {
        return this;
    }
    
    @Override
    public void invokeChannelRegistered(final ChannelHandlerContext ctx) {
        ChannelHandlerInvokerUtil.invokeChannelRegisteredNow(ctx);
    }
    
    @Override
    public void invokeChannelUnregistered(final ChannelHandlerContext ctx) {
        ChannelHandlerInvokerUtil.invokeChannelUnregisteredNow(ctx);
    }
    
    @Override
    public void invokeChannelActive(final ChannelHandlerContext ctx) {
        ChannelHandlerInvokerUtil.invokeChannelActiveNow(ctx);
    }
    
    @Override
    public void invokeChannelInactive(final ChannelHandlerContext ctx) {
        ChannelHandlerInvokerUtil.invokeChannelInactiveNow(ctx);
    }
    
    @Override
    public void invokeExceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        ChannelHandlerInvokerUtil.invokeExceptionCaughtNow(ctx, cause);
    }
    
    @Override
    public void invokeUserEventTriggered(final ChannelHandlerContext ctx, final Object event) {
        ChannelHandlerInvokerUtil.invokeUserEventTriggeredNow(ctx, event);
    }
    
    @Override
    public void invokeChannelRead(final ChannelHandlerContext ctx, final Object msg) {
        ChannelHandlerInvokerUtil.invokeChannelReadNow(ctx, msg);
    }
    
    @Override
    public void invokeChannelReadComplete(final ChannelHandlerContext ctx) {
        ChannelHandlerInvokerUtil.invokeChannelReadCompleteNow(ctx);
    }
    
    @Override
    public void invokeChannelWritabilityChanged(final ChannelHandlerContext ctx) {
        ChannelHandlerInvokerUtil.invokeChannelWritabilityChangedNow(ctx);
    }
    
    @Override
    public void invokeBind(final ChannelHandlerContext ctx, final SocketAddress localAddress, final ChannelPromise promise) {
        ChannelHandlerInvokerUtil.invokeBindNow(ctx, localAddress, promise);
    }
    
    @Override
    public void invokeConnect(final ChannelHandlerContext ctx, final SocketAddress remoteAddress, final SocketAddress localAddress, final ChannelPromise promise) {
        ChannelHandlerInvokerUtil.invokeConnectNow(ctx, remoteAddress, localAddress, promise);
    }
    
    @Override
    public void invokeDisconnect(final ChannelHandlerContext ctx, final ChannelPromise promise) {
        ChannelHandlerInvokerUtil.invokeDisconnectNow(ctx, promise);
    }
    
    @Override
    public void invokeClose(final ChannelHandlerContext ctx, final ChannelPromise promise) {
        ChannelHandlerInvokerUtil.invokeCloseNow(ctx, promise);
    }
    
    @Override
    public void invokeDeregister(final ChannelHandlerContext ctx, final ChannelPromise promise) {
        ChannelHandlerInvokerUtil.invokeDeregisterNow(ctx, promise);
    }
    
    @Override
    public void invokeRead(final ChannelHandlerContext ctx) {
        ChannelHandlerInvokerUtil.invokeReadNow(ctx);
    }
    
    @Override
    public void invokeWrite(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise promise) {
        ChannelHandlerInvokerUtil.invokeWriteNow(ctx, msg, promise);
    }
    
    @Override
    public void invokeFlush(final ChannelHandlerContext ctx) {
        ChannelHandlerInvokerUtil.invokeFlushNow(ctx);
    }
}
