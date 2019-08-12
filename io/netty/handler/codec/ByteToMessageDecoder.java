// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec;

import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.util.internal.StringUtil;
import java.util.List;
import io.netty.util.internal.RecyclableArrayList;
import io.netty.channel.ChannelHandlerContext;
import io.netty.buffer.Unpooled;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;

public abstract class ByteToMessageDecoder extends ChannelHandlerAdapter
{
    public static final Cumulator MERGE_CUMULATOR;
    public static final Cumulator COMPOSITE_CUMULATOR;
    ByteBuf cumulation;
    private Cumulator cumulator;
    private boolean singleDecode;
    private boolean first;
    
    protected ByteToMessageDecoder() {
        this.cumulator = ByteToMessageDecoder.MERGE_CUMULATOR;
        CodecUtil.ensureNotSharable(this);
    }
    
    public void setSingleDecode(final boolean singleDecode) {
        this.singleDecode = singleDecode;
    }
    
    public boolean isSingleDecode() {
        return this.singleDecode;
    }
    
    public void setCumulator(final Cumulator cumulator) {
        if (cumulator == null) {
            throw new NullPointerException("cumulator");
        }
        this.cumulator = cumulator;
    }
    
    protected int actualReadableBytes() {
        return this.internalBuffer().readableBytes();
    }
    
    protected ByteBuf internalBuffer() {
        if (this.cumulation != null) {
            return this.cumulation;
        }
        return Unpooled.EMPTY_BUFFER;
    }
    
    @Override
    public final void handlerRemoved(final ChannelHandlerContext ctx) throws Exception {
        final ByteBuf buf = this.internalBuffer();
        final int readable = buf.readableBytes();
        if (readable > 0) {
            final ByteBuf bytes = buf.readBytes(readable);
            buf.release();
            ctx.fireChannelRead(bytes);
            ctx.fireChannelReadComplete();
        }
        else {
            buf.release();
        }
        this.cumulation = null;
        this.handlerRemoved0(ctx);
    }
    
    protected void handlerRemoved0(final ChannelHandlerContext ctx) throws Exception {
    }
    
    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            final RecyclableArrayList out = RecyclableArrayList.newInstance();
            try {
                final ByteBuf data = (ByteBuf)msg;
                this.first = (this.cumulation == null);
                if (this.first) {
                    this.cumulation = data;
                }
                else {
                    this.cumulation = this.cumulator.cumulate(ctx.alloc(), this.cumulation, data);
                }
                this.callDecode(ctx, this.cumulation, out);
            }
            catch (DecoderException e) {
                throw e;
            }
            catch (Throwable t) {
                throw new DecoderException(t);
            }
            finally {
                if (this.cumulation != null && !this.cumulation.isReadable()) {
                    this.cumulation.release();
                    this.cumulation = null;
                }
                for (int size = out.size(), i = 0; i < size; ++i) {
                    ctx.fireChannelRead(out.get(i));
                }
                out.recycle();
            }
        }
        else {
            ctx.fireChannelRead(msg);
        }
    }
    
    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) throws Exception {
        if (this.cumulation != null && !this.first && this.cumulation.refCnt() == 1) {
            this.cumulation.discardSomeReadBytes();
        }
        ctx.fireChannelReadComplete();
    }
    
    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        final RecyclableArrayList out = RecyclableArrayList.newInstance();
        try {
            if (this.cumulation != null) {
                this.callDecode(ctx, this.cumulation, out);
                this.decodeLast(ctx, this.cumulation, out);
            }
            else {
                this.decodeLast(ctx, Unpooled.EMPTY_BUFFER, out);
            }
        }
        catch (DecoderException e) {
            throw e;
        }
        catch (Exception e2) {
            throw new DecoderException(e2);
        }
        finally {
            try {
                if (this.cumulation != null) {
                    this.cumulation.release();
                    this.cumulation = null;
                }
                final int size = out.size();
                for (int i = 0; i < size; ++i) {
                    ctx.fireChannelRead(out.get(i));
                }
                if (size > 0) {
                    ctx.fireChannelReadComplete();
                }
                ctx.fireChannelInactive();
            }
            finally {
                out.recycle();
            }
        }
    }
    
    protected void callDecode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) {
        try {
            while (in.isReadable()) {
                final int outSize = out.size();
                final int oldInputLength = in.readableBytes();
                this.decode(ctx, in, out);
                if (ctx.isRemoved()) {
                    break;
                }
                if (outSize == out.size()) {
                    if (oldInputLength == in.readableBytes()) {
                        break;
                    }
                    continue;
                }
                else {
                    if (oldInputLength == in.readableBytes()) {
                        throw new DecoderException(StringUtil.simpleClassName(this.getClass()) + ".decode() did not read anything but decoded a message.");
                    }
                    if (this.isSingleDecode()) {
                        break;
                    }
                    continue;
                }
            }
        }
        catch (DecoderException e) {
            throw e;
        }
        catch (Throwable cause) {
            throw new DecoderException(cause);
        }
    }
    
    protected abstract void decode(final ChannelHandlerContext p0, final ByteBuf p1, final List<Object> p2) throws Exception;
    
    protected void decodeLast(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) throws Exception {
        this.decode(ctx, in, out);
    }
    
    static ByteBuf expandCumulation(final ByteBufAllocator alloc, ByteBuf cumulation, final int readable) {
        final ByteBuf oldCumulation = cumulation;
        cumulation = alloc.buffer(oldCumulation.readableBytes() + readable);
        cumulation.writeBytes(oldCumulation);
        oldCumulation.release();
        return cumulation;
    }
    
    static {
        MERGE_CUMULATOR = new Cumulator() {
            @Override
            public ByteBuf cumulate(final ByteBufAllocator alloc, final ByteBuf cumulation, final ByteBuf in) {
                ByteBuf buffer;
                if (cumulation.writerIndex() > cumulation.maxCapacity() - in.readableBytes() || cumulation.refCnt() > 1) {
                    buffer = ByteToMessageDecoder.expandCumulation(alloc, cumulation, in.readableBytes());
                }
                else {
                    buffer = cumulation;
                }
                buffer.writeBytes(in);
                in.release();
                return buffer;
            }
        };
        COMPOSITE_CUMULATOR = new Cumulator() {
            @Override
            public ByteBuf cumulate(final ByteBufAllocator alloc, final ByteBuf cumulation, final ByteBuf in) {
                ByteBuf buffer;
                if (cumulation.refCnt() > 1) {
                    buffer = ByteToMessageDecoder.expandCumulation(alloc, cumulation, in.readableBytes());
                    buffer.writeBytes(in);
                    in.release();
                }
                else {
                    CompositeByteBuf composite;
                    if (cumulation instanceof CompositeByteBuf) {
                        composite = (CompositeByteBuf)cumulation;
                    }
                    else {
                        final int readable = cumulation.readableBytes();
                        composite = alloc.compositeBuffer();
                        composite.addComponent(cumulation).writerIndex(readable);
                    }
                    composite.addComponent(in).writerIndex(composite.writerIndex() + in.readableBytes());
                    buffer = composite;
                }
                return buffer;
            }
        };
    }
    
    public interface Cumulator
    {
        ByteBuf cumulate(final ByteBufAllocator p0, final ByteBuf p1, final ByteBuf p2);
    }
}
