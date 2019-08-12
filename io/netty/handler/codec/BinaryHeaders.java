// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec;

public interface BinaryHeaders extends Headers<AsciiString>
{
    BinaryHeaders add(final AsciiString p0, final AsciiString p1);
    
    BinaryHeaders add(final AsciiString p0, final Iterable<? extends AsciiString> p1);
    
    BinaryHeaders add(final AsciiString p0, final AsciiString... p1);
    
    BinaryHeaders addObject(final AsciiString p0, final Object p1);
    
    BinaryHeaders addObject(final AsciiString p0, final Iterable<?> p1);
    
    BinaryHeaders addObject(final AsciiString p0, final Object... p1);
    
    BinaryHeaders addBoolean(final AsciiString p0, final boolean p1);
    
    BinaryHeaders addByte(final AsciiString p0, final byte p1);
    
    BinaryHeaders addChar(final AsciiString p0, final char p1);
    
    BinaryHeaders addShort(final AsciiString p0, final short p1);
    
    BinaryHeaders addInt(final AsciiString p0, final int p1);
    
    BinaryHeaders addLong(final AsciiString p0, final long p1);
    
    BinaryHeaders addFloat(final AsciiString p0, final float p1);
    
    BinaryHeaders addDouble(final AsciiString p0, final double p1);
    
    BinaryHeaders addTimeMillis(final AsciiString p0, final long p1);
    
    BinaryHeaders add(final BinaryHeaders p0);
    
    BinaryHeaders set(final AsciiString p0, final AsciiString p1);
    
    BinaryHeaders set(final AsciiString p0, final Iterable<? extends AsciiString> p1);
    
    BinaryHeaders set(final AsciiString p0, final AsciiString... p1);
    
    BinaryHeaders setObject(final AsciiString p0, final Object p1);
    
    BinaryHeaders setObject(final AsciiString p0, final Iterable<?> p1);
    
    BinaryHeaders setObject(final AsciiString p0, final Object... p1);
    
    BinaryHeaders setBoolean(final AsciiString p0, final boolean p1);
    
    BinaryHeaders setByte(final AsciiString p0, final byte p1);
    
    BinaryHeaders setChar(final AsciiString p0, final char p1);
    
    BinaryHeaders setShort(final AsciiString p0, final short p1);
    
    BinaryHeaders setInt(final AsciiString p0, final int p1);
    
    BinaryHeaders setLong(final AsciiString p0, final long p1);
    
    BinaryHeaders setFloat(final AsciiString p0, final float p1);
    
    BinaryHeaders setDouble(final AsciiString p0, final double p1);
    
    BinaryHeaders setTimeMillis(final AsciiString p0, final long p1);
    
    BinaryHeaders set(final BinaryHeaders p0);
    
    BinaryHeaders setAll(final BinaryHeaders p0);
    
    BinaryHeaders clear();
    
    public interface NameVisitor extends Headers.NameVisitor<AsciiString>
    {
    }
    
    public interface EntryVisitor extends Headers.EntryVisitor<AsciiString>
    {
    }
}
