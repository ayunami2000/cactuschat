// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.packet.ingame.server;

import org.spacehq.mc.protocol.util.ReflectionToString;
import org.spacehq.packetlib.io.NetOutput;
import java.io.IOException;
import org.spacehq.packetlib.io.NetInput;
import org.spacehq.packetlib.packet.Packet;

public class ServerKeepAlivePacket implements Packet
{
    private int id;
    
    private ServerKeepAlivePacket() {
    }
    
    public ServerKeepAlivePacket(final int id) {
        this.id = id;
    }
    
    public int getPingId() {
        return this.id;
    }
    
    @Override
    public void read(final NetInput in) throws IOException {
        this.id = in.readVarInt();
    }
    
    @Override
    public void write(final NetOutput out) throws IOException {
        out.writeVarInt(this.id);
    }
    
    @Override
    public boolean isPriority() {
        return false;
    }
    
    @Override
    public String toString() {
        return ReflectionToString.toString(this);
    }
}
