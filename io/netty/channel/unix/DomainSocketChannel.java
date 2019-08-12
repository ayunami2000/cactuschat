// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.channel.unix;

public interface DomainSocketChannel extends UnixChannel
{
    DomainSocketAddress remoteAddress();
    
    DomainSocketAddress localAddress();
    
    DomainSocketChannelConfig config();
}
