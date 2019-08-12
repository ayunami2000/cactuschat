// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.util.internal;

public final class ObjectUtil
{
    private ObjectUtil() {
    }
    
    public static <T> T checkNotNull(final T arg, final String text) {
        if (arg == null) {
            throw new NullPointerException(text);
        }
        return arg;
    }
}
