// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.spdy;

import io.netty.handler.codec.Headers;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.AsciiString;
import java.util.Iterator;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.UnsupportedMessageTypeException;
import java.util.Map;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import java.util.List;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.MessageToMessageEncoder;

public class SpdyHttpEncoder extends MessageToMessageEncoder<HttpObject>
{
    private int currentStreamId;
    
    public SpdyHttpEncoder(final SpdyVersion version) {
        if (version == null) {
            throw new NullPointerException("version");
        }
    }
    
    @Override
    protected void encode(final ChannelHandlerContext ctx, final HttpObject msg, final List<Object> out) throws Exception {
        boolean valid = false;
        boolean last = false;
        if (msg instanceof HttpRequest) {
            final HttpRequest httpRequest = (HttpRequest)msg;
            final SpdySynStreamFrame spdySynStreamFrame = this.createSynStreamFrame(httpRequest);
            out.add(spdySynStreamFrame);
            last = (spdySynStreamFrame.isLast() || spdySynStreamFrame.isUnidirectional());
            valid = true;
        }
        if (msg instanceof HttpResponse) {
            final HttpResponse httpResponse = (HttpResponse)msg;
            final SpdyHeadersFrame spdyHeadersFrame = this.createHeadersFrame(httpResponse);
            out.add(spdyHeadersFrame);
            last = spdyHeadersFrame.isLast();
            valid = true;
        }
        if (msg instanceof HttpContent && !last) {
            final HttpContent chunk = (HttpContent)msg;
            chunk.content().retain();
            final SpdyDataFrame spdyDataFrame = new DefaultSpdyDataFrame(this.currentStreamId, chunk.content());
            if (chunk instanceof LastHttpContent) {
                final LastHttpContent trailer = (LastHttpContent)chunk;
                final HttpHeaders trailers = trailer.trailingHeaders();
                if (trailers.isEmpty()) {
                    spdyDataFrame.setLast(true);
                    out.add(spdyDataFrame);
                }
                else {
                    final SpdyHeadersFrame spdyHeadersFrame2 = new DefaultSpdyHeadersFrame(this.currentStreamId);
                    spdyHeadersFrame2.setLast(true);
                    for (final Map.Entry<CharSequence, CharSequence> entry : trailers) {
                        spdyHeadersFrame2.headers().add((CharSequence)entry.getKey(), (CharSequence)entry.getValue());
                    }
                    out.add(spdyDataFrame);
                    out.add(spdyHeadersFrame2);
                }
            }
            else {
                out.add(spdyDataFrame);
            }
            valid = true;
        }
        if (!valid) {
            throw new UnsupportedMessageTypeException(msg, (Class<?>[])new Class[0]);
        }
    }
    
