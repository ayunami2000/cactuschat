// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http;

import io.netty.handler.codec.Headers;
import io.netty.util.internal.StringUtil;
import io.netty.handler.codec.compression.ZlibWrapper;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.compression.ZlibCodecFactory;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.AsciiString;

public class HttpContentCompressor extends HttpContentEncoder
{
    private final int compressionLevel;
    private final int windowBits;
    private final int memLevel;
    
    public HttpContentCompressor() {
        this(6);
    }
    
    public HttpContentCompressor(final int compressionLevel) {
        this(compressionLevel, 15, 8);
    }
    
    public HttpContentCompressor(final int compressionLevel, final int windowBits, final int memLevel) {
        if (compressionLevel < 0 || compressionLevel > 9) {
            throw new IllegalArgumentException("compressionLevel: " + compressionLevel + " (expected: 0-9)");
        }
        if (windowBits < 9 || windowBits > 15) {
            throw new IllegalArgumentException("windowBits: " + windowBits + " (expected: 9-15)");
        }
        if (memLevel < 1 || memLevel > 9) {
            throw new IllegalArgumentException("memLevel: " + memLevel + " (expected: 1-9)");
        }
        this.compressionLevel = compressionLevel;
        this.windowBits = windowBits;
        this.memLevel = memLevel;
    }
    
    @Override
    protected Result beginEncode(final HttpResponse headers, final CharSequence acceptEncoding) throws Exception {
        final CharSequence contentEncoding = ((Headers<AsciiString>)headers.headers()).get(HttpHeaderNames.CONTENT_ENCODING);
        if (contentEncoding != null && !HttpHeaderValues.IDENTITY.equalsIgnoreCase(contentEncoding)) {
            return null;
        }
        final ZlibWrapper wrapper = this.determineWrapper(acceptEncoding);
        if (wrapper == null) {
            return null;
        }
        String targetContentEncoding = null;
        switch (wrapper) {
            case GZIP: {
                targetContentEncoding = "gzip";
                break;
            }
            case ZLIB: {
                targetContentEncoding = "deflate";
                break;
            }
            default: {
                throw new Error();
            }
        }
        return new Result(targetContentEncoding, new EmbeddedChannel(new ChannelHandler[] { ZlibCodecFactory.newZlibEncoder(wrapper, this.compressionLevel, this.windowBits, this.memLevel) }));
    }
    
    protected ZlibWrapper determineWrapper(final CharSequence acceptEncoding) {
        float starQ = -1.0f;
        float gzipQ = -1.0f;
        float deflateQ = -1.0f;
        for (final String encoding : StringUtil.split(acceptEncoding.toString(), ',')) {
            float q = 1.0f;
            final int equalsPos = encoding.indexOf(61);
            if (equalsPos != -1) {
                try {
                    q = Float.valueOf(encoding.substring(equalsPos + 1));
                }
                catch (NumberFormatException e) {
                    q = 0.0f;
                }
            }
            if (encoding.contains("*")) {
                starQ = q;
            }
            else if (encoding.contains("gzip") && q > gzipQ) {
                gzipQ = q;
            }
            else if (encoding.contains("deflate") && q > deflateQ) {
                deflateQ = q;
            }
        }
        if (gzipQ <= 0.0f && deflateQ <= 0.0f) {
            if (starQ > 0.0f) {
                if (gzipQ == -1.0f) {
                    return ZlibWrapper.GZIP;
                }
                if (deflateQ == -1.0f) {
                    return ZlibWrapper.ZLIB;
                }
            }
            return null;
        }
        if (gzipQ >= deflateQ) {
            return ZlibWrapper.GZIP;
        }
        return ZlibWrapper.ZLIB;
    }
}
