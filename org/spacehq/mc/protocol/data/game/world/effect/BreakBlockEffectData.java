// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.data.game.world.effect;

import org.spacehq.mc.protocol.data.game.world.block.BlockState;

public class BreakBlockEffectData implements WorldEffectData
{
    private BlockState blockState;
    
    public BreakBlockEffectData(final BlockState blockState) {
        this.blockState = blockState;
    }
    
    public BlockState getBlockState() {
        return this.blockState;
    }
    
    @Override
    public boolean equals(final Object o) {
        return o instanceof BreakBlockEffectData && this.blockState.equals(((BreakBlockEffectData)o).blockState);
    }
    
    @Override
    public int hashCode() {
        return this.blockState.hashCode();
    }
}
