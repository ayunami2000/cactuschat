// 
// Decompiled by Procyon v0.5.36
// 

package org.spacehq.mc.protocol.data.game.chunk;

import java.util.Arrays;

public class FlexibleStorage
{
    private final long[] data;
    private final int bitsPerEntry;
    private final int size;
    private final long maxEntryValue;
    
    public FlexibleStorage(final int bitsPerEntry, final int size) {
        this(bitsPerEntry, new long[roundToNearest(size * bitsPerEntry, 64) / 64]);
    }
    
    public FlexibleStorage(final int bitsPerEntry, final long[] data) {
        if (bitsPerEntry < 1 || bitsPerEntry > 32) {
            throw new IllegalArgumentException("BitsPerEntry cannot be outside of accepted range.");
        }
        this.bitsPerEntry = bitsPerEntry;
        this.data = data;
        this.size = this.data.length * 64 / this.bitsPerEntry;
        this.maxEntryValue = (1L << this.bitsPerEntry) - 1L;
    }
    
    public long[] getData() {
        return this.data;
    }
    
    public int getBitsPerEntry() {
        return this.bitsPerEntry;
    }
    
    public int getSize() {
        return this.size;
    }
    
    public int get(final int index) {
        if (index < 0 || index > this.size - 1) {
            throw new IndexOutOfBoundsException();
        }
        final int bitIndex = index * this.bitsPerEntry;
        final int startIndex = bitIndex / 64;
        final int endIndex = ((index + 1) * this.bitsPerEntry - 1) / 64;
        final int startBitSubIndex = bitIndex % 64;
        if (startIndex == endIndex) {
            return (int)(this.data[startIndex] >>> startBitSubIndex & this.maxEntryValue);
        }
        final int endBitSubIndex = 64 - startBitSubIndex;
        return (int)((this.data[startIndex] >>> startBitSubIndex | this.data[endIndex] << endBitSubIndex) & this.maxEntryValue);
    }
    
    public void set(final int index, final int value) {
        if (index < 0 || index > this.size - 1) {
            throw new IndexOutOfBoundsException();
        }
        if (value < 0 || value > this.maxEntryValue) {
            throw new IllegalArgumentException("Value cannot be outside of accepted range.");
        }
        final int bitIndex = index * this.bitsPerEntry;
        final int startIndex = bitIndex / 64;
        final int endIndex = ((index + 1) * this.bitsPerEntry - 1) / 64;
        final int startBitSubIndex = bitIndex % 64;
        this.data[startIndex] = ((this.data[startIndex] & ~(this.maxEntryValue << startBitSubIndex)) | ((long)value & this.maxEntryValue) << startBitSubIndex);
        if (startIndex != endIndex) {
            final int endBitSubIndex = 64 - startBitSubIndex;
            this.data[endIndex] = (this.data[endIndex] >>> endBitSubIndex << endBitSubIndex | ((long)value & this.maxEntryValue) >> endBitSubIndex);
        }
    }
    
    private static int roundToNearest(final int value, int roundTo) {
        if (roundTo == 0) {
            return 0;
        }
        if (value == 0) {
            return roundTo;
        }
        if (value < 0) {
            roundTo *= -1;
        }
        final int remainder = value % roundTo;
        return (remainder != 0) ? (value + roundTo - remainder) : value;
    }
    
    @Override
    public boolean equals(final Object o) {
        return this == o || (o instanceof FlexibleStorage && Arrays.equals(this.data, ((FlexibleStorage)o).data) && this.bitsPerEntry == ((FlexibleStorage)o).bitsPerEntry && this.size == ((FlexibleStorage)o).size && this.maxEntryValue == ((FlexibleStorage)o).maxEntryValue);
    }
    
    @Override
    public int hashCode() {
        int result = Arrays.hashCode(this.data);
        result = 31 * result + this.bitsPerEntry;
        result = 31 * result + this.size;
        result = 31 * result + (int)this.maxEntryValue;
        return result;
    }
}
