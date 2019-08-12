// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.ssl;

import java.util.HashSet;
import java.security.SecureRandom;
import javax.net.ssl.TrustManager;
import javax.net.ssl.KeyManager;
import io.netty.util.internal.logging.InternalLoggerFactory;
import javax.security.auth.x500.X500Principal;
import java.security.cert.X509Certificate;
import javax.net.ssl.TrustManagerFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import io.netty.buffer.ByteBuf;
import java.security.Key;
import io.netty.buffer.ByteBufInputStream;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.security.spec.KeySpec;
import io.netty.util.internal.EmptyArrays;
import java.security.KeyFactory;
import java.security.cert.CertificateFactory;
import java.io.InputStream;
import java.security.KeyStore;
import java.io.IOException;
import java.security.KeyException;
import java.security.cert.CertificateException;
import java.security.InvalidAlgorithmParameterException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.NoSuchPaddingException;
import java.security.NoSuchAlgorithmException;
import java.security.KeyStoreException;
import java.security.UnrecoverableKeyException;
import java.security.Security;
import javax.net.ssl.KeyManagerFactory;
import java.io.File;
import javax.net.ssl.SSLEngine;
import io.netty.buffer.ByteBufAllocator;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.SSLContext;
import java.util.Collections;
import java.util.Arrays;
import io.netty.util.internal.ObjectUtil;
import java.util.Set;
import java.util.List;
import io.netty.util.internal.logging.InternalLogger;

public abstract class JdkSslContext extends SslContext
{
    private static final InternalLogger logger;
    static final String PROTOCOL = "TLS";
    static final String[] PROTOCOLS;
    static final List<String> DEFAULT_CIPHERS;
    static final Set<String> SUPPORTED_CIPHERS;
    private final String[] cipherSuites;
    private final List<String> unmodifiableCipherSuites;
    private final JdkApplicationProtocolNegotiator apn;
    
    private static void addIfSupported(final Set<String> supported, final List<String> enabled, final String... names) {
        for (final String n : names) {
            if (supported.contains(n)) {
                enabled.add(n);
            }
        }
    }
    
    JdkSslContext(final Iterable<String> ciphers, final CipherSuiteFilter cipherFilter, final ApplicationProtocolConfig config, final boolean isServer) {
        this(ciphers, cipherFilter, toNegotiator(config, isServer));
    }
    
    JdkSslContext(final Iterable<String> ciphers, final CipherSuiteFilter cipherFilter, final JdkApplicationProtocolNegotiator apn) {
        this.apn = ObjectUtil.checkNotNull(apn, "apn");
        this.cipherSuites = ObjectUtil.checkNotNull(cipherFilter, "cipherFilter").filterCipherSuites(ciphers, JdkSslContext.DEFAULT_CIPHERS, JdkSslContext.SUPPORTED_CIPHERS);
        this.unmodifiableCipherSuites = Collections.unmodifiableList((List<? extends String>)Arrays.asList((T[])this.cipherSuites));
    }
    
    public abstract SSLContext context();
    
    @Override
    public final SSLSessionContext sessionContext() {
        if (this.isServer()) {
            return this.context().getServerSessionContext();
        }
        return this.context().getClientSessionContext();
    }
    
    @Override
    public final List<String> cipherSuites() {
        return this.unmodifiableCipherSuites;
    }
    
    @Override
    public final long sessionCacheSize() {
        return this.sessionContext().getSessionCacheSize();
    }
    
    @Override
    public final long sessionTimeout() {
        return this.sessionContext().getSessionTimeout();
    }
    
    @Override
    public final SSLEngine newEngine(final ByteBufAllocator alloc) {
        final SSLEngine engine = this.context().createSSLEngine();
        engine.setEnabledCipherSuites(this.cipherSuites);
        engine.setEnabledProtocols(JdkSslContext.PROTOCOLS);
        engine.setUseClientMode(this.isClient());
        return this.wrapEngine(engine);
    }
    
