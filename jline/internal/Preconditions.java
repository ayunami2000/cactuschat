// 
// Decompiled by Procyon v0.5.36
// 

package jline.internal;

public class Preconditions
{
    public static <T> T checkNotNull(final T reference) {
        if (reference == null) {
            throw new NullPointerException();
        }
        return reference;
    }
}
