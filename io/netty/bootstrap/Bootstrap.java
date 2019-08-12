// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.bootstrap;

import io.netty.resolver.DefaultNameResolverGroup;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.util.Iterator;
import io.netty.channel.ChannelPipeline;
import io.netty.util.AttributeKey;
import io.netty.channel.ChannelOption;
import java.util.Map;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.resolver.NameResolver;
import io.netty.channel.EventLoop;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Future;
import io.netty.channel.ChannelPromise;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.EventExecutor;
import io.netty.channel.ChannelFuture;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import io.netty.resolver.NameResolverGroup;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.channel.Channel;

public class Bootstrap extends AbstractBootstrap<Bootstrap, Channel>
{
    private static final InternalLogger logger;
    private static final NameResolverGroup<?> DEFAULT_RESOLVER;
    private volatile NameResolverGroup<SocketAddress> resolver;
    private volatile SocketAddress remoteAddress;
    
    public Bootstrap() {
        this.resolver = (NameResolverGroup<SocketAddress>)Bootstrap.DEFAULT_RESOLVER;
    }
    
    private Bootstrap(final Bootstrap bootstrap) {
        super(bootstrap);
        this.resolver = (NameResolverGroup<SocketAddress>)Bootstrap.DEFAULT_RESOLVER;
        this.resolver = bootstrap.resolver;
        this.remoteAddress = bootstrap.remoteAddress;
    }
    
    public Bootstrap resolver(final NameResolverGroup<?> resolver) {
        if (resolver == null) {
            throw new NullPointerException("resolver");
        }
        this.resolver = (NameResolverGroup<SocketAddress>)resolver;
        return this;
    }
    
    public Bootstrap remoteAddress(final SocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
        return this;
    }
    
    public Bootstrap remoteAddress(final String inetHost, final int inetPort) {
        this.remoteAddress = InetSocketAddress.createUnresolved(inetHost, inetPort);
        return this;
    }
    
    public Bootstrap remoteAddress(final InetAddress inetHost, final int inetPort) {
        this.remoteAddress = new InetSocketAddress(inetHost, inetPort);
        return this;
    }
    
    public ChannelFuture connect() {
        this.validate();
        final SocketAddress remoteAddress = this.remoteAddress;
        if (remoteAddress == null) {
            throw new IllegalStateException("remoteAddress not set");
        }
        return this.doResolveAndConnect(remoteAddress, this.localAddress());
    }
    
    public ChannelFuture connect(final String inetHost, final int inetPort) {
        return this.connect(InetSocketAddress.createUnresolved(inetHost, inetPort));
    }
    
    public ChannelFuture connect(final InetAddress inetHost, final int inetPort) {
        return this.connect(new InetSocketAddress(inetHost, inetPort));
    }
    
    public ChannelFuture connect(final SocketAddress remoteAddress) {
        if (remoteAddress == null) {
            throw new NullPointerException("remoteAddress");
        }
        this.validate();
        return this.doResolveAndConnect(remoteAddress, this.localAddress());
    }
    
    public ChannelFuture connect(final SocketAddress remoteAddress, final SocketAddress localAddress) {
        if (remoteAddress == null) {
            throw new NullPointerException("remoteAddress");
        }
        this.validate();
        return this.doResolveAndConnect(remoteAddress, localAddress);
    }
    
