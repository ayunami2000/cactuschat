// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.channel;

import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.Promise;
import java.net.SocketException;
import java.net.NoRouteToHostException;
import java.net.ConnectException;
import java.util.concurrent.RejectedExecutionException;
import io.netty.util.ReferenceCountUtil;
import java.util.concurrent.Executor;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Future;
import io.netty.util.internal.PlatformDependent;
import java.net.InetSocketAddress;
import io.netty.util.internal.OneTimeTask;
import io.netty.util.internal.EmptyArrays;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.buffer.ByteBufAllocator;
import io.netty.util.concurrent.EventExecutor;
import java.net.SocketAddress;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.ClosedChannelException;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.DefaultAttributeMap;

public abstract class AbstractChannel extends DefaultAttributeMap implements Channel
{
    private static final InternalLogger logger;
    static final ClosedChannelException CLOSED_CHANNEL_EXCEPTION;
    static final NotYetConnectedException NOT_YET_CONNECTED_EXCEPTION;
    private MessageSizeEstimator.Handle estimatorHandle;
    private final Channel parent;
    private final ChannelId id;
    private final Unsafe unsafe;
    private final DefaultChannelPipeline pipeline;
    private final ChannelFuture succeededFuture;
    private final VoidChannelPromise voidPromise;
    private final VoidChannelPromise unsafeVoidPromise;
    private final CloseFuture closeFuture;
    private volatile SocketAddress localAddress;
    private volatile SocketAddress remoteAddress;
    private volatile PausableChannelEventLoop eventLoop;
    private volatile boolean registered;
    private boolean strValActive;
    private String strVal;
    
    protected AbstractChannel(final Channel parent) {
        this.succeededFuture = new SucceededChannelFuture(this, null);
        this.voidPromise = new VoidChannelPromise(this, true);
        this.unsafeVoidPromise = new VoidChannelPromise(this, false);
        this.closeFuture = new CloseFuture(this);
        this.parent = parent;
        this.id = DefaultChannelId.newInstance();
        this.unsafe = this.newUnsafe();
        this.pipeline = new DefaultChannelPipeline(this);
    }
    
    protected AbstractChannel(final Channel parent, final ChannelId id) {
        this.succeededFuture = new SucceededChannelFuture(this, null);
        this.voidPromise = new VoidChannelPromise(this, true);
        this.unsafeVoidPromise = new VoidChannelPromise(this, false);
        this.closeFuture = new CloseFuture(this);
        this.parent = parent;
        this.id = id;
        this.unsafe = this.newUnsafe();
        this.pipeline = new DefaultChannelPipeline(this);
    }
    
    @Override
    public final ChannelId id() {
        return this.id;
    }
    
    @Override
    public boolean isWritable() {
        final ChannelOutboundBuffer buf = this.unsafe.outboundBuffer();
        return buf != null && buf.isWritable();
    }
    
    @Override
    public Channel parent() {
        return this.parent;
    }
    
    @Override
    public ChannelPipeline pipeline() {
        return this.pipeline;
    }
    
    @Override
    public ByteBufAllocator alloc() {
        return this.config().getAllocator();
    }
    
    @Override
    public final EventLoop eventLoop() {
        final EventLoop eventLoop = this.eventLoop;
        if (eventLoop == null) {
            throw new IllegalStateException("channel not registered to an event loop");
        }
        return eventLoop;
    }
    
    @Override
    public SocketAddress localAddress() {
        SocketAddress localAddress = this.localAddress;
        if (localAddress == null) {
            try {
                localAddress = (this.localAddress = this.unsafe().localAddress());
            }
            catch (Throwable t) {
                return null;
            }
        }
        return localAddress;
    }
    
    protected void invalidateLocalAddress() {
        this.localAddress = null;
    }
    
