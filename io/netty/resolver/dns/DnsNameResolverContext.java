// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.resolver.dns;

import io.netty.util.internal.StringUtil;
import io.netty.util.CharsetUtil;
import java.net.Inet6Address;
import java.net.Inet4Address;
import io.netty.handler.codec.dns.DnsClass;
import java.util.HashMap;
import io.netty.buffer.ByteBuf;
import java.util.Iterator;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Locale;
import io.netty.handler.codec.dns.DnsResource;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.handler.codec.dns.DnsQuestion;
import io.netty.handler.codec.dns.DnsType;
import java.util.Map;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.net.InetAddress;
import java.util.List;
import io.netty.util.concurrent.Future;
import java.util.Set;
import io.netty.channel.socket.InternetProtocolFamily;
import java.net.InetSocketAddress;
import io.netty.util.concurrent.Promise;
import io.netty.handler.codec.dns.DnsResponse;
import io.netty.util.concurrent.FutureListener;

final class DnsNameResolverContext
{
    private static final int INADDRSZ4 = 4;
    private static final int INADDRSZ6 = 16;
    private static final FutureListener<DnsResponse> RELEASE_RESPONSE;
    private final DnsNameResolver parent;
    private final Promise<InetSocketAddress> promise;
    private final String hostname;
    private final int port;
    private final int maxAllowedQueries;
    private final InternetProtocolFamily[] resolveAddressTypes;
    private final Set<Future<DnsResponse>> queriesInProgress;
    private List<InetAddress> resolvedAddresses;
    private StringBuilder trace;
    private int allowedQueries;
    private boolean triedCNAME;
    
    DnsNameResolverContext(final DnsNameResolver parent, final String hostname, final int port, final Promise<InetSocketAddress> promise) {
        this.queriesInProgress = Collections.newSetFromMap(new IdentityHashMap<Future<DnsResponse>, Boolean>());
        this.parent = parent;
        this.promise = promise;
        this.hostname = hostname;
        this.port = port;
        this.maxAllowedQueries = parent.maxQueriesPerResolve();
        this.resolveAddressTypes = parent.resolveAddressTypesUnsafe();
        this.allowedQueries = this.maxAllowedQueries;
    }
    
    void resolve() {
        for (final InternetProtocolFamily f : this.resolveAddressTypes) {
            DnsType type = null;
            switch (f) {
                case IPv4: {
                    type = DnsType.A;
                    break;
                }
                case IPv6: {
                    type = DnsType.AAAA;
                    break;
                }
                default: {
                    throw new Error();
                }
            }
            this.query(this.parent.nameServerAddresses, new DnsQuestion(this.hostname, type));
        }
    }
    
    private void query(final Iterable<InetSocketAddress> nameServerAddresses, final DnsQuestion question) {
        if (this.allowedQueries == 0 || this.promise.isCancelled()) {
            return;
        }
        --this.allowedQueries;
        final Future<DnsResponse> f = this.parent.query(nameServerAddresses, question);
        this.queriesInProgress.add(f);
        f.addListener(new FutureListener<DnsResponse>() {
            @Override
            public void operationComplete(final Future<DnsResponse> future) throws Exception {
                DnsNameResolverContext.this.queriesInProgress.remove(future);
                if (DnsNameResolverContext.this.promise.isDone()) {
                    return;
                }
                try {
                    if (future.isSuccess()) {
                        DnsNameResolverContext.this.onResponse(question, future.getNow());
                    }
                    else {
                        DnsNameResolverContext.this.addTrace(future.cause());
                    }
                }
                finally {
                    DnsNameResolverContext.this.tryToFinishResolve();
                }
            }
        });
    }
    
    void onResponse(final DnsQuestion question, final DnsResponse response) {
        final DnsType type = question.type();
        try {
            if (type == DnsType.A || type == DnsType.AAAA) {
                this.onResponseAorAAAA(type, question, response);
            }
            else if (type == DnsType.CNAME) {
                this.onResponseCNAME(question, response);
            }
        }
        finally {
            ReferenceCountUtil.safeRelease(response);
        }
    }
    
