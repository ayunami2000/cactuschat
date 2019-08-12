// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.net;

import java.io.ByteArrayOutputStream;
import java.net.UnknownHostException;
import java.net.ConnectException;
import java.util.concurrent.CountDownLatch;
import org.apache.logging.log4j.Logger;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.appender.ManagerFactory;
import org.apache.logging.log4j.util.Strings;
import org.apache.logging.log4j.core.appender.OutputStreamManager;
import java.io.Serializable;
import org.apache.logging.log4j.core.Layout;
import java.net.InetAddress;
import java.io.OutputStream;
import java.net.Socket;

public class TcpSocketManager extends AbstractSocketManager
{
    public static final int DEFAULT_RECONNECTION_DELAY_MILLIS = 30000;
    private static final int DEFAULT_PORT = 4560;
    private static final TcpSocketManagerFactory FACTORY;
    private final int reconnectionDelay;
    private Reconnector connector;
    private Socket socket;
    private final boolean retry;
    private final boolean immediateFail;
    private final int connectTimeoutMillis;
    
    public TcpSocketManager(final String name, final OutputStream os, final Socket sock, final InetAddress inetAddress, final String host, final int port, final int connectTimeoutMillis, final int delay, final boolean immediateFail, final Layout<? extends Serializable> layout) {
        super(name, os, inetAddress, host, port, layout, true);
        this.connectTimeoutMillis = connectTimeoutMillis;
        this.reconnectionDelay = delay;
        this.socket = sock;
        this.immediateFail = immediateFail;
        this.retry = (delay > 0);
        if (sock == null) {
            (this.connector = new Reconnector(this)).setDaemon(true);
            this.connector.setPriority(1);
            this.connector.start();
        }
    }
    
    public static TcpSocketManager getSocketManager(final String host, int port, final int connectTimeoutMillis, int delayMillis, final boolean immediateFail, final Layout<? extends Serializable> layout) {
        if (Strings.isEmpty(host)) {
            throw new IllegalArgumentException("A host name is required");
        }
        if (port <= 0) {
            port = 4560;
        }
        if (delayMillis == 0) {
            delayMillis = 30000;
        }
        return (TcpSocketManager)OutputStreamManager.getManager("TCP:" + host + ':' + port, new FactoryData(host, port, connectTimeoutMillis, delayMillis, immediateFail, layout), TcpSocketManager.FACTORY);
    }
    
    @Override
    protected void write(final byte[] bytes, final int offset, final int length) {
        if (this.socket == null) {
            if (this.connector != null && !this.immediateFail) {
                this.connector.latch();
            }
            if (this.socket == null) {
                final String msg = "Error writing to " + this.getName() + " socket not available";
                throw new AppenderLoggingException(msg);
            }
        }
        synchronized (this) {
            try {
                this.getOutputStream().write(bytes, offset, length);
            }
            catch (IOException ex) {
                if (this.retry && this.connector == null) {
                    (this.connector = new Reconnector(this)).setDaemon(true);
                    this.connector.setPriority(1);
                    this.connector.start();
                }
                final String msg2 = "Error writing to " + this.getName();
                throw new AppenderLoggingException(msg2, ex);
            }
        }
    }
    
    @Override
    protected synchronized void close() {
        super.close();
        if (this.connector != null) {
            this.connector.shutdown();
            this.connector.interrupt();
            this.connector = null;
        }
    }
    
    public int getConnectTimeoutMillis() {
        return this.connectTimeoutMillis;
    }
    
    @Override
    public Map<String, String> getContentFormat() {
        final Map<String, String> result = new HashMap<String, String>(super.getContentFormat());
        result.put("protocol", "tcp");
        result.put("direction", "out");
        return result;
    }
    
    protected Socket createSocket(final InetAddress host, final int port) throws IOException {
        return this.createSocket(host.getHostName(), port);
    }
    
    protected Socket createSocket(final String host, final int port) throws IOException {
        final InetSocketAddress address = new InetSocketAddress(host, port);
        final Socket newSocket = new Socket();
        newSocket.connect(address, this.connectTimeoutMillis);
        return newSocket;
    }
    
