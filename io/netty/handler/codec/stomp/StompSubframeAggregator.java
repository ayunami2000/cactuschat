// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.stomp;

import io.netty.handler.codec.AsciiString;
import io.netty.handler.codec.Headers;
import io.netty.buffer.ByteBufHolder;
import io.netty.handler.codec.TextHeaders;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.MessageAggregator;

public class StompSubframeAggregator extends MessageAggregator<StompSubframe, StompHeadersSubframe, StompContentSubframe, StompFrame>
{
    public StompSubframeAggregator(final int maxContentLength) {
        super(maxContentLength);
    }
    
    @Override
    protected boolean isStartMessage(final StompSubframe msg) throws Exception {
        return msg instanceof StompHeadersSubframe;
    }
    
    @Override
    protected boolean isContentMessage(final StompSubframe msg) throws Exception {
        return msg instanceof StompContentSubframe;
    }
    
    @Override
    protected boolean isLastContentMessage(final StompContentSubframe msg) throws Exception {
        return msg instanceof LastStompContentSubframe;
    }
    
    @Override
    protected boolean isAggregated(final StompSubframe msg) throws Exception {
        return msg instanceof StompFrame;
    }
    
    @Override
    protected boolean hasContentLength(final StompHeadersSubframe start) throws Exception {
        return ((Headers<AsciiString>)start.headers()).contains(StompHeaders.CONTENT_LENGTH);
    }
    
    @Override
    protected long contentLength(final StompHeadersSubframe start) throws Exception {
        return ((Headers<AsciiString>)start.headers()).getLong(StompHeaders.CONTENT_LENGTH, 0L);
    }
    
    @Override
    protected Object newContinueResponse(final StompHeadersSubframe start) throws Exception {
        return null;
    }
    
    @Override
    protected StompFrame beginAggregation(final StompHeadersSubframe start, final ByteBuf content) throws Exception {
        final StompFrame ret = new DefaultStompFrame(start.command(), content);
        ret.headers().set((TextHeaders)start.headers());
        return ret;
    }
}
