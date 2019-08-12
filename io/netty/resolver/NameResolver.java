// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.resolver;

import io.netty.util.concurrent.Promise;
import io.netty.util.concurrent.Future;
import java.io.Closeable;
import java.net.SocketAddress;

public interface NameResolver<T extends SocketAddress> extends Closeable
{
    boolean isSupported(final SocketAddress p0);
    
    boolean isResolved(final SocketAddress p0);
    
    Future<T> resolve(final String p0, final int p1);
    
    Future<T> resolve(final String p0, final int p1, final Promise<T> p2);
    
    Future<T> resolve(final SocketAddress p0);
    
    Future<T> resolve(final SocketAddress p0, final Promise<T> p1);
    
    void close();
}