    private void onResponseAorAAAA(final DnsType qType, final DnsQuestion question, final DnsResponse response) {
        final Map<String, String> cnames = buildAliasMap(response);
        boolean found = false;
        for (final DnsResource r : response.answers()) {
            final DnsType type = r.type();
            if (type != DnsType.A && type != DnsType.AAAA) {
                continue;
            }
            final String qName = question.name().toLowerCase(Locale.US);
            final String rName = r.name().toLowerCase(Locale.US);
            if (!rName.equals(qName)) {
                String resolved = qName;
                do {
                    resolved = cnames.get(resolved);
                    if (rName.equals(resolved)) {
                        break;
                    }
                } while (resolved != null);
                if (resolved == null) {
                    continue;
                }
            }
            final ByteBuf content = r.content();
            final int contentLen = content.readableBytes();
            if (contentLen != 4 && contentLen != 16) {
                continue;
            }
            final byte[] addrBytes = new byte[contentLen];
            content.getBytes(content.readerIndex(), addrBytes);
            try {
                final InetAddress resolved2 = InetAddress.getByAddress(this.hostname, addrBytes);
                if (this.resolvedAddresses == null) {
                    this.resolvedAddresses = new ArrayList<InetAddress>();
                }
                this.resolvedAddresses.add(resolved2);
                found = true;
            }
            catch (UnknownHostException e) {
                throw new Error(e);
            }
        }
        if (found) {
            return;
        }
        this.addTrace(response.sender(), "no matching " + qType + " record found");
        if (!cnames.isEmpty()) {
            this.onResponseCNAME(question, response, cnames, false);
        }
    }
    
    private void onResponseCNAME(final DnsQuestion question, final DnsResponse response) {
        this.onResponseCNAME(question, response, buildAliasMap(response), true);
    }
    
    private void onResponseCNAME(final DnsQuestion question, final DnsResponse response, final Map<String, String> cnames, final boolean trace) {
        String resolved;
        final String name = resolved = question.name().toLowerCase(Locale.US);
        boolean found = false;
        while (true) {
            final String next = cnames.get(resolved);
            if (next == null) {
                break;
            }
            found = true;
            resolved = next;
        }
        if (found) {
            this.followCname(response.sender(), name, resolved);
        }
        else if (trace) {
            this.addTrace(response.sender(), "no matching CNAME record found");
        }
    }
    
    private static Map<String, String> buildAliasMap(final DnsResponse response) {
        Map<String, String> cnames = null;
        for (final DnsResource r : response.answers()) {
            final DnsType type = r.type();
            if (type != DnsType.CNAME) {
                continue;
            }
            final String content = decodeDomainName(r.content());
            if (content == null) {
                continue;
            }
            if (cnames == null) {
                cnames = new HashMap<String, String>();
            }
            cnames.put(r.name().toLowerCase(Locale.US), content.toLowerCase(Locale.US));
        }
        return (cnames != null) ? cnames : Collections.emptyMap();
    }
    
    void tryToFinishResolve() {
        if (!this.queriesInProgress.isEmpty()) {
            if (this.gotPreferredAddress()) {
                this.finishResolve();
            }
            return;
        }
        if (this.resolvedAddresses == null && !this.triedCNAME) {
            this.triedCNAME = true;
            this.query(this.parent.nameServerAddresses, new DnsQuestion(this.hostname, DnsType.CNAME, DnsClass.IN));
            return;
        }
        this.finishResolve();
    }
    
    private boolean gotPreferredAddress() {
        if (this.resolvedAddresses == null) {
            return false;
        }
        final int size = this.resolvedAddresses.size();
        switch (this.resolveAddressTypes[0]) {
            case IPv4: {
                for (int i = 0; i < size; ++i) {
                    if (this.resolvedAddresses.get(i) instanceof Inet4Address) {
                        return true;
                    }
                }
                break;
            }
            case IPv6: {
                for (int i = 0; i < size; ++i) {
                    if (this.resolvedAddresses.get(i) instanceof Inet6Address) {
                        return true;
                    }
                }
                break;
            }
        }
        return false;
    }
    
