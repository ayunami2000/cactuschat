// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http;

import io.netty.handler.codec.Headers;
import java.util.Iterator;
import java.util.Set;
import java.util.Collection;
import java.util.LinkedHashSet;
import io.netty.util.ReferenceCountUtil;
import io.netty.handler.codec.AsciiString;
import java.util.List;
import io.netty.channel.ChannelPromise;
import io.netty.channel.ChannelHandlerContext;

public class HttpClientUpgradeHandler extends HttpObjectAggregator
{
    private final SourceCodec sourceCodec;
    private final UpgradeCodec upgradeCodec;
    private boolean upgradeRequested;
    
    public HttpClientUpgradeHandler(final SourceCodec sourceCodec, final UpgradeCodec upgradeCodec, final int maxContentLength) {
        super(maxContentLength);
        if (sourceCodec == null) {
            throw new NullPointerException("sourceCodec");
        }
        if (upgradeCodec == null) {
            throw new NullPointerException("upgradeCodec");
        }
        this.sourceCodec = sourceCodec;
        this.upgradeCodec = upgradeCodec;
    }
    
    @Override
    public void write(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise promise) throws Exception {
        if (!(msg instanceof HttpRequest)) {
            super.write(ctx, msg, promise);
            return;
        }
        if (this.upgradeRequested) {
            promise.setFailure((Throwable)new IllegalStateException("Attempting to write HTTP request with upgrade in progress"));
            return;
        }
        this.upgradeRequested = true;
        this.setUpgradeRequestHeaders(ctx, (HttpRequest)msg);
        super.write(ctx, msg, promise);
        ctx.fireUserEventTriggered(UpgradeEvent.UPGRADE_ISSUED);
    }
    
    @Override
    protected void decode(final ChannelHandlerContext ctx, final HttpObject msg, final List<Object> out) throws Exception {
        FullHttpResponse response = null;
        try {
            if (!this.upgradeRequested) {
                throw new IllegalStateException("Read HTTP response without requesting protocol switch");
            }
            if (msg instanceof FullHttpResponse) {
                response = (FullHttpResponse)msg;
                response.retain();
                out.add(response);
            }
            else {
                super.decode(ctx, msg, out);
                if (out.isEmpty()) {
                    return;
                }
                assert out.size() == 1;
                response = out.get(0);
            }
            if (!HttpResponseStatus.SWITCHING_PROTOCOLS.equals(response.status())) {
                ctx.fireUserEventTriggered(UpgradeEvent.UPGRADE_REJECTED);
                removeThisHandler(ctx);
                return;
            }
            final CharSequence upgradeHeader = ((Headers<AsciiString>)response.headers()).get(HttpHeaderNames.UPGRADE);
            if (upgradeHeader == null) {
                throw new IllegalStateException("Switching Protocols response missing UPGRADE header");
            }
            if (!AsciiString.equalsIgnoreCase(this.upgradeCodec.protocol(), upgradeHeader)) {
                throw new IllegalStateException("Switching Protocols response with unexpected UPGRADE protocol: " + (Object)upgradeHeader);
            }
            this.sourceCodec.upgradeFrom(ctx);
            this.upgradeCodec.upgradeTo(ctx, response);
            ctx.fireUserEventTriggered(UpgradeEvent.UPGRADE_SUCCESSFUL);
            response.release();
            out.clear();
            removeThisHandler(ctx);
        }
        catch (Throwable t) {
            ReferenceCountUtil.release(response);
            ctx.fireExceptionCaught(t);
            removeThisHandler(ctx);
        }
    }
    
    private static void removeThisHandler(final ChannelHandlerContext ctx) {
        ctx.pipeline().remove(ctx.name());
    }
    
    private void setUpgradeRequestHeaders(final ChannelHandlerContext ctx, final HttpRequest request) {
        request.headers().set((CharSequence)HttpHeaderNames.UPGRADE, (CharSequence)this.upgradeCodec.protocol());
        final Set<String> connectionParts = new LinkedHashSet<String>(2);
        connectionParts.addAll(this.upgradeCodec.setUpgradeHeaders(ctx, request));
        final StringBuilder builder = new StringBuilder();
        for (final String part : connectionParts) {
            builder.append(part);
            builder.append(',');
        }
        builder.append(HttpHeaderNames.UPGRADE);
        request.headers().set((CharSequence)HttpHeaderNames.CONNECTION, (CharSequence)builder.toString());
    }
    
    public enum UpgradeEvent
    {
        UPGRADE_ISSUED, 
        UPGRADE_SUCCESSFUL, 
        UPGRADE_REJECTED;
    }
    
    public interface UpgradeCodec
    {
        String protocol();
        
        Collection<String> setUpgradeHeaders(final ChannelHandlerContext p0, final HttpRequest p1);
        
        void upgradeTo(final ChannelHandlerContext p0, final FullHttpResponse p1) throws Exception;
    }
    
    public interface SourceCodec
    {
        void upgradeFrom(final ChannelHandlerContext p0);
    }
}
