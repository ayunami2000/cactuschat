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

public class ServerEntityEffectPacket implements Packet
{
    private int entityId;
    private Effect effect;
    private int amplifier;
    private int duration;
    private boolean ambient;
    private boolean showParticles;
    
    private ServerEntityEffectPacket() {
    }
    
    public ServerEntityEffectPacket(final int entityId, final Effect effect, final int amplifier, final int duration, final boolean ambient, final boolean showParticles) {
        this.entityId = entityId;
        this.effect = effect;
        this.amplifier = amplifier;
        this.duration = duration;
        this.ambient = ambient;
        this.showParticles = showParticles;
    }
    
    public int getEntityId() {
        return this.entityId;
    }
    
    public Effect getEffect() {
        return this.effect;
    }
    
    public int getAmplifier() {
        return this.amplifier;
    }
    
    public int getDuration() {
        return this.duration;
    }
    
    public boolean isAmbient() {
        return this.ambient;
    }
    
    public boolean getShowParticles() {
        return this.showParticles;
    }
    
    @Override
    public void read(final NetInput in) throws IOException {
        this.entityId = in.readVarInt();
        this.effect = MagicValues.key(Effect.class, in.readByte());
        this.amplifier = in.readByte();
        this.duration = in.readVarInt();
        final int flags = in.readByte();
        this.ambient = ((flags & 0x1) == 0x1);
        this.showParticles = ((flags & 0x2) == 0x2);
    }
    
    @Override
    public void write(final NetOutput out) throws IOException {
        out.writeVarInt(this.entityId);
        out.writeByte(MagicValues.value(Integer.class, this.effect));
        out.writeByte(this.amplifier);
        out.writeVarInt(this.duration);
        int flags = 0;
        if (this.ambient) {
            flags |= 0x1;
        }
        if (this.showParticles) {
            flags |= 0x2;
        }
        out.writeByte(flags);
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
