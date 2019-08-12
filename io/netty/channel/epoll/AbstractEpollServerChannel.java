// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.channel.epoll;

import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelPromise;
import io.netty.channel.AbstractChannel;
import java.net.SocketAddress;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelOutboundBuffer;
import java.net.InetSocketAddress;
import io.netty.channel.EventLoop;
import io.netty.channel.Channel;
import io.netty.channel.unix.FileDescriptor;
import io.netty.channel.ServerChannel;

public abstract class AbstractEpollServerChannel extends AbstractEpollChannel implements ServerChannel
{
    protected AbstractEpollServerChannel(final int fd) {
        super(fd, Native.EPOLLIN);
    }
    
    protected AbstractEpollServerChannel(final FileDescriptor fd) {
        super(null, fd, Native.EPOLLIN, Native.getSoError(fd.intValue()) == 0);
    }
    
    @Override
    protected boolean isCompatible(final EventLoop loop) {
        return loop instanceof EpollEventLoop;
    }
    
    @Override
    protected InetSocketAddress remoteAddress0() {
        return null;
    }
    
    @Override
    protected AbstractEpollUnsafe newUnsafe() {
        return new EpollServerSocketUnsafe();
    }
    
    @Override
    protected void doWrite(final ChannelOutboundBuffer in) throws Exception {
        throw new UnsupportedOperationException();
    }
    
    @Override
    protected Object filterOutboundMessage(final Object msg) throws Exception {
        throw new UnsupportedOperationException();
    }
    
    abstract Channel newChildChannel(final int p0, final byte[] p1, final int p2, final int p3) throws Exception;
    
    final class EpollServerSocketUnsafe extends AbstractEpollUnsafe
    {
        private final byte[] acceptedAddress;
        
        EpollServerSocketUnsafe() {
            this.acceptedAddress = new byte[26];
        }
        
        @Override
        public void connect(final SocketAddress socketAddress, final SocketAddress socketAddress2, final ChannelPromise channelPromise) {
            channelPromise.setFailure((Throwable)new UnsupportedOperationException());
        }
        
        @Override
        void epollInReady() {
            assert AbstractEpollServerChannel.this.eventLoop().inEventLoop();
            final boolean edgeTriggered = AbstractEpollServerChannel.this.isFlagSet(Native.EPOLLET);
            final ChannelConfig config = AbstractEpollServerChannel.this.config();
            if (!this.readPending && !edgeTriggered && !config.isAutoRead()) {
                this.clearEpollIn0();
                return;
            }
            final ChannelPipeline pipeline = AbstractEpollServerChannel.this.pipeline();
            Throwable exception = null;
            try {
                try {
                    final int maxMessagesPerRead = edgeTriggered ? Integer.MAX_VALUE : config.getMaxMessagesPerRead();
                    int messages = 0;
                    do {
                        final int socketFd = Native.accept(AbstractEpollServerChannel.this.fd().intValue(), this.acceptedAddress);
                        if (socketFd == -1) {
                            break;
                        }
                        this.readPending = false;
                        try {
                            final int len = this.acceptedAddress[0];
                            pipeline.fireChannelRead(AbstractEpollServerChannel.this.newChildChannel(socketFd, this.acceptedAddress, 1, len));
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
                }
                catch (Throwable t2) {
                    exception = t2;
                }
                pipeline.fireChannelReadComplete();
                if (exception != null) {
                    pipeline.fireExceptionCaught(exception);
                }
            }
            finally {
                if (!this.readPending && !config.isAutoRead()) {
                    this.clearEpollIn0();
                }
            }
        }
    }
}
