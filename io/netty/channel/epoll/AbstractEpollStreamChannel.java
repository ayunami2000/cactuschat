// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.channel.epoll;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelConfig;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Future;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import java.util.concurrent.TimeUnit;
import io.netty.channel.ConnectTimeoutException;
import io.netty.channel.socket.ChannelInputShutdownEvent;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.RecvByteBufAllocator;
import java.util.concurrent.ScheduledFuture;
import io.netty.channel.AbstractChannel;
import io.netty.channel.ChannelMetadata;
import java.net.SocketAddress;
import io.netty.channel.ChannelPromise;
import io.netty.util.internal.StringUtil;
import io.netty.buffer.CompositeByteBuf;
import io.netty.util.internal.PlatformDependent;
import io.netty.channel.DefaultFileRegion;
import java.io.IOException;
import java.nio.ByteBuffer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.unix.FileDescriptor;
import io.netty.channel.Channel;

public abstract class AbstractEpollStreamChannel extends AbstractEpollChannel
{
    private static final String EXPECTED_TYPES;
    private volatile boolean inputShutdown;
    private volatile boolean outputShutdown;
    
    protected AbstractEpollStreamChannel(final Channel parent, final int fd) {
        super(parent, fd, Native.EPOLLIN, true);
        this.flags |= Native.EPOLLRDHUP;
    }
    
    protected AbstractEpollStreamChannel(final int fd) {
        super(fd, Native.EPOLLIN);
        this.flags |= Native.EPOLLRDHUP;
    }
    
    protected AbstractEpollStreamChannel(final FileDescriptor fd) {
        super(null, fd, Native.EPOLLIN, Native.getSoError(fd.intValue()) == 0);
    }
    
    @Override
    protected AbstractEpollUnsafe newUnsafe() {
        return new EpollStreamUnsafe();
    }
    
    private boolean writeBytes(final ChannelOutboundBuffer in, final ByteBuf buf, final int writeSpinCount) throws Exception {
        final int readableBytes = buf.readableBytes();
        if (readableBytes == 0) {
            in.remove();
            return true;
        }
        if (buf.hasMemoryAddress() || buf.nioBufferCount() == 1) {
            final int writtenBytes = this.doWriteBytes(buf, writeSpinCount);
            in.removeBytes(writtenBytes);
            return writtenBytes == readableBytes;
        }
        final ByteBuffer[] nioBuffers = buf.nioBuffers();
        return this.writeBytesMultiple(in, nioBuffers, nioBuffers.length, readableBytes, writeSpinCount);
    }
    
    private boolean writeBytesMultiple(final ChannelOutboundBuffer in, final IovArray array, final int writeSpinCount) throws IOException {
        final long initialExpectedWrittenBytes;
        long expectedWrittenBytes = initialExpectedWrittenBytes = array.size();
        int cnt = array.count();
        assert expectedWrittenBytes != 0L;
        assert cnt != 0;
        boolean done = false;
        int offset = 0;
        final int end = offset + cnt;
        for (int i = writeSpinCount - 1; i >= 0; --i) {
            long localWrittenBytes = Native.writevAddresses(this.fd().intValue(), array.memoryAddress(offset), cnt);
            if (localWrittenBytes == 0L) {
                break;
            }
            expectedWrittenBytes -= localWrittenBytes;
            if (expectedWrittenBytes == 0L) {
                done = true;
                break;
            }
            do {
                final long bytes = array.processWritten(offset, localWrittenBytes);
                if (bytes == -1L) {
                    break;
                }
                ++offset;
                --cnt;
                localWrittenBytes -= bytes;
            } while (offset < end && localWrittenBytes > 0L);
        }
        if (!done) {
            this.setFlag(Native.EPOLLOUT);
        }
        in.removeBytes(initialExpectedWrittenBytes - expectedWrittenBytes);
        return done;
    }
    
    private boolean writeBytesMultiple(final ChannelOutboundBuffer in, final ByteBuffer[] nioBuffers, int nioBufferCnt, long expectedWrittenBytes, final int writeSpinCount) throws IOException {
        assert expectedWrittenBytes != 0L;
        final long initialExpectedWrittenBytes = expectedWrittenBytes;
        boolean done = false;
        int offset = 0;
        final int end = offset + nioBufferCnt;
        for (int i = writeSpinCount - 1; i >= 0; --i) {
            long localWrittenBytes = Native.writev(this.fd().intValue(), nioBuffers, offset, nioBufferCnt);
            if (localWrittenBytes == 0L) {
                break;
            }
            expectedWrittenBytes -= localWrittenBytes;
            if (expectedWrittenBytes == 0L) {
                done = true;
                break;
            }
            do {
                final ByteBuffer buffer = nioBuffers[offset];
                final int pos = buffer.position();
                final int bytes = buffer.limit() - pos;
                if (bytes > localWrittenBytes) {
                    buffer.position(pos + (int)localWrittenBytes);
                    break;
                }
                ++offset;
                --nioBufferCnt;
                localWrittenBytes -= bytes;
            } while (offset < end && localWrittenBytes > 0L);
        }
        in.removeBytes(initialExpectedWrittenBytes - expectedWrittenBytes);
        if (!done) {
            this.setFlag(Native.EPOLLOUT);
        }
        return done;
    }
    
