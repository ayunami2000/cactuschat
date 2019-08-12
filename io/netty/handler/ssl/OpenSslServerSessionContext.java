// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.ssl;

import org.apache.tomcat.jni.SSLContext;

public final class OpenSslServerSessionContext extends OpenSslSessionContext
{
    OpenSslServerSessionContext(final long context) {
        super(context);
    }
    
    @Override
    public void setSessionTimeout(final int seconds) {
        if (seconds < 0) {
            throw new IllegalArgumentException();
        }
        SSLContext.setSessionCacheTimeout(this.context, (long)seconds);
    }
    
    @Override
    public int getSessionTimeout() {
        return (int)SSLContext.getSessionCacheTimeout(this.context);
    }
    
    @Override
    public void setSessionCacheSize(final int size) {
        if (size < 0) {
            throw new IllegalArgumentException();
        }
        SSLContext.setSessionCacheSize(this.context, (long)size);
    }
    
    @Override
    public int getSessionCacheSize() {
        return (int)SSLContext.getSessionCacheSize(this.context);
    }
    
    @Override
    public void setSessionCacheEnabled(final boolean enabled) {
        final long mode = enabled ? 2L : 0L;
        SSLContext.setSessionCacheMode(this.context, mode);
    }
    
    @Override
    public boolean isSessionCacheEnabled() {
        return SSLContext.getSessionCacheMode(this.context) == 2L;
    }
    
    public boolean setSessionIdContext(final byte[] sidCtx) {
        return SSLContext.setSessionIdContext(this.context, sidCtx);
    }
}
