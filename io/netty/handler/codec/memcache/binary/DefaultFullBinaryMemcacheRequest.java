// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.memcache.binary;

import io.netty.buffer.ByteBufHolder;
import io.netty.handler.codec.memcache.MemcacheContent;
import io.netty.handler.codec.memcache.LastMemcacheContent;
import io.netty.handler.codec.memcache.FullMemcacheMessage;
import io.netty.util.ReferenceCounted;
import io.netty.handler.codec.memcache.MemcacheMessage;
import io.netty.buffer.Unpooled;
import io.netty.buffer.ByteBuf;

public class DefaultFullBinaryMemcacheRequest extends DefaultBinaryMemcacheRequest implements FullBinaryMemcacheRequest
{
    private final ByteBuf content;
    
    public DefaultFullBinaryMemcacheRequest(final String key, final ByteBuf extras) {
        this(key, extras, Unpooled.buffer(0));
    }
    
    public DefaultFullBinaryMemcacheRequest(final String key, final ByteBuf extras, final ByteBuf content) {
        super(key, extras);
        if (content == null) {
            throw new NullPointerException("Supplied content is null.");
        }
        this.content = content;
    }
    
    @Override
    public ByteBuf content() {
        return this.content;
    }
    
    @Override
    public int refCnt() {
        return this.content.refCnt();
    }
    
    @Override
    public FullBinaryMemcacheRequest retain() {
        super.retain();
        this.content.retain();
        return this;
    }
    
    @Override
    public FullBinaryMemcacheRequest retain(final int increment) {
        super.retain(increment);
        this.content.retain(increment);
        return this;
    }
    
    @Override
    public FullBinaryMemcacheRequest touch() {
        super.touch();
        this.content.touch();
        return this;
    }
    
    @Override
    public FullBinaryMemcacheRequest touch(final Object hint) {
        super.touch(hint);
        this.content.touch(hint);
        return this;
    }
    
    @Override
    public boolean release() {
        super.release();
        return this.content.release();
    }
    
    @Override
    public boolean release(final int decrement) {
        super.release(decrement);
        return this.content.release(decrement);
    }
    
    @Override
    public FullBinaryMemcacheRequest copy() {
        ByteBuf extras = this.extras();
        if (extras != null) {
            extras = extras.copy();
        }
        return new DefaultFullBinaryMemcacheRequest(this.key(), extras, this.content().copy());
    }
    
    @Override
    public FullBinaryMemcacheRequest duplicate() {
        ByteBuf extras = this.extras();
        if (extras != null) {
            extras = extras.duplicate();
        }
        return new DefaultFullBinaryMemcacheRequest(this.key(), extras, this.content().duplicate());
    }
}