    private boolean writeFileRegion(final ChannelOutboundBuffer in, final DefaultFileRegion region, final int writeSpinCount) throws Exception {
        final long regionCount = region.count();
        if (region.transfered() >= regionCount) {
            in.remove();
            return true;
        }
        final long baseOffset = region.position();
        boolean done = false;
        long flushedAmount = 0L;
        for (int i = writeSpinCount - 1; i >= 0; --i) {
            final long offset = region.transfered();
            final long localFlushedAmount = Native.sendfile(this.fd().intValue(), region, baseOffset, offset, regionCount - offset);
            if (localFlushedAmount == 0L) {
                break;
            }
            flushedAmount += localFlushedAmount;
            if (region.transfered() >= regionCount) {
                done = true;
                break;
            }
        }
        if (flushedAmount > 0L) {
            in.progress(flushedAmount);
        }
        if (done) {
            in.remove();
        }
        else {
            this.setFlag(Native.EPOLLOUT);
        }
        return done;
    }
    
    @Override
    protected void doWrite(final ChannelOutboundBuffer in) throws Exception {
        final int writeSpinCount = this.config().getWriteSpinCount();
        while (true) {
            final int msgCount = in.size();
            if (msgCount == 0) {
                this.clearFlag(Native.EPOLLOUT);
                break;
            }
            if (msgCount > 1 && in.current() instanceof ByteBuf) {
                if (!this.doWriteMultiple(in, writeSpinCount)) {
                    break;
                }
                continue;
            }
            else {
                if (!this.doWriteSingle(in, writeSpinCount)) {
                    break;
                }
                continue;
            }
        }
    }
    
