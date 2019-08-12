// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.data.game.world.block;

public class BlockState
{
    private int id;
    private int data;
    
    public BlockState(final int id, final int data) {
        this.id = id;
        this.data = data;
    }
    
    public int getId() {
        return this.id;
    }
    
    public int getData() {
        return this.data;
    }
    
    @Override
    public boolean equals(final Object o) {
        return o instanceof BlockState && this.id == ((BlockState)o).id && this.data == ((BlockState)o).data;
    }
    
    @Override
    public int hashCode() {
        int result = this.id;
        result = 31 * result + this.data;
        return result;
    }
}
