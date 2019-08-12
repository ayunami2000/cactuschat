// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.resolver.dns;

import io.netty.handler.codec.dns.DnsResource;
import io.netty.util.concurrent.ScheduledFuture;
import io.netty.util.ReferenceCountUtil;
import io.netty.handler.codec.dns.DnsResponseCode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.util.concurrent.TimeUnit;
import io.netty.util.internal.OneTimeTask;
import io.netty.handler.codec.dns.DnsResponse;
import java.net.IDN;
import io.netty.util.concurrent.Promise;
import java.util.Map;
import io.netty.channel.RecvByteBufAllocator;
import io.netty.channel.FixedRecvByteBufAllocator;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Future;
import io.netty.channel.ChannelFutureListener;
import java.net.SocketAddress;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.bootstrap.Bootstrap;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.concurrent.EventExecutor;
import io.netty.channel.ChannelFactory;
import io.netty.channel.ReflectiveChannelFactory;
import io.netty.channel.EventLoop;
import io.netty.handler.codec.dns.DnsClass;
import io.netty.handler.codec.dns.DnsQuestion;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReferenceArray;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.dns.DnsQueryEncoder;
import io.netty.handler.codec.dns.DnsResponseDecoder;
import io.netty.channel.socket.InternetProtocolFamily;
import io.netty.util.internal.logging.InternalLogger;
import java.net.InetSocketAddress;
import io.netty.resolver.SimpleNameResolver;

public class DnsNameResolver extends SimpleNameResolver<InetSocketAddress>
{
    private static final InternalLogger logger;
    static final InetSocketAddress ANY_LOCAL_ADDR;
    private static final InternetProtocolFamily[] DEFAULT_RESOLVE_ADDRESS_TYPES;
    private static final DnsResponseDecoder DECODER;
    private static final DnsQueryEncoder ENCODER;
    final Iterable<InetSocketAddress> nameServerAddresses;
    final ChannelFuture bindFuture;
    final DatagramChannel ch;
    final AtomicReferenceArray<DnsQueryContext> promises;
    final ConcurrentMap<DnsQuestion, DnsCacheEntry> queryCache;
    private final DnsResponseHandler responseHandler;
    private volatile long queryTimeoutMillis;
    private volatile int minTtl;
    private volatile int maxTtl;
    private volatile int negativeTtl;
    private volatile int maxTriesPerQuery;
    private volatile InternetProtocolFamily[] resolveAddressTypes;
    private volatile boolean recursionDesired;
    private volatile int maxQueriesPerResolve;
    private volatile int maxPayloadSize;
    private volatile DnsClass maxPayloadSizeClass;
    
    public DnsNameResolver(final EventLoop eventLoop, final Class<? extends DatagramChannel> channelType, final InetSocketAddress nameServerAddress) {
        this(eventLoop, channelType, DnsNameResolver.ANY_LOCAL_ADDR, nameServerAddress);
    }
    
    public DnsNameResolver(final EventLoop eventLoop, final Class<? extends DatagramChannel> channelType, final InetSocketAddress localAddress, final InetSocketAddress nameServerAddress) {
        this(eventLoop, new ReflectiveChannelFactory<DatagramChannel>(channelType), localAddress, nameServerAddress);
    }
    
    public DnsNameResolver(final EventLoop eventLoop, final ChannelFactory<? extends DatagramChannel> channelFactory, final InetSocketAddress nameServerAddress) {
        this(eventLoop, channelFactory, DnsNameResolver.ANY_LOCAL_ADDR, nameServerAddress);
    }
    
    public DnsNameResolver(final EventLoop eventLoop, final ChannelFactory<? extends DatagramChannel> channelFactory, final InetSocketAddress localAddress, final InetSocketAddress nameServerAddress) {
        this(eventLoop, channelFactory, localAddress, DnsServerAddresses.singleton(nameServerAddress));
    }
    
    public DnsNameResolver(final EventLoop eventLoop, final Class<? extends DatagramChannel> channelType, final Iterable<InetSocketAddress> nameServerAddresses) {
        this(eventLoop, channelType, DnsNameResolver.ANY_LOCAL_ADDR, nameServerAddresses);
    }
    
    public DnsNameResolver(final EventLoop eventLoop, final Class<? extends DatagramChannel> channelType, final InetSocketAddress localAddress, final Iterable<InetSocketAddress> nameServerAddresses) {
        this(eventLoop, new ReflectiveChannelFactory<DatagramChannel>(channelType), localAddress, nameServerAddresses);
    }
    
