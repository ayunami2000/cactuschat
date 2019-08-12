// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.data.game.entity.metadata;

import org.spacehq.opennbt.tag.builtin.CompoundTag;

public class ItemStack
{
    private int id;
    private int amount;
    private int data;
    private CompoundTag nbt;
    
    public ItemStack(final int id) {
        this(id, 1);
    }
    
    public ItemStack(final int id, final int amount) {
        this(id, amount, 0);
    }
    
    public ItemStack(final int id, final int amount, final int data) {
        this(id, amount, data, null);
    }
    
    public ItemStack(final int id, final int amount, final int data, final CompoundTag nbt) {
        this.id = id;
        this.amount = amount;
        this.data = data;
        this.nbt = nbt;
    }
    
    public int getId() {
        return this.id;
    }
    
    public int getAmount() {
        return this.amount;
    }
    
    public int getData() {
        return this.data;
    }
    
    public CompoundTag getNBT() {
        return this.nbt;
    }
    
    @Override
    public boolean equals(final Object o) {
        return this == o || (o instanceof ItemStack && this.id == ((ItemStack)o).id && this.amount == ((ItemStack)o).amount && this.data == ((ItemStack)o).data && ((this.nbt != null && ((ItemStack)o).nbt != null) || (this.nbt != null && this.nbt.equals(((ItemStack)o).nbt))));
    }
    
    @Override
    public int hashCode() {
        int result = this.id;
        result = 31 * result + this.amount;
        result = 31 * result + this.data;
        result = 31 * result + ((this.nbt != null) ? this.nbt.hashCode() : 0);
        return result;
    }
}
