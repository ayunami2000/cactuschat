// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.ssl;

import io.netty.util.internal.PlatformDependent;
import java.util.Collection;
import io.netty.util.internal.logging.InternalLoggerFactory;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.TrustManager;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLEngine;
import io.netty.buffer.ByteBufAllocator;
import java.util.Iterator;
import org.apache.tomcat.jni.SSLContext;
import org.apache.tomcat.jni.Pool;
import io.netty.util.internal.ObjectUtil;
import java.util.Collections;
import java.util.ArrayList;
import javax.net.ssl.SSLException;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.List;
import io.netty.util.internal.logging.InternalLogger;

public abstract class OpenSslContext extends SslContext
{
    private static final InternalLogger logger;
    private static final List<String> DEFAULT_CIPHERS;
    private static final AtomicIntegerFieldUpdater<OpenSslContext> DESTROY_UPDATER;
    protected static final int VERIFY_DEPTH = 10;
    private final long aprPool;
    private volatile int aprPoolDestroyed;
    private final List<String> ciphers;
    private final List<String> unmodifiableCiphers;
    private final long sessionCacheSize;
    private final long sessionTimeout;
    private final OpenSslApplicationProtocolNegotiator apn;
    protected final long ctx;
    private final int mode;
    
    OpenSslContext(final Iterable<String> ciphers, final ApplicationProtocolConfig apnCfg, final long sessionCacheSize, final long sessionTimeout, final int mode) throws SSLException {
        this(ciphers, toNegotiator(apnCfg, mode == 1), sessionCacheSize, sessionTimeout, mode);
    }
    
    OpenSslContext(Iterable<String> ciphers, final OpenSslApplicationProtocolNegotiator apn, long sessionCacheSize, long sessionTimeout, final int mode) throws SSLException {
        this.ciphers = new ArrayList<String>();
        this.unmodifiableCiphers = Collections.unmodifiableList((List<? extends String>)this.ciphers);
        OpenSsl.ensureAvailability();
        if (mode != 1 && mode != 0) {
            throw new IllegalArgumentException("mode most be either SSL.SSL_MODE_SERVER or SSL.SSL_MODE_CLIENT");
        }
        this.mode = mode;
        if (ciphers == null) {
            ciphers = OpenSslContext.DEFAULT_CIPHERS;
        }
        for (String c : ciphers) {
            if (c == null) {
                break;
            }
            final String converted = CipherSuiteConverter.toOpenSsl(c);
            if (converted != null) {
                c = converted;
            }
            this.ciphers.add(c);
        }
        this.apn = ObjectUtil.checkNotNull(apn, "apn");
        this.aprPool = Pool.create(0L);
        boolean success = false;
        try {
            synchronized (OpenSslContext.class) {
                try {
                    this.ctx = SSLContext.make(this.aprPool, 28, mode);
                }
                catch (Exception e) {
                    throw new SSLException("failed to create an SSL_CTX", e);
                }
                SSLContext.setOptions(this.ctx, 4095);
                SSLContext.setOptions(this.ctx, 16777216);
                SSLContext.setOptions(this.ctx, 33554432);
                SSLContext.setOptions(this.ctx, 4194304);
                SSLContext.setOptions(this.ctx, 524288);
                SSLContext.setOptions(this.ctx, 1048576);
                SSLContext.setOptions(this.ctx, 65536);
                try {
                    SSLContext.setCipherSuite(this.ctx, CipherSuiteConverter.toOpenSsl(this.ciphers));
                }
                catch (SSLException e2) {
                    throw e2;
                }
                catch (Exception e) {
                    throw new SSLException("failed to set cipher suite: " + this.ciphers, e);
                }
                final List<String> nextProtoList = apn.protocols();
                if (!nextProtoList.isEmpty()) {
                    final StringBuilder nextProtocolBuf = new StringBuilder();
                    for (final String p : nextProtoList) {
                        nextProtocolBuf.append(p);
                        nextProtocolBuf.append(',');
                    }
                    nextProtocolBuf.setLength(nextProtocolBuf.length() - 1);
                    SSLContext.setNextProtos(this.ctx, nextProtocolBuf.toString());
                }
                if (sessionCacheSize > 0L) {
                    this.sessionCacheSize = sessionCacheSize;
                    SSLContext.setSessionCacheSize(this.ctx, sessionCacheSize);
                }
                else {
                    sessionCacheSize = (this.sessionCacheSize = SSLContext.setSessionCacheSize(this.ctx, 20480L));
                    SSLContext.setSessionCacheSize(this.ctx, sessionCacheSize);
                }
                if (sessionTimeout > 0L) {
                    this.sessionTimeout = sessionTimeout;
                    SSLContext.setSessionCacheTimeout(this.ctx, sessionTimeout);
                }
                else {
                    sessionTimeout = (this.sessionTimeout = SSLContext.setSessionCacheTimeout(this.ctx, 300L));
                    SSLContext.setSessionCacheTimeout(this.ctx, sessionTimeout);
                }
            }
            success = true;
        }
        finally {
            if (!success) {
                this.destroyPools();
            }
        }
    }
    
