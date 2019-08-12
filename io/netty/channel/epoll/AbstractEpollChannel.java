// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.channel.epoll;

import io.netty.channel.ChannelConfig;
import java.nio.ByteBuffer;
import java.nio.channels.UnresolvedAddressException;
import java.net.InetSocketAddress;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
import io.netty.buffer.ByteBuf;
import io.netty.util.internal.OneTimeTask;
import io.netty.channel.EventLoop;
import io.netty.channel.Channel;
import io.netty.channel.unix.FileDescriptor;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.unix.UnixChannel;
import io.netty.channel.AbstractChannel;

abstract class AbstractEpollChannel extends AbstractChannel implements UnixChannel
{
    private static final ChannelMetadata DATA;
    private final int readFlag;
    private final FileDescriptor fileDescriptor;
    protected int flags;
    protected volatile boolean active;
    
    AbstractEpollChannel(final int fd, final int flag) {
        this(null, fd, flag, false);
    }
    
    AbstractEpollChannel(final Channel parent, final int fd, final int flag, final boolean active) {
        this(parent, new FileDescriptor(fd), flag, active);
    }
    
    AbstractEpollChannel(final Channel parent, final FileDescriptor fd, final int flag, final boolean active) {
        super(parent);
        this.flags = Native.EPOLLET;
        if (fd == null) {
            throw new NullPointerException("fd");
        }
        this.readFlag = flag;
        this.flags |= flag;
        this.active = active;
        this.fileDescriptor = fd;
    }
    
    void setFlag(final int flag) {
        if (!this.isFlagSet(flag)) {
            this.flags |= flag;
            this.modifyEvents();
        }
    }
    
    void clearFlag(final int flag) {
        if (this.isFlagSet(flag)) {
            this.flags &= ~flag;
            this.modifyEvents();
        }
    }
    
    boolean isFlagSet(final int flag) {
        return (this.flags & flag) != 0x0;
    }
    
    @Override
    public final FileDescriptor fd() {
        return this.fileDescriptor;
    }
    
    @Override
    public abstract EpollChannelConfig config();
    
    @Override
    public boolean isActive() {
        return this.active;
    }
    
    @Override
    public ChannelMetadata metadata() {
        return AbstractEpollChannel.DATA;
    }
    
    @Override
    protected void doClose() throws Exception {
        this.active = false;
        this.doDeregister();
        final FileDescriptor fd = this.fileDescriptor;
        fd.close();
    }
    
    @Override
    protected void doDisconnect() throws Exception {
        this.doClose();
    }
    
    @Override
    protected boolean isCompatible(final EventLoop loop) {
        return loop instanceof EpollEventLoop;
    }
    
    @Override
    public boolean isOpen() {
        return this.fileDescriptor.isOpen();
    }
    
    @Override
    protected void doDeregister() throws Exception {
        ((EpollEventLoop)this.eventLoop().unwrap()).remove(this);
    }
    
    @Override
    protected void doBeginRead() throws Exception {
        ((AbstractEpollUnsafe)this.unsafe()).readPending = true;
        this.setFlag(this.readFlag);
    }
    
    final void clearEpollIn() {
        if (this.isRegistered()) {
            final EventLoop loop = this.eventLoop();
            final AbstractEpollUnsafe unsafe = (AbstractEpollUnsafe)this.unsafe();
            if (loop.inEventLoop()) {
                unsafe.clearEpollIn0();
            }
            else {
                loop.execute(new OneTimeTask() {
                    @Override
                    public void run() {
                        if (!AbstractEpollChannel.this.config().isAutoRead() && !unsafe.readPending) {
                            unsafe.clearEpollIn0();
                        }
                    }
                });
            }
        }
        else {
            this.flags &= ~this.readFlag;
        }
    }
    
    private void modifyEvents() {
        if (this.isOpen() && this.isRegistered()) {
            ((EpollEventLoop)this.eventLoop().unwrap()).modify(this);
        }
    }
    
    @Override
    protected void doRegister() throws Exception {
        ((EpollEventLoop)this.eventLoop().unwrap()).add(this);
    }
    
    @Override
    protected abstract AbstractEpollUnsafe newUnsafe();
    
    protected final ByteBuf newDirectBuffer(final ByteBuf buf) {
        return this.newDirectBuffer(buf, buf);
    }
    
