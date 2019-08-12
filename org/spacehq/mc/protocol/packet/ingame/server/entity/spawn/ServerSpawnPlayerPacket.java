// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.packet.ingame.server.entity.spawn;

import org.spacehq.mc.protocol.util.ReflectionToString;
import org.spacehq.packetlib.io.NetOutput;
import java.io.IOException;
import org.spacehq.mc.protocol.util.NetUtil;
import org.spacehq.packetlib.io.NetInput;
import org.spacehq.mc.protocol.data.game.entity.metadata.EntityMetadata;
import java.util.UUID;
import org.spacehq.packetlib.packet.Packet;

public class ServerSpawnPlayerPacket implements Packet
{
    private int entityId;
    private UUID uuid;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private EntityMetadata[] metadata;
    
    private ServerSpawnPlayerPacket() {
    }
    
    public ServerSpawnPlayerPacket(final int entityId, final UUID uuid, final double x, final double y, final double z, final float yaw, final float pitch, final EntityMetadata[] metadata) {
        this.entityId = entityId;
        this.uuid = uuid;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.metadata = metadata;
    }
    
    public int getEntityId() {
        return this.entityId;
    }
    
    public UUID getUUID() {
        return this.uuid;
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
    
    public float getYaw() {
        return this.yaw;
    }
    
    public float getPitch() {
        return this.pitch;
    }
    
    public EntityMetadata[] getMetadata() {
        return this.metadata;
    }
    
    @Override
    public void read(final NetInput in) throws IOException {
        this.entityId = in.readVarInt();
        this.uuid = in.readUUID();
        this.x = in.readDouble();
        this.y = in.readDouble();
        this.z = in.readDouble();
        this.yaw = in.readByte() * 360 / 256.0f;
        this.pitch = in.readByte() * 360 / 256.0f;
        this.metadata = NetUtil.readEntityMetadata(in);
    }
    
    @Override
    public void write(final NetOutput out) throws IOException {
        out.writeVarInt(this.entityId);
        out.writeUUID(this.uuid);
        out.writeDouble(this.x);
        out.writeDouble(this.y);
        out.writeDouble(this.z);
        out.writeByte((byte)(this.yaw * 256.0f / 360.0f));
        out.writeByte((byte)(this.pitch * 256.0f / 360.0f));
        NetUtil.writeEntityMetadata(out, this.metadata);
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
