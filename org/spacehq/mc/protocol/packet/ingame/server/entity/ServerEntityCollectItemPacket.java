// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.packet.ingame.server.entity;

import org.spacehq.mc.protocol.util.ReflectionToString;
import org.spacehq.packetlib.io.NetOutput;
import java.io.IOException;
import org.spacehq.packetlib.io.NetInput;
import org.spacehq.packetlib.packet.Packet;

public class ServerEntityCollectItemPacket implements Packet
{
    private int collectedEntityId;
    private int collectorEntityId;
    
    private ServerEntityCollectItemPacket() {
    }
    
    public ServerEntityCollectItemPacket(final int collectedEntityId, final int collectorEntityId) {
        this.collectedEntityId = collectedEntityId;
        this.collectorEntityId = collectorEntityId;
    }
    
    public int getCollectedEntityId() {
        return this.collectedEntityId;
    }
    
    public int getCollectorEntityId() {
        return this.collectorEntityId;
    }
    
    @Override
    public void read(final NetInput in) throws IOException {
        this.collectedEntityId = in.readVarInt();
        this.collectorEntityId = in.readVarInt();
    }
    
    @Override
    public void write(final NetOutput out) throws IOException {
        out.writeVarInt(this.collectedEntityId);
        out.writeVarInt(this.collectorEntityId);
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
