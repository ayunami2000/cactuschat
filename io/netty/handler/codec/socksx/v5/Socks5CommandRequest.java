// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.socksx.v5;

public interface Socks5CommandRequest extends Socks5Message
{
    Socks5CommandType type();
    
    Socks5AddressType dstAddrType();
    
    String dstAddr();
    
    int dstPort();
}
