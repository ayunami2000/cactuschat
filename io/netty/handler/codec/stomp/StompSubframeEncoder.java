// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.stomp;

import io.netty.util.internal.PlatformDependent;
import io.netty.handler.codec.Headers;
import io.netty.handler.codec.AsciiHeadersEncoder;
import io.netty.util.CharsetUtil;
import io.netty.buffer.ByteBuf;
import java.util.List;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

public class StompSubframeEncoder extends MessageToMessageEncoder<StompSubframe>
{
    @Override
    protected void encode(final ChannelHandlerContext ctx, final StompSubframe msg, final List<Object> out) throws Exception {
        if (msg instanceof StompFrame) {
            final StompFrame frame = (StompFrame)msg;
            final ByteBuf frameBuf = encodeFrame(frame, ctx);
            out.add(frameBuf);
            final ByteBuf contentBuf = encodeContent(frame, ctx);
            out.add(contentBuf);
        }
        else if (msg instanceof StompHeadersSubframe) {
            final StompHeadersSubframe frame2 = (StompHeadersSubframe)msg;
            final ByteBuf buf = encodeFrame(frame2, ctx);
            out.add(buf);
        }
        else if (msg instanceof StompContentSubframe) {
            final StompContentSubframe stompContentSubframe = (StompContentSubframe)msg;
            final ByteBuf buf = encodeContent(stompContentSubframe, ctx);
            out.add(buf);
        }
    }
    
    private static ByteBuf encodeContent(final StompContentSubframe content, final ChannelHandlerContext ctx) {
        if (content instanceof LastStompContentSubframe) {
            final ByteBuf buf = ctx.alloc().buffer(content.content().readableBytes() + 1);
            buf.writeBytes(content.content());
            buf.writeByte(0);
            return buf;
        }
        return content.content().retain();
    }
    
    private static ByteBuf encodeFrame(final StompHeadersSubframe frame, final ChannelHandlerContext ctx) {
        final ByteBuf buf = ctx.alloc().buffer();
        buf.writeBytes(frame.command().toString().getBytes(CharsetUtil.US_ASCII));
        buf.writeByte(10);
        try {
            frame.headers().forEachEntry(new AsciiHeadersEncoder(buf, AsciiHeadersEncoder.SeparatorType.COLON, AsciiHeadersEncoder.NewlineType.LF));
        }
        catch (Exception ex) {
            buf.release();
            PlatformDependent.throwException(ex);
        }
        buf.writeByte(10);
        return buf;
    }
}
