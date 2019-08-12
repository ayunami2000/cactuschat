// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.packet.ingame.server.entity.spawn;

import org.spacehq.mc.protocol.util.ReflectionToString;
import org.spacehq.packetlib.io.NetOutput;
import java.io.IOException;
import org.spacehq.packetlib.io.NetInput;
import org.spacehq.packetlib.packet.Packet;

public class ServerSpawnExpOrbPacket implements Packet
{
    private int entityId;
    private double x;
    private double y;
    private double z;
    private int exp;
    
    private ServerSpawnExpOrbPacket() {
    }
    
    public ServerSpawnExpOrbPacket(final int entityId, final double x, final double y, final double z, final int exp) {
        this.entityId = entityId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.exp = exp;
    }
    
    public int getEntityId() {
        return this.entityId;
    }
    
    public double getX() {
        return this.x;
    }
    
    public double getY() {
        return this.y;
    }
    
    public double getZ() {
        return this.z;
    }
    
    public int getExp() {
        return this.exp;
    }
    
    @Override
    public void read(final NetInput in) throws IOException {
        this.entityId = in.readVarInt();
        this.x = in.readDouble();
        this.y = in.readDouble();
        this.z = in.readDouble();
        this.exp = in.readShort();
    }
    
    @Override
    public void write(final NetOutput out) throws IOException {
        out.writeVarInt(this.entityId);
        out.writeDouble(this.x);
        out.writeDouble(this.y);
        out.writeDouble(this.z);
        out.writeShort(this.exp);
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
