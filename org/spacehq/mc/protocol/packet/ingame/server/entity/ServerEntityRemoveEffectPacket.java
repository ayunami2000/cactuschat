// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.packet.ingame.server.entity;

import org.spacehq.mc.protocol.util.ReflectionToString;
import org.spacehq.packetlib.io.NetOutput;
import java.io.IOException;
import org.spacehq.mc.protocol.data.MagicValues;
import org.spacehq.packetlib.io.NetInput;
import org.spacehq.mc.protocol.data.game.entity.Effect;
import org.spacehq.packetlib.packet.Packet;

public class ServerEntityRemoveEffectPacket implements Packet
{
    private int entityId;
    private Effect effect;
    
    private ServerEntityRemoveEffectPacket() {
    }
    
    public ServerEntityRemoveEffectPacket(final int entityId, final Effect effect) {
        this.entityId = entityId;
        this.effect = effect;
    }
    
    public int getEntityId() {
        return this.entityId;
    }
    
    public Effect getEffect() {
        return this.effect;
    }
    
    @Override
    public void read(final NetInput in) throws IOException {
        this.entityId = in.readVarInt();
        this.effect = MagicValues.key(Effect.class, in.readUnsignedByte());
    }
    
    @Override
    public void write(final NetOutput out) throws IOException {
        out.writeVarInt(this.entityId);
        out.writeByte(MagicValues.value(Integer.class, this.effect));
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
