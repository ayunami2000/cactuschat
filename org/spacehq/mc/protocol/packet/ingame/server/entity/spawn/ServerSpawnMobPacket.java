// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.packet.ingame.server.entity.spawn;

import org.spacehq.mc.protocol.util.ReflectionToString;
import org.spacehq.packetlib.io.NetOutput;
import java.io.IOException;
import org.spacehq.mc.protocol.util.NetUtil;
import org.spacehq.mc.protocol.data.MagicValues;
import org.spacehq.packetlib.io.NetInput;
import org.spacehq.mc.protocol.data.game.entity.metadata.EntityMetadata;
import org.spacehq.mc.protocol.data.game.entity.type.MobType;
import java.util.UUID;
import org.spacehq.packetlib.packet.Packet;

public class ServerSpawnMobPacket implements Packet
{
    private int entityId;
    private UUID uuid;
    private MobType type;
    private double x;
    private double y;
    private double z;
    private float pitch;
    private float yaw;
    private float headYaw;
    private double motX;
    private double motY;
    private double motZ;
    private EntityMetadata[] metadata;
    
    private ServerSpawnMobPacket() {
    }
    
    public ServerSpawnMobPacket(final int entityId, final UUID uuid, final MobType type, final double x, final double y, final double z, final float yaw, final float pitch, final float headYaw, final double motX, final double motY, final double motZ, final EntityMetadata[] metadata) {
        this.entityId = entityId;
        this.uuid = uuid;
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.headYaw = headYaw;
        this.motX = motX;
        this.motY = motY;
        this.motZ = motZ;
        this.metadata = metadata;
    }
    
    public int getEntityId() {
        return this.entityId;
    }
    
    public UUID getUUID() {
        return this.uuid;
    }
    
    public MobType getType() {
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
    
    public float getYaw() {
        return this.yaw;
    }
    
    public float getPitch() {
        return this.pitch;
    }
    
    public float getHeadYaw() {
        return this.headYaw;
    }
    
    public double getMotionX() {
        return this.motX;
    }
    
    public double getMotionY() {
        return this.motY;
    }
    
    public double getMotionZ() {
        return this.motZ;
    }
    
    public EntityMetadata[] getMetadata() {
        return this.metadata;
    }
    
    @Override
    public void read(final NetInput in) throws IOException {
        this.entityId = in.readVarInt();
        this.uuid = in.readUUID();
        this.type = MagicValues.key(MobType.class, in.readUnsignedByte());
        this.x = in.readDouble();
        this.y = in.readDouble();
        this.z = in.readDouble();
        this.yaw = in.readByte() * 360 / 256.0f;
        this.pitch = in.readByte() * 360 / 256.0f;
        this.headYaw = in.readByte() * 360 / 256.0f;
        this.motX = in.readShort() / 8000.0;
        this.motY = in.readShort() / 8000.0;
        this.motZ = in.readShort() / 8000.0;
        this.metadata = NetUtil.readEntityMetadata(in);
    }
    
    @Override
    public void write(final NetOutput out) throws IOException {
        out.writeVarInt(this.entityId);
        out.writeUUID(this.uuid);
        out.writeByte(MagicValues.value(Integer.class, this.type));
        out.writeDouble(this.x);
        out.writeDouble(this.y);
        out.writeDouble(this.z);
        out.writeByte((byte)(this.yaw * 256.0f / 360.0f));
        out.writeByte((byte)(this.pitch * 256.0f / 360.0f));
        out.writeByte((byte)(this.headYaw * 256.0f / 360.0f));
        out.writeShort((int)(this.motX * 8000.0));
        out.writeShort((int)(this.motY * 8000.0));
        out.writeShort((int)(this.motZ * 8000.0));
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
