// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.data.game.chunk;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import org.spacehq.packetlib.io.NetOutput;
import java.io.IOException;
import org.spacehq.mc.protocol.util.NetUtil;
import org.spacehq.packetlib.io.NetInput;
import java.util.ArrayList;
import java.util.List;
import org.spacehq.mc.protocol.data.game.world.block.BlockState;

public class BlockStorage
{
    private static final BlockState AIR;
    private int bitsPerEntry;
    private List<BlockState> states;
    private FlexibleStorage storage;
    
    public BlockStorage() {
        this.bitsPerEntry = 4;
        (this.states = new ArrayList<BlockState>()).add(BlockStorage.AIR);
        this.storage = new FlexibleStorage(this.bitsPerEntry, 4096);
    }
    
    public BlockStorage(final NetInput in) throws IOException {
        this.bitsPerEntry = in.readUnsignedByte();
        this.states = new ArrayList<BlockState>();
        for (int stateCount = in.readVarInt(), i = 0; i < stateCount; ++i) {
            this.states.add(NetUtil.readBlockState(in));
        }
        this.storage = new FlexibleStorage(this.bitsPerEntry, in.readLongs(in.readVarInt()));
    }
    
    public void write(final NetOutput out) throws IOException {
        out.writeByte(this.bitsPerEntry);
        out.writeVarInt(this.states.size());
        for (final BlockState state : this.states) {
            NetUtil.writeBlockState(out, state);
        }
        final long[] data = this.storage.getData();
        out.writeVarInt(data.length);
        out.writeLongs(data);
    }
    
    public int getBitsPerEntry() {
        return this.bitsPerEntry;
    }
    
    public List<BlockState> getStates() {
        return Collections.unmodifiableList((List<? extends BlockState>)this.states);
    }
    
    public FlexibleStorage getStorage() {
        return this.storage;
    }
    
    public BlockState get(final int x, final int y, final int z) {
        final int id = this.storage.get(index(x, y, z));
        return (this.bitsPerEntry <= 8) ? ((id >= 0 && id < this.states.size()) ? this.states.get(id) : BlockStorage.AIR) : rawToState(id);
    }
    
    public void set(final int x, final int y, final int z, final BlockState state) {
        int id = (this.bitsPerEntry <= 8) ? this.states.indexOf(state) : stateToRaw(state);
        if (id == -1) {
            this.states.add(state);
            if (this.states.size() > 1 << this.bitsPerEntry) {
                ++this.bitsPerEntry;
                List<BlockState> oldStates = this.states;
                if (this.bitsPerEntry > 8) {
                    oldStates = new ArrayList<BlockState>(this.states);
                    this.states.clear();
                    this.bitsPerEntry = 13;
                }
                final FlexibleStorage oldStorage = this.storage;
                this.storage = new FlexibleStorage(this.bitsPerEntry, this.storage.getSize());
                for (int index = 0; index < this.storage.getSize(); ++index) {
                    this.storage.set(index, (this.bitsPerEntry <= 8) ? oldStorage.get(index) : stateToRaw(oldStates.get(index)));
                }
            }
            id = ((this.bitsPerEntry <= 8) ? this.states.indexOf(state) : stateToRaw(state));
        }
        this.storage.set(index(x, y, z), id);
    }
    
    public boolean isEmpty() {
        for (int index = 0; index < this.storage.getSize(); ++index) {
            if (this.storage.get(index) != 0) {
                return false;
            }
        }
        return true;
    }
    
    private static int index(final int x, final int y, final int z) {
        return y << 8 | z << 4 | x;
    }
    
    private static BlockState rawToState(final int raw) {
        return new BlockState(raw >> 4, raw & 0xF);
    }
    
    private static int stateToRaw(final BlockState state) {
        return state.getId() << 4 | (state.getData() & 0xF);
    }
    
    @Override
    public boolean equals(final Object o) {
        return o instanceof BlockStorage && this.bitsPerEntry == ((BlockStorage)o).bitsPerEntry && this.states.equals(((BlockStorage)o).states) && this.storage.equals(((BlockStorage)o).storage);
    }
    
    @Override
    public int hashCode() {
        int result = this.bitsPerEntry;
        result = 31 * result + this.states.hashCode();
        result = 31 * result + this.storage.hashCode();
        return result;
    }
    
    static {
        AIR = new BlockState(0, 0);
    }
}
