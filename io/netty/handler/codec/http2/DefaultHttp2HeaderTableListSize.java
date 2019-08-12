// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http2;

class DefaultHttp2HeaderTableListSize
{
    private int maxHeaderListSize;
    
    DefaultHttp2HeaderTableListSize() {
        this.maxHeaderListSize = Integer.MAX_VALUE;
    }
    
    public void maxHeaderListSize(final int max) throws Http2Exception {
        if (max < 0) {
            throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Header List Size must be non-negative but was %d", max);
        }
        this.maxHeaderListSize = max;
    }
    
    public int maxHeaderListSize() {
        return this.maxHeaderListSize;
    }
}
