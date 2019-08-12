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
import io.netty.util.NetUtil;

public class EpollServerChannelConfig extends EpollChannelConfig
{
    protected final AbstractEpollChannel channel;
    private volatile int backlog;
    
    EpollServerChannelConfig(final AbstractEpollChannel channel) {
        super(channel);
        this.backlog = NetUtil.SOMAXCONN;
        this.channel = channel;
    }
    
    @Override
    public Map<ChannelOption<?>, Object> getOptions() {
        return this.getOptions(super.getOptions(), ChannelOption.SO_RCVBUF, ChannelOption.SO_REUSEADDR, ChannelOption.SO_BACKLOG);
    }
    
    @Override
    public <T> T getOption(final ChannelOption<T> option) {
        if (option == ChannelOption.SO_RCVBUF) {
            return (T)Integer.valueOf(this.getReceiveBufferSize());
        }
        if (option == ChannelOption.SO_REUSEADDR) {
            return (T)Boolean.valueOf(this.isReuseAddress());
        }
        if (option == ChannelOption.SO_BACKLOG) {
            return (T)Integer.valueOf(this.getBacklog());
        }
        return super.getOption(option);
    }
    
    @Override
    public <T> boolean setOption(final ChannelOption<T> option, final T value) {
        this.validate(option, value);
        if (option == ChannelOption.SO_RCVBUF) {
            this.setReceiveBufferSize((int)value);
        }
        else if (option == ChannelOption.SO_REUSEADDR) {
            this.setReuseAddress((boolean)value);
        }
        else {
            if (option != ChannelOption.SO_BACKLOG) {
                return super.setOption(option, value);
            }
            this.setBacklog((int)value);
        }
        return true;
    }
    
    public boolean isReuseAddress() {
        return Native.isReuseAddress(this.channel.fd().intValue()) == 1;
    }
    
    public EpollServerChannelConfig setReuseAddress(final boolean reuseAddress) {
        Native.setReuseAddress(this.channel.fd().intValue(), reuseAddress ? 1 : 0);
        return this;
    }
    
    public int getReceiveBufferSize() {
        return Native.getReceiveBufferSize(this.channel.fd().intValue());
    }
    
    public EpollServerChannelConfig setReceiveBufferSize(final int receiveBufferSize) {
        Native.setReceiveBufferSize(this.channel.fd().intValue(), receiveBufferSize);
        return this;
    }
    
    public int getBacklog() {
        return this.backlog;
    }
    
    public EpollServerChannelConfig setBacklog(final int backlog) {
        if (backlog < 0) {
            throw new IllegalArgumentException("backlog: " + backlog);
        }
        this.backlog = backlog;
        return this;
    }
    
    @Override
    public EpollServerChannelConfig setConnectTimeoutMillis(final int connectTimeoutMillis) {
        super.setConnectTimeoutMillis(connectTimeoutMillis);
        return this;
    }
    
    @Override
    public EpollServerChannelConfig setMaxMessagesPerRead(final int maxMessagesPerRead) {
        super.setMaxMessagesPerRead(maxMessagesPerRead);
        return this;
    }
    
    @Override
    public EpollServerChannelConfig setWriteSpinCount(final int writeSpinCount) {
        super.setWriteSpinCount(writeSpinCount);
        return this;
    }
    
    @Override
    public EpollServerChannelConfig setAllocator(final ByteBufAllocator allocator) {
        super.setAllocator(allocator);
        return this;
    }
    
    @Override
    public EpollServerChannelConfig setRecvByteBufAllocator(final RecvByteBufAllocator allocator) {
        super.setRecvByteBufAllocator(allocator);
        return this;
    }
    
    @Override
    public EpollServerChannelConfig setAutoRead(final boolean autoRead) {
        super.setAutoRead(autoRead);
        return this;
    }
    
    @Override
    public EpollServerChannelConfig setWriteBufferHighWaterMark(final int writeBufferHighWaterMark) {
        super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
        return this;
    }
    
    @Override
    public EpollServerChannelConfig setWriteBufferLowWaterMark(final int writeBufferLowWaterMark) {
        super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
        return this;
    }
    
    @Override
    public EpollServerChannelConfig setMessageSizeEstimator(final MessageSizeEstimator estimator) {
        super.setMessageSizeEstimator(estimator);
        return this;
    }
    
    @Override
    public EpollServerChannelConfig setEpollMode(final EpollMode mode) {
        super.setEpollMode(mode);
        return this;
    }
}
