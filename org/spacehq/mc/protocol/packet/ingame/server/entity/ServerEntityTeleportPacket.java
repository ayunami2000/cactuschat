// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.packet.ingame.server.entity;

import org.spacehq.mc.protocol.util.ReflectionToString;
import org.spacehq.packetlib.io.NetOutput;
import java.io.IOException;
import org.spacehq.packetlib.io.NetInput;
import org.spacehq.packetlib.packet.Packet;

public class ServerEntityTeleportPacket implements Packet
{
    private int entityId;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private boolean onGround;
    
    private ServerEntityTeleportPacket() {
    }
    
    public ServerEntityTeleportPacket(final int entityId, final double x, final double y, final double z, final float yaw, final float pitch, final boolean onGround) {
        this.entityId = entityId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
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
    
    public float getYaw() {
        return this.yaw;
    }
    
    public float getPitch() {
        return this.pitch;
    }
    
    public boolean isOnGround() {
        return this.onGround;
    }
    
    @Override
    public void read(final NetInput in) throws IOException {
        this.entityId = in.readVarInt();
        this.x = in.readDouble();
        this.y = in.readDouble();
        this.z = in.readDouble();
        this.yaw = in.readByte() * 360 / 256.0f;
        this.pitch = in.readByte() * 360 / 256.0f;
        this.onGround = in.readBoolean();
    }
    
    @Override
    public void write(final NetOutput out) throws IOException {
        out.writeVarInt(this.entityId);
        out.writeDouble(this.x);
        out.writeDouble(this.y);
        out.writeDouble(this.z);
        out.writeByte((byte)(this.yaw * 256.0f / 360.0f));
        out.writeByte((byte)(this.pitch * 256.0f / 360.0f));
        out.writeBoolean(this.onGround);
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
