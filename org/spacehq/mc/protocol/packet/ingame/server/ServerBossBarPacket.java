// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.packet.ingame.server;

import org.spacehq.mc.protocol.util.ReflectionToString;
import org.spacehq.packetlib.io.NetOutput;
import java.io.IOException;
import org.spacehq.mc.protocol.data.MagicValues;
import org.spacehq.packetlib.io.NetInput;
import org.spacehq.mc.protocol.data.game.BossBarDivision;
import org.spacehq.mc.protocol.data.game.BossBarColor;
import org.spacehq.mc.protocol.data.message.Message;
import org.spacehq.mc.protocol.data.game.BossBarAction;
import java.util.UUID;
import org.spacehq.packetlib.packet.Packet;

public class ServerBossBarPacket implements Packet
{
    private UUID uuid;
    private BossBarAction action;
    private Message title;
    private float health;
    private BossBarColor color;
    private BossBarDivision division;
    private boolean darkenSky;
    private boolean dragonBar;
    
    private ServerBossBarPacket() {
    }
    
    public ServerBossBarPacket(final UUID uuid, final BossBarAction action, final Message title, final float health, final BossBarColor color, final BossBarDivision division, final boolean darkenSky, final boolean dragonBar) {
        this.uuid = uuid;
        this.action = BossBarAction.ADD;
        this.title = title;
        this.health = health;
        this.color = color;
        this.division = division;
        this.darkenSky = darkenSky;
        this.dragonBar = dragonBar;
    }
    
    public ServerBossBarPacket(final UUID uuid) {
        this.uuid = uuid;
        this.action = BossBarAction.REMOVE;
    }
    
    public ServerBossBarPacket(final UUID uuid, final BossBarAction action, final float health) {
        this.uuid = uuid;
        this.action = BossBarAction.UPDATE_HEALTH;
        this.health = health;
    }
    
    public ServerBossBarPacket(final UUID uuid, final BossBarAction action, final Message title) {
        this.uuid = uuid;
        this.action = BossBarAction.UPDATE_TITLE;
        this.title = title;
    }
    
    public ServerBossBarPacket(final UUID uuid, final BossBarAction action, final BossBarColor color, final BossBarDivision division) {
        this.uuid = uuid;
        this.action = BossBarAction.UPDATE_STYLE;
        this.color = color;
        this.division = division;
    }
    
    public ServerBossBarPacket(final UUID uuid, final BossBarAction action, final boolean darkenSky, final boolean dragonBar) {
        this.uuid = uuid;
        this.action = BossBarAction.UPDATE_FLAGS;
        this.darkenSky = darkenSky;
        this.dragonBar = dragonBar;
    }
    
    public UUID getUUID() {
        return this.uuid;
    }
    
    public BossBarAction getAction() {
        return this.action;
    }
    
    public Message getTitle() {
        return this.title;
    }
    
    public float getHealth() {
        return this.health;
    }
    
    public BossBarColor getColor() {
        return this.color;
    }
    
    public BossBarDivision getDivision() {
        return this.division;
    }
    
    public boolean getDarkenSky() {
        return this.darkenSky;
    }
    
    public boolean isDragonBar() {
        return this.dragonBar;
    }
    
    @Override
    public void read(final NetInput in) throws IOException {
        this.uuid = in.readUUID();
        this.action = MagicValues.key(BossBarAction.class, in.readVarInt());
        if (this.action == BossBarAction.ADD || this.action == BossBarAction.UPDATE_TITLE) {
            this.title = Message.fromString(in.readString());
        }
        if (this.action == BossBarAction.ADD || this.action == BossBarAction.UPDATE_HEALTH) {
            this.health = in.readFloat();
        }
        if (this.action == BossBarAction.ADD || this.action == BossBarAction.UPDATE_STYLE) {
            this.color = MagicValues.key(BossBarColor.class, in.readVarInt());
            this.division = MagicValues.key(BossBarDivision.class, in.readVarInt());
        }
        if (this.action == BossBarAction.ADD || this.action == BossBarAction.UPDATE_FLAGS) {
            final int flags = in.readUnsignedByte();
            this.darkenSky = ((flags & 0x1) == 0x1);
            this.dragonBar = ((flags & 0x2) == 0x2);
        }
    }
    
    @Override
    public void write(final NetOutput out) throws IOException {
        out.writeUUID(this.uuid);
        out.writeVarInt(MagicValues.value(Integer.class, this.action));
        if (this.action == BossBarAction.ADD || this.action == BossBarAction.UPDATE_TITLE) {
            out.writeString(this.title.toJsonString());
        }
        if (this.action == BossBarAction.ADD || this.action == BossBarAction.UPDATE_HEALTH) {
            out.writeFloat(this.health);
        }
        if (this.action == BossBarAction.ADD || this.action == BossBarAction.UPDATE_STYLE) {
            out.writeVarInt(MagicValues.value(Integer.class, this.color));
            out.writeVarInt(MagicValues.value(Integer.class, this.division));
        }
        if (this.action == BossBarAction.ADD || this.action == BossBarAction.UPDATE_FLAGS) {
            int flags = 0;
            if (this.darkenSky) {
                flags |= 0x1;
            }
            if (this.dragonBar) {
                flags |= 0x2;
            }
            out.writeByte(flags);
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
