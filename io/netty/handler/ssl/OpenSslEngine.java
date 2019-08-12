// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.ssl;

import io.netty.buffer.Unpooled;
import java.util.Collection;
import java.util.HashSet;
import java.util.Arrays;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.security.Principal;
import javax.security.cert.CertificateException;
import javax.net.ssl.SSLSessionBindingEvent;
import javax.net.ssl.SSLSessionBindingListener;
import java.util.HashMap;
import javax.net.ssl.SSLSessionContext;
import java.util.Map;
import javax.security.cert.X509Certificate;
import javax.net.ssl.SSLPeerUnverifiedException;
import java.util.List;
import java.util.ArrayList;
import io.netty.util.internal.EmptyArrays;
import java.nio.ReadOnlyBufferException;
import javax.net.ssl.SSLEngineResult;
import io.netty.buffer.ByteBuf;
import org.apache.tomcat.jni.Buffer;
import java.nio.ByteBuffer;
import org.apache.tomcat.jni.SSL;
import io.netty.buffer.ByteBufAllocator;
import javax.net.ssl.SSLSession;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.Set;
import javax.net.ssl.SSLException;
import java.security.cert.Certificate;
import io.netty.util.internal.logging.InternalLogger;
import javax.net.ssl.SSLEngine;

public final class OpenSslEngine extends SSLEngine
{
    private static final InternalLogger logger;
    private static final Certificate[] EMPTY_CERTIFICATES;
    private static final SSLException ENGINE_CLOSED;
    private static final SSLException RENEGOTIATION_UNSUPPORTED;
    private static final SSLException ENCRYPTED_PACKET_OVERSIZED;
    private static final int MAX_PLAINTEXT_LENGTH = 16384;
    private static final int MAX_COMPRESSED_LENGTH = 17408;
    private static final int MAX_CIPHERTEXT_LENGTH = 18432;
    private static final String PROTOCOL_SSL_V2_HELLO = "SSLv2Hello";
    private static final String PROTOCOL_SSL_V2 = "SSLv2";
    private static final String PROTOCOL_SSL_V3 = "SSLv3";
    private static final String PROTOCOL_TLS_V1 = "TLSv1";
    private static final String PROTOCOL_TLS_V1_1 = "TLSv1.1";
    private static final String PROTOCOL_TLS_V1_2 = "TLSv1.2";
    private static final String[] SUPPORTED_PROTOCOLS;
    private static final Set<String> SUPPORTED_PROTOCOLS_SET;
    static final int MAX_ENCRYPTED_PACKET_LENGTH = 18713;
    static final int MAX_ENCRYPTION_OVERHEAD_LENGTH = 2329;
    private static final AtomicIntegerFieldUpdater<OpenSslEngine> DESTROYED_UPDATER;
    private static final AtomicReferenceFieldUpdater<OpenSslEngine, SSLSession> SESSION_UPDATER;
    private static final String INVALID_CIPHER = "SSL_NULL_WITH_NULL_NULL";
    private static final long EMPTY_ADDR;
    private long ssl;
    private long networkBIO;
    private int accepted;
    private boolean handshakeFinished;
    private boolean receivedShutdown;
    private volatile int destroyed;
    private volatile String cipher;
    private volatile String applicationProtocol;
    private volatile Certificate[] peerCerts;
    private volatile ClientAuthMode clientAuth;
    private boolean isInboundDone;
    private boolean isOutboundDone;
    private boolean engineClosed;
    private final boolean clientMode;
    private final ByteBufAllocator alloc;
    private final String fallbackApplicationProtocol;
    private final OpenSslSessionContext sessionContext;
    private volatile SSLSession session;
    
    @Deprecated
    public OpenSslEngine(final long sslCtx, final ByteBufAllocator alloc, final String fallbackApplicationProtocol) {
        this(sslCtx, alloc, fallbackApplicationProtocol, false, null);
    }
    
    OpenSslEngine(final long sslCtx, final ByteBufAllocator alloc, final String fallbackApplicationProtocol, final boolean clientMode, final OpenSslSessionContext sessionContext) {
        this.clientAuth = ClientAuthMode.NONE;
        OpenSsl.ensureAvailability();
        if (sslCtx == 0L) {
            throw new NullPointerException("sslContext");
        }
        if (alloc == null) {
            throw new NullPointerException("alloc");
        }
        this.alloc = alloc;
        this.ssl = SSL.newSSL(sslCtx, !clientMode);
        this.networkBIO = SSL.makeNetworkBIO(this.ssl);
        this.fallbackApplicationProtocol = fallbackApplicationProtocol;
        this.clientMode = clientMode;
        this.sessionContext = sessionContext;
    }
    
    public synchronized void shutdown() {
        if (OpenSslEngine.DESTROYED_UPDATER.compareAndSet(this, 0, 1)) {
            SSL.freeSSL(this.ssl);
            SSL.freeBIO(this.networkBIO);
            final long n = 0L;
            this.networkBIO = n;
            this.ssl = n;
            final boolean isInboundDone = true;
            this.engineClosed = isInboundDone;
            this.isOutboundDone = isInboundDone;
            this.isInboundDone = isInboundDone;
        }
    }
    
