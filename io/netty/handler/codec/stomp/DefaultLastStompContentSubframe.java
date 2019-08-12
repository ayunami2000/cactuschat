// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.stomp;

import io.netty.util.ReferenceCounted;
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.ByteBuf;

public class DefaultLastStompContentSubframe extends DefaultStompContentSubframe implements LastStompContentSubframe
{
    public DefaultLastStompContentSubframe(final ByteBuf content) {
        super(content);
    }
    
    @Override
    public DefaultLastStompContentSubframe retain() {
        super.retain();
        return this;
    }
    
    @Override
    public LastStompContentSubframe retain(final int increment) {
        super.retain(increment);
        return this;
    }
    
    @Override
    public LastStompContentSubframe touch() {
        super.touch();
        return this;
    }
    
    @Override
    public LastStompContentSubframe touch(final Object hint) {
        super.touch(hint);
        return this;
    }
    
    @Override
    public LastStompContentSubframe copy() {
        return new DefaultLastStompContentSubframe(this.content().copy());
    }
    
    @Override
    public LastStompContentSubframe duplicate() {
        return new DefaultLastStompContentSubframe(this.content().duplicate());
    }
    
    @Override
    public String toString() {
        return "DefaultLastStompContent{decoderResult=" + this.decoderResult() + '}';
    }
}
