// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.data.game.world.effect;

public class BonemealGrowEffectData implements WorldEffectData
{
    private int particleCount;
    
    public BonemealGrowEffectData(final int particleCount) {
        this.particleCount = particleCount;
    }
    
    public int getParticleCount() {
        return this.particleCount;
    }
    
    @Override
    public boolean equals(final Object o) {
        return o instanceof BonemealGrowEffectData && this.particleCount == ((BonemealGrowEffectData)o).particleCount;
    }
    
    @Override
    public int hashCode() {
        return this.particleCount;
    }
}