    private int writePlaintextData(final ByteBuffer src) {
        final int pos = src.position();
        final int limit = src.limit();
        final int len = Math.min(limit - pos, 16384);
        int sslWrote;
        if (src.isDirect()) {
            final long addr = Buffer.address(src) + pos;
            sslWrote = SSL.writeToSSL(this.ssl, addr, len);
            if (sslWrote > 0) {
                src.position(pos + sslWrote);
                return sslWrote;
            }
        }
        else {
            final ByteBuf buf = this.alloc.directBuffer(len);
            try {
                final long addr2 = memoryAddress(buf);
                src.limit(pos + len);
                buf.setBytes(0, src);
                src.limit(limit);
                sslWrote = SSL.writeToSSL(this.ssl, addr2, len);
                if (sslWrote > 0) {
                    src.position(pos + sslWrote);
                    return sslWrote;
                }
                src.position(pos);
            }
            finally {
                buf.release();
            }
        }
        throw new IllegalStateException("SSL.writeToSSL() returned a non-positive value: " + sslWrote);
    }
    
    private int writeEncryptedData(final ByteBuffer src) {
        final int pos = src.position();
        final int len = src.remaining();
        if (src.isDirect()) {
            final long addr = Buffer.address(src) + pos;
            final int netWrote = SSL.writeToBIO(this.networkBIO, addr, len);
            if (netWrote >= 0) {
                src.position(pos + netWrote);
                return netWrote;
            }
        }
        else {
            final ByteBuf buf = this.alloc.directBuffer(len);
            try {
                final long addr2 = memoryAddress(buf);
                buf.setBytes(0, src);
                final int netWrote2 = SSL.writeToBIO(this.networkBIO, addr2, len);
                if (netWrote2 >= 0) {
                    src.position(pos + netWrote2);
                    return netWrote2;
                }
                src.position(pos);
            }
            finally {
                buf.release();
            }
        }
        return -1;
    }
    
    private int readPlaintextData(final ByteBuffer dst) {
        if (dst.isDirect()) {
            final int pos = dst.position();
            final long addr = Buffer.address(dst) + pos;
            final int len = dst.limit() - pos;
            final int sslRead = SSL.readFromSSL(this.ssl, addr, len);
            if (sslRead > 0) {
                dst.position(pos + sslRead);
                return sslRead;
            }
        }
        else {
            final int pos = dst.position();
            final int limit = dst.limit();
            final int len2 = Math.min(18713, limit - pos);
            final ByteBuf buf = this.alloc.directBuffer(len2);
            try {
                final long addr2 = memoryAddress(buf);
                final int sslRead2 = SSL.readFromSSL(this.ssl, addr2, len2);
                if (sslRead2 > 0) {
                    dst.limit(pos + sslRead2);
                    buf.getBytes(0, dst);
                    dst.limit(limit);
                    return sslRead2;
                }
            }
            finally {
                buf.release();
            }
        }
        return 0;
    }
    
    private int readEncryptedData(final ByteBuffer dst, final int pending) {
        if (dst.isDirect() && dst.remaining() >= pending) {
            final int pos = dst.position();
            final long addr = Buffer.address(dst) + pos;
            final int bioRead = SSL.readFromBIO(this.networkBIO, addr, pending);
            if (bioRead > 0) {
                dst.position(pos + bioRead);
                return bioRead;
            }
        }
        else {
            final ByteBuf buf = this.alloc.directBuffer(pending);
            try {
                final long addr = memoryAddress(buf);
                final int bioRead = SSL.readFromBIO(this.networkBIO, addr, pending);
                if (bioRead > 0) {
                    final int oldLimit = dst.limit();
                    dst.limit(dst.position() + bioRead);
                    buf.getBytes(0, dst);
                    dst.limit(oldLimit);
                    return bioRead;
                }
            }
            finally {
                buf.release();
            }
        }
        return 0;
    }
    
