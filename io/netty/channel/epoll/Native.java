// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.channel.epoll;

import io.netty.util.internal.NativeLibraryLoader;
import io.netty.util.internal.PlatformDependent;
import java.util.Locale;
import io.netty.util.internal.SystemPropertyUtil;
import java.net.UnknownHostException;
import io.netty.channel.unix.DomainSocketAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import io.netty.channel.ChannelException;
import java.net.Inet6Address;
import java.net.InetAddress;
import io.netty.channel.DefaultFileRegion;
import java.nio.ByteBuffer;
import io.netty.util.internal.EmptyArrays;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;

final class Native
{
    public static final int EPOLLIN;
    public static final int EPOLLOUT;
    public static final int EPOLLRDHUP;
    public static final int EPOLLET;
    public static final int IOV_MAX;
    public static final int UIO_MAX_IOV;
    public static final boolean IS_SUPPORTING_SENDMMSG;
    private static final byte[] IPV4_MAPPED_IPV6_PREFIX;
    private static final int ERRNO_EBADF_NEGATIVE;
    private static final int ERRNO_EPIPE_NEGATIVE;
    private static final int ERRNO_ECONNRESET_NEGATIVE;
    private static final int ERRNO_EAGAIN_NEGATIVE;
    private static final int ERRNO_EWOULDBLOCK_NEGATIVE;
    private static final int ERRNO_EINPROGRESS_NEGATIVE;
    private static final String[] ERRORS;
    private static final ClosedChannelException CLOSED_CHANNEL_EXCEPTION;
    private static final IOException CONNECTION_RESET_EXCEPTION_WRITE;
    private static final IOException CONNECTION_RESET_EXCEPTION_WRITEV;
    private static final IOException CONNECTION_RESET_EXCEPTION_READ;
    private static final IOException CONNECTION_RESET_EXCEPTION_SENDFILE;
    private static final IOException CONNECTION_RESET_EXCEPTION_SENDTO;
    private static final IOException CONNECTION_RESET_EXCEPTION_SENDMSG;
    private static final IOException CONNECTION_RESET_EXCEPTION_SENDMMSG;
    
    private static IOException newConnectionResetException(final String method, final int errnoNegative) {
        final IOException exception = newIOException(method, errnoNegative);
        exception.setStackTrace(EmptyArrays.EMPTY_STACK_TRACE);
        return exception;
    }
    
    private static IOException newIOException(final String method, final int err) {
        return new IOException(method + "() failed: " + Native.ERRORS[-err]);
    }
    
    private static int ioResult(final String method, final int err, final IOException resetCause) throws IOException {
        if (err == Native.ERRNO_EAGAIN_NEGATIVE || err == Native.ERRNO_EWOULDBLOCK_NEGATIVE) {
            return 0;
        }
        if (err == Native.ERRNO_EPIPE_NEGATIVE || err == Native.ERRNO_ECONNRESET_NEGATIVE) {
            throw resetCause;
        }
        if (err == Native.ERRNO_EBADF_NEGATIVE) {
            throw Native.CLOSED_CHANNEL_EXCEPTION;
        }
        throw newIOException(method, err);
    }
    
    public static native int eventFd();
    
    public static native void eventFdWrite(final int p0, final long p1);
    
    public static native void eventFdRead(final int p0);
    
    public static native int epollCreate();
    
    public static int epollWait(final int efd, final EpollEventArray events, final int timeout) throws IOException {
        final int ready = epollWait0(efd, events.memoryAddress(), events.length(), timeout);
        if (ready < 0) {
            throw newIOException("epoll_wait", ready);
        }
        return ready;
    }
    
    private static native int epollWait0(final int p0, final long p1, final int p2, final int p3);
    
    public static native void epollCtlAdd(final int p0, final int p1, final int p2);
    
    public static native void epollCtlMod(final int p0, final int p1, final int p2);
    
    public static native void epollCtlDel(final int p0, final int p1);
    
    private static native int errnoEBADF();
    
