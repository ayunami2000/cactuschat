// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http.websocketx;

import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.MessageAggregator;

public class WebSocketFrameAggregator extends MessageAggregator<WebSocketFrame, WebSocketFrame, ContinuationWebSocketFrame, WebSocketFrame>
{
    public WebSocketFrameAggregator(final int maxContentLength) {
        super(maxContentLength);
    }
    
    @Override
    protected boolean isStartMessage(final WebSocketFrame msg) throws Exception {
        return msg instanceof TextWebSocketFrame || msg instanceof BinaryWebSocketFrame;
    }
    
    @Override
    protected boolean isContentMessage(final WebSocketFrame msg) throws Exception {
        return msg instanceof ContinuationWebSocketFrame;
    }
    
    @Override
    protected boolean isLastContentMessage(final ContinuationWebSocketFrame msg) throws Exception {
        return this.isContentMessage(msg) && msg.isFinalFragment();
    }
    
    @Override
    protected boolean isAggregated(final WebSocketFrame msg) throws Exception {
        if (msg.isFinalFragment()) {
            return !this.isContentMessage(msg);
        }
        return !this.isStartMessage(msg) && !this.isContentMessage(msg);
    }
    
    @Override
    protected boolean hasContentLength(final WebSocketFrame start) throws Exception {
        return false;
    }
    
    @Override
    protected long contentLength(final WebSocketFrame start) throws Exception {
        throw new UnsupportedOperationException();
    }
    
    @Override
    protected Object newContinueResponse(final WebSocketFrame start) throws Exception {
        return null;
    }
    
    @Override
    protected WebSocketFrame beginAggregation(final WebSocketFrame start, final ByteBuf content) throws Exception {
        if (start instanceof TextWebSocketFrame) {
            return new TextWebSocketFrame(true, start.rsv(), content);
        }
        if (start instanceof BinaryWebSocketFrame) {
            return new BinaryWebSocketFrame(true, start.rsv(), content);
        }
        throw new Error();
    }
}