    @Override
    public synchronized SSLEngineResult wrap(final ByteBuffer[] srcs, final int offset, final int length, final ByteBuffer dst) throws SSLException {
        if (this.destroyed != 0) {
            return new SSLEngineResult(SSLEngineResult.Status.CLOSED, SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING, 0, 0);
        }
        if (srcs == null) {
            throw new IllegalArgumentException("srcs is null");
        }
        if (dst == null) {
            throw new IllegalArgumentException("dst is null");
        }
        if (offset >= srcs.length || offset + length > srcs.length) {
            throw new IndexOutOfBoundsException("offset: " + offset + ", length: " + length + " (expected: offset <= offset + length <= srcs.length (" + srcs.length + "))");
        }
        if (dst.isReadOnly()) {
            throw new ReadOnlyBufferException();
        }
        if (this.accepted == 0) {
            this.beginHandshakeImplicitly();
        }
        final SSLEngineResult.HandshakeStatus handshakeStatus = this.getHandshakeStatus();
        if ((!this.handshakeFinished || this.engineClosed) && handshakeStatus == SSLEngineResult.HandshakeStatus.NEED_UNWRAP) {
            return new SSLEngineResult(this.getEngineStatus(), SSLEngineResult.HandshakeStatus.NEED_UNWRAP, 0, 0);
        }
        int bytesProduced = 0;
        int pendingNet = SSL.pendingWrittenBytesInBIO(this.networkBIO);
        if (pendingNet <= 0) {
            int bytesConsumed = 0;
            for (int endOffset = offset + length, i = offset; i < endOffset; ++i) {
                final ByteBuffer src = srcs[i];
                if (src == null) {
                    throw new IllegalArgumentException("srcs[" + i + "] is null");
                }
                while (src.hasRemaining()) {
                    try {
                        bytesConsumed += this.writePlaintextData(src);
                    }
                    catch (Exception e) {
                        throw new SSLException(e);
                    }
                    pendingNet = SSL.pendingWrittenBytesInBIO(this.networkBIO);
                    if (pendingNet > 0) {
                        final int capacity = dst.remaining();
                        if (capacity < pendingNet) {
                            return new SSLEngineResult(SSLEngineResult.Status.BUFFER_OVERFLOW, this.getHandshakeStatus(), bytesConsumed, bytesProduced);
                        }
                        try {
                            bytesProduced += this.readEncryptedData(dst, pendingNet);
                        }
                        catch (Exception e2) {
                            throw new SSLException(e2);
                        }
                        return new SSLEngineResult(this.getEngineStatus(), this.getHandshakeStatus(), bytesConsumed, bytesProduced);
                    }
                }
            }
            return new SSLEngineResult(this.getEngineStatus(), this.getHandshakeStatus(), bytesConsumed, bytesProduced);
        }
        final int capacity2 = dst.remaining();
        if (capacity2 < pendingNet) {
            return new SSLEngineResult(SSLEngineResult.Status.BUFFER_OVERFLOW, handshakeStatus, 0, bytesProduced);
        }
        try {
            bytesProduced += this.readEncryptedData(dst, pendingNet);
        }
        catch (Exception e3) {
            throw new SSLException(e3);
        }
        if (this.isOutboundDone) {
            this.shutdown();
        }
        return new SSLEngineResult(this.getEngineStatus(), this.getHandshakeStatus(), 0, bytesProduced);
    }
    