    @Override
    public final SSLEngine newEngine(final ByteBufAllocator alloc, final String peerHost, final int peerPort) {
        final SSLEngine engine = this.context().createSSLEngine(peerHost, peerPort);
        engine.setEnabledCipherSuites(this.cipherSuites);
        engine.setEnabledProtocols(JdkSslContext.PROTOCOLS);
        engine.setUseClientMode(this.isClient());
        return this.wrapEngine(engine);
    }
    
    private SSLEngine wrapEngine(final SSLEngine engine) {
        return this.apn.wrapperFactory().wrapSslEngine(engine, this.apn, this.isServer());
    }
    
    @Override
    public JdkApplicationProtocolNegotiator applicationProtocolNegotiator() {
        return this.apn;
    }
    
    static JdkApplicationProtocolNegotiator toNegotiator(final ApplicationProtocolConfig config, final boolean isServer) {
        if (config == null) {
            return JdkDefaultApplicationProtocolNegotiator.INSTANCE;
        }
        switch (config.protocol()) {
            case NONE: {
                return JdkDefaultApplicationProtocolNegotiator.INSTANCE;
            }
            case ALPN: {
                if (isServer) {
                    switch (config.selectorFailureBehavior()) {
                        case FATAL_ALERT: {
                            return new JdkAlpnApplicationProtocolNegotiator(true, config.supportedProtocols());
                        }
                        case NO_ADVERTISE: {
                            return new JdkAlpnApplicationProtocolNegotiator(false, config.supportedProtocols());
                        }
                        default: {
                            throw new UnsupportedOperationException("JDK provider does not support " + config.selectorFailureBehavior() + " failure behavior");
                        }
                    }
                }
                else {
                    switch (config.selectedListenerFailureBehavior()) {
                        case ACCEPT: {
                            return new JdkAlpnApplicationProtocolNegotiator(false, config.supportedProtocols());
                        }
                        case FATAL_ALERT: {
                            return new JdkAlpnApplicationProtocolNegotiator(true, config.supportedProtocols());
                        }
                        default: {
                            throw new UnsupportedOperationException("JDK provider does not support " + config.selectedListenerFailureBehavior() + " failure behavior");
                        }
                    }
                }
                break;
            }
            case NPN: {
                if (isServer) {
                    switch (config.selectedListenerFailureBehavior()) {
                        case ACCEPT: {
                            return new JdkNpnApplicationProtocolNegotiator(false, config.supportedProtocols());
                        }
                        case FATAL_ALERT: {
                            return new JdkNpnApplicationProtocolNegotiator(true, config.supportedProtocols());
                        }
                        default: {
                            throw new UnsupportedOperationException("JDK provider does not support " + config.selectedListenerFailureBehavior() + " failure behavior");
                        }
                    }
                }
                else {
                    switch (config.selectorFailureBehavior()) {
                        case FATAL_ALERT: {
                            return new JdkNpnApplicationProtocolNegotiator(true, config.supportedProtocols());
                        }
                        case NO_ADVERTISE: {
                            return new JdkNpnApplicationProtocolNegotiator(false, config.supportedProtocols());
                        }
                        default: {
                            throw new UnsupportedOperationException("JDK provider does not support " + config.selectorFailureBehavior() + " failure behavior");
                        }
                    }
                }
                break;
            }
            default: {
                throw new UnsupportedOperationException("JDK provider does not support " + config.protocol() + " protocol");
            }
        }
    }
    
    protected static KeyManagerFactory buildKeyManagerFactory(final File certChainFile, final File keyFile, final String keyPassword, final KeyManagerFactory kmf) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, InvalidAlgorithmParameterException, CertificateException, KeyException, IOException {
        String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
        if (algorithm == null) {
            algorithm = "SunX509";
        }
        return buildKeyManagerFactory(certChainFile, algorithm, keyFile, keyPassword, kmf);
    }
    
