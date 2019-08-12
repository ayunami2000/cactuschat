// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.ssl;

import io.netty.util.internal.logging.InternalLoggerFactory;
import javax.net.ssl.SSLSessionContext;
import java.util.List;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import io.netty.buffer.ByteBuf;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;
import org.apache.tomcat.jni.CertificateVerifier;
import java.security.Key;
import io.netty.buffer.ByteBufInputStream;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.KeyFactory;
import java.security.cert.CertificateFactory;
import java.io.InputStream;
import java.security.KeyStore;
import org.apache.tomcat.jni.SSL;
import org.apache.tomcat.jni.SSLContext;
import io.netty.util.internal.ObjectUtil;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.SSLException;
import java.io.File;
import io.netty.util.internal.logging.InternalLogger;

public final class OpenSslServerContext extends OpenSslContext
{
    private static final InternalLogger logger;
    private final OpenSslServerSessionContext sessionContext;
    
    public OpenSslServerContext(final File certChainFile, final File keyFile) throws SSLException {
        this(certChainFile, keyFile, null);
    }
    
    public OpenSslServerContext(final File certChainFile, final File keyFile, final String keyPassword) throws SSLException {
        this(certChainFile, keyFile, keyPassword, null, null, OpenSslDefaultApplicationProtocolNegotiator.INSTANCE, 0L, 0L);
    }
    
    public OpenSslServerContext(final File certChainFile, final File keyFile, final String keyPassword, final Iterable<String> ciphers, final ApplicationProtocolConfig apn, final long sessionCacheSize, final long sessionTimeout) throws SSLException {
        this(certChainFile, keyFile, keyPassword, null, ciphers, OpenSslContext.toNegotiator(apn, false), sessionCacheSize, sessionTimeout);
    }
    
    public OpenSslServerContext(final File certChainFile, final File keyFile, final String keyPassword, final TrustManagerFactory trustManagerFactory, final Iterable<String> ciphers, final ApplicationProtocolConfig config, final long sessionCacheSize, final long sessionTimeout) throws SSLException {
        this(certChainFile, keyFile, keyPassword, trustManagerFactory, ciphers, OpenSslContext.toNegotiator(config, true), sessionCacheSize, sessionTimeout);
    }
    
    public OpenSslServerContext(final File certChainFile, final File keyFile, String keyPassword, TrustManagerFactory trustManagerFactory, final Iterable<String> ciphers, final OpenSslApplicationProtocolNegotiator apn, final long sessionCacheSize, final long sessionTimeout) throws SSLException {
        super(ciphers, apn, sessionCacheSize, sessionTimeout, 1);
        OpenSsl.ensureAvailability();
        ObjectUtil.checkNotNull(certChainFile, "certChainFile");
        if (!certChainFile.isFile()) {
            throw new IllegalArgumentException("certChainFile is not a file: " + certChainFile);
        }
        ObjectUtil.checkNotNull(keyFile, "keyFile");
        if (!keyFile.isFile()) {
            throw new IllegalArgumentException("keyPath is not a file: " + keyFile);
        }
        if (keyPassword == null) {
            keyPassword = "";
        }
        boolean success = false;
        try {
            synchronized (OpenSslContext.class) {
                SSLContext.setVerify(this.ctx, 0, 10);
                if (!SSLContext.setCertificateChainFile(this.ctx, certChainFile.getPath(), true)) {
                    final long error = SSL.getLastErrorNumber();
                    if (OpenSsl.isError(error)) {
                        final String err = SSL.getErrorString(error);
                        throw new SSLException("failed to set certificate chain: " + certChainFile + " (" + err + ')');
                    }
                }
                try {
                    if (!SSLContext.setCertificate(this.ctx, certChainFile.getPath(), keyFile.getPath(), keyPassword, 0)) {
                        final long error = SSL.getLastErrorNumber();
                        if (OpenSsl.isError(error)) {
                            final String err = SSL.getErrorString(error);
                            throw new SSLException("failed to set certificate: " + certChainFile + " and " + keyFile + " (" + err + ')');
                        }
                    }
                }
                catch (SSLException e) {
                    throw e;
                }
                catch (Exception e2) {
                    throw new SSLException("failed to set certificate: " + certChainFile + " and " + keyFile, e2);
                }
                try {
                    final KeyStore ks = KeyStore.getInstance("JKS");
                    ks.load(null, null);
                    final CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    final KeyFactory rsaKF = KeyFactory.getInstance("RSA");
                    final KeyFactory dsaKF = KeyFactory.getInstance("DSA");
                    final ByteBuf encodedKeyBuf = PemReader.readPrivateKey(keyFile);
                    final byte[] encodedKey = new byte[encodedKeyBuf.readableBytes()];
                    encodedKeyBuf.readBytes(encodedKey).release();
                    final char[] keyPasswordChars = keyPassword.toCharArray();
                    final PKCS8EncodedKeySpec encodedKeySpec = SslContext.generateKeySpec(keyPasswordChars, encodedKey);
                    PrivateKey key;
                    try {
                        key = rsaKF.generatePrivate(encodedKeySpec);
                    }
                    catch (InvalidKeySpecException ignore) {
                        key = dsaKF.generatePrivate(encodedKeySpec);
                    }
                    final List<Certificate> certChain = new ArrayList<Certificate>();
                    final ByteBuf[] certs = PemReader.readCertificates(certChainFile);
                    try {
                        for (final ByteBuf buf : certs) {
                            certChain.add(cf.generateCertificate(new ByteBufInputStream(buf)));
                        }
                    }
                    finally {
                        for (final ByteBuf buf2 : certs) {
                            buf2.release();
                        }
                    }
                    ks.setKeyEntry("key", key, keyPasswordChars, certChain.toArray(new Certificate[certChain.size()]));
                    if (trustManagerFactory == null) {
                        trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                        trustManagerFactory.init((KeyStore)null);
                    }
                    else {
                        trustManagerFactory.init(ks);
                    }
                    final X509TrustManager manager = OpenSslContext.chooseTrustManager(trustManagerFactory.getTrustManagers());
                    SSLContext.setCertVerifyCallback(this.ctx, (CertificateVerifier)new CertificateVerifier() {
                        public boolean verify(final long ssl, final byte[][] chain, final String auth) {
                            final X509Certificate[] peerCerts = OpenSslContext.certificates(chain);
                            try {
                                manager.checkClientTrusted(peerCerts, auth);
                                return true;
                            }
                            catch (Exception e) {
                                OpenSslServerContext.logger.debug("verification of certificate failed", e);
                                return false;
                            }
                        }
                    });
                }
                catch (Exception e2) {
                    throw new SSLException("unable to setup trustmanager", e2);
                }
            }
            this.sessionContext = new OpenSslServerSessionContext(this.ctx);
            success = true;
        }
        finally {
            if (!success) {
                this.destroyPools();
            }
        }
    }
    
    @Override
    public OpenSslServerSessionContext sessionContext() {
        return this.sessionContext;
    }
    
    static {
        logger = InternalLoggerFactory.getInstance(OpenSslServerContext.class);
    }
}
