// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.memcache;

import io.netty.util.ReferenceCounted;
import io.netty.buffer.ByteBufHolder;
import io.netty.util.internal.StringUtil;
import io.netty.buffer.ByteBuf;

public class DefaultMemcacheContent extends AbstractMemcacheObject implements MemcacheContent
{
    private final ByteBuf content;
    
    public DefaultMemcacheContent(final ByteBuf content) {
        if (content == null) {
            throw new NullPointerException("Content cannot be null.");
        }
        this.content = content;
    }
    
    @Override
    public ByteBuf content() {
        return this.content;
    }
    
    @Override
    public MemcacheContent copy() {
        return new DefaultMemcacheContent(this.content.copy());
    }
    
    @Override
    public MemcacheContent duplicate() {
        return new DefaultMemcacheContent(this.content.duplicate());
    }
    
    @Override
    public int refCnt() {
        return this.content.refCnt();
    }
    
    @Override
    public MemcacheContent retain() {
        this.content.retain();
        return this;
    }
    
    @Override
    public MemcacheContent retain(final int increment) {
        this.content.retain(increment);
        return this;
    }
    
    @Override
    public MemcacheContent touch() {
        this.content.touch();
        return this;
    }
    
    @Override
    public MemcacheContent touch(final Object hint) {
        this.content.touch(hint);
        return this;
    }
    
    @Override
    public boolean release() {
        return this.content.release();
    }
    
    @Override
    public boolean release(final int decrement) {
        return this.content.release(decrement);
    }
    
    @Override
    public String toString() {
        return StringUtil.simpleClassName(this) + "(data: " + this.content() + ", decoderResult: " + this.decoderResult() + ')';
    }
}
