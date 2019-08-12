// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.ssl;

import javax.net.ssl.SSLSessionContext;
import java.security.SecureRandom;
import javax.net.ssl.TrustManager;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import javax.net.ssl.SSLContext;

public final class JdkSslClientContext extends JdkSslContext
{
    private final SSLContext ctx;
    
    public JdkSslClientContext() throws SSLException {
        this(null, null);
    }
    
    public JdkSslClientContext(final File certChainFile) throws SSLException {
        this(certChainFile, null);
    }
    
    public JdkSslClientContext(final TrustManagerFactory trustManagerFactory) throws SSLException {
        this(null, trustManagerFactory);
    }
    
    public JdkSslClientContext(final File certChainFile, final TrustManagerFactory trustManagerFactory) throws SSLException {
        this(certChainFile, trustManagerFactory, null, IdentityCipherSuiteFilter.INSTANCE, JdkDefaultApplicationProtocolNegotiator.INSTANCE, 0L, 0L);
    }
    
    public JdkSslClientContext(final File certChainFile, final TrustManagerFactory trustManagerFactory, final Iterable<String> ciphers, final CipherSuiteFilter cipherFilter, final ApplicationProtocolConfig apn, final long sessionCacheSize, final long sessionTimeout) throws SSLException {
        this(certChainFile, trustManagerFactory, ciphers, cipherFilter, JdkSslContext.toNegotiator(apn, false), sessionCacheSize, sessionTimeout);
    }
    
    public JdkSslClientContext(final File certChainFile, final TrustManagerFactory trustManagerFactory, final Iterable<String> ciphers, final CipherSuiteFilter cipherFilter, final JdkApplicationProtocolNegotiator apn, final long sessionCacheSize, final long sessionTimeout) throws SSLException {
        this(certChainFile, trustManagerFactory, null, null, null, null, ciphers, cipherFilter, apn, sessionCacheSize, sessionTimeout);
    }
    
    public JdkSslClientContext(final File trustCertChainFile, final TrustManagerFactory trustManagerFactory, final File keyCertChainFile, final File keyFile, final String keyPassword, final KeyManagerFactory keyManagerFactory, final Iterable<String> ciphers, final CipherSuiteFilter cipherFilter, final ApplicationProtocolConfig apn, final long sessionCacheSize, final long sessionTimeout) throws SSLException {
        this(trustCertChainFile, trustManagerFactory, keyCertChainFile, keyFile, keyPassword, keyManagerFactory, ciphers, cipherFilter, JdkSslContext.toNegotiator(apn, false), sessionCacheSize, sessionTimeout);
    }
    
    public JdkSslClientContext(final File trustCertChainFile, TrustManagerFactory trustManagerFactory, final File keyCertChainFile, final File keyFile, final String keyPassword, KeyManagerFactory keyManagerFactory, final Iterable<String> ciphers, final CipherSuiteFilter cipherFilter, final JdkApplicationProtocolNegotiator apn, final long sessionCacheSize, final long sessionTimeout) throws SSLException {
        super(ciphers, cipherFilter, apn);
        try {
            if (trustCertChainFile != null) {
                trustManagerFactory = JdkSslContext.buildTrustManagerFactory(trustCertChainFile, trustManagerFactory);
            }
            if (keyFile != null) {
                keyManagerFactory = JdkSslContext.buildKeyManagerFactory(keyCertChainFile, keyFile, keyPassword, keyManagerFactory);
            }
            (this.ctx = SSLContext.getInstance("TLS")).init((KeyManager[])((keyManagerFactory == null) ? null : keyManagerFactory.getKeyManagers()), (TrustManager[])((trustManagerFactory == null) ? null : trustManagerFactory.getTrustManagers()), null);
            final SSLSessionContext sessCtx = this.ctx.getClientSessionContext();
            if (sessionCacheSize > 0L) {
                sessCtx.setSessionCacheSize((int)Math.min(sessionCacheSize, 2147483647L));
            }
            if (sessionTimeout > 0L) {
                sessCtx.setSessionTimeout((int)Math.min(sessionTimeout, 2147483647L));
            }
        }
        catch (Exception e) {
            throw new SSLException("failed to initialize the client-side SSL context", e);
        }
    }
    
    @Override
    public boolean isClient() {
        return true;
    }
    
    @Override
    public SSLContext context() {
        return this.ctx;
    }
}
