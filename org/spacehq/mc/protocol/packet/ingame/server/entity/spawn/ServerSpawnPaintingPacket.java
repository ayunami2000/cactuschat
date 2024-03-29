// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.packet.ingame.server.entity.spawn;

import org.spacehq.mc.protocol.util.ReflectionToString;
import org.spacehq.packetlib.io.NetOutput;
import java.io.IOException;
import org.spacehq.mc.protocol.util.NetUtil;
import org.spacehq.mc.protocol.data.MagicValues;
import org.spacehq.packetlib.io.NetInput;
import org.spacehq.mc.protocol.data.game.entity.type.object.HangingDirection;
import org.spacehq.mc.protocol.data.game.entity.metadata.Position;
import org.spacehq.mc.protocol.data.game.entity.type.PaintingType;
import java.util.UUID;
import org.spacehq.packetlib.packet.Packet;

public class ServerSpawnPaintingPacket implements Packet
{
    private int entityId;
    private UUID uuid;
    private PaintingType paintingType;
    private Position position;
    private HangingDirection direction;
    
    private ServerSpawnPaintingPacket() {
    }
    
    public ServerSpawnPaintingPacket(final int entityId, final UUID uuid, final PaintingType paintingType, final Position position, final HangingDirection direction) {
        this.entityId = entityId;
        this.uuid = uuid;
        this.paintingType = paintingType;
        this.position = position;
        this.direction = direction;
    }
    
    public int getEntityId() {
        return this.entityId;
    }
    
    public UUID getUUID() {
        return this.uuid;
    }
    
    public PaintingType getPaintingType() {
        return this.paintingType;
    }
    
    public Position getPosition() {
        return this.position;
    }
    
    public HangingDirection getDirection() {
        return this.direction;
    }
    
    @Override
    public void read(final NetInput in) throws IOException {
        this.entityId = in.readVarInt();
        this.uuid = in.readUUID();
        this.paintingType = MagicValues.key(PaintingType.class, in.readString());
        this.position = NetUtil.readPosition(in);
        this.direction = MagicValues.key(HangingDirection.class, in.readUnsignedByte());
    }
    
    @Override
    public void write(final NetOutput out) throws IOException {
        out.writeVarInt(this.entityId);
        out.writeUUID(this.uuid);
        out.writeString(MagicValues.value(String.class, this.paintingType));
        NetUtil.writePosition(out, this.position);
        out.writeByte(MagicValues.value(Integer.class, this.direction));
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
