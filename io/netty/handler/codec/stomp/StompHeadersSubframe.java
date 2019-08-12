// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.stomp;

public interface StompHeadersSubframe extends StompSubframe
{
    StompCommand command();
    
    StompHeaders headers();
}
