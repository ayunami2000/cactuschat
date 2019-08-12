// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http;

import io.netty.util.ReferenceCounted;
import io.netty.buffer.ByteBufHolder;
import io.netty.handler.codec.TextHeaders;
import io.netty.buffer.Unpooled;
import io.netty.buffer.ByteBuf;

public class DefaultFullHttpResponse extends DefaultHttpResponse implements FullHttpResponse
{
    private static final int HASH_CODE_PRIME = 31;
    private final ByteBuf content;
    private final HttpHeaders trailingHeaders;
    private final boolean validateHeaders;
    
    public DefaultFullHttpResponse(final HttpVersion version, final HttpResponseStatus status) {
        this(version, status, Unpooled.buffer(0));
    }
    
    public DefaultFullHttpResponse(final HttpVersion version, final HttpResponseStatus status, final ByteBuf content) {
        this(version, status, content, false);
    }
    
    public DefaultFullHttpResponse(final HttpVersion version, final HttpResponseStatus status, final boolean validateHeaders) {
        this(version, status, Unpooled.buffer(0), validateHeaders, false);
    }
    
    public DefaultFullHttpResponse(final HttpVersion version, final HttpResponseStatus status, final boolean validateHeaders, final boolean singleFieldHeaders) {
        this(version, status, Unpooled.buffer(0), validateHeaders, singleFieldHeaders);
    }
    
    public DefaultFullHttpResponse(final HttpVersion version, final HttpResponseStatus status, final ByteBuf content, final boolean singleFieldHeaders) {
        this(version, status, content, true, singleFieldHeaders);
    }
    
    public DefaultFullHttpResponse(final HttpVersion version, final HttpResponseStatus status, final ByteBuf content, final boolean validateHeaders, final boolean singleFieldHeaders) {
        super(version, status, validateHeaders, singleFieldHeaders);
        if (content == null) {
            throw new NullPointerException("content");
        }
        this.content = content;
        this.trailingHeaders = new DefaultHttpHeaders(validateHeaders, singleFieldHeaders);
        this.validateHeaders = validateHeaders;
    }
    
    @Override
    public HttpHeaders trailingHeaders() {
        return this.trailingHeaders;
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
    public FullHttpResponse retain() {
        this.content.retain();
        return this;
    }
    
    @Override
    public FullHttpResponse retain(final int increment) {
        this.content.retain(increment);
        return this;
    }
    
    @Override
    public FullHttpResponse touch() {
        this.content.touch();
        return this;
    }
    
    @Override
    public FullHttpResponse touch(final Object hint) {
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
    public FullHttpResponse setProtocolVersion(final HttpVersion version) {
        super.setProtocolVersion(version);
        return this;
    }
    
    @Override
    public FullHttpResponse setStatus(final HttpResponseStatus status) {
        super.setStatus(status);
        return this;
    }
    
    private FullHttpResponse copy(final boolean copyContent, final ByteBuf newContent) {
        final DefaultFullHttpResponse copy = new DefaultFullHttpResponse(this.protocolVersion(), this.status(), copyContent ? this.content().copy() : ((newContent == null) ? Unpooled.buffer(0) : newContent));
        copy.headers().set((TextHeaders)this.headers());
        copy.trailingHeaders().set((TextHeaders)this.trailingHeaders());
        return copy;
    }
    
    @Override
    public FullHttpResponse copy(final ByteBuf newContent) {
        return this.copy(false, newContent);
    }
    
    @Override
    public FullHttpResponse copy() {
        return this.copy(true, null);
    }
    
    @Override
    public FullHttpResponse duplicate() {
        final DefaultFullHttpResponse duplicate = new DefaultFullHttpResponse(this.protocolVersion(), this.status(), this.content().duplicate(), this.validateHeaders);
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
        if (!(o instanceof DefaultFullHttpResponse)) {
            return false;
        }
        final DefaultFullHttpResponse other = (DefaultFullHttpResponse)o;
        return super.equals(other) && this.content().equals(other.content()) && this.trailingHeaders().equals(other.trailingHeaders());
    }
    
    @Override
    public String toString() {
        return HttpMessageUtil.appendFullResponse(new StringBuilder(256), this).toString();
    }
}
