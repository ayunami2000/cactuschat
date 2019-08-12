// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.util;

import org.spacehq.mc.protocol.data.game.chunk.NibbleArray3d;
import org.spacehq.mc.protocol.data.game.chunk.BlockStorage;
import org.spacehq.mc.protocol.data.game.chunk.Chunk;
import org.spacehq.packetlib.io.stream.StreamNetInput;
import java.io.ByteArrayInputStream;
import org.spacehq.mc.protocol.data.game.chunk.Column;
import java.util.UUID;
import java.util.List;
import org.spacehq.mc.protocol.data.game.world.block.BlockFace;
import org.spacehq.mc.protocol.data.message.Message;
import org.spacehq.mc.protocol.data.MagicValues;
import org.spacehq.mc.protocol.data.game.entity.metadata.MetadataType;
import java.util.ArrayList;
import org.spacehq.mc.protocol.data.game.entity.metadata.EntityMetadata;
import org.spacehq.mc.protocol.data.game.entity.metadata.Rotation;
import org.spacehq.mc.protocol.data.game.entity.metadata.Position;
import org.spacehq.mc.protocol.data.game.entity.metadata.ItemStack;
import org.spacehq.mc.protocol.data.game.world.block.BlockState;
import org.spacehq.opennbt.tag.builtin.Tag;
import java.io.OutputStream;
import java.io.DataOutputStream;
import org.spacehq.packetlib.io.NetOutput;
import java.io.IOException;
import org.spacehq.opennbt.NBTIO;
import java.io.InputStream;
import java.io.DataInputStream;
import org.spacehq.opennbt.tag.builtin.CompoundTag;
import org.spacehq.packetlib.io.NetInput;

public class NetUtil
{
    private static final int POSITION_X_SIZE = 38;
    private static final int POSITION_Y_SIZE = 26;
    private static final int POSITION_Z_SIZE = 38;
    private static final int POSITION_Y_SHIFT = 4095;
    private static final int POSITION_WRITE_SHIFT = 67108863;
    
    public static CompoundTag readNBT(final NetInput in) throws IOException {
        final byte b = in.readByte();
        if (b == 0) {
            return null;
        }
        return (CompoundTag)NBTIO.readTag(new DataInputStream(new NetInputStream(in, b)));
    }
    
    public static void writeNBT(final NetOutput out, final CompoundTag tag) throws IOException {
        if (tag == null) {
            out.writeByte(0);
        }
        else {
            NBTIO.writeTag(new DataOutputStream(new NetOutputStream(out)), tag);
        }
    }
    
    public static BlockState readBlockState(final NetInput in) throws IOException {
        final int rawId = in.readVarInt();
        return new BlockState(rawId >> 4, rawId & 0xF);
    }
    
    public static void writeBlockState(final NetOutput out, final BlockState blockState) throws IOException {
        out.writeVarInt(blockState.getId() << 4 | (blockState.getData() & 0xF));
    }
    
    public static ItemStack readItem(final NetInput in) throws IOException {
        final short item = in.readShort();
        if (item < 0) {
            return null;
        }
        return new ItemStack(item, in.readByte(), in.readShort(), readNBT(in));
    }
    
    public static void writeItem(final NetOutput out, final ItemStack item) throws IOException {
        if (item == null) {
            out.writeShort(-1);
        }
        else {
            out.writeShort(item.getId());
            out.writeByte(item.getAmount());
            out.writeShort(item.getData());
            writeNBT(out, item.getNBT());
        }
    }
    
    public static Position readPosition(final NetInput in) throws IOException {
        final long val = in.readLong();
        final int x = (int)(val >> 38);
        final int y = (int)(val >> 26 & 0xFFFL);
        final int z = (int)(val << 38 >> 38);
        return new Position(x, y, z);
    }
    
    public static void writePosition(final NetOutput out, final Position pos) throws IOException {
        final long x = pos.getX() & 0x3FFFFFF;
        final long y = pos.getY() & 0xFFF;
        final long z = pos.getZ() & 0x3FFFFFF;
        out.writeLong(x << 38 | y << 26 | z);
    }
    
    public static Rotation readRotation(final NetInput in) throws IOException {
        return new Rotation(in.readFloat(), in.readFloat(), in.readFloat());
    }
    
    public static void writeRotation(final NetOutput out, final Rotation rot) throws IOException {
        out.writeFloat(rot.getPitch());
        out.writeFloat(rot.getYaw());
        out.writeFloat(rot.getRoll());
    }
    
    public static EntityMetadata[] readEntityMetadata(final NetInput in) throws IOException {
        final List<EntityMetadata> ret = new ArrayList<EntityMetadata>();
        int id;
        while ((id = in.readUnsignedByte()) != 255) {
            final int typeId = in.readVarInt();
            final MetadataType type = MagicValues.key(MetadataType.class, typeId);
            Object value = null;
            switch (type) {
                case BYTE: {
                    value = in.readByte();
                    break;
                }
                case INT: {
                    value = in.readVarInt();
                    break;
                }
                case FLOAT: {
                    value = in.readFloat();
                    break;
                }
                case STRING: {
                    value = in.readString();
                    break;
                }
                case CHAT: {
                    value = Message.fromString(in.readString());
                    break;
                }
                case ITEM: {
                    value = readItem(in);
                    break;
                }
                case BOOLEAN: {
                    value = in.readBoolean();
                    break;
                }
                case ROTATION: {
                    value = readRotation(in);
                    break;
                }
                case POSITION: {
                    value = readPosition(in);
                    break;
                }
                case OPTIONAL_POSITION: {
                    final boolean positionPresent = in.readBoolean();
                    if (positionPresent) {
                        value = readPosition(in);
                        break;
                    }
                    break;
                }
                case BLOCK_FACE: {
                    value = MagicValues.key(BlockFace.class, in.readVarInt());
                    break;
                }
                case OPTIONAL_UUID: {
                    final boolean uuidPresent = in.readBoolean();
                    if (uuidPresent) {
                        value = in.readUUID();
                        break;
                    }
                    break;
                }
                case BLOCK_STATE: {
                    value = readBlockState(in);
                    break;
                }
                default: {
                    throw new IOException("Unknown metadata type id: " + typeId);
                }
            }
            ret.add(new EntityMetadata(id, type, value));
        }
        return ret.toArray(new EntityMetadata[ret.size()]);
    }
    
