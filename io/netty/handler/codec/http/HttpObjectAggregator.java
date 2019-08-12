// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http;

import io.netty.handler.codec.TextHeaders;
import io.netty.util.ReferenceCounted;
import io.netty.handler.codec.DecoderResult;
import io.netty.buffer.DefaultByteBufHolder;
import io.netty.buffer.Unpooled;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.buffer.ByteBufHolder;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Future;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.buffer.ByteBuf;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.handler.codec.MessageAggregator;

public class HttpObjectAggregator extends MessageAggregator<HttpObject, HttpMessage, HttpContent, FullHttpMessage>
{
    private static final InternalLogger logger;
    private static final FullHttpResponse CONTINUE;
    private static final FullHttpResponse TOO_LARGE;
    
    public HttpObjectAggregator(final int maxContentLength) {
        super(maxContentLength);
    }
    
    @Override
    protected boolean isStartMessage(final HttpObject msg) throws Exception {
        return msg instanceof HttpMessage;
    }
    
    @Override
    protected boolean isContentMessage(final HttpObject msg) throws Exception {
        return msg instanceof HttpContent;
    }
    
    @Override
    protected boolean isLastContentMessage(final HttpContent msg) throws Exception {
        return msg instanceof LastHttpContent;
    }
    
    @Override
    protected boolean isAggregated(final HttpObject msg) throws Exception {
        return msg instanceof FullHttpMessage;
    }
    
    @Override
    protected boolean hasContentLength(final HttpMessage start) throws Exception {
        return HttpHeaderUtil.isContentLengthSet(start);
    }
    
    @Override
    protected long contentLength(final HttpMessage start) throws Exception {
        return HttpHeaderUtil.getContentLength(start);
    }
    
    @Override
    protected Object newContinueResponse(final HttpMessage start) throws Exception {
        if (HttpHeaderUtil.is100ContinueExpected(start)) {
            return HttpObjectAggregator.CONTINUE;
        }
        return null;
    }
    
    @Override
    protected FullHttpMessage beginAggregation(final HttpMessage start, final ByteBuf content) throws Exception {
        assert !(start instanceof FullHttpMessage);
        HttpHeaderUtil.setTransferEncodingChunked(start, false);
        AggregatedFullHttpMessage ret;
        if (start instanceof HttpRequest) {
            ret = new AggregatedFullHttpRequest((HttpRequest)start, content, null);
        }
        else {
            if (!(start instanceof HttpResponse)) {
                throw new Error();
            }
            ret = new AggregatedFullHttpResponse((HttpResponse)start, content, null);
        }
        return ret;
    }
    
    @Override
    protected void aggregate(final FullHttpMessage aggregated, final HttpContent content) throws Exception {
        if (content instanceof LastHttpContent) {
            ((AggregatedFullHttpMessage)aggregated).setTrailingHeaders(((LastHttpContent)content).trailingHeaders());
        }
    }
    
    @Override
    protected void finishAggregation(final FullHttpMessage aggregated) throws Exception {
        if (!HttpHeaderUtil.isContentLengthSet(aggregated)) {
            aggregated.headers().set((CharSequence)HttpHeaderNames.CONTENT_LENGTH, (CharSequence)String.valueOf(aggregated.content().readableBytes()));
        }
    }
    
    @Override
    protected void handleOversizedMessage(final ChannelHandlerContext ctx, final HttpMessage oversized) throws Exception {
        if (oversized instanceof HttpRequest) {
            final ChannelFuture future = ctx.writeAndFlush(HttpObjectAggregator.TOO_LARGE).addListener((GenericFutureListener<? extends Future<? super Void>>)new ChannelFutureListener() {
                @Override
                public void operationComplete(final ChannelFuture future) throws Exception {
                    if (!future.isSuccess()) {
                        HttpObjectAggregator.logger.debug("Failed to send a 413 Request Entity Too Large.", future.cause());
                        ctx.close();
                    }
                }
            });
            if (oversized instanceof FullHttpMessage || (!HttpHeaderUtil.is100ContinueExpected(oversized) && !HttpHeaderUtil.isKeepAlive(oversized))) {
                future.addListener((GenericFutureListener<? extends Future<? super Void>>)ChannelFutureListener.CLOSE);
            }
            final HttpObjectDecoder decoder = ctx.pipeline().get(HttpObjectDecoder.class);
            if (decoder != null) {
                decoder.reset();
            }
            return;
        }
        if (oversized instanceof HttpResponse) {
            ctx.close();
            throw new TooLongFrameException("Response entity too large: " + oversized);
        }
        throw new IllegalStateException();
    }
    
