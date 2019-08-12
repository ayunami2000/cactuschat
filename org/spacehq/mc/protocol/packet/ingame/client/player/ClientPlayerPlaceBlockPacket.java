// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.packet.ingame.client.player;

import org.spacehq.mc.protocol.util.ReflectionToString;
import org.spacehq.packetlib.io.NetOutput;
import java.io.IOException;
import org.spacehq.mc.protocol.data.MagicValues;
import org.spacehq.mc.protocol.util.NetUtil;
import org.spacehq.packetlib.io.NetInput;
import org.spacehq.mc.protocol.data.game.entity.player.Hand;
import org.spacehq.mc.protocol.data.game.world.block.BlockFace;
import org.spacehq.mc.protocol.data.game.entity.metadata.Position;
import org.spacehq.packetlib.packet.Packet;

public class ClientPlayerPlaceBlockPacket implements Packet
{
    private Position position;
    private BlockFace face;
    private Hand hand;
    private float cursorX;
    private float cursorY;
    private float cursorZ;
    
    private ClientPlayerPlaceBlockPacket() {
    }
    
    public ClientPlayerPlaceBlockPacket(final Position position, final BlockFace face, final Hand hand, final float cursorX, final float cursorY, final float cursorZ) {
        this.position = position;
        this.face = face;
        this.hand = hand;
        this.cursorX = cursorX;
        this.cursorY = cursorY;
        this.cursorZ = cursorZ;
    }
    
    public Position getPosition() {
        return this.position;
    }
    
    public BlockFace getFace() {
        return this.face;
    }
    
    public Hand getHand() {
        return this.hand;
    }
    
    public float getCursorX() {
        return this.cursorX;
    }
    
    public float getCursorY() {
        return this.cursorY;
    }
    
    public float getCursorZ() {
        return this.cursorZ;
    }
    
    @Override
    public void read(final NetInput in) throws IOException {
        this.position = NetUtil.readPosition(in);
        this.face = MagicValues.key(BlockFace.class, in.readVarInt());
        this.hand = MagicValues.key(Hand.class, in.readVarInt());
        this.cursorX = in.readUnsignedByte() / 16.0f;
        this.cursorY = in.readUnsignedByte() / 16.0f;
        this.cursorZ = in.readUnsignedByte() / 16.0f;
    }
    
    @Override
    public void write(final NetOutput out) throws IOException {
        NetUtil.writePosition(out, this.position);
        out.writeVarInt(MagicValues.value(Integer.class, this.face));
        out.writeVarInt(MagicValues.value(Integer.class, this.hand));
        out.writeByte((int)(this.cursorX * 16.0f));
        out.writeByte((int)(this.cursorY * 16.0f));
        out.writeByte((int)(this.cursorZ * 16.0f));
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
