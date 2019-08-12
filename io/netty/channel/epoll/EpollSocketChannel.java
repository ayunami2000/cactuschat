// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.channel.epoll;

import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.channel.socket.SocketChannelConfig;
import io.netty.channel.AbstractChannel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.EventLoop;
import java.util.concurrent.Executor;
import io.netty.util.internal.OneTimeTask;
import io.netty.channel.ChannelPromise;
import io.netty.channel.ChannelFuture;
import java.net.SocketAddress;
import io.netty.channel.unix.FileDescriptor;
import io.netty.channel.Channel;
import java.net.InetSocketAddress;
import io.netty.channel.socket.SocketChannel;

public final class EpollSocketChannel extends AbstractEpollStreamChannel implements SocketChannel
{
    private final EpollSocketChannelConfig config;
    private volatile InetSocketAddress local;
    private volatile InetSocketAddress remote;
    
    EpollSocketChannel(final Channel parent, final int fd, final InetSocketAddress remote) {
        super(parent, fd);
        this.config = new EpollSocketChannelConfig(this);
        this.remote = remote;
        this.local = Native.localAddress(fd);
    }
    
    public EpollSocketChannel() {
        super(Native.socketStreamFd());
        this.config = new EpollSocketChannelConfig(this);
    }
    
    public EpollSocketChannel(final FileDescriptor fd) {
        super(fd);
        this.config = new EpollSocketChannelConfig(this);
        this.remote = Native.remoteAddress(fd.intValue());
        this.local = Native.localAddress(fd.intValue());
    }
    
    public EpollTcpInfo tcpInfo() {
        return this.tcpInfo(new EpollTcpInfo());
    }
    
    public EpollTcpInfo tcpInfo(final EpollTcpInfo info) {
        Native.tcpInfo(this.fd().intValue(), info);
        return info;
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
    protected SocketAddress localAddress0() {
        return this.local;
    }
    
    @Override
    protected SocketAddress remoteAddress0() {
        if (this.remote == null) {
            final InetSocketAddress address = Native.remoteAddress(this.fd().intValue());
            if (address != null) {
                this.remote = address;
            }
            return address;
        }
        return this.remote;
    }
    
    @Override
    protected void doBind(final SocketAddress local) throws Exception {
        final InetSocketAddress localAddress = (InetSocketAddress)local;
        final int fd = this.fd().intValue();
        Native.bind(fd, localAddress);
        this.local = Native.localAddress(fd);
    }
    
    @Override
    public EpollSocketChannelConfig config() {
        return this.config;
    }
    
    @Override
    public boolean isInputShutdown() {
        return this.isInputShutdown0();
    }
    
    @Override
    public boolean isOutputShutdown() {
        return this.isOutputShutdown0();
    }
    
    @Override
    public ChannelFuture shutdownOutput() {
        return this.shutdownOutput(this.newPromise());
    }
    
    @Override
    public ChannelFuture shutdownOutput(final ChannelPromise promise) {
        final Executor closeExecutor = ((EpollSocketChannelUnsafe)this.unsafe()).closeExecutor();
        if (closeExecutor != null) {
            closeExecutor.execute(new OneTimeTask() {
                @Override
                public void run() {
                    EpollSocketChannel.this.shutdownOutput0(promise);
                }
            });
        }
        else {
            final EventLoop loop = this.eventLoop();
            if (loop.inEventLoop()) {
                this.shutdownOutput0(promise);
            }
            else {
                loop.execute(new OneTimeTask() {
                    @Override
                    public void run() {
                        EpollSocketChannel.this.shutdownOutput0(promise);
                    }
                });
            }
        }
        return promise;
    }
    
    @Override
    public ServerSocketChannel parent() {
        return (ServerSocketChannel)super.parent();
    }
    
    @Override
    protected AbstractEpollUnsafe newUnsafe() {
        return new EpollSocketChannelUnsafe();
    }
    
    @Override
    protected boolean doConnect(final SocketAddress remoteAddress, final SocketAddress localAddress) throws Exception {
        if (localAddress != null) {
            AbstractEpollChannel.checkResolvable((InetSocketAddress)localAddress);
        }
        AbstractEpollChannel.checkResolvable((InetSocketAddress)remoteAddress);
        if (super.doConnect(remoteAddress, localAddress)) {
            final int fd = this.fd().intValue();
            this.local = Native.localAddress(fd);
            this.remote = (InetSocketAddress)remoteAddress;
            return true;
        }
        return false;
    }
    
    private final class EpollSocketChannelUnsafe extends EpollStreamUnsafe
    {
        @Override
        protected Executor closeExecutor() {
            if (EpollSocketChannel.this.config().getSoLinger() > 0) {
                return GlobalEventExecutor.INSTANCE;
            }
            return null;
        }
    }
}
