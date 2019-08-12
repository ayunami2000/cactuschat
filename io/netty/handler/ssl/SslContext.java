// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.ssl;

import java.security.cert.CertificateException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.NoSuchPaddingException;
import java.security.NoSuchAlgorithmException;
import java.io.IOException;
import javax.crypto.SecretKey;
import java.security.Key;
import javax.crypto.Cipher;
import java.security.spec.KeySpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.EncryptedPrivateKeyInfo;
import java.security.spec.PKCS8EncodedKeySpec;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.SSLEngine;
import io.netty.buffer.ByteBufAllocator;
import java.util.List;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.SSLException;
import java.io.File;
import java.security.cert.CertificateFactory;

public abstract class SslContext
{
    static final CertificateFactory X509_CERT_FACTORY;
    
    public static SslProvider defaultServerProvider() {
        return defaultProvider();
    }
    
    public static SslProvider defaultClientProvider() {
        return defaultProvider();
    }
    
    private static SslProvider defaultProvider() {
        if (OpenSsl.isAvailable()) {
            return SslProvider.OPENSSL;
        }
        return SslProvider.JDK;
    }
    
    public static SslContext newServerContext(final File certChainFile, final File keyFile) throws SSLException {
        return newServerContext(certChainFile, keyFile, null);
    }
    
    public static SslContext newServerContext(final File certChainFile, final File keyFile, final String keyPassword) throws SSLException {
        return newServerContext(null, certChainFile, keyFile, keyPassword);
    }
    
    public static SslContext newServerContext(final File certChainFile, final File keyFile, final String keyPassword, final Iterable<String> ciphers, final CipherSuiteFilter cipherFilter, final ApplicationProtocolConfig apn, final long sessionCacheSize, final long sessionTimeout) throws SSLException {
        return newServerContext(null, certChainFile, keyFile, keyPassword, ciphers, cipherFilter, apn, sessionCacheSize, sessionTimeout);
    }
    
    public static SslContext newServerContext(final SslProvider provider, final File certChainFile, final File keyFile) throws SSLException {
        return newServerContext(provider, certChainFile, keyFile, null);
    }
    
    public static SslContext newServerContext(final SslProvider provider, final File certChainFile, final File keyFile, final String keyPassword) throws SSLException {
        return newServerContext(provider, certChainFile, keyFile, keyPassword, null, IdentityCipherSuiteFilter.INSTANCE, null, 0L, 0L);
    }
    
    public static SslContext newServerContext(final SslProvider provider, final File certChainFile, final File keyFile, final String keyPassword, final Iterable<String> ciphers, final CipherSuiteFilter cipherFilter, final ApplicationProtocolConfig apn, final long sessionCacheSize, final long sessionTimeout) throws SSLException {
        return newServerContext(provider, null, null, certChainFile, keyFile, keyPassword, null, ciphers, cipherFilter, apn, sessionCacheSize, sessionTimeout);
    }
    
    public static SslContext newServerContext(SslProvider provider, final File trustCertChainFile, final TrustManagerFactory trustManagerFactory, final File keyCertChainFile, final File keyFile, final String keyPassword, final KeyManagerFactory keyManagerFactory, final Iterable<String> ciphers, final CipherSuiteFilter cipherFilter, final ApplicationProtocolConfig apn, final long sessionCacheSize, final long sessionTimeout) throws SSLException {
        if (provider == null) {
            provider = defaultServerProvider();
        }
        switch (provider) {
            case JDK: {
                return new JdkSslServerContext(trustCertChainFile, trustManagerFactory, keyCertChainFile, keyFile, keyPassword, keyManagerFactory, ciphers, cipherFilter, apn, sessionCacheSize, sessionTimeout);
            }
            case OPENSSL: {
                return new OpenSslServerContext(keyCertChainFile, keyFile, keyPassword, trustManagerFactory, ciphers, apn, sessionCacheSize, sessionTimeout);
            }
            default: {
                throw new Error(provider.toString());
            }
        }
    }
    
    public static SslContext newClientContext() throws SSLException {
        return newClientContext(null, null, null);
    }
    
    public static SslContext newClientContext(final File certChainFile) throws SSLException {
        return newClientContext(null, certChainFile);
    }
    
    public static SslContext newClientContext(final TrustManagerFactory trustManagerFactory) throws SSLException {
        return newClientContext(null, null, trustManagerFactory);
    }
    
    public static SslContext newClientContext(final File certChainFile, final TrustManagerFactory trustManagerFactory) throws SSLException {
        return newClientContext(null, certChainFile, trustManagerFactory);
    }
    
