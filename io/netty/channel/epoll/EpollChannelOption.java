// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.channel.epoll;

import io.netty.channel.unix.DomainSocketReadMode;
import io.netty.channel.ChannelOption;

public final class EpollChannelOption
{
    private static final Class<EpollChannelOption> T;
    public static final ChannelOption<Boolean> TCP_CORK;
    public static final ChannelOption<Boolean> SO_REUSEPORT;
    public static final ChannelOption<Integer> TCP_KEEPIDLE;
    public static final ChannelOption<Integer> TCP_KEEPINTVL;
    public static final ChannelOption<Integer> TCP_KEEPCNT;
    public static final ChannelOption<DomainSocketReadMode> DOMAIN_SOCKET_READ_MODE;
    public static final ChannelOption<EpollMode> EPOLL_MODE;
    
    private EpollChannelOption() {
    }
    
    static {
        T = EpollChannelOption.class;
        TCP_CORK = ChannelOption.valueOf(EpollChannelOption.T, "TCP_CORK");
        SO_REUSEPORT = ChannelOption.valueOf(EpollChannelOption.T, "SO_REUSEPORT");
        TCP_KEEPIDLE = ChannelOption.valueOf(EpollChannelOption.T, "TCP_KEEPIDLE");
        TCP_KEEPINTVL = ChannelOption.valueOf(EpollChannelOption.T, "TCP_KEEPINTVL");
        TCP_KEEPCNT = ChannelOption.valueOf(EpollChannelOption.T, "TCP_KEEPCNT");
        DOMAIN_SOCKET_READ_MODE = ChannelOption.valueOf(EpollChannelOption.T, "DOMAIN_SOCKET_READ_MODE");
        EPOLL_MODE = ChannelOption.valueOf(EpollChannelOption.T, "EPOLL_MODE");
    }
}
