// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.packet.status.client;

import org.spacehq.mc.protocol.util.ReflectionToString;
import org.spacehq.packetlib.io.NetOutput;
import java.io.IOException;
import org.spacehq.packetlib.io.NetInput;
import org.spacehq.packetlib.packet.Packet;

public class StatusQueryPacket implements Packet
{
    @Override
    public void read(final NetInput in) throws IOException {
    }
    
    @Override
    public void write(final NetOutput out) throws IOException {
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
