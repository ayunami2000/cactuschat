// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.data.game.world.effect;

public class RecordEffectData implements WorldEffectData
{
    private int recordId;
    
    public RecordEffectData(final int recordId) {
        this.recordId = recordId;
    }
    
    public int getRecordId() {
        return this.recordId;
    }
    
    @Override
    public boolean equals(final Object o) {
        return o instanceof RecordEffectData && this.recordId == ((RecordEffectData)o).recordId;
    }
    
    @Override
    public int hashCode() {
        return this.recordId;
    }
}
