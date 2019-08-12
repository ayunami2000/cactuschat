// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.compression;

import java.util.concurrent.TimeUnit;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.channel.ChannelPromiseNotifier;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import net.jpountz.lz4.LZ4Exception;
import net.jpountz.xxhash.XXHashFactory;
import net.jpountz.lz4.LZ4Factory;
import io.netty.channel.ChannelHandlerContext;
import java.util.zip.Checksum;
import net.jpountz.lz4.LZ4Compressor;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.MessageToByteEncoder;

public class Lz4FrameEncoder extends MessageToByteEncoder<ByteBuf>
{
    private LZ4Compressor compressor;
    private Checksum checksum;
    private final int compressionLevel;
    private byte[] buffer;
    private int currentBlockLength;
    private final int compressedBlockSize;
    private volatile boolean finished;
    private volatile ChannelHandlerContext ctx;
    
    public Lz4FrameEncoder() {
        this(false);
    }
    
    public Lz4FrameEncoder(final boolean highCompressor) {
        this(LZ4Factory.fastestInstance(), highCompressor, 65536, XXHashFactory.fastestInstance().newStreamingHash32(-1756908916).asChecksum());
    }
    
    public Lz4FrameEncoder(final LZ4Factory factory, final boolean highCompressor, final int blockSize, final Checksum checksum) {
        super(false);
        if (factory == null) {
            throw new NullPointerException("factory");
        }
        if (checksum == null) {
            throw new NullPointerException("checksum");
        }
        this.compressor = (highCompressor ? factory.highCompressor() : factory.fastCompressor());
        this.checksum = checksum;
        this.compressionLevel = compressionLevel(blockSize);
        this.buffer = new byte[blockSize];
        this.currentBlockLength = 0;
        this.compressedBlockSize = 21 + this.compressor.maxCompressedLength(blockSize);
        this.finished = false;
    }
    
    private static int compressionLevel(final int blockSize) {
        if (blockSize < 64 || blockSize > 33554432) {
            throw new IllegalArgumentException(String.format("blockSize: %d (expected: %d-%d)", blockSize, 64, 33554432));
        }
        int compressionLevel = 32 - Integer.numberOfLeadingZeros(blockSize - 1);
        compressionLevel = Math.max(0, compressionLevel - 10);
        return compressionLevel;
    }
    
    @Override
    protected void encode(final ChannelHandlerContext ctx, final ByteBuf in, final ByteBuf out) throws Exception {
        if (this.finished) {
            out.writeBytes(in);
            return;
        }
        int length = in.readableBytes();
        final byte[] buffer = this.buffer;
        int tail;
        for (int blockSize = buffer.length; this.currentBlockLength + length >= blockSize; length -= tail) {
            tail = blockSize - this.currentBlockLength;
            in.getBytes(in.readerIndex(), buffer, this.currentBlockLength, tail);
            this.currentBlockLength = blockSize;
            this.flushBufferedData(out);
            in.skipBytes(tail);
        }
        in.readBytes(buffer, this.currentBlockLength, length);
        this.currentBlockLength += length;
    }
    
    private void flushBufferedData(final ByteBuf out) {
        int currentBlockLength = this.currentBlockLength;
        if (currentBlockLength == 0) {
            return;
        }
        this.checksum.reset();
        this.checksum.update(this.buffer, 0, currentBlockLength);
        final int check = (int)this.checksum.getValue();
        out.ensureWritable(this.compressedBlockSize);
        final int idx = out.writerIndex();
        final byte[] dest = out.array();
        final int destOff = out.arrayOffset() + idx;
        int compressedLength;
        try {
            compressedLength = this.compressor.compress(this.buffer, 0, currentBlockLength, dest, destOff + 21);
        }
        catch (LZ4Exception e) {
            throw new CompressionException((Throwable)e);
        }
        int blockType;
        if (compressedLength >= currentBlockLength) {
            blockType = 16;
            compressedLength = currentBlockLength;
            System.arraycopy(this.buffer, 0, dest, destOff + 21, currentBlockLength);
        }
        else {
            blockType = 32;
        }
        out.setLong(idx, 5501767354678207339L);
        dest[destOff + 8] = (byte)(blockType | this.compressionLevel);
        writeIntLE(compressedLength, dest, destOff + 9);
        writeIntLE(currentBlockLength, dest, destOff + 13);
        writeIntLE(check, dest, destOff + 17);
        out.writerIndex(idx + 21 + compressedLength);
        currentBlockLength = 0;
        this.currentBlockLength = currentBlockLength;
    }
    
    private ChannelFuture finishEncode(final ChannelHandlerContext ctx, final ChannelPromise promise) {
        if (this.finished) {
            promise.setSuccess();
            return promise;
        }
        this.finished = true;
        final ByteBuf footer = ctx.alloc().heapBuffer(this.compressor.maxCompressedLength(this.currentBlockLength) + 21);
        this.flushBufferedData(footer);
        final int idx = footer.writerIndex();
        final byte[] dest = footer.array();
        final int destOff = footer.arrayOffset() + idx;
        footer.setLong(idx, 5501767354678207339L);
        dest[destOff + 8] = (byte)(0x10 | this.compressionLevel);
        writeIntLE(0, dest, destOff + 9);
        writeIntLE(0, dest, destOff + 13);
        writeIntLE(0, dest, destOff + 17);
        footer.writerIndex(idx + 21);
        this.compressor = null;
        this.checksum = null;
        this.buffer = null;
        return ctx.writeAndFlush(footer, promise);
    }
    
    private static void writeIntLE(final int i, final byte[] buf, int off) {
        buf[off++] = (byte)i;
        buf[off++] = (byte)(i >>> 8);
        buf[off++] = (byte)(i >>> 16);
        buf[off] = (byte)(i >>> 24);
    }
    
    public boolean isClosed() {
        return this.finished;
    }
    
    public ChannelFuture close() {
        return this.close(this.ctx().newPromise());
    }
    
    public ChannelFuture close(final ChannelPromise promise) {
        final ChannelHandlerContext ctx = this.ctx();
        final EventExecutor executor = ctx.executor();
        if (executor.inEventLoop()) {
            return this.finishEncode(ctx, promise);
        }
        executor.execute(new Runnable() {
            @Override
            public void run() {
                final ChannelFuture f = Lz4FrameEncoder.this.finishEncode(Lz4FrameEncoder.this.ctx(), promise);
                f.addListener((GenericFutureListener<? extends Future<? super Void>>)new ChannelPromiseNotifier(new ChannelPromise[] { promise }));
            }
        });
        return promise;
    }
    
    @Override
    public void close(final ChannelHandlerContext ctx, final ChannelPromise promise) throws Exception {
        final ChannelFuture f = this.finishEncode(ctx, ctx.newPromise());
        f.addListener((GenericFutureListener<? extends Future<? super Void>>)new ChannelFutureListener() {
            @Override
            public void operationComplete(final ChannelFuture f) throws Exception {
                ctx.close(promise);
            }
        });
        if (!f.isDone()) {
            ctx.executor().schedule((Runnable)new Runnable() {
                @Override
                public void run() {
                    ctx.close(promise);
                }
            }, 10L, TimeUnit.SECONDS);
        }
    }
    
    private ChannelHandlerContext ctx() {
        final ChannelHandlerContext ctx = this.ctx;
        if (ctx == null) {
            throw new IllegalStateException("not added to a pipeline");
        }
        return ctx;
    }
    
    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
    }
}
