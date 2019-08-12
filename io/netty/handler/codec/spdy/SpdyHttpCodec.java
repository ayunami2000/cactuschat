// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.spdy;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAppender;

public final class SpdyHttpCodec extends ChannelHandlerAppender
{
    public SpdyHttpCodec(final SpdyVersion version, final int maxContentLength) {
        super(new ChannelHandler[] { new SpdyHttpDecoder(version, maxContentLength), new SpdyHttpEncoder(version) });
    }
    
    public SpdyHttpCodec(final SpdyVersion version, final int maxContentLength, final boolean validateHttpHeaders) {
        super(new ChannelHandler[] { new SpdyHttpDecoder(version, maxContentLength, validateHttpHeaders), new SpdyHttpEncoder(version) });
    }
}