    @Override
    public final List<String> cipherSuites() {
        return this.unmodifiableCiphers;
    }
    
    @Override
    public final long sessionCacheSize() {
        return this.sessionCacheSize;
    }
    
    @Override
    public final long sessionTimeout() {
        return this.sessionTimeout;
    }
    
    @Override
    public ApplicationProtocolNegotiator applicationProtocolNegotiator() {
        return this.apn;
    }
    
    @Override
    public final boolean isClient() {
        return this.mode == 0;
    }
    
    @Override
    public final SSLEngine newEngine(final ByteBufAllocator alloc, final String peerHost, final int peerPort) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public final SSLEngine newEngine(final ByteBufAllocator alloc) {
        final List<String> protos = this.applicationProtocolNegotiator().protocols();
        if (protos.isEmpty()) {
            return new OpenSslEngine(this.ctx, alloc, null, this.isClient(), this.sessionContext());
        }
        return new OpenSslEngine(this.ctx, alloc, protos.get(protos.size() - 1), this.isClient(), this.sessionContext());
    }
    
    public final long context() {
        return this.ctx;
    }
    
    @Deprecated
    public final OpenSslSessionStats stats() {
        return this.sessionContext().stats();
    }
    
    @Override
    protected final void finalize() throws Throwable {
        super.finalize();
        synchronized (OpenSslContext.class) {
            if (this.ctx != 0L) {
                SSLContext.free(this.ctx);
            }
        }
        this.destroyPools();
    }
    
    @Deprecated
    public final void setTicketKeys(final byte[] keys) {
        this.sessionContext().setTicketKeys(keys);
    }
    
    @Override
    public abstract OpenSslSessionContext sessionContext();
    
    protected final void destroyPools() {
        if (this.aprPool != 0L && OpenSslContext.DESTROY_UPDATER.compareAndSet(this, 0, 1)) {
            Pool.destroy(this.aprPool);
        }
    }
    
    protected static X509Certificate[] certificates(final byte[][] chain) {
        final X509Certificate[] peerCerts = new X509Certificate[chain.length];
        for (int i = 0; i < peerCerts.length; ++i) {
            peerCerts[i] = new OpenSslX509Certificate(chain[i]);
        }
        return peerCerts;
    }
    
    protected static X509TrustManager chooseTrustManager(final TrustManager[] managers) {
        for (final TrustManager m : managers) {
            if (m instanceof X509TrustManager) {
                return (X509TrustManager)m;
            }
        }
        throw new IllegalStateException("no X509TrustManager found");
    }
    
    static OpenSslApplicationProtocolNegotiator toNegotiator(final ApplicationProtocolConfig config, final boolean isServer) {
        if (config == null) {
            return OpenSslDefaultApplicationProtocolNegotiator.INSTANCE;
        }
        switch (config.protocol()) {
            case NONE: {
                return OpenSslDefaultApplicationProtocolNegotiator.INSTANCE;
            }
            case NPN: {
                if (!isServer) {
                    throw new UnsupportedOperationException("OpenSSL provider does not support client mode");
                }
                switch (config.selectedListenerFailureBehavior()) {
                    case CHOOSE_MY_LAST_PROTOCOL: {
                        return new OpenSslNpnApplicationProtocolNegotiator(config.supportedProtocols());
                    }
                    default: {
                        throw new UnsupportedOperationException("OpenSSL provider does not support " + config.selectedListenerFailureBehavior() + " behavior");
                    }
                }
                break;
            }
            default: {
                throw new UnsupportedOperationException("OpenSSL provider does not support " + config.protocol() + " protocol");
            }
        }
    }
    
    static {
        logger = InternalLoggerFactory.getInstance(OpenSslContext.class);
        final List<String> ciphers = new ArrayList<String>();
        Collections.addAll(ciphers, new String[] { "ECDHE-RSA-AES128-GCM-SHA256", "ECDHE-RSA-AES128-SHA", "ECDHE-RSA-AES256-SHA", "AES128-GCM-SHA256", "AES128-SHA", "AES256-SHA", "DES-CBC3-SHA", "RC4-SHA" });
        DEFAULT_CIPHERS = Collections.unmodifiableList((List<? extends String>)ciphers);
        if (OpenSslContext.logger.isDebugEnabled()) {
            OpenSslContext.logger.debug("Default cipher suite (OpenSSL): " + ciphers);
        }
        AtomicIntegerFieldUpdater<OpenSslContext> updater = PlatformDependent.newAtomicIntegerFieldUpdater(OpenSslContext.class, "aprPoolDestroyed");
        if (updater == null) {
            updater = AtomicIntegerFieldUpdater.newUpdater(OpenSslContext.class, "aprPoolDestroyed");
        }
        DESTROY_UPDATER = updater;
    }
}