    protected static KeyManagerFactory buildKeyManagerFactory(final File certChainFile, final String keyAlgorithm, final File keyFile, final String keyPassword, KeyManagerFactory kmf) throws KeyStoreException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, InvalidAlgorithmParameterException, IOException, CertificateException, KeyException, UnrecoverableKeyException {
        final KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(null, null);
        final CertificateFactory cf = CertificateFactory.getInstance("X.509");
        final KeyFactory rsaKF = KeyFactory.getInstance("RSA");
        final KeyFactory dsaKF = KeyFactory.getInstance("DSA");
        final ByteBuf encodedKeyBuf = PemReader.readPrivateKey(keyFile);
        final byte[] encodedKey = new byte[encodedKeyBuf.readableBytes()];
        encodedKeyBuf.readBytes(encodedKey).release();
        final char[] keyPasswordChars = (keyPassword == null) ? EmptyArrays.EMPTY_CHARS : keyPassword.toCharArray();
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
        if (kmf == null) {
            kmf = KeyManagerFactory.getInstance(keyAlgorithm);
        }
        kmf.init(ks, keyPasswordChars);
        return kmf;
    }
    
    protected static TrustManagerFactory buildTrustManagerFactory(final File certChainFile, TrustManagerFactory trustManagerFactory) throws NoSuchAlgorithmException, CertificateException, KeyStoreException, IOException {
        final KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(null, null);
        final CertificateFactory cf = CertificateFactory.getInstance("X.509");
        final ByteBuf[] certs = PemReader.readCertificates(certChainFile);
        try {
            for (final ByteBuf buf : certs) {
                final X509Certificate cert = (X509Certificate)cf.generateCertificate(new ByteBufInputStream(buf));
                final X500Principal principal = cert.getSubjectX500Principal();
                ks.setCertificateEntry(principal.getName("RFC2253"), cert);
            }
        }
        finally {
            for (final ByteBuf buf2 : certs) {
                buf2.release();
            }
        }
        if (trustManagerFactory == null) {
            trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        }
        trustManagerFactory.init(ks);
        return trustManagerFactory;
    }
    
    static {
        logger = InternalLoggerFactory.getInstance(JdkSslContext.class);
        SSLContext context;
        try {
            context = SSLContext.getInstance("TLS");
            context.init(null, null, null);
        }
        catch (Exception e) {
            throw new Error("failed to initialize the default SSL context", e);
        }
        final SSLEngine engine = context.createSSLEngine();
        final String[] supportedProtocols = engine.getSupportedProtocols();
        final Set<String> supportedProtocolsSet = new HashSet<String>(supportedProtocols.length);
        for (int i = 0; i < supportedProtocols.length; ++i) {
            supportedProtocolsSet.add(supportedProtocols[i]);
        }
        final List<String> protocols = new ArrayList<String>();
        addIfSupported(supportedProtocolsSet, protocols, "TLSv1.2", "TLSv1.1", "TLSv1");
        if (!protocols.isEmpty()) {
            PROTOCOLS = protocols.toArray(new String[protocols.size()]);
        }
        else {
            PROTOCOLS = engine.getEnabledProtocols();
        }
        final String[] supportedCiphers = engine.getSupportedCipherSuites();
        SUPPORTED_CIPHERS = new HashSet<String>(supportedCiphers.length);
        for (int i = 0; i < supportedCiphers.length; ++i) {
            JdkSslContext.SUPPORTED_CIPHERS.add(supportedCiphers[i]);
        }
        final List<String> ciphers = new ArrayList<String>();
        addIfSupported(JdkSslContext.SUPPORTED_CIPHERS, ciphers, "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256", "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA", "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA", "TLS_RSA_WITH_AES_128_GCM_SHA256", "TLS_RSA_WITH_AES_128_CBC_SHA", "TLS_RSA_WITH_AES_256_CBC_SHA", "SSL_RSA_WITH_3DES_EDE_CBC_SHA", "SSL_RSA_WITH_RC4_128_SHA");
        if (!ciphers.isEmpty()) {
            DEFAULT_CIPHERS = Collections.unmodifiableList((List<? extends String>)ciphers);
        }
        else {
            DEFAULT_CIPHERS = Collections.unmodifiableList((List<? extends String>)Arrays.asList((T[])engine.getEnabledCipherSuites()));
        }
        if (JdkSslContext.logger.isDebugEnabled()) {
            JdkSslContext.logger.debug("Default protocols (JDK): {} ", Arrays.asList(JdkSslContext.PROTOCOLS));
            JdkSslContext.logger.debug("Default cipher suites (JDK): {}", JdkSslContext.DEFAULT_CIPHERS);
        }
    }
}
