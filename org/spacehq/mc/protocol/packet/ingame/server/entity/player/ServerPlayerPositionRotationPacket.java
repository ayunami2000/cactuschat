// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.packet.ingame.server.entity.player;

import org.spacehq.mc.protocol.util.ReflectionToString;
import java.util.Iterator;
import org.spacehq.packetlib.io.NetOutput;
import java.io.IOException;
import org.spacehq.mc.protocol.data.MagicValues;
import java.util.ArrayList;
import org.spacehq.packetlib.io.NetInput;
import java.util.Arrays;
import org.spacehq.mc.protocol.data.game.entity.player.PositionElement;
import java.util.List;
import org.spacehq.packetlib.packet.Packet;

public class ServerPlayerPositionRotationPacket implements Packet
{
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private List<PositionElement> relative;
    private int teleportId;
    
    private ServerPlayerPositionRotationPacket() {
    }
    
    public ServerPlayerPositionRotationPacket(final double x, final double y, final double z, final float yaw, final float pitch, final int teleportId, final PositionElement... relative) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.teleportId = teleportId;
        this.relative = Arrays.asList((relative != null) ? relative : new PositionElement[0]);
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
    
    public List<PositionElement> getRelativeElements() {
        return this.relative;
    }
    
    public int getTeleportId() {
        return this.teleportId;
    }
    
    @Override
    public void read(final NetInput in) throws IOException {
        this.x = in.readDouble();
        this.y = in.readDouble();
        this.z = in.readDouble();
        this.yaw = in.readFloat();
        this.pitch = in.readFloat();
        this.relative = new ArrayList<PositionElement>();
        final int flags = in.readUnsignedByte();
        for (final PositionElement element : PositionElement.values()) {
            final int bit = 1 << MagicValues.value(Integer.class, element);
            if ((flags & bit) == bit) {
                this.relative.add(element);
            }
        }
        this.teleportId = in.readVarInt();
    }
    
    @Override
    public void write(final NetOutput out) throws IOException {
        out.writeDouble(this.x);
        out.writeDouble(this.y);
        out.writeDouble(this.z);
        out.writeFloat(this.yaw);
        out.writeFloat(this.pitch);
        int flags = 0;
        for (final PositionElement element : this.relative) {
            flags |= 1 << MagicValues.value(Integer.class, element);
        }
        out.writeByte(flags);
        out.writeVarInt(this.teleportId);
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
