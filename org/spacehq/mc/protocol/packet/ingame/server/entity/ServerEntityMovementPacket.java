// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.packet.ingame.server.entity;

import org.spacehq.mc.protocol.util.ReflectionToString;
import org.spacehq.packetlib.io.NetOutput;
import java.io.IOException;
import org.spacehq.packetlib.io.NetInput;
import org.spacehq.packetlib.packet.Packet;

public class ServerEntityMovementPacket implements Packet
{
    protected int entityId;
    protected double moveX;
    protected double moveY;
    protected double moveZ;
    protected float yaw;
    protected float pitch;
    private boolean onGround;
    protected boolean pos;
    protected boolean rot;
    
    protected ServerEntityMovementPacket() {
        this.pos = false;
        this.rot = false;
    }
    
    public ServerEntityMovementPacket(final int entityId, final boolean onGround) {
        this.pos = false;
        this.rot = false;
        this.entityId = entityId;
        this.onGround = onGround;
    }
    
    public int getEntityId() {
        return this.entityId;
    }
    
    public double getMovementX() {
        return this.moveX;
    }
    
    public double getMovementY() {
        return this.moveY;
    }
    
    public double getMovementZ() {
        return this.moveZ;
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
        if (this.pos) {
            this.moveX = in.readShort() / 4096.0;
            this.moveY = in.readShort() / 4096.0;
            this.moveZ = in.readShort() / 4096.0;
        }
        if (this.rot) {
            this.yaw = in.readByte() * 360 / 256.0f;
            this.pitch = in.readByte() * 360 / 256.0f;
        }
        if (this.pos || this.rot) {
            this.onGround = in.readBoolean();
        }
    }
    
    @Override
    public void write(final NetOutput out) throws IOException {
        out.writeVarInt(this.entityId);
        if (this.pos) {
            out.writeShort((int)(this.moveX * 4096.0));
            out.writeShort((int)(this.moveY * 4096.0));
            out.writeShort((int)(this.moveZ * 4096.0));
        }
        if (this.rot) {
            out.writeByte((byte)(this.yaw * 256.0f / 360.0f));
            out.writeByte((byte)(this.pitch * 256.0f / 360.0f));
        }
        if (this.pos || this.rot) {
            out.writeBoolean(this.onGround);
        }
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
