// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.spdy;

import io.netty.handler.codec.AsciiString;
import io.netty.handler.codec.Headers;
import io.netty.util.ReferenceCountUtil;
import java.util.List;
import io.netty.channel.ChannelHandlerContext;
import java.util.LinkedList;
import java.util.Queue;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.MessageToMessageCodec;

public class SpdyHttpResponseStreamIdHandler extends MessageToMessageCodec<Object, HttpMessage>
{
    private static final Integer NO_ID;
    private final Queue<Integer> ids;
    
    public SpdyHttpResponseStreamIdHandler() {
        this.ids = new LinkedList<Integer>();
    }
    
    @Override
    public boolean acceptInboundMessage(final Object msg) throws Exception {
        return msg instanceof HttpMessage || msg instanceof SpdyRstStreamFrame;
    }
    
    @Override
    protected void encode(final ChannelHandlerContext ctx, final HttpMessage msg, final List<Object> out) throws Exception {
        final Integer id = this.ids.poll();
        if (id != null && id != (int)SpdyHttpResponseStreamIdHandler.NO_ID && !((Headers<AsciiString>)msg.headers()).contains(SpdyHttpHeaders.Names.STREAM_ID)) {
            msg.headers().setInt((CharSequence)SpdyHttpHeaders.Names.STREAM_ID, (int)id);
        }
        out.add(ReferenceCountUtil.retain(msg));
    }
    
    @Override
    protected void decode(final ChannelHandlerContext ctx, final Object msg, final List<Object> out) throws Exception {
        if (msg instanceof HttpMessage) {
            final boolean contains = ((Headers<AsciiString>)((HttpMessage)msg).headers()).contains(SpdyHttpHeaders.Names.STREAM_ID);
            if (!contains) {
                this.ids.add(SpdyHttpResponseStreamIdHandler.NO_ID);
            }
            else {
                this.ids.add(((Headers<AsciiString>)((HttpMessage)msg).headers()).getInt(SpdyHttpHeaders.Names.STREAM_ID));
            }
        }
        else if (msg instanceof SpdyRstStreamFrame) {
            this.ids.remove(((SpdyRstStreamFrame)msg).streamId());
        }
        out.add(ReferenceCountUtil.retain(msg));
    }
    
    static {
        NO_ID = -1;
    }
}
