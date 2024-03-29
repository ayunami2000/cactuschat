// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.packet.ingame.server.entity;

import org.spacehq.mc.protocol.util.ReflectionToString;
import org.spacehq.packetlib.io.NetOutput;
import java.io.IOException;
import org.spacehq.packetlib.io.NetInput;
import org.spacehq.packetlib.packet.Packet;

public class ServerEntitySetPassengersPacket implements Packet
{
    private int entityId;
    private int[] passengerIds;
    
    private ServerEntitySetPassengersPacket() {
    }
    
    public ServerEntitySetPassengersPacket(final int entityId, final int... passengerIds) {
        this.entityId = entityId;
        this.passengerIds = passengerIds;
    }
    
    public int getEntityId() {
        return this.entityId;
    }
    
    public int[] getPassengerIds() {
        return this.passengerIds;
    }
    
    @Override
    public void read(final NetInput in) throws IOException {
        this.entityId = in.readVarInt();
        this.passengerIds = new int[in.readVarInt()];
        for (int index = 0; index < this.passengerIds.length; ++index) {
            this.passengerIds[index] = in.readVarInt();
        }
    }
    
    @Override
    public void write(final NetOutput out) throws IOException {
        out.writeVarInt(this.entityId);
        out.writeVarInt(this.passengerIds.length);
        for (final int entityId : this.passengerIds) {
            out.writeVarInt(entityId);
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
