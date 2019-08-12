// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.packet.ingame.client.world;

import org.spacehq.mc.protocol.util.ReflectionToString;
import org.spacehq.packetlib.io.NetOutput;
import java.io.IOException;
import org.spacehq.packetlib.io.NetInput;
import org.spacehq.packetlib.packet.Packet;

public class ClientSteerBoatPacket implements Packet
{
    private boolean unk1;
    private boolean unk2;
    
    private ClientSteerBoatPacket() {
    }
    
    public ClientSteerBoatPacket(final boolean unk1, final boolean unk2) {
        this.unk1 = unk1;
        this.unk2 = unk2;
    }
    
    public boolean getUnknown1() {
        return this.unk1;
    }
    
    public boolean getUnknown2() {
        return this.unk2;
    }
    
    @Override
    public void read(final NetInput in) throws IOException {
        this.unk1 = in.readBoolean();
        this.unk2 = in.readBoolean();
    }
    
    @Override
    public void write(final NetOutput out) throws IOException {
        out.writeBoolean(this.unk1);
        out.writeBoolean(this.unk2);
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
