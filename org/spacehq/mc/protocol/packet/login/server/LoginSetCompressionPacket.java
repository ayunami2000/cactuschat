// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.packet.login.server;

import org.spacehq.mc.protocol.util.ReflectionToString;
import org.spacehq.packetlib.io.NetOutput;
import java.io.IOException;
import org.spacehq.packetlib.io.NetInput;
import org.spacehq.packetlib.packet.Packet;

public class LoginSetCompressionPacket implements Packet
{
    private int threshold;
    
    private LoginSetCompressionPacket() {
    }
    
    public LoginSetCompressionPacket(final int threshold) {
        this.threshold = threshold;
    }
    
    public int getThreshold() {
        return this.threshold;
    }
    
    @Override
    public void read(final NetInput in) throws IOException {
        this.threshold = in.readVarInt();
    }
    
    @Override
    public void write(final NetOutput out) throws IOException {
        out.writeVarInt(this.threshold);
    }
    
    @Override
    public boolean isPriority() {
        return true;
    }
    
    @Override
    public String toString() {
        return ReflectionToString.toString(this);
    }
}
