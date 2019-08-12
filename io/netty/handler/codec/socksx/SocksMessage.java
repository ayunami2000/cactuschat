// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.socksx;

import io.netty.handler.codec.DecoderResultProvider;

public interface SocksMessage extends DecoderResultProvider
{
    SocksVersion version();
}
