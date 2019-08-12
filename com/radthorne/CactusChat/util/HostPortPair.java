// 
// Decompiled by Procyon v0.5.36
// 

package com.radthorne.CactusChat.util;

public class HostPortPair
{
    private final String host;
    private final int port;
    
    public HostPortPair(final String host, final int port) {
        this.host = host;
        this.port = port;
    }
    
    public String getHost() {
        return (this.host != null) ? this.host : "localhost";
    }
    
    public int getPort() {
        return (this.port > 0) ? this.port : 25565;
    }
}
