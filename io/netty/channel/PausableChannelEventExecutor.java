// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.channel;

import io.netty.util.internal.RunnableEventExecutorAdapter;
import io.netty.util.internal.CallableEventExecutorAdapter;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;
import java.util.Collection;
import io.netty.util.concurrent.ScheduledFuture;
import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.ProgressivePromise;
import io.netty.util.concurrent.Promise;
import io.netty.util.concurrent.EventExecutorGroup;
import java.util.Set;
import java.net.SocketAddress;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.PausableEventExecutor;

abstract class PausableChannelEventExecutor implements PausableEventExecutor, ChannelHandlerInvoker
{
    abstract Channel channel();
    
    abstract ChannelHandlerInvoker unwrapInvoker();
    
    @Override
    public void invokeFlush(final ChannelHandlerContext ctx) {
        this.unwrapInvoker().invokeFlush(ctx);
    }
    
    @Override
    public EventExecutor executor() {
        return this;
    }
    
    @Override
    public void invokeChannelRegistered(final ChannelHandlerContext ctx) {
        this.unwrapInvoker().invokeChannelRegistered(ctx);
    }
    
    @Override
    public void invokeChannelUnregistered(final ChannelHandlerContext ctx) {
        this.unwrapInvoker().invokeChannelUnregistered(ctx);
    }
    
    @Override
    public void invokeChannelActive(final ChannelHandlerContext ctx) {
        this.unwrapInvoker().invokeChannelActive(ctx);
    }
    
    @Override
    public void invokeChannelInactive(final ChannelHandlerContext ctx) {
        this.unwrapInvoker().invokeChannelInactive(ctx);
    }
    