    private ChannelFuture doResolveAndConnect(final SocketAddress remoteAddress, final SocketAddress localAddress) {
        final ChannelFuture regFuture = this.initAndRegister();
        if (regFuture.cause() != null) {
            return regFuture;
        }
        final Channel channel = regFuture.channel();
        final EventLoop eventLoop = channel.eventLoop();
        final NameResolver<SocketAddress> resolver = this.resolver.getResolver(eventLoop);
        if (!resolver.isSupported(remoteAddress) || resolver.isResolved(remoteAddress)) {
            return doConnect(remoteAddress, localAddress, regFuture, channel.newPromise());
        }
        final Future<SocketAddress> resolveFuture = resolver.resolve(remoteAddress);
        final Throwable resolveFailureCause = resolveFuture.cause();
        if (resolveFailureCause != null) {
            channel.close();
            return channel.newFailedFuture(resolveFailureCause);
        }
        if (resolveFuture.isDone()) {
            return doConnect(resolveFuture.getNow(), localAddress, regFuture, channel.newPromise());
        }
        final ChannelPromise connectPromise = channel.newPromise();
        resolveFuture.addListener(new FutureListener<SocketAddress>() {
            @Override
            public void operationComplete(final Future<SocketAddress> future) throws Exception {
                if (future.cause() != null) {
                    channel.close();
                    connectPromise.setFailure(future.cause());
                }
                else {
                    doConnect(future.getNow(), localAddress, regFuture, connectPromise);
                }
            }
        });
        return connectPromise;
    }
    
    private static ChannelFuture doConnect(final SocketAddress remoteAddress, final SocketAddress localAddress, final ChannelFuture regFuture, final ChannelPromise connectPromise) {
        if (regFuture.isDone()) {
            doConnect0(remoteAddress, localAddress, regFuture, connectPromise);
        }
        else {
            regFuture.addListener((GenericFutureListener<? extends Future<? super Void>>)new ChannelFutureListener() {
                @Override
                public void operationComplete(final ChannelFuture future) throws Exception {
                    doConnect0(remoteAddress, localAddress, regFuture, connectPromise);
                }
            });
        }
        return connectPromise;
    }
    
    private static void doConnect0(final SocketAddress remoteAddress, final SocketAddress localAddress, final ChannelFuture regFuture, final ChannelPromise connectPromise) {
        final Channel channel = connectPromise.channel();
        channel.eventLoop().execute(new Runnable() {
            @Override
            public void run() {
                if (regFuture.isSuccess()) {
                    if (localAddress == null) {
                        channel.connect(remoteAddress, connectPromise);
                    }
                    else {
                        channel.connect(remoteAddress, localAddress, connectPromise);
                    }
                    connectPromise.addListener((GenericFutureListener<? extends Future<? super Void>>)ChannelFutureListener.CLOSE_ON_FAILURE);
                }
                else {
                    connectPromise.setFailure(regFuture.cause());
                }
            }
        });
    }
    
    @Override
    void init(final Channel channel) throws Exception {
        final ChannelPipeline p = channel.pipeline();
        p.addLast(this.handler());
        final Map<ChannelOption<?>, Object> options = this.options();
        synchronized (options) {
            for (final Map.Entry<ChannelOption<?>, Object> e : options.entrySet()) {
                try {
                    if (channel.config().setOption(e.getKey(), e.getValue())) {
                        continue;
                    }
                    Bootstrap.logger.warn("Unknown channel option: " + e);
                }
                catch (Throwable t) {
                    Bootstrap.logger.warn("Failed to set a channel option: " + channel, t);
                }
            }
        }
        final Map<AttributeKey<?>, Object> attrs = this.attrs();
        synchronized (attrs) {
            for (final Map.Entry<AttributeKey<?>, Object> e2 : attrs.entrySet()) {
                channel.attr(e2.getKey()).set(e2.getValue());
            }
        }
    }
    
    @Override
    public Bootstrap validate() {
        super.validate();
        if (this.handler() == null) {
            throw new IllegalStateException("handler not set");
        }
        return this;
    }
    
    @Override
    public Bootstrap clone() {
        return new Bootstrap(this);
    }
    
    @Override
    public String toString() {
        if (this.remoteAddress == null) {
            return super.toString();
        }
        final StringBuilder buf = new StringBuilder(super.toString());
        buf.setLength(buf.length() - 1);
        return buf.append(", remoteAddress: ").append(this.remoteAddress).append(')').toString();
    }
    
    static {
        logger = InternalLoggerFactory.getInstance(Bootstrap.class);
        DEFAULT_RESOLVER = DefaultNameResolverGroup.INSTANCE;
    }
}
