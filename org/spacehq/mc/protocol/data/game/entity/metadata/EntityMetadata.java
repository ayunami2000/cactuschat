// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.data.game.entity.metadata;

import org.spacehq.mc.protocol.util.ReflectionToString;

public class EntityMetadata
{
    private int id;
    private MetadataType type;
    private Object value;
    
    public EntityMetadata(final int id, final MetadataType type, final Object value) {
        this.id = id;
        this.type = type;
        this.value = value;
    }
    
    public int getId() {
        return this.id;
    }
    
    public MetadataType getType() {
        return this.type;
    }
    
    public Object getValue() {
        return this.value;
    }
    
    @Override
    public boolean equals(final Object o) {
        return this == o || (o instanceof EntityMetadata && this.id == ((EntityMetadata)o).id && this.type == ((EntityMetadata)o).type && this.value.equals(((EntityMetadata)o).value));
    }
    
    @Override
    public int hashCode() {
        int result = this.id;
        result = 31 * result + this.type.hashCode();
        result = 31 * result + this.value.hashCode();
        return result;
    }
    
    @Override
    public String toString() {
        return ReflectionToString.toString(this);
    }
}
