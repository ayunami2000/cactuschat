// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.socksx.v4;

public interface Socks4CommandResponse extends Socks4Message
{
    Socks4CommandStatus status();
    
    String dstAddr();
    
    int dstPort();
}
