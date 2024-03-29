// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.packet.ingame.server.world;

import org.spacehq.mc.protocol.util.ReflectionToString;
import org.spacehq.packetlib.io.NetOutput;
import java.io.IOException;
import org.spacehq.mc.protocol.data.game.world.effect.BonemealGrowEffectData;
import org.spacehq.mc.protocol.data.game.world.effect.BreakPotionEffectData;
import org.spacehq.mc.protocol.data.game.world.effect.BreakBlockEffectData;
import org.spacehq.mc.protocol.data.game.world.block.BlockState;
import org.spacehq.mc.protocol.data.game.world.effect.SmokeEffectData;
import org.spacehq.mc.protocol.data.game.world.effect.ParticleEffect;
import org.spacehq.mc.protocol.data.game.world.effect.RecordEffectData;
import org.spacehq.mc.protocol.data.game.world.effect.SoundEffect;
import org.spacehq.mc.protocol.util.NetUtil;
import org.spacehq.mc.protocol.data.MagicValues;
import org.spacehq.packetlib.io.NetInput;
import org.spacehq.mc.protocol.data.game.world.effect.WorldEffectData;
import org.spacehq.mc.protocol.data.game.entity.metadata.Position;
import org.spacehq.mc.protocol.data.game.world.effect.WorldEffect;
import org.spacehq.packetlib.packet.Packet;

public class ServerPlayEffectPacket implements Packet
{
    private WorldEffect effect;
    private Position position;
    private WorldEffectData data;
    private boolean broadcast;
    
    private ServerPlayEffectPacket() {
    }
    
    public ServerPlayEffectPacket(final WorldEffect effect, final Position position, final WorldEffectData data) {
        this(effect, position, data, false);
    }
    
    public ServerPlayEffectPacket(final WorldEffect effect, final Position position, final WorldEffectData data, final boolean broadcast) {
        this.effect = effect;
        this.position = position;
        this.data = data;
        this.broadcast = broadcast;
    }
    
    public WorldEffect getEffect() {
        return this.effect;
    }
    
    public Position getPosition() {
        return this.position;
    }
    
    public WorldEffectData getData() {
        return this.data;
    }
    
    public boolean getBroadcast() {
        return this.broadcast;
    }
    
    @Override
    public void read(final NetInput in) throws IOException {
        this.effect = MagicValues.key(WorldEffect.class, in.readInt());
        this.position = NetUtil.readPosition(in);
        final int value = in.readInt();
        if (this.effect == SoundEffect.RECORD) {
            this.data = new RecordEffectData(value);
        }
        else if (this.effect == ParticleEffect.SMOKE) {
            this.data = MagicValues.key(SmokeEffectData.class, value);
        }
        else if (this.effect == ParticleEffect.BREAK_BLOCK) {
            this.data = new BreakBlockEffectData(new BlockState(value & 0xFFF, value >> 12 & 0xFF));
        }
        else if (this.effect == ParticleEffect.BREAK_SPLASH_POTION) {
            this.data = new BreakPotionEffectData(value);
        }
        else if (this.effect == ParticleEffect.BONEMEAL_GROW) {
            this.data = new BonemealGrowEffectData(value);
        }
        this.broadcast = in.readBoolean();
    }
    
    @Override
    public void write(final NetOutput out) throws IOException {
        out.writeInt(MagicValues.value(Integer.class, this.effect));
        NetUtil.writePosition(out, this.position);
        int value = 0;
        if (this.data instanceof RecordEffectData) {
            value = ((RecordEffectData)this.data).getRecordId();
        }
        else if (this.data instanceof SmokeEffectData) {
            value = MagicValues.value(Integer.class, this.data);
        }
        else if (this.data instanceof BreakBlockEffectData) {
            value = ((((BreakBlockEffectData)this.data).getBlockState().getId() & 0xFFF) | (((BreakBlockEffectData)this.data).getBlockState().getData() & 0xFF) << 12);
        }
        else if (this.data instanceof BreakPotionEffectData) {
            value = ((BreakPotionEffectData)this.data).getPotionId();
        }
        else if (this.data instanceof BonemealGrowEffectData) {
            value = ((BonemealGrowEffectData)this.data).getParticleCount();
        }
        out.writeInt(value);
        out.writeBoolean(this.broadcast);
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
