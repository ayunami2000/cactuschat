// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.packet.ingame.client.player;

import org.spacehq.mc.protocol.util.ReflectionToString;
import org.spacehq.packetlib.io.NetOutput;
import java.io.IOException;
import org.spacehq.mc.protocol.util.NetUtil;
import org.spacehq.mc.protocol.data.MagicValues;
import org.spacehq.packetlib.io.NetInput;
import org.spacehq.mc.protocol.data.game.world.block.BlockFace;
import org.spacehq.mc.protocol.data.game.entity.metadata.Position;
import org.spacehq.mc.protocol.data.game.entity.player.PlayerAction;
import org.spacehq.packetlib.packet.Packet;

public class ClientPlayerActionPacket implements Packet
{
    private PlayerAction action;
    private Position position;
    private BlockFace face;
    
    private ClientPlayerActionPacket() {
    }
    
    public ClientPlayerActionPacket(final PlayerAction action, final Position position, final BlockFace face) {
        this.action = action;
        this.position = position;
        this.face = face;
    }
    
    public PlayerAction getAction() {
        return this.action;
    }
    
    public Position getPosition() {
        return this.position;
    }
    
    public BlockFace getFace() {
        return this.face;
    }
    
    @Override
    public void read(final NetInput in) throws IOException {
        this.action = MagicValues.key(PlayerAction.class, in.readVarInt());
        this.position = NetUtil.readPosition(in);
        this.face = MagicValues.key(BlockFace.class, in.readUnsignedByte());
    }
    
    @Override
    public void write(final NetOutput out) throws IOException {
        out.writeVarInt(MagicValues.value(Integer.class, this.action));
        NetUtil.writePosition(out, this.position);
        out.writeByte(MagicValues.value(Integer.class, this.face));
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