    public synchronized SSLEngineResult unwrap(final ByteBuffer[] srcs, int srcsOffset, final int srcsLength, final ByteBuffer[] dsts, final int dstsOffset, final int dstsLength) throws SSLException {
        if (this.destroyed != 0) {
            return new SSLEngineResult(SSLEngineResult.Status.CLOSED, SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING, 0, 0);
        }
        if (srcs == null) {
            throw new NullPointerException("srcs");
        }
        if (srcsOffset >= srcs.length || srcsOffset + srcsLength > srcs.length) {
            throw new IndexOutOfBoundsException("offset: " + srcsOffset + ", length: " + srcsLength + " (expected: offset <= offset + length <= srcs.length (" + srcs.length + "))");
        }
        if (dsts == null) {
            throw new IllegalArgumentException("dsts is null");
        }
        if (dstsOffset >= dsts.length || dstsOffset + dstsLength > dsts.length) {
            throw new IndexOutOfBoundsException("offset: " + dstsOffset + ", length: " + dstsLength + " (expected: offset <= offset + length <= dsts.length (" + dsts.length + "))");
        }
        int capacity = 0;
        final int endOffset = dstsOffset + dstsLength;
        for (int i = dstsOffset; i < endOffset; ++i) {
            final ByteBuffer dst = dsts[i];
            if (dst == null) {
                throw new IllegalArgumentException("dsts[" + i + "] is null");
            }
            if (dst.isReadOnly()) {
                throw new ReadOnlyBufferException();
            }
            capacity += dst.remaining();
        }
        if (this.accepted == 0) {
            this.beginHandshakeImplicitly();
        }
        final SSLEngineResult.HandshakeStatus handshakeStatus = this.getHandshakeStatus();
        if ((!this.handshakeFinished || this.engineClosed) && handshakeStatus == SSLEngineResult.HandshakeStatus.NEED_WRAP) {
            return new SSLEngineResult(this.getEngineStatus(), SSLEngineResult.HandshakeStatus.NEED_WRAP, 0, 0);
        }
        final int srcsEndOffset = srcsOffset + srcsLength;
        int len = 0;
        for (int j = srcsOffset; j < srcsEndOffset; ++j) {
            final ByteBuffer src = srcs[j];
            if (src == null) {
                throw new IllegalArgumentException("srcs[" + j + "] is null");
            }
            len += src.remaining();
        }
        if (len > 18713) {
            this.isInboundDone = true;
            this.isOutboundDone = true;
            this.engineClosed = true;
            this.shutdown();
            throw OpenSslEngine.ENCRYPTED_PACKET_OVERSIZED;
        }
        int bytesConsumed = -1;
        try {
            while (srcsOffset < srcsEndOffset) {
                final ByteBuffer src = srcs[srcsOffset];
                final int remaining = src.remaining();
                final int written = this.writeEncryptedData(src);
                if (written < 0) {
                    break;
                }
                if (bytesConsumed == -1) {
                    bytesConsumed = written;
                }
                else {
                    bytesConsumed += written;
                }
                if (written == remaining) {
                    ++srcsOffset;
                }
                else {
                    if (written == 0) {
                        break;
                    }
                    continue;
                }
            }
        }
        catch (Exception e) {
            throw new SSLException(e);
        }
        if (bytesConsumed >= 0) {
            final int lastPrimingReadResult = SSL.readFromSSL(this.ssl, OpenSslEngine.EMPTY_ADDR, 0);
            if (lastPrimingReadResult <= 0) {
                final long error = SSL.getLastErrorNumber();
                if (OpenSsl.isError(error)) {
                    final String err = SSL.getErrorString(error);
                    if (OpenSslEngine.logger.isDebugEnabled()) {
                        OpenSslEngine.logger.debug("SSL_read failed: primingReadResult: " + lastPrimingReadResult + "; OpenSSL error: '" + err + '\'');
                    }
                    this.shutdown();
                    throw new SSLException(err);
                }
            }
        }
        else {
            bytesConsumed = 0;
        }
        int pendingApp = (this.handshakeFinished || SSL.isInInit(this.ssl) == 0) ? SSL.pendingReadableBytesInSSL(this.ssl) : 0;
        int bytesProduced = 0;
        if (pendingApp > 0) {
            if (capacity < pendingApp) {
                return new SSLEngineResult(SSLEngineResult.Status.BUFFER_OVERFLOW, this.getHandshakeStatus(), bytesConsumed, 0);
            }
            int idx = dstsOffset;
            while (idx < endOffset) {
                final ByteBuffer dst2 = dsts[idx];
                if (!dst2.hasRemaining()) {
                    ++idx;
                }
                else {
                    if (pendingApp <= 0) {
                        break;
                    }
                    int bytesRead;
                    try {
                        bytesRead = this.readPlaintextData(dst2);
                    }
                    catch (Exception e2) {
                        throw new SSLException(e2);
                    }
                    if (bytesRead == 0) {
                        break;
                    }
                    bytesProduced += bytesRead;
                    pendingApp -= bytesRead;
                    if (dst2.hasRemaining()) {
                        continue;
                    }
                    ++idx;
                }
            }
        }
        if (!this.receivedShutdown && (SSL.getShutdown(this.ssl) & 0x2) == 0x2) {
            this.receivedShutdown = true;
            this.closeOutbound();
            this.closeInbound();
        }
        return new SSLEngineResult(this.getEngineStatus(), this.getHandshakeStatus(), bytesConsumed, bytesProduced);
    }
    
    public SSLEngineResult unwrap(final ByteBuffer[] srcs, final ByteBuffer[] dsts) throws SSLException {
        return this.unwrap(srcs, 0, srcs.length, dsts, 0, dsts.length);
    }
    
    @Override
    public SSLEngineResult unwrap(final ByteBuffer src, final ByteBuffer[] dsts, final int offset, final int length) throws SSLException {
        return this.unwrap(new ByteBuffer[] { src }, 0, 1, dsts, offset, length);
    }
    
    @Override
    public Runnable getDelegatedTask() {
        return null;
    }
    
    @Override
    public synchronized void closeInbound() throws SSLException {
        if (this.isInboundDone) {
            return;
        }
        this.isInboundDone = true;
        this.engineClosed = true;
        this.shutdown();
        if (this.accepted != 0 && !this.receivedShutdown) {
            throw new SSLException("Inbound closed before receiving peer's close_notify: possible truncation attack?");
        }
    }
    
    @Override
    public synchronized boolean isInboundDone() {
        return this.isInboundDone || this.engineClosed;
    }
    
    @Override
    public synchronized void closeOutbound() {
        if (this.isOutboundDone) {
            return;
        }
        this.isOutboundDone = true;
        this.engineClosed = true;
        if (this.accepted != 0 && this.destroyed == 0) {
            final int mode = SSL.getShutdown(this.ssl);
            if ((mode & 0x1) != 0x1) {
                SSL.shutdownSSL(this.ssl);
            }
        }
        else {
            this.shutdown();
        }
    }
    
    @Override
    public synchronized boolean isOutboundDone() {
        return this.isOutboundDone;
    }
    
    @Override
    public String[] getSupportedCipherSuites() {
        final Set<String> availableCipherSuites = OpenSsl.availableCipherSuites();
        return availableCipherSuites.toArray(new String[availableCipherSuites.size()]);
    }
    