    @Override
    public void invokeExceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        this.unwrapInvoker().invokeExceptionCaught(ctx, cause);
    }
    
    @Override
    public void invokeUserEventTriggered(final ChannelHandlerContext ctx, final Object event) {
        this.unwrapInvoker().invokeUserEventTriggered(ctx, event);
    }
    
    @Override
    public void invokeChannelRead(final ChannelHandlerContext ctx, final Object msg) {
        this.unwrapInvoker().invokeChannelRead(ctx, msg);
    }
    
    @Override
    public void invokeChannelReadComplete(final ChannelHandlerContext ctx) {
        this.unwrapInvoker().invokeChannelReadComplete(ctx);
    }
    
    @Override
    public void invokeChannelWritabilityChanged(final ChannelHandlerContext ctx) {
        this.unwrapInvoker().invokeChannelWritabilityChanged(ctx);
    }
    
    @Override
    public void invokeBind(final ChannelHandlerContext ctx, final SocketAddress localAddress, final ChannelPromise promise) {
        this.unwrapInvoker().invokeBind(ctx, localAddress, promise);
    }
    
    @Override
    public void invokeConnect(final ChannelHandlerContext ctx, final SocketAddress remoteAddress, final SocketAddress localAddress, final ChannelPromise promise) {
        this.unwrapInvoker().invokeConnect(ctx, remoteAddress, localAddress, promise);
    }
    
    @Override
    public void invokeDisconnect(final ChannelHandlerContext ctx, final ChannelPromise promise) {
        this.unwrapInvoker().invokeDisconnect(ctx, promise);
    }
    
    @Override
    public void invokeClose(final ChannelHandlerContext ctx, final ChannelPromise promise) {
        this.unwrapInvoker().invokeClose(ctx, promise);
    }
    
    @Override
    public void invokeDeregister(final ChannelHandlerContext ctx, final ChannelPromise promise) {
        this.unwrapInvoker().invokeDeregister(ctx, promise);
    }
    
    @Override
    public void invokeRead(final ChannelHandlerContext ctx) {
        this.unwrapInvoker().invokeRead(ctx);
    }
    
    @Override
    public void invokeWrite(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise promise) {
        this.unwrapInvoker().invokeWrite(ctx, msg, promise);
    }
    
    @Override
    public EventExecutor next() {
        return this.unwrap().next();
    }
    
    @Override
    public <E extends EventExecutor> Set<E> children() {
        return this.unwrap().children();
    }
    
    @Override
    public EventExecutorGroup parent() {
        return this.unwrap().parent();
    }
    
    @Override
    public boolean inEventLoop() {
        return this.unwrap().inEventLoop();
    }
    
    @Override
    public boolean inEventLoop(final Thread thread) {
        return this.unwrap().inEventLoop(thread);
    }
    
    @Override
    public <V> Promise<V> newPromise() {
        return this.unwrap().newPromise();
    }
    
    @Override
    public <V> ProgressivePromise<V> newProgressivePromise() {
        return this.unwrap().newProgressivePromise();
    }
    
    @Override
    public <V> Future<V> newSucceededFuture(final V result) {
        return this.unwrap().newSucceededFuture(result);
    }
    
    @Override
    public <V> Future<V> newFailedFuture(final Throwable cause) {
        return this.unwrap().newFailedFuture(cause);
    }
    
    @Override
    public boolean isShuttingDown() {
        return this.unwrap().isShuttingDown();
    }
    
    @Override
    public Future<?> shutdownGracefully() {
        return this.unwrap().shutdownGracefully();
    }
    
    @Override
    public Future<?> shutdownGracefully(final long quietPeriod, final long timeout, final TimeUnit unit) {
        return this.unwrap().shutdownGracefully(quietPeriod, timeout, unit);
    }
    
    @Override
    public Future<?> terminationFuture() {
        return this.unwrap().terminationFuture();
    }
    
    @Deprecated
    @Override
    public void shutdown() {
        this.unwrap().shutdown();
    }
    
    @Deprecated
    @Override
    public List<Runnable> shutdownNow() {
        return this.unwrap().shutdownNow();
    }
    
    @Override
    public Future<?> submit(final Runnable task) {
        if (!this.isAcceptingNewTasks()) {
            throw new RejectedExecutionException();
        }
        return this.unwrap().submit(task);
    }
    
    @Override
    public <T> Future<T> submit(final Runnable task, final T result) {
        if (!this.isAcceptingNewTasks()) {
            throw new RejectedExecutionException();
        }
        return this.unwrap().submit(task, result);
    }
    
    @Override
    public <T> Future<T> submit(final Callable<T> task) {
        if (!this.isAcceptingNewTasks()) {
            throw new RejectedExecutionException();
        }
        return this.unwrap().submit(task);
    }
    
    @Override
    public ScheduledFuture<?> schedule(final Runnable command, final long delay, final TimeUnit unit) {
        if (!this.isAcceptingNewTasks()) {
            throw new RejectedExecutionException();
        }
        return this.unwrap().schedule((Runnable)new ChannelRunnableEventExecutor(this.channel(), command), delay, unit);
    }
    
    @Override
    public <V> ScheduledFuture<V> schedule(final Callable<V> callable, final long delay, final TimeUnit unit) {
        if (!this.isAcceptingNewTasks()) {
            throw new RejectedExecutionException();
        }
        return this.unwrap().schedule((Callable<V>)new ChannelCallableEventExecutor<V>(this.channel(), callable), delay, unit);
    }
    
    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(final Runnable command, final long initialDelay, final long period, final TimeUnit unit) {
        if (!this.isAcceptingNewTasks()) {
            throw new RejectedExecutionException();
        }
        return this.unwrap().scheduleAtFixedRate((Runnable)new ChannelRunnableEventExecutor(this.channel(), command), initialDelay, period, unit);
    }
    
    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(final Runnable command, final long initialDelay, final long delay, final TimeUnit unit) {
        if (!this.isAcceptingNewTasks()) {
            throw new RejectedExecutionException();
        }
        return this.unwrap().scheduleWithFixedDelay((Runnable)new ChannelRunnableEventExecutor(this.channel(), command), initialDelay, delay, unit);
    }
    
    @Override
    public boolean isShutdown() {
        return this.unwrap().isShutdown();
    }
    
    @Override
    public boolean isTerminated() {
        return this.unwrap().isTerminated();
    }
    
    @Override
    public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
        return this.unwrap().awaitTermination(timeout, unit);
    }
    
    @Override
    public <T> List<java.util.concurrent.Future<T>> invokeAll(final Collection<? extends Callable<T>> tasks) throws InterruptedException {
        if (!this.isAcceptingNewTasks()) {
            throw new RejectedExecutionException();
        }
        return this.unwrap().invokeAll(tasks);
    }
    
    @Override
    public <T> List<java.util.concurrent.Future<T>> invokeAll(final Collection<? extends Callable<T>> tasks, final long timeout, final TimeUnit unit) throws InterruptedException {
        if (!this.isAcceptingNewTasks()) {
            throw new RejectedExecutionException();
        }
        return this.unwrap().invokeAll(tasks, timeout, unit);
    }
    
    @Override
    public <T> T invokeAny(final Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        if (!this.isAcceptingNewTasks()) {
            throw new RejectedExecutionException();
        }
        return this.unwrap().invokeAny(tasks);
    }
    
    @Override
    public <T> T invokeAny(final Collection<? extends Callable<T>> tasks, final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (!this.isAcceptingNewTasks()) {
            throw new RejectedExecutionException();
        }
        return this.unwrap().invokeAny(tasks, timeout, unit);
    }
    
    @Override
    public void execute(final Runnable command) {
        if (!this.isAcceptingNewTasks()) {
            throw new RejectedExecutionException();
        }
        this.unwrap().execute(command);
    }
    
    @Override
    public void close() throws Exception {
        this.unwrap().close();
    }
    
    private static final class ChannelCallableEventExecutor<V> implements CallableEventExecutorAdapter<V>
    {
        final Channel channel;
        final Callable<V> callable;
        
        ChannelCallableEventExecutor(final Channel channel, final Callable<V> callable) {
            this.channel = channel;
            this.callable = callable;
        }
        
        @Override
        public EventExecutor executor() {
            return this.channel.eventLoop();
        }
        
        @Override
        public Callable unwrap() {
            return this.callable;
        }
        
        @Override
        public V call() throws Exception {
            return this.callable.call();
        }
    }
    
    private static final class ChannelRunnableEventExecutor implements RunnableEventExecutorAdapter
    {
        final Channel channel;
        final Runnable runnable;
        
        ChannelRunnableEventExecutor(final Channel channel, final Runnable runnable) {
            this.channel = channel;
            this.runnable = runnable;
        }
        
        @Override
        public EventExecutor executor() {
            return this.channel.eventLoop();
        }
        
        @Override
        public Runnable unwrap() {
            return this.runnable;
        }
        
        @Override
        public void run() {
            this.runnable.run();
        }
    }
}
