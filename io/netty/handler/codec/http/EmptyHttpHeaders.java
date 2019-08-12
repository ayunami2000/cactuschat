// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http;

import io.netty.handler.codec.Headers;
import io.netty.handler.codec.TextHeaders;
import io.netty.handler.codec.EmptyTextHeaders;

public class EmptyHttpHeaders extends EmptyTextHeaders implements HttpHeaders
{
    public static final EmptyHttpHeaders INSTANCE;
    
    protected EmptyHttpHeaders() {
    }
    
    @Override
    public HttpHeaders add(final CharSequence name, final CharSequence value) {
        super.add(name, value);
        return this;
    }
    
    @Override
    public HttpHeaders add(final CharSequence name, final Iterable<? extends CharSequence> values) {
        super.add(name, values);
        return this;
    }
    
    @Override
    public HttpHeaders add(final CharSequence name, final CharSequence... values) {
        super.add(name, values);
        return this;
    }
    
    @Override
    public HttpHeaders addObject(final CharSequence name, final Object value) {
        super.addObject(name, value);
        return this;
    }
    
    @Override
    public HttpHeaders addObject(final CharSequence name, final Iterable<?> values) {
        super.addObject(name, values);
        return this;
    }
    
    @Override
    public HttpHeaders addObject(final CharSequence name, final Object... values) {
        super.addObject(name, values);
        return this;
    }
    
    @Override
    public HttpHeaders addBoolean(final CharSequence name, final boolean value) {
        super.addBoolean(name, value);
        return this;
    }
    
    @Override
    public HttpHeaders addChar(final CharSequence name, final char value) {
        super.addChar(name, value);
        return this;
    }
    
    @Override
    public HttpHeaders addByte(final CharSequence name, final byte value) {
        super.addByte(name, value);
        return this;
    }
    
    @Override
    public HttpHeaders addShort(final CharSequence name, final short value) {
        super.addShort(name, value);
        return this;
    }
    
    @Override
    public HttpHeaders addInt(final CharSequence name, final int value) {
        super.addInt(name, value);
        return this;
    }
    
    @Override
    public HttpHeaders addLong(final CharSequence name, final long value) {
        super.addLong(name, value);
        return this;
    }
    
    @Override
    public HttpHeaders addFloat(final CharSequence name, final float value) {
        super.addFloat(name, value);
        return this;
    }
    
    @Override
    public HttpHeaders addDouble(final CharSequence name, final double value) {
        super.addDouble(name, value);
        return this;
    }
    
    @Override
    public HttpHeaders addTimeMillis(final CharSequence name, final long value) {
        super.addTimeMillis(name, value);
        return this;
    }
    
    @Override
    public HttpHeaders add(final TextHeaders headers) {
        super.add(headers);
        return this;
    }
    
    @Override
    public HttpHeaders set(final CharSequence name, final CharSequence value) {
        super.set(name, value);
        return this;
    }
    
    @Override
    public HttpHeaders set(final CharSequence name, final Iterable<? extends CharSequence> values) {
        super.set(name, values);
        return this;
    }
    
    @Override
    public HttpHeaders set(final CharSequence name, final CharSequence... values) {
        super.set(name, values);
        return this;
    }
    
    @Override
    public HttpHeaders setObject(final CharSequence name, final Object value) {
        super.setObject(name, value);
        return this;
    }
    
    @Override
    public HttpHeaders setObject(final CharSequence name, final Iterable<?> values) {
        super.setObject(name, values);
        return this;
    }
    
    @Override
    public HttpHeaders setObject(final CharSequence name, final Object... values) {
        super.setObject(name, values);
        return this;
    }
    
    @Override
    public HttpHeaders setBoolean(final CharSequence name, final boolean value) {
        super.setBoolean(name, value);
        return this;
    }
    
    @Override
    public HttpHeaders setChar(final CharSequence name, final char value) {
        super.setChar(name, value);
        return this;
    }
    
    @Override
    public HttpHeaders setByte(final CharSequence name, final byte value) {
        super.setByte(name, value);
        return this;
    }
    
    @Override
    public HttpHeaders setShort(final CharSequence name, final short value) {
        super.setShort(name, value);
        return this;
    }
    
    @Override
    public HttpHeaders setInt(final CharSequence name, final int value) {
        super.setInt(name, value);
        return this;
    }
    
    @Override
    public HttpHeaders setLong(final CharSequence name, final long value) {
        super.setLong(name, value);
        return this;
    }
    
    @Override
    public HttpHeaders setFloat(final CharSequence name, final float value) {
        super.setFloat(name, value);
        return this;
    }
    
    @Override
    public HttpHeaders setDouble(final CharSequence name, final double value) {
        super.setDouble(name, value);
        return this;
    }
    
    @Override
    public HttpHeaders setTimeMillis(final CharSequence name, final long value) {
        super.setTimeMillis(name, value);
        return this;
    }
    
    @Override
    public HttpHeaders set(final TextHeaders headers) {
        super.set(headers);
        return this;
    }
    
    @Override
    public HttpHeaders setAll(final TextHeaders headers) {
        super.setAll(headers);
        return this;
    }
    
    @Override
    public HttpHeaders clear() {
        super.clear();
        return this;
    }
    
    static {
        INSTANCE = new EmptyHttpHeaders();
    }
}
