// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http;

import io.netty.util.ReferenceCounted;
import io.netty.buffer.ByteBufHolder;
import io.netty.handler.codec.TextHeaders;
import io.netty.buffer.Unpooled;
import io.netty.buffer.ByteBuf;

public class DefaultFullHttpRequest extends DefaultHttpRequest implements FullHttpRequest
{
    private static final int HASH_CODE_PRIME = 31;
    private final ByteBuf content;
    private final HttpHeaders trailingHeader;
    private final boolean validateHeaders;
    
    public DefaultFullHttpRequest(final HttpVersion httpVersion, final HttpMethod method, final String uri) {
        this(httpVersion, method, uri, Unpooled.buffer(0));
    }
    
    public DefaultFullHttpRequest(final HttpVersion httpVersion, final HttpMethod method, final String uri, final ByteBuf content) {
        this(httpVersion, method, uri, content, true);
    }
    
    public DefaultFullHttpRequest(final HttpVersion httpVersion, final HttpMethod method, final String uri, final boolean validateHeaders) {
        this(httpVersion, method, uri, Unpooled.buffer(0), true);
    }
    
    public DefaultFullHttpRequest(final HttpVersion httpVersion, final HttpMethod method, final String uri, final ByteBuf content, final boolean validateHeaders) {
        super(httpVersion, method, uri, validateHeaders);
        if (content == null) {
            throw new NullPointerException("content");
        }
        this.content = content;
        this.trailingHeader = new DefaultHttpHeaders(validateHeaders);
        this.validateHeaders = validateHeaders;
    }
    
    @Override
    public HttpHeaders trailingHeaders() {
        return this.trailingHeader;
    }
    
    @Override
    public ByteBuf content() {
        return this.content;
    }
    
    @Override
    public int refCnt() {
        return this.content.refCnt();
    }
    
    @Override
    public FullHttpRequest retain() {
        this.content.retain();
        return this;
    }
    
    @Override
    public FullHttpRequest retain(final int increment) {
        this.content.retain(increment);
        return this;
    }
    
    @Override
    public FullHttpRequest touch() {
        this.content.touch();
        return this;
    }
    
    @Override
    public FullHttpRequest touch(final Object hint) {
        this.content.touch(hint);
        return this;
    }
    
    @Override
    public boolean release() {
        return this.content.release();
    }
    
    @Override
    public boolean release(final int decrement) {
        return this.content.release(decrement);
    }
    
    @Override
    public FullHttpRequest setProtocolVersion(final HttpVersion version) {
        super.setProtocolVersion(version);
        return this;
    }
    
    @Override
    public FullHttpRequest setMethod(final HttpMethod method) {
        super.setMethod(method);
        return this;
    }
    
    @Override
    public FullHttpRequest setUri(final String uri) {
        super.setUri(uri);
        return this;
    }
    
    private FullHttpRequest copy(final boolean copyContent, final ByteBuf newContent) {
        final DefaultFullHttpRequest copy = new DefaultFullHttpRequest(this.protocolVersion(), this.method(), this.uri(), copyContent ? this.content().copy() : ((newContent == null) ? Unpooled.buffer(0) : newContent));
        copy.headers().set((TextHeaders)this.headers());
        copy.trailingHeaders().set((TextHeaders)this.trailingHeaders());
        return copy;
    }
    
    @Override
    public FullHttpRequest copy(final ByteBuf newContent) {
        return this.copy(false, newContent);
    }
    
    @Override
    public FullHttpRequest copy() {
        return this.copy(true, null);
    }
    
    @Override
    public FullHttpRequest duplicate() {
        final DefaultFullHttpRequest duplicate = new DefaultFullHttpRequest(this.protocolVersion(), this.method(), this.uri(), this.content().duplicate(), this.validateHeaders);
        duplicate.headers().set((TextHeaders)this.headers());
        duplicate.trailingHeaders().set((TextHeaders)this.trailingHeaders());
        return duplicate;
    }
    
    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + this.content().hashCode();
        result = 31 * result + this.trailingHeaders().hashCode();
        result = 31 * result + super.hashCode();
        return result;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof DefaultFullHttpRequest)) {
            return false;
        }
        final DefaultFullHttpRequest other = (DefaultFullHttpRequest)o;
        return super.equals(other) && this.content().equals(other.content()) && this.trailingHeaders().equals(other.trailingHeaders());
    }
    
    @Override
    public String toString() {
        return HttpMessageUtil.appendFullRequest(new StringBuilder(256), this).toString();
    }
}
