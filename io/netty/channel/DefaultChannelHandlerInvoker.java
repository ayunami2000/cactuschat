// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.channel;

import io.netty.util.Recycler;
import io.netty.util.internal.RecyclableMpscLinkedQueueNode;
import io.netty.util.ReferenceCountUtil;
import java.net.SocketAddress;
import io.netty.util.internal.OneTimeTask;
import io.netty.util.concurrent.EventExecutor;

public class DefaultChannelHandlerInvoker implements ChannelHandlerInvoker
{
    private final EventExecutor executor;
    
    public DefaultChannelHandlerInvoker(final EventExecutor executor) {
        if (executor == null) {
            throw new NullPointerException("executor");
        }
        this.executor = executor;
    }
    
    @Override
    public EventExecutor executor() {
        return this.executor;
    }
    
    @Override
    public void invokeChannelRegistered(final ChannelHandlerContext ctx) {
        if (this.executor.inEventLoop()) {
            ChannelHandlerInvokerUtil.invokeChannelRegisteredNow(ctx);
        }
        else {
            this.executor.execute(new OneTimeTask() {
                @Override
                public void run() {
                    ChannelHandlerInvokerUtil.invokeChannelRegisteredNow(ctx);
                }
            });
        }
    }
    
    @Override
    public void invokeChannelUnregistered(final ChannelHandlerContext ctx) {
        if (this.executor.inEventLoop()) {
            ChannelHandlerInvokerUtil.invokeChannelUnregisteredNow(ctx);
        }
        else {
            this.executor.execute(new OneTimeTask() {
                @Override
                public void run() {
                    ChannelHandlerInvokerUtil.invokeChannelUnregisteredNow(ctx);
                }
            });
        }
    }
    
    @Override
    public void invokeChannelActive(final ChannelHandlerContext ctx) {
        if (this.executor.inEventLoop()) {
            ChannelHandlerInvokerUtil.invokeChannelActiveNow(ctx);
        }
        else {
            this.executor.execute(new OneTimeTask() {
                @Override
                public void run() {
                    ChannelHandlerInvokerUtil.invokeChannelActiveNow(ctx);
                }
            });
        }
    }
    
    @Override
    public void invokeChannelInactive(final ChannelHandlerContext ctx) {
        if (this.executor.inEventLoop()) {
            ChannelHandlerInvokerUtil.invokeChannelInactiveNow(ctx);
        }
        else {
            this.executor.execute(new OneTimeTask() {
                @Override
                public void run() {
                    ChannelHandlerInvokerUtil.invokeChannelInactiveNow(ctx);
                }
            });
        }
    }
    
