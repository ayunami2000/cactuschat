// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.packet.ingame.server.entity.spawn;

import org.spacehq.mc.protocol.util.ReflectionToString;
import org.spacehq.packetlib.io.NetOutput;
import java.io.IOException;
import org.spacehq.mc.protocol.data.MagicValues;
import org.spacehq.packetlib.io.NetInput;
import org.spacehq.mc.protocol.data.game.entity.type.GlobalEntityType;
import org.spacehq.packetlib.packet.Packet;

public class ServerSpawnGlobalEntityPacket implements Packet
{
    private int entityId;
    private GlobalEntityType type;
    private double x;
    private double y;
    private double z;
    
    private ServerSpawnGlobalEntityPacket() {
    }
    
    public ServerSpawnGlobalEntityPacket(final int entityId, final GlobalEntityType type, final double x, final double y, final double z) {
        this.entityId = entityId;
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public int getEntityId() {
        return this.entityId;
    }
    
    public GlobalEntityType getType() {
        return this.type;
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
    
    @Override
    public void read(final NetInput in) throws IOException {
        this.entityId = in.readVarInt();
        this.type = MagicValues.key(GlobalEntityType.class, in.readByte());
        this.x = in.readDouble();
        this.y = in.readDouble();
        this.z = in.readDouble();
    }
    
    @Override
    public void write(final NetOutput out) throws IOException {
        out.writeVarInt(this.entityId);
        out.writeByte(MagicValues.value(Integer.class, this.type));
        out.writeDouble(this.x);
        out.writeDouble(this.y);
        out.writeDouble(this.z);
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