    @Override
    public String[] getEnabledCipherSuites() {
        final String[] enabled = SSL.getCiphers(this.ssl);
        if (enabled == null) {
            return EmptyArrays.EMPTY_STRINGS;
        }
        for (int i = 0; i < enabled.length; ++i) {
            final String mapped = this.toJavaCipherSuite(enabled[i]);
            if (mapped != null) {
                enabled[i] = mapped;
            }
        }
        return enabled;
    }
    
    @Override
    public void setEnabledCipherSuites(final String[] cipherSuites) {
        if (cipherSuites == null) {
            throw new NullPointerException("cipherSuites");
        }
        final StringBuilder buf = new StringBuilder();
        for (final String c : cipherSuites) {
            if (c == null) {
                break;
            }
            String converted = CipherSuiteConverter.toOpenSsl(c);
            if (converted == null) {
                converted = c;
            }
            if (!OpenSsl.isCipherSuiteAvailable(converted)) {
                throw new IllegalArgumentException("unsupported cipher suite: " + c + '(' + converted + ')');
            }
            buf.append(converted);
            buf.append(':');
        }
        if (buf.length() == 0) {
            throw new IllegalArgumentException("empty cipher suites");
        }
        buf.setLength(buf.length() - 1);
        final String cipherSuiteSpec = buf.toString();
        try {
            SSL.setCipherSuites(this.ssl, cipherSuiteSpec);
        }
        catch (Exception e) {
            throw new IllegalStateException("failed to enable cipher suites: " + cipherSuiteSpec, e);
        }
    }
    
    @Override
    public String[] getSupportedProtocols() {
        return OpenSslEngine.SUPPORTED_PROTOCOLS.clone();
    }
    
    @Override
    public String[] getEnabledProtocols() {
        final List<String> enabled = new ArrayList<String>();
        enabled.add("SSLv2Hello");
        final int opts = SSL.getOptions(this.ssl);
        if ((opts & 0x4000000) == 0x0) {
            enabled.add("TLSv1");
        }
        if ((opts & 0x8000000) == 0x0) {
            enabled.add("TLSv1.1");
        }
        if ((opts & 0x10000000) == 0x0) {
            enabled.add("TLSv1.2");
        }
        if ((opts & 0x1000000) == 0x0) {
            enabled.add("SSLv2");
        }
        if ((opts & 0x2000000) == 0x0) {
            enabled.add("SSLv3");
        }
        final int size = enabled.size();
        if (size == 0) {
            return EmptyArrays.EMPTY_STRINGS;
        }
        return enabled.toArray(new String[size]);
    }
    
    @Override
    public void setEnabledProtocols(final String[] protocols) {
        if (protocols == null) {
            throw new IllegalArgumentException();
        }
        boolean sslv2 = false;
        boolean sslv3 = false;
        boolean tlsv1 = false;
        boolean tlsv1_1 = false;
        boolean tlsv1_2 = false;
        for (final String p : protocols) {
            if (!OpenSslEngine.SUPPORTED_PROTOCOLS_SET.contains(p)) {
                throw new IllegalArgumentException("Protocol " + p + " is not supported.");
            }
            if (p.equals("SSLv2")) {
                sslv2 = true;
            }
            else if (p.equals("SSLv3")) {
                sslv3 = true;
            }
            else if (p.equals("TLSv1")) {
                tlsv1 = true;
            }
            else if (p.equals("TLSv1.1")) {
                tlsv1_1 = true;
            }
            else if (p.equals("TLSv1.2")) {
                tlsv1_2 = true;
            }
        }
        SSL.setOptions(this.ssl, 4095);
        if (!sslv2) {
            SSL.setOptions(this.ssl, 16777216);
        }
        if (!sslv3) {
            SSL.setOptions(this.ssl, 33554432);
        }
        if (!tlsv1) {
            SSL.setOptions(this.ssl, 67108864);
        }
        if (!tlsv1_1) {
            SSL.setOptions(this.ssl, 134217728);
        }
        if (!tlsv1_2) {
            SSL.setOptions(this.ssl, 268435456);
        }
    }
    
    private Certificate[] initPeerCertChain() throws SSLPeerUnverifiedException {
        final byte[][] chain = SSL.getPeerCertChain(this.ssl);
        byte[] clientCert;
        if (!this.clientMode) {
            clientCert = SSL.getPeerCertificate(this.ssl);
        }
        else {
            clientCert = null;
        }
        if (chain == null && clientCert == null) {
            throw new SSLPeerUnverifiedException("peer not verified");
        }
        int len = 0;
        if (chain != null) {
            len += chain.length;
        }
        int i = 0;
        Certificate[] peerCerts;
        if (clientCert != null) {
            peerCerts = new Certificate[++len];
            peerCerts[i++] = new OpenSslX509Certificate(clientCert);
        }
        else {
            peerCerts = new Certificate[len];
        }
        if (chain != null) {
            int a = 0;
            while (i < peerCerts.length) {
                peerCerts[i] = new OpenSslX509Certificate(chain[a++]);
                ++i;
            }
        }
        return peerCerts;
    }
    
