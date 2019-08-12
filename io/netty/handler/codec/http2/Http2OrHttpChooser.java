// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http2;

import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.ssl.SslHandler;
import io.netty.channel.ChannelHandler;
import java.util.List;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import javax.net.ssl.SSLEngine;
import io.netty.handler.codec.ByteToMessageDecoder;

public abstract class Http2OrHttpChooser extends ByteToMessageDecoder
{
    private final int maxHttpContentLength;
    
    protected Http2OrHttpChooser(final int maxHttpContentLength) {
        this.maxHttpContentLength = maxHttpContentLength;
    }
    
    protected abstract SelectedProtocol getProtocol(final SSLEngine p0);
    
    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) throws Exception {
        if (this.initPipeline(ctx)) {
            ctx.pipeline().remove(this);
        }
    }
    
    private boolean initPipeline(final ChannelHandlerContext ctx) {
        final SslHandler handler = ctx.pipeline().get(SslHandler.class);
        if (handler == null) {
            throw new IllegalStateException("SslHandler is needed for HTTP2");
        }
        final SelectedProtocol protocol = this.getProtocol(handler.engine());
        switch (protocol) {
            case UNKNOWN: {
                return false;
            }
            case HTTP_2: {
                this.addHttp2Handlers(ctx);
                break;
            }
            case HTTP_1_0:
            case HTTP_1_1: {
                this.addHttpHandlers(ctx);
                break;
            }
            default: {
                throw new IllegalStateException("Unknown SelectedProtocol");
            }
        }
        return true;
    }
    
    protected void addHttp2Handlers(final ChannelHandlerContext ctx) {
        final ChannelPipeline pipeline = ctx.pipeline();
        pipeline.addLast("http2ConnectionHandler", this.createHttp2RequestHandler());
    }
    
    protected void addHttpHandlers(final ChannelHandlerContext ctx) {
        final ChannelPipeline pipeline = ctx.pipeline();
        pipeline.addLast("httpRequestDecoder", new HttpRequestDecoder());
        pipeline.addLast("httpResponseEncoder", new HttpResponseEncoder());
        pipeline.addLast("httpChunkAggregator", new HttpObjectAggregator(this.maxHttpContentLength));
        pipeline.addLast("httpRequestHandler", this.createHttp1RequestHandler());
    }
    
    protected abstract ChannelHandler createHttp1RequestHandler();
    
    protected abstract Http2ConnectionHandler createHttp2RequestHandler();
    
    public enum SelectedProtocol
    {
        HTTP_2("h2-16"), 
        HTTP_1_1("http/1.1"), 
        HTTP_1_0("http/1.0"), 
        UNKNOWN("Unknown");
        
        private final String name;
        
        private SelectedProtocol(final String defaultName) {
            this.name = defaultName;
        }
        
        public String protocolName() {
            return this.name;
        }
        
        public static SelectedProtocol protocol(final String name) {
            for (final SelectedProtocol protocol : values()) {
                if (protocol.protocolName().equals(name)) {
                    return protocol;
                }
            }
            return SelectedProtocol.UNKNOWN;
        }
    }
}
