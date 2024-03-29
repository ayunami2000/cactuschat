// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.data.game.world.block;

public class ExplodedBlockRecord
{
    private int x;
    private int y;
    private int z;
    
    public ExplodedBlockRecord(final int x, final int y, final int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public int getX() {
        return this.x;
    }
    
    public int getY() {
        return this.y;
    }
    
    public int getZ() {
        return this.z;
    }
    
    @Override
    public boolean equals(final Object o) {
        return o instanceof ExplodedBlockRecord && this.x == ((ExplodedBlockRecord)o).x && this.y == ((ExplodedBlockRecord)o).y && this.z == ((ExplodedBlockRecord)o).z;
    }
    
    @Override
    public int hashCode() {
        int result = this.x;
        result = 31 * result + this.y;
        result = 31 * result + this.z;
        return result;
    }
}
