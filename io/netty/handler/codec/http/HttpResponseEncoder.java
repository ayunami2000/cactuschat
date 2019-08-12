// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http;

import io.netty.handler.codec.AsciiString;
import io.netty.buffer.ByteBuf;

public class HttpResponseEncoder extends HttpObjectEncoder<HttpResponse>
{
    private static final byte[] CRLF;
    
    @Override
    public boolean acceptOutboundMessage(final Object msg) throws Exception {
        return super.acceptOutboundMessage(msg) && !(msg instanceof HttpRequest);
    }
    
    @Override
    protected void encodeInitialLine(final ByteBuf buf, final HttpResponse response) throws Exception {
        final AsciiString version = response.protocolVersion().text();
        buf.writeBytes(version.array(), version.arrayOffset(), version.length());
        buf.writeByte(32);
        final AsciiString code = response.status().codeAsText();
        buf.writeBytes(code.array(), code.arrayOffset(), code.length());
        buf.writeByte(32);
        final AsciiString reasonPhrase = response.status().reasonPhrase();
        buf.writeBytes(reasonPhrase.array(), reasonPhrase.arrayOffset(), reasonPhrase.length());
        buf.writeBytes(HttpResponseEncoder.CRLF);
    }
    
    static {
        CRLF = new byte[] { 13, 10 };
    }
}
