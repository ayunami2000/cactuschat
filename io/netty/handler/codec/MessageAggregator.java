// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec;

import io.netty.util.ReferenceCountUtil;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Future;
import io.netty.channel.ChannelFuture;
import java.util.List;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.buffer.ByteBufHolder;

public abstract class MessageAggregator<I, S, C extends ByteBufHolder, O extends ByteBufHolder> extends MessageToMessageDecoder<I>
{
    private static final int DEFAULT_MAX_COMPOSITEBUFFER_COMPONENTS = 1024;
    private final int maxContentLength;
    private O currentMessage;
    private boolean handlingOversizedMessage;
    private int maxCumulationBufferComponents;
    private ChannelHandlerContext ctx;
    private ChannelFutureListener continueResponseWriteListener;
    
    protected MessageAggregator(final int maxContentLength) {
        this.maxCumulationBufferComponents = 1024;
        validateMaxContentLength(maxContentLength);
        this.maxContentLength = maxContentLength;
    }
    
    protected MessageAggregator(final int maxContentLength, final Class<? extends I> inboundMessageType) {
        super(inboundMessageType);
        this.maxCumulationBufferComponents = 1024;
        validateMaxContentLength(maxContentLength);
        this.maxContentLength = maxContentLength;
    }
    
    private static void validateMaxContentLength(final int maxContentLength) {
        if (maxContentLength <= 0) {
            throw new IllegalArgumentException("maxContentLength must be a positive integer: " + maxContentLength);
        }
    }
    
    @Override
    public boolean acceptInboundMessage(final Object msg) throws Exception {
        if (!super.acceptInboundMessage(msg)) {
            return false;
        }
        final I in = (I)msg;
        return (this.isContentMessage(in) || this.isStartMessage(in)) && !this.isAggregated(in);
    }
    
    protected abstract boolean isStartMessage(final I p0) throws Exception;
    
    protected abstract boolean isContentMessage(final I p0) throws Exception;
    
    protected abstract boolean isLastContentMessage(final C p0) throws Exception;
    
    protected abstract boolean isAggregated(final I p0) throws Exception;
    
    public final int maxContentLength() {
        return this.maxContentLength;
    }
    
    public final int maxCumulationBufferComponents() {
        return this.maxCumulationBufferComponents;
    }
    
    public final void setMaxCumulationBufferComponents(final int maxCumulationBufferComponents) {
        if (maxCumulationBufferComponents < 2) {
            throw new IllegalArgumentException("maxCumulationBufferComponents: " + maxCumulationBufferComponents + " (expected: >= 2)");
        }
        if (this.ctx == null) {
            this.maxCumulationBufferComponents = maxCumulationBufferComponents;
            return;
        }
        throw new IllegalStateException("decoder properties cannot be changed once the decoder is added to a pipeline.");
    }
    
    public final boolean isHandlingOversizedMessage() {
        return this.handlingOversizedMessage;
    }
    
    protected final ChannelHandlerContext ctx() {
        if (this.ctx == null) {
            throw new IllegalStateException("not added to a pipeline yet");
        }
        return this.ctx;
    }
    
