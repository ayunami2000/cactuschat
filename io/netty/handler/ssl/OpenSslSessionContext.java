// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.ssl;

import java.util.NoSuchElementException;
import org.apache.tomcat.jni.SSLContext;
import javax.net.ssl.SSLSession;
import java.util.Enumeration;
import javax.net.ssl.SSLSessionContext;

public abstract class OpenSslSessionContext implements SSLSessionContext
{
    private static final Enumeration<byte[]> EMPTY;
    private final OpenSslSessionStats stats;
    final long context;
    
    OpenSslSessionContext(final long context) {
        this.context = context;
        this.stats = new OpenSslSessionStats(context);
    }
    
    @Override
    public SSLSession getSession(final byte[] bytes) {
        if (bytes == null) {
            throw new NullPointerException("bytes");
        }
        return null;
    }
    
    @Override
    public Enumeration<byte[]> getIds() {
        return OpenSslSessionContext.EMPTY;
    }
    
    public void setTicketKeys(final byte[] keys) {
        if (keys == null) {
            throw new NullPointerException("keys");
        }
        SSLContext.setSessionTicketKeys(this.context, keys);
    }
    
    public abstract void setSessionCacheEnabled(final boolean p0);
    
    public abstract boolean isSessionCacheEnabled();
    
    public OpenSslSessionStats stats() {
        return this.stats;
    }
    
    static {
        EMPTY = new EmptyEnumeration();
    }
    
    private static final class EmptyEnumeration implements Enumeration<byte[]>
    {
        @Override
        public boolean hasMoreElements() {
            return false;
        }
        
        @Override
        public byte[] nextElement() {
            throw new NoSuchElementException();
        }
    }
}