    protected final ByteBuf newDirectBuffer(final Object holder, final ByteBuf buf) {
        final int readableBytes = buf.readableBytes();
        if (readableBytes == 0) {
            ReferenceCountUtil.safeRelease(holder);
            return Unpooled.EMPTY_BUFFER;
        }
        final ByteBufAllocator alloc = this.alloc();
        if (alloc.isDirectBufferPooled()) {
            return newDirectBuffer0(holder, buf, alloc, readableBytes);
        }
        final ByteBuf directBuf = ByteBufUtil.threadLocalDirectBuffer();
        if (directBuf == null) {
            return newDirectBuffer0(holder, buf, alloc, readableBytes);
        }
        directBuf.writeBytes(buf, buf.readerIndex(), readableBytes);
        ReferenceCountUtil.safeRelease(holder);
        return directBuf;
    }
    
    private static ByteBuf newDirectBuffer0(final Object holder, final ByteBuf buf, final ByteBufAllocator alloc, final int capacity) {
        final ByteBuf directBuf = alloc.directBuffer(capacity);
        directBuf.writeBytes(buf, buf.readerIndex(), capacity);
        ReferenceCountUtil.safeRelease(holder);
        return directBuf;
    }
    
    protected static void checkResolvable(final InetSocketAddress addr) {
        if (addr.isUnresolved()) {
            throw new UnresolvedAddressException();
        }
    }
    
    protected final int doReadBytes(final ByteBuf byteBuf) throws Exception {
        final int writerIndex = byteBuf.writerIndex();
        int localReadAmount;
        if (byteBuf.hasMemoryAddress()) {
            localReadAmount = Native.readAddress(this.fileDescriptor.intValue(), byteBuf.memoryAddress(), writerIndex, byteBuf.capacity());
        }
        else {
            final ByteBuffer buf = byteBuf.internalNioBuffer(writerIndex, byteBuf.writableBytes());
            localReadAmount = Native.read(this.fileDescriptor.intValue(), buf, buf.position(), buf.limit());
        }
        if (localReadAmount > 0) {
            byteBuf.writerIndex(writerIndex + localReadAmount);
        }
        return localReadAmount;
    }
    
    protected final int doWriteBytes(final ByteBuf buf, final int writeSpinCount) throws Exception {
        final int readableBytes = buf.readableBytes();
        int writtenBytes = 0;
        if (buf.hasMemoryAddress()) {
            final long memoryAddress = buf.memoryAddress();
            int readerIndex = buf.readerIndex();
            final int writerIndex = buf.writerIndex();
            for (int i = writeSpinCount - 1; i >= 0; --i) {
                final int localFlushedAmount = Native.writeAddress(this.fileDescriptor.intValue(), memoryAddress, readerIndex, writerIndex);
                if (localFlushedAmount <= 0) {
                    break;
                }
                writtenBytes += localFlushedAmount;
                if (writtenBytes == readableBytes) {
                    return writtenBytes;
                }
                readerIndex += localFlushedAmount;
            }
        }
        else {
            ByteBuffer nioBuf;
            if (buf.nioBufferCount() == 1) {
                nioBuf = buf.internalNioBuffer(buf.readerIndex(), buf.readableBytes());
            }
            else {
                nioBuf = buf.nioBuffer();
            }
            for (int j = writeSpinCount - 1; j >= 0; --j) {
                final int pos = nioBuf.position();
                final int limit = nioBuf.limit();
                final int localFlushedAmount2 = Native.write(this.fileDescriptor.intValue(), nioBuf, pos, limit);
                if (localFlushedAmount2 <= 0) {
                    break;
                }
                nioBuf.position(pos + localFlushedAmount2);
                writtenBytes += localFlushedAmount2;
                if (writtenBytes == readableBytes) {
                    return writtenBytes;
                }
            }
        }
        if (writtenBytes < readableBytes) {
            this.setFlag(Native.EPOLLOUT);
        }
        return writtenBytes;
    }
    
    static {
        DATA = new ChannelMetadata(false);
    }
    
    protected abstract class AbstractEpollUnsafe extends AbstractUnsafe
    {
        protected boolean readPending;
        
        abstract void epollInReady();
        
        void epollRdHupReady() {
        }
        
        @Override
        protected void flush0() {
            if (AbstractEpollChannel.this.isFlagSet(Native.EPOLLOUT)) {
                return;
            }
            super.flush0();
        }
        
        void epollOutReady() {
            super.flush0();
        }
        
        protected final void clearEpollIn0() {
            AbstractEpollChannel.this.clearFlag(AbstractEpollChannel.this.readFlag);
        }
    }
}
