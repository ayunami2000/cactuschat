// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.rtsp;

import io.netty.handler.codec.AsciiString;
import io.netty.handler.codec.Headers;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpObjectDecoder;

public abstract class RtspObjectDecoder extends HttpObjectDecoder
{
    protected RtspObjectDecoder() {
        this(4096, 8192, 8192);
    }
    
    protected RtspObjectDecoder(final int maxInitialLineLength, final int maxHeaderSize, final int maxContentLength) {
        super(maxInitialLineLength, maxHeaderSize, maxContentLength * 2, false);
    }
    
    protected RtspObjectDecoder(final int maxInitialLineLength, final int maxHeaderSize, final int maxContentLength, final boolean validateHeaders) {
        super(maxInitialLineLength, maxHeaderSize, maxContentLength * 2, false, validateHeaders);
    }
    
    @Override
    protected boolean isContentAlwaysEmpty(final HttpMessage msg) {
        final boolean empty = super.isContentAlwaysEmpty(msg);
        return empty || !((Headers<AsciiString>)msg.headers()).contains(RtspHeaderNames.CONTENT_LENGTH) || empty;
    }
}
