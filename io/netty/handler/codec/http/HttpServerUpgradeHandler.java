// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http;

import io.netty.handler.codec.Headers;
import io.netty.util.ReferenceCounted;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.LinkedHashSet;
import java.util.Set;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Future;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.AsciiString;
import io.netty.util.ReferenceCountUtil;
import java.util.List;
import io.netty.channel.ChannelHandlerContext;
import java.util.Iterator;
import java.util.Locale;
import java.util.LinkedHashMap;
import java.util.Collection;
import java.util.Map;

public class HttpServerUpgradeHandler extends HttpObjectAggregator
{
    private final Map<String, UpgradeCodec> upgradeCodecMap;
    private final SourceCodec sourceCodec;
    private boolean handlingUpgrade;
    
    public HttpServerUpgradeHandler(final SourceCodec sourceCodec, final Collection<UpgradeCodec> upgradeCodecs, final int maxContentLength) {
        super(maxContentLength);
        if (sourceCodec == null) {
            throw new NullPointerException("sourceCodec");
        }
        if (upgradeCodecs == null) {
            throw new NullPointerException("upgradeCodecs");
        }
        this.sourceCodec = sourceCodec;
        this.upgradeCodecMap = new LinkedHashMap<String, UpgradeCodec>(upgradeCodecs.size());
        for (final UpgradeCodec upgradeCodec : upgradeCodecs) {
            final String name = upgradeCodec.protocol().toUpperCase(Locale.US);
            this.upgradeCodecMap.put(name, upgradeCodec);
        }
    }
    
    @Override
    protected void decode(final ChannelHandlerContext ctx, final HttpObject msg, final List<Object> out) throws Exception {
        if (!(this.handlingUpgrade |= isUpgradeRequest(msg))) {
            ReferenceCountUtil.retain(msg);
            out.add(msg);
            return;
        }
        FullHttpRequest fullRequest;
        if (msg instanceof FullHttpRequest) {
            fullRequest = (FullHttpRequest)msg;
            ReferenceCountUtil.retain(msg);
            out.add(msg);
        }
        else {
            super.decode(ctx, msg, out);
            if (out.isEmpty()) {
                return;
            }
            assert out.size() == 1;
            this.handlingUpgrade = false;
            fullRequest = out.get(0);
        }
        if (this.upgrade(ctx, fullRequest)) {
            out.clear();
        }
    }
    
    private static boolean isUpgradeRequest(final HttpObject msg) {
        return msg instanceof HttpRequest && ((Headers<AsciiString>)((HttpRequest)msg).headers()).get(HttpHeaderNames.UPGRADE) != null;
    }
    
    private boolean upgrade(final ChannelHandlerContext ctx, final FullHttpRequest request) {
        final CharSequence upgradeHeader = ((Headers<AsciiString>)request.headers()).get(HttpHeaderNames.UPGRADE);
        final UpgradeCodec upgradeCodec = this.selectUpgradeCodec(upgradeHeader);
        if (upgradeCodec == null) {
            return false;
        }
        final CharSequence connectionHeader = ((Headers<AsciiString>)request.headers()).get(HttpHeaderNames.CONNECTION);
        if (connectionHeader == null) {
            return false;
        }
        final Collection<String> requiredHeaders = upgradeCodec.requiredUpgradeHeaders();
        final Set<CharSequence> values = splitHeader(connectionHeader);
        if (!values.contains(HttpHeaderNames.UPGRADE) || !values.containsAll(requiredHeaders)) {
            return false;
        }
        for (final String requiredHeader : requiredHeaders) {
            if (!((Headers<String>)request.headers()).contains(requiredHeader)) {
                return false;
            }
        }
        final UpgradeEvent event = new UpgradeEvent(upgradeCodec.protocol(), request);
        final FullHttpResponse upgradeResponse = createUpgradeResponse(upgradeCodec);
        upgradeCodec.prepareUpgradeResponse(ctx, request, upgradeResponse);
        ctx.writeAndFlush(upgradeResponse).addListener((GenericFutureListener<? extends Future<? super Void>>)new ChannelFutureListener() {
            @Override
            public void operationComplete(final ChannelFuture future) throws Exception {
                try {
                    if (future.isSuccess()) {
                        HttpServerUpgradeHandler.this.sourceCodec.upgradeFrom(ctx);
                        upgradeCodec.upgradeTo(ctx, request, upgradeResponse);
                        ctx.fireUserEventTriggered(event.retain());
                        ctx.pipeline().remove(HttpServerUpgradeHandler.this);
                    }
                    else {
                        future.channel().close();
                    }
                }
                finally {
                    event.release();
                }
            }
        });
        return true;
    }
    
