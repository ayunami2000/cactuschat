// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http;

import io.netty.handler.codec.compression.ZlibCodecFactory;
import io.netty.handler.codec.compression.ZlibWrapper;
import io.netty.channel.ChannelHandler;
import io.netty.channel.embedded.EmbeddedChannel;

public class HttpContentDecompressor extends HttpContentDecoder
{
    private final boolean strict;
    
    public HttpContentDecompressor() {
        this(false);
    }
    
    public HttpContentDecompressor(final boolean strict) {
        this.strict = strict;
    }
    
    @Override
    protected EmbeddedChannel newContentDecoder(final String contentEncoding) throws Exception {
        if (HttpHeaderValues.GZIP.equalsIgnoreCase(contentEncoding) || HttpHeaderValues.X_GZIP.equalsIgnoreCase(contentEncoding)) {
            return new EmbeddedChannel(new ChannelHandler[] { ZlibCodecFactory.newZlibDecoder(ZlibWrapper.GZIP) });
        }
        if (HttpHeaderValues.DEFLATE.equalsIgnoreCase(contentEncoding) || HttpHeaderValues.X_DEFLATE.equalsIgnoreCase(contentEncoding)) {
            final ZlibWrapper wrapper = this.strict ? ZlibWrapper.ZLIB : ZlibWrapper.ZLIB_OR_NONE;
            return new EmbeddedChannel(new ChannelHandler[] { ZlibCodecFactory.newZlibDecoder(wrapper) });
        }
        return null;
    }
}
