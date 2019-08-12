// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.memcache;

import io.netty.util.ReferenceCounted;
import io.netty.buffer.ByteBufHolder;
import io.netty.handler.codec.DecoderResult;
import io.netty.buffer.Unpooled;
import io.netty.buffer.ByteBuf;

public interface LastMemcacheContent extends MemcacheContent
{
    public static final LastMemcacheContent EMPTY_LAST_CONTENT = new LastMemcacheContent() {
        @Override
        public LastMemcacheContent copy() {
            return LastMemcacheContent$1.EMPTY_LAST_CONTENT;
        }
        
        @Override
        public LastMemcacheContent retain(final int increment) {
            return this;
        }
        
        @Override
        public LastMemcacheContent retain() {
            return this;
        }
        
        @Override
        public LastMemcacheContent touch() {
            return this;
        }
        
        @Override
        public LastMemcacheContent touch(final Object hint) {
            return this;
        }
        
        @Override
        public LastMemcacheContent duplicate() {
            return this;
        }
        
        @Override
        public ByteBuf content() {
            return Unpooled.EMPTY_BUFFER;
        }
        
        @Override
        public DecoderResult decoderResult() {
            return DecoderResult.SUCCESS;
        }
        
        @Override
        public void setDecoderResult(final DecoderResult result) {
            throw new UnsupportedOperationException("read only");
        }
        
        @Override
        public int refCnt() {
            return 1;
        }
        
        @Override
        public boolean release() {
            return false;
        }
        
        @Override
        public boolean release(final int decrement) {
            return false;
        }
    };
    
    LastMemcacheContent copy();
    
    LastMemcacheContent retain(final int p0);
    
    LastMemcacheContent retain();
    
    LastMemcacheContent touch();
    
    LastMemcacheContent touch(final Object p0);
    
    LastMemcacheContent duplicate();
}
