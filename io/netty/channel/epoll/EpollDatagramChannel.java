// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.channel.epoll;

import io.netty.channel.ChannelPipeline;
import io.netty.channel.RecvByteBufAllocator;
import java.util.ArrayList;
import java.util.List;
import io.netty.channel.socket.DatagramChannelConfig;
import io.netty.channel.AbstractChannel;
import io.netty.channel.ChannelConfig;
import io.netty.util.internal.StringUtil;
import io.netty.channel.DefaultAddressedEnvelope;
import io.netty.util.internal.PlatformDependent;
import io.netty.channel.socket.DatagramPacket;
import java.nio.ByteBuffer;
import io.netty.buffer.CompositeByteBuf;
import java.nio.channels.NotYetConnectedException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.AddressedEnvelope;
import java.io.IOException;
import io.netty.channel.ChannelOutboundBuffer;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.NetworkInterface;
import io.netty.channel.ChannelPromise;
import io.netty.channel.ChannelFuture;
import java.net.InetAddress;
import io.netty.channel.ChannelOption;
import io.netty.channel.Channel;
import io.netty.channel.unix.FileDescriptor;
import java.net.InetSocketAddress;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.socket.DatagramChannel;

public final class EpollDatagramChannel extends AbstractEpollChannel implements DatagramChannel
{
    private static final ChannelMetadata METADATA;
    private static final String EXPECTED_TYPES;
    private volatile InetSocketAddress local;
    private volatile InetSocketAddress remote;
    private volatile boolean connected;
    private final EpollDatagramChannelConfig config;
    
    public EpollDatagramChannel() {
        super(Native.socketDgramFd(), Native.EPOLLIN);
        this.config = new EpollDatagramChannelConfig(this);
    }
    
    public EpollDatagramChannel(final FileDescriptor fd) {
        super(null, fd, Native.EPOLLIN, true);
        this.config = new EpollDatagramChannelConfig(this);
        this.local = Native.localAddress(fd.intValue());
    }
    
    @Override
    public InetSocketAddress remoteAddress() {
        return (InetSocketAddress)super.remoteAddress();
    }
    
    @Override
    public InetSocketAddress localAddress() {
        return (InetSocketAddress)super.localAddress();
    }
    
    @Override
    public ChannelMetadata metadata() {
        return EpollDatagramChannel.METADATA;
    }
    
    @Override
    public boolean isActive() {
        return this.fd().isOpen() && ((this.config.getOption(ChannelOption.DATAGRAM_CHANNEL_ACTIVE_ON_REGISTRATION) && this.isRegistered()) || this.active);
    }
    
    @Override
    public boolean isConnected() {
        return this.connected;
    }
    
    @Override
    public ChannelFuture joinGroup(final InetAddress multicastAddress) {
        return this.joinGroup(multicastAddress, this.newPromise());
    }
    
    @Override
    public ChannelFuture joinGroup(final InetAddress multicastAddress, final ChannelPromise promise) {
        try {
            return this.joinGroup(multicastAddress, NetworkInterface.getByInetAddress(this.localAddress().getAddress()), null, promise);
        }
        catch (SocketException e) {
            promise.setFailure((Throwable)e);
            return promise;
        }
    }
    
    @Override
    public ChannelFuture joinGroup(final InetSocketAddress multicastAddress, final NetworkInterface networkInterface) {
        return this.joinGroup(multicastAddress, networkInterface, this.newPromise());
    }
    
    @Override
    public ChannelFuture joinGroup(final InetSocketAddress multicastAddress, final NetworkInterface networkInterface, final ChannelPromise promise) {
        return this.joinGroup(multicastAddress.getAddress(), networkInterface, null, promise);
    }
    
    @Override
    public ChannelFuture joinGroup(final InetAddress multicastAddress, final NetworkInterface networkInterface, final InetAddress source) {
        return this.joinGroup(multicastAddress, networkInterface, source, this.newPromise());
    }
    
    @Override
    public ChannelFuture joinGroup(final InetAddress multicastAddress, final NetworkInterface networkInterface, final InetAddress source, final ChannelPromise promise) {
        if (multicastAddress == null) {
            throw new NullPointerException("multicastAddress");
        }
        if (networkInterface == null) {
            throw new NullPointerException("networkInterface");
        }
        promise.setFailure((Throwable)new UnsupportedOperationException("Multicast not supported"));
        return promise;
    }
    
    @Override
    public ChannelFuture leaveGroup(final InetAddress multicastAddress) {
        return this.leaveGroup(multicastAddress, this.newPromise());
    }
    
