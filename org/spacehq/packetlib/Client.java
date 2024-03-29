// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.packetlib;

import org.spacehq.packetlib.packet.PacketProtocol;

public class Client
{
    private String host;
    private int port;
    private PacketProtocol protocol;
    private Session session;
    
    public Client(final String host, final int port, final PacketProtocol protocol, final SessionFactory factory) {
        this.host = host;
        this.port = port;
        this.protocol = protocol;
        this.session = factory.createClientSession(this);
    }
    
    public String getHost() {
        return this.host;
    }
    
    public int getPort() {
        return this.port;
    }
    
    public PacketProtocol getPacketProtocol() {
        return this.protocol;
    }
    
    public Session getSession() {
        return this.session;
    }
}
