// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.packet.ingame.server.entity;

import org.spacehq.mc.protocol.util.ReflectionToString;
import org.spacehq.packetlib.io.NetOutput;
import java.io.IOException;
import org.spacehq.packetlib.io.NetInput;
import org.spacehq.packetlib.packet.Packet;

public class ServerEntityAttachPacket implements Packet
{
    private int entityId;
    private int attachedToId;
    
    private ServerEntityAttachPacket() {
    }
    
    public ServerEntityAttachPacket(final int entityId, final int attachedToId) {
        this.entityId = entityId;
        this.attachedToId = attachedToId;
    }
    
    public int getEntityId() {
        return this.entityId;
    }
    
    public int getAttachedToId() {
        return this.attachedToId;
    }
    
    @Override
    public void read(final NetInput in) throws IOException {
        this.entityId = in.readInt();
        this.attachedToId = in.readInt();
    }
    
    @Override
    public void write(final NetOutput out) throws IOException {
        out.writeInt(this.entityId);
        out.writeInt(this.attachedToId);
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
