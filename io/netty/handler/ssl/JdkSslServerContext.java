// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.ssl;

import javax.net.ssl.SSLSessionContext;
import java.security.SecureRandom;
import javax.net.ssl.TrustManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.SSLException;
import java.io.File;
import javax.net.ssl.SSLContext;

public final class JdkSslServerContext extends JdkSslContext
{
    private final SSLContext ctx;
    
    public JdkSslServerContext(final File certChainFile, final File keyFile) throws SSLException {
        this(certChainFile, keyFile, null);
    }
    
    public JdkSslServerContext(final File certChainFile, final File keyFile, final String keyPassword) throws SSLException {
        this(certChainFile, keyFile, keyPassword, null, IdentityCipherSuiteFilter.INSTANCE, JdkDefaultApplicationProtocolNegotiator.INSTANCE, 0L, 0L);
    }
    
    public JdkSslServerContext(final File certChainFile, final File keyFile, final String keyPassword, final Iterable<String> ciphers, final CipherSuiteFilter cipherFilter, final ApplicationProtocolConfig apn, final long sessionCacheSize, final long sessionTimeout) throws SSLException {
        this(certChainFile, keyFile, keyPassword, ciphers, cipherFilter, JdkSslContext.toNegotiator(apn, true), sessionCacheSize, sessionTimeout);
    }
    
    public JdkSslServerContext(final File certChainFile, final File keyFile, final String keyPassword, final Iterable<String> ciphers, final CipherSuiteFilter cipherFilter, final JdkApplicationProtocolNegotiator apn, final long sessionCacheSize, final long sessionTimeout) throws SSLException {
        this(null, null, certChainFile, keyFile, keyPassword, null, ciphers, cipherFilter, apn, sessionCacheSize, sessionTimeout);
    }
    
    public JdkSslServerContext(final File trustCertChainFile, final TrustManagerFactory trustManagerFactory, final File keyCertChainFile, final File keyFile, final String keyPassword, final KeyManagerFactory keyManagerFactory, final Iterable<String> ciphers, final CipherSuiteFilter cipherFilter, final ApplicationProtocolConfig apn, final long sessionCacheSize, final long sessionTimeout) throws SSLException {
        this(trustCertChainFile, trustManagerFactory, keyCertChainFile, keyFile, keyPassword, keyManagerFactory, ciphers, cipherFilter, JdkSslContext.toNegotiator(apn, true), sessionCacheSize, sessionTimeout);
    }
    
    public JdkSslServerContext(final File trustCertChainFile, TrustManagerFactory trustManagerFactory, final File keyCertChainFile, final File keyFile, final String keyPassword, KeyManagerFactory keyManagerFactory, final Iterable<String> ciphers, final CipherSuiteFilter cipherFilter, final JdkApplicationProtocolNegotiator apn, final long sessionCacheSize, final long sessionTimeout) throws SSLException {
        super(ciphers, cipherFilter, apn);
        if (keyFile == null && keyManagerFactory == null) {
            throw new NullPointerException("keyFile, keyManagerFactory");
        }
        try {
            if (trustCertChainFile != null) {
                trustManagerFactory = JdkSslContext.buildTrustManagerFactory(trustCertChainFile, trustManagerFactory);
            }
            if (keyFile != null) {
                keyManagerFactory = JdkSslContext.buildKeyManagerFactory(keyCertChainFile, keyFile, keyPassword, keyManagerFactory);
            }
            (this.ctx = SSLContext.getInstance("TLS")).init(keyManagerFactory.getKeyManagers(), (TrustManager[])((trustManagerFactory == null) ? null : trustManagerFactory.getTrustManagers()), null);
            final SSLSessionContext sessCtx = this.ctx.getServerSessionContext();
            if (sessionCacheSize > 0L) {
                sessCtx.setSessionCacheSize((int)Math.min(sessionCacheSize, 2147483647L));
            }
            if (sessionTimeout > 0L) {
                sessCtx.setSessionTimeout((int)Math.min(sessionTimeout, 2147483647L));
            }
        }
        catch (Exception e) {
            throw new SSLException("failed to initialize the server-side SSL context", e);
        }
    }
    
    @Override
    public boolean isClient() {
        return false;
    }
    
    @Override
    public SSLContext context() {
        return this.ctx;
    }
}
