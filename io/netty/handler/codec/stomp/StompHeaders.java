// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.stomp;

import io.netty.handler.codec.AsciiString;
import io.netty.handler.codec.TextHeaders;

public interface StompHeaders extends TextHeaders
{
    public static final AsciiString ACCEPT_VERSION = new AsciiString("accept-version");
    public static final AsciiString HOST = new AsciiString("host");
    public static final AsciiString LOGIN = new AsciiString("login");
    public static final AsciiString PASSCODE = new AsciiString("passcode");
    public static final AsciiString HEART_BEAT = new AsciiString("heart-beat");
    public static final AsciiString VERSION = new AsciiString("version");
    public static final AsciiString SESSION = new AsciiString("session");
    public static final AsciiString SERVER = new AsciiString("server");
    public static final AsciiString DESTINATION = new AsciiString("destination");
    public static final AsciiString ID = new AsciiString("id");
    public static final AsciiString ACK = new AsciiString("ack");
    public static final AsciiString TRANSACTION = new AsciiString("transaction");
    public static final AsciiString RECEIPT = new AsciiString("receipt");
    public static final AsciiString MESSAGE_ID = new AsciiString("message-id");
    public static final AsciiString SUBSCRIPTION = new AsciiString("subscription");
    public static final AsciiString RECEIPT_ID = new AsciiString("receipt-id");
    public static final AsciiString MESSAGE = new AsciiString("message");
    public static final AsciiString CONTENT_LENGTH = new AsciiString("content-length");
    public static final AsciiString CONTENT_TYPE = new AsciiString("content-type");
    
    StompHeaders add(final CharSequence p0, final CharSequence p1);
    
    StompHeaders add(final CharSequence p0, final Iterable<? extends CharSequence> p1);
    
    StompHeaders add(final CharSequence p0, final CharSequence... p1);
    
    StompHeaders addObject(final CharSequence p0, final Object p1);
    
    StompHeaders addObject(final CharSequence p0, final Iterable<?> p1);
    
    StompHeaders addObject(final CharSequence p0, final Object... p1);
    
    StompHeaders addBoolean(final CharSequence p0, final boolean p1);
    
    StompHeaders addByte(final CharSequence p0, final byte p1);
    
    StompHeaders addChar(final CharSequence p0, final char p1);
    
    StompHeaders addShort(final CharSequence p0, final short p1);
    
    StompHeaders addInt(final CharSequence p0, final int p1);
    
    StompHeaders addLong(final CharSequence p0, final long p1);
    
    StompHeaders addFloat(final CharSequence p0, final float p1);
    
    StompHeaders addDouble(final CharSequence p0, final double p1);
    
    StompHeaders addTimeMillis(final CharSequence p0, final long p1);
    
    StompHeaders add(final TextHeaders p0);
    
    StompHeaders set(final CharSequence p0, final CharSequence p1);
    
    StompHeaders set(final CharSequence p0, final Iterable<? extends CharSequence> p1);
    
    StompHeaders set(final CharSequence p0, final CharSequence... p1);
    
    StompHeaders setObject(final CharSequence p0, final Object p1);
    
    StompHeaders setObject(final CharSequence p0, final Iterable<?> p1);
    
    StompHeaders setObject(final CharSequence p0, final Object... p1);
    
    StompHeaders setBoolean(final CharSequence p0, final boolean p1);
    
    StompHeaders setByte(final CharSequence p0, final byte p1);
    
    StompHeaders setChar(final CharSequence p0, final char p1);
    
    StompHeaders setShort(final CharSequence p0, final short p1);
    
    StompHeaders setInt(final CharSequence p0, final int p1);
    
    StompHeaders setLong(final CharSequence p0, final long p1);
    
    StompHeaders setFloat(final CharSequence p0, final float p1);
    
    StompHeaders setDouble(final CharSequence p0, final double p1);
    
    StompHeaders setTimeMillis(final CharSequence p0, final long p1);
    
    StompHeaders set(final TextHeaders p0);
    
    StompHeaders setAll(final TextHeaders p0);
    
    StompHeaders clear();
}
