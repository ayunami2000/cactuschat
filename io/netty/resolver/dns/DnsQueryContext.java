// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.resolver.dns;

import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.StringUtil;
import java.util.concurrent.TimeUnit;
import io.netty.util.internal.OneTimeTask;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Future;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.dns.DnsQuery;
import java.net.UnknownHostException;
import io.netty.util.internal.ThreadLocalRandom;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.dns.DnsType;
import io.netty.util.concurrent.ScheduledFuture;
import java.net.InetSocketAddress;
import java.util.Iterator;
import io.netty.handler.codec.dns.DnsResource;
import io.netty.handler.codec.dns.DnsQuestion;
import io.netty.handler.codec.dns.DnsResponse;
import io.netty.util.concurrent.Promise;
import io.netty.util.internal.logging.InternalLogger;

final class DnsQueryContext
{
    private static final InternalLogger logger;
    private final DnsNameResolver parent;
    private final Promise<DnsResponse> promise;
    private final int id;
    private final DnsQuestion question;
    private final DnsResource optResource;
    private final Iterator<InetSocketAddress> nameServerAddresses;
    private final boolean recursionDesired;
    private final int maxTries;
    private int remainingTries;
    private volatile ScheduledFuture<?> timeoutFuture;
    private StringBuilder trace;
    
    DnsQueryContext(final DnsNameResolver parent, final Iterable<InetSocketAddress> nameServerAddresses, final DnsQuestion question, final Promise<DnsResponse> promise) {
        this.parent = parent;
        this.promise = promise;
        this.question = question;
        this.id = this.allocateId();
        this.recursionDesired = parent.isRecursionDesired();
        this.maxTries = parent.maxTriesPerQuery();
        this.remainingTries = this.maxTries;
        this.optResource = new DnsResource("", DnsType.OPT, parent.maxPayloadSizeClass(), 0L, Unpooled.EMPTY_BUFFER);
        this.nameServerAddresses = nameServerAddresses.iterator();
    }
    
    private int allocateId() {
        int id = ThreadLocalRandom.current().nextInt(this.parent.promises.length());
        final int maxTries = this.parent.promises.length() << 1;
        int tries = 0;
        while (!this.parent.promises.compareAndSet(id, null, this)) {
            id = (id + 1 & 0xFFFF);
            if (++tries >= maxTries) {
                throw new IllegalStateException("query ID space exhausted: " + this.question);
            }
        }
        return id;
    }
    
    Promise<DnsResponse> promise() {
        return this.promise;
    }
    
    DnsQuestion question() {
        return this.question;
    }
    
    ScheduledFuture<?> timeoutFuture() {
        return this.timeoutFuture;
    }
    
    void query() {
        final DnsQuestion question = this.question;
        if (this.remainingTries <= 0 || !this.nameServerAddresses.hasNext()) {
            this.parent.promises.set(this.id, null);
            final int tries = this.maxTries - this.remainingTries;
            UnknownHostException cause;
            if (tries > 1) {
                cause = new UnknownHostException("failed to resolve " + question + " after " + tries + " attempts:" + (Object)this.trace);
            }
            else {
                cause = new UnknownHostException("failed to resolve " + question + ':' + (Object)this.trace);
            }
            this.cache(question, cause);
            this.promise.tryFailure(cause);
            return;
        }
        --this.remainingTries;
        final InetSocketAddress nameServerAddr = this.nameServerAddresses.next();
        final DnsQuery query = new DnsQuery(this.id, nameServerAddr);
        query.addQuestion(question);
        query.header().setRecursionDesired(this.recursionDesired);
        query.addAdditionalResource(this.optResource);
        if (DnsQueryContext.logger.isDebugEnabled()) {
            DnsQueryContext.logger.debug("{} WRITE: [{}: {}], {}", this.parent.ch, this.id, nameServerAddr, question);
        }
        this.sendQuery(query, nameServerAddr);
    }
    
    private void sendQuery(final DnsQuery query, final InetSocketAddress nameServerAddr) {
        if (this.parent.bindFuture.isDone()) {
            this.writeQuery(query, nameServerAddr);
        }
        else {
            this.parent.bindFuture.addListener((GenericFutureListener<? extends Future<? super Void>>)new ChannelFutureListener() {
                @Override
                public void operationComplete(final ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        DnsQueryContext.this.writeQuery(query, nameServerAddr);
                    }
                    else {
                        DnsQueryContext.this.promise.tryFailure(future.cause());
                    }
                }
            });
        }
    }
    
    private void writeQuery(final DnsQuery query, final InetSocketAddress nameServerAddr) {
        final ChannelFuture writeFuture = this.parent.ch.writeAndFlush(query);
        if (writeFuture.isDone()) {
            this.onQueryWriteCompletion(writeFuture, nameServerAddr);
        }
        else {
            writeFuture.addListener((GenericFutureListener<? extends Future<? super Void>>)new ChannelFutureListener() {
                @Override
                public void operationComplete(final ChannelFuture future) throws Exception {
                    DnsQueryContext.this.onQueryWriteCompletion(writeFuture, nameServerAddr);
                }
            });
        }
    }
    
    private void onQueryWriteCompletion(final ChannelFuture writeFuture, final InetSocketAddress nameServerAddr) {
        if (!writeFuture.isSuccess()) {
            this.retry(nameServerAddr, "failed to send a query: " + writeFuture.cause());
            return;
        }
        final long queryTimeoutMillis = this.parent.queryTimeoutMillis();
        if (queryTimeoutMillis > 0L) {
            this.timeoutFuture = this.parent.ch.eventLoop().schedule((Runnable)new OneTimeTask() {
                @Override
                public void run() {
                    if (DnsQueryContext.this.promise.isDone()) {
                        return;
                    }
                    DnsQueryContext.this.retry(nameServerAddr, "query timed out after " + queryTimeoutMillis + " milliseconds");
                }
            }, queryTimeoutMillis, TimeUnit.MILLISECONDS);
        }
    }
    
    void retry(final InetSocketAddress nameServerAddr, final String message) {
        if (this.promise.isCancelled()) {
            return;
        }
        if (this.trace == null) {
            this.trace = new StringBuilder(128);
        }
        this.trace.append(StringUtil.NEWLINE);
        this.trace.append("\tfrom ");
        this.trace.append(nameServerAddr);
        this.trace.append(": ");
        this.trace.append(message);
        this.query();
    }
    
    private void cache(final DnsQuestion question, final Throwable cause) {
        final int negativeTtl = this.parent.negativeTtl();
        if (negativeTtl == 0) {
            return;
        }
        this.parent.cache(question, new DnsNameResolver.DnsCacheEntry(cause), negativeTtl);
    }
    
    static {
        logger = InternalLoggerFactory.getInstance(DnsQueryContext.class);
    }
}
