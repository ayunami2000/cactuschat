// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.channel;

import io.netty.util.concurrent.PausableEventExecutor;
import io.netty.util.internal.StringUtil;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.EventExecutor;
import io.netty.buffer.ByteBufAllocator;
import java.lang.annotation.Annotation;
import io.netty.util.internal.PlatformDependent;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.WeakHashMap;
import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.ResourceLeakHint;

abstract class AbstractChannelHandlerContext implements ChannelHandlerContext, ResourceLeakHint
{
    static final int MASK_HANDLER_ADDED = 1;
    static final int MASK_HANDLER_REMOVED = 2;
    private static final int MASK_EXCEPTION_CAUGHT = 4;
    private static final int MASK_CHANNEL_REGISTERED = 8;
    private static final int MASK_CHANNEL_UNREGISTERED = 16;
    private static final int MASK_CHANNEL_ACTIVE = 32;
    private static final int MASK_CHANNEL_INACTIVE = 64;
    private static final int MASK_CHANNEL_READ = 128;
    private static final int MASK_CHANNEL_READ_COMPLETE = 256;
    private static final int MASK_CHANNEL_WRITABILITY_CHANGED = 512;
    private static final int MASK_USER_EVENT_TRIGGERED = 1024;
    private static final int MASK_BIND = 2048;
    private static final int MASK_CONNECT = 4096;
    private static final int MASK_DISCONNECT = 8192;
    private static final int MASK_CLOSE = 16384;
    private static final int MASK_DEREGISTER = 32768;
    private static final int MASK_READ = 65536;
    private static final int MASK_WRITE = 131072;
    private static final int MASK_FLUSH = 262144;
    private static final int MASKGROUP_INBOUND = 2044;
    private static final int MASKGROUP_OUTBOUND = 522240;
    private static final FastThreadLocal<WeakHashMap<Class<?>, Integer>> skipFlagsCache;
    private static final AtomicReferenceFieldUpdater<AbstractChannelHandlerContext, PausableChannelEventExecutor> WRAPPED_EVENTEXECUTOR_UPDATER;
    volatile AbstractChannelHandlerContext next;
    volatile AbstractChannelHandlerContext prev;
    private final AbstractChannel channel;
    private final DefaultChannelPipeline pipeline;
    private final String name;
    boolean invokedThisChannelRead;
    private volatile boolean invokedNextChannelRead;
    private volatile boolean invokedPrevRead;
    private boolean removed;
    final int skipFlags;
    final ChannelHandlerInvoker invoker;
    private ChannelFuture succeededFuture;
    volatile Runnable invokeChannelReadCompleteTask;
    volatile Runnable invokeReadTask;
    volatile Runnable invokeFlushTask;
    volatile Runnable invokeChannelWritableStateChangedTask;
    private volatile PausableChannelEventExecutor wrappedEventLoop;
    
    static int skipFlags(final ChannelHandler handler) {
        final WeakHashMap<Class<?>, Integer> cache = AbstractChannelHandlerContext.skipFlagsCache.get();
        final Class<? extends ChannelHandler> handlerType = handler.getClass();
        final Integer flags = cache.get(handlerType);
        int flagsVal;
        if (flags != null) {
            flagsVal = flags;
        }
        else {
            flagsVal = skipFlags0(handlerType);
            cache.put(handlerType, flagsVal);
        }
        return flagsVal;
    }
    
