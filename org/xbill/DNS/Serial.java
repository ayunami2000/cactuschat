// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

public final class Serial
{
    private static final long MAX32 = 4294967295L;
    
    private Serial() {
    }
    
    public static int compare(final long serial1, final long serial2) {
        if (serial1 < 0L || serial1 > 4294967295L) {
            throw new IllegalArgumentException(serial1 + " out of range");
        }
        if (serial2 < 0L || serial2 > 4294967295L) {
            throw new IllegalArgumentException(serial2 + " out of range");
        }
        long diff = serial1 - serial2;
        if (diff >= 4294967295L) {
            diff -= 4294967296L;
        }
        else if (diff < -4294967295L) {
            diff += 4294967296L;
        }
        return (int)diff;
    }
    
    public static long increment(final long serial) {
        if (serial < 0L || serial > 4294967295L) {
            throw new IllegalArgumentException(serial + " out of range");
        }
        if (serial == 4294967295L) {
            return 0L;
        }
        return serial + 1L;
    }
}
