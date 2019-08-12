// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.packet.ingame.client.player;

import org.spacehq.mc.protocol.util.ReflectionToString;
import org.spacehq.packetlib.io.NetOutput;
import java.io.IOException;
import org.spacehq.mc.protocol.data.MagicValues;
import org.spacehq.packetlib.io.NetInput;
import org.spacehq.mc.protocol.data.game.entity.player.PlayerState;
import org.spacehq.packetlib.packet.Packet;

public class ClientPlayerStatePacket implements Packet
{
    private int entityId;
    private PlayerState state;
    private int jumpBoost;
    
    private ClientPlayerStatePacket() {
    }
    
    public ClientPlayerStatePacket(final int entityId, final PlayerState state) {
        this(entityId, state, 0);
    }
    
    public ClientPlayerStatePacket(final int entityId, final PlayerState state, final int jumpBoost) {
        this.entityId = entityId;
        this.state = state;
        this.jumpBoost = jumpBoost;
    }
    
    public int getEntityId() {
        return this.entityId;
    }
    
    public PlayerState getState() {
        return this.state;
    }
    
    public int getJumpBoost() {
        return this.jumpBoost;
    }
    
    @Override
    public void read(final NetInput in) throws IOException {
        this.entityId = in.readVarInt();
        this.state = MagicValues.key(PlayerState.class, in.readVarInt());
        this.jumpBoost = in.readVarInt();
    }
    
    @Override
    public void write(final NetOutput out) throws IOException {
        out.writeVarInt(this.entityId);
        out.writeVarInt(MagicValues.value(Integer.class, this.state));
        out.writeVarInt(this.jumpBoost);
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
