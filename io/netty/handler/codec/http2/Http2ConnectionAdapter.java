// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http2;

public class Http2ConnectionAdapter implements Http2Connection.Listener
{
    @Override
    public void streamAdded(final Http2Stream stream) {
    }
    
    @Override
    public void streamActive(final Http2Stream stream) {
    }
    
    @Override
    public void streamHalfClosed(final Http2Stream stream) {
    }
    
    @Override
    public void streamInactive(final Http2Stream stream) {
    }
    
    @Override
    public void streamRemoved(final Http2Stream stream) {
    }
    
    @Override
    public void goingAway() {
    }
    
    @Override
    public void priorityTreeParentChanged(final Http2Stream stream, final Http2Stream oldParent) {
    }
    
    @Override
    public void priorityTreeParentChanging(final Http2Stream stream, final Http2Stream newParent) {
    }
    
    @Override
    public void onWeightChanged(final Http2Stream stream, final short oldWeight) {
    }
}
