// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.ssl;

import io.netty.util.internal.logging.InternalLoggerFactory;
import javax.net.ssl.SSLSessionContext;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.KeyStoreException;
import javax.security.auth.x500.X500Principal;
import io.netty.buffer.ByteBuf;
import java.security.cert.Certificate;
import io.netty.buffer.ByteBufInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;
import org.apache.tomcat.jni.CertificateVerifier;
import org.apache.tomcat.jni.SSL;
import org.apache.tomcat.jni.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import io.netty.util.internal.logging.InternalLogger;

public final class OpenSslClientContext extends OpenSslContext
{
    private static final InternalLogger logger;
    private final OpenSslSessionContext sessionContext;
    
    public OpenSslClientContext() throws SSLException {
        this(null, null, null, null, 0L, 0L);
    }
    
    public OpenSslClientContext(final File certChainFile) throws SSLException {
        this(certChainFile, null);
    }
    
    public OpenSslClientContext(final TrustManagerFactory trustManagerFactory) throws SSLException {
        this(null, trustManagerFactory);
    }
    
    public OpenSslClientContext(final File certChainFile, final TrustManagerFactory trustManagerFactory) throws SSLException {
        this(certChainFile, trustManagerFactory, null, null, 0L, 0L);
    }
    
    public OpenSslClientContext(final File certChainFile, TrustManagerFactory trustManagerFactory, final Iterable<String> ciphers, final ApplicationProtocolConfig apn, final long sessionCacheSize, final long sessionTimeout) throws SSLException {
        super(ciphers, apn, sessionCacheSize, sessionTimeout, 0);
        boolean success = false;
        try {
            if (certChainFile != null && !certChainFile.isFile()) {
                throw new IllegalArgumentException("certChainFile is not a file: " + certChainFile);
            }
            synchronized (OpenSslContext.class) {
                if (certChainFile != null && !SSLContext.setCertificateChainFile(this.ctx, certChainFile.getPath(), true)) {
                    final long error = SSL.getLastErrorNumber();
                    if (OpenSsl.isError(error)) {
                        throw new SSLException("failed to set certificate chain: " + certChainFile + " (" + SSL.getErrorString(error) + ')');
                    }
                }
                SSLContext.setVerify(this.ctx, 0, 10);
                try {
                    if (trustManagerFactory == null) {
                        trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                    }
                    initTrustManagerFactory(certChainFile, trustManagerFactory);
                    final X509TrustManager manager = OpenSslContext.chooseTrustManager(trustManagerFactory.getTrustManagers());
                    SSLContext.setCertVerifyCallback(this.ctx, (CertificateVerifier)new CertificateVerifier() {
                        public boolean verify(final long ssl, final byte[][] chain, final String auth) {
                            final X509Certificate[] peerCerts = OpenSslContext.certificates(chain);
                            try {
                                manager.checkServerTrusted(peerCerts, auth);
                                return true;
                            }
                            catch (Exception e) {
                                OpenSslClientContext.logger.debug("verification of certificate failed", e);
                                return false;
                            }
                        }
                    });
                }
                catch (Exception e) {
                    throw new SSLException("unable to setup trustmanager", e);
                }
            }
            this.sessionContext = new OpenSslClientSessionContext(this.ctx);
            success = true;
        }
        finally {
            if (!success) {
                this.destroyPools();
            }
        }
    }
    
    private static void initTrustManagerFactory(final File certChainFile, final TrustManagerFactory trustManagerFactory) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        final KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(null, null);
        if (certChainFile != null) {
            final ByteBuf[] certs = PemReader.readCertificates(certChainFile);
            try {
                for (final ByteBuf buf : certs) {
                    final X509Certificate cert = (X509Certificate)OpenSslClientContext.X509_CERT_FACTORY.generateCertificate(new ByteBufInputStream(buf));
                    final X500Principal principal = cert.getSubjectX500Principal();
                    ks.setCertificateEntry(principal.getName("RFC2253"), cert);
                }
            }
            finally {
                for (final ByteBuf buf2 : certs) {
                    buf2.release();
                }
            }
        }
        trustManagerFactory.init(ks);
    }
    
    @Override
    public OpenSslSessionContext sessionContext() {
        return this.sessionContext;
    }
    
    static {
        logger = InternalLoggerFactory.getInstance(OpenSslClientContext.class);
    }
    
    private static final class OpenSslClientSessionContext extends OpenSslSessionContext
    {
        private OpenSslClientSessionContext(final long context) {
            super(context);
        }
        
        @Override
        public void setSessionTimeout(final int seconds) {
            if (seconds < 0) {
                throw new IllegalArgumentException();
            }
        }
        
        @Override
        public int getSessionTimeout() {
            return 0;
        }
        
        @Override
        public void setSessionCacheSize(final int size) {
            if (size < 0) {
                throw new IllegalArgumentException();
            }
        }
        
        @Override
        public int getSessionCacheSize() {
            return 0;
        }
        
        @Override
        public void setSessionCacheEnabled(final boolean enabled) {
        }
        
        @Override
        public boolean isSessionCacheEnabled() {
            return false;
        }
    }
}
