// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.packet.ingame.server.world;

import org.spacehq.mc.protocol.util.ReflectionToString;
import org.spacehq.packetlib.io.NetOutput;
import java.io.IOException;
import org.spacehq.packetlib.io.NetInput;
import org.spacehq.packetlib.packet.Packet;

public class ServerUnloadChunkPacket implements Packet
{
    private int x;
    private int z;
    
    private ServerUnloadChunkPacket() {
    }
    
    public ServerUnloadChunkPacket(final int x, final int z) {
        this.x = x;
        this.z = z;
    }
    
    public int getX() {
        return this.x;
    }
    
    public int getZ() {
        return this.z;
    }
    
    @Override
    public void read(final NetInput in) throws IOException {
        this.x = in.readInt();
        this.z = in.readInt();
    }
    
    @Override
    public void write(final NetOutput out) throws IOException {
        out.writeInt(this.x);
        out.writeInt(this.z);
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
