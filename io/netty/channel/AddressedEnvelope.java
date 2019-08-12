// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.channel;

import io.netty.util.ReferenceCounted;
import java.net.SocketAddress;

public interface AddressedEnvelope<M, A extends SocketAddress> extends ReferenceCounted
{
    M content();
    
    A sender();
    
    A recipient();
    
    AddressedEnvelope<M, A> retain();
    
    AddressedEnvelope<M, A> retain(final int p0);
    
    AddressedEnvelope<M, A> touch();
    
    AddressedEnvelope<M, A> touch(final Object p0);
}