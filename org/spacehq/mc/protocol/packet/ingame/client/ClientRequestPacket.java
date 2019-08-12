// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.packet.ingame.client;

import org.spacehq.mc.protocol.util.ReflectionToString;
import org.spacehq.packetlib.io.NetOutput;
import java.io.IOException;
import org.spacehq.mc.protocol.data.MagicValues;
import org.spacehq.packetlib.io.NetInput;
import org.spacehq.mc.protocol.data.game.ClientRequest;
import org.spacehq.packetlib.packet.Packet;

public class ClientRequestPacket implements Packet
{
    private ClientRequest request;
    
    private ClientRequestPacket() {
    }
    
    public ClientRequestPacket(final ClientRequest request) {
        this.request = request;
    }
    
    public ClientRequest getRequest() {
        return this.request;
    }
    
    @Override
    public void read(final NetInput in) throws IOException {
        this.request = MagicValues.key(ClientRequest.class, in.readVarInt());
    }
    
    @Override
    public void write(final NetOutput out) throws IOException {
        out.writeVarInt(MagicValues.value(Integer.class, this.request));
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