    @Override
    public SocketAddress remoteAddress() {
        SocketAddress remoteAddress = this.remoteAddress;
        if (remoteAddress == null) {
            try {
                remoteAddress = (this.remoteAddress = this.unsafe().remoteAddress());
            }
            catch (Throwable t) {
                return null;
            }
        }
        return remoteAddress;
    }
    
    protected void invalidateRemoteAddress() {
        this.remoteAddress = null;
    }
    
    @Override
    public boolean isRegistered() {
        return this.registered;
    }
    
    @Override
    public ChannelFuture bind(final SocketAddress localAddress) {
        return this.pipeline.bind(localAddress);
    }
    
    @Override
    public ChannelFuture connect(final SocketAddress remoteAddress) {
        return this.pipeline.connect(remoteAddress);
    }
    
    @Override
    public ChannelFuture connect(final SocketAddress remoteAddress, final SocketAddress localAddress) {
        return this.pipeline.connect(remoteAddress, localAddress);
    }
    
    @Override
    public ChannelFuture disconnect() {
        return this.pipeline.disconnect();
    }
    
    @Override
    public ChannelFuture close() {
        return this.pipeline.close();
    }
    
    @Override
    public ChannelFuture deregister() {
        this.eventLoop.rejectNewTasks();
        return this.pipeline.deregister();
    }
    
    @Override
    public Channel flush() {
        this.pipeline.flush();
        return this;
    }
    
    @Override
    public ChannelFuture bind(final SocketAddress localAddress, final ChannelPromise promise) {
        return this.pipeline.bind(localAddress, promise);
    }
    
    @Override
    public ChannelFuture connect(final SocketAddress remoteAddress, final ChannelPromise promise) {
        return this.pipeline.connect(remoteAddress, promise);
    }
    
    @Override
    public ChannelFuture connect(final SocketAddress remoteAddress, final SocketAddress localAddress, final ChannelPromise promise) {
        return this.pipeline.connect(remoteAddress, localAddress, promise);
    }
    
    @Override
    public ChannelFuture disconnect(final ChannelPromise promise) {
        return this.pipeline.disconnect(promise);
    }
    
    @Override
    public ChannelFuture close(final ChannelPromise promise) {
        return this.pipeline.close(promise);
    }
    
    @Override
    public ChannelFuture deregister(final ChannelPromise promise) {
        this.eventLoop.rejectNewTasks();
        return this.pipeline.deregister(promise);
    }
    
    @Override
    public Channel read() {
        this.pipeline.read();
        return this;
    }
    
    @Override
    public ChannelFuture write(final Object msg) {
        return this.pipeline.write(msg);
    }
    
    @Override
    public ChannelFuture write(final Object msg, final ChannelPromise promise) {
        return this.pipeline.write(msg, promise);
    }
    
    @Override
    public ChannelFuture writeAndFlush(final Object msg) {
        return this.pipeline.writeAndFlush(msg);
    }
    
    @Override
    public ChannelFuture writeAndFlush(final Object msg, final ChannelPromise promise) {
        return this.pipeline.writeAndFlush(msg, promise);
    }
    
    @Override
    public ChannelPromise newPromise() {
        return new DefaultChannelPromise(this);
    }
    
    @Override
    public ChannelProgressivePromise newProgressivePromise() {
        return new DefaultChannelProgressivePromise(this);
    }
    
    @Override
    public ChannelFuture newSucceededFuture() {
        return this.succeededFuture;
    }
    
    @Override
    public ChannelFuture newFailedFuture(final Throwable cause) {
        return new FailedChannelFuture(this, null, cause);
    }
    
    @Override
    public ChannelFuture closeFuture() {
        return this.closeFuture;
    }
    
    @Override
    public Unsafe unsafe() {
        return this.unsafe;
    }
    
    protected abstract AbstractUnsafe newUnsafe();
    
    @Override
    public final int hashCode() {
        return this.id.hashCode();
    }
    
    @Override
    public final boolean equals(final Object o) {
        return this == o;
    }
    