    @Override
    public SSLSession getSession() {
        SSLSession session = this.session;
        if (session == null) {
            session = new SSLSession() {
                private X509Certificate[] x509PeerCerts;
                private Map<String, Object> values;
                
                @Override
                public byte[] getId() {
                    final byte[] id = SSL.getSessionId(OpenSslEngine.this.ssl);
                    if (id == null) {
                        throw new IllegalStateException("SSL session ID not available");
                    }
                    return id;
                }
                
                @Override
                public SSLSessionContext getSessionContext() {
                    return OpenSslEngine.this.sessionContext;
                }
                
                @Override
                public long getCreationTime() {
                    return SSL.getTime(OpenSslEngine.this.ssl) * 1000L;
                }
                
                @Override
                public long getLastAccessedTime() {
                    return this.getCreationTime();
                }
                
                @Override
                public void invalidate() {
                }
                
                @Override
                public boolean isValid() {
                    return false;
                }
                
                @Override
                public void putValue(final String name, final Object value) {
                    if (name == null) {
                        throw new NullPointerException("name");
                    }
                    if (value == null) {
                        throw new NullPointerException("value");
                    }
                    Map<String, Object> values = this.values;
                    if (values == null) {
                        final HashMap<String, Object> values2 = new HashMap<String, Object>(2);
                        this.values = values2;
                        values = values2;
                    }
                    final Object old = values.put(name, value);
                    if (value instanceof SSLSessionBindingListener) {
                        ((SSLSessionBindingListener)value).valueBound(new SSLSessionBindingEvent(this, name));
                    }
                    this.notifyUnbound(old, name);
                }
                
                @Override
                public Object getValue(final String name) {
                    if (name == null) {
                        throw new NullPointerException("name");
                    }
                    if (this.values == null) {
                        return null;
                    }
                    return this.values.get(name);
                }
                
                @Override
                public void removeValue(final String name) {
                    if (name == null) {
                        throw new NullPointerException("name");
                    }
                    final Map<String, Object> values = this.values;
                    if (values == null) {
                        return;
                    }
                    final Object old = values.remove(name);
                    this.notifyUnbound(old, name);
                }
                
                @Override
                public String[] getValueNames() {
                    final Map<String, Object> values = this.values;
                    if (values == null || values.isEmpty()) {
                        return EmptyArrays.EMPTY_STRINGS;
                    }
                    return values.keySet().toArray(new String[values.size()]);
                }
                
                private void notifyUnbound(final Object value, final String name) {
                    if (value instanceof SSLSessionBindingListener) {
                        ((SSLSessionBindingListener)value).valueUnbound(new SSLSessionBindingEvent(this, name));
                    }
                }
                
                @Override
                public Certificate[] getPeerCertificates() throws SSLPeerUnverifiedException {
                    Certificate[] c = OpenSslEngine.this.peerCerts;
                    if (c == null) {
                        if (SSL.isInInit(OpenSslEngine.this.ssl) != 0) {
                            throw new SSLPeerUnverifiedException("peer not verified");
                        }
                        c = (OpenSslEngine.this.peerCerts = OpenSslEngine.this.initPeerCertChain());
                    }
                    return c;
                }
                
                @Override
                public Certificate[] getLocalCertificates() {
                    return OpenSslEngine.EMPTY_CERTIFICATES;
                }
                
                @Override
                public X509Certificate[] getPeerCertificateChain() throws SSLPeerUnverifiedException {
                    X509Certificate[] c = this.x509PeerCerts;
                    if (c == null) {
                        if (SSL.isInInit(OpenSslEngine.this.ssl) != 0) {
                            throw new SSLPeerUnverifiedException("peer not verified");
                        }
                        final byte[][] chain = SSL.getPeerCertChain(OpenSslEngine.this.ssl);
                        if (chain == null) {
                            throw new SSLPeerUnverifiedException("peer not verified");
                        }
                        final X509Certificate[] peerCerts = new X509Certificate[chain.length];
                        for (int i = 0; i < peerCerts.length; ++i) {
                            try {
                                peerCerts[i] = X509Certificate.getInstance(chain[i]);
                            }
                            catch (CertificateException e) {
                                throw new IllegalStateException(e);
                            }
                        }
                        final X509Certificate[] x509PeerCerts = peerCerts;
                        this.x509PeerCerts = x509PeerCerts;
                        c = x509PeerCerts;
                    }
                    return c;
                }
                
                @Override
                public Principal getPeerPrincipal() throws SSLPeerUnverifiedException {
                    final Certificate[] peer = this.getPeerCertificates();
                    if (peer == null || peer.length == 0) {
                        return null;
                    }
                    return this.principal(peer);
                }
                
                @Override
                public Principal getLocalPrincipal() {
                    final Certificate[] local = this.getLocalCertificates();
                    if (local == null || local.length == 0) {
                        return null;
                    }
                    return this.principal(local);
                }
                
                private Principal principal(final Certificate[] certs) {
                    return ((java.security.cert.X509Certificate)certs[0]).getIssuerX500Principal();
                }
                
                @Override
                public String getCipherSuite() {
                    if (!OpenSslEngine.this.handshakeFinished) {
                        return "SSL_NULL_WITH_NULL_NULL";
                    }
                    if (OpenSslEngine.this.cipher == null) {
                        final String c = OpenSslEngine.this.toJavaCipherSuite(SSL.getCipherForSSL(OpenSslEngine.this.ssl));
                        if (c != null) {
                            OpenSslEngine.this.cipher = c;
                        }
                    }
                    return OpenSslEngine.this.cipher;
                }
                
                @Override
                public String getProtocol() {
                    String applicationProtocol = OpenSslEngine.this.applicationProtocol;
                    if (applicationProtocol == null) {
                        applicationProtocol = SSL.getNextProtoNegotiated(OpenSslEngine.this.ssl);
                        if (applicationProtocol == null) {
                            applicationProtocol = OpenSslEngine.this.fallbackApplicationProtocol;
                        }
                        if (applicationProtocol != null) {
                            OpenSslEngine.this.applicationProtocol = applicationProtocol.replace(':', '_');
                        }
                        else {
                            OpenSslEngine.this.applicationProtocol = (applicationProtocol = "");
                        }
                    }
                    final String version = SSL.getVersion(OpenSslEngine.this.ssl);
                    if (applicationProtocol.isEmpty()) {
                        return version;
                    }
                    return version + ':' + applicationProtocol;
                }
                
                @Override
                public String getPeerHost() {
                    return null;
                }
                
                @Override
                public int getPeerPort() {
                    return 0;
                }
                
                @Override
                public int getPacketBufferSize() {
                    return 18713;
                }
                
                @Override
                public int getApplicationBufferSize() {
                    return 16384;
                }
            };
            if (!OpenSslEngine.SESSION_UPDATER.compareAndSet(this, null, session)) {
                session = this.session;
            }
        }
        return session;
    }
    