    private static native int errnoEPIPE();
    
    private static native int errnoECONNRESET();
    
    private static native int errnoEAGAIN();
    
    private static native int errnoEWOULDBLOCK();
    
    private static native int errnoEINPROGRESS();
    
    private static native String strError(final int p0);
    
    public static void close(final int fd) throws IOException {
        final int res = close0(fd);
        if (res < 0) {
            throw newIOException("close", res);
        }
    }
    
    private static native int close0(final int p0);
    
    public static int write(final int fd, final ByteBuffer buf, final int pos, final int limit) throws IOException {
        final int res = write0(fd, buf, pos, limit);
        if (res >= 0) {
            return res;
        }
        return ioResult("write", res, Native.CONNECTION_RESET_EXCEPTION_WRITE);
    }
    
    private static native int write0(final int p0, final ByteBuffer p1, final int p2, final int p3);
    
    public static int writeAddress(final int fd, final long address, final int pos, final int limit) throws IOException {
        final int res = writeAddress0(fd, address, pos, limit);
        if (res >= 0) {
            return res;
        }
        return ioResult("writeAddress", res, Native.CONNECTION_RESET_EXCEPTION_WRITE);
    }
    
    private static native int writeAddress0(final int p0, final long p1, final int p2, final int p3);
    
    public static long writev(final int fd, final ByteBuffer[] buffers, final int offset, final int length) throws IOException {
        final long res = writev0(fd, buffers, offset, length);
        if (res >= 0L) {
            return res;
        }
        return ioResult("writev", (int)res, Native.CONNECTION_RESET_EXCEPTION_WRITEV);
    }
    
    private static native long writev0(final int p0, final ByteBuffer[] p1, final int p2, final int p3);
    
    public static long writevAddresses(final int fd, final long memoryAddress, final int length) throws IOException {
        final long res = writevAddresses0(fd, memoryAddress, length);
        if (res >= 0L) {
            return res;
        }
        return ioResult("writevAddresses", (int)res, Native.CONNECTION_RESET_EXCEPTION_WRITEV);
    }
    
    private static native long writevAddresses0(final int p0, final long p1, final int p2);
    
    public static int read(final int fd, final ByteBuffer buf, final int pos, final int limit) throws IOException {
        final int res = read0(fd, buf, pos, limit);
        if (res > 0) {
            return res;
        }
        if (res == 0) {
            return -1;
        }
        return ioResult("read", res, Native.CONNECTION_RESET_EXCEPTION_READ);
    }
    
    private static native int read0(final int p0, final ByteBuffer p1, final int p2, final int p3);
    
    public static int readAddress(final int fd, final long address, final int pos, final int limit) throws IOException {
        final int res = readAddress0(fd, address, pos, limit);
        if (res > 0) {
            return res;
        }
        if (res == 0) {
            return -1;
        }
        return ioResult("readAddress", res, Native.CONNECTION_RESET_EXCEPTION_READ);
    }
    
    private static native int readAddress0(final int p0, final long p1, final int p2, final int p3);
    
    public static long sendfile(final int dest, final DefaultFileRegion src, final long baseOffset, final long offset, final long length) throws IOException {
        src.open();
        final long res = sendfile0(dest, src, baseOffset, offset, length);
        if (res >= 0L) {
            return res;
        }
        return ioResult("sendfile", (int)res, Native.CONNECTION_RESET_EXCEPTION_SENDFILE);
    }
    
    private static native long sendfile0(final int p0, final DefaultFileRegion p1, final long p2, final long p3, final long p4) throws IOException;
    
    public static int sendTo(final int fd, final ByteBuffer buf, final int pos, final int limit, final InetAddress addr, final int port) throws IOException {
        byte[] address;
        int scopeId;
        if (addr instanceof Inet6Address) {
            address = addr.getAddress();
            scopeId = ((Inet6Address)addr).getScopeId();
        }
        else {
            scopeId = 0;
            address = ipv4MappedIpv6Address(addr.getAddress());
        }
        final int res = sendTo0(fd, buf, pos, limit, address, scopeId, port);
        if (res >= 0) {
            return res;
        }
        return ioResult("sendTo", res, Native.CONNECTION_RESET_EXCEPTION_SENDTO);
    }
    