    public static SslContext newClientContext(final File certChainFile, final TrustManagerFactory trustManagerFactory, final Iterable<String> ciphers, final CipherSuiteFilter cipherFilter, final ApplicationProtocolConfig apn, final long sessionCacheSize, final long sessionTimeout) throws SSLException {
        return newClientContext(null, certChainFile, trustManagerFactory, ciphers, cipherFilter, apn, sessionCacheSize, sessionTimeout);
    }
    
    public static SslContext newClientContext(final SslProvider provider) throws SSLException {
        return newClientContext(provider, null, null);
    }
    
    public static SslContext newClientContext(final SslProvider provider, final File certChainFile) throws SSLException {
        return newClientContext(provider, certChainFile, null);
    }
    
    public static SslContext newClientContext(final SslProvider provider, final TrustManagerFactory trustManagerFactory) throws SSLException {
        return newClientContext(provider, null, trustManagerFactory);
    }
    
    public static SslContext newClientContext(final SslProvider provider, final File certChainFile, final TrustManagerFactory trustManagerFactory) throws SSLException {
        return newClientContext(provider, certChainFile, trustManagerFactory, null, IdentityCipherSuiteFilter.INSTANCE, null, 0L, 0L);
    }
    
    public static SslContext newClientContext(final SslProvider provider, final File certChainFile, final TrustManagerFactory trustManagerFactory, final Iterable<String> ciphers, final CipherSuiteFilter cipherFilter, final ApplicationProtocolConfig apn, final long sessionCacheSize, final long sessionTimeout) throws SSLException {
        return newClientContext(provider, certChainFile, trustManagerFactory, null, null, null, null, ciphers, cipherFilter, apn, sessionCacheSize, sessionTimeout);
    }
    
    public static SslContext newClientContext(SslProvider provider, final File trustCertChainFile, final TrustManagerFactory trustManagerFactory, final File keyCertChainFile, final File keyFile, final String keyPassword, final KeyManagerFactory keyManagerFactory, final Iterable<String> ciphers, final CipherSuiteFilter cipherFilter, final ApplicationProtocolConfig apn, final long sessionCacheSize, final long sessionTimeout) throws SSLException {
        if (provider == null) {
            provider = defaultClientProvider();
        }
        switch (provider) {
            case JDK: {
                return new JdkSslClientContext(trustCertChainFile, trustManagerFactory, keyCertChainFile, keyFile, keyPassword, keyManagerFactory, ciphers, cipherFilter, apn, sessionCacheSize, sessionTimeout);
            }
            case OPENSSL: {
                return new OpenSslClientContext(trustCertChainFile, trustManagerFactory, ciphers, apn, sessionCacheSize, sessionTimeout);
            }
            default: {
                throw new Error();
            }
        }
    }
    
    SslContext() {
    }
    
    public final boolean isServer() {
        return !this.isClient();
    }
    
    public abstract boolean isClient();
    
    public abstract List<String> cipherSuites();
    
    public abstract long sessionCacheSize();
    
    public abstract long sessionTimeout();
    
    public abstract ApplicationProtocolNegotiator applicationProtocolNegotiator();
    
    public abstract SSLEngine newEngine(final ByteBufAllocator p0);
    
    public abstract SSLEngine newEngine(final ByteBufAllocator p0, final String p1, final int p2);
    
    public abstract SSLSessionContext sessionContext();
    
    public final SslHandler newHandler(final ByteBufAllocator alloc) {
        return newHandler(this.newEngine(alloc));
    }
    
    public final SslHandler newHandler(final ByteBufAllocator alloc, final String peerHost, final int peerPort) {
        return newHandler(this.newEngine(alloc, peerHost, peerPort));
    }
    
    private static SslHandler newHandler(final SSLEngine engine) {
        return new SslHandler(engine);
    }
    
    protected static PKCS8EncodedKeySpec generateKeySpec(final char[] password, final byte[] key) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, InvalidKeyException, InvalidAlgorithmParameterException {
        if (password == null || password.length == 0) {
            return new PKCS8EncodedKeySpec(key);
        }
        final EncryptedPrivateKeyInfo encryptedPrivateKeyInfo = new EncryptedPrivateKeyInfo(key);
        final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(encryptedPrivateKeyInfo.getAlgName());
        final PBEKeySpec pbeKeySpec = new PBEKeySpec(password);
        final SecretKey pbeKey = keyFactory.generateSecret(pbeKeySpec);
        final Cipher cipher = Cipher.getInstance(encryptedPrivateKeyInfo.getAlgName());
        cipher.init(2, pbeKey, encryptedPrivateKeyInfo.getAlgParameters());
        return encryptedPrivateKeyInfo.getKeySpec(cipher);
    }
    
    static {
        try {
            X509_CERT_FACTORY = CertificateFactory.getInstance("X.509");
        }
        catch (CertificateException e) {
            throw new IllegalStateException("unable to instance X.509 CertificateFactory", e);
        }
    }
}
