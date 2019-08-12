// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.channel;

import io.netty.util.concurrent.Promise;
import io.netty.util.concurrent.PromiseNotifier;

public final class ChannelPromiseNotifier extends PromiseNotifier<Void, ChannelFuture> implements ChannelFutureListener
{
    public ChannelPromiseNotifier(final ChannelPromise... promises) {
        super((Promise[])promises);
    }
}
