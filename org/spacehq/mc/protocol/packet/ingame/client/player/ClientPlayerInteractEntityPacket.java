// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.packet.ingame.client.player;

import org.spacehq.mc.protocol.util.ReflectionToString;
import org.spacehq.packetlib.io.NetOutput;
import java.io.IOException;
import org.spacehq.mc.protocol.data.MagicValues;
import org.spacehq.packetlib.io.NetInput;
import org.spacehq.mc.protocol.data.game.entity.player.Hand;
import org.spacehq.mc.protocol.data.game.entity.player.InteractAction;
import org.spacehq.packetlib.packet.Packet;

public class ClientPlayerInteractEntityPacket implements Packet
{
    private int entityId;
    private InteractAction action;
    private float targetX;
    private float targetY;
    private float targetZ;
    private Hand hand;
    
    private ClientPlayerInteractEntityPacket() {
    }
    
    public ClientPlayerInteractEntityPacket(final int entityId, final InteractAction action) {
        this(entityId, action, Hand.MAIN_HAND);
    }
    
    public ClientPlayerInteractEntityPacket(final int entityId, final InteractAction action, final Hand hand) {
        this(entityId, action, 0.0f, 0.0f, 0.0f, hand);
    }
    
    public ClientPlayerInteractEntityPacket(final int entityId, final InteractAction action, final float targetX, final float targetY, final float targetZ, final Hand hand) {
        this.entityId = entityId;
        this.action = action;
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;
        this.hand = hand;
    }
    
    public int getEntityId() {
        return this.entityId;
    }
    
    public InteractAction getAction() {
        return this.action;
    }
    
    public float getTargetX() {
        return this.targetX;
    }
    
    public float getTargetY() {
        return this.targetY;
    }
    
    public float getTargetZ() {
        return this.targetZ;
    }
    
    public Hand getHand() {
        return this.hand;
    }
    
    @Override
    public void read(final NetInput in) throws IOException {
        this.entityId = in.readVarInt();
        this.action = MagicValues.key(InteractAction.class, in.readVarInt());
        if (this.action == InteractAction.INTERACT_AT) {
            this.targetX = in.readFloat();
            this.targetY = in.readFloat();
            this.targetZ = in.readFloat();
        }
        if (this.action == InteractAction.INTERACT || this.action == InteractAction.INTERACT_AT) {
            this.hand = MagicValues.key(Hand.class, in.readVarInt());
        }
    }
    
    @Override
    public void write(final NetOutput out) throws IOException {
        out.writeVarInt(this.entityId);
        out.writeVarInt(MagicValues.value(Integer.class, this.action));
        if (this.action == InteractAction.INTERACT_AT) {
            out.writeFloat(this.targetX);
            out.writeFloat(this.targetY);
            out.writeFloat(this.targetZ);
        }
        if (this.action == InteractAction.INTERACT || this.action == InteractAction.INTERACT_AT) {
            out.writeVarInt(MagicValues.value(Integer.class, this.hand));
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
