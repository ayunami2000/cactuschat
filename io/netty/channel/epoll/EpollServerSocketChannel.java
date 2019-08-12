// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.channel.epoll;

import io.netty.channel.socket.ServerSocketChannelConfig;
import io.netty.channel.ChannelConfig;
import io.netty.channel.Channel;
import java.net.SocketAddress;
import io.netty.channel.EventLoop;
import io.netty.channel.unix.FileDescriptor;
import java.net.InetSocketAddress;
import io.netty.channel.socket.ServerSocketChannel;

public final class EpollServerSocketChannel extends AbstractEpollServerChannel implements ServerSocketChannel
{
    private final EpollServerSocketChannelConfig config;
    private volatile InetSocketAddress local;
    
    public EpollServerSocketChannel() {
        super(Native.socketStreamFd());
        this.config = new EpollServerSocketChannelConfig(this);
    }
    
    public EpollServerSocketChannel(final FileDescriptor fd) {
        super(fd);
        this.config = new EpollServerSocketChannelConfig(this);
        this.local = Native.localAddress(fd.intValue());
    }
    
    @Override
    protected boolean isCompatible(final EventLoop loop) {
        return loop instanceof EpollEventLoop;
    }
    
    @Override
    protected void doBind(final SocketAddress localAddress) throws Exception {
        final InetSocketAddress addr = (InetSocketAddress)localAddress;
        AbstractEpollChannel.checkResolvable(addr);
        final int fd = this.fd().intValue();
        Native.bind(fd, addr);
        this.local = Native.localAddress(fd);
        Native.listen(fd, this.config.getBacklog());
        this.active = true;
    }
    
    @Override
    public InetSocketAddress remoteAddress() {
        return (InetSocketAddress)super.remoteAddress();
    }
    
    @Override
    public InetSocketAddress localAddress() {
        return (InetSocketAddress)super.localAddress();
    }
    
    @Override
    public EpollServerSocketChannelConfig config() {
        return this.config;
    }
    
    @Override
    protected InetSocketAddress localAddress0() {
        return this.local;
    }
    
    protected Channel newChildChannel(final int fd, final byte[] address, final int offset, final int len) throws Exception {
        return new EpollSocketChannel(this, fd, Native.address(address, offset, len));
    }
}
