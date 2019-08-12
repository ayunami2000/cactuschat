// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.channel.udt;

import io.netty.channel.ChannelOption;

public final class UdtChannelOption
{
    private static final Class<UdtChannelOption> T;
    public static final ChannelOption<Integer> PROTOCOL_RECEIVE_BUFFER_SIZE;
    public static final ChannelOption<Integer> PROTOCOL_SEND_BUFFER_SIZE;
    public static final ChannelOption<Integer> SYSTEM_RECEIVE_BUFFER_SIZE;
    public static final ChannelOption<Integer> SYSTEM_SEND_BUFFER_SIZE;
    
    private UdtChannelOption() {
    }
    
    static {
        T = UdtChannelOption.class;
        PROTOCOL_RECEIVE_BUFFER_SIZE = ChannelOption.valueOf(UdtChannelOption.T, "PROTOCOL_RECEIVE_BUFFER_SIZE");
        PROTOCOL_SEND_BUFFER_SIZE = ChannelOption.valueOf(UdtChannelOption.T, "PROTOCOL_SEND_BUFFER_SIZE");
        SYSTEM_RECEIVE_BUFFER_SIZE = ChannelOption.valueOf(UdtChannelOption.T, "SYSTEM_RECEIVE_BUFFER_SIZE");
        SYSTEM_SEND_BUFFER_SIZE = ChannelOption.valueOf(UdtChannelOption.T, "SYSTEM_SEND_BUFFER_SIZE");
    }
}
