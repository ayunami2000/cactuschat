// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.memcache;

import io.netty.buffer.ByteBufHolder;
import io.netty.handler.codec.MessageAggregator;

public abstract class AbstractMemcacheObjectAggregator<H extends MemcacheMessage> extends MessageAggregator<MemcacheObject, H, MemcacheContent, FullMemcacheMessage>
{
    protected AbstractMemcacheObjectAggregator(final int maxContentLength) {
        super(maxContentLength);
    }
    
    @Override
    protected boolean isContentMessage(final MemcacheObject msg) throws Exception {
        return msg instanceof MemcacheContent;
    }
    
    @Override
    protected boolean isLastContentMessage(final MemcacheContent msg) throws Exception {
        return msg instanceof LastMemcacheContent;
    }
    
    @Override
    protected boolean isAggregated(final MemcacheObject msg) throws Exception {
        return msg instanceof FullMemcacheMessage;
    }
    
    @Override
    protected boolean hasContentLength(final H start) throws Exception {
        return false;
    }
    
    @Override
    protected long contentLength(final H start) throws Exception {
        throw new UnsupportedOperationException();
    }
    
    @Override
    protected Object newContinueResponse(final H start) throws Exception {
        return null;
    }
}