    @Override
    public ChannelFuture leaveGroup(final InetAddress multicastAddress, final ChannelPromise promise) {
        try {
            return this.leaveGroup(multicastAddress, NetworkInterface.getByInetAddress(this.localAddress().getAddress()), null, promise);
        }
        catch (SocketException e) {
            promise.setFailure((Throwable)e);
            return promise;
        }
    }
    
    @Override
    public ChannelFuture leaveGroup(final InetSocketAddress multicastAddress, final NetworkInterface networkInterface) {
        return this.leaveGroup(multicastAddress, networkInterface, this.newPromise());
    }
    
    @Override
    public ChannelFuture leaveGroup(final InetSocketAddress multicastAddress, final NetworkInterface networkInterface, final ChannelPromise promise) {
        return this.leaveGroup(multicastAddress.getAddress(), networkInterface, null, promise);
    }
    
    @Override
    public ChannelFuture leaveGroup(final InetAddress multicastAddress, final NetworkInterface networkInterface, final InetAddress source) {
        return this.leaveGroup(multicastAddress, networkInterface, source, this.newPromise());
    }
    
    @Override
    public ChannelFuture leaveGroup(final InetAddress multicastAddress, final NetworkInterface networkInterface, final InetAddress source, final ChannelPromise promise) {
        if (multicastAddress == null) {
            throw new NullPointerException("multicastAddress");
        }
        if (networkInterface == null) {
            throw new NullPointerException("networkInterface");
        }
        promise.setFailure((Throwable)new UnsupportedOperationException("Multicast not supported"));
        return promise;
    }
    
    @Override
    public ChannelFuture block(final InetAddress multicastAddress, final NetworkInterface networkInterface, final InetAddress sourceToBlock) {
        return this.block(multicastAddress, networkInterface, sourceToBlock, this.newPromise());
    }
    
    @Override
    public ChannelFuture block(final InetAddress multicastAddress, final NetworkInterface networkInterface, final InetAddress sourceToBlock, final ChannelPromise promise) {
        if (multicastAddress == null) {
            throw new NullPointerException("multicastAddress");
        }
        if (sourceToBlock == null) {
            throw new NullPointerException("sourceToBlock");
        }
        if (networkInterface == null) {
            throw new NullPointerException("networkInterface");
        }
        promise.setFailure((Throwable)new UnsupportedOperationException("Multicast not supported"));
        return promise;
    }
    
    @Override
    public ChannelFuture block(final InetAddress multicastAddress, final InetAddress sourceToBlock) {
        return this.block(multicastAddress, sourceToBlock, this.newPromise());
    }
    
    @Override
    public ChannelFuture block(final InetAddress multicastAddress, final InetAddress sourceToBlock, final ChannelPromise promise) {
        try {
            return this.block(multicastAddress, NetworkInterface.getByInetAddress(this.localAddress().getAddress()), sourceToBlock, promise);
        }
        catch (Throwable e) {
            promise.setFailure(e);
            return promise;
        }
    }
    
    @Override
    protected AbstractEpollUnsafe newUnsafe() {
        return new EpollDatagramChannelUnsafe();
    }
    
    @Override
    protected InetSocketAddress localAddress0() {
        return this.local;
    }
    
    @Override
    protected InetSocketAddress remoteAddress0() {
        return this.remote;
    }
    
    @Override
    protected void doBind(final SocketAddress localAddress) throws Exception {
        final InetSocketAddress addr = (InetSocketAddress)localAddress;
        AbstractEpollChannel.checkResolvable(addr);
        final int fd = this.fd().intValue();
        Native.bind(fd, addr);
        this.local = Native.localAddress(fd);
        this.active = true;
    }
    
    @Override
    protected void doWrite(final ChannelOutboundBuffer in) throws Exception {
        while (true) {
            final Object msg = in.current();
            if (msg == null) {
                this.clearFlag(Native.EPOLLOUT);
                break;
            }
            try {
                if (Native.IS_SUPPORTING_SENDMMSG && in.size() > 1) {
                    final NativeDatagramPacketArray array = NativeDatagramPacketArray.getInstance(in);
                    int cnt = array.count();
                    if (cnt >= 1) {
                        int offset = 0;
                        final NativeDatagramPacketArray.NativeDatagramPacket[] packets = array.packets();
                        while (cnt > 0) {
                            final int send = Native.sendmmsg(this.fd().intValue(), packets, offset, cnt);
                            if (send == 0) {
                                this.setFlag(Native.EPOLLOUT);
                                return;
                            }
                            for (int i = 0; i < send; ++i) {
                                in.remove();
                            }
                            cnt -= send;
                            offset += send;
                        }
                        continue;
                    }
                }
                boolean done = false;
                for (int j = this.config().getWriteSpinCount() - 1; j >= 0; --j) {
                    if (this.doWriteMessage(msg)) {
                        done = true;
                        break;
                    }
                }
                if (!done) {
                    this.setFlag(Native.EPOLLOUT);
                    break;
                }
                in.remove();
            }
            catch (IOException e) {
                in.remove(e);
            }
        }
    }
    