    @Override
    protected void decode(final ChannelHandlerContext ctx, final I msg, final List<Object> out) throws Exception {
        final O currentMessage = this.currentMessage;
        if (this.isStartMessage(msg)) {
            this.handlingOversizedMessage = false;
            if (currentMessage != null) {
                throw new MessageAggregationException();
            }
            final S m = (S)msg;
            if (this.hasContentLength(m) && this.contentLength(m) > this.maxContentLength) {
                this.invokeHandleOversizedMessage(ctx, m);
                return;
            }
            final Object continueResponse = this.newContinueResponse(m);
            if (continueResponse != null) {
                ChannelFutureListener listener = this.continueResponseWriteListener;
                if (listener == null) {
                    listener = (this.continueResponseWriteListener = new ChannelFutureListener() {
                        @Override
                        public void operationComplete(final ChannelFuture future) throws Exception {
                            if (!future.isSuccess()) {
                                ctx.fireExceptionCaught(future.cause());
                            }
                        }
                    });
                }
                ctx.writeAndFlush(continueResponse).addListener((GenericFutureListener<? extends Future<? super Void>>)listener);
            }
            if (m instanceof DecoderResultProvider && !((DecoderResultProvider)m).decoderResult().isSuccess()) {
                O aggregated;
                if (m instanceof ByteBufHolder && ((ByteBufHolder)m).content().isReadable()) {
                    aggregated = this.beginAggregation(m, ((ByteBufHolder)m).content().retain());
                }
                else {
                    aggregated = this.beginAggregation(m, Unpooled.EMPTY_BUFFER);
                }
                this.finishAggregation(aggregated);
                out.add(aggregated);
                this.currentMessage = null;
                return;
            }
            final CompositeByteBuf content = ctx.alloc().compositeBuffer(this.maxCumulationBufferComponents);
            if (m instanceof ByteBufHolder) {
                appendPartialContent(content, ((ByteBufHolder)m).content());
            }
            this.currentMessage = this.beginAggregation(m, content);
        }
        else {
            if (!this.isContentMessage(msg)) {
                throw new MessageAggregationException();
            }
            final C i = (C)msg;
            final ByteBuf partialContent = ((ByteBufHolder)msg).content();
            final boolean isLastContentMessage = this.isLastContentMessage(i);
            if (this.handlingOversizedMessage) {
                if (isLastContentMessage) {
                    this.currentMessage = null;
                }
                return;
            }
            if (currentMessage == null) {
                throw new MessageAggregationException();
            }
            final CompositeByteBuf content2 = (CompositeByteBuf)currentMessage.content();
            if (content2.readableBytes() > this.maxContentLength - partialContent.readableBytes()) {
                final S s = (S)currentMessage;
                this.invokeHandleOversizedMessage(ctx, s);
                return;
            }
            appendPartialContent(content2, partialContent);
            this.aggregate(currentMessage, i);
            boolean last;
            if (i instanceof DecoderResultProvider) {
                final DecoderResult decoderResult = ((DecoderResultProvider)i).decoderResult();
                if (!decoderResult.isSuccess()) {
                    if (currentMessage instanceof DecoderResultProvider) {
                        ((DecoderResultProvider)currentMessage).setDecoderResult(DecoderResult.failure(decoderResult.cause()));
                    }
                    last = true;
                }
                else {
                    last = isLastContentMessage;
                }
            }
            else {
                last = isLastContentMessage;
            }
            if (last) {
                this.finishAggregation(currentMessage);
                out.add(currentMessage);
                this.currentMessage = null;
            }
        }
    }
    
    private static void appendPartialContent(final CompositeByteBuf content, final ByteBuf partialContent) {
        if (partialContent.isReadable()) {
            partialContent.retain();
            content.addComponent(partialContent);
            content.writerIndex(content.writerIndex() + partialContent.readableBytes());
        }
    }
    
    protected abstract boolean hasContentLength(final S p0) throws Exception;
    
    protected abstract long contentLength(final S p0) throws Exception;
    
    protected abstract Object newContinueResponse(final S p0) throws Exception;
    
    protected abstract O beginAggregation(final S p0, final ByteBuf p1) throws Exception;
    
    protected void aggregate(final O aggregated, final C content) throws Exception {
    }
    
    protected void finishAggregation(final O aggregated) throws Exception {
    }
    
    private void invokeHandleOversizedMessage(final ChannelHandlerContext ctx, final S oversized) throws Exception {
        this.handlingOversizedMessage = true;
        this.currentMessage = null;
        try {
            this.handleOversizedMessage(ctx, oversized);
        }
        finally {
            ReferenceCountUtil.release(oversized);
        }
    }
    
    protected void handleOversizedMessage(final ChannelHandlerContext ctx, final S oversized) throws Exception {
        ctx.fireExceptionCaught(new TooLongFrameException("content length exceeded " + this.maxContentLength() + " bytes."));
    }
    
    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        if (this.currentMessage != null) {
            this.currentMessage.release();
            this.currentMessage = null;
        }
        super.channelInactive(ctx);
    }
    
    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
    }
    
    @Override
    public void handlerRemoved(final ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
        if (this.currentMessage != null) {
            this.currentMessage.release();
            this.currentMessage = null;
        }
    }
}
