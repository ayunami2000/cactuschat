// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http2;

import java.util.HashSet;
import java.util.Set;
import io.netty.handler.codec.AsciiString;
import io.netty.handler.codec.BinaryHeaders;

public interface Http2Headers extends BinaryHeaders
{
    Http2Headers add(final AsciiString p0, final AsciiString p1);
    
    Http2Headers add(final AsciiString p0, final Iterable<? extends AsciiString> p1);
    
    Http2Headers add(final AsciiString p0, final AsciiString... p1);
    
    Http2Headers addObject(final AsciiString p0, final Object p1);
    
    Http2Headers addObject(final AsciiString p0, final Iterable<?> p1);
    
    Http2Headers addObject(final AsciiString p0, final Object... p1);
    
    Http2Headers addBoolean(final AsciiString p0, final boolean p1);
    
    Http2Headers addByte(final AsciiString p0, final byte p1);
    
    Http2Headers addChar(final AsciiString p0, final char p1);
    
    Http2Headers addShort(final AsciiString p0, final short p1);
    
    Http2Headers addInt(final AsciiString p0, final int p1);
    
    Http2Headers addLong(final AsciiString p0, final long p1);
    
    Http2Headers addFloat(final AsciiString p0, final float p1);
    
    Http2Headers addDouble(final AsciiString p0, final double p1);
    
    Http2Headers addTimeMillis(final AsciiString p0, final long p1);
    
    Http2Headers add(final BinaryHeaders p0);
    
    Http2Headers set(final AsciiString p0, final AsciiString p1);
    
    Http2Headers set(final AsciiString p0, final Iterable<? extends AsciiString> p1);
    
    Http2Headers set(final AsciiString p0, final AsciiString... p1);
    
    Http2Headers setObject(final AsciiString p0, final Object p1);
    
    Http2Headers setObject(final AsciiString p0, final Iterable<?> p1);
    
    Http2Headers setObject(final AsciiString p0, final Object... p1);
    
    Http2Headers setBoolean(final AsciiString p0, final boolean p1);
    
    Http2Headers setByte(final AsciiString p0, final byte p1);
    
    Http2Headers setChar(final AsciiString p0, final char p1);
    
    Http2Headers setShort(final AsciiString p0, final short p1);
    
    Http2Headers setInt(final AsciiString p0, final int p1);
    
    Http2Headers setLong(final AsciiString p0, final long p1);
    
    Http2Headers setFloat(final AsciiString p0, final float p1);
    
    Http2Headers setDouble(final AsciiString p0, final double p1);
    
    Http2Headers setTimeMillis(final AsciiString p0, final long p1);
    
    Http2Headers set(final BinaryHeaders p0);
    
    Http2Headers setAll(final BinaryHeaders p0);
    
    Http2Headers clear();
    
    Http2Headers method(final AsciiString p0);
    
    Http2Headers scheme(final AsciiString p0);
    
    Http2Headers authority(final AsciiString p0);
    
    Http2Headers path(final AsciiString p0);
    
    Http2Headers status(final AsciiString p0);
    
    AsciiString method();
    
    AsciiString scheme();
    
    AsciiString authority();
    
    AsciiString path();
    
    AsciiString status();
    
    public enum PseudoHeaderName
    {
        METHOD(":method"), 
        SCHEME(":scheme"), 
        AUTHORITY(":authority"), 
        PATH(":path"), 
        STATUS(":status");
        
        private final AsciiString value;
        private static final Set<AsciiString> PSEUDO_HEADERS;
        
        private PseudoHeaderName(final String value) {
            this.value = new AsciiString(value);
        }
        
        public AsciiString value() {
            return this.value;
        }
        
        public static boolean isPseudoHeader(final AsciiString header) {
            return PseudoHeaderName.PSEUDO_HEADERS.contains(header);
        }
        
        static {
            PSEUDO_HEADERS = new HashSet<AsciiString>();
            for (final PseudoHeaderName pseudoHeader : values()) {
                PseudoHeaderName.PSEUDO_HEADERS.add(pseudoHeader.value());
            }
        }
    }
}
