// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.util;

import io.netty.util.internal.ThreadLocalRandom;
import io.netty.util.internal.PlatformDependent;
import java.nio.ByteBuffer;

public abstract class AbstractConstant<T extends AbstractConstant<T>> implements Constant<T>
{
    private final int id;
    private final String name;
    private volatile long uniquifier;
    private ByteBuffer directBuffer;
    
    protected AbstractConstant(final int id, final String name) {
        this.id = id;
        this.name = name;
    }
    
    @Override
    public final String name() {
        return this.name;
    }
    
    @Override
    public final int id() {
        return this.id;
    }
    
    @Override
    public final String toString() {
        return this.name();
    }
    
    @Override
    public final int hashCode() {
        return super.hashCode();
    }
    
    @Override
    public final boolean equals(final Object obj) {
        return super.equals(obj);
    }
    
    @Override
    public final int compareTo(final T o) {
        if (this == o) {
            return 0;
        }
        final AbstractConstant<T> other = o;
        final int returnCode = this.hashCode() - other.hashCode();
        if (returnCode != 0) {
            return returnCode;
        }
        final long thisUV = this.uniquifier();
        final long otherUV = other.uniquifier();
        if (thisUV < otherUV) {
            return -1;
        }
        if (thisUV > otherUV) {
            return 1;
        }
        throw new Error("failed to compare two different constants");
    }
    
    private long uniquifier() {
        long uniquifier;
        if ((uniquifier = this.uniquifier) == 0L) {
            synchronized (this) {
                while ((uniquifier = this.uniquifier) == 0L) {
                    if (PlatformDependent.hasUnsafe()) {
                        this.directBuffer = ByteBuffer.allocateDirect(1);
                        this.uniquifier = PlatformDependent.directBufferAddress(this.directBuffer);
                    }
                    else {
                        this.directBuffer = null;
                        this.uniquifier = ThreadLocalRandom.current().nextLong();
                    }
                }
            }
        }
        return uniquifier;
    }
}
