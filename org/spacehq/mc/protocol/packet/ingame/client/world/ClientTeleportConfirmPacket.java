// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.packet.ingame.client.world;

import org.spacehq.mc.protocol.util.ReflectionToString;
import org.spacehq.packetlib.io.NetOutput;
import java.io.IOException;
import org.spacehq.packetlib.io.NetInput;
import org.spacehq.packetlib.packet.Packet;

public class ClientTeleportConfirmPacket implements Packet
{
    private int id;
    
    private ClientTeleportConfirmPacket() {
    }
    
    public ClientTeleportConfirmPacket(final int id) {
        this.id = id;
    }
    
    public int getTeleportId() {
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
