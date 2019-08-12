// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec;

public interface TextHeaders extends ConvertibleHeaders<CharSequence, String>
{
    boolean contains(final CharSequence p0, final CharSequence p1, final boolean p2);
    
    boolean containsObject(final CharSequence p0, final Object p1, final boolean p2);
    
    TextHeaders add(final CharSequence p0, final CharSequence p1);
    
    TextHeaders add(final CharSequence p0, final Iterable<? extends CharSequence> p1);
    
    TextHeaders add(final CharSequence p0, final CharSequence... p1);
    
    TextHeaders addObject(final CharSequence p0, final Object p1);
    
    TextHeaders addObject(final CharSequence p0, final Iterable<?> p1);
    
    TextHeaders addObject(final CharSequence p0, final Object... p1);
    
    TextHeaders addBoolean(final CharSequence p0, final boolean p1);
    
    TextHeaders addByte(final CharSequence p0, final byte p1);
    
    TextHeaders addChar(final CharSequence p0, final char p1);
    
    TextHeaders addShort(final CharSequence p0, final short p1);
    
    TextHeaders addInt(final CharSequence p0, final int p1);
    
    TextHeaders addLong(final CharSequence p0, final long p1);
    
    TextHeaders addFloat(final CharSequence p0, final float p1);
    
    TextHeaders addDouble(final CharSequence p0, final double p1);
    
    TextHeaders addTimeMillis(final CharSequence p0, final long p1);
    
    TextHeaders add(final TextHeaders p0);
    
    TextHeaders set(final CharSequence p0, final CharSequence p1);
    
    TextHeaders set(final CharSequence p0, final Iterable<? extends CharSequence> p1);
    
    TextHeaders set(final CharSequence p0, final CharSequence... p1);
    
    TextHeaders setObject(final CharSequence p0, final Object p1);
    
    TextHeaders setObject(final CharSequence p0, final Iterable<?> p1);
    
    TextHeaders setObject(final CharSequence p0, final Object... p1);
    
    TextHeaders setBoolean(final CharSequence p0, final boolean p1);
    
    TextHeaders setByte(final CharSequence p0, final byte p1);
    
    TextHeaders setChar(final CharSequence p0, final char p1);
    
    TextHeaders setShort(final CharSequence p0, final short p1);
    
    TextHeaders setInt(final CharSequence p0, final int p1);
    
    TextHeaders setLong(final CharSequence p0, final long p1);
    
    TextHeaders setFloat(final CharSequence p0, final float p1);
    
    TextHeaders setDouble(final CharSequence p0, final double p1);
    
    TextHeaders setTimeMillis(final CharSequence p0, final long p1);
    
    TextHeaders set(final TextHeaders p0);
    
    TextHeaders setAll(final TextHeaders p0);
    
    TextHeaders clear();
    
    public interface NameVisitor extends Headers.NameVisitor<CharSequence>
    {
    }
    
    public interface EntryVisitor extends Headers.EntryVisitor<CharSequence>
    {
    }
}
