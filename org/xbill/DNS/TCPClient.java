// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.net.SocketTimeoutException;
import java.io.EOFException;
import java.nio.ByteBuffer;
import java.net.SocketAddress;
import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;

final class TCPClient extends Client
{
    public TCPClient(final long endTime) throws IOException {
        super(SocketChannel.open(), endTime);
    }
    
    void bind(final SocketAddress addr) throws IOException {
        final SocketChannel channel = (SocketChannel)this.key.channel();
        channel.socket().bind(addr);
    }
    
    void connect(final SocketAddress addr) throws IOException {
        final SocketChannel channel = (SocketChannel)this.key.channel();
        if (channel.connect(addr)) {
            return;
        }
        this.key.interestOps(8);
        try {
            while (!channel.finishConnect()) {
                if (!this.key.isConnectable()) {
                    Client.blockUntil(this.key, this.endTime);
                }
            }
        }
        finally {
            if (this.key.isValid()) {
                this.key.interestOps(0);
            }
        }
    }
    
    void send(final byte[] data) throws IOException {
        final SocketChannel channel = (SocketChannel)this.key.channel();
        Client.verboseLog("TCP write", data);
        final byte[] lengthArray = { (byte)(data.length >>> 8), (byte)(data.length & 0xFF) };
        final ByteBuffer[] buffers = { ByteBuffer.wrap(lengthArray), ByteBuffer.wrap(data) };
        int nsent = 0;
        this.key.interestOps(4);
        try {
            while (nsent < data.length + 2) {
                if (this.key.isWritable()) {
                    final long n = channel.write(buffers);
                    if (n < 0L) {
                        throw new EOFException();
                    }
                    nsent += (int)n;
                    if (nsent < data.length + 2 && System.currentTimeMillis() > this.endTime) {
                        throw new SocketTimeoutException();
                    }
                    continue;
                }
                else {
                    Client.blockUntil(this.key, this.endTime);
                }
            }
        }
        finally {
            if (this.key.isValid()) {
                this.key.interestOps(0);
            }
        }
    }
    
    private byte[] _recv(final int length) throws IOException {
        final SocketChannel channel = (SocketChannel)this.key.channel();
        int nrecvd = 0;
        final byte[] data = new byte[length];
        final ByteBuffer buffer = ByteBuffer.wrap(data);
        this.key.interestOps(1);
        try {
            while (nrecvd < length) {
                if (this.key.isReadable()) {
                    final long n = channel.read(buffer);
                    if (n < 0L) {
                        throw new EOFException();
                    }
                    nrecvd += (int)n;
                    if (nrecvd < length && System.currentTimeMillis() > this.endTime) {
                        throw new SocketTimeoutException();
                    }
                    continue;
                }
                else {
                    Client.blockUntil(this.key, this.endTime);
                }
            }
        }
        finally {
            if (this.key.isValid()) {
                this.key.interestOps(0);
            }
        }
        return data;
    }
    
    byte[] recv() throws IOException {
        final byte[] buf = this._recv(2);
        final int length = ((buf[0] & 0xFF) << 8) + (buf[1] & 0xFF);
        final byte[] data = this._recv(length);
        Client.verboseLog("TCP read", data);
        return data;
    }
    
    static byte[] sendrecv(final SocketAddress local, final SocketAddress remote, final byte[] data, final long endTime) throws IOException {
        final TCPClient client = new TCPClient(endTime);
        try {
            if (local != null) {
                client.bind(local);
            }
            client.connect(remote);
            client.send(data);
            return client.recv();
        }
        finally {
            client.cleanup();
        }
    }
    
    static byte[] sendrecv(final SocketAddress addr, final byte[] data, final long endTime) throws IOException {
        return sendrecv(null, addr, data, endTime);
    }
}
