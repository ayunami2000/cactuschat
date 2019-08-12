// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.io.EOFException;
import java.nio.ByteBuffer;
import java.net.SocketException;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.DatagramChannel;
import java.security.SecureRandom;

final class UDPClient extends Client
{
    private static final int EPHEMERAL_START = 1024;
    private static final int EPHEMERAL_STOP = 65535;
    private static final int EPHEMERAL_RANGE = 64511;
    private static SecureRandom prng;
    private static volatile boolean prng_initializing;
    private boolean bound;
    
    public UDPClient(final long endTime) throws IOException {
        super(DatagramChannel.open(), endTime);
        this.bound = false;
    }
    
    private void bind_random(final InetSocketAddress addr) throws IOException {
        if (UDPClient.prng_initializing) {
            try {
                Thread.sleep(2L);
            }
            catch (InterruptedException ex) {}
            if (UDPClient.prng_initializing) {
                return;
            }
        }
        final DatagramChannel channel = (DatagramChannel)this.key.channel();
        int i = 0;
        while (i < 1024) {
            try {
                final int port = UDPClient.prng.nextInt(64511) + 1024;
                InetSocketAddress temp;
                if (addr != null) {
                    temp = new InetSocketAddress(addr.getAddress(), port);
                }
                else {
                    temp = new InetSocketAddress(port);
                }
                channel.socket().bind(temp);
                this.bound = true;
                return;
            }
            catch (SocketException e) {
                ++i;
                continue;
            }
            break;
        }
    }
    
    void bind(final SocketAddress addr) throws IOException {
        if (addr == null || (addr instanceof InetSocketAddress && ((InetSocketAddress)addr).getPort() == 0)) {
            this.bind_random((InetSocketAddress)addr);
            if (this.bound) {
                return;
            }
        }
        if (addr != null) {
            final DatagramChannel channel = (DatagramChannel)this.key.channel();
            channel.socket().bind(addr);
            this.bound = true;
        }
    }
    
    void connect(final SocketAddress addr) throws IOException {
        if (!this.bound) {
            this.bind(null);
        }
        final DatagramChannel channel = (DatagramChannel)this.key.channel();
        channel.connect(addr);
    }
    
    void send(final byte[] data) throws IOException {
        final DatagramChannel channel = (DatagramChannel)this.key.channel();
        Client.verboseLog("UDP write", data);
        channel.write(ByteBuffer.wrap(data));
    }
    
    byte[] recv(final int max) throws IOException {
        final DatagramChannel channel = (DatagramChannel)this.key.channel();
        final byte[] temp = new byte[max];
        this.key.interestOps(1);
        try {
            while (!this.key.isReadable()) {
                Client.blockUntil(this.key, this.endTime);
            }
        }
        finally {
            if (this.key.isValid()) {
                this.key.interestOps(0);
            }
        }
        final long ret = channel.read(ByteBuffer.wrap(temp));
        if (ret <= 0L) {
            throw new EOFException();
        }
        final int len = (int)ret;
        final byte[] data = new byte[len];
        System.arraycopy(temp, 0, data, 0, len);
        Client.verboseLog("UDP read", data);
        return data;
    }
    
    static byte[] sendrecv(final SocketAddress local, final SocketAddress remote, final byte[] data, final int max, final long endTime) throws IOException {
        final UDPClient client = new UDPClient(endTime);
        try {
            client.bind(local);
            client.connect(remote);
            client.send(data);
            return client.recv(max);
        }
        finally {
            client.cleanup();
        }
    }
    
    static byte[] sendrecv(final SocketAddress addr, final byte[] data, final int max, final long endTime) throws IOException {
        return sendrecv(null, addr, data, max, endTime);
    }
    
    static {
        UDPClient.prng = new SecureRandom();
        UDPClient.prng_initializing = true;
        new Thread(new Runnable() {
            public void run() {
                final int n = UDPClient.prng.nextInt();
                UDPClient.prng_initializing = false;
            }
        }).start();
    }
}
