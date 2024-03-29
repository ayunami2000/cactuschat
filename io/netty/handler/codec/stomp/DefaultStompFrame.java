// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.stomp;

import io.netty.util.ReferenceCounted;
import io.netty.buffer.ByteBufHolder;
import io.netty.util.CharsetUtil;
import io.netty.buffer.Unpooled;
import io.netty.buffer.ByteBuf;

public class DefaultStompFrame extends DefaultStompHeadersSubframe implements StompFrame
{
    private final ByteBuf content;
    
    public DefaultStompFrame(final StompCommand command) {
        this(command, Unpooled.buffer(0));
    }
    
    public DefaultStompFrame(final StompCommand command, final ByteBuf content) {
        super(command);
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
    public StompFrame copy() {
        return new DefaultStompFrame(this.command, this.content.copy());
    }
    
    @Override
    public StompFrame duplicate() {
        return new DefaultStompFrame(this.command, this.content.duplicate());
    }
    
    @Override
    public int refCnt() {
        return this.content.refCnt();
    }
    
    @Override
    public StompFrame retain() {
        this.content.retain();
        return this;
    }
    
    @Override
    public StompFrame retain(final int increment) {
        this.content.retain();
        return this;
    }
    
    @Override
    public StompFrame touch() {
        this.content.touch();
        return this;
    }
    
    @Override
    public StompFrame touch(final Object hint) {
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
        return "DefaultFullStompFrame{command=" + this.command + ", headers=" + this.headers + ", content=" + this.content.toString(CharsetUtil.UTF_8) + '}';
    }
}