    @Override
    public final int compareTo(final Channel o) {
        if (this == o) {
            return 0;
        }
        return this.id().compareTo(o.id());
    }
    
    @Override
    public String toString() {
        final boolean active = this.isActive();
        if (this.strValActive == active && this.strVal != null) {
            return this.strVal;
        }
        final SocketAddress remoteAddr = this.remoteAddress();
        final SocketAddress localAddr = this.localAddress();
        if (remoteAddr != null) {
            SocketAddress srcAddr;
            SocketAddress dstAddr;
            if (this.parent == null) {
                srcAddr = localAddr;
                dstAddr = remoteAddr;
            }
            else {
                srcAddr = remoteAddr;
                dstAddr = localAddr;
            }
            final StringBuilder buf = new StringBuilder(96).append("[id: 0x").append(this.id.asShortText()).append(", ").append(srcAddr).append(active ? " => " : " :> ").append(dstAddr).append(']');
            this.strVal = buf.toString();
        }
        else if (localAddr != null) {
            final StringBuilder buf2 = new StringBuilder(64).append("[id: 0x").append(this.id.asShortText()).append(", ").append(localAddr).append(']');
            this.strVal = buf2.toString();
        }
        else {
            final StringBuilder buf2 = new StringBuilder(16).append("[id: 0x").append(this.id.asShortText()).append(']');
            this.strVal = buf2.toString();
        }
        this.strValActive = active;
        return this.strVal;
    }
    
    @Override
    public final ChannelPromise voidPromise() {
        return this.voidPromise;
    }
    
    final MessageSizeEstimator.Handle estimatorHandle() {
        if (this.estimatorHandle == null) {
            this.estimatorHandle = this.config().getMessageSizeEstimator().newHandle();
        }
        return this.estimatorHandle;
    }
    
    protected abstract boolean isCompatible(final EventLoop p0);
    
    protected abstract SocketAddress localAddress0();
    
    protected abstract SocketAddress remoteAddress0();
    
    protected void doRegister() throws Exception {
    }
    
    protected abstract void doBind(final SocketAddress p0) throws Exception;
    
    protected abstract void doDisconnect() throws Exception;
    
    protected abstract void doClose() throws Exception;
    
    protected void doDeregister() throws Exception {
    }
    
    protected abstract void doBeginRead() throws Exception;
    
    protected abstract void doWrite(final ChannelOutboundBuffer p0) throws Exception;
    
    protected Object filterOutboundMessage(final Object msg) throws Exception {
        return msg;
    }
    
    static {
        logger = InternalLoggerFactory.getInstance(AbstractChannel.class);
        CLOSED_CHANNEL_EXCEPTION = new ClosedChannelException();
        NOT_YET_CONNECTED_EXCEPTION = new NotYetConnectedException();
        AbstractChannel.CLOSED_CHANNEL_EXCEPTION.setStackTrace(EmptyArrays.EMPTY_STACK_TRACE);
        AbstractChannel.NOT_YET_CONNECTED_EXCEPTION.setStackTrace(EmptyArrays.EMPTY_STACK_TRACE);
    }
    
    protected abstract class AbstractUnsafe implements Unsafe
    {
        private ChannelOutboundBuffer outboundBuffer;
        private RecvByteBufAllocator.Handle recvHandle;
        private boolean inFlush0;
        private boolean neverRegistered;
        
        protected AbstractUnsafe() {
            this.outboundBuffer = new ChannelOutboundBuffer(AbstractChannel.this);
            this.neverRegistered = true;
        }
        
        @Override
        public RecvByteBufAllocator.Handle recvBufAllocHandle() {
            if (this.recvHandle == null) {
                this.recvHandle = AbstractChannel.this.config().getRecvByteBufAllocator().newHandle();
            }
            return this.recvHandle;
        }
        
        @Override
        public final ChannelHandlerInvoker invoker() {
            return ((PausableChannelEventExecutor)AbstractChannel.this.eventLoop().asInvoker()).unwrapInvoker();
        }
        
