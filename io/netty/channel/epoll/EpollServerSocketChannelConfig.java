// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.channel.epoll;

import io.netty.channel.ChannelConfig;
import io.netty.channel.MessageSizeEstimator;
import io.netty.channel.RecvByteBufAllocator;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelOption;
import java.util.Map;
import io.netty.channel.socket.ServerSocketChannelConfig;

public final class EpollServerSocketChannelConfig extends EpollServerChannelConfig implements ServerSocketChannelConfig
{
    EpollServerSocketChannelConfig(final EpollServerSocketChannel channel) {
        super(channel);
        this.setReuseAddress(true);
    }
    
    @Override
    public Map<ChannelOption<?>, Object> getOptions() {
        return this.getOptions(super.getOptions(), EpollChannelOption.SO_REUSEPORT);
    }
    
    @Override
    public <T> T getOption(final ChannelOption<T> option) {
        if (option == EpollChannelOption.SO_REUSEPORT) {
            return (T)Boolean.valueOf(this.isReusePort());
        }
        return super.getOption(option);
    }
    
    @Override
    public <T> boolean setOption(final ChannelOption<T> option, final T value) {
        this.validate(option, value);
        if (option == EpollChannelOption.SO_REUSEPORT) {
            this.setReusePort((boolean)value);
            return true;
        }
        return super.setOption(option, value);
    }
    
    @Override
    public EpollServerSocketChannelConfig setReuseAddress(final boolean reuseAddress) {
        super.setReuseAddress(reuseAddress);
        return this;
    }
    
    @Override
    public EpollServerSocketChannelConfig setReceiveBufferSize(final int receiveBufferSize) {
        super.setReceiveBufferSize(receiveBufferSize);
        return this;
    }
    
    @Override
    public EpollServerSocketChannelConfig setPerformancePreferences(final int connectionTime, final int latency, final int bandwidth) {
        return this;
    }
    
    @Override
    public EpollServerSocketChannelConfig setBacklog(final int backlog) {
        super.setBacklog(backlog);
        return this;
    }
    
    @Override
    public EpollServerSocketChannelConfig setConnectTimeoutMillis(final int connectTimeoutMillis) {
        super.setConnectTimeoutMillis(connectTimeoutMillis);
        return this;
    }
    
    @Override
    public EpollServerSocketChannelConfig setMaxMessagesPerRead(final int maxMessagesPerRead) {
        super.setMaxMessagesPerRead(maxMessagesPerRead);
        return this;
    }
    
    @Override
    public EpollServerSocketChannelConfig setWriteSpinCount(final int writeSpinCount) {
        super.setWriteSpinCount(writeSpinCount);
        return this;
    }
    
    @Override
    public EpollServerSocketChannelConfig setAllocator(final ByteBufAllocator allocator) {
        super.setAllocator(allocator);
        return this;
    }
    
    @Override
    public EpollServerSocketChannelConfig setRecvByteBufAllocator(final RecvByteBufAllocator allocator) {
        super.setRecvByteBufAllocator(allocator);
        return this;
    }
    
    @Override
    public EpollServerSocketChannelConfig setAutoRead(final boolean autoRead) {
        super.setAutoRead(autoRead);
        return this;
    }
    
    @Override
    public EpollServerSocketChannelConfig setWriteBufferHighWaterMark(final int writeBufferHighWaterMark) {
        super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
        return this;
    }
    
    @Override
    public EpollServerSocketChannelConfig setWriteBufferLowWaterMark(final int writeBufferLowWaterMark) {
        super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
        return this;
    }
    
    @Override
    public EpollServerSocketChannelConfig setMessageSizeEstimator(final MessageSizeEstimator estimator) {
        super.setMessageSizeEstimator(estimator);
        return this;
    }
    
    public boolean isReusePort() {
        return Native.isReusePort(this.channel.fd().intValue()) == 1;
    }
    
    public EpollServerSocketChannelConfig setReusePort(final boolean reusePort) {
        Native.setReusePort(this.channel.fd().intValue(), reusePort ? 1 : 0);
        return this;
    }
}
