// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http2;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class Http2EventAdapter implements Http2Connection.Listener, Http2FrameListener
{
    @Override
    public int onDataRead(final ChannelHandlerContext ctx, final int streamId, final ByteBuf data, final int padding, final boolean endOfStream) throws Http2Exception {
        return data.readableBytes() + padding;
    }
    
    @Override
    public void onHeadersRead(final ChannelHandlerContext ctx, final int streamId, final Http2Headers headers, final int padding, final boolean endStream) throws Http2Exception {
    }
    
    @Override
    public void onHeadersRead(final ChannelHandlerContext ctx, final int streamId, final Http2Headers headers, final int streamDependency, final short weight, final boolean exclusive, final int padding, final boolean endStream) throws Http2Exception {
    }
    
    @Override
    public void onPriorityRead(final ChannelHandlerContext ctx, final int streamId, final int streamDependency, final short weight, final boolean exclusive) throws Http2Exception {
    }
    
    @Override
    public void onRstStreamRead(final ChannelHandlerContext ctx, final int streamId, final long errorCode) throws Http2Exception {
    }
    
    @Override
    public void onSettingsAckRead(final ChannelHandlerContext ctx) throws Http2Exception {
    }
    
    @Override
    public void onSettingsRead(final ChannelHandlerContext ctx, final Http2Settings settings) throws Http2Exception {
    }
    
    @Override
    public void onPingRead(final ChannelHandlerContext ctx, final ByteBuf data) throws Http2Exception {
    }
    
    @Override
    public void onPingAckRead(final ChannelHandlerContext ctx, final ByteBuf data) throws Http2Exception {
    }
    
    @Override
    public void onPushPromiseRead(final ChannelHandlerContext ctx, final int streamId, final int promisedStreamId, final Http2Headers headers, final int padding) throws Http2Exception {
    }
    
    @Override
    public void onGoAwayRead(final ChannelHandlerContext ctx, final int lastStreamId, final long errorCode, final ByteBuf debugData) throws Http2Exception {
    }
    
    @Override
    public void onWindowUpdateRead(final ChannelHandlerContext ctx, final int streamId, final int windowSizeIncrement) throws Http2Exception {
    }
    
    @Override
    public void onUnknownFrame(final ChannelHandlerContext ctx, final byte frameType, final int streamId, final Http2Flags flags, final ByteBuf payload) {
    }
    
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
    public void priorityTreeParentChanged(final Http2Stream stream, final Http2Stream oldParent) {
    }
    
    @Override
    public void priorityTreeParentChanging(final Http2Stream stream, final Http2Stream newParent) {
    }
    
    @Override
    public void onWeightChanged(final Http2Stream stream, final short oldWeight) {
    }
    
    @Override
    public void goingAway() {
    }
}
