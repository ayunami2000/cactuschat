// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http2;

public interface Http2StreamRemovalPolicy
{
    void setAction(final Action p0);
    
    void markForRemoval(final Http2Stream p0);
    
    public interface Action
    {
        void removeStream(final Http2Stream p0);
    }
}
