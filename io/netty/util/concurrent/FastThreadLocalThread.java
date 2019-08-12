// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.util.concurrent;

import io.netty.util.internal.InternalThreadLocalMap;

public class FastThreadLocalThread extends Thread implements FastThreadLocalAccess
{
    private InternalThreadLocalMap threadLocalMap;
    
    public FastThreadLocalThread() {
    }
    
    public FastThreadLocalThread(final Runnable target) {
        super(target);
    }
    
    public FastThreadLocalThread(final ThreadGroup group, final Runnable target) {
        super(group, target);
    }
    
    public FastThreadLocalThread(final String name) {
        super(name);
    }
    
    public FastThreadLocalThread(final ThreadGroup group, final String name) {
        super(group, name);
    }
    
    public FastThreadLocalThread(final Runnable target, final String name) {
        super(target, name);
    }
    
    public FastThreadLocalThread(final ThreadGroup group, final Runnable target, final String name) {
        super(group, target, name);
    }
    
    public FastThreadLocalThread(final ThreadGroup group, final Runnable target, final String name, final long stackSize) {
        super(group, target, name, stackSize);
    }
    
    @Override
    public final InternalThreadLocalMap threadLocalMap() {
        return this.threadLocalMap;
    }
    
    @Override
    public final void setThreadLocalMap(final InternalThreadLocalMap threadLocalMap) {
        this.threadLocalMap = threadLocalMap;
    }
}
