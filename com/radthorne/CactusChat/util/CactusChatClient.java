// 
// Decompiled by Procyon v0.5.36
// 

package com.radthorne.CactusChat.util;

import org.spacehq.packetlib.SessionFactory;
import org.spacehq.packetlib.packet.PacketProtocol;
import org.spacehq.packetlib.tcp.TcpSessionFactory;
import org.spacehq.packetlib.event.session.SessionListener;
import org.spacehq.mc.auth.exception.request.RequestException;
import com.radthorne.CactusChat.Main;
import org.spacehq.mc.protocol.MinecraftProtocol;
import org.spacehq.packetlib.Client;
import org.spacehq.packetlib.Session;

public class CactusChatClient
{
    private String HOST;
    private int PORT;
    private Session session;
    private Client client;
    MinecraftProtocol protocol;
    private boolean connected;
    
    public CactusChatClient() {
        this.connected = false;
    }
    
    public void login(final String username, final String password) throws RequestException {
        Main.debug("Attempting to log in...");
        this.protocol = new MinecraftProtocol(username, password, false);
        Main.debug("Logged in successfully");
    }
    
    public void connect(final String host, final int port, final SessionListener listener) {
        this.HOST = host;
        this.PORT = port;
        Main.debug("Attempting to connect to " + this.HOST + "...");
        this.client = new Client(this.HOST, this.PORT, this.protocol, new TcpSessionFactory());
        (this.session = this.client.getSession()).addListener(listener);
        this.session.connect();
        Main.debug("Connected successfully");
        this.connected = true;
    }
    
    public int getPORT() {
        return this.PORT;
    }
    
    public String getHOST() {
        return this.HOST;
    }
    
    public Session getSession() {
        return this.session;
    }
    
    public Client getClient() {
        return this.client;
    }
    
    public boolean isConnected() {
        return this.connected;
    }
    
    public void disconnect() {
        this.disconnect("Quitting");
    }
    
    public void disconnect(final String reason) {
        this.session.disconnect(reason);
    }
}
