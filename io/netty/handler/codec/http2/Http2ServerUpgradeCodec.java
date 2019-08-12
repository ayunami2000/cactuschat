// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http2;

import io.netty.handler.codec.Headers;
import java.util.Collections;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.base64.Base64;
import io.netty.handler.codec.base64.Base64Dialect;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.AsciiString;
import io.netty.util.CharsetUtil;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.channel.ChannelHandlerContext;
import java.util.Collection;
import io.netty.util.internal.ObjectUtil;
import java.util.List;
import io.netty.handler.codec.http.HttpServerUpgradeHandler;

public class Http2ServerUpgradeCodec implements HttpServerUpgradeHandler.UpgradeCodec
{
    private static final List<String> REQUIRED_UPGRADE_HEADERS;
    private final String handlerName;
    private final Http2ConnectionHandler connectionHandler;
    private final Http2FrameReader frameReader;
    
    public Http2ServerUpgradeCodec(final Http2ConnectionHandler connectionHandler) {
        this("http2ConnectionHandler", connectionHandler);
    }
    
    public Http2ServerUpgradeCodec(final String handlerName, final Http2ConnectionHandler connectionHandler) {
        this.handlerName = ObjectUtil.checkNotNull(handlerName, "handlerName");
        this.connectionHandler = ObjectUtil.checkNotNull(connectionHandler, "connectionHandler");
        this.frameReader = new DefaultHttp2FrameReader();
    }
    
    @Override
    public String protocol() {
        return "h2c-16";
    }
    
    @Override
    public Collection<String> requiredUpgradeHeaders() {
        return Http2ServerUpgradeCodec.REQUIRED_UPGRADE_HEADERS;
    }
    
    @Override
    public void prepareUpgradeResponse(final ChannelHandlerContext ctx, final FullHttpRequest upgradeRequest, final FullHttpResponse upgradeResponse) {
        try {
            final List<CharSequence> upgradeHeaders = (List<CharSequence>)((Headers<String>)upgradeRequest.headers()).getAll("HTTP2-Settings");
            if (upgradeHeaders.isEmpty() || upgradeHeaders.size() > 1) {
                throw new IllegalArgumentException("There must be 1 and only 1 HTTP2-Settings header.");
            }
            final Http2Settings settings = this.decodeSettingsHeader(ctx, upgradeHeaders.get(0));
            this.connectionHandler.onHttpServerUpgrade(settings);
        }
        catch (Throwable e) {
            upgradeResponse.setStatus(HttpResponseStatus.BAD_REQUEST);
            upgradeResponse.headers().clear();
        }
    }
    
    @Override
    public void upgradeTo(final ChannelHandlerContext ctx, final FullHttpRequest upgradeRequest, final FullHttpResponse upgradeResponse) {
        ctx.pipeline().addAfter(ctx.name(), this.handlerName, this.connectionHandler);
    }
    
    private Http2Settings decodeSettingsHeader(final ChannelHandlerContext ctx, final CharSequence settingsHeader) throws Http2Exception {
        final ByteBuf header = Unpooled.wrappedBuffer(AsciiString.getBytes(settingsHeader, CharsetUtil.UTF_8));
        try {
            final ByteBuf payload = Base64.decode(header, Base64Dialect.URL_SAFE);
            final ByteBuf frame = createSettingsFrame(ctx, payload);
            return this.decodeSettings(ctx, frame);
        }
        finally {
            header.release();
        }
    }
    
    private Http2Settings decodeSettings(final ChannelHandlerContext ctx, final ByteBuf frame) throws Http2Exception {
        try {
            final Http2Settings decodedSettings = new Http2Settings();
            this.frameReader.readFrame(ctx, frame, new Http2FrameAdapter() {
                @Override
                public void onSettingsRead(final ChannelHandlerContext ctx, final Http2Settings settings) {
                    decodedSettings.copyFrom(settings);
                }
            });
            return decodedSettings;
        }
        finally {
            frame.release();
        }
    }
    
    private static ByteBuf createSettingsFrame(final ChannelHandlerContext ctx, final ByteBuf payload) {
        final ByteBuf frame = ctx.alloc().buffer(9 + payload.readableBytes());
        Http2CodecUtil.writeFrameHeader(frame, payload.readableBytes(), (byte)4, new Http2Flags(), 0);
        frame.writeBytes(payload);
        payload.release();
        return frame;
    }
    
    static {
        REQUIRED_UPGRADE_HEADERS = Collections.singletonList("HTTP2-Settings");
    }
}
