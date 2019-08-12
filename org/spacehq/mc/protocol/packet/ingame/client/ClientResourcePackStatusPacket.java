// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.packet.ingame.client;

import org.spacehq.mc.protocol.util.ReflectionToString;
import org.spacehq.packetlib.io.NetOutput;
import java.io.IOException;
import org.spacehq.mc.protocol.data.MagicValues;
import org.spacehq.packetlib.io.NetInput;
import org.spacehq.mc.protocol.data.game.ResourcePackStatus;
import org.spacehq.packetlib.packet.Packet;

public class ClientResourcePackStatusPacket implements Packet
{
    private ResourcePackStatus status;
    
    private ClientResourcePackStatusPacket() {
    }
    
    public ClientResourcePackStatusPacket(final ResourcePackStatus status) {
        this.status = status;
    }
    
    public ResourcePackStatus getStatus() {
        return this.status;
    }
    
    @Override
    public void read(final NetInput in) throws IOException {
        this.status = MagicValues.key(ResourcePackStatus.class, in.readVarInt());
    }
    
    @Override
    public void write(final NetOutput out) throws IOException {
        out.writeVarInt(MagicValues.value(Integer.class, this.status));
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