    private void finishResolve() {
        if (!this.queriesInProgress.isEmpty()) {
            final Iterator<Future<DnsResponse>> i = this.queriesInProgress.iterator();
            while (i.hasNext()) {
                final Future<DnsResponse> f = i.next();
                i.remove();
                if (!f.cancel(false)) {
                    f.addListener(DnsNameResolverContext.RELEASE_RESPONSE);
                }
            }
        }
        if (this.resolvedAddresses != null) {
            for (final InternetProtocolFamily f2 : this.resolveAddressTypes) {
                switch (f2) {
                    case IPv4: {
                        if (this.finishResolveWithIPv4()) {
                            return;
                        }
                        break;
                    }
                    case IPv6: {
                        if (this.finishResolveWithIPv6()) {
                            return;
                        }
                        break;
                    }
                }
            }
        }
        final int tries = this.maxAllowedQueries - this.allowedQueries;
        UnknownHostException cause;
        if (tries > 1) {
            cause = new UnknownHostException("failed to resolve " + this.hostname + " after " + tries + " queries:" + (Object)this.trace);
        }
        else {
            cause = new UnknownHostException("failed to resolve " + this.hostname + ':' + (Object)this.trace);
        }
        this.promise.tryFailure(cause);
    }
    
    private boolean finishResolveWithIPv4() {
        final List<InetAddress> resolvedAddresses = this.resolvedAddresses;
        for (int size = resolvedAddresses.size(), i = 0; i < size; ++i) {
            final InetAddress a = resolvedAddresses.get(i);
            if (a instanceof Inet4Address) {
                this.promise.trySuccess(new InetSocketAddress(a, this.port));
                return true;
            }
        }
        return false;
    }
    
    private boolean finishResolveWithIPv6() {
        final List<InetAddress> resolvedAddresses = this.resolvedAddresses;
        for (int size = resolvedAddresses.size(), i = 0; i < size; ++i) {
            final InetAddress a = resolvedAddresses.get(i);
            if (a instanceof Inet6Address) {
                this.promise.trySuccess(new InetSocketAddress(a, this.port));
                return true;
            }
        }
        return false;
    }
    
    static String decodeDomainName(final ByteBuf buf) {
        buf.markReaderIndex();
        try {
            int position = -1;
            int checked = 0;
            final int length = buf.writerIndex();
            final StringBuilder name = new StringBuilder(64);
            for (int len = buf.readUnsignedByte(); buf.isReadable() && len != 0; len = buf.readUnsignedByte()) {
                final boolean pointer = (len & 0xC0) == 0xC0;
                if (pointer) {
                    if (position == -1) {
                        position = buf.readerIndex() + 1;
                    }
                    buf.readerIndex((len & 0x3F) << 8 | buf.readUnsignedByte());
                    checked += 2;
                    if (checked >= length) {
                        return null;
                    }
                }
                else {
                    name.append(buf.toString(buf.readerIndex(), len, CharsetUtil.UTF_8)).append('.');
                    buf.skipBytes(len);
                }
            }
            if (position != -1) {
                buf.readerIndex(position);
            }
            if (name.length() == 0) {
                return null;
            }
            return name.substring(0, name.length() - 1);
        }
        finally {
            buf.resetReaderIndex();
        }
    }
    
    private void followCname(final InetSocketAddress nameServerAddr, final String name, final String cname) {
        if (this.trace == null) {
            this.trace = new StringBuilder(128);
        }
        this.trace.append(StringUtil.NEWLINE);
        this.trace.append("\tfrom ");
        this.trace.append(nameServerAddr);
        this.trace.append(": ");
        this.trace.append(name);
        this.trace.append(" CNAME ");
        this.trace.append(cname);
        this.query(this.parent.nameServerAddresses, new DnsQuestion(cname, DnsType.A, DnsClass.IN));
        this.query(this.parent.nameServerAddresses, new DnsQuestion(cname, DnsType.AAAA, DnsClass.IN));
    }
    
    private void addTrace(final InetSocketAddress nameServerAddr, final String msg) {
        if (this.trace == null) {
            this.trace = new StringBuilder(128);
        }
        this.trace.append(StringUtil.NEWLINE);
        this.trace.append("\tfrom ");
        this.trace.append(nameServerAddr);
        this.trace.append(": ");
        this.trace.append(msg);
    }
    
    private void addTrace(final Throwable cause) {
        if (this.trace == null) {
            this.trace = new StringBuilder(128);
        }
        this.trace.append(StringUtil.NEWLINE);
        this.trace.append("Caused by: ");
        this.trace.append(cause);
    }
    
    static {
        RELEASE_RESPONSE = new FutureListener<DnsResponse>() {
            @Override
            public void operationComplete(final Future<DnsResponse> future) {
                if (future.isSuccess()) {
                    future.getNow().release();
                }
            }
        };
    }
}