    static int skipFlags0(final Class<? extends ChannelHandler> handlerType) {
        int flags = 0;
        try {
            if (isSkippable(handlerType, "handlerAdded", (Class<?>[])new Class[0])) {
                flags |= 0x1;
            }
            if (isSkippable(handlerType, "handlerRemoved", (Class<?>[])new Class[0])) {
                flags |= 0x2;
            }
            if (isSkippable(handlerType, "exceptionCaught", Throwable.class)) {
                flags |= 0x4;
            }
            if (isSkippable(handlerType, "channelRegistered", (Class<?>[])new Class[0])) {
                flags |= 0x8;
            }
            if (isSkippable(handlerType, "channelUnregistered", (Class<?>[])new Class[0])) {
                flags |= 0x10;
            }
            if (isSkippable(handlerType, "channelActive", (Class<?>[])new Class[0])) {
                flags |= 0x20;
            }
            if (isSkippable(handlerType, "channelInactive", (Class<?>[])new Class[0])) {
                flags |= 0x40;
            }
            if (isSkippable(handlerType, "channelRead", Object.class)) {
                flags |= 0x80;
            }
            if (isSkippable(handlerType, "channelReadComplete", (Class<?>[])new Class[0])) {
                flags |= 0x100;
            }
            if (isSkippable(handlerType, "channelWritabilityChanged", (Class<?>[])new Class[0])) {
                flags |= 0x200;
            }
            if (isSkippable(handlerType, "userEventTriggered", Object.class)) {
                flags |= 0x400;
            }
            if (isSkippable(handlerType, "bind", SocketAddress.class, ChannelPromise.class)) {
                flags |= 0x800;
            }
            if (isSkippable(handlerType, "connect", SocketAddress.class, SocketAddress.class, ChannelPromise.class)) {
                flags |= 0x1000;
            }
            if (isSkippable(handlerType, "disconnect", ChannelPromise.class)) {
                flags |= 0x2000;
            }
            if (isSkippable(handlerType, "close", ChannelPromise.class)) {
                flags |= 0x4000;
            }
            if (isSkippable(handlerType, "deregister", ChannelPromise.class)) {
                flags |= 0x8000;
            }
            if (isSkippable(handlerType, "read", (Class<?>[])new Class[0])) {
                flags |= 0x10000;
            }
            if (isSkippable(handlerType, "write", Object.class, ChannelPromise.class)) {
                flags |= 0x20000;
            }
            if (isSkippable(handlerType, "flush", (Class<?>[])new Class[0])) {
                flags |= 0x40000;
            }
        }
        catch (Exception e) {
            PlatformDependent.throwException(e);
        }
        return flags;
    }
    
    private static boolean isSkippable(final Class<?> handlerType, final String methodName, final Class<?>... paramTypes) throws Exception {
        final Class[] newParamTypes = new Class[paramTypes.length + 1];
        newParamTypes[0] = ChannelHandlerContext.class;
        System.arraycopy(paramTypes, 0, newParamTypes, 1, paramTypes.length);
        return handlerType.getMethod(methodName, (Class<?>[])newParamTypes).isAnnotationPresent(ChannelHandler.Skip.class);
    }
    