    public DnsNameResolver(final EventLoop eventLoop, final ChannelFactory<? extends DatagramChannel> channelFactory, final Iterable<InetSocketAddress> nameServerAddresses) {
        this(eventLoop, channelFactory, DnsNameResolver.ANY_LOCAL_ADDR, nameServerAddresses);
    }
    
    public DnsNameResolver(final EventLoop eventLoop, final ChannelFactory<? extends DatagramChannel> channelFactory, final InetSocketAddress localAddress, final Iterable<InetSocketAddress> nameServerAddresses) {
        super(eventLoop);
        this.promises = new AtomicReferenceArray<DnsQueryContext>(65536);
        this.queryCache = PlatformDependent.newConcurrentHashMap();
        this.responseHandler = new DnsResponseHandler();
        this.queryTimeoutMillis = 5000L;
        this.maxTtl = Integer.MAX_VALUE;
        this.maxTriesPerQuery = 2;
        this.resolveAddressTypes = DnsNameResolver.DEFAULT_RESOLVE_ADDRESS_TYPES;
        this.recursionDesired = true;
        this.maxQueriesPerResolve = 8;
        if (channelFactory == null) {
            throw new NullPointerException("channelFactory");
        }
        if (nameServerAddresses == null) {
            throw new NullPointerException("nameServerAddresses");
        }
        if (!nameServerAddresses.iterator().hasNext()) {
            throw new NullPointerException("nameServerAddresses is empty");
        }
        if (localAddress == null) {
            throw new NullPointerException("localAddress");
        }
        this.nameServerAddresses = nameServerAddresses;
        this.bindFuture = this.newChannel(channelFactory, localAddress);
        this.ch = (DatagramChannel)this.bindFuture.channel();
        this.setMaxPayloadSize(4096);
    }
    
    private ChannelFuture newChannel(final ChannelFactory<? extends DatagramChannel> channelFactory, final InetSocketAddress localAddress) {
        final Bootstrap b = new Bootstrap();
        b.group(this.executor());
        b.channelFactory(channelFactory);
        b.handler(new ChannelInitializer<DatagramChannel>() {
            @Override
            protected void initChannel(final DatagramChannel ch) throws Exception {
                ch.pipeline().addLast(DnsNameResolver.DECODER, DnsNameResolver.ENCODER, DnsNameResolver.this.responseHandler);
            }
        });
        final ChannelFuture bindFuture = b.bind(localAddress);
        bindFuture.channel().closeFuture().addListener((GenericFutureListener<? extends Future<? super Void>>)new ChannelFutureListener() {
            @Override
            public void operationComplete(final ChannelFuture future) throws Exception {
                DnsNameResolver.this.clearCache();
            }
        });
        return bindFuture;
    }
    
    public int minTtl() {
        return this.minTtl;
    }
    
    public int maxTtl() {
        return this.maxTtl;
    }
    
    public DnsNameResolver setTtl(final int minTtl, final int maxTtl) {
        if (minTtl < 0) {
            throw new IllegalArgumentException("minTtl: " + minTtl + " (expected: >= 0)");
        }
        if (maxTtl < 0) {
            throw new IllegalArgumentException("maxTtl: " + maxTtl + " (expected: >= 0)");
        }
        if (minTtl > maxTtl) {
            throw new IllegalArgumentException("minTtl: " + minTtl + ", maxTtl: " + maxTtl + " (expected: 0 <= minTtl <= maxTtl)");
        }
        this.maxTtl = maxTtl;
        this.minTtl = minTtl;
        return this;
    }
    
    public int negativeTtl() {
        return this.negativeTtl;
    }
    
    public DnsNameResolver setNegativeTtl(final int negativeTtl) {
        if (negativeTtl < 0) {
            throw new IllegalArgumentException("negativeTtl: " + negativeTtl + " (expected: >= 0)");
        }
        this.negativeTtl = negativeTtl;
        return this;
    }
    
    public long queryTimeoutMillis() {
        return this.queryTimeoutMillis;
    }
    
