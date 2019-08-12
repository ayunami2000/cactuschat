// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.packet.ingame.client.player;

import org.spacehq.mc.protocol.util.ReflectionToString;
import org.spacehq.packetlib.io.NetOutput;
import java.io.IOException;
import org.spacehq.packetlib.io.NetInput;
import org.spacehq.packetlib.packet.Packet;

public class ClientPlayerChangeHeldItemPacket implements Packet
{
    private int slot;
    
    private ClientPlayerChangeHeldItemPacket() {
    }
    
    public ClientPlayerChangeHeldItemPacket(final int slot) {
        this.slot = slot;
    }
    
    public int getSlot() {
        return this.slot;
    }
    
    @Override
    public void read(final NetInput in) throws IOException {
        this.slot = in.readShort();
    }
    
    @Override
    public void write(final NetOutput out) throws IOException {
        out.writeShort(this.slot);
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
