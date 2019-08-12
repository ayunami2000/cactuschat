// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.packet.ingame.server;

import org.spacehq.mc.protocol.util.ReflectionToString;
import org.spacehq.packetlib.io.NetOutput;
import java.io.IOException;
import org.spacehq.packetlib.io.NetInput;
import org.spacehq.packetlib.packet.Packet;

public class ServerSetCooldownPacket implements Packet
{
    private int itemId;
    private int cooldownTicks;
    
    private ServerSetCooldownPacket() {
    }
    
    public ServerSetCooldownPacket(final int itemId, final int cooldownTicks) {
        this.itemId = itemId;
        this.cooldownTicks = cooldownTicks;
    }
    
    public int getItemId() {
        return this.itemId;
    }
    
    public int getCooldownTicks() {
        return this.cooldownTicks;
    }
    
    @Override
    public void read(final NetInput in) throws IOException {
        this.itemId = in.readVarInt();
        this.cooldownTicks = in.readVarInt();
    }
    
    @Override
    public void write(final NetOutput out) throws IOException {
        out.writeVarInt(this.itemId);
        out.writeVarInt(this.cooldownTicks);
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