    public DnsNameResolver setQueryTimeoutMillis(final long queryTimeoutMillis) {
        if (queryTimeoutMillis < 0L) {
            throw new IllegalArgumentException("queryTimeoutMillis: " + queryTimeoutMillis + " (expected: >= 0)");
        }
        this.queryTimeoutMillis = queryTimeoutMillis;
        return this;
    }
    
    public int maxTriesPerQuery() {
        return this.maxTriesPerQuery;
    }
    
    public DnsNameResolver setMaxTriesPerQuery(final int maxTriesPerQuery) {
        if (maxTriesPerQuery < 1) {
            throw new IllegalArgumentException("maxTries: " + maxTriesPerQuery + " (expected: > 0)");
        }
        this.maxTriesPerQuery = maxTriesPerQuery;
        return this;
    }
    
    public List<InternetProtocolFamily> resolveAddressTypes() {
        return Arrays.asList(this.resolveAddressTypes);
    }
    
    InternetProtocolFamily[] resolveAddressTypesUnsafe() {
        return this.resolveAddressTypes;
    }
    
    public DnsNameResolver setResolveAddressTypes(final InternetProtocolFamily... resolveAddressTypes) {
        if (resolveAddressTypes == null) {
            throw new NullPointerException("resolveAddressTypes");
        }
        final List<InternetProtocolFamily> list = new ArrayList<InternetProtocolFamily>(InternetProtocolFamily.values().length);
        for (final InternetProtocolFamily f : resolveAddressTypes) {
            if (f == null) {
                break;
            }
            if (!list.contains(f)) {
                list.add(f);
            }
        }
        if (list.isEmpty()) {
            throw new IllegalArgumentException("no protocol family specified");
        }
        this.resolveAddressTypes = list.toArray(new InternetProtocolFamily[list.size()]);
        return this;
    }
    
    public DnsNameResolver setResolveAddressTypes(final Iterable<InternetProtocolFamily> resolveAddressTypes) {
        if (resolveAddressTypes == null) {
            throw new NullPointerException("resolveAddressTypes");
        }
        final List<InternetProtocolFamily> list = new ArrayList<InternetProtocolFamily>(InternetProtocolFamily.values().length);
        for (final InternetProtocolFamily f : resolveAddressTypes) {
            if (f == null) {
                break;
            }
            if (list.contains(f)) {
                continue;
            }
            list.add(f);
        }
        if (list.isEmpty()) {
            throw new IllegalArgumentException("no protocol family specified");
        }
        this.resolveAddressTypes = list.toArray(new InternetProtocolFamily[list.size()]);
        return this;
    }
    
    public boolean isRecursionDesired() {
        return this.recursionDesired;
    }
    
    public DnsNameResolver setRecursionDesired(final boolean recursionDesired) {
        this.recursionDesired = recursionDesired;
        return this;
    }
    
    public int maxQueriesPerResolve() {
        return this.maxQueriesPerResolve;
    }
    
    public DnsNameResolver setMaxQueriesPerResolve(final int maxQueriesPerResolve) {
        if (maxQueriesPerResolve <= 0) {
            throw new IllegalArgumentException("maxQueriesPerResolve: " + maxQueriesPerResolve + " (expected: > 0)");
        }
        this.maxQueriesPerResolve = maxQueriesPerResolve;
        return this;
    }
    
    public int maxPayloadSize() {
        return this.maxPayloadSize;
    }
    
    public DnsNameResolver setMaxPayloadSize(final int maxPayloadSize) {
        if (maxPayloadSize <= 0) {
            throw new IllegalArgumentException("maxPayloadSize: " + maxPayloadSize + " (expected: > 0)");
        }
        if (this.maxPayloadSize == maxPayloadSize) {
            return this;
        }
        this.maxPayloadSize = maxPayloadSize;
        this.maxPayloadSizeClass = DnsClass.valueOf(maxPayloadSize);
        this.ch.config().setRecvByteBufAllocator((RecvByteBufAllocator)new FixedRecvByteBufAllocator(maxPayloadSize));
        return this;
    }
    
    DnsClass maxPayloadSizeClass() {
        return this.maxPayloadSizeClass;
    }
    
    public DnsNameResolver clearCache() {
        final Iterator<Map.Entry<DnsQuestion, DnsCacheEntry>> i = (Iterator<Map.Entry<DnsQuestion, DnsCacheEntry>>)this.queryCache.entrySet().iterator();
        while (i.hasNext()) {
            final Map.Entry<DnsQuestion, DnsCacheEntry> e = i.next();
            i.remove();
            e.getValue().release();
        }
        return this;
    }
    