    private boolean doWriteMessage(final Object msg) throws Exception {
        ByteBuf data;
        InetSocketAddress remoteAddress;
        if (msg instanceof AddressedEnvelope) {
            final AddressedEnvelope<ByteBuf, InetSocketAddress> envelope = (AddressedEnvelope<ByteBuf, InetSocketAddress>)msg;
            data = envelope.content();
            remoteAddress = envelope.recipient();
        }
        else {
            data = (ByteBuf)msg;
            remoteAddress = null;
        }
        final int dataLen = data.readableBytes();
        if (dataLen == 0) {
            return true;
        }
        if (remoteAddress == null) {
            remoteAddress = this.remote;
            if (remoteAddress == null) {
                throw new NotYetConnectedException();
            }
        }
        int writtenBytes;
        if (data.hasMemoryAddress()) {
            final long memoryAddress = data.memoryAddress();
            writtenBytes = Native.sendToAddress(this.fd().intValue(), memoryAddress, data.readerIndex(), data.writerIndex(), remoteAddress.getAddress(), remoteAddress.getPort());
        }
        else if (data instanceof CompositeByteBuf) {
            final IovArray array = IovArrayThreadLocal.get((CompositeByteBuf)data);
            final int cnt = array.count();
            assert cnt != 0;
            writtenBytes = Native.sendToAddresses(this.fd().intValue(), array.memoryAddress(0), cnt, remoteAddress.getAddress(), remoteAddress.getPort());
        }
        else {
            final ByteBuffer nioData = data.internalNioBuffer(data.readerIndex(), data.readableBytes());
            writtenBytes = Native.sendTo(this.fd().intValue(), nioData, nioData.position(), nioData.limit(), remoteAddress.getAddress(), remoteAddress.getPort());
        }
        return writtenBytes > 0;
    }
    
