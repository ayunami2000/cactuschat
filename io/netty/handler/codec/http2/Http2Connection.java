// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http2;

import java.util.Collection;

public interface Http2Connection
{
    void addListener(final Listener p0);
    
    void removeListener(final Listener p0);
    
    Http2Stream requireStream(final int p0) throws Http2Exception;
    
    Http2Stream stream(final int p0);
    
    Http2Stream connectionStream();
    
    int numActiveStreams();
    
    Collection<Http2Stream> activeStreams();
    
    void deactivate(final Http2Stream p0);
    
    boolean isServer();
    
    Endpoint<Http2LocalFlowController> local();
    
    Http2Stream createLocalStream(final int p0) throws Http2Exception;
    
    Endpoint<Http2RemoteFlowController> remote();
    
    Http2Stream createRemoteStream(final int p0) throws Http2Exception;
    
    boolean goAwayReceived();
    
    void goAwayReceived(final int p0);
    
    boolean goAwaySent();
    
    void goAwaySent(final int p0);
    
    boolean isGoAway();
    
    public interface Endpoint<F extends Http2FlowController>
    {
        int nextStreamId();
        
        boolean createdStreamId(final int p0);
        
        boolean acceptingNewStreams();
        
        Http2Stream createStream(final int p0) throws Http2Exception;
        
        Http2Stream reservePushStream(final int p0, final Http2Stream p1) throws Http2Exception;
        
        boolean isServer();
        
        void allowPushTo(final boolean p0);
        
        boolean allowPushTo();
        
        int numActiveStreams();
        
        int maxStreams();
        
        void maxStreams(final int p0);
        
        int lastStreamCreated();
        
        int lastKnownStream();
        
        F flowController();
        
        void flowController(final F p0);
        
        Endpoint<? extends Http2FlowController> opposite();
    }
    
    public interface Listener
    {
        void streamAdded(final Http2Stream p0);
        
        void streamActive(final Http2Stream p0);
        
        void streamHalfClosed(final Http2Stream p0);
        
        void streamInactive(final Http2Stream p0);
        
        void streamRemoved(final Http2Stream p0);
        
        void priorityTreeParentChanged(final Http2Stream p0, final Http2Stream p1);
        
        void priorityTreeParentChanging(final Http2Stream p0, final Http2Stream p1);
        
        void onWeightChanged(final Http2Stream p0, final short p1);
        
        void goingAway();
    }
}