    public boolean clearCache(final DnsQuestion question) {
        final DnsCacheEntry e = this.queryCache.remove(question);
        if (e != null) {
            e.release();
            return true;
        }
        return false;
    }
    
    @Override
    public void close() {
        this.ch.close();
    }
    
    @Override
    protected EventLoop executor() {
        return (EventLoop)super.executor();
    }
    
    @Override
    protected boolean doIsResolved(final InetSocketAddress address) {
        return !address.isUnresolved();
    }
    
    @Override
    protected void doResolve(final InetSocketAddress unresolvedAddress, final Promise<InetSocketAddress> promise) throws Exception {
        final String hostname = IDN.toASCII(hostname(unresolvedAddress));
        final int port = unresolvedAddress.getPort();
        final DnsNameResolverContext ctx = new DnsNameResolverContext(this, hostname, port, promise);
        ctx.resolve();
    }
    
    private static String hostname(final InetSocketAddress addr) {
        if (PlatformDependent.javaVersion() < 7) {
            return addr.getHostName();
        }
        return addr.getHostString();
    }
    
    public Future<DnsResponse> query(final DnsQuestion question) {
        return this.query(this.nameServerAddresses, question);
    }
    
    public Future<DnsResponse> query(final DnsQuestion question, final Promise<DnsResponse> promise) {
        return this.query(this.nameServerAddresses, question, promise);
    }
    
    public Future<DnsResponse> query(final Iterable<InetSocketAddress> nameServerAddresses, final DnsQuestion question) {
        if (nameServerAddresses == null) {
            throw new NullPointerException("nameServerAddresses");
        }
        if (question == null) {
            throw new NullPointerException("question");
        }
        final EventLoop eventLoop = this.ch.eventLoop();
        final DnsCacheEntry cachedResult = this.queryCache.get(question);
        if (cachedResult == null) {
            return this.query0(nameServerAddresses, question, eventLoop.newPromise());
        }
        if (cachedResult.response != null) {
            return eventLoop.newSucceededFuture(cachedResult.response.retain());
        }
        return eventLoop.newFailedFuture(cachedResult.cause);
    }
    
    public Future<DnsResponse> query(final Iterable<InetSocketAddress> nameServerAddresses, final DnsQuestion question, final Promise<DnsResponse> promise) {
        if (nameServerAddresses == null) {
            throw new NullPointerException("nameServerAddresses");
        }
        if (question == null) {
            throw new NullPointerException("question");
        }
        if (promise == null) {
            throw new NullPointerException("promise");
        }
        final DnsCacheEntry cachedResult = this.queryCache.get(question);
        if (cachedResult == null) {
            return this.query0(nameServerAddresses, question, promise);
        }
        if (cachedResult.response != null) {
            return promise.setSuccess(cachedResult.response.retain());
        }
        return promise.setFailure(cachedResult.cause);
    }
    
    private Future<DnsResponse> query0(final Iterable<InetSocketAddress> nameServerAddresses, final DnsQuestion question, final Promise<DnsResponse> promise) {
        try {
            new DnsQueryContext(this, nameServerAddresses, question, promise).query();
            return promise;
        }
        catch (Exception e) {
            return promise.setFailure(e);
        }
    }
    
    void cache(final DnsQuestion question, final DnsCacheEntry entry, final long delaySeconds) {
        final DnsCacheEntry oldEntry = this.queryCache.put(question, entry);
        if (oldEntry != null) {
            oldEntry.release();
        }
        boolean scheduled = false;
        try {
            entry.expirationFuture = this.ch.eventLoop().schedule((Runnable)new OneTimeTask() {
                @Override
                public void run() {
                    DnsNameResolver.this.clearCache(question);
                }
            }, delaySeconds, TimeUnit.SECONDS);
            scheduled = true;
        }
        finally {
            if (!scheduled) {
                this.clearCache(question);
                entry.release();
            }
        }
    }
    
