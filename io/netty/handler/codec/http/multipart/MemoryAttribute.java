// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http.multipart;

import io.netty.buffer.ByteBufHolder;
import io.netty.util.ReferenceCounted;
import io.netty.channel.ChannelException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.nio.charset.Charset;
import io.netty.handler.codec.http.HttpConstants;

public class MemoryAttribute extends AbstractMemoryHttpData implements Attribute
{
    public MemoryAttribute(final String name) {
        this(name, HttpConstants.DEFAULT_CHARSET);
    }
    
    public MemoryAttribute(final String name, final Charset charset) {
        super(name, charset, 0L);
    }
    
    public MemoryAttribute(final String name, final String value) throws IOException {
        this(name, value, HttpConstants.DEFAULT_CHARSET);
    }
    
    public MemoryAttribute(final String name, final String value, final Charset charset) throws IOException {
        super(name, charset, 0L);
        this.setValue(value);
    }
    
    @Override
    public InterfaceHttpData.HttpDataType getHttpDataType() {
        return InterfaceHttpData.HttpDataType.Attribute;
    }
    
    @Override
    public String getValue() {
        return this.getByteBuf().toString(this.getCharset());
    }
    
    @Override
    public void setValue(final String value) throws IOException {
        if (value == null) {
            throw new NullPointerException("value");
        }
        final byte[] bytes = value.getBytes(this.getCharset());
        this.checkSize(bytes.length);
        final ByteBuf buffer = Unpooled.wrappedBuffer(bytes);
        if (this.definedSize > 0L) {
            this.definedSize = buffer.readableBytes();
        }
        this.setContent(buffer);
    }
    
    @Override
    public void addContent(final ByteBuf buffer, final boolean last) throws IOException {
        final int localsize = buffer.readableBytes();
        this.checkSize(this.size + localsize);
        if (this.definedSize > 0L && this.definedSize < this.size + localsize) {
            this.definedSize = this.size + localsize;
        }
        super.addContent(buffer, last);
    }
    
    @Override
    public int hashCode() {
        return this.getName().hashCode();
    }
    
    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof Attribute)) {
            return false;
        }
        final Attribute attribute = (Attribute)o;
        return this.getName().equalsIgnoreCase(attribute.getName());
    }
    
    @Override
    public int compareTo(final InterfaceHttpData other) {
        if (!(other instanceof Attribute)) {
            throw new ClassCastException("Cannot compare " + this.getHttpDataType() + " with " + other.getHttpDataType());
        }
        return this.compareTo((Attribute)other);
    }
    
    public int compareTo(final Attribute o) {
        return this.getName().compareToIgnoreCase(o.getName());
    }
    
    @Override
    public String toString() {
        return this.getName() + '=' + this.getValue();
    }
    
    @Override
    public Attribute copy() {
        final MemoryAttribute attr = new MemoryAttribute(this.getName());
        attr.setCharset(this.getCharset());
        final ByteBuf content = this.content();
        if (content != null) {
            try {
                attr.setContent(content.copy());
            }
            catch (IOException e) {
                throw new ChannelException(e);
            }
        }
        return attr;
    }
    
    @Override
    public Attribute duplicate() {
        final MemoryAttribute attr = new MemoryAttribute(this.getName());
        attr.setCharset(this.getCharset());
        final ByteBuf content = this.content();
        if (content != null) {
            try {
                attr.setContent(content.duplicate());
            }
            catch (IOException e) {
                throw new ChannelException(e);
            }
        }
        return attr;
    }
    
    @Override
    public Attribute retain() {
        super.retain();
        return this;
    }
    
    @Override
    public Attribute retain(final int increment) {
        super.retain(increment);
        return this;
    }
    
    @Override
    public Attribute touch() {
        super.touch();
        return this;
    }
    
    @Override
    public Attribute touch(final Object hint) {
        super.touch(hint);
        return this;
    }
}
