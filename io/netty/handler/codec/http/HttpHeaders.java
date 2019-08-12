// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http;

import io.netty.handler.codec.TextHeaders;

public interface HttpHeaders extends TextHeaders
{
    HttpHeaders add(final CharSequence p0, final CharSequence p1);
    
    HttpHeaders add(final CharSequence p0, final Iterable<? extends CharSequence> p1);
    
    HttpHeaders add(final CharSequence p0, final CharSequence... p1);
    
    HttpHeaders addObject(final CharSequence p0, final Object p1);
    
    HttpHeaders addObject(final CharSequence p0, final Iterable<?> p1);
    
    HttpHeaders addObject(final CharSequence p0, final Object... p1);
    
    HttpHeaders addBoolean(final CharSequence p0, final boolean p1);
    
    HttpHeaders addByte(final CharSequence p0, final byte p1);
    
    HttpHeaders addChar(final CharSequence p0, final char p1);
    
    HttpHeaders addShort(final CharSequence p0, final short p1);
    
    HttpHeaders addInt(final CharSequence p0, final int p1);
    
    HttpHeaders addLong(final CharSequence p0, final long p1);
    
    HttpHeaders addFloat(final CharSequence p0, final float p1);
    
    HttpHeaders addDouble(final CharSequence p0, final double p1);
    
    HttpHeaders addTimeMillis(final CharSequence p0, final long p1);
    
    HttpHeaders add(final TextHeaders p0);
    
    HttpHeaders set(final CharSequence p0, final CharSequence p1);
    
    HttpHeaders set(final CharSequence p0, final Iterable<? extends CharSequence> p1);
    
    HttpHeaders set(final CharSequence p0, final CharSequence... p1);
    
    HttpHeaders setObject(final CharSequence p0, final Object p1);
    
    HttpHeaders setObject(final CharSequence p0, final Iterable<?> p1);
    
    HttpHeaders setObject(final CharSequence p0, final Object... p1);
    
    HttpHeaders setBoolean(final CharSequence p0, final boolean p1);
    
    HttpHeaders setByte(final CharSequence p0, final byte p1);
    
    HttpHeaders setChar(final CharSequence p0, final char p1);
    
    HttpHeaders setShort(final CharSequence p0, final short p1);
    
    HttpHeaders setInt(final CharSequence p0, final int p1);
    
    HttpHeaders setLong(final CharSequence p0, final long p1);
    
    HttpHeaders setFloat(final CharSequence p0, final float p1);
    
    HttpHeaders setDouble(final CharSequence p0, final double p1);
    
    HttpHeaders setTimeMillis(final CharSequence p0, final long p1);
    
    HttpHeaders set(final TextHeaders p0);
    
    HttpHeaders setAll(final TextHeaders p0);
    
    HttpHeaders clear();
}