    static {
        logger = InternalLoggerFactory.getInstance(DnsNameResolver.class);
        ANY_LOCAL_ADDR = new InetSocketAddress(0);
        DEFAULT_RESOLVE_ADDRESS_TYPES = new InternetProtocolFamily[2];
        if ("true".equalsIgnoreCase(SystemPropertyUtil.get("java.net.preferIPv6Addresses"))) {
            DnsNameResolver.DEFAULT_RESOLVE_ADDRESS_TYPES[0] = InternetProtocolFamily.IPv6;
            DnsNameResolver.DEFAULT_RESOLVE_ADDRESS_TYPES[1] = InternetProtocolFamily.IPv4;
            DnsNameResolver.logger.debug("-Djava.net.preferIPv6Addresses: true");
        }
        else {
            DnsNameResolver.DEFAULT_RESOLVE_ADDRESS_TYPES[0] = InternetProtocolFamily.IPv4;
            DnsNameResolver.DEFAULT_RESOLVE_ADDRESS_TYPES[1] = InternetProtocolFamily.IPv6;
            DnsNameResolver.logger.debug("-Djava.net.preferIPv6Addresses: false");
        }
        DECODER = new DnsResponseDecoder();
        ENCODER = new DnsQueryEncoder();
    }
    
    private final class DnsResponseHandler extends ChannelHandlerAdapter
    {
        @Override
        public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
            try {
                final DnsResponse res = (DnsResponse)msg;
                final int queryId = res.header().id();
                if (DnsNameResolver.logger.isDebugEnabled()) {
                    DnsNameResolver.logger.debug("{} RECEIVED: [{}: {}], {}", DnsNameResolver.this.ch, queryId, res.sender(), res);
                }
                final DnsQueryContext qCtx = DnsNameResolver.this.promises.get(queryId);
                if (qCtx == null) {
                    if (DnsNameResolver.logger.isWarnEnabled()) {
                        DnsNameResolver.logger.warn("Received a DNS response with an unknown ID: {}", (Object)queryId);
                    }
                    return;
                }
                final List<DnsQuestion> questions = res.questions();
                if (questions.size() != 1) {
                    DnsNameResolver.logger.warn("Received a DNS response with invalid number of questions: {}", res);
                    return;
                }
                final DnsQuestion q = qCtx.question();
                if (!q.equals(questions.get(0))) {
                    DnsNameResolver.logger.warn("Received a mismatching DNS response: {}", res);
                    return;
                }
                final ScheduledFuture<?> timeoutFuture = qCtx.timeoutFuture();
                if (timeoutFuture != null) {
                    timeoutFuture.cancel(false);
                }
                if (res.header().responseCode() == DnsResponseCode.NOERROR) {
                    this.cache(q, res);
                    DnsNameResolver.this.promises.set(queryId, null);
                    final Promise<DnsResponse> qPromise = qCtx.promise();
                    if (qPromise.setUncancellable()) {
                        qPromise.setSuccess(res.retain());
                    }
                }
                else {
                    qCtx.retry(res.sender(), "response code: " + res.header().responseCode() + " with " + res.answers().size() + " answer(s) and " + res.authorityResources().size() + " authority resource(s)");
                }
            }
            finally {
                ReferenceCountUtil.safeRelease(msg);
            }
        }
        
        private void cache(final DnsQuestion question, final DnsResponse res) {
            final int maxTtl = DnsNameResolver.this.maxTtl();
            if (maxTtl == 0) {
                return;
            }
            long ttl = Long.MAX_VALUE;
            for (final DnsResource r : res.answers()) {
                final long rTtl = r.timeToLive();
                if (ttl > rTtl) {
                    ttl = rTtl;
                }
            }
            ttl = Math.max(DnsNameResolver.this.minTtl(), Math.min(maxTtl, ttl));
            DnsNameResolver.this.cache(question, new DnsCacheEntry(res), ttl);
        }
        
        @Override
        public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
            DnsNameResolver.logger.warn("Unexpected exception: ", cause);
        }
    }
    
    static final class DnsCacheEntry
    {
        final DnsResponse response;
        final Throwable cause;
        volatile ScheduledFuture<?> expirationFuture;
        
        DnsCacheEntry(final DnsResponse response) {
            this.response = response.retain();
            this.cause = null;
        }
        
        DnsCacheEntry(final Throwable cause) {
            this.cause = cause;
            this.response = null;
        }
        
        void release() {
            final DnsResponse response = this.response;
            if (response != null) {
                ReferenceCountUtil.safeRelease(response);
            }
            final ScheduledFuture<?> expirationFuture = this.expirationFuture;
            if (expirationFuture != null) {
                expirationFuture.cancel(false);
            }
        }
    }
}