    static {
        FACTORY = new TcpSocketManagerFactory();
    }
    
    private class Reconnector extends Thread
    {
        private final CountDownLatch latch;
        private boolean shutdown;
        private final Object owner;
        
        public Reconnector(final OutputStreamManager owner) {
            this.latch = new CountDownLatch(1);
            this.shutdown = false;
            this.owner = owner;
        }
        
        public void latch() {
            try {
                this.latch.await();
            }
            catch (InterruptedException ex) {}
        }
        
        public void shutdown() {
            this.shutdown = true;
        }
        
        @Override
        public void run() {
            while (!this.shutdown) {
                try {
                    Thread.sleep(TcpSocketManager.this.reconnectionDelay);
                    final Socket sock = TcpSocketManager.this.createSocket(TcpSocketManager.this.inetAddress, TcpSocketManager.this.port);
                    final OutputStream newOS = sock.getOutputStream();
                    synchronized (this.owner) {
                        try {
                            OutputStreamManager.this.getOutputStream().close();
                        }
                        catch (IOException ex2) {}
                        OutputStreamManager.this.setOutputStream(newOS);
                        TcpSocketManager.this.socket = sock;
                        TcpSocketManager.this.connector = null;
                        this.shutdown = true;
                    }
                    TcpSocketManager.LOGGER.debug("Connection to " + TcpSocketManager.this.host + ':' + TcpSocketManager.this.port + " reestablished.");
                }
                catch (InterruptedException ie) {
                    TcpSocketManager.LOGGER.debug("Reconnection interrupted.");
                }
                catch (ConnectException ex) {
                    TcpSocketManager.LOGGER.debug(TcpSocketManager.this.host + ':' + TcpSocketManager.this.port + " refused connection");
                }
                catch (IOException ioe) {
                    TcpSocketManager.LOGGER.debug("Unable to reconnect to " + TcpSocketManager.this.host + ':' + TcpSocketManager.this.port);
                }
                finally {
                    this.latch.countDown();
                }
            }
        }
    }
    
    private static class FactoryData
    {
        private final String host;
        private final int port;
        private final int connectTimeoutMillis;
        private final int delayMillis;
        private final boolean immediateFail;
        private final Layout<? extends Serializable> layout;
        
        public FactoryData(final String host, final int port, final int connectTimeoutMillis, final int delayMillis, final boolean immediateFail, final Layout<? extends Serializable> layout) {
            this.host = host;
            this.port = port;
            this.connectTimeoutMillis = connectTimeoutMillis;
            this.delayMillis = delayMillis;
            this.immediateFail = immediateFail;
            this.layout = layout;
        }
    }
    
    protected static class TcpSocketManagerFactory implements ManagerFactory<TcpSocketManager, FactoryData>
    {
        @Override
        public TcpSocketManager createManager(final String name, final FactoryData data) {
            InetAddress inetAddress;
            try {
                inetAddress = InetAddress.getByName(data.host);
            }
            catch (UnknownHostException ex) {
                TcpSocketManager.LOGGER.error("Could not find address of " + data.host, ex);
                return null;
            }
            try {
                final Socket socket = new Socket();
                socket.connect(new InetSocketAddress(data.host, data.port), data.connectTimeoutMillis);
                final OutputStream os = socket.getOutputStream();
                return new TcpSocketManager(name, os, socket, inetAddress, data.host, data.port, data.connectTimeoutMillis, data.delayMillis, data.immediateFail, data.layout);
            }
            catch (IOException ex2) {
                TcpSocketManager.LOGGER.error("TcpSocketManager (" + name + ") " + ex2);
                final OutputStream os = new ByteArrayOutputStream();
                if (data.delayMillis == 0) {
                    return null;
                }
                return new TcpSocketManager(name, os, null, inetAddress, data.host, data.port, data.connectTimeoutMillis, data.delayMillis, data.immediateFail, data.layout);
            }
        }
    }
}
