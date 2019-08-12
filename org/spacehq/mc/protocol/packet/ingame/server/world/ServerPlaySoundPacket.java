// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.packet.ingame.server.world;

import org.spacehq.mc.protocol.util.ReflectionToString;
import org.spacehq.packetlib.io.NetOutput;
import java.io.IOException;
import org.spacehq.mc.protocol.data.game.world.sound.CustomSound;
import org.spacehq.mc.protocol.data.MagicValues;
import org.spacehq.mc.protocol.data.game.world.sound.BuiltinSound;
import org.spacehq.packetlib.io.NetInput;
import org.spacehq.mc.protocol.data.game.world.sound.SoundCategory;
import org.spacehq.mc.protocol.data.game.world.sound.Sound;
import org.spacehq.packetlib.packet.Packet;

public class ServerPlaySoundPacket implements Packet
{
    private Sound sound;
    private SoundCategory category;
    private double x;
    private double y;
    private double z;
    private float volume;
    private float pitch;
    
    private ServerPlaySoundPacket() {
    }
    
    public ServerPlaySoundPacket(final Sound sound, final SoundCategory category, final double x, final double y, final double z, final float volume, final float pitch) {
        this.sound = sound;
        this.category = category;
        this.x = x;
        this.y = y;
        this.z = z;
        this.volume = volume;
        this.pitch = pitch;
    }
    
    public Sound getSound() {
        return this.sound;
    }
    
    public SoundCategory getCategory() {
        return this.category;
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
    
    public float getVolume() {
        return this.volume;
    }
    
    public float getPitch() {
        return this.pitch;
    }
    
    @Override
    public void read(final NetInput in) throws IOException {
        final String value = in.readString();
        try {
            this.sound = MagicValues.key(BuiltinSound.class, value);
        }
        catch (IllegalArgumentException e) {
            this.sound = new CustomSound(value);
        }
        this.category = MagicValues.key(SoundCategory.class, in.readVarInt());
        this.x = in.readInt() / 8.0;
        this.y = in.readInt() / 8.0;
        this.z = in.readInt() / 8.0;
        this.volume = in.readFloat();
        this.pitch = in.readFloat();
    }
    
    @Override
    public void write(final NetOutput out) throws IOException {
        String value = "";
        if (this.sound instanceof CustomSound) {
            value = ((CustomSound)this.sound).getName();
        }
        else if (this.sound instanceof BuiltinSound) {
            value = MagicValues.value(String.class, this.sound);
        }
        out.writeString(value);
        out.writeVarInt(MagicValues.value(Integer.class, this.category));
        out.writeInt((int)(this.x * 8.0));
        out.writeInt((int)(this.y * 8.0));
        out.writeInt((int)(this.z * 8.0));
        out.writeFloat(this.volume);
        out.writeFloat(this.pitch);
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