        @Override
        public final ChannelOutboundBuffer outboundBuffer() {
            return this.outboundBuffer;
        }
        
        @Override
        public final SocketAddress localAddress() {
            return AbstractChannel.this.localAddress0();
        }
        
        @Override
        public final SocketAddress remoteAddress() {
            return AbstractChannel.this.remoteAddress0();
        }
        
        @Override
        public final void register(final EventLoop eventLoop, final ChannelPromise promise) {
            if (eventLoop == null) {
                throw new NullPointerException("eventLoop");
            }
            if (promise == null) {
                throw new NullPointerException("promise");
            }
            if (AbstractChannel.this.isRegistered()) {
                promise.setFailure((Throwable)new IllegalStateException("registered to an event loop already"));
                return;
            }
            if (!AbstractChannel.this.isCompatible(eventLoop)) {
                promise.setFailure((Throwable)new IllegalStateException("incompatible event loop type: " + eventLoop.getClass().getName()));
                return;
            }
            if (AbstractChannel.this.eventLoop == null) {
                AbstractChannel.this.eventLoop = new PausableChannelEventLoop(eventLoop);
            }
            else {
                AbstractChannel.this.eventLoop.unwrapped = eventLoop;
            }
            if (eventLoop.inEventLoop()) {
                this.register0(promise);
            }
            else {
                try {
                    eventLoop.execute(new OneTimeTask() {
                        @Override
                        public void run() {
                            AbstractUnsafe.this.register0(promise);
                        }
                    });
                }
                catch (Throwable t) {
                    AbstractChannel.logger.warn("Force-closing a channel whose registration task was not accepted by an event loop: {}", AbstractChannel.this, t);
                    this.closeForcibly();
                    AbstractChannel.this.closeFuture.setClosed();
                    this.safeSetFailure(promise, t);
                }
            }
        }
        
        private void register0(final ChannelPromise promise) {
            try {
                if (!promise.setUncancellable() || !this.ensureOpen(promise)) {
                    return;
                }
                final boolean firstRegistration = this.neverRegistered;
                AbstractChannel.this.doRegister();
                this.neverRegistered = false;
                AbstractChannel.this.registered = true;
                AbstractChannel.this.eventLoop.acceptNewTasks();
                this.safeSetSuccess(promise);
                AbstractChannel.this.pipeline.fireChannelRegistered();
                if (firstRegistration && AbstractChannel.this.isActive()) {
                    AbstractChannel.this.pipeline.fireChannelActive();
                }
            }
            catch (Throwable t) {
                this.closeForcibly();
                AbstractChannel.this.closeFuture.setClosed();
                this.safeSetFailure(promise, t);
            }
        }
        
        @Override
        public final void bind(final SocketAddress localAddress, final ChannelPromise promise) {
            if (!promise.setUncancellable() || !this.ensureOpen(promise)) {
                return;
            }
            if (Boolean.TRUE.equals(AbstractChannel.this.config().getOption(ChannelOption.SO_BROADCAST)) && localAddress instanceof InetSocketAddress && !((InetSocketAddress)localAddress).getAddress().isAnyLocalAddress() && !PlatformDependent.isWindows() && !PlatformDependent.isRoot()) {
                AbstractChannel.logger.warn("A non-root user can't receive a broadcast packet if the socket is not bound to a wildcard address; binding to a non-wildcard address (" + localAddress + ") anyway as requested.");
            }
            final boolean wasActive = AbstractChannel.this.isActive();
            try {
                AbstractChannel.this.doBind(localAddress);
            }
            catch (Throwable t) {
                this.safeSetFailure(promise, t);
                this.closeIfClosed();
                return;
            }
            if (!wasActive && AbstractChannel.this.isActive()) {
                this.invokeLater(new OneTimeTask() {
                    @Override
                    public void run() {
                        AbstractChannel.this.pipeline.fireChannelActive();
                    }
                });
            }
            this.safeSetSuccess(promise);
        }
        
