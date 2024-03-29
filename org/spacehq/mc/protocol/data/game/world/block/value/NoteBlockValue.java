// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.data.game.world.block.value;

public class NoteBlockValue implements BlockValue
{
    private int pitch;
    
    public NoteBlockValue(final int pitch) {
        if (pitch < 0 || pitch > 24) {
            throw new IllegalArgumentException("Pitch must be between 0 and 24.");
        }
        this.pitch = pitch;
    }
    
    public int getPitch() {
        return this.pitch;
    }
    
    @Override
    public boolean equals(final Object o) {
        return o instanceof NoteBlockValue && this.pitch == ((NoteBlockValue)o).pitch;
    }
    
    @Override
    public int hashCode() {
        return this.pitch;
    }
}