    @Override
    public synchronized void beginHandshake() throws SSLException {
        if (this.engineClosed || this.destroyed != 0) {
            throw OpenSslEngine.ENGINE_CLOSED;
        }
        switch (this.accepted) {
            case 0: {
                this.handshake();
                this.accepted = 2;
                break;
            }
            case 1: {
                this.accepted = 2;
                break;
            }
            case 2: {
                throw OpenSslEngine.RENEGOTIATION_UNSUPPORTED;
            }
            default: {
                throw new Error();
            }
        }
    }
    
    private void beginHandshakeImplicitly() throws SSLException {
        if (this.engineClosed || this.destroyed != 0) {
            throw OpenSslEngine.ENGINE_CLOSED;
        }
        if (this.accepted == 0) {
            this.handshake();
            this.accepted = 1;
        }
    }
    
    private void handshake() throws SSLException {
        final int code = SSL.doHandshake(this.ssl);
        if (code <= 0) {
            final long error = SSL.getLastErrorNumber();
            if (OpenSsl.isError(error)) {
                final String err = SSL.getErrorString(error);
                if (OpenSslEngine.logger.isDebugEnabled()) {
                    OpenSslEngine.logger.debug("SSL_do_handshake failed: OpenSSL error: '" + err + '\'');
                }
                this.shutdown();
                throw new SSLException(err);
            }
        }
        else {
            this.handshakeFinished = true;
        }
    }
    
    private static long memoryAddress(final ByteBuf buf) {
        if (buf.hasMemoryAddress()) {
            return buf.memoryAddress();
        }
        return Buffer.address(buf.nioBuffer());
    }
    
    private SSLEngineResult.Status getEngineStatus() {
        return this.engineClosed ? SSLEngineResult.Status.CLOSED : SSLEngineResult.Status.OK;
    }
    
    @Override
    public synchronized SSLEngineResult.HandshakeStatus getHandshakeStatus() {
        if (this.accepted == 0 || this.destroyed != 0) {
            return SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING;
        }
        if (!this.handshakeFinished) {
            if (SSL.pendingWrittenBytesInBIO(this.networkBIO) != 0) {
                return SSLEngineResult.HandshakeStatus.NEED_WRAP;
            }
            if (SSL.isInInit(this.ssl) == 0) {
                this.handshakeFinished = true;
                return SSLEngineResult.HandshakeStatus.FINISHED;
            }
            return SSLEngineResult.HandshakeStatus.NEED_UNWRAP;
        }
        else {
            if (!this.engineClosed) {
                return SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING;
            }
            if (SSL.pendingWrittenBytesInBIO(this.networkBIO) != 0) {
                return SSLEngineResult.HandshakeStatus.NEED_WRAP;
            }
            return SSLEngineResult.HandshakeStatus.NEED_UNWRAP;
        }
    }
    
