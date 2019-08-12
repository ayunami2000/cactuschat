// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http;

public class DefaultHttpRequest extends DefaultHttpMessage implements HttpRequest
{
    private static final int HASH_CODE_PRIME = 31;
    private HttpMethod method;
    private String uri;
    
    public DefaultHttpRequest(final HttpVersion httpVersion, final HttpMethod method, final String uri) {
        this(httpVersion, method, uri, true);
    }
    
    public DefaultHttpRequest(final HttpVersion httpVersion, final HttpMethod method, final String uri, final boolean validateHeaders) {
        super(httpVersion, validateHeaders, false);
        if (method == null) {
            throw new NullPointerException("method");
        }
        if (uri == null) {
            throw new NullPointerException("uri");
        }
        this.method = method;
        this.uri = uri;
    }
    
    @Override
    public HttpMethod method() {
        return this.method;
    }
    
    @Override
    public String uri() {
        return this.uri;
    }
    
    @Override
    public HttpRequest setMethod(final HttpMethod method) {
        if (method == null) {
            throw new NullPointerException("method");
        }
        this.method = method;
        return this;
    }
    
    @Override
    public HttpRequest setUri(final String uri) {
        if (uri == null) {
            throw new NullPointerException("uri");
        }
        this.uri = uri;
        return this;
    }
    
    @Override
    public HttpRequest setProtocolVersion(final HttpVersion version) {
        super.setProtocolVersion(version);
        return this;
    }
    
    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + this.method.hashCode();
        result = 31 * result + this.uri.hashCode();
        result = 31 * result + super.hashCode();
        return result;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof DefaultHttpRequest)) {
            return false;
        }
        final DefaultHttpRequest other = (DefaultHttpRequest)o;
        return this.method().equals(other.method()) && this.uri().equalsIgnoreCase(other.uri()) && super.equals(o);
    }
    
    @Override
    public String toString() {
        return HttpMessageUtil.appendRequest(new StringBuilder(256), this).toString();
    }
}
