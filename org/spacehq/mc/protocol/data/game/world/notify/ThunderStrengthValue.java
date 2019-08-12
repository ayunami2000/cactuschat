// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.data.game.world.notify;

public class ThunderStrengthValue implements ClientNotificationValue
{
    private float strength;
    
    public ThunderStrengthValue(float strength) {
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
        return o instanceof ThunderStrengthValue && Float.compare(this.strength, ((ThunderStrengthValue)o).strength) == 0;
    }
    
    @Override
    public int hashCode() {
        return (this.strength != 0.0f) ? Float.floatToIntBits(this.strength) : 0;
    }
}
