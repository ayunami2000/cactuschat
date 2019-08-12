// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec;

public class EmptyTextHeaders extends EmptyConvertibleHeaders<CharSequence, String> implements TextHeaders
{
    protected EmptyTextHeaders() {
    }
    
    @Override
    public boolean contains(final CharSequence name, final CharSequence value, final boolean ignoreCase) {
        return false;
    }
    
    @Override
    public boolean containsObject(final CharSequence name, final Object value, final boolean ignoreCase) {
        return false;
    }
    
    @Override
    public TextHeaders add(final CharSequence name, final CharSequence value) {
        super.add((UnconvertedType)name, (UnconvertedType)value);
        return this;
    }
    
    @Override
    public TextHeaders add(final CharSequence name, final Iterable<? extends CharSequence> values) {
        super.add((UnconvertedType)name, (Iterable<? extends UnconvertedType>)values);
        return this;
    }
    
    @Override
    public TextHeaders add(final CharSequence name, final CharSequence... values) {
        super.add((UnconvertedType)name, (UnconvertedType[])values);
        return this;
    }
    
    @Override
    public TextHeaders addObject(final CharSequence name, final Object value) {
        super.addObject((UnconvertedType)name, value);
        return this;
    }
    
    @Override
    public TextHeaders addObject(final CharSequence name, final Iterable<?> values) {
        super.addObject((UnconvertedType)name, values);
        return this;
    }
    
    @Override
    public TextHeaders addObject(final CharSequence name, final Object... values) {
        super.addObject((UnconvertedType)name, values);
        return this;
    }
    
    @Override
    public TextHeaders addBoolean(final CharSequence name, final boolean value) {
        super.addBoolean((UnconvertedType)name, value);
        return this;
    }
    
    @Override
    public TextHeaders addChar(final CharSequence name, final char value) {
        super.addChar((UnconvertedType)name, value);
        return this;
    }
    
    @Override
    public TextHeaders addByte(final CharSequence name, final byte value) {
        super.addByte((UnconvertedType)name, value);
        return this;
    }
    
    @Override
    public TextHeaders addShort(final CharSequence name, final short value) {
        super.addShort((UnconvertedType)name, value);
        return this;
    }
    
    @Override
    public TextHeaders addInt(final CharSequence name, final int value) {
        super.addInt((UnconvertedType)name, value);
        return this;
    }
    
    @Override
    public TextHeaders addLong(final CharSequence name, final long value) {
        super.addLong((UnconvertedType)name, value);
        return this;
    }
    
    @Override
    public TextHeaders addFloat(final CharSequence name, final float value) {
        super.addFloat((UnconvertedType)name, value);
        return this;
    }
    
    @Override
    public TextHeaders addDouble(final CharSequence name, final double value) {
        super.addDouble((UnconvertedType)name, value);
        return this;
    }
    
    @Override
    public TextHeaders addTimeMillis(final CharSequence name, final long value) {
        super.addTimeMillis((UnconvertedType)name, value);
        return this;
    }
    
    @Override
    public TextHeaders add(final TextHeaders headers) {
        super.add((Headers<UnconvertedType>)headers);
        return this;
    }
    
    @Override
    public TextHeaders set(final CharSequence name, final CharSequence value) {
        super.set((UnconvertedType)name, (UnconvertedType)value);
        return this;
    }
    
    @Override
    public TextHeaders set(final CharSequence name, final Iterable<? extends CharSequence> values) {
        super.set((UnconvertedType)name, (Iterable<? extends UnconvertedType>)values);
        return this;
    }
    
    @Override
    public TextHeaders set(final CharSequence name, final CharSequence... values) {
        super.set((UnconvertedType)name, (UnconvertedType[])values);
        return this;
    }
    
    @Override
    public TextHeaders setObject(final CharSequence name, final Object value) {
        super.setObject((UnconvertedType)name, value);
        return this;
    }
    
    @Override
    public TextHeaders setObject(final CharSequence name, final Iterable<?> values) {
        super.setObject((UnconvertedType)name, values);
        return this;
    }
    
    @Override
    public TextHeaders setObject(final CharSequence name, final Object... values) {
        super.setObject((UnconvertedType)name, values);
        return this;
    }
    
    @Override
    public TextHeaders setBoolean(final CharSequence name, final boolean value) {
        super.setBoolean((UnconvertedType)name, value);
        return this;
    }
    
    @Override
    public TextHeaders setChar(final CharSequence name, final char value) {
        super.setChar((UnconvertedType)name, value);
        return this;
    }
    
    @Override
    public TextHeaders setByte(final CharSequence name, final byte value) {
        super.setByte((UnconvertedType)name, value);
        return this;
    }
    
    @Override
    public TextHeaders setShort(final CharSequence name, final short value) {
        super.setShort((UnconvertedType)name, value);
        return this;
    }
    
    @Override
    public TextHeaders setInt(final CharSequence name, final int value) {
        super.setInt((UnconvertedType)name, value);
        return this;
    }
    
    @Override
    public TextHeaders setLong(final CharSequence name, final long value) {
        super.setLong((UnconvertedType)name, value);
        return this;
    }
    
    @Override
    public TextHeaders setFloat(final CharSequence name, final float value) {
        super.setFloat((UnconvertedType)name, value);
        return this;
    }
    
    @Override
    public TextHeaders setDouble(final CharSequence name, final double value) {
        super.setDouble((UnconvertedType)name, value);
        return this;
    }
    
    @Override
    public TextHeaders setTimeMillis(final CharSequence name, final long value) {
        super.setTimeMillis((UnconvertedType)name, value);
        return this;
    }
    
    @Override
    public TextHeaders set(final TextHeaders headers) {
        super.set((Headers<UnconvertedType>)headers);
        return this;
    }
    
    @Override
    public TextHeaders setAll(final TextHeaders headers) {
        super.setAll((Headers<UnconvertedType>)headers);
        return this;
    }
    
    @Override
    public TextHeaders clear() {
        super.clear();
        return this;
    }
}
