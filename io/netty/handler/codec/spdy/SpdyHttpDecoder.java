// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.spdy;

import io.netty.handler.codec.Headers;
import io.netty.handler.codec.ConvertibleHeaders;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.AsciiString;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.buffer.ByteBuf;
import java.util.Iterator;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpHeaderUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.util.List;
import io.netty.channel.ChannelHandlerContext;
import java.util.HashMap;
import io.netty.handler.codec.http.FullHttpMessage;
import java.util.Map;
import io.netty.handler.codec.MessageToMessageDecoder;

public class SpdyHttpDecoder extends MessageToMessageDecoder<SpdyFrame>
{
    private final boolean validateHeaders;
    private final int spdyVersion;
    private final int maxContentLength;
    private final Map<Integer, FullHttpMessage> messageMap;
    
    public SpdyHttpDecoder(final SpdyVersion version, final int maxContentLength) {
        this(version, maxContentLength, new HashMap<Integer, FullHttpMessage>(), true);
    }
    
    public SpdyHttpDecoder(final SpdyVersion version, final int maxContentLength, final boolean validateHeaders) {
        this(version, maxContentLength, new HashMap<Integer, FullHttpMessage>(), validateHeaders);
    }
    
    protected SpdyHttpDecoder(final SpdyVersion version, final int maxContentLength, final Map<Integer, FullHttpMessage> messageMap) {
        this(version, maxContentLength, messageMap, true);
    }
    
    protected SpdyHttpDecoder(final SpdyVersion version, final int maxContentLength, final Map<Integer, FullHttpMessage> messageMap, final boolean validateHeaders) {
        if (version == null) {
            throw new NullPointerException("version");
        }
        if (maxContentLength <= 0) {
            throw new IllegalArgumentException("maxContentLength must be a positive integer: " + maxContentLength);
        }
        this.spdyVersion = version.getVersion();
        this.maxContentLength = maxContentLength;
        this.messageMap = messageMap;
        this.validateHeaders = validateHeaders;
    }
    
    protected FullHttpMessage putMessage(final int streamId, final FullHttpMessage message) {
        return this.messageMap.put(streamId, message);
    }
    
    protected FullHttpMessage getMessage(final int streamId) {
        return this.messageMap.get(streamId);
    }
    
    protected FullHttpMessage removeMessage(final int streamId) {
        return this.messageMap.remove(streamId);
    }
    
