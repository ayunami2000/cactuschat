// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http2;

import java.util.Collection;

public interface Http2Stream
{
    int id();
    
    State state();
    
    Http2Stream open(final boolean p0) throws Http2Exception;
    
    Http2Stream close();
    
    Http2Stream closeLocalSide();
    
    Http2Stream closeRemoteSide();
    
    boolean isResetSent();
    
    Http2Stream resetSent();
    
    boolean remoteSideOpen();
    
    boolean localSideOpen();
    
    Object setProperty(final Object p0, final Object p1);
    
     <V> V getProperty(final Object p0);
    
     <V> V removeProperty(final Object p0);
    
    Http2Stream setPriority(final int p0, final short p1, final boolean p2) throws Http2Exception;
    
    boolean isRoot();
    
    boolean isLeaf();
    
    short weight();
    
    int totalChildWeights();
    
    Http2Stream parent();
    
    boolean isDescendantOf(final Http2Stream p0);
    
    int numChildren();
    
    boolean hasChild(final int p0);
    
    Http2Stream child(final int p0);
    
    Collection<? extends Http2Stream> children();
    
    public enum State
    {
        IDLE, 
        RESERVED_LOCAL, 
        RESERVED_REMOTE, 
        OPEN, 
        HALF_CLOSED_LOCAL, 
        HALF_CLOSED_REMOTE, 
        CLOSED;
    }
}
