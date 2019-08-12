// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.net.server;

import java.io.OptionalDataException;
import java.io.EOFException;
import org.apache.logging.log4j.core.LogEventListener;
import java.util.Iterator;
import java.net.Socket;
import java.util.Map;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import org.apache.logging.log4j.core.util.Log4jThread;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import java.io.ObjectInputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ConcurrentMap;
import java.io.InputStream;

public class TcpSocketServer<T extends InputStream> extends AbstractSocketServer<T>
{
    private final ConcurrentMap<Long, SocketHandler> handlers;
    private final ServerSocket serverSocket;
    
    public TcpSocketServer(final int port, final LogEventBridge<T> logEventInput) throws IOException {
        this(port, logEventInput, new ServerSocket(port));
    }
    
    public TcpSocketServer(final int port, final LogEventBridge<T> logEventInput, final ServerSocket serverSocket) throws IOException {
        super(port, logEventInput);
        this.handlers = new ConcurrentHashMap<Long, SocketHandler>();
        this.serverSocket = serverSocket;
    }
    
    public static TcpSocketServer<InputStream> createJsonSocketServer(final int port) throws IOException {
        TcpSocketServer.LOGGER.entry(new Object[] { "createJsonSocketServer", port });
        final TcpSocketServer<InputStream> socketServer = new TcpSocketServer<InputStream>(port, new JsonInputStreamLogEventBridge());
        return TcpSocketServer.LOGGER.exit(socketServer);
    }
    
    public static TcpSocketServer<ObjectInputStream> createSerializedSocketServer(final int port) throws IOException {
        TcpSocketServer.LOGGER.entry(port);
        final TcpSocketServer<ObjectInputStream> socketServer = new TcpSocketServer<ObjectInputStream>(port, new ObjectInputStreamLogEventBridge());
        return TcpSocketServer.LOGGER.exit(socketServer);
    }
    
    public static TcpSocketServer<InputStream> createXmlSocketServer(final int port) throws IOException {
        TcpSocketServer.LOGGER.entry(port);
        final TcpSocketServer<InputStream> socketServer = new TcpSocketServer<InputStream>(port, new XmlInputStreamLogEventBridge());
        return TcpSocketServer.LOGGER.exit(socketServer);
    }
    
    public static void main(final String[] args) throws Exception {
        if (args.length < 1 || args.length > 2) {
            System.err.println("Incorrect number of arguments");
            printUsage();
            return;
        }
        final int port = Integer.parseInt(args[0]);
        if (port <= 0 || port >= 65534) {
            System.err.println("Invalid port number");
            printUsage();
            return;
        }
        if (args.length == 2 && args[1].length() > 0) {
            ConfigurationFactory.setConfigurationFactory(new ServerConfigurationFactory(args[1]));
        }
        final TcpSocketServer<ObjectInputStream> socketServer = createSerializedSocketServer(port);
        final Thread serverThread = new Log4jThread(socketServer);
        serverThread.start();
        final Charset enc = Charset.defaultCharset();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, enc));
        String line;
        do {
            line = reader.readLine();
        } while (line != null && !line.equalsIgnoreCase("Quit") && !line.equalsIgnoreCase("Stop") && !line.equalsIgnoreCase("Exit"));
        socketServer.shutdown();
        serverThread.join();
    }
    
    private static void printUsage() {
        System.out.println("Usage: ServerSocket port configFilePath");
    }
    
    @Override
    public void run() {
        this.logger.entry();
        while (this.isActive()) {
            if (this.serverSocket.isClosed()) {
                return;
            }
            try {
                this.logger.debug("Socket accept()...");
                final Socket clientSocket = this.serverSocket.accept();
                this.logger.debug("Socket accepted: {}", new Object[] { clientSocket });
                clientSocket.setSoLinger(true, 0);
                final SocketHandler handler = new SocketHandler(clientSocket);
                this.handlers.put(handler.getId(), handler);
                handler.start();
            }
            catch (IOException e) {
                if (this.serverSocket.isClosed()) {
                    this.logger.exit();
                    return;
                }
                this.logger.error("Exception encountered on accept. Ignoring. Stack Trace :", e);
            }
        }
        for (final Map.Entry<Long, SocketHandler> entry : this.handlers.entrySet()) {
            final SocketHandler handler2 = entry.getValue();
            handler2.shutdown();
            try {
                handler2.join();
            }
            catch (InterruptedException ex) {}
        }
        this.logger.exit();
    }
    
    public void shutdown() throws IOException {
        this.logger.entry();
        this.setActive(false);
        Thread.currentThread().interrupt();
        this.serverSocket.close();
        this.logger.exit();
    }
    
    private class SocketHandler extends Thread
    {
        private final T inputStream;
        private volatile boolean shutdown;
        
        public SocketHandler(final Socket socket) throws IOException {
            this.shutdown = false;
            this.inputStream = (T)TcpSocketServer.this.logEventInput.wrapStream(socket.getInputStream());
        }
        
        @Override
        public void run() {
            TcpSocketServer.this.logger.entry();
            boolean closed = false;
            try {
                try {
                    while (!this.shutdown) {
                        TcpSocketServer.this.logEventInput.logEvents((T)this.inputStream, TcpSocketServer.this);
                    }
                }
                catch (EOFException e3) {
                    closed = true;
                }
                catch (OptionalDataException e) {
                    TcpSocketServer.this.logger.error("OptionalDataException eof=" + e.eof + " length=" + e.length, e);
                }
                catch (IOException e2) {
                    TcpSocketServer.this.logger.error("IOException encountered while reading from socket", e2);
                }
                if (!closed) {
                    try {
                        this.inputStream.close();
                    }
                    catch (Exception ex) {}
                }
            }
            finally {
                TcpSocketServer.this.handlers.remove(this.getId());
            }
            TcpSocketServer.this.logger.exit();
        }
        
        public void shutdown() {
            this.shutdown = true;
            this.interrupt();
        }
    }
}
