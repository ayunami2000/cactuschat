// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.packet.ingame.server.world;

import org.spacehq.mc.protocol.util.ReflectionToString;
import org.spacehq.packetlib.io.NetOutput;
import java.io.IOException;
import org.spacehq.mc.protocol.data.MagicValues;
import org.spacehq.mc.protocol.util.NetUtil;
import org.spacehq.packetlib.io.NetInput;
import org.spacehq.mc.protocol.data.game.entity.player.BlockBreakStage;
import org.spacehq.mc.protocol.data.game.entity.metadata.Position;
import org.spacehq.packetlib.packet.Packet;

public class ServerBlockBreakAnimPacket implements Packet
{
    private int breakerEntityId;
    private Position position;
    private BlockBreakStage stage;
    
    private ServerBlockBreakAnimPacket() {
    }
    
    public ServerBlockBreakAnimPacket(final int breakerEntityId, final Position position, final BlockBreakStage stage) {
        this.breakerEntityId = breakerEntityId;
        this.position = position;
        this.stage = stage;
    }
    
    public int getBreakerEntityId() {
        return this.breakerEntityId;
    }
    
    public Position getPosition() {
        return this.position;
    }
    
    public BlockBreakStage getStage() {
        return this.stage;
    }
    
    @Override
    public void read(final NetInput in) throws IOException {
        this.breakerEntityId = in.readVarInt();
        this.position = NetUtil.readPosition(in);
        try {
            this.stage = MagicValues.key(BlockBreakStage.class, in.readUnsignedByte());
        }
        catch (IllegalArgumentException e) {
            this.stage = BlockBreakStage.RESET;
        }
    }
    
    @Override
    public void write(final NetOutput out) throws IOException {
        out.writeVarInt(this.breakerEntityId);
        NetUtil.writePosition(out, this.position);
        out.writeByte(MagicValues.value(Integer.class, this.stage));
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
