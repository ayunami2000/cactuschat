// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http;

public interface HttpRequest extends HttpMessage
{
    HttpMethod method();
    
    HttpRequest setMethod(final HttpMethod p0);
    
    String uri();
    
    HttpRequest setUri(final String p0);
    
    HttpRequest setProtocolVersion(final HttpVersion p0);
}
