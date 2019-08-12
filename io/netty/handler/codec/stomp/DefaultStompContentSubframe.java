// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.stomp;

import io.netty.util.ReferenceCounted;
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderResult;

public class DefaultStompContentSubframe implements StompContentSubframe
{
    private DecoderResult decoderResult;
    private final ByteBuf content;
    
    public DefaultStompContentSubframe(final ByteBuf content) {
        this.decoderResult = DecoderResult.SUCCESS;
        if (content == null) {
            throw new NullPointerException("content");
        }
        this.content = content;
    }
    
    @Override
    public ByteBuf content() {
        return this.content;
    }
    
    @Override
    public StompContentSubframe copy() {
        return new DefaultStompContentSubframe(this.content().copy());
    }
    
    @Override
    public StompContentSubframe duplicate() {
        return new DefaultStompContentSubframe(this.content().duplicate());
    }
    
    @Override
    public int refCnt() {
        return this.content().refCnt();
    }
    
    @Override
    public StompContentSubframe retain() {
        this.content().retain();
        return this;
    }
    
    @Override
    public StompContentSubframe retain(final int increment) {
        this.content().retain(increment);
        return this;
    }
    
    @Override
    public StompContentSubframe touch() {
        this.content.touch();
        return this;
    }
    
    @Override
    public StompContentSubframe touch(final Object hint) {
        this.content.touch(hint);
        return this;
    }
    
    @Override
    public boolean release() {
        return this.content().release();
    }
    
    @Override
    public boolean release(final int decrement) {
        return this.content().release(decrement);
    }
    
    @Override
    public DecoderResult decoderResult() {
        return this.decoderResult;
    }
    
    @Override
    public void setDecoderResult(final DecoderResult decoderResult) {
        this.decoderResult = decoderResult;
    }
    
    @Override
    public String toString() {
        return "DefaultStompContent{decoderResult=" + this.decoderResult + '}';
    }
}
