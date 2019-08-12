// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.packet.ingame.server.scoreboard;

import org.spacehq.mc.protocol.util.ReflectionToString;
import org.spacehq.packetlib.io.NetOutput;
import java.io.IOException;
import org.spacehq.mc.protocol.data.MagicValues;
import org.spacehq.packetlib.io.NetInput;
import org.spacehq.mc.protocol.data.game.scoreboard.ScoreboardAction;
import org.spacehq.packetlib.packet.Packet;

public class ServerUpdateScorePacket implements Packet
{
    private String entry;
    private ScoreboardAction action;
    private String objective;
    private int value;
    
    private ServerUpdateScorePacket() {
    }
    
    public ServerUpdateScorePacket(final String entry, final String objective) {
        this.entry = entry;
        this.objective = objective;
        this.action = ScoreboardAction.REMOVE;
    }
    
    public ServerUpdateScorePacket(final String entry, final String objective, final int value) {
        this.entry = entry;
        this.objective = objective;
        this.value = value;
        this.action = ScoreboardAction.ADD_OR_UPDATE;
    }
    
    public String getEntry() {
        return this.entry;
    }
    
    public ScoreboardAction getAction() {
        return this.action;
    }
    
    public String getObjective() {
        return this.objective;
    }
    
    public int getValue() {
        return this.value;
    }
    
    @Override
    public void read(final NetInput in) throws IOException {
        this.entry = in.readString();
        this.action = MagicValues.key(ScoreboardAction.class, in.readVarInt());
        this.objective = in.readString();
        if (this.action == ScoreboardAction.ADD_OR_UPDATE) {
            this.value = in.readVarInt();
        }
    }
    
    @Override
    public void write(final NetOutput out) throws IOException {
        out.writeString(this.entry);
        out.writeVarInt(MagicValues.value(Integer.class, this.action));
        out.writeString(this.objective);
        if (this.action == ScoreboardAction.ADD_OR_UPDATE) {
            out.writeVarInt(this.value);
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