    private static native int sendTo0(final int p0, final ByteBuffer p1, final int p2, final int p3, final byte[] p4, final int p5, final int p6);
    
    public static int sendToAddress(final int fd, final long memoryAddress, final int pos, final int limit, final InetAddress addr, final int port) throws IOException {
        byte[] address;
        int scopeId;
        if (addr instanceof Inet6Address) {
            address = addr.getAddress();
            scopeId = ((Inet6Address)addr).getScopeId();
        }
        else {
            scopeId = 0;
            address = ipv4MappedIpv6Address(addr.getAddress());
        }
        final int res = sendToAddress0(fd, memoryAddress, pos, limit, address, scopeId, port);
        if (res >= 0) {
            return res;
        }
        return ioResult("sendToAddress", res, Native.CONNECTION_RESET_EXCEPTION_SENDTO);
    }
    
    private static native int sendToAddress0(final int p0, final long p1, final int p2, final int p3, final byte[] p4, final int p5, final int p6);
    
    public static int sendToAddresses(final int fd, final long memoryAddress, final int length, final InetAddress addr, final int port) throws IOException {
        byte[] address;
        int scopeId;
        if (addr instanceof Inet6Address) {
            address = addr.getAddress();
            scopeId = ((Inet6Address)addr).getScopeId();
        }
        else {
            scopeId = 0;
            address = ipv4MappedIpv6Address(addr.getAddress());
        }
        final int res = sendToAddresses(fd, memoryAddress, length, address, scopeId, port);
        if (res >= 0) {
            return res;
        }
        return ioResult("sendToAddresses", res, Native.CONNECTION_RESET_EXCEPTION_SENDMSG);
    }
    
    private static native int sendToAddresses(final int p0, final long p1, final int p2, final byte[] p3, final int p4, final int p5);
    
    public static native EpollDatagramChannel.DatagramSocketAddress recvFrom(final int p0, final ByteBuffer p1, final int p2, final int p3) throws IOException;
    
    public static native EpollDatagramChannel.DatagramSocketAddress recvFromAddress(final int p0, final long p1, final int p2, final int p3) throws IOException;
    
    public static int sendmmsg(final int fd, final NativeDatagramPacketArray.NativeDatagramPacket[] msgs, final int offset, final int len) throws IOException {
        final int res = sendmmsg0(fd, msgs, offset, len);
        if (res >= 0) {
            return res;
        }
        return ioResult("sendmmsg", res, Native.CONNECTION_RESET_EXCEPTION_SENDMMSG);
    }
    
    private static native int sendmmsg0(final int p0, final NativeDatagramPacketArray.NativeDatagramPacket[] p1, final int p2, final int p3);
    
    private static native boolean isSupportingSendmmsg();
    
    public static int socketStreamFd() {
        final int res = socketStream();
        if (res < 0) {
            throw new ChannelException(newIOException("socketStreamFd", res));
        }
        return res;
    }
    
    public static int socketDgramFd() {
        final int res = socketDgram();
        if (res < 0) {
            throw new ChannelException(newIOException("socketDgramFd", res));
        }
        return res;
    }
    
    public static int socketDomainFd() {
        final int res = socketDomain();
        if (res < 0) {
            throw new ChannelException(newIOException("socketDomain", res));
        }
        return res;
    }
    
    private static native int socketStream();
    
    private static native int socketDgram();
    
    private static native int socketDomain();
    