    @Override
    protected void decode(final ChannelHandlerContext ctx, final SpdyFrame msg, final List<Object> out) throws Exception {
        if (msg instanceof SpdySynStreamFrame) {
            final SpdySynStreamFrame spdySynStreamFrame = (SpdySynStreamFrame)msg;
            final int streamId = spdySynStreamFrame.streamId();
            if (SpdyCodecUtil.isServerId(streamId)) {
                final int associatedToStreamId = spdySynStreamFrame.associatedStreamId();
                if (associatedToStreamId == 0) {
                    final SpdyRstStreamFrame spdyRstStreamFrame = new DefaultSpdyRstStreamFrame(streamId, SpdyStreamStatus.INVALID_STREAM);
                    ctx.writeAndFlush(spdyRstStreamFrame);
                    return;
                }
                if (spdySynStreamFrame.isLast()) {
                    final SpdyRstStreamFrame spdyRstStreamFrame = new DefaultSpdyRstStreamFrame(streamId, SpdyStreamStatus.PROTOCOL_ERROR);
                    ctx.writeAndFlush(spdyRstStreamFrame);
                    return;
                }
                if (spdySynStreamFrame.isTruncated()) {
                    final SpdyRstStreamFrame spdyRstStreamFrame = new DefaultSpdyRstStreamFrame(streamId, SpdyStreamStatus.INTERNAL_ERROR);
                    ctx.writeAndFlush(spdyRstStreamFrame);
                    return;
                }
                try {
                    final FullHttpRequest httpRequestWithEntity = createHttpRequest(this.spdyVersion, spdySynStreamFrame);
                    httpRequestWithEntity.headers().setInt((CharSequence)SpdyHttpHeaders.Names.STREAM_ID, streamId);
                    httpRequestWithEntity.headers().setInt((CharSequence)SpdyHttpHeaders.Names.ASSOCIATED_TO_STREAM_ID, associatedToStreamId);
                    httpRequestWithEntity.headers().setByte((CharSequence)SpdyHttpHeaders.Names.PRIORITY, spdySynStreamFrame.priority());
                    out.add(httpRequestWithEntity);
                }
                catch (Exception ignored) {
                    final SpdyRstStreamFrame spdyRstStreamFrame2 = new DefaultSpdyRstStreamFrame(streamId, SpdyStreamStatus.PROTOCOL_ERROR);
                    ctx.writeAndFlush(spdyRstStreamFrame2);
                }
            }
            else {
                if (spdySynStreamFrame.isTruncated()) {
                    final SpdySynReplyFrame spdySynReplyFrame = new DefaultSpdySynReplyFrame(streamId);
                    spdySynReplyFrame.setLast(true);
                    final SpdyHeaders frameHeaders = spdySynReplyFrame.headers();
                    frameHeaders.setInt((CharSequence)SpdyHeaders.HttpNames.STATUS, HttpResponseStatus.REQUEST_HEADER_FIELDS_TOO_LARGE.code());
                    frameHeaders.setObject((CharSequence)SpdyHeaders.HttpNames.VERSION, (Object)HttpVersion.HTTP_1_0);
                    ctx.writeAndFlush(spdySynReplyFrame);
                    return;
                }
                try {
                    final FullHttpRequest httpRequestWithEntity2 = createHttpRequest(this.spdyVersion, spdySynStreamFrame);
                    httpRequestWithEntity2.headers().setInt((CharSequence)SpdyHttpHeaders.Names.STREAM_ID, streamId);
                    if (spdySynStreamFrame.isLast()) {
                        out.add(httpRequestWithEntity2);
                    }
                    else {
                        this.putMessage(streamId, httpRequestWithEntity2);
                    }
                }
                catch (Exception e2) {
                    final SpdySynReplyFrame spdySynReplyFrame2 = new DefaultSpdySynReplyFrame(streamId);
                    spdySynReplyFrame2.setLast(true);
                    final SpdyHeaders frameHeaders2 = spdySynReplyFrame2.headers();
                    frameHeaders2.setInt((CharSequence)SpdyHeaders.HttpNames.STATUS, HttpResponseStatus.BAD_REQUEST.code());
                    frameHeaders2.setObject((CharSequence)SpdyHeaders.HttpNames.VERSION, (Object)HttpVersion.HTTP_1_0);
                    ctx.writeAndFlush(spdySynReplyFrame2);
                }
            }
        }
        else if (msg instanceof SpdySynReplyFrame) {
            final SpdySynReplyFrame spdySynReplyFrame3 = (SpdySynReplyFrame)msg;
            final int streamId = spdySynReplyFrame3.streamId();
            if (spdySynReplyFrame3.isTruncated()) {
                final SpdyRstStreamFrame spdyRstStreamFrame3 = new DefaultSpdyRstStreamFrame(streamId, SpdyStreamStatus.INTERNAL_ERROR);
                ctx.writeAndFlush(spdyRstStreamFrame3);
                return;
            }
            try {
                final FullHttpResponse httpResponseWithEntity = createHttpResponse(ctx, spdySynReplyFrame3, this.validateHeaders);
                httpResponseWithEntity.headers().setInt((CharSequence)SpdyHttpHeaders.Names.STREAM_ID, streamId);
                if (spdySynReplyFrame3.isLast()) {
                    HttpHeaderUtil.setContentLength(httpResponseWithEntity, 0L);
                    out.add(httpResponseWithEntity);
                }
                else {
                    this.putMessage(streamId, httpResponseWithEntity);
                }
            }
            catch (Exception e2) {
                final SpdyRstStreamFrame spdyRstStreamFrame = new DefaultSpdyRstStreamFrame(streamId, SpdyStreamStatus.PROTOCOL_ERROR);
                ctx.writeAndFlush(spdyRstStreamFrame);
            }
        }
        else if (msg instanceof SpdyHeadersFrame) {
            final SpdyHeadersFrame spdyHeadersFrame = (SpdyHeadersFrame)msg;
            final int streamId = spdyHeadersFrame.streamId();
            FullHttpMessage fullHttpMessage = this.getMessage(streamId);
            if (fullHttpMessage == null) {
                if (SpdyCodecUtil.isServerId(streamId)) {
                    if (spdyHeadersFrame.isTruncated()) {
                        final SpdyRstStreamFrame spdyRstStreamFrame = new DefaultSpdyRstStreamFrame(streamId, SpdyStreamStatus.INTERNAL_ERROR);
                        ctx.writeAndFlush(spdyRstStreamFrame);
                        return;
                    }
                    try {
                        fullHttpMessage = createHttpResponse(ctx, spdyHeadersFrame, this.validateHeaders);
                        fullHttpMessage.headers().setInt((CharSequence)SpdyHttpHeaders.Names.STREAM_ID, streamId);
                        if (spdyHeadersFrame.isLast()) {
                            HttpHeaderUtil.setContentLength(fullHttpMessage, 0L);
                            out.add(fullHttpMessage);
                        }
                        else {
                            this.putMessage(streamId, fullHttpMessage);
                        }
                    }
                    catch (Exception e3) {
                        final SpdyRstStreamFrame spdyRstStreamFrame2 = new DefaultSpdyRstStreamFrame(streamId, SpdyStreamStatus.PROTOCOL_ERROR);
                        ctx.writeAndFlush(spdyRstStreamFrame2);
                    }
                }
                return;
            }
            if (!spdyHeadersFrame.isTruncated()) {
                for (final Map.Entry<CharSequence, CharSequence> e : spdyHeadersFrame.headers()) {
                    fullHttpMessage.headers().add((CharSequence)e.getKey(), (CharSequence)e.getValue());
                }
            }
            if (spdyHeadersFrame.isLast()) {
                HttpHeaderUtil.setContentLength(fullHttpMessage, fullHttpMessage.content().readableBytes());
                this.removeMessage(streamId);
                out.add(fullHttpMessage);
            }
        }
        else if (msg instanceof SpdyDataFrame) {
            final SpdyDataFrame spdyDataFrame = (SpdyDataFrame)msg;
            final int streamId = spdyDataFrame.streamId();
            final FullHttpMessage fullHttpMessage = this.getMessage(streamId);
            if (fullHttpMessage == null) {
                return;
            }
            final ByteBuf content = fullHttpMessage.content();
            if (content.readableBytes() > this.maxContentLength - spdyDataFrame.content().readableBytes()) {
                this.removeMessage(streamId);
                throw new TooLongFrameException("HTTP content length exceeded " + this.maxContentLength + " bytes.");
            }
            final ByteBuf spdyDataFrameData = spdyDataFrame.content();
            final int spdyDataFrameDataLen = spdyDataFrameData.readableBytes();
            content.writeBytes(spdyDataFrameData, spdyDataFrameData.readerIndex(), spdyDataFrameDataLen);
            if (spdyDataFrame.isLast()) {
                HttpHeaderUtil.setContentLength(fullHttpMessage, content.readableBytes());
                this.removeMessage(streamId);
                out.add(fullHttpMessage);
            }
        }
        else if (msg instanceof SpdyRstStreamFrame) {
            final SpdyRstStreamFrame spdyRstStreamFrame4 = (SpdyRstStreamFrame)msg;
            final int streamId = spdyRstStreamFrame4.streamId();
            this.removeMessage(streamId);
        }
    }
    
