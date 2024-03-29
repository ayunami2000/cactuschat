// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http2;

import java.util.Collections;
import java.util.Iterator;
import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.CharsetUtil;
import io.netty.handler.codec.base64.Base64;
import io.netty.handler.codec.base64.Base64Dialect;
import io.netty.util.collection.IntObjectMap;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import java.util.Collection;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.ObjectUtil;
import java.util.List;
import io.netty.handler.codec.http.HttpClientUpgradeHandler;

public class Http2ClientUpgradeCodec implements HttpClientUpgradeHandler.UpgradeCodec
{
    private static final List<String> UPGRADE_HEADERS;
    private final String handlerName;
    private final Http2ConnectionHandler connectionHandler;
    
    public Http2ClientUpgradeCodec(final Http2ConnectionHandler connectionHandler) {
        this("http2ConnectionHandler", connectionHandler);
    }
    
    public Http2ClientUpgradeCodec(final String handlerName, final Http2ConnectionHandler connectionHandler) {
        this.handlerName = ObjectUtil.checkNotNull(handlerName, "handlerName");
        this.connectionHandler = ObjectUtil.checkNotNull(connectionHandler, "connectionHandler");
    }
    
    @Override
    public String protocol() {
        return "h2c-16";
    }
    
    @Override
    public Collection<String> setUpgradeHeaders(final ChannelHandlerContext ctx, final HttpRequest upgradeRequest) {
        final String settingsValue = this.getSettingsHeaderValue(ctx);
        upgradeRequest.headers().set((CharSequence)"HTTP2-Settings", (CharSequence)settingsValue);
        return Http2ClientUpgradeCodec.UPGRADE_HEADERS;
    }
    
    @Override
    public void upgradeTo(final ChannelHandlerContext ctx, final FullHttpResponse upgradeResponse) throws Exception {
        this.connectionHandler.onHttpClientUpgrade();
        ctx.pipeline().addAfter(ctx.name(), this.handlerName, this.connectionHandler);
    }
    
    private String getSettingsHeaderValue(final ChannelHandlerContext ctx) {
        ByteBuf buf = null;
        ByteBuf encodedBuf = null;
        try {
            final Http2Settings settings = this.connectionHandler.decoder().localSettings();
            final int payloadLength = 6 * settings.size();
            buf = ctx.alloc().buffer(payloadLength);
            for (final IntObjectMap.Entry<Long> entry : settings.entries()) {
                Http2CodecUtil.writeUnsignedShort(entry.key(), buf);
                Http2CodecUtil.writeUnsignedInt(entry.value(), buf);
            }
            encodedBuf = Base64.encode(buf, Base64Dialect.URL_SAFE);
            return encodedBuf.toString(CharsetUtil.UTF_8);
        }
        finally {
            ReferenceCountUtil.release(buf);
            ReferenceCountUtil.release(encodedBuf);
        }
    }
    
    static {
        UPGRADE_HEADERS = Collections.singletonList("HTTP2-Settings");
    }
}
