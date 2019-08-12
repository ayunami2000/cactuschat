// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.packet.ingame.server.world;

import org.spacehq.mc.protocol.util.ReflectionToString;
import java.io.OutputStream;
import org.spacehq.packetlib.io.stream.StreamNetOutput;
import java.io.ByteArrayOutputStream;
import org.spacehq.packetlib.io.NetOutput;
import java.io.IOException;
import org.spacehq.mc.protocol.util.NetUtil;
import org.spacehq.opennbt.tag.builtin.CompoundTag;
import org.spacehq.packetlib.io.NetInput;
import org.spacehq.mc.protocol.data.game.chunk.Column;
import org.spacehq.packetlib.packet.Packet;

public class ServerChunkDataPacket implements Packet
{
    private Column column;
    
    private ServerChunkDataPacket() {
    }
    
    public ServerChunkDataPacket(final Column column) {
        this.column = column;
    }
    
    public Column getColumn() {
        return this.column;
    }
    
    @Override
    public void read(final NetInput in) throws IOException {
        final int x = in.readInt();
        final int z = in.readInt();
        final boolean fullChunk = in.readBoolean();
        final int chunkMask = in.readVarInt();
        final byte[] data = in.readBytes(in.readVarInt());
        final CompoundTag[] tileEntities = new CompoundTag[in.readVarInt()];
        for (int i = 0; i < tileEntities.length; ++i) {
            tileEntities[i] = NetUtil.readNBT(in);
        }
        this.column = NetUtil.readColumn(data, x, z, fullChunk, false, chunkMask, tileEntities);
    }
    
    @Override
    public void write(final NetOutput out) throws IOException {
        final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        final NetOutput netOut = new StreamNetOutput(byteOut);
        final int mask = NetUtil.writeColumn(netOut, this.column, this.column.hasBiomeData(), this.column.hasSkylight());
        out.writeInt(this.column.getX());
        out.writeInt(this.column.getZ());
        out.writeBoolean(this.column.hasBiomeData());
        out.writeVarInt(mask);
        out.writeVarInt(byteOut.size());
        out.writeBytes(byteOut.toByteArray(), byteOut.size());
        final ByteArrayOutputStream tileEntitiesByteOut = new ByteArrayOutputStream();
        final NetOutput tileEntitiesNetOut = new StreamNetOutput(tileEntitiesByteOut);
        for (final CompoundTag compoundTag : this.column.getTileEntities()) {
            NetUtil.writeNBT(tileEntitiesNetOut, compoundTag);
        }
        out.writeVarInt(this.column.getTileEntities().length);
        out.writeBytes(tileEntitiesByteOut.toByteArray(), tileEntitiesByteOut.size());
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
