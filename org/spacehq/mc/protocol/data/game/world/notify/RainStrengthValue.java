// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.data.game.world.notify;

public class RainStrengthValue implements ClientNotificationValue
{
    private float strength;
    
    public RainStrengthValue(float strength) {
        if (strength > 1.0f) {
            strength = 1.0f;
        }
        if (strength < 0.0f) {
            strength = 0.0f;
        }
        this.strength = strength;
    }
    
    public float getStrength() {
        return this.strength;
    }
    
    @Override
    public boolean equals(final Object o) {
        return o instanceof RainStrengthValue && Float.compare(this.strength, ((RainStrengthValue)o).strength) == 0;
    }
    
    @Override
    public int hashCode() {
        return (this.strength != 0.0f) ? Float.floatToIntBits(this.strength) : 0;
    }
}