    static {
        logger = InternalLoggerFactory.getInstance(HttpObjectAggregator.class);
        CONTINUE = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE, Unpooled.EMPTY_BUFFER);
        TOO_LARGE = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE, Unpooled.EMPTY_BUFFER);
        HttpObjectAggregator.TOO_LARGE.headers().setInt((CharSequence)HttpHeaderNames.CONTENT_LENGTH, 0);
    }
    
    private abstract static class AggregatedFullHttpMessage extends DefaultByteBufHolder implements FullHttpMessage
    {
        protected final HttpMessage message;
        private HttpHeaders trailingHeaders;
        
        AggregatedFullHttpMessage(final HttpMessage message, final ByteBuf content, final HttpHeaders trailingHeaders) {
            super(content);
            this.message = message;
            this.trailingHeaders = trailingHeaders;
        }
        
        @Override
        public HttpHeaders trailingHeaders() {
            final HttpHeaders trailingHeaders = this.trailingHeaders;
            if (trailingHeaders == null) {
                return EmptyHttpHeaders.INSTANCE;
            }
            return trailingHeaders;
        }
        
        void setTrailingHeaders(final HttpHeaders trailingHeaders) {
            this.trailingHeaders = trailingHeaders;
        }
        
        @Override
        public HttpVersion protocolVersion() {
            return this.message.protocolVersion();
        }
        
        @Override
        public FullHttpMessage setProtocolVersion(final HttpVersion version) {
            this.message.setProtocolVersion(version);
            return this;
        }
        
        @Override
        public HttpHeaders headers() {
            return this.message.headers();
        }
        
        @Override
        public DecoderResult decoderResult() {
            return this.message.decoderResult();
        }
        
        @Override
        public void setDecoderResult(final DecoderResult result) {
            this.message.setDecoderResult(result);
        }
        
        @Override
        public FullHttpMessage retain(final int increment) {
            super.retain(increment);
            return this;
        }
        
        @Override
        public FullHttpMessage retain() {
            super.retain();
            return this;
        }
        
        @Override
        public FullHttpMessage touch(final Object hint) {
            super.touch(hint);
            return this;
        }
        
        @Override
        public FullHttpMessage touch() {
            super.touch();
            return this;
        }
        
        @Override
        public abstract FullHttpMessage copy();
        
        @Override
        public abstract FullHttpMessage duplicate();
    }
    
    private static final class AggregatedFullHttpRequest extends AggregatedFullHttpMessage implements FullHttpRequest
    {
        AggregatedFullHttpRequest(final HttpRequest request, final ByteBuf content, final HttpHeaders trailingHeaders) {
            super(request, content, trailingHeaders);
        }
        
        private FullHttpRequest copy(final boolean copyContent, final ByteBuf newContent) {
            final DefaultFullHttpRequest copy = new DefaultFullHttpRequest(this.protocolVersion(), this.method(), this.uri(), copyContent ? this.content().copy() : ((newContent == null) ? Unpooled.buffer(0) : newContent));
            copy.headers().set((TextHeaders)this.headers());
            copy.trailingHeaders().set((TextHeaders)this.trailingHeaders());
            return copy;
        }
        
        @Override
        public FullHttpRequest copy(final ByteBuf newContent) {
            return this.copy(false, newContent);
        }
        
        @Override
        public FullHttpRequest copy() {
            return this.copy(true, null);
        }
        
        @Override
        public FullHttpRequest duplicate() {
            final DefaultFullHttpRequest duplicate = new DefaultFullHttpRequest(this.protocolVersion(), this.method(), this.uri(), this.content().duplicate());
            duplicate.headers().set((TextHeaders)this.headers());
            duplicate.trailingHeaders().set((TextHeaders)this.trailingHeaders());
            return duplicate;
        }
        
        @Override
        public FullHttpRequest retain(final int increment) {
            super.retain(increment);
            return this;
        }
        
        @Override
        public FullHttpRequest retain() {
            super.retain();
            return this;
        }
        
        @Override
        public FullHttpRequest touch() {
            super.touch();
            return this;
        }
        
        @Override
        public FullHttpRequest touch(final Object hint) {
            super.touch(hint);
            return this;
        }
        
        @Override
        public FullHttpRequest setMethod(final HttpMethod method) {
            ((HttpRequest)this.message).setMethod(method);
            return this;
        }
        
        @Override
        public FullHttpRequest setUri(final String uri) {
            ((HttpRequest)this.message).setUri(uri);
            return this;
        }
        
        @Override
        public HttpMethod method() {
            return ((HttpRequest)this.message).method();
        }
        
        @Override
        public String uri() {
            return ((HttpRequest)this.message).uri();
        }
        
        @Override
        public FullHttpRequest setProtocolVersion(final HttpVersion version) {
            super.setProtocolVersion(version);
            return this;
        }
        
        @Override
        public String toString() {
            return HttpMessageUtil.appendFullRequest(new StringBuilder(256), this).toString();
        }
    }
    
    private static final class AggregatedFullHttpResponse extends AggregatedFullHttpMessage implements FullHttpResponse
    {
        AggregatedFullHttpResponse(final HttpResponse message, final ByteBuf content, final HttpHeaders trailingHeaders) {
            super(message, content, trailingHeaders);
        }
        
        private FullHttpResponse copy(final boolean copyContent, final ByteBuf newContent) {
            final DefaultFullHttpResponse copy = new DefaultFullHttpResponse(this.protocolVersion(), this.status(), copyContent ? this.content().copy() : ((newContent == null) ? Unpooled.buffer(0) : newContent));
            copy.headers().set((TextHeaders)this.headers());
            copy.trailingHeaders().set((TextHeaders)this.trailingHeaders());
            return copy;
        }
        
        @Override
        public FullHttpResponse copy(final ByteBuf newContent) {
            return this.copy(false, newContent);
        }
        
        @Override
        public FullHttpResponse copy() {
            return this.copy(true, null);
        }
        
        @Override
        public FullHttpResponse duplicate() {
            final DefaultFullHttpResponse duplicate = new DefaultFullHttpResponse(this.protocolVersion(), this.status(), this.content().duplicate());
            duplicate.headers().set((TextHeaders)this.headers());
            duplicate.trailingHeaders().set((TextHeaders)this.trailingHeaders());
            return duplicate;
        }
        
        @Override
        public FullHttpResponse setStatus(final HttpResponseStatus status) {
            ((HttpResponse)this.message).setStatus(status);
            return this;
        }
        
        @Override
        public HttpResponseStatus status() {
            return ((HttpResponse)this.message).status();
        }
        
        @Override
        public FullHttpResponse setProtocolVersion(final HttpVersion version) {
            super.setProtocolVersion(version);
            return this;
        }
        
        @Override
        public FullHttpResponse retain(final int increment) {
            super.retain(increment);
            return this;
        }
        
        @Override
        public FullHttpResponse retain() {
            super.retain();
            return this;
        }
        
        @Override
        public FullHttpResponse touch(final Object hint) {
            super.touch(hint);
            return this;
        }
        
        @Override
        public FullHttpResponse touch() {
            super.touch();
            return this;
        }
        
        @Override
        public String toString() {
            return HttpMessageUtil.appendFullResponse(new StringBuilder(256), this).toString();
        }
    }
}
