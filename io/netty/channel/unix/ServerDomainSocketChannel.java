// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.channel.unix;

import io.netty.channel.ServerChannel;

public interface ServerDomainSocketChannel extends ServerChannel, UnixChannel
{
    DomainSocketAddress remoteAddress();
    
    DomainSocketAddress localAddress();
}