    private UpgradeCodec selectUpgradeCodec(final CharSequence upgradeHeader) {
        final Set<CharSequence> requestedProtocols = splitHeader(upgradeHeader);
        final Set<String> supportedProtocols = new LinkedHashSet<String>(this.upgradeCodecMap.keySet());
        supportedProtocols.retainAll(requestedProtocols);
        if (!supportedProtocols.isEmpty()) {
            final String protocol = supportedProtocols.iterator().next().toUpperCase(Locale.US);
            return this.upgradeCodecMap.get(protocol);
        }
        return null;
    }
    
    private static FullHttpResponse createUpgradeResponse(final UpgradeCodec upgradeCodec) {
        final DefaultFullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.SWITCHING_PROTOCOLS);
        res.headers().add((CharSequence)HttpHeaderNames.CONNECTION, (CharSequence)HttpHeaderValues.UPGRADE);
        res.headers().add((CharSequence)HttpHeaderNames.UPGRADE, (CharSequence)upgradeCodec.protocol());
        res.headers().add((CharSequence)HttpHeaderNames.CONTENT_LENGTH, (CharSequence)"0");
        return res;
    }
    
    private static Set<CharSequence> splitHeader(final CharSequence header) {
        final StringBuilder builder = new StringBuilder(header.length());
        final Set<CharSequence> protocols = new TreeSet<CharSequence>(AsciiString.CHARSEQUENCE_CASE_INSENSITIVE_ORDER);
        for (int i = 0; i < header.length(); ++i) {
            final char c = header.charAt(i);
            if (!Character.isWhitespace(c)) {
                if (c == ',') {
                    protocols.add(builder.toString());
                    builder.setLength(0);
                }
                else {
                    builder.append(c);
                }
            }
        }
        if (builder.length() > 0) {
            protocols.add(builder.toString());
        }
        return protocols;
    }
    
    public static final class UpgradeEvent implements ReferenceCounted
    {
        private final String protocol;
        private final FullHttpRequest upgradeRequest;
        
        private UpgradeEvent(final String protocol, final FullHttpRequest upgradeRequest) {
            this.protocol = protocol;
            this.upgradeRequest = upgradeRequest;
        }
        
        public String protocol() {
            return this.protocol;
        }
        
        public FullHttpRequest upgradeRequest() {
            return this.upgradeRequest;
        }
        
        @Override
        public int refCnt() {
            return this.upgradeRequest.refCnt();
        }
        
        @Override
        public UpgradeEvent retain() {
            this.upgradeRequest.retain();
            return this;
        }
        
        @Override
        public UpgradeEvent retain(final int increment) {
            this.upgradeRequest.retain(increment);
            return this;
        }
        
        @Override
        public UpgradeEvent touch() {
            this.upgradeRequest.touch();
            return this;
        }
        
        @Override
        public UpgradeEvent touch(final Object hint) {
            this.upgradeRequest.touch(hint);
            return this;
        }
        
        @Override
        public boolean release() {
            return this.upgradeRequest.release();
        }
        
        @Override
        public boolean release(final int decrement) {
            return this.upgradeRequest.release();
        }
        
        @Override
        public String toString() {
            return "UpgradeEvent [protocol=" + this.protocol + ", upgradeRequest=" + this.upgradeRequest + ']';
        }
    }
    
    public interface UpgradeCodec
    {
        String protocol();
        
        Collection<String> requiredUpgradeHeaders();
        
        void prepareUpgradeResponse(final ChannelHandlerContext p0, final FullHttpRequest p1, final FullHttpResponse p2);
        
        void upgradeTo(final ChannelHandlerContext p0, final FullHttpRequest p1, final FullHttpResponse p2);
    }
    
    public interface SourceCodec
    {
        void upgradeFrom(final ChannelHandlerContext p0);
    }
}