    public static void bind(final int fd, final SocketAddress socketAddress) throws IOException {
        if (socketAddress instanceof InetSocketAddress) {
            final InetSocketAddress addr = (InetSocketAddress)socketAddress;
            final NativeInetAddress address = toNativeInetAddress(addr.getAddress());
            final int res = bind(fd, address.address, address.scopeId, addr.getPort());
            if (res < 0) {
                throw newIOException("bind", res);
            }
        }
        else {
            if (!(socketAddress instanceof DomainSocketAddress)) {
                throw new Error("Unexpected SocketAddress implementation " + socketAddress);
            }
            final DomainSocketAddress addr2 = (DomainSocketAddress)socketAddress;
            final int res2 = bindDomainSocket(fd, addr2.path());
            if (res2 < 0) {
                throw newIOException("bind", res2);
            }
        }
    }
    
    private static native int bind(final int p0, final byte[] p1, final int p2, final int p3);
    
    private static native int bindDomainSocket(final int p0, final String p1);
    
    public static void listen(final int fd, final int backlog) throws IOException {
        final int res = listen0(fd, backlog);
        if (res < 0) {
            throw newIOException("listen", res);
        }
    }
    
    private static native int listen0(final int p0, final int p1);
    
    public static boolean connect(final int fd, final SocketAddress socketAddress) throws IOException {
        int res;
        if (socketAddress instanceof InetSocketAddress) {
            final InetSocketAddress inetSocketAddress = (InetSocketAddress)socketAddress;
            final NativeInetAddress address = toNativeInetAddress(inetSocketAddress.getAddress());
            res = connect(fd, address.address, address.scopeId, inetSocketAddress.getPort());
        }
        else {
            if (!(socketAddress instanceof DomainSocketAddress)) {
                throw new Error("Unexpected SocketAddress implementation " + socketAddress);
            }
            final DomainSocketAddress unixDomainSocketAddress = (DomainSocketAddress)socketAddress;
            res = connectDomainSocket(fd, unixDomainSocketAddress.path());
        }
        if (res >= 0) {
            return true;
        }
        if (res == Native.ERRNO_EINPROGRESS_NEGATIVE) {
            return false;
        }
        throw newIOException("connect", res);
    }
    
    private static native int connect(final int p0, final byte[] p1, final int p2, final int p3);
    
    private static native int connectDomainSocket(final int p0, final String p1);
    
    public static boolean finishConnect(final int fd) throws IOException {
        final int res = finishConnect0(fd);
        if (res >= 0) {
            return true;
        }
        if (res == Native.ERRNO_EINPROGRESS_NEGATIVE) {
            return false;
        }
        throw newIOException("finishConnect", res);
    }
    
    private static native int finishConnect0(final int p0);
    
    public static InetSocketAddress remoteAddress(final int fd) {
        final byte[] addr = remoteAddress0(fd);
        if (addr == null) {
            return null;
        }
        return address(addr, 0, addr.length);
    }
    
    public static InetSocketAddress localAddress(final int fd) {
        final byte[] addr = localAddress0(fd);
        if (addr == null) {
            return null;
        }
        return address(addr, 0, addr.length);
    }
    
    static InetSocketAddress address(final byte[] addr, final int offset, final int len) {
        final int port = decodeInt(addr, offset + len - 4);
        try {
            InetAddress address = null;
            switch (len) {
                case 8: {
                    final byte[] ipv4 = new byte[4];
                    System.arraycopy(addr, offset, ipv4, 0, 4);
                    address = InetAddress.getByAddress(ipv4);
                    break;
                }
                case 24: {
                    final byte[] ipv5 = new byte[16];
                    System.arraycopy(addr, offset, ipv5, 0, 16);
                    final int scopeId = decodeInt(addr, offset + len - 8);
                    address = Inet6Address.getByAddress(null, ipv5, scopeId);
                    break;
                }
                default: {
                    throw new Error();
                }
            }
            return new InetSocketAddress(address, port);
        }
        catch (UnknownHostException e) {
            throw new Error("Should never happen", e);
        }
    }
    
    static int decodeInt(final byte[] addr, final int index) {
        return (addr[index] & 0xFF) << 24 | (addr[index + 1] & 0xFF) << 16 | (addr[index + 2] & 0xFF) << 8 | (addr[index + 3] & 0xFF);
    }
    