    private String toJavaCipherSuite(final String openSslCipherSuite) {
        if (openSslCipherSuite == null) {
            return null;
        }
        final String prefix = toJavaCipherSuitePrefix(SSL.getVersion(this.ssl));
        return CipherSuiteConverter.toJava(openSslCipherSuite, prefix);
    }
    
    private static String toJavaCipherSuitePrefix(final String protocolVersion) {
        char c;
        if (protocolVersion == null || protocolVersion.length() == 0) {
            c = '\0';
        }
        else {
            c = protocolVersion.charAt(0);
        }
        switch (c) {
            case 'T': {
                return "TLS";
            }
            case 'S': {
                return "SSL";
            }
            default: {
                return "UNKNOWN";
            }
        }
    }
    
    @Override
    public void setUseClientMode(final boolean clientMode) {
        if (clientMode != this.clientMode) {
            throw new UnsupportedOperationException();
        }
    }
    
    @Override
    public boolean getUseClientMode() {
        return this.clientMode;
    }
    
    @Override
    public void setNeedClientAuth(final boolean b) {
        this.setClientAuth(b ? ClientAuthMode.REQUIRE : ClientAuthMode.NONE);
    }
    
    @Override
    public boolean getNeedClientAuth() {
        return this.clientAuth == ClientAuthMode.REQUIRE;
    }
    
    @Override
    public void setWantClientAuth(final boolean b) {
        this.setClientAuth(b ? ClientAuthMode.OPTIONAL : ClientAuthMode.NONE);
    }
    
    @Override
    public boolean getWantClientAuth() {
        return this.clientAuth == ClientAuthMode.OPTIONAL;
    }
    
    private void setClientAuth(final ClientAuthMode mode) {
        if (this.clientMode) {
            return;
        }
        synchronized (this) {
            if (this.clientAuth == mode) {
                return;
            }
            switch (mode) {
                case NONE: {
                    SSL.setVerify(this.ssl, 0, 10);
                    break;
                }
                case REQUIRE: {
                    SSL.setVerify(this.ssl, 2, 10);
                    break;
                }
                case OPTIONAL: {
                    SSL.setVerify(this.ssl, 1, 10);
                    break;
                }
            }
            this.clientAuth = mode;
        }
    }
    
    @Override
    public void setEnableSessionCreation(final boolean b) {
        if (b) {
            throw new UnsupportedOperationException();
        }
    }
    
    @Override
    public boolean getEnableSessionCreation() {
        return false;
    }
    
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        this.shutdown();
    }
    
    static {
        logger = InternalLoggerFactory.getInstance(OpenSslEngine.class);
        EMPTY_CERTIFICATES = new Certificate[0];
        ENGINE_CLOSED = new SSLException("engine closed");
        RENEGOTIATION_UNSUPPORTED = new SSLException("renegotiation unsupported");
        ENCRYPTED_PACKET_OVERSIZED = new SSLException("encrypted packet oversized");
        OpenSslEngine.ENGINE_CLOSED.setStackTrace(EmptyArrays.EMPTY_STACK_TRACE);
        OpenSslEngine.RENEGOTIATION_UNSUPPORTED.setStackTrace(EmptyArrays.EMPTY_STACK_TRACE);
        OpenSslEngine.ENCRYPTED_PACKET_OVERSIZED.setStackTrace(EmptyArrays.EMPTY_STACK_TRACE);
        AtomicIntegerFieldUpdater<OpenSslEngine> destroyedUpdater = PlatformDependent.newAtomicIntegerFieldUpdater(OpenSslEngine.class, "destroyed");
        if (destroyedUpdater == null) {
            destroyedUpdater = AtomicIntegerFieldUpdater.newUpdater(OpenSslEngine.class, "destroyed");
        }
        DESTROYED_UPDATER = destroyedUpdater;
        AtomicReferenceFieldUpdater<OpenSslEngine, SSLSession> sessionUpdater = PlatformDependent.newAtomicReferenceFieldUpdater(OpenSslEngine.class, "session");
        if (sessionUpdater == null) {
            sessionUpdater = AtomicReferenceFieldUpdater.newUpdater(OpenSslEngine.class, SSLSession.class, "session");
        }
        SESSION_UPDATER = sessionUpdater;
        SUPPORTED_PROTOCOLS = new String[] { "SSLv2Hello", "SSLv2", "SSLv3", "TLSv1", "TLSv1.1", "TLSv1.2" };
        SUPPORTED_PROTOCOLS_SET = new HashSet<String>(Arrays.asList(OpenSslEngine.SUPPORTED_PROTOCOLS));
        EMPTY_ADDR = Buffer.address(Unpooled.EMPTY_BUFFER.nioBuffer());
    }
    
    enum ClientAuthMode
    {
        NONE, 
        OPTIONAL, 
        REQUIRE;
    }
}
