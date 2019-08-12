// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.memcache;

import io.netty.handler.codec.DecoderResult;

public abstract class AbstractMemcacheObject implements MemcacheObject
{
    private DecoderResult decoderResult;
    
    protected AbstractMemcacheObject() {
        this.decoderResult = DecoderResult.SUCCESS;
    }
    
    @Override
    public DecoderResult decoderResult() {
        return this.decoderResult;
    }
    
    @Override
    public void setDecoderResult(final DecoderResult result) {
        if (result == null) {
            throw new NullPointerException("DecoderResult should not be null.");
        }
        this.decoderResult = result;
    }
}
