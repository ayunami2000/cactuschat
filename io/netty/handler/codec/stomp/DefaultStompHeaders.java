// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.stomp;

import io.netty.handler.codec.Headers;
import io.netty.handler.codec.TextHeaders;
import io.netty.handler.codec.DefaultTextHeaders;

public class DefaultStompHeaders extends DefaultTextHeaders implements StompHeaders
{
    @Override
    public StompHeaders add(final CharSequence name, final CharSequence value) {
        super.add(name, value);
        return this;
    }
    
    @Override
    public StompHeaders add(final CharSequence name, final Iterable<? extends CharSequence> values) {
        super.add(name, values);
        return this;
    }
    
    @Override
    public StompHeaders add(final CharSequence name, final CharSequence... values) {
        super.add(name, values);
        return this;
    }
    
    @Override
    public StompHeaders addObject(final CharSequence name, final Object value) {
        super.addObject(name, value);
        return this;
    }
    
    @Override
    public StompHeaders addObject(final CharSequence name, final Iterable<?> values) {
        super.addObject(name, values);
        return this;
    }
    
    @Override
    public StompHeaders addObject(final CharSequence name, final Object... values) {
        super.addObject(name, values);
        return this;
    }
    
    @Override
    public StompHeaders addBoolean(final CharSequence name, final boolean value) {
        super.addBoolean(name, value);
        return this;
    }
    
    @Override
    public StompHeaders addChar(final CharSequence name, final char value) {
        super.addChar(name, value);
        return this;
    }
    
    @Override
    public StompHeaders addByte(final CharSequence name, final byte value) {
        super.addByte(name, value);
        return this;
    }
    
    @Override
    public StompHeaders addShort(final CharSequence name, final short value) {
        super.addShort(name, value);
        return this;
    }
    
    @Override
    public StompHeaders addInt(final CharSequence name, final int value) {
        super.addInt(name, value);
        return this;
    }
    
    @Override
    public StompHeaders addLong(final CharSequence name, final long value) {
        super.addLong(name, value);
        return this;
    }
    
    @Override
    public StompHeaders addFloat(final CharSequence name, final float value) {
        super.addFloat(name, value);
        return this;
    }
    
    @Override
    public StompHeaders addDouble(final CharSequence name, final double value) {
        super.addDouble(name, value);
        return this;
    }
    
    @Override
    public StompHeaders addTimeMillis(final CharSequence name, final long value) {
        super.addTimeMillis(name, value);
        return this;
    }
    
    @Override
    public StompHeaders add(final TextHeaders headers) {
        super.add(headers);
        return this;
    }
    
    @Override
    public StompHeaders set(final CharSequence name, final CharSequence value) {
        super.set(name, value);
        return this;
    }
    
    @Override
    public StompHeaders set(final CharSequence name, final Iterable<? extends CharSequence> values) {
        super.set(name, values);
        return this;
    }
    
    @Override
    public StompHeaders set(final CharSequence name, final CharSequence... values) {
        super.set(name, values);
        return this;
    }
    
    @Override
    public StompHeaders setObject(final CharSequence name, final Object value) {
        super.setObject(name, value);
        return this;
    }
    
    @Override
    public StompHeaders setObject(final CharSequence name, final Iterable<?> values) {
        super.setObject(name, values);
        return this;
    }
    
    @Override
    public StompHeaders setObject(final CharSequence name, final Object... values) {
        super.setObject(name, values);
        return this;
    }
    
    @Override
    public StompHeaders setBoolean(final CharSequence name, final boolean value) {
        super.setBoolean(name, value);
        return this;
    }
    
    @Override
    public StompHeaders setChar(final CharSequence name, final char value) {
        super.setChar(name, value);
        return this;
    }
    
    @Override
    public StompHeaders setByte(final CharSequence name, final byte value) {
        super.setByte(name, value);
        return this;
    }
    
    @Override
    public StompHeaders setShort(final CharSequence name, final short value) {
        super.setShort(name, value);
        return this;
    }
    
    @Override
    public StompHeaders setInt(final CharSequence name, final int value) {
        super.setInt(name, value);
        return this;
    }
    
    @Override
    public StompHeaders setLong(final CharSequence name, final long value) {
        super.setLong(name, value);
        return this;
    }
    
    @Override
    public StompHeaders setFloat(final CharSequence name, final float value) {
        super.setFloat(name, value);
        return this;
    }
    
    @Override
    public StompHeaders setDouble(final CharSequence name, final double value) {
        super.setDouble(name, value);
        return this;
    }
    
    @Override
    public StompHeaders setTimeMillis(final CharSequence name, final long value) {
        super.setTimeMillis(name, value);
        return this;
    }
    
    @Override
    public StompHeaders set(final TextHeaders headers) {
        super.set(headers);
        return this;
    }
    
    @Override
    public StompHeaders setAll(final TextHeaders headers) {
        super.setAll(headers);
        return this;
    }
    
    @Override
    public StompHeaders clear() {
        super.clear();
        return this;
    }
}
