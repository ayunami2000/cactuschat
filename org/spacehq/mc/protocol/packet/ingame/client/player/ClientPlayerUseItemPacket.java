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
import org.spacehq.packetlib.packet.Packet;

public class ClientPlayerUseItemPacket implements Packet
{
    private Hand hand;
    
    private ClientPlayerUseItemPacket() {
    }
    
    public ClientPlayerUseItemPacket(final Hand hand) {
        this.hand = hand;
    }
    
    public Hand getHand() {
        return this.hand;
    }
    
    @Override
    public void read(final NetInput in) throws IOException {
        this.hand = MagicValues.key(Hand.class, in.readVarInt());
    }
    
    @Override
    public void write(final NetOutput out) throws IOException {
        out.writeVarInt(MagicValues.value(Integer.class, this.hand));
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
