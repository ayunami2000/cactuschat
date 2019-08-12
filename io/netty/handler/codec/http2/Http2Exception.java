// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http2;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

public class Http2Exception extends Exception
{
    private static final long serialVersionUID = -6943456574080986447L;
    private final Http2Error error;
    
    public Http2Exception(final Http2Error error) {
        this.error = error;
    }
    
    public Http2Exception(final Http2Error error, final String message) {
        super(message);
        this.error = error;
    }
    
    public Http2Exception(final Http2Error error, final String message, final Throwable cause) {
        super(message, cause);
        this.error = error;
    }
    
    public Http2Error error() {
        return this.error;
    }
    
    public static Http2Exception connectionError(final Http2Error error, final String fmt, final Object... args) {
        return new Http2Exception(error, String.format(fmt, args));
    }
    
    public static Http2Exception connectionError(final Http2Error error, final Throwable cause, final String fmt, final Object... args) {
        return new Http2Exception(error, String.format(fmt, args), cause);
    }
    
    public static Http2Exception streamError(final int id, final Http2Error error, final String fmt, final Object... args) {
        return (0 == id) ? connectionError(error, fmt, args) : new StreamException(id, error, String.format(fmt, args));
    }
    
    public static Http2Exception streamError(final int id, final Http2Error error, final Throwable cause, final String fmt, final Object... args) {
        return (0 == id) ? connectionError(error, cause, fmt, args) : new StreamException(id, error, String.format(fmt, args), cause);
    }
    
    public static boolean isStreamError(final Http2Exception e) {
        return e instanceof StreamException;
    }
    
    public static int streamId(final Http2Exception e) {
        return isStreamError(e) ? ((StreamException)e).streamId() : 0;
    }
    
    public static final class StreamException extends Http2Exception
    {
        private static final long serialVersionUID = 462766352505067095L;
        private final int streamId;
        
        StreamException(final int streamId, final Http2Error error, final String message) {
            super(error, message);
            this.streamId = streamId;
        }
        
        StreamException(final int streamId, final Http2Error error, final String message, final Throwable cause) {
            super(error, message, cause);
            this.streamId = streamId;
        }
        
        public int streamId() {
            return this.streamId;
        }
    }
    
    public static final class CompositeStreamException extends Http2Exception implements Iterable<StreamException>
    {
        private static final long serialVersionUID = -434398146294199889L;
        private final List<StreamException> exceptions;
        
        public CompositeStreamException(final Http2Error error, final int initialCapacity) {
            super(error);
            this.exceptions = new ArrayList<StreamException>(initialCapacity);
        }
        
        public void add(final StreamException e) {
            this.exceptions.add(e);
        }
        
        @Override
        public Iterator<StreamException> iterator() {
            return this.exceptions.iterator();
        }
    }
}