    public static void writeEntityMetadata(final NetOutput out, final EntityMetadata[] metadata) throws IOException {
        for (final EntityMetadata meta : metadata) {
            out.writeByte(meta.getId());
            out.writeVarInt(MagicValues.value(Integer.class, meta.getType()));
            switch (meta.getType()) {
                case BYTE: {
                    out.writeByte((byte)meta.getValue());
                    break;
                }
                case INT: {
                    out.writeVarInt((int)meta.getValue());
                    break;
                }
                case FLOAT: {
                    out.writeFloat((float)meta.getValue());
                    break;
                }
                case STRING: {
                    out.writeString((String)meta.getValue());
                    break;
                }
                case CHAT: {
                    out.writeString(((Message)meta.getValue()).toJsonString());
                    break;
                }
                case ITEM: {
                    writeItem(out, (ItemStack)meta.getValue());
                    break;
                }
                case BOOLEAN: {
                    out.writeBoolean((boolean)meta.getValue());
                    break;
                }
                case ROTATION: {
                    writeRotation(out, (Rotation)meta.getValue());
                    break;
                }
                case POSITION: {
                    writePosition(out, (Position)meta.getValue());
                    break;
                }
                case OPTIONAL_POSITION: {
                    out.writeBoolean(meta.getValue() != null);
                    if (meta.getValue() != null) {
                        writePosition(out, (Position)meta.getValue());
                        break;
                    }
                    break;
                }
                case BLOCK_FACE: {
                    out.writeVarInt(MagicValues.value(Integer.class, meta.getValue()));
                    break;
                }
                case OPTIONAL_UUID: {
                    out.writeBoolean(meta.getValue() != null);
                    if (meta.getValue() != null) {
                        out.writeUUID((UUID)meta.getValue());
                        break;
                    }
                    break;
                }
                case BLOCK_STATE: {
                    writeBlockState(out, (BlockState)meta.getValue());
                    break;
                }
                default: {
                    throw new IOException("Unknown metadata type: " + meta.getType());
                }
            }
        }
        out.writeByte(255);
    }
    
    public static Column readColumn(final byte[] data, final int x, final int z, final boolean fullChunk, final boolean hasSkylight, final int mask, final CompoundTag[] tileEntities) throws IOException {
        final NetInput in = new StreamNetInput(new ByteArrayInputStream(data));
        Exception ex = null;
        Column column = null;
        try {
            final Chunk[] chunks = new Chunk[16];
            for (int index = 0; index < chunks.length; ++index) {
                if ((mask & 1 << index) != 0x0) {
                    final BlockStorage blocks = new BlockStorage(in);
                    final NibbleArray3d blocklight = new NibbleArray3d(in, 2048);
                    final NibbleArray3d skylight = hasSkylight ? new NibbleArray3d(in, 2048) : null;
                    chunks[index] = new Chunk(blocks, blocklight, skylight);
                }
            }
            byte[] biomeData = null;
            if (fullChunk) {
                biomeData = in.readBytes(256);
            }
            column = new Column(x, z, chunks, biomeData, tileEntities);
        }
        catch (Exception e) {
            ex = e;
        }
        if ((in.available() > 0 || ex != null) && !hasSkylight) {
            return readColumn(data, x, z, fullChunk, true, mask, tileEntities);
        }
        if (ex != null) {
            throw new IOException("Failed to read chunk data.", ex);
        }
        return column;
    }
    
    public static int writeColumn(final NetOutput out, final Column column, final boolean fullChunk, final boolean hasSkylight) throws IOException {
        int mask = 0;
        final Chunk[] chunks = column.getChunks();
        for (int index = 0; index < chunks.length; ++index) {
            final Chunk chunk = chunks[index];
            if (chunk != null && (!fullChunk || !chunk.isEmpty())) {
                mask |= 1 << index;
                chunk.getBlocks().write(out);
                chunk.getBlockLight().write(out);
                if (hasSkylight) {
                    chunk.getSkyLight().write(out);
                }
            }
        }
        if (fullChunk) {
            out.writeBytes(column.getBiomeData());
        }
        return mask;
    }
    
    private static class NetInputStream extends InputStream
    {
        private NetInput in;
        private boolean readFirst;
        private byte firstByte;
        
        public NetInputStream(final NetInput in, final byte firstByte) {
            this.in = in;
            this.firstByte = firstByte;
        }
        
        @Override
        public int read() throws IOException {
            if (!this.readFirst) {
                this.readFirst = true;
                return this.firstByte;
            }
            return this.in.readUnsignedByte();
        }
    }
    
    private static class NetOutputStream extends OutputStream
    {
        private NetOutput out;
        
        public NetOutputStream(final NetOutput out) {
            this.out = out;
        }
        
        @Override
        public void write(final int b) throws IOException {
            this.out.writeByte(b);
        }
    }
}
