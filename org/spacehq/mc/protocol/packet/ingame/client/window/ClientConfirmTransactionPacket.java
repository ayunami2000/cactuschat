// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.packet.ingame.client.window;

import org.spacehq.mc.protocol.util.ReflectionToString;
import org.spacehq.packetlib.io.NetOutput;
import java.io.IOException;
import org.spacehq.packetlib.io.NetInput;
import org.spacehq.packetlib.packet.Packet;

public class ClientConfirmTransactionPacket implements Packet
{
    private int windowId;
    private int actionId;
    private boolean accepted;
    
    private ClientConfirmTransactionPacket() {
    }
    
    public ClientConfirmTransactionPacket(final int windowId, final int actionId, final boolean accepted) {
        this.windowId = windowId;
        this.actionId = actionId;
        this.accepted = accepted;
    }
    
    public int getWindowId() {
        return this.windowId;
    }
    
    public int getActionId() {
        return this.actionId;
    }
    
    public boolean getAccepted() {
        return this.accepted;
    }
    
    @Override
    public void read(final NetInput in) throws IOException {
        this.windowId = in.readByte();
        this.actionId = in.readShort();
        this.accepted = in.readBoolean();
    }
    
    @Override
    public void write(final NetOutput out) throws IOException {
        out.writeByte(this.windowId);
        out.writeShort(this.actionId);
        out.writeBoolean(this.accepted);
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
