// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.data.game.entity.type.object;

public class ProjectileData implements ObjectData
{
    private int ownerId;
    
    public ProjectileData(final int ownerId) {
        this.ownerId = ownerId;
    }
    
    public int getOwnerId() {
        return this.ownerId;
    }
    
    @Override
    public boolean equals(final Object o) {
        return this == o || (o instanceof ProjectileData && this.ownerId == ((ProjectileData)o).ownerId);
    }
    
    @Override
    public int hashCode() {
        return this.ownerId;
    }
}