    @Override
    protected Object filterOutboundMessage(final Object msg) {
        if (msg instanceof DatagramPacket) {
            final DatagramPacket packet = (DatagramPacket)msg;
            final ByteBuf content = ((DefaultAddressedEnvelope<ByteBuf, A>)packet).content();
            if (content.hasMemoryAddress()) {
                return msg;
            }
            if (content.isDirect() && content instanceof CompositeByteBuf) {
                final CompositeByteBuf comp = (CompositeByteBuf)content;
                if (comp.isDirect() && comp.nioBufferCount() <= Native.IOV_MAX) {
                    return msg;
                }
            }
            return new DatagramPacket(this.newDirectBuffer(packet, content), ((DefaultAddressedEnvelope<M, InetSocketAddress>)packet).recipient());
        }
        else {
            if (msg instanceof ByteBuf) {
                ByteBuf buf = (ByteBuf)msg;
                if (!buf.hasMemoryAddress() && (PlatformDependent.hasUnsafe() || !buf.isDirect())) {
                    if (buf instanceof CompositeByteBuf) {
                        final CompositeByteBuf comp2 = (CompositeByteBuf)buf;
                        if (!comp2.isDirect() || comp2.nioBufferCount() > Native.IOV_MAX) {
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
            if (msg instanceof AddressedEnvelope) {
                final AddressedEnvelope<Object, SocketAddress> e = (AddressedEnvelope<Object, SocketAddress>)msg;
                if (e.content() instanceof ByteBuf && (e.recipient() == null || e.recipient() instanceof InetSocketAddress)) {
                    final ByteBuf content = e.content();
                    if (content.hasMemoryAddress()) {
                        return e;
                    }
                    if (content instanceof CompositeByteBuf) {
                        final CompositeByteBuf comp = (CompositeByteBuf)content;
                        if (comp.isDirect() && comp.nioBufferCount() <= Native.IOV_MAX) {
                            return e;
                        }
                    }
                    return new DefaultAddressedEnvelope(this.newDirectBuffer(e, content), e.recipient());
                }
            }
            throw new UnsupportedOperationException("unsupported message type: " + StringUtil.simpleClassName(msg) + EpollDatagramChannel.EXPECTED_TYPES);
        }
    }
    
    @Override
    public EpollDatagramChannelConfig config() {
        return this.config;
    }
    
    @Override
    protected void doDisconnect() throws Exception {
        this.connected = false;
    }
    
    static {
        METADATA = new ChannelMetadata(true);
        EXPECTED_TYPES = " (expected: " + StringUtil.simpleClassName(DatagramPacket.class) + ", " + StringUtil.simpleClassName(AddressedEnvelope.class) + '<' + StringUtil.simpleClassName(ByteBuf.class) + ", " + StringUtil.simpleClassName(InetSocketAddress.class) + ">, " + StringUtil.simpleClassName(ByteBuf.class) + ')';
    }
    
    final class EpollDatagramChannelUnsafe extends AbstractEpollUnsafe
    {
        private final List<Object> readBuf;
        
        EpollDatagramChannelUnsafe() {
            this.readBuf = new ArrayList<Object>();
        }
        
        @Override
        public void connect(final SocketAddress remote, final SocketAddress local, final ChannelPromise channelPromise) {
            boolean success = false;
            try {
                try {
                    final boolean wasActive = EpollDatagramChannel.this.isActive();
                    final InetSocketAddress remoteAddress = (InetSocketAddress)remote;
                    if (local != null) {
                        final InetSocketAddress localAddress = (InetSocketAddress)local;
                        EpollDatagramChannel.this.doBind(localAddress);
                    }
                    AbstractEpollChannel.checkResolvable(remoteAddress);
                    EpollDatagramChannel.this.remote = remoteAddress;
                    EpollDatagramChannel.this.local = Native.localAddress(EpollDatagramChannel.this.fd().intValue());
                    success = true;
                    if (!wasActive && EpollDatagramChannel.this.isActive()) {
                        EpollDatagramChannel.this.pipeline().fireChannelActive();
                    }
                }
                finally {
                    if (!success) {
                        EpollDatagramChannel.this.doClose();
                    }
                    else {
                        channelPromise.setSuccess();
                        EpollDatagramChannel.this.connected = true;
                    }
                }
            }
            catch (Throwable cause) {
                channelPromise.setFailure(cause);
            }
        }
        
        @Override
        void epollInReady() {
            assert EpollDatagramChannel.this.eventLoop().inEventLoop();
            final DatagramChannelConfig config = EpollDatagramChannel.this.config();
            final boolean edgeTriggered = EpollDatagramChannel.this.isFlagSet(Native.EPOLLET);
            if (!this.readPending && !edgeTriggered && !config.isAutoRead()) {
                this.clearEpollIn0();
                return;
            }
            final RecvByteBufAllocator.Handle allocHandle = EpollDatagramChannel.this.unsafe().recvBufAllocHandle();
            final ChannelPipeline pipeline = EpollDatagramChannel.this.pipeline();
            Throwable exception = null;
            try {
                final int maxMessagesPerRead = edgeTriggered ? Integer.MAX_VALUE : config.getMaxMessagesPerRead();
                int messages = 0;
                do {
                    ByteBuf data = null;
                    try {
                        data = allocHandle.allocate(config.getAllocator());
                        final int writerIndex = data.writerIndex();
                        DatagramSocketAddress remoteAddress;
                        if (data.hasMemoryAddress()) {
                            remoteAddress = Native.recvFromAddress(EpollDatagramChannel.this.fd().intValue(), data.memoryAddress(), writerIndex, data.capacity());
                        }
                        else {
                            final ByteBuffer nioData = data.internalNioBuffer(writerIndex, data.writableBytes());
                            remoteAddress = Native.recvFrom(EpollDatagramChannel.this.fd().intValue(), nioData, nioData.position(), nioData.limit());
                        }
                        if (remoteAddress == null) {}
                        final int readBytes = remoteAddress.receivedAmount;
                        data.writerIndex(data.writerIndex() + readBytes);
                        allocHandle.record(readBytes);
                        this.readPending = false;
                        this.readBuf.add(new DatagramPacket(data, (InetSocketAddress)this.localAddress(), remoteAddress));
                        data = null;
                    }
                    catch (Throwable t) {
                        exception = t;
                    }
                    finally {
                        if (data != null) {
                            data.release();
                        }
                        if (!edgeTriggered && !config.isAutoRead()) {
                            break;
                        }
                    }
                } while (++messages < maxMessagesPerRead);
                for (int size = this.readBuf.size(), i = 0; i < size; ++i) {
                    pipeline.fireChannelRead(this.readBuf.get(i));
                }
                this.readBuf.clear();
                pipeline.fireChannelReadComplete();
                if (exception != null) {
                    pipeline.fireExceptionCaught(exception);
                }
            }
            finally {
                if (!this.readPending && !config.isAutoRead()) {
                    EpollDatagramChannel.this.clearEpollIn();
                }
            }
        }
    }
    
    static final class DatagramSocketAddress extends InetSocketAddress
    {
        private static final long serialVersionUID = 1348596211215015739L;
        final int receivedAmount;
        
        DatagramSocketAddress(final String addr, final int port, final int receivedAmount) {
            super(addr, port);
            this.receivedAmount = receivedAmount;
        }
    }
}