        @Override
        public final void disconnect(final ChannelPromise promise) {
            if (!promise.setUncancellable()) {
                return;
            }
            final boolean wasActive = AbstractChannel.this.isActive();
            try {
                AbstractChannel.this.doDisconnect();
            }
            catch (Throwable t) {
                this.safeSetFailure(promise, t);
                this.closeIfClosed();
                return;
            }
            if (wasActive && !AbstractChannel.this.isActive()) {
                this.invokeLater(new OneTimeTask() {
                    @Override
                    public void run() {
                        AbstractChannel.this.pipeline.fireChannelInactive();
                    }
                });
            }
            this.safeSetSuccess(promise);
            this.closeIfClosed();
        }
        
        @Override
        public final void close(final ChannelPromise promise) {
            if (!promise.setUncancellable()) {
                return;
            }
            if (this.inFlush0) {
                this.invokeLater(new OneTimeTask() {
                    @Override
                    public void run() {
                        AbstractUnsafe.this.close(promise);
                    }
                });
                return;
            }
            if (this.outboundBuffer == null) {
                AbstractChannel.this.closeFuture.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(final ChannelFuture future) throws Exception {
                        promise.setSuccess();
                    }
                });
                return;
            }
            if (AbstractChannel.this.closeFuture.isDone()) {
                this.safeSetSuccess(promise);
                return;
            }
            final boolean wasActive = AbstractChannel.this.isActive();
            final ChannelOutboundBuffer buffer = this.outboundBuffer;
            this.outboundBuffer = null;
            final Executor closeExecutor = this.closeExecutor();
            if (closeExecutor != null) {
                closeExecutor.execute(new OneTimeTask() {
                    @Override
                    public void run() {
                        Throwable cause = null;
                        try {
                            AbstractChannel.this.doClose();
                        }
                        catch (Throwable t) {
                            cause = t;
                        }
                        final Throwable error = cause;
                        AbstractUnsafe.this.invokeLater(new OneTimeTask() {
                            @Override
                            public void run() {
                                AbstractUnsafe.this.closeAndDeregister(buffer, wasActive, promise, error);
                            }
                        });
                    }
                });
            }
            else {
                Throwable error = null;
                try {
                    AbstractChannel.this.doClose();
                }
                catch (Throwable t) {
                    error = t;
                }
                this.closeAndDeregister(buffer, wasActive, promise, error);
            }
        }
        
        private void closeAndDeregister(final ChannelOutboundBuffer outboundBuffer, final boolean wasActive, final ChannelPromise promise, final Throwable error) {
            try {
                outboundBuffer.failFlushed(AbstractChannel.CLOSED_CHANNEL_EXCEPTION);
                outboundBuffer.close(AbstractChannel.CLOSED_CHANNEL_EXCEPTION);
            }
            finally {
                if (wasActive && !AbstractChannel.this.isActive()) {
                    this.invokeLater(new OneTimeTask() {
                        @Override
                        public void run() {
                            AbstractChannel.this.pipeline.fireChannelInactive();
                            AbstractUnsafe.this.deregister(AbstractUnsafe.this.voidPromise());
                        }
                    });
                }
                else {
                    this.invokeLater(new OneTimeTask() {
                        @Override
                        public void run() {
                            AbstractUnsafe.this.deregister(AbstractUnsafe.this.voidPromise());
                        }
                    });
                }
                AbstractChannel.this.closeFuture.setClosed();
                if (error != null) {
                    this.safeSetFailure(promise, error);
                }
                else {
                    this.safeSetSuccess(promise);
                }
            }
        }
        
        @Override
        public final void closeForcibly() {
            try {
                AbstractChannel.this.doClose();
            }
            catch (Exception e) {
                AbstractChannel.logger.warn("Failed to close a channel.", e);
            }
        }
        
        @Override
        public final void deregister(final ChannelPromise promise) {
            if (!promise.setUncancellable()) {
                return;
            }
            if (!AbstractChannel.this.registered) {
                this.safeSetSuccess(promise);
                return;
            }
            try {
                AbstractChannel.this.doDeregister();
            }
            catch (Throwable t) {
                this.safeSetFailure(promise, t);
                AbstractChannel.logger.warn("Unexpected exception occurred while deregistering a channel.", t);
            }
            finally {
                if (AbstractChannel.this.registered) {
                    AbstractChannel.this.registered = false;
                    this.safeSetSuccess(promise);
                    AbstractChannel.this.pipeline.fireChannelUnregistered();
                }
                else {
                    this.safeSetSuccess(promise);
                }
            }
        }
        
        @Override
        public final void beginRead() {
            if (!AbstractChannel.this.isActive()) {
                return;
            }
            try {
                AbstractChannel.this.doBeginRead();
            }
            catch (Exception e) {
                this.invokeLater(new OneTimeTask() {
                    @Override
                    public void run() {
                        AbstractChannel.this.pipeline.fireExceptionCaught(e);
                    }
                });
                this.close(this.voidPromise());
            }
        }
        
        @Override
        public final void write(Object msg, final ChannelPromise promise) {
            final ChannelOutboundBuffer outboundBuffer = this.outboundBuffer;
            if (outboundBuffer == null) {
                this.safeSetFailure(promise, AbstractChannel.CLOSED_CHANNEL_EXCEPTION);
                ReferenceCountUtil.release(msg);
                return;
            }
            int size;
            try {
                msg = AbstractChannel.this.filterOutboundMessage(msg);
                size = AbstractChannel.this.estimatorHandle().size(msg);
                if (size < 0) {
                    size = 0;
                }
            }
            catch (Throwable t) {
                this.safeSetFailure(promise, t);
                ReferenceCountUtil.release(msg);
                return;
            }
            outboundBuffer.addMessage(msg, size, promise);
        }
        
        @Override
        public final void flush() {
            final ChannelOutboundBuffer outboundBuffer = this.outboundBuffer;
            if (outboundBuffer == null) {
                return;
            }
            outboundBuffer.addFlush();
            this.flush0();
        }
        
        protected void flush0() {
            if (this.inFlush0) {
                return;
            }
            final ChannelOutboundBuffer outboundBuffer = this.outboundBuffer;
            if (outboundBuffer == null || outboundBuffer.isEmpty()) {
                return;
            }
            this.inFlush0 = true;
            if (!AbstractChannel.this.isActive()) {
                try {
                    if (AbstractChannel.this.isOpen()) {
                        outboundBuffer.failFlushed(AbstractChannel.NOT_YET_CONNECTED_EXCEPTION);
                    }
                    else {
                        outboundBuffer.failFlushed(AbstractChannel.CLOSED_CHANNEL_EXCEPTION);
                    }
                }
                finally {
                    this.inFlush0 = false;
                }
                return;
            }
            try {
                AbstractChannel.this.doWrite(outboundBuffer);
            }
            catch (Throwable t) {
                outboundBuffer.failFlushed(t);
            }
            finally {
                this.inFlush0 = false;
            }
        }
        
        @Override
        public final ChannelPromise voidPromise() {
            return AbstractChannel.this.unsafeVoidPromise;
        }
        
        protected final boolean ensureOpen(final ChannelPromise promise) {
            if (AbstractChannel.this.isOpen()) {
                return true;
            }
            this.safeSetFailure(promise, AbstractChannel.CLOSED_CHANNEL_EXCEPTION);
            return false;
        }
        
        protected final void safeSetSuccess(final ChannelPromise promise) {
            if (!(promise instanceof VoidChannelPromise) && !promise.trySuccess()) {
                AbstractChannel.logger.warn("Failed to mark a promise as success because it is done already: {}", promise);
            }
        }
        
        protected final void safeSetFailure(final ChannelPromise promise, final Throwable cause) {
            if (!(promise instanceof VoidChannelPromise) && !promise.tryFailure(cause)) {
                AbstractChannel.logger.warn("Failed to mark a promise as failure because it's done already: {}", promise, cause);
            }
        }
        
        protected final void closeIfClosed() {
            if (AbstractChannel.this.isOpen()) {
                return;
            }
            this.close(this.voidPromise());
        }
        
        private void invokeLater(final Runnable task) {
            try {
                AbstractChannel.this.eventLoop().unwrap().execute(task);
            }
            catch (RejectedExecutionException e) {
                AbstractChannel.logger.warn("Can't invoke task later as EventLoop rejected it", e);
            }
        }
        
        protected final Throwable annotateConnectException(Throwable cause, final SocketAddress remoteAddress) {
            if (cause instanceof ConnectException) {
                final Throwable newT = new ConnectException(cause.getMessage() + ": " + remoteAddress);
                newT.setStackTrace(cause.getStackTrace());
                cause = newT;
            }
            else if (cause instanceof NoRouteToHostException) {
                final Throwable newT = new NoRouteToHostException(cause.getMessage() + ": " + remoteAddress);
                newT.setStackTrace(cause.getStackTrace());
                cause = newT;
            }
            else if (cause instanceof SocketException) {
                final Throwable newT = new SocketException(cause.getMessage() + ": " + remoteAddress);
                newT.setStackTrace(cause.getStackTrace());
                cause = newT;
            }
            return cause;
        }
        
        protected Executor closeExecutor() {
            return null;
        }
    }
    
    static final class CloseFuture extends DefaultChannelPromise
    {
        CloseFuture(final AbstractChannel ch) {
            super(ch);
        }
        
        @Override
        public ChannelPromise setSuccess() {
            throw new IllegalStateException();
        }
        
        @Override
        public ChannelPromise setFailure(final Throwable cause) {
            throw new IllegalStateException();
        }
        
        @Override
        public boolean trySuccess() {
            throw new IllegalStateException();
        }
        
        @Override
        public boolean tryFailure(final Throwable cause) {
            throw new IllegalStateException();
        }
        
        boolean setClosed() {
            return super.trySuccess();
        }
    }
    
    private final class PausableChannelEventLoop extends PausableChannelEventExecutor implements EventLoop
    {
        volatile boolean isAcceptingNewTasks;
        volatile EventLoop unwrapped;
        
        PausableChannelEventLoop(final EventLoop unwrapped) {
            this.isAcceptingNewTasks = true;
            this.unwrapped = unwrapped;
        }
        
        @Override
        public void rejectNewTasks() {
            this.isAcceptingNewTasks = false;
        }
        
        @Override
        public void acceptNewTasks() {
            this.isAcceptingNewTasks = true;
        }
        
        @Override
        public boolean isAcceptingNewTasks() {
            return this.isAcceptingNewTasks;
        }
        
        @Override
        public EventLoopGroup parent() {
            return this.unwrap().parent();
        }
        
        @Override
        public EventLoop next() {
            return this.unwrap().next();
        }
        
        @Override
        public EventLoop unwrap() {
            return this.unwrapped;
        }
        
        @Override
        public ChannelHandlerInvoker asInvoker() {
            return this;
        }
        
        @Override
        public ChannelFuture register(final Channel channel) {
            return this.unwrap().register(channel);
        }
        
        @Override
        public ChannelFuture register(final Channel channel, final ChannelPromise promise) {
            return this.unwrap().register(channel, promise);
        }
        
        @Override
        Channel channel() {
            return AbstractChannel.this;
        }
        
        @Override
        ChannelHandlerInvoker unwrapInvoker() {
            return this.unwrapped.asInvoker();
        }
    }
}
