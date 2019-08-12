// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec;

public interface DecoderResultProvider
{
    DecoderResult decoderResult();
    
    void setDecoderResult(final DecoderResult p0);
}