    private static native byte[] remoteAddress0(final int p0);
    
    private static native byte[] localAddress0(final int p0);
    
    public static int accept(final int fd, final byte[] addr) throws IOException {
        final int res = accept0(fd, addr);
        if (res >= 0) {
            return res;
        }
        if (res == Native.ERRNO_EAGAIN_NEGATIVE || res == Native.ERRNO_EWOULDBLOCK_NEGATIVE) {
            return -1;
        }
        throw newIOException("accept", res);
    }
    
    private static native int accept0(final int p0, final byte[] p1);
    
    public static int recvFd(final int fd) throws IOException {
        final int res = recvFd0(fd);
        if (res > 0) {
            return res;
        }
        if (res == 0) {
            return -1;
        }
        if (res == Native.ERRNO_EAGAIN_NEGATIVE || res == Native.ERRNO_EWOULDBLOCK_NEGATIVE) {
            return 0;
        }
        throw newIOException("recvFd", res);
    }
    
    private static native int recvFd0(final int p0);
    
    public static int sendFd(final int socketFd, final int fd) throws IOException {
        final int res = sendFd0(socketFd, fd);
        if (res >= 0) {
            return res;
        }
        if (res == Native.ERRNO_EAGAIN_NEGATIVE || res == Native.ERRNO_EWOULDBLOCK_NEGATIVE) {
            return -1;
        }
        throw newIOException("sendFd", res);
    }
    
    private static native int sendFd0(final int p0, final int p1);
    
    public static void shutdown(final int fd, final boolean read, final boolean write) throws IOException {
        final int res = shutdown0(fd, read, write);
        if (res < 0) {
            throw newIOException("shutdown", res);
        }
    }
    
    private static native int shutdown0(final int p0, final boolean p1, final boolean p2);
    
    public static native int getReceiveBufferSize(final int p0);
    
    public static native int getSendBufferSize(final int p0);
    
    public static native int isKeepAlive(final int p0);
    
    public static native int isReuseAddress(final int p0);
    
    public static native int isReusePort(final int p0);
    
    public static native int isTcpNoDelay(final int p0);
    
    public static native int isTcpCork(final int p0);
    
    public static native int getSoLinger(final int p0);
    
    public static native int getTrafficClass(final int p0);
    
    public static native int isBroadcast(final int p0);
    
    public static native int getTcpKeepIdle(final int p0);
    
    public static native int getTcpKeepIntvl(final int p0);
    
    public static native int getTcpKeepCnt(final int p0);
    
    public static native int getSoError(final int p0);
    
    public static native void setKeepAlive(final int p0, final int p1);
    
    public static native void setReceiveBufferSize(final int p0, final int p1);
    
    public static native void setReuseAddress(final int p0, final int p1);
    
    public static native void setReusePort(final int p0, final int p1);
    
    public static native void setSendBufferSize(final int p0, final int p1);
    
    public static native void setTcpNoDelay(final int p0, final int p1);
    
    public static native void setTcpCork(final int p0, final int p1);
    
    public static native void setSoLinger(final int p0, final int p1);
    
    public static native void setTrafficClass(final int p0, final int p1);
    
    public static native void setBroadcast(final int p0, final int p1);
    
    public static native void setTcpKeepIdle(final int p0, final int p1);
    
    public static native void setTcpKeepIntvl(final int p0, final int p1);
    
    public static native void setTcpKeepCnt(final int p0, final int p1);
    
    public static void tcpInfo(final int fd, final EpollTcpInfo info) {
        tcpInfo0(fd, info.info);
    }
    
    private static native void tcpInfo0(final int p0, final int[] p1);
    
    private static NativeInetAddress toNativeInetAddress(final InetAddress addr) {
        final byte[] bytes = addr.getAddress();
        if (addr instanceof Inet6Address) {
            return new NativeInetAddress(bytes, ((Inet6Address)addr).getScopeId());
        }
        return new NativeInetAddress(ipv4MappedIpv6Address(bytes));
    }
    
