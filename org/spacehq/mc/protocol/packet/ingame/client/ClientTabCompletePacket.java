// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.packet.ingame.client;

import org.spacehq.mc.protocol.util.ReflectionToString;
import org.spacehq.packetlib.io.NetOutput;
import java.io.IOException;
import org.spacehq.mc.protocol.util.NetUtil;
import org.spacehq.packetlib.io.NetInput;
import org.spacehq.mc.protocol.data.game.entity.metadata.Position;
import org.spacehq.packetlib.packet.Packet;

public class ClientTabCompletePacket implements Packet
{
    private String text;
    private boolean assumeCommand;
    private Position lookingAt;
    
    private ClientTabCompletePacket() {
    }
    
    public ClientTabCompletePacket(final String text, final boolean assumeCommand) {
        this(text, assumeCommand, null);
    }
    
    public ClientTabCompletePacket(final String text, final boolean assumeCommand, final Position lookingAt) {
        this.text = text;
        this.assumeCommand = assumeCommand;
        this.lookingAt = lookingAt;
    }
    
    public String getText() {
        return this.text;
    }
    
    public boolean getAssumeCommand() {
        return this.assumeCommand;
    }
    
    public Position getLookingAt() {
        return this.lookingAt;
    }
    
    @Override
    public void read(final NetInput in) throws IOException {
        this.text = in.readString();
        this.assumeCommand = in.readBoolean();
        this.lookingAt = (in.readBoolean() ? NetUtil.readPosition(in) : null);
    }
    
    @Override
    public void write(final NetOutput out) throws IOException {
        out.writeString(this.text);
        out.writeBoolean(this.assumeCommand);
        out.writeBoolean(this.lookingAt != null);
        if (this.lookingAt != null) {
            NetUtil.writePosition(out, this.lookingAt);
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
