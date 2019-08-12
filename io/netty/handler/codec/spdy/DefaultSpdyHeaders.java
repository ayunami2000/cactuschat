// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.spdy;

import java.util.Locale;
import io.netty.handler.codec.AsciiString;
import io.netty.handler.codec.TextHeaders;
import io.netty.handler.codec.DefaultHeaders;
import io.netty.handler.codec.Headers;
import io.netty.handler.codec.DefaultTextHeaders;

public class DefaultSpdyHeaders extends DefaultTextHeaders implements SpdyHeaders
{
    private static final Headers.ValueConverter<CharSequence> SPDY_VALUE_CONVERTER;
    private static final NameConverter<CharSequence> SPDY_NAME_CONVERTER;
    
    public DefaultSpdyHeaders() {
        super(true, DefaultSpdyHeaders.SPDY_VALUE_CONVERTER, DefaultSpdyHeaders.SPDY_NAME_CONVERTER);
    }
    
    @Override
    public SpdyHeaders add(final CharSequence name, final CharSequence value) {
        super.add(name, value);
        return this;
    }
    
    @Override
    public SpdyHeaders add(final CharSequence name, final Iterable<? extends CharSequence> values) {
        super.add(name, values);
        return this;
    }
    
    @Override
    public SpdyHeaders add(final CharSequence name, final CharSequence... values) {
        super.add(name, values);
        return this;
    }
    
    @Override
    public SpdyHeaders addObject(final CharSequence name, final Object value) {
        super.addObject(name, value);
        return this;
    }
    
    @Override
    public SpdyHeaders addObject(final CharSequence name, final Iterable<?> values) {
        super.addObject(name, values);
        return this;
    }
    
    @Override
    public SpdyHeaders addObject(final CharSequence name, final Object... values) {
        super.addObject(name, values);
        return this;
    }
    
    @Override
    public SpdyHeaders addBoolean(final CharSequence name, final boolean value) {
        super.addBoolean(name, value);
        return this;
    }
    
    @Override
    public SpdyHeaders addChar(final CharSequence name, final char value) {
        super.addChar(name, value);
        return this;
    }
    
    @Override
    public SpdyHeaders addByte(final CharSequence name, final byte value) {
        super.addByte(name, value);
        return this;
    }
    
    @Override
    public SpdyHeaders addShort(final CharSequence name, final short value) {
        super.addShort(name, value);
        return this;
    }
    
    @Override
    public SpdyHeaders addInt(final CharSequence name, final int value) {
        super.addInt(name, value);
        return this;
    }
    
    @Override
    public SpdyHeaders addLong(final CharSequence name, final long value) {
        super.addLong(name, value);
        return this;
    }
    
    @Override
    public SpdyHeaders addFloat(final CharSequence name, final float value) {
        super.addFloat(name, value);
        return this;
    }
    
    @Override
    public SpdyHeaders addDouble(final CharSequence name, final double value) {
        super.addDouble(name, value);
        return this;
    }
    
    @Override
    public SpdyHeaders addTimeMillis(final CharSequence name, final long value) {
        super.addTimeMillis(name, value);
        return this;
    }
    
    @Override
    public SpdyHeaders add(final TextHeaders headers) {
        super.add(headers);
        return this;
    }
    
    @Override
    public SpdyHeaders set(final CharSequence name, final CharSequence value) {
        super.set(name, value);
        return this;
    }
    
    @Override
    public SpdyHeaders set(final CharSequence name, final Iterable<? extends CharSequence> values) {
        super.set(name, values);
        return this;
    }
    
    @Override
    public SpdyHeaders set(final CharSequence name, final CharSequence... values) {
        super.set(name, values);
        return this;
    }
    
    @Override
    public SpdyHeaders setObject(final CharSequence name, final Object value) {
        super.setObject(name, value);
        return this;
    }
    
    @Override
    public SpdyHeaders setObject(final CharSequence name, final Iterable<?> values) {
        super.setObject(name, values);
        return this;
    }
    
    @Override
    public SpdyHeaders setObject(final CharSequence name, final Object... values) {
        super.setObject(name, values);
        return this;
    }
    
    @Override
    public SpdyHeaders setBoolean(final CharSequence name, final boolean value) {
        super.setBoolean(name, value);
        return this;
    }
    
    @Override
    public SpdyHeaders setChar(final CharSequence name, final char value) {
        super.setChar(name, value);
        return this;
    }
    
    @Override
    public SpdyHeaders setByte(final CharSequence name, final byte value) {
        super.setByte(name, value);
        return this;
    }
    
    @Override
    public SpdyHeaders setShort(final CharSequence name, final short value) {
        super.setShort(name, value);
        return this;
    }
    
    @Override
    public SpdyHeaders setInt(final CharSequence name, final int value) {
        super.setInt(name, value);
        return this;
    }
    
    @Override
    public SpdyHeaders setLong(final CharSequence name, final long value) {
        super.setLong(name, value);
        return this;
    }
    
    @Override
    public SpdyHeaders setFloat(final CharSequence name, final float value) {
        super.setFloat(name, value);
        return this;
    }
    
    @Override
    public SpdyHeaders setDouble(final CharSequence name, final double value) {
        super.setDouble(name, value);
        return this;
    }
    
    @Override
    public SpdyHeaders setTimeMillis(final CharSequence name, final long value) {
        super.setTimeMillis(name, value);
        return this;
    }
    
    @Override
    public SpdyHeaders set(final TextHeaders headers) {
        super.set(headers);
        return this;
    }
    
    @Override
    public SpdyHeaders setAll(final TextHeaders headers) {
        super.setAll(headers);
        return this;
    }
    
    @Override
    public SpdyHeaders clear() {
        super.clear();
        return this;
    }
    
    static {
        SPDY_VALUE_CONVERTER = new DefaultTextValueTypeConverter() {
            @Override
            public CharSequence convertObject(final Object value) {
                CharSequence seq;
                if (value instanceof CharSequence) {
                    seq = (CharSequence)value;
                }
                else {
                    seq = value.toString();
                }
                SpdyCodecUtil.validateHeaderValue(seq);
                return seq;
            }
        };
        SPDY_NAME_CONVERTER = new NameConverter<CharSequence>() {
            @Override
            public CharSequence convertName(CharSequence name) {
                if (name instanceof AsciiString) {
                    name = ((AsciiString)name).toLowerCase();
                }
                else {
                    name = name.toString().toLowerCase(Locale.US);
                }
                SpdyCodecUtil.validateHeaderName(name);
                return name;
            }
        };
    }
}