    static byte[] ipv4MappedIpv6Address(final byte[] ipv4) {
        final byte[] address = new byte[16];
        System.arraycopy(Native.IPV4_MAPPED_IPV6_PREFIX, 0, address, 0, Native.IPV4_MAPPED_IPV6_PREFIX.length);
        System.arraycopy(ipv4, 0, address, 12, ipv4.length);
        return address;
    }
    
    public static native String kernelVersion();
    
    private static native int iovMax();
    
    private static native int uioMaxIov();
    
    public static native int sizeofEpollEvent();
    
    public static native int offsetofEpollData();
    
    private static native int epollin();
    
    private static native int epollout();
    
    private static native int epollrdhup();
    
    private static native int epollet();
    
    private Native() {
    }
    
    static {
        final String name = SystemPropertyUtil.get("os.name").toLowerCase(Locale.UK).trim();
        if (!name.startsWith("linux")) {
            throw new IllegalStateException("Only supported on Linux");
        }
        NativeLibraryLoader.load("netty-transport-native-epoll", PlatformDependent.getClassLoader(Native.class));
        EPOLLIN = epollin();
        EPOLLOUT = epollout();
        EPOLLRDHUP = epollrdhup();
        EPOLLET = epollet();
        IOV_MAX = iovMax();
        UIO_MAX_IOV = uioMaxIov();
        IS_SUPPORTING_SENDMMSG = isSupportingSendmmsg();
        IPV4_MAPPED_IPV6_PREFIX = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1 };
        ERRNO_EBADF_NEGATIVE = -errnoEBADF();
        ERRNO_EPIPE_NEGATIVE = -errnoEPIPE();
        ERRNO_ECONNRESET_NEGATIVE = -errnoECONNRESET();
        ERRNO_EAGAIN_NEGATIVE = -errnoEAGAIN();
        ERRNO_EWOULDBLOCK_NEGATIVE = -errnoEWOULDBLOCK();
        ERRNO_EINPROGRESS_NEGATIVE = -errnoEINPROGRESS();
        ERRORS = new String[1024];
        for (int i = 0; i < Native.ERRORS.length; ++i) {
            Native.ERRORS[i] = strError(i);
        }
        CONNECTION_RESET_EXCEPTION_READ = newConnectionResetException("syscall:read(...)", Native.ERRNO_ECONNRESET_NEGATIVE);
        CONNECTION_RESET_EXCEPTION_WRITE = newConnectionResetException("syscall:write(...)", Native.ERRNO_EPIPE_NEGATIVE);
        CONNECTION_RESET_EXCEPTION_WRITEV = newConnectionResetException("syscall:writev(...)", Native.ERRNO_EPIPE_NEGATIVE);
        CONNECTION_RESET_EXCEPTION_SENDFILE = newConnectionResetException("syscall:sendfile(...)", Native.ERRNO_EPIPE_NEGATIVE);
        CONNECTION_RESET_EXCEPTION_SENDTO = newConnectionResetException("syscall:sendto(...)", Native.ERRNO_EPIPE_NEGATIVE);
        CONNECTION_RESET_EXCEPTION_SENDMSG = newConnectionResetException("syscall:sendmsg(...)", Native.ERRNO_EPIPE_NEGATIVE);
        CONNECTION_RESET_EXCEPTION_SENDMMSG = newConnectionResetException("syscall:sendmmsg(...)", Native.ERRNO_EPIPE_NEGATIVE);
        (CLOSED_CHANNEL_EXCEPTION = new ClosedChannelException()).setStackTrace(EmptyArrays.EMPTY_STACK_TRACE);
    }
    
    private static class NativeInetAddress
    {
        final byte[] address;
        final int scopeId;
        
        NativeInetAddress(final byte[] address, final int scopeId) {
            this.address = address;
            this.scopeId = scopeId;
        }
        
        NativeInetAddress(final byte[] address) {
            this(address, 0);
        }
    }
}
