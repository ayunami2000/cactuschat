// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.packet.ingame.server.entity;

import org.spacehq.mc.protocol.util.ReflectionToString;
import org.spacehq.packetlib.io.NetOutput;
import java.io.IOException;
import org.spacehq.mc.protocol.data.MagicValues;
import org.spacehq.packetlib.io.NetInput;
import org.spacehq.mc.protocol.data.game.entity.player.Animation;
import org.spacehq.packetlib.packet.Packet;

public class ServerEntityAnimationPacket implements Packet
{
    private int entityId;
    private Animation animation;
    
    private ServerEntityAnimationPacket() {
    }
    
    public ServerEntityAnimationPacket(final int entityId, final Animation animation) {
        this.entityId = entityId;
        this.animation = animation;
    }
    
    public int getEntityId() {
        return this.entityId;
    }
    
    public Animation getAnimation() {
        return this.animation;
    }
    
    @Override
    public void read(final NetInput in) throws IOException {
        this.entityId = in.readVarInt();
        this.animation = MagicValues.key(Animation.class, in.readUnsignedByte());
    }
    
    @Override
    public void write(final NetOutput out) throws IOException {
        out.writeVarInt(this.entityId);
        out.writeByte(MagicValues.value(Integer.class, this.animation));
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