    AbstractChannelHandlerContext(final DefaultChannelPipeline pipeline, final ChannelHandlerInvoker invoker, final String name, final int skipFlags) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        this.channel = pipeline.channel;
        this.pipeline = pipeline;
        this.name = name;
        this.invoker = invoker;
        this.skipFlags = skipFlags;
    }
    
    @Override
    public final Channel channel() {
        return this.channel;
    }
    
    @Override
    public ChannelPipeline pipeline() {
        return this.pipeline;
    }
    
    @Override
    public ByteBufAllocator alloc() {
        return this.channel().config().getAllocator();
    }
    
    @Override
    public final EventExecutor executor() {
        if (this.invoker == null) {
            return this.channel().eventLoop();
        }
        return this.wrappedEventLoop();
    }
    
    @Override
    public final ChannelHandlerInvoker invoker() {
        if (this.invoker == null) {
            return this.channel().eventLoop().asInvoker();
        }
        return this.wrappedEventLoop();
    }
    
    private PausableChannelEventExecutor wrappedEventLoop() {
        PausableChannelEventExecutor wrapped = this.wrappedEventLoop;
        if (wrapped == null) {
            wrapped = new PausableChannelEventExecutor0();
            if (!AbstractChannelHandlerContext.WRAPPED_EVENTEXECUTOR_UPDATER.compareAndSet(this, null, wrapped)) {
                return this.wrappedEventLoop;
            }
        }
        return wrapped;
    }
    
    @Override
    public String name() {
        return this.name;
    }
    
    @Override
    public <T> Attribute<T> attr(final AttributeKey<T> key) {
        return this.channel.attr(key);
    }
    
    @Override
    public <T> boolean hasAttr(final AttributeKey<T> key) {
        return this.channel.hasAttr(key);
    }
    
    @Override
    public ChannelHandlerContext fireChannelRegistered() {
        final AbstractChannelHandlerContext next = this.findContextInbound();
        next.invoker().invokeChannelRegistered(next);
        return this;
    }
    
    @Override
    public ChannelHandlerContext fireChannelUnregistered() {
        final AbstractChannelHandlerContext next = this.findContextInbound();
        next.invoker().invokeChannelUnregistered(next);
        return this;
    }
    
    @Override
    public ChannelHandlerContext fireChannelActive() {
        final AbstractChannelHandlerContext next = this.findContextInbound();
        next.invoker().invokeChannelActive(next);
        return this;
    }
    
    @Override
    public ChannelHandlerContext fireChannelInactive() {
        final AbstractChannelHandlerContext next = this.findContextInbound();
        next.invoker().invokeChannelInactive(next);
        return this;
    }
    
    @Override
    public ChannelHandlerContext fireExceptionCaught(final Throwable cause) {
        final AbstractChannelHandlerContext next = this.findContextInbound();
        next.invoker().invokeExceptionCaught(next, cause);
        return this;
    }
    
    @Override
    public ChannelHandlerContext fireUserEventTriggered(final Object event) {
        final AbstractChannelHandlerContext next = this.findContextInbound();
        next.invoker().invokeUserEventTriggered(next, event);
        return this;
    }
    
    @Override
    public ChannelHandlerContext fireChannelRead(final Object msg) {
        final AbstractChannelHandlerContext next = this.findContextInbound();
        ReferenceCountUtil.touch(msg, next);
        this.invokedNextChannelRead = true;
        next.invoker().invokeChannelRead(next, msg);
        return this;
    }
    
    @Override
    public ChannelHandlerContext fireChannelReadComplete() {
        if (this.invokedNextChannelRead || !this.invokedThisChannelRead) {
            this.invokedNextChannelRead = false;
            this.invokedPrevRead = false;
            final AbstractChannelHandlerContext next = this.findContextInbound();
            next.invoker().invokeChannelReadComplete(next);
            return this;
        }
        if (this.invokedPrevRead && !this.channel().config().isAutoRead()) {
            this.read();
        }
        else {
            this.invokedPrevRead = false;
        }
        return this;
    }
    
    @Override
    public ChannelHandlerContext fireChannelWritabilityChanged() {
        final AbstractChannelHandlerContext next = this.findContextInbound();
        next.invoker().invokeChannelWritabilityChanged(next);
        return this;
    }
    
    @Override
    public ChannelFuture bind(final SocketAddress localAddress) {
        return this.bind(localAddress, this.newPromise());
    }
    
    @Override
    public ChannelFuture connect(final SocketAddress remoteAddress) {
        return this.connect(remoteAddress, this.newPromise());
    }
    
    @Override
    public ChannelFuture connect(final SocketAddress remoteAddress, final SocketAddress localAddress) {
        return this.connect(remoteAddress, localAddress, this.newPromise());
    }
    
    @Override
    public ChannelFuture disconnect() {
        return this.disconnect(this.newPromise());
    }
    
    @Override
    public ChannelFuture close() {
        return this.close(this.newPromise());
    }
    
    @Override
    public ChannelFuture deregister() {
        return this.deregister(this.newPromise());
    }
    
    @Override
    public ChannelFuture bind(final SocketAddress localAddress, final ChannelPromise promise) {
        final AbstractChannelHandlerContext next = this.findContextOutbound();
        next.invoker().invokeBind(next, localAddress, promise);
        return promise;
    }
    
    @Override
    public ChannelFuture connect(final SocketAddress remoteAddress, final ChannelPromise promise) {
        return this.connect(remoteAddress, null, promise);
    }
    
    @Override
    public ChannelFuture connect(final SocketAddress remoteAddress, final SocketAddress localAddress, final ChannelPromise promise) {
        final AbstractChannelHandlerContext next = this.findContextOutbound();
        next.invoker().invokeConnect(next, remoteAddress, localAddress, promise);
        return promise;
    }
    
    @Override
    public ChannelFuture disconnect(final ChannelPromise promise) {
        if (!this.channel().metadata().hasDisconnect()) {
            return this.close(promise);
        }
        final AbstractChannelHandlerContext next = this.findContextOutbound();
        next.invoker().invokeDisconnect(next, promise);
        return promise;
    }
    
    @Override
    public ChannelFuture close(final ChannelPromise promise) {
        final AbstractChannelHandlerContext next = this.findContextOutbound();
        next.invoker().invokeClose(next, promise);
        return promise;
    }
    
    @Override
    public ChannelFuture deregister(final ChannelPromise promise) {
        final AbstractChannelHandlerContext next = this.findContextOutbound();
        next.invoker().invokeDeregister(next, promise);
        return promise;
    }
    
    @Override
    public ChannelHandlerContext read() {
        final AbstractChannelHandlerContext next = this.findContextOutbound();
        this.invokedPrevRead = true;
        next.invoker().invokeRead(next);
        return this;
    }
    
    @Override
    public ChannelFuture write(final Object msg) {
        return this.write(msg, this.newPromise());
    }
    
    @Override
    public ChannelFuture write(final Object msg, final ChannelPromise promise) {
        final AbstractChannelHandlerContext next = this.findContextOutbound();
        ReferenceCountUtil.touch(msg, next);
        next.invoker().invokeWrite(next, msg, promise);
        return promise;
    }
    
    @Override
    public ChannelHandlerContext flush() {
        final AbstractChannelHandlerContext next = this.findContextOutbound();
        next.invoker().invokeFlush(next);
        return this;
    }
    
    @Override
    public ChannelFuture writeAndFlush(final Object msg, final ChannelPromise promise) {
        AbstractChannelHandlerContext next = this.findContextOutbound();
        ReferenceCountUtil.touch(msg, next);
        next.invoker().invokeWrite(next, msg, promise);
        next = this.findContextOutbound();
        next.invoker().invokeFlush(next);
        return promise;
    }
    
    @Override
    public ChannelFuture writeAndFlush(final Object msg) {
        return this.writeAndFlush(msg, this.newPromise());
    }
    
    @Override
    public ChannelPromise newPromise() {
        return new DefaultChannelPromise(this.channel(), this.executor());
    }
    
    @Override
    public ChannelProgressivePromise newProgressivePromise() {
        return new DefaultChannelProgressivePromise(this.channel(), this.executor());
    }
    
    @Override
    public ChannelFuture newSucceededFuture() {
        ChannelFuture succeededFuture = this.succeededFuture;
        if (succeededFuture == null) {
            succeededFuture = (this.succeededFuture = new SucceededChannelFuture(this.channel(), this.executor()));
        }
        return succeededFuture;
    }
    
    @Override
    public ChannelFuture newFailedFuture(final Throwable cause) {
        return new FailedChannelFuture(this.channel(), this.executor(), cause);
    }
    
    private AbstractChannelHandlerContext findContextInbound() {
        AbstractChannelHandlerContext ctx = this;
        do {
            ctx = ctx.next;
        } while ((ctx.skipFlags & 0x7FC) == 0x7FC);
        return ctx;
    }
    
    private AbstractChannelHandlerContext findContextOutbound() {
        AbstractChannelHandlerContext ctx = this;
        do {
            ctx = ctx.prev;
        } while ((ctx.skipFlags & 0x7F800) == 0x7F800);
        return ctx;
    }
    
    @Override
    public ChannelPromise voidPromise() {
        return this.channel.voidPromise();
    }
    
    void setRemoved() {
        this.removed = true;
    }
    
    @Override
    public boolean isRemoved() {
        return this.removed;
    }
    
    @Override
    public String toHintString() {
        return '\'' + this.name + "' will handle the message from this point.";
    }
    
    @Override
    public String toString() {
        return StringUtil.simpleClassName(ChannelHandlerContext.class) + '(' + this.name + ", " + this.channel + ')';
    }
    
    static {
        skipFlagsCache = new FastThreadLocal<WeakHashMap<Class<?>, Integer>>() {
            @Override
            protected WeakHashMap<Class<?>, Integer> initialValue() throws Exception {
                return new WeakHashMap<Class<?>, Integer>();
            }
        };
        AtomicReferenceFieldUpdater<AbstractChannelHandlerContext, PausableChannelEventExecutor> updater = PlatformDependent.newAtomicReferenceFieldUpdater(AbstractChannelHandlerContext.class, "wrappedEventLoop");
        if (updater == null) {
            updater = AtomicReferenceFieldUpdater.newUpdater(AbstractChannelHandlerContext.class, PausableChannelEventExecutor.class, "wrappedEventLoop");
        }
        WRAPPED_EVENTEXECUTOR_UPDATER = updater;
    }
    
    private final class PausableChannelEventExecutor0 extends PausableChannelEventExecutor
    {
        @Override
        public void rejectNewTasks() {
            ((PausableEventExecutor)this.channel().eventLoop()).rejectNewTasks();
        }
        
        @Override
        public void acceptNewTasks() {
            ((PausableEventExecutor)this.channel().eventLoop()).acceptNewTasks();
        }
        
        @Override
        public boolean isAcceptingNewTasks() {
            return ((PausableEventExecutor)this.channel().eventLoop()).isAcceptingNewTasks();
        }
        
        public Channel channel() {
            return AbstractChannelHandlerContext.this.channel();
        }
        
        @Override
        public EventExecutor unwrap() {
            return this.unwrapInvoker().executor();
        }
        
        public ChannelHandlerInvoker unwrapInvoker() {
            return AbstractChannelHandlerContext.this.invoker;
        }
    }
}