    private static FullHttpRequest createHttpRequest(final int spdyVersion, final SpdyHeadersFrame requestFrame) throws Exception {
        final SpdyHeaders headers = requestFrame.headers();
        final HttpMethod method = HttpMethod.valueOf(((ConvertibleHeaders<AsciiString, String>)headers).getAndConvert(SpdyHeaders.HttpNames.METHOD));
        final String url = ((ConvertibleHeaders<AsciiString, String>)headers).getAndConvert(SpdyHeaders.HttpNames.PATH);
        final HttpVersion httpVersion = HttpVersion.valueOf(((ConvertibleHeaders<AsciiString, String>)headers).getAndConvert(SpdyHeaders.HttpNames.VERSION));
        ((Headers<AsciiString>)headers).remove(SpdyHeaders.HttpNames.METHOD);
        ((Headers<AsciiString>)headers).remove(SpdyHeaders.HttpNames.PATH);
        ((Headers<AsciiString>)headers).remove(SpdyHeaders.HttpNames.VERSION);
        final FullHttpRequest req = new DefaultFullHttpRequest(httpVersion, method, url);
        ((Headers<AsciiString>)headers).remove(SpdyHeaders.HttpNames.SCHEME);
        final CharSequence host = ((Headers<AsciiString>)headers).get(SpdyHeaders.HttpNames.HOST);
        ((Headers<AsciiString>)headers).remove(SpdyHeaders.HttpNames.HOST);
        req.headers().set((CharSequence)HttpHeaderNames.HOST, host);
        for (final Map.Entry<CharSequence, CharSequence> e : requestFrame.headers()) {
            req.headers().add((CharSequence)e.getKey(), (CharSequence)e.getValue());
        }
        HttpHeaderUtil.setKeepAlive(req, true);
        ((Headers<AsciiString>)req.headers()).remove(HttpHeaderNames.TRANSFER_ENCODING);
        return req;
    }
    
    private static FullHttpResponse createHttpResponse(final ChannelHandlerContext ctx, final SpdyHeadersFrame responseFrame, final boolean validateHeaders) throws Exception {
        final SpdyHeaders headers = responseFrame.headers();
        final HttpResponseStatus status = HttpResponseStatus.parseLine(((Headers<AsciiString>)headers).get(SpdyHeaders.HttpNames.STATUS));
        final HttpVersion version = HttpVersion.valueOf(((ConvertibleHeaders<AsciiString, String>)headers).getAndConvert(SpdyHeaders.HttpNames.VERSION));
        ((Headers<AsciiString>)headers).remove(SpdyHeaders.HttpNames.STATUS);
        ((Headers<AsciiString>)headers).remove(SpdyHeaders.HttpNames.VERSION);
        final FullHttpResponse res = new DefaultFullHttpResponse(version, status, ctx.alloc().buffer(), validateHeaders);
        for (final Map.Entry<CharSequence, CharSequence> e : responseFrame.headers()) {
            res.headers().add((CharSequence)e.getKey(), (CharSequence)e.getValue());
        }
        HttpHeaderUtil.setKeepAlive(res, true);
        ((Headers<AsciiString>)res.headers()).remove(HttpHeaderNames.TRANSFER_ENCODING);
        ((Headers<AsciiString>)res.headers()).remove(HttpHeaderNames.TRAILER);
        return res;
    }
}
