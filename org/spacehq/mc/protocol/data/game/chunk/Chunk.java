// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.data.game.chunk;

public class Chunk
{
    private BlockStorage blocks;
    private NibbleArray3d blocklight;
    private NibbleArray3d skylight;
    
    public Chunk(final boolean skylight) {
        this(new BlockStorage(), new NibbleArray3d(4096), skylight ? new NibbleArray3d(4096) : null);
    }
    
    public Chunk(final BlockStorage blocks, final NibbleArray3d blocklight, final NibbleArray3d skylight) {
        this.blocks = blocks;
        this.blocklight = blocklight;
        this.skylight = skylight;
    }
    
    public BlockStorage getBlocks() {
        return this.blocks;
    }
    
    public NibbleArray3d getBlockLight() {
        return this.blocklight;
    }
    
    public NibbleArray3d getSkyLight() {
        return this.skylight;
    }
    
    public boolean isEmpty() {
        return this.blocks.isEmpty();
    }
    
    @Override
    public boolean equals(final Object o) {
        return this == o || (o instanceof Chunk && this.blocks.equals(((Chunk)o).blocks) && this.blocklight.equals(((Chunk)o).blocklight) && ((this.skylight == null && ((Chunk)o).skylight == null) || (this.skylight != null && this.skylight.equals(((Chunk)o).skylight))));
    }
    
    @Override
    public int hashCode() {
        int result = this.blocks.hashCode();
        result = 31 * result + this.blocklight.hashCode();
        result = 31 * result + ((this.skylight != null) ? this.skylight.hashCode() : 0);
        return result;
    }
}
