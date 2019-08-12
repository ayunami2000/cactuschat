// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.data.game.world.effect;

public class BreakPotionEffectData implements WorldEffectData
{
    private int potionId;
    
    public BreakPotionEffectData(final int potionId) {
        this.potionId = potionId;
    }
    
    public int getPotionId() {
        return this.potionId;
    }
    
    @Override
    public boolean equals(final Object o) {
        return o instanceof BreakPotionEffectData && this.potionId == ((BreakPotionEffectData)o).potionId;
    }
    
    @Override
    public int hashCode() {
        return this.potionId;
    }
}
