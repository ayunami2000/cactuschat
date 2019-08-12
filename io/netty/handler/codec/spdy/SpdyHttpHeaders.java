// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.spdy;

import io.netty.handler.codec.AsciiString;

public final class SpdyHttpHeaders
{
    private SpdyHttpHeaders() {
    }
    
    public static final class Names
    {
        public static final AsciiString STREAM_ID;
        public static final AsciiString ASSOCIATED_TO_STREAM_ID;
        public static final AsciiString PRIORITY;
        public static final AsciiString SCHEME;
        
        private Names() {
        }
        
        static {
            STREAM_ID = new AsciiString("X-SPDY-Stream-ID");
            ASSOCIATED_TO_STREAM_ID = new AsciiString("X-SPDY-Associated-To-Stream-ID");
            PRIORITY = new AsciiString("X-SPDY-Priority");
            SCHEME = new AsciiString("X-SPDY-Scheme");
        }
    }
}