    private SpdySynStreamFrame createSynStreamFrame(final HttpRequest httpRequest) throws Exception {
        final HttpHeaders httpHeaders = httpRequest.headers();
        final int streamId = ((Headers<AsciiString>)httpHeaders).getInt(SpdyHttpHeaders.Names.STREAM_ID);
        final int associatedToStreamId = ((Headers<AsciiString>)httpHeaders).getInt(SpdyHttpHeaders.Names.ASSOCIATED_TO_STREAM_ID, 0);
        final byte priority = (byte)((Headers<AsciiString>)httpHeaders).getInt(SpdyHttpHeaders.Names.PRIORITY, 0);
        CharSequence scheme = ((Headers<AsciiString>)httpHeaders).get(SpdyHttpHeaders.Names.SCHEME);
        ((Headers<AsciiString>)httpHeaders).remove(SpdyHttpHeaders.Names.STREAM_ID);
        ((Headers<AsciiString>)httpHeaders).remove(SpdyHttpHeaders.Names.ASSOCIATED_TO_STREAM_ID);
        ((Headers<AsciiString>)httpHeaders).remove(SpdyHttpHeaders.Names.PRIORITY);
        ((Headers<AsciiString>)httpHeaders).remove(SpdyHttpHeaders.Names.SCHEME);
        ((Headers<AsciiString>)httpHeaders).remove(HttpHeaderNames.CONNECTION);
        ((Headers<AsciiString>)httpHeaders).remove(HttpHeaderNames.KEEP_ALIVE);
        ((Headers<AsciiString>)httpHeaders).remove(HttpHeaderNames.PROXY_CONNECTION);
        ((Headers<AsciiString>)httpHeaders).remove(HttpHeaderNames.TRANSFER_ENCODING);
        final SpdySynStreamFrame spdySynStreamFrame = new DefaultSpdySynStreamFrame(streamId, associatedToStreamId, priority);
        final SpdyHeaders frameHeaders = spdySynStreamFrame.headers();
        frameHeaders.set((CharSequence)SpdyHeaders.HttpNames.METHOD, (CharSequence)httpRequest.method().name());
        frameHeaders.set((CharSequence)SpdyHeaders.HttpNames.PATH, (CharSequence)httpRequest.uri());
        frameHeaders.set((CharSequence)SpdyHeaders.HttpNames.VERSION, (CharSequence)httpRequest.protocolVersion().text());
        final CharSequence host = ((Headers<AsciiString>)httpHeaders).get(HttpHeaderNames.HOST);
        ((Headers<AsciiString>)httpHeaders).remove(HttpHeaderNames.HOST);
        frameHeaders.set((CharSequence)SpdyHeaders.HttpNames.HOST, host);
        if (scheme == null) {
            scheme = "https";
        }
        frameHeaders.set((CharSequence)SpdyHeaders.HttpNames.SCHEME, scheme);
        for (final Map.Entry<CharSequence, CharSequence> entry : httpHeaders) {
            frameHeaders.add((CharSequence)entry.getKey(), (CharSequence)entry.getValue());
        }
        this.currentStreamId = spdySynStreamFrame.streamId();
        if (associatedToStreamId == 0) {
            spdySynStreamFrame.setLast(isLast(httpRequest));
        }
        else {
            spdySynStreamFrame.setUnidirectional(true);
        }
        return spdySynStreamFrame;
    }
    
    private SpdyHeadersFrame createHeadersFrame(final HttpResponse httpResponse) throws Exception {
        final HttpHeaders httpHeaders = httpResponse.headers();
        final int streamId = ((Headers<AsciiString>)httpHeaders).getInt(SpdyHttpHeaders.Names.STREAM_ID);
        ((Headers<AsciiString>)httpHeaders).remove(SpdyHttpHeaders.Names.STREAM_ID);
        ((Headers<AsciiString>)httpHeaders).remove(HttpHeaderNames.CONNECTION);
        ((Headers<AsciiString>)httpHeaders).remove(HttpHeaderNames.KEEP_ALIVE);
        ((Headers<AsciiString>)httpHeaders).remove(HttpHeaderNames.PROXY_CONNECTION);
        ((Headers<AsciiString>)httpHeaders).remove(HttpHeaderNames.TRANSFER_ENCODING);
        SpdyHeadersFrame spdyHeadersFrame;
        if (SpdyCodecUtil.isServerId(streamId)) {
            spdyHeadersFrame = new DefaultSpdyHeadersFrame(streamId);
        }
        else {
            spdyHeadersFrame = new DefaultSpdySynReplyFrame(streamId);
        }
        final SpdyHeaders frameHeaders = spdyHeadersFrame.headers();
        frameHeaders.set((CharSequence)SpdyHeaders.HttpNames.STATUS, (CharSequence)httpResponse.status().codeAsText());
        frameHeaders.set((CharSequence)SpdyHeaders.HttpNames.VERSION, (CharSequence)httpResponse.protocolVersion().text());
        for (final Map.Entry<CharSequence, CharSequence> entry : httpHeaders) {
            spdyHeadersFrame.headers().add((CharSequence)entry.getKey(), (CharSequence)entry.getValue());
        }
        this.currentStreamId = streamId;
        spdyHeadersFrame.setLast(isLast(httpResponse));
        return spdyHeadersFrame;
    }
    
    private static boolean isLast(final HttpMessage httpMessage) {
        if (httpMessage instanceof FullHttpMessage) {
            final FullHttpMessage fullMessage = (FullHttpMessage)httpMessage;
            if (fullMessage.trailingHeaders().isEmpty() && !fullMessage.content().isReadable()) {
                return true;
            }
        }
        return false;
    }
}
