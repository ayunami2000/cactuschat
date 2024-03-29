// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http;

import io.netty.handler.codec.DefaultHeaders;
import io.netty.util.ReferenceCounted;
import io.netty.buffer.ByteBufHolder;
import java.util.Iterator;
import java.util.Map;
import io.netty.util.internal.StringUtil;
import io.netty.handler.codec.TextHeaders;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class DefaultLastHttpContent extends DefaultHttpContent implements LastHttpContent
{
    private final HttpHeaders trailingHeaders;
    private final boolean validateHeaders;
    
    public DefaultLastHttpContent() {
        this(Unpooled.buffer(0));
    }
    
    public DefaultLastHttpContent(final ByteBuf content) {
        this(content, true);
    }
    
    public DefaultLastHttpContent(final ByteBuf content, final boolean validateHeaders) {
        super(content);
        this.trailingHeaders = new TrailingHttpHeaders(validateHeaders);
        this.validateHeaders = validateHeaders;
    }
    
    @Override
    public LastHttpContent copy() {
        final DefaultLastHttpContent copy = new DefaultLastHttpContent(this.content().copy(), this.validateHeaders);
        copy.trailingHeaders().set((TextHeaders)this.trailingHeaders());
        return copy;
    }
    
    @Override
    public LastHttpContent duplicate() {
        final DefaultLastHttpContent copy = new DefaultLastHttpContent(this.content().duplicate(), this.validateHeaders);
        copy.trailingHeaders().set((TextHeaders)this.trailingHeaders());
        return copy;
    }
    
    @Override
    public LastHttpContent retain(final int increment) {
        super.retain(increment);
        return this;
    }
    
    @Override
    public LastHttpContent retain() {
        super.retain();
        return this;
    }
    
    @Override
    public LastHttpContent touch() {
        super.touch();
        return this;
    }
    
    @Override
    public LastHttpContent touch(final Object hint) {
        super.touch(hint);
        return this;
    }
    
    @Override
    public HttpHeaders trailingHeaders() {
        return this.trailingHeaders;
    }
    
    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder(super.toString());
        buf.append(StringUtil.NEWLINE);
        this.appendHeaders(buf);
        buf.setLength(buf.length() - StringUtil.NEWLINE.length());
        return buf.toString();
    }
    
    private void appendHeaders(final StringBuilder buf) {
        for (final Map.Entry<CharSequence, CharSequence> e : this.trailingHeaders()) {
            buf.append(e.getKey());
            buf.append(": ");
            buf.append(e.getValue());
            buf.append(StringUtil.NEWLINE);
        }
    }
    
    private static final class TrailingHttpHeaders extends DefaultHttpHeaders
    {
        private static final TrailingHttpHeadersNameConverter VALIDATE_NAME_CONVERTER;
        private static final TrailingHttpHeadersNameConverter NO_VALIDATE_NAME_CONVERTER;
        
        TrailingHttpHeaders(final boolean validate) {
            super(validate, validate ? TrailingHttpHeaders.VALIDATE_NAME_CONVERTER : TrailingHttpHeaders.NO_VALIDATE_NAME_CONVERTER, false);
        }
        
        static {
            VALIDATE_NAME_CONVERTER = new TrailingHttpHeadersNameConverter(true);
            NO_VALIDATE_NAME_CONVERTER = new TrailingHttpHeadersNameConverter(false);
        }
        
        private static final class TrailingHttpHeadersNameConverter extends HttpHeadersNameConverter
        {
            TrailingHttpHeadersNameConverter(final boolean validate) {
                super(validate);
            }
            
            @Override
            public CharSequence convertName(CharSequence name) {
                name = super.convertName(name);
                if (this.validate && (HttpHeaderNames.CONTENT_LENGTH.equalsIgnoreCase(name) || HttpHeaderNames.TRANSFER_ENCODING.equalsIgnoreCase(name) || HttpHeaderNames.TRAILER.equalsIgnoreCase(name))) {
                    throw new IllegalArgumentException("prohibited trailing header: " + (Object)name);
                }
                return name;
            }
        }
    }
}
