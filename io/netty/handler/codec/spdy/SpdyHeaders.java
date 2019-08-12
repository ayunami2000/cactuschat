// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.spdy;

import io.netty.handler.codec.AsciiString;
import io.netty.handler.codec.TextHeaders;

public interface SpdyHeaders extends TextHeaders
{
    SpdyHeaders add(final CharSequence p0, final CharSequence p1);
    
    SpdyHeaders add(final CharSequence p0, final Iterable<? extends CharSequence> p1);
    
    SpdyHeaders add(final CharSequence p0, final CharSequence... p1);
    
    SpdyHeaders addObject(final CharSequence p0, final Object p1);
    
    SpdyHeaders addObject(final CharSequence p0, final Iterable<?> p1);
    
    SpdyHeaders addObject(final CharSequence p0, final Object... p1);
    
    SpdyHeaders addBoolean(final CharSequence p0, final boolean p1);
    
    SpdyHeaders addByte(final CharSequence p0, final byte p1);
    
    SpdyHeaders addChar(final CharSequence p0, final char p1);
    
    SpdyHeaders addShort(final CharSequence p0, final short p1);
    
    SpdyHeaders addInt(final CharSequence p0, final int p1);
    
    SpdyHeaders addLong(final CharSequence p0, final long p1);
    
    SpdyHeaders addFloat(final CharSequence p0, final float p1);
    
    SpdyHeaders addDouble(final CharSequence p0, final double p1);
    
    SpdyHeaders addTimeMillis(final CharSequence p0, final long p1);
    
    SpdyHeaders add(final TextHeaders p0);
    
    SpdyHeaders set(final CharSequence p0, final CharSequence p1);
    
    SpdyHeaders set(final CharSequence p0, final Iterable<? extends CharSequence> p1);
    
    SpdyHeaders set(final CharSequence p0, final CharSequence... p1);
    
    SpdyHeaders setBoolean(final CharSequence p0, final boolean p1);
    
    SpdyHeaders setByte(final CharSequence p0, final byte p1);
    
    SpdyHeaders setChar(final CharSequence p0, final char p1);
    
    SpdyHeaders setShort(final CharSequence p0, final short p1);
    
    SpdyHeaders setInt(final CharSequence p0, final int p1);
    
    SpdyHeaders setLong(final CharSequence p0, final long p1);
    
    SpdyHeaders setFloat(final CharSequence p0, final float p1);
    
    SpdyHeaders setDouble(final CharSequence p0, final double p1);
    
    SpdyHeaders setTimeMillis(final CharSequence p0, final long p1);
    
    SpdyHeaders setObject(final CharSequence p0, final Object p1);
    
    SpdyHeaders setObject(final CharSequence p0, final Iterable<?> p1);
    
    SpdyHeaders setObject(final CharSequence p0, final Object... p1);
    
    SpdyHeaders set(final TextHeaders p0);
    
    SpdyHeaders setAll(final TextHeaders p0);
    
    SpdyHeaders clear();
    
    public static final class HttpNames
    {
        public static final AsciiString HOST;
        public static final AsciiString METHOD;
        public static final AsciiString PATH;
        public static final AsciiString SCHEME;
        public static final AsciiString STATUS;
        public static final AsciiString VERSION;
        
        private HttpNames() {
        }
        
        static {
            HOST = new AsciiString(":host");
            METHOD = new AsciiString(":method");
            PATH = new AsciiString(":path");
            SCHEME = new AsciiString(":scheme");
            STATUS = new AsciiString(":status");
            VERSION = new AsciiString(":version");
        }
    }
}
