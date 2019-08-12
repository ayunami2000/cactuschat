// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.channel.rxtx;

import io.netty.channel.ChannelOption;

public final class RxtxChannelOption
{
    private static final Class<RxtxChannelOption> T;
    public static final ChannelOption<Integer> BAUD_RATE;
    public static final ChannelOption<Boolean> DTR;
    public static final ChannelOption<Boolean> RTS;
    public static final ChannelOption<RxtxChannelConfig.Stopbits> STOP_BITS;
    public static final ChannelOption<RxtxChannelConfig.Databits> DATA_BITS;
    public static final ChannelOption<RxtxChannelConfig.Paritybit> PARITY_BIT;
    public static final ChannelOption<Integer> WAIT_TIME;
    public static final ChannelOption<Integer> READ_TIMEOUT;
    
    private RxtxChannelOption() {
    }
    
    static {
        T = RxtxChannelOption.class;
        BAUD_RATE = ChannelOption.valueOf(RxtxChannelOption.T, "BAUD_RATE");
        DTR = ChannelOption.valueOf(RxtxChannelOption.T, "DTR");
        RTS = ChannelOption.valueOf(RxtxChannelOption.T, "RTS");
        STOP_BITS = ChannelOption.valueOf(RxtxChannelOption.T, "STOP_BITS");
        DATA_BITS = ChannelOption.valueOf(RxtxChannelOption.T, "DATA_BITS");
        PARITY_BIT = ChannelOption.valueOf(RxtxChannelOption.T, "PARITY_BIT");
        WAIT_TIME = ChannelOption.valueOf(RxtxChannelOption.T, "WAIT_TIME");
        READ_TIMEOUT = ChannelOption.valueOf(RxtxChannelOption.T, "READ_TIMEOUT");
    }
}
