// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.channel.epoll;

import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.channel.ChannelConfig;
import java.io.File;
import java.net.SocketAddress;
import io.netty.channel.Channel;
import io.netty.channel.unix.FileDescriptor;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.channel.unix.ServerDomainSocketChannel;

public final class EpollServerDomainSocketChannel extends AbstractEpollServerChannel implements ServerDomainSocketChannel
{
    private static final InternalLogger logger;
    private final EpollServerChannelConfig config;
    private volatile DomainSocketAddress local;
    
    public EpollServerDomainSocketChannel() {
        super(Native.socketDomainFd());
        this.config = new EpollServerChannelConfig(this);
    }
    
    public EpollServerDomainSocketChannel(final FileDescriptor fd) {
        super(fd);
        this.config = new EpollServerChannelConfig(this);
    }
    
    protected Channel newChildChannel(final int fd, final byte[] addr, final int offset, final int len) throws Exception {
        return new EpollDomainSocketChannel(this, fd);
    }
    
    @Override
    protected DomainSocketAddress localAddress0() {
        return this.local;
    }
    
    @Override
    protected void doBind(final SocketAddress localAddress) throws Exception {
        final int fd = this.fd().intValue();
        Native.bind(fd, localAddress);
        Native.listen(fd, this.config.getBacklog());
        this.local = (DomainSocketAddress)localAddress;
    }
    
    @Override
    protected void doClose() throws Exception {
        try {
            super.doClose();
        }
        finally {
            final DomainSocketAddress local = this.local;
            if (local != null) {
                final File socketFile = new File(local.path());
                final boolean success = socketFile.delete();
                if (!success && EpollServerDomainSocketChannel.logger.isDebugEnabled()) {
                    EpollServerDomainSocketChannel.logger.debug("Failed to delete a domain socket file: {}", local.path());
                }
            }
        }
    }
    
    @Override
    public EpollServerChannelConfig config() {
        return this.config;
    }
    
    @Override
    public DomainSocketAddress remoteAddress() {
        return (DomainSocketAddress)super.remoteAddress();
    }
    
    @Override
    public DomainSocketAddress localAddress() {
        return (DomainSocketAddress)super.localAddress();
    }
    
    static {
        logger = InternalLoggerFactory.getInstance(EpollServerDomainSocketChannel.class);
    }
}
