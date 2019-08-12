// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.data.game.world.sound;

public class CustomSound implements Sound
{
    private String name;
    
    public CustomSound(final String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
    
    @Override
    public boolean equals(final Object o) {
        return o instanceof CustomSound && this.name.equals(((CustomSound)o).name);
    }
    
    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
}
