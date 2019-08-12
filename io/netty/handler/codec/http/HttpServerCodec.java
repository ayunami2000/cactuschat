// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAppender;

public final class HttpServerCodec extends ChannelHandlerAppender implements HttpServerUpgradeHandler.SourceCodec
{
    public HttpServerCodec() {
        this(4096, 8192, 8192);
    }
    
    public HttpServerCodec(final int maxInitialLineLength, final int maxHeaderSize, final int maxChunkSize) {
        super(new ChannelHandler[] { new HttpRequestDecoder(maxInitialLineLength, maxHeaderSize, maxChunkSize), new HttpResponseEncoder() });
    }
    
    public HttpServerCodec(final int maxInitialLineLength, final int maxHeaderSize, final int maxChunkSize, final boolean validateHeaders) {
        super(new ChannelHandler[] { new HttpRequestDecoder(maxInitialLineLength, maxHeaderSize, maxChunkSize, validateHeaders), new HttpResponseEncoder() });
    }
    
    @Override
    public void upgradeFrom(final ChannelHandlerContext ctx) {
        ctx.pipeline().remove(HttpRequestDecoder.class);
        ctx.pipeline().remove(HttpResponseEncoder.class);
    }
    
    public HttpResponseEncoder encoder() {
        return this.handlerAt(1);
    }
    
    public HttpRequestDecoder decoder() {
        return this.handlerAt(0);
    }
}