    protected boolean doWriteSingle(final ChannelOutboundBuffer in, final int writeSpinCount) throws Exception {
        final Object msg = in.current();
        if (msg instanceof ByteBuf) {
            final ByteBuf buf = (ByteBuf)msg;
            if (!this.writeBytes(in, buf, writeSpinCount)) {
                return false;
            }
        }
        else {
            if (!(msg instanceof DefaultFileRegion)) {
                throw new Error();
            }
            final DefaultFileRegion region = (DefaultFileRegion)msg;
            if (!this.writeFileRegion(in, region, writeSpinCount)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean doWriteMultiple(final ChannelOutboundBuffer in, final int writeSpinCount) throws Exception {
        if (PlatformDependent.hasUnsafe()) {
            final IovArray array = IovArrayThreadLocal.get(in);
            final int cnt = array.count();
            if (cnt >= 1) {
                if (!this.writeBytesMultiple(in, array, writeSpinCount)) {
                    return false;
                }
            }
            else {
                in.removeBytes(0L);
            }
        }
        else {
            final ByteBuffer[] buffers = in.nioBuffers();
            final int cnt = in.nioBufferCount();
            if (cnt >= 1) {
                if (!this.writeBytesMultiple(in, buffers, cnt, in.nioBufferSize(), writeSpinCount)) {
                    return false;
                }
            }
            else {
                in.removeBytes(0L);
            }
        }
        return true;
    }
    
    @Override
    protected Object filterOutboundMessage(final Object msg) {
        if (msg instanceof ByteBuf) {
            ByteBuf buf = (ByteBuf)msg;
            if (!buf.hasMemoryAddress() && (PlatformDependent.hasUnsafe() || !buf.isDirect())) {
                if (buf instanceof CompositeByteBuf) {
                    final CompositeByteBuf comp = (CompositeByteBuf)buf;
                    if (!comp.isDirect() || comp.nioBufferCount() > Native.IOV_MAX) {
                        buf = this.newDirectBuffer(buf);
                        assert buf.hasMemoryAddress();
                    }
                }
                else {
                    buf = this.newDirectBuffer(buf);
                    assert buf.hasMemoryAddress();
                }
            }
            return buf;
        }
        if (msg instanceof DefaultFileRegion) {
            return msg;
        }
        throw new UnsupportedOperationException("unsupported message type: " + StringUtil.simpleClassName(msg) + AbstractEpollStreamChannel.EXPECTED_TYPES);
    }
    
    protected boolean isInputShutdown0() {
        return this.inputShutdown;
    }
    
    protected boolean isOutputShutdown0() {
        return this.outputShutdown || !this.isActive();
    }
    
    protected void shutdownOutput0(final ChannelPromise promise) {
        try {
            Native.shutdown(this.fd().intValue(), false, true);
            this.outputShutdown = true;
            promise.setSuccess();
        }
        catch (Throwable cause) {
            promise.setFailure(cause);
        }
    }
    
    protected boolean doConnect(final SocketAddress remoteAddress, final SocketAddress localAddress) throws Exception {
        if (localAddress != null) {
            Native.bind(this.fd().intValue(), localAddress);
        }
        boolean success = false;
        try {
            final boolean connected = Native.connect(this.fd().intValue(), remoteAddress);
            if (!connected) {
                this.setFlag(Native.EPOLLOUT);
            }
            success = true;
            return connected;
        }
        finally {
            if (!success) {
                this.doClose();
            }
        }
    }
    
    static {
        EXPECTED_TYPES = " (expected: " + StringUtil.simpleClassName(ByteBuf.class) + ", " + StringUtil.simpleClassName(DefaultFileRegion.class) + ')';
    }
    
    class EpollStreamUnsafe extends AbstractEpollUnsafe
    {
        private ChannelPromise connectPromise;
        private ScheduledFuture<?> connectTimeoutFuture;
        private SocketAddress requestedRemoteAddress;
        private RecvByteBufAllocator.Handle allocHandle;
        
        private void closeOnRead(final ChannelPipeline pipeline) {
            AbstractEpollStreamChannel.this.inputShutdown = true;
            if (AbstractEpollStreamChannel.this.isOpen()) {
                if (Boolean.TRUE.equals(AbstractEpollStreamChannel.this.config().getOption(ChannelOption.ALLOW_HALF_CLOSURE))) {
                    this.clearEpollIn0();
                    pipeline.fireUserEventTriggered(ChannelInputShutdownEvent.INSTANCE);
                }
                else {
                    this.close(this.voidPromise());
                }
            }
        }
        
        private boolean handleReadException(final ChannelPipeline pipeline, final ByteBuf byteBuf, final Throwable cause, final boolean close) {
            if (byteBuf != null) {
                if (byteBuf.isReadable()) {
                    this.readPending = false;
                    pipeline.fireChannelRead(byteBuf);
                }
                else {
                    byteBuf.release();
                }
            }
            pipeline.fireChannelReadComplete();
            pipeline.fireExceptionCaught(cause);
            if (close || cause instanceof IOException) {
                this.closeOnRead(pipeline);
                return true;
            }
            return false;
        }
        
        @Override
        public void connect(final SocketAddress remoteAddress, final SocketAddress localAddress, final ChannelPromise promise) {
            if (!promise.setUncancellable() || !this.ensureOpen(promise)) {
                return;
            }
            try {
                if (this.connectPromise != null) {
                    throw new IllegalStateException("connection attempt already made");
                }
                final boolean wasActive = AbstractEpollStreamChannel.this.isActive();
                if (AbstractEpollStreamChannel.this.doConnect(remoteAddress, localAddress)) {
                    this.fulfillConnectPromise(promise, wasActive);
                }
                else {
                    this.connectPromise = promise;
                    this.requestedRemoteAddress = remoteAddress;
                    final int connectTimeoutMillis = AbstractEpollStreamChannel.this.config().getConnectTimeoutMillis();
                    if (connectTimeoutMillis > 0) {
                        this.connectTimeoutFuture = AbstractEpollStreamChannel.this.eventLoop().schedule((Runnable)new Runnable() {
                            @Override
                            public void run() {
                                final ChannelPromise connectPromise = EpollStreamUnsafe.this.connectPromise;
                                final ConnectTimeoutException cause = new ConnectTimeoutException("connection timed out: " + remoteAddress);
                                if (connectPromise != null && connectPromise.tryFailure(cause)) {
                                    EpollStreamUnsafe.this.close(EpollStreamUnsafe.this.voidPromise());
                                }
                            }
                        }, (long)connectTimeoutMillis, TimeUnit.MILLISECONDS);
                    }
                    promise.addListener((GenericFutureListener<? extends Future<? super Void>>)new ChannelFutureListener() {
                        @Override
                        public void operationComplete(final ChannelFuture future) throws Exception {
                            if (future.isCancelled()) {
                                if (EpollStreamUnsafe.this.connectTimeoutFuture != null) {
                                    EpollStreamUnsafe.this.connectTimeoutFuture.cancel(false);
                                }
                                EpollStreamUnsafe.this.connectPromise = null;
                                EpollStreamUnsafe.this.close(EpollStreamUnsafe.this.voidPromise());
                            }
                        }
                    });
                }
            }
            catch (Throwable t) {
                this.closeIfClosed();
                promise.tryFailure(this.annotateConnectException(t, remoteAddress));
            }
        }
        
        private void fulfillConnectPromise(final ChannelPromise promise, final boolean wasActive) {
            if (promise == null) {
                return;
            }
            AbstractEpollStreamChannel.this.active = true;
            final boolean promiseSet = promise.trySuccess();
            if (!wasActive && AbstractEpollStreamChannel.this.isActive()) {
                AbstractEpollStreamChannel.this.pipeline().fireChannelActive();
            }
            if (!promiseSet) {
                this.close(this.voidPromise());
            }
        }
        
        private void fulfillConnectPromise(final ChannelPromise promise, final Throwable cause) {
            if (promise == null) {
                return;
            }
            promise.tryFailure(cause);
            this.closeIfClosed();
        }
        
        private void finishConnect() {
            assert AbstractEpollStreamChannel.this.eventLoop().inEventLoop();
            boolean connectStillInProgress = false;
            try {
                final boolean wasActive = AbstractEpollStreamChannel.this.isActive();
                if (!this.doFinishConnect()) {
                    connectStillInProgress = true;
                    return;
                }
                this.fulfillConnectPromise(this.connectPromise, wasActive);
            }
            catch (Throwable t) {
                this.fulfillConnectPromise(this.connectPromise, this.annotateConnectException(t, this.requestedRemoteAddress));
            }
            finally {
                if (!connectStillInProgress) {
                    if (this.connectTimeoutFuture != null) {
                        this.connectTimeoutFuture.cancel(false);
                    }
                    this.connectPromise = null;
                }
            }
        }
        
        @Override
        void epollOutReady() {
            if (this.connectPromise != null) {
                this.finishConnect();
            }
            else {
                super.epollOutReady();
            }
        }
        
        private boolean doFinishConnect() throws Exception {
            if (Native.finishConnect(AbstractEpollStreamChannel.this.fd().intValue())) {
                AbstractEpollStreamChannel.this.clearFlag(Native.EPOLLOUT);
                return true;
            }
            AbstractEpollStreamChannel.this.setFlag(Native.EPOLLOUT);
            return false;
        }
        
        @Override
        void epollRdHupReady() {
            if (AbstractEpollStreamChannel.this.isActive()) {
                this.epollInReady();
            }
            else {
                this.closeOnRead(AbstractEpollStreamChannel.this.pipeline());
            }
        }
        
        @Override
        void epollInReady() {
            final ChannelConfig config = AbstractEpollStreamChannel.this.config();
            final boolean edgeTriggered = AbstractEpollStreamChannel.this.isFlagSet(Native.EPOLLET);
            if (!this.readPending && !edgeTriggered && !config.isAutoRead()) {
                this.clearEpollIn0();
                return;
            }
            final ChannelPipeline pipeline = AbstractEpollStreamChannel.this.pipeline();
            final ByteBufAllocator allocator = config.getAllocator();
            RecvByteBufAllocator.Handle allocHandle = this.allocHandle;
            if (allocHandle == null) {
                allocHandle = (this.allocHandle = config.getRecvByteBufAllocator().newHandle());
            }
            ByteBuf byteBuf = null;
            boolean close = false;
            try {
                final int maxMessagesPerRead = edgeTriggered ? Integer.MAX_VALUE : config.getMaxMessagesPerRead();
                int messages = 0;
                int totalReadAmount = 0;
                do {
                    byteBuf = allocHandle.allocate(allocator);
                    final int writable = byteBuf.writableBytes();
                    final int localReadAmount = AbstractEpollStreamChannel.this.doReadBytes(byteBuf);
                    if (localReadAmount <= 0) {
                        byteBuf.release();
                        close = (localReadAmount < 0);
                        break;
                    }
                    this.readPending = false;
                    pipeline.fireChannelRead(byteBuf);
                    byteBuf = null;
                    if (totalReadAmount >= Integer.MAX_VALUE - localReadAmount) {
                        allocHandle.record(totalReadAmount);
                        totalReadAmount = localReadAmount;
                    }
                    else {
                        totalReadAmount += localReadAmount;
                    }
                    if (localReadAmount < writable) {
                        break;
                    }
                    if (!edgeTriggered && !config.isAutoRead()) {
                        break;
                    }
                } while (++messages < maxMessagesPerRead);
                pipeline.fireChannelReadComplete();
                allocHandle.record(totalReadAmount);
                if (close) {
                    this.closeOnRead(pipeline);
                    close = false;
                }
            }
            catch (Throwable t) {
                final boolean closed = this.handleReadException(pipeline, byteBuf, t, close);
                if (!closed) {
                    AbstractEpollStreamChannel.this.eventLoop().execute(new Runnable() {
                        @Override
                        public void run() {
                            EpollStreamUnsafe.this.epollInReady();
                        }
                    });
                }
            }
            finally {
                if (!this.readPending && !config.isAutoRead()) {
                    this.clearEpollIn0();
                }
            }
        }
    }
}
