// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec;

public class EmptyBinaryHeaders extends EmptyHeaders<AsciiString> implements BinaryHeaders
{
    protected EmptyBinaryHeaders() {
    }
    
    @Override
    public BinaryHeaders add(final AsciiString name, final AsciiString value) {
        super.add(name, value);
        return this;
    }
    
    @Override
    public BinaryHeaders add(final AsciiString name, final Iterable<? extends AsciiString> values) {
        super.add(name, values);
        return this;
    }
    
    @Override
    public BinaryHeaders add(final AsciiString name, final AsciiString... values) {
        super.add(name, values);
        return this;
    }
    
    @Override
    public BinaryHeaders addObject(final AsciiString name, final Object value) {
        super.addObject(name, value);
        return this;
    }
    
    @Override
    public BinaryHeaders addObject(final AsciiString name, final Iterable<?> values) {
        super.addObject(name, values);
        return this;
    }
    
    @Override
    public BinaryHeaders addObject(final AsciiString name, final Object... values) {
        super.addObject(name, values);
        return this;
    }
    
    @Override
    public BinaryHeaders addBoolean(final AsciiString name, final boolean value) {
        super.addBoolean(name, value);
        return this;
    }
    
    @Override
    public BinaryHeaders addChar(final AsciiString name, final char value) {
        super.addChar(name, value);
        return this;
    }
    
    @Override
    public BinaryHeaders addByte(final AsciiString name, final byte value) {
        super.addByte(name, value);
        return this;
    }
    
    @Override
    public BinaryHeaders addShort(final AsciiString name, final short value) {
        super.addShort(name, value);
        return this;
    }
    
    @Override
    public BinaryHeaders addInt(final AsciiString name, final int value) {
        super.addInt(name, value);
        return this;
    }
    
    @Override
    public BinaryHeaders addLong(final AsciiString name, final long value) {
        super.addLong(name, value);
        return this;
    }
    
    @Override
    public BinaryHeaders addFloat(final AsciiString name, final float value) {
        super.addFloat(name, value);
        return this;
    }
    
    @Override
    public BinaryHeaders addDouble(final AsciiString name, final double value) {
        super.addDouble(name, value);
        return this;
    }
    
    @Override
    public BinaryHeaders addTimeMillis(final AsciiString name, final long value) {
        super.addTimeMillis(name, value);
        return this;
    }
    
    @Override
    public BinaryHeaders add(final BinaryHeaders headers) {
        super.add(headers);
        return this;
    }
    
    @Override
    public BinaryHeaders set(final AsciiString name, final AsciiString value) {
        super.set(name, value);
        return this;
    }
    
    @Override
    public BinaryHeaders set(final AsciiString name, final Iterable<? extends AsciiString> values) {
        super.set(name, values);
        return this;
    }
    
    @Override
    public BinaryHeaders set(final AsciiString name, final AsciiString... values) {
        super.set(name, values);
        return this;
    }
    
    @Override
    public BinaryHeaders setObject(final AsciiString name, final Object value) {
        super.setObject(name, value);
        return this;
    }
    
    @Override
    public BinaryHeaders setObject(final AsciiString name, final Iterable<?> values) {
        super.setObject(name, values);
        return this;
    }
    
    @Override
    public BinaryHeaders setObject(final AsciiString name, final Object... values) {
        super.setObject(name, values);
        return this;
    }
    
    @Override
    public BinaryHeaders setBoolean(final AsciiString name, final boolean value) {
        super.setBoolean(name, value);
        return this;
    }
    
    @Override
    public BinaryHeaders setChar(final AsciiString name, final char value) {
        super.setChar(name, value);
        return this;
    }
    
    @Override
    public BinaryHeaders setByte(final AsciiString name, final byte value) {
        super.setByte(name, value);
        return this;
    }
    
    @Override
    public BinaryHeaders setShort(final AsciiString name, final short value) {
        super.setShort(name, value);
        return this;
    }
    
    @Override
    public BinaryHeaders setInt(final AsciiString name, final int value) {
        super.setInt(name, value);
        return this;
    }
    
    @Override
    public BinaryHeaders setLong(final AsciiString name, final long value) {
        super.setLong(name, value);
        return this;
    }
    
    @Override
    public BinaryHeaders setFloat(final AsciiString name, final float value) {
        super.setFloat(name, value);
        return this;
    }
    
    @Override
    public BinaryHeaders setDouble(final AsciiString name, final double value) {
        super.setDouble(name, value);
        return this;
    }
    
    @Override
    public BinaryHeaders setTimeMillis(final AsciiString name, final long value) {
        super.setTimeMillis(name, value);
        return this;
    }
    
    @Override
    public BinaryHeaders set(final BinaryHeaders headers) {
        super.set(headers);
        return this;
    }
    
    @Override
    public BinaryHeaders setAll(final BinaryHeaders headers) {
        super.setAll(headers);
        return this;
    }
    
    @Override
    public BinaryHeaders clear() {
        super.clear();
        return this;
    }
}
