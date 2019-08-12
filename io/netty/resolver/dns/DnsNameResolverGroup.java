// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.resolver.dns;

import io.netty.util.internal.StringUtil;
import io.netty.channel.EventLoop;
import io.netty.resolver.NameResolver;
import io.netty.util.concurrent.EventExecutor;
import io.netty.channel.ReflectiveChannelFactory;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.ChannelFactory;
import java.net.InetSocketAddress;
import io.netty.resolver.NameResolverGroup;

public final class DnsNameResolverGroup extends NameResolverGroup<InetSocketAddress>
{
    private final ChannelFactory<? extends DatagramChannel> channelFactory;
    private final InetSocketAddress localAddress;
    private final Iterable<InetSocketAddress> nameServerAddresses;
    
    public DnsNameResolverGroup(final Class<? extends DatagramChannel> channelType, final InetSocketAddress nameServerAddress) {
        this(channelType, DnsNameResolver.ANY_LOCAL_ADDR, nameServerAddress);
    }
    
    public DnsNameResolverGroup(final Class<? extends DatagramChannel> channelType, final InetSocketAddress localAddress, final InetSocketAddress nameServerAddress) {
        this(new ReflectiveChannelFactory<DatagramChannel>(channelType), localAddress, nameServerAddress);
    }
    
    public DnsNameResolverGroup(final ChannelFactory<? extends DatagramChannel> channelFactory, final InetSocketAddress nameServerAddress) {
        this(channelFactory, DnsNameResolver.ANY_LOCAL_ADDR, nameServerAddress);
    }
    
    public DnsNameResolverGroup(final ChannelFactory<? extends DatagramChannel> channelFactory, final InetSocketAddress localAddress, final InetSocketAddress nameServerAddress) {
        this(channelFactory, localAddress, DnsServerAddresses.singleton(nameServerAddress));
    }
    
    public DnsNameResolverGroup(final Class<? extends DatagramChannel> channelType, final Iterable<InetSocketAddress> nameServerAddresses) {
        this(channelType, DnsNameResolver.ANY_LOCAL_ADDR, nameServerAddresses);
    }
    
    public DnsNameResolverGroup(final Class<? extends DatagramChannel> channelType, final InetSocketAddress localAddress, final Iterable<InetSocketAddress> nameServerAddresses) {
        this(new ReflectiveChannelFactory<DatagramChannel>(channelType), localAddress, nameServerAddresses);
    }
    
    public DnsNameResolverGroup(final ChannelFactory<? extends DatagramChannel> channelFactory, final Iterable<InetSocketAddress> nameServerAddresses) {
        this(channelFactory, DnsNameResolver.ANY_LOCAL_ADDR, nameServerAddresses);
    }
    
    public DnsNameResolverGroup(final ChannelFactory<? extends DatagramChannel> channelFactory, final InetSocketAddress localAddress, final Iterable<InetSocketAddress> nameServerAddresses) {
        this.channelFactory = channelFactory;
        this.localAddress = localAddress;
        this.nameServerAddresses = nameServerAddresses;
    }
    
    @Override
    protected NameResolver<InetSocketAddress> newResolver(final EventExecutor executor) throws Exception {
        if (!(executor instanceof EventLoop)) {
            throw new IllegalStateException("unsupported executor type: " + StringUtil.simpleClassName(executor) + " (expected: " + StringUtil.simpleClassName(EventLoop.class));
        }
        return new DnsNameResolver((EventLoop)executor, this.channelFactory, this.localAddress, this.nameServerAddresses);
    }
}
