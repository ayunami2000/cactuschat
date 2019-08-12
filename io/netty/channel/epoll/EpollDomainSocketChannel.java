// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.channel.epoll;

import io.netty.channel.ChannelPipeline;
import io.netty.channel.unix.DomainSocketChannelConfig;
import io.netty.channel.AbstractChannel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelOutboundBuffer;
import java.net.SocketAddress;
import io.netty.channel.unix.FileDescriptor;
import io.netty.channel.Channel;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.channel.unix.DomainSocketChannel;

public final class EpollDomainSocketChannel extends AbstractEpollStreamChannel implements DomainSocketChannel
{
    private final EpollDomainSocketChannelConfig config;
    private volatile DomainSocketAddress local;
    private volatile DomainSocketAddress remote;
    
    public EpollDomainSocketChannel() {
        super(Native.socketDomainFd());
        this.config = new EpollDomainSocketChannelConfig(this);
    }
    
    public EpollDomainSocketChannel(final Channel parent, final FileDescriptor fd) {
        super(parent, fd.intValue());
        this.config = new EpollDomainSocketChannelConfig(this);
    }
    
    public EpollDomainSocketChannel(final FileDescriptor fd) {
        super(fd);
        this.config = new EpollDomainSocketChannelConfig(this);
    }
    
    EpollDomainSocketChannel(final Channel parent, final int fd) {
        super(parent, fd);
        this.config = new EpollDomainSocketChannelConfig(this);
    }
    
    @Override
    protected AbstractEpollUnsafe newUnsafe() {
        return new EpollDomainUnsafe();
    }
    
    @Override
    protected DomainSocketAddress localAddress0() {
        return this.local;
    }
    
    @Override
    protected DomainSocketAddress remoteAddress0() {
        return this.remote;
    }
    
    @Override
    protected void doBind(final SocketAddress localAddress) throws Exception {
        Native.bind(this.fd().intValue(), localAddress);
        this.local = (DomainSocketAddress)localAddress;
    }
    
    @Override
    public EpollDomainSocketChannelConfig config() {
        return this.config;
    }
    
    @Override
    protected boolean doConnect(final SocketAddress remoteAddress, final SocketAddress localAddress) throws Exception {
        if (super.doConnect(remoteAddress, localAddress)) {
            this.local = (DomainSocketAddress)localAddress;
            this.remote = (DomainSocketAddress)remoteAddress;
            return true;
        }
        return false;
    }
    
    @Override
    public DomainSocketAddress remoteAddress() {
        return (DomainSocketAddress)super.remoteAddress();
    }
    
    @Override
    public DomainSocketAddress localAddress() {
        return (DomainSocketAddress)super.localAddress();
    }
    
    @Override
    protected boolean doWriteSingle(final ChannelOutboundBuffer in, final int writeSpinCount) throws Exception {
        final Object msg = in.current();
        if (msg instanceof FileDescriptor && Native.sendFd(this.fd().intValue(), ((FileDescriptor)msg).intValue()) > 0) {
            in.remove();
            return true;
        }
        return super.doWriteSingle(in, writeSpinCount);
    }
    
    @Override
    protected Object filterOutboundMessage(final Object msg) {
        if (msg instanceof FileDescriptor) {
            return msg;
        }
        return super.filterOutboundMessage(msg);
    }
    
    private final class EpollDomainUnsafe extends EpollStreamUnsafe
    {
        @Override
        void epollInReady() {
            switch (EpollDomainSocketChannel.this.config().getReadMode()) {
                case BYTES: {
                    super.epollInReady();
                    break;
                }
                case FILE_DESCRIPTORS: {
                    this.epollInReadFd();
                    break;
                }
                default: {
                    throw new Error();
                }
            }
        }
        
        private void epollInReadFd() {
            final boolean edgeTriggered = EpollDomainSocketChannel.this.isFlagSet(Native.EPOLLET);
            final ChannelConfig config = EpollDomainSocketChannel.this.config();
            if (!this.readPending && !edgeTriggered && !config.isAutoRead()) {
                this.clearEpollIn0();
                return;
            }
            final ChannelPipeline pipeline = EpollDomainSocketChannel.this.pipeline();
            try {
                final int maxMessagesPerRead = edgeTriggered ? Integer.MAX_VALUE : config.getMaxMessagesPerRead();
                int messages = 0;
                do {
                    final int socketFd = Native.recvFd(EpollDomainSocketChannel.this.fd().intValue());
                    if (socketFd == 0) {
                        break;
                    }
                    if (socketFd == -1) {
                        this.close(this.voidPromise());
                        return;
                    }
                    this.readPending = false;
                    try {
                        pipeline.fireChannelRead(new FileDescriptor(socketFd));
                    }
                    catch (Throwable t) {
                        pipeline.fireChannelReadComplete();
                        pipeline.fireExceptionCaught(t);
                    }
                    finally {
                        if (!edgeTriggered && !config.isAutoRead()) {
                            break;
                        }
                    }
                } while (++messages < maxMessagesPerRead);
                pipeline.fireChannelReadComplete();
            }
            catch (Throwable t2) {
                pipeline.fireChannelReadComplete();
                pipeline.fireExceptionCaught(t2);
                EpollDomainSocketChannel.this.eventLoop().execute(new Runnable() {
                    @Override
                    public void run() {
                        EpollDomainUnsafe.this.epollInReady();
                    }
                });
            }
            finally {
                if (!this.readPending && !config.isAutoRead()) {
                    this.clearEpollIn0();
                }
            }
        }
    }
}
