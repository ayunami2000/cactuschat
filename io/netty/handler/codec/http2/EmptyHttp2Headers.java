// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http2;

import io.netty.handler.codec.Headers;
import io.netty.handler.codec.BinaryHeaders;
import io.netty.handler.codec.AsciiString;
import io.netty.handler.codec.EmptyBinaryHeaders;

public final class EmptyHttp2Headers extends EmptyBinaryHeaders implements Http2Headers
{
    public static final EmptyHttp2Headers INSTANCE;
    
    private EmptyHttp2Headers() {
    }
    
    @Override
    public Http2Headers add(final AsciiString name, final AsciiString value) {
        super.add(name, value);
        return this;
    }
    
    @Override
    public Http2Headers add(final AsciiString name, final Iterable<? extends AsciiString> values) {
        super.add(name, values);
        return this;
    }
    
    @Override
    public Http2Headers add(final AsciiString name, final AsciiString... values) {
        super.add(name, values);
        return this;
    }
    
    @Override
    public Http2Headers addObject(final AsciiString name, final Object value) {
        super.addObject(name, value);
        return this;
    }
    
    @Override
    public Http2Headers addObject(final AsciiString name, final Iterable<?> values) {
        super.addObject(name, values);
        return this;
    }
    
    @Override
    public Http2Headers addObject(final AsciiString name, final Object... values) {
        super.addObject(name, values);
        return this;
    }
    
    @Override
    public Http2Headers addBoolean(final AsciiString name, final boolean value) {
        super.addBoolean(name, value);
        return this;
    }
    
    @Override
    public Http2Headers addChar(final AsciiString name, final char value) {
        super.addChar(name, value);
        return this;
    }
    
    @Override
    public Http2Headers addByte(final AsciiString name, final byte value) {
        super.addByte(name, value);
        return this;
    }
    
    @Override
    public Http2Headers addShort(final AsciiString name, final short value) {
        super.addShort(name, value);
        return this;
    }
    
    @Override
    public Http2Headers addInt(final AsciiString name, final int value) {
        super.addInt(name, value);
        return this;
    }
    
    @Override
    public Http2Headers addLong(final AsciiString name, final long value) {
        super.addLong(name, value);
        return this;
    }
    
    @Override
    public Http2Headers addFloat(final AsciiString name, final float value) {
        super.addFloat(name, value);
        return this;
    }
    
    @Override
    public Http2Headers addDouble(final AsciiString name, final double value) {
        super.addDouble(name, value);
        return this;
    }
    
    @Override
    public Http2Headers addTimeMillis(final AsciiString name, final long value) {
        super.addTimeMillis(name, value);
        return this;
    }
    
    @Override
    public Http2Headers add(final BinaryHeaders headers) {
        super.add(headers);
        return this;
    }
    
    @Override
    public Http2Headers set(final AsciiString name, final AsciiString value) {
        super.set(name, value);
        return this;
    }
    
    @Override
    public Http2Headers set(final AsciiString name, final Iterable<? extends AsciiString> values) {
        super.set(name, values);
        return this;
    }
    
    @Override
    public Http2Headers set(final AsciiString name, final AsciiString... values) {
        super.set(name, values);
        return this;
    }
    
    @Override
    public Http2Headers setObject(final AsciiString name, final Object value) {
        super.setObject(name, value);
        return this;
    }
    
    @Override
    public Http2Headers setObject(final AsciiString name, final Iterable<?> values) {
        super.setObject(name, values);
        return this;
    }
    
    @Override
    public Http2Headers setObject(final AsciiString name, final Object... values) {
        super.setObject(name, values);
        return this;
    }
    
    @Override
    public Http2Headers setBoolean(final AsciiString name, final boolean value) {
        super.setBoolean(name, value);
        return this;
    }
    
    @Override
    public Http2Headers setChar(final AsciiString name, final char value) {
        super.setChar(name, value);
        return this;
    }
    
    @Override
    public Http2Headers setByte(final AsciiString name, final byte value) {
        super.setByte(name, value);
        return this;
    }
    
    @Override
    public Http2Headers setShort(final AsciiString name, final short value) {
        super.setShort(name, value);
        return this;
    }
    
    @Override
    public Http2Headers setInt(final AsciiString name, final int value) {
        super.setInt(name, value);
        return this;
    }
    
    @Override
    public Http2Headers setLong(final AsciiString name, final long value) {
        super.setLong(name, value);
        return this;
    }
    
    @Override
    public Http2Headers setFloat(final AsciiString name, final float value) {
        super.setFloat(name, value);
        return this;
    }
    
    @Override
    public Http2Headers setDouble(final AsciiString name, final double value) {
        super.setDouble(name, value);
        return this;
    }
    
    @Override
    public Http2Headers setTimeMillis(final AsciiString name, final long value) {
        super.setTimeMillis(name, value);
        return this;
    }
    
    @Override
    public Http2Headers set(final BinaryHeaders headers) {
        super.set(headers);
        return this;
    }
    
    @Override
    public Http2Headers setAll(final BinaryHeaders headers) {
        super.setAll(headers);
        return this;
    }
    
    @Override
    public Http2Headers clear() {
        super.clear();
        return this;
    }
    
    @Override
    public EmptyHttp2Headers method(final AsciiString method) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public EmptyHttp2Headers scheme(final AsciiString status) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public EmptyHttp2Headers authority(final AsciiString authority) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public EmptyHttp2Headers path(final AsciiString path) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public EmptyHttp2Headers status(final AsciiString status) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public AsciiString method() {
        return this.get(PseudoHeaderName.METHOD.value());
    }
    
    @Override
    public AsciiString scheme() {
        return this.get(PseudoHeaderName.SCHEME.value());
    }
    
    @Override
    public AsciiString authority() {
        return this.get(PseudoHeaderName.AUTHORITY.value());
    }
    
    @Override
    public AsciiString path() {
        return this.get(PseudoHeaderName.PATH.value());
    }
    
    @Override
    public AsciiString status() {
        return this.get(PseudoHeaderName.STATUS.value());
    }
    
    static {
        INSTANCE = new EmptyHttp2Headers();
    }
}
