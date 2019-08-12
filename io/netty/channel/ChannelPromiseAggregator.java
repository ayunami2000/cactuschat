// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.channel;

import io.netty.util.concurrent.Promise;
import io.netty.util.concurrent.PromiseAggregator;

public final class ChannelPromiseAggregator extends PromiseAggregator<Void, ChannelFuture> implements ChannelFutureListener
{
    public ChannelPromiseAggregator(final ChannelPromise aggregatePromise) {
        super(aggregatePromise);
    }
}