    @Override
    public void invokeExceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        if (cause == null) {
            throw new NullPointerException("cause");
        }
        if (this.executor.inEventLoop()) {
            ChannelHandlerInvokerUtil.invokeExceptionCaughtNow(ctx, cause);
        }
        else {
            try {
                this.executor.execute(new OneTimeTask() {
                    @Override
                    public void run() {
                        ChannelHandlerInvokerUtil.invokeExceptionCaughtNow(ctx, cause);
                    }
                });
            }
            catch (Throwable t) {
                if (DefaultChannelPipeline.logger.isWarnEnabled()) {
                    DefaultChannelPipeline.logger.warn("Failed to submit an exceptionCaught() event.", t);
                    DefaultChannelPipeline.logger.warn("The exceptionCaught() event that was failed to submit was:", cause);
                }
            }
        }
    }
    
    @Override
    public void invokeUserEventTriggered(final ChannelHandlerContext ctx, final Object event) {
        if (event == null) {
            throw new NullPointerException("event");
        }
        if (this.executor.inEventLoop()) {
            ChannelHandlerInvokerUtil.invokeUserEventTriggeredNow(ctx, event);
        }
        else {
            this.safeExecuteInbound(new OneTimeTask() {
                @Override
                public void run() {
                    ChannelHandlerInvokerUtil.invokeUserEventTriggeredNow(ctx, event);
                }
            }, event);
        }
    }
    
    @Override
    public void invokeChannelRead(final ChannelHandlerContext ctx, final Object msg) {
        if (msg == null) {
            throw new NullPointerException("msg");
        }
        if (this.executor.inEventLoop()) {
            ChannelHandlerInvokerUtil.invokeChannelReadNow(ctx, msg);
        }
        else {
            this.safeExecuteInbound(new OneTimeTask() {
                @Override
                public void run() {
                    ChannelHandlerInvokerUtil.invokeChannelReadNow(ctx, msg);
                }
            }, msg);
        }
    }
    
    @Override
    public void invokeChannelReadComplete(final ChannelHandlerContext ctx) {
        if (this.executor.inEventLoop()) {
            ChannelHandlerInvokerUtil.invokeChannelReadCompleteNow(ctx);
        }
        else {
            final AbstractChannelHandlerContext dctx = (AbstractChannelHandlerContext)ctx;
            Runnable task = dctx.invokeChannelReadCompleteTask;
            if (task == null) {
                task = (dctx.invokeChannelReadCompleteTask = new Runnable() {
                    @Override
                    public void run() {
                        ChannelHandlerInvokerUtil.invokeChannelReadCompleteNow(ctx);
                    }
                });
            }
            this.executor.execute(task);
        }
    }
    
    @Override
    public void invokeChannelWritabilityChanged(final ChannelHandlerContext ctx) {
        if (this.executor.inEventLoop()) {
            ChannelHandlerInvokerUtil.invokeChannelWritabilityChangedNow(ctx);
        }
        else {
            final AbstractChannelHandlerContext dctx = (AbstractChannelHandlerContext)ctx;
            Runnable task = dctx.invokeChannelWritableStateChangedTask;
            if (task == null) {
                task = (dctx.invokeChannelWritableStateChangedTask = new Runnable() {
                    @Override
                    public void run() {
                        ChannelHandlerInvokerUtil.invokeChannelWritabilityChangedNow(ctx);
                    }
                });
            }
            this.executor.execute(task);
        }
    }
    
    @Override
    public void invokeBind(final ChannelHandlerContext ctx, final SocketAddress localAddress, final ChannelPromise promise) {
        if (localAddress == null) {
            throw new NullPointerException("localAddress");
        }
        if (!ChannelHandlerInvokerUtil.validatePromise(ctx, promise, false)) {
            return;
        }
        if (this.executor.inEventLoop()) {
            ChannelHandlerInvokerUtil.invokeBindNow(ctx, localAddress, promise);
        }
        else {
            this.safeExecuteOutbound(new OneTimeTask() {
                @Override
                public void run() {
                    ChannelHandlerInvokerUtil.invokeBindNow(ctx, localAddress, promise);
                }
            }, promise);
        }
    }
    
    @Override
    public void invokeConnect(final ChannelHandlerContext ctx, final SocketAddress remoteAddress, final SocketAddress localAddress, final ChannelPromise promise) {
        if (remoteAddress == null) {
            throw new NullPointerException("remoteAddress");
        }
        if (!ChannelHandlerInvokerUtil.validatePromise(ctx, promise, false)) {
            return;
        }
        if (this.executor.inEventLoop()) {
            ChannelHandlerInvokerUtil.invokeConnectNow(ctx, remoteAddress, localAddress, promise);
        }
        else {
            this.safeExecuteOutbound(new OneTimeTask() {
                @Override
                public void run() {
                    ChannelHandlerInvokerUtil.invokeConnectNow(ctx, remoteAddress, localAddress, promise);
                }
            }, promise);
        }
    }
    
    @Override
    public void invokeDisconnect(final ChannelHandlerContext ctx, final ChannelPromise promise) {
        if (!ChannelHandlerInvokerUtil.validatePromise(ctx, promise, false)) {
            return;
        }
        if (this.executor.inEventLoop()) {
            ChannelHandlerInvokerUtil.invokeDisconnectNow(ctx, promise);
        }
        else {
            this.safeExecuteOutbound(new OneTimeTask() {
                @Override
                public void run() {
                    ChannelHandlerInvokerUtil.invokeDisconnectNow(ctx, promise);
                }
            }, promise);
        }
    }
    
    @Override
    public void invokeClose(final ChannelHandlerContext ctx, final ChannelPromise promise) {
        if (!ChannelHandlerInvokerUtil.validatePromise(ctx, promise, false)) {
            return;
        }
        if (this.executor.inEventLoop()) {
            ChannelHandlerInvokerUtil.invokeCloseNow(ctx, promise);
        }
        else {
            this.safeExecuteOutbound(new OneTimeTask() {
                @Override
                public void run() {
                    ChannelHandlerInvokerUtil.invokeCloseNow(ctx, promise);
                }
            }, promise);
        }
    }
    
    @Override
    public void invokeDeregister(final ChannelHandlerContext ctx, final ChannelPromise promise) {
        if (!ChannelHandlerInvokerUtil.validatePromise(ctx, promise, false)) {
            return;
        }
        if (this.executor.inEventLoop()) {
            ChannelHandlerInvokerUtil.invokeDeregisterNow(ctx, promise);
        }
        else {
            this.safeExecuteOutbound(new OneTimeTask() {
                @Override
                public void run() {
                    ChannelHandlerInvokerUtil.invokeDeregisterNow(ctx, promise);
                }
            }, promise);
        }
    }
    
    @Override
    public void invokeRead(final ChannelHandlerContext ctx) {
        if (this.executor.inEventLoop()) {
            ChannelHandlerInvokerUtil.invokeReadNow(ctx);
        }
        else {
            final AbstractChannelHandlerContext dctx = (AbstractChannelHandlerContext)ctx;
            Runnable task = dctx.invokeReadTask;
            if (task == null) {
                task = (dctx.invokeReadTask = new Runnable() {
                    @Override
                    public void run() {
                        ChannelHandlerInvokerUtil.invokeReadNow(ctx);
                    }
                });
            }
            this.executor.execute(task);
        }
    }
    
    @Override
    public void invokeWrite(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise promise) {
        if (msg == null) {
            throw new NullPointerException("msg");
        }
        if (!ChannelHandlerInvokerUtil.validatePromise(ctx, promise, true)) {
            ReferenceCountUtil.release(msg);
            return;
        }
        if (this.executor.inEventLoop()) {
            ChannelHandlerInvokerUtil.invokeWriteNow(ctx, msg, promise);
        }
        else {
            final AbstractChannel channel = (AbstractChannel)ctx.channel();
            final int size = channel.estimatorHandle().size(msg);
            if (size > 0) {
                final ChannelOutboundBuffer buffer = channel.unsafe().outboundBuffer();
                if (buffer != null) {
                    buffer.incrementPendingOutboundBytes(size);
                }
            }
            this.safeExecuteOutbound(newInstance(ctx, msg, size, promise), promise, msg);
        }
    }
    
    @Override
    public void invokeFlush(final ChannelHandlerContext ctx) {
        if (this.executor.inEventLoop()) {
            ChannelHandlerInvokerUtil.invokeFlushNow(ctx);
        }
        else {
            final AbstractChannelHandlerContext dctx = (AbstractChannelHandlerContext)ctx;
            Runnable task = dctx.invokeFlushTask;
            if (task == null) {
                task = (dctx.invokeFlushTask = new Runnable() {
                    @Override
                    public void run() {
                        ChannelHandlerInvokerUtil.invokeFlushNow(ctx);
                    }
                });
            }
            this.executor.execute(task);
        }
    }
    
    private void safeExecuteInbound(final Runnable task, final Object msg) {
        boolean success = false;
        try {
            this.executor.execute(task);
            success = true;
        }
        finally {
            if (!success) {
                ReferenceCountUtil.release(msg);
            }
        }
    }
    
    private void safeExecuteOutbound(final Runnable task, final ChannelPromise promise) {
        try {
            this.executor.execute(task);
        }
        catch (Throwable cause) {
            promise.setFailure(cause);
        }
    }
    
    private void safeExecuteOutbound(final Runnable task, final ChannelPromise promise, final Object msg) {
        try {
            this.executor.execute(task);
        }
        catch (Throwable cause) {
            try {
                promise.setFailure(cause);
            }
            finally {
                ReferenceCountUtil.release(msg);
            }
        }
    }
    
    static final class WriteTask extends RecyclableMpscLinkedQueueNode<SingleThreadEventLoop.NonWakeupRunnable> implements SingleThreadEventLoop.NonWakeupRunnable
    {
        private ChannelHandlerContext ctx;
        private Object msg;
        private ChannelPromise promise;
        private int size;
        private static final Recycler<WriteTask> RECYCLER;
        
        private static WriteTask newInstance(final ChannelHandlerContext ctx, final Object msg, final int size, final ChannelPromise promise) {
            final WriteTask task = WriteTask.RECYCLER.get();
            task.ctx = ctx;
            task.msg = msg;
            task.promise = promise;
            task.size = size;
            return task;
        }
        
        private WriteTask(final Recycler.Handle<WriteTask> handle) {
            super(handle);
        }
        
        @Override
        public void run() {
            try {
                if (this.size > 0) {
                    final ChannelOutboundBuffer buffer = this.ctx.channel().unsafe().outboundBuffer();
                    if (buffer != null) {
                        buffer.decrementPendingOutboundBytes(this.size);
                    }
                }
                ChannelHandlerInvokerUtil.invokeWriteNow(this.ctx, this.msg, this.promise);
            }
            finally {
                this.ctx = null;
                this.msg = null;
                this.promise = null;
            }
        }
        
        @Override
        public SingleThreadEventLoop.NonWakeupRunnable value() {
            return this;
        }
        
        static {
            RECYCLER = new Recycler<WriteTask>() {
                @Override
                protected WriteTask newObject(final Handle<WriteTask> handle) {
                    return new WriteTask((Handle)handle);
                }
            };
        }
    }
}
