// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http2;

import java.util.concurrent.TimeUnit;
import io.netty.channel.ChannelHandlerContext;
import java.util.ArrayDeque;
import java.util.concurrent.ScheduledFuture;
import java.util.Queue;
import io.netty.channel.ChannelHandlerAdapter;

public class DefaultHttp2StreamRemovalPolicy extends ChannelHandlerAdapter implements Http2StreamRemovalPolicy, Runnable
{
    private static final long GARBAGE_COLLECTION_INTERVAL;
    private final Queue<Garbage> garbage;
    private ScheduledFuture<?> timerFuture;
    private Action action;
    
    public DefaultHttp2StreamRemovalPolicy() {
        this.garbage = new ArrayDeque<Garbage>();
    }
    
    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) throws Exception {
        this.timerFuture = ctx.channel().eventLoop().scheduleWithFixedDelay((Runnable)this, DefaultHttp2StreamRemovalPolicy.GARBAGE_COLLECTION_INTERVAL, DefaultHttp2StreamRemovalPolicy.GARBAGE_COLLECTION_INTERVAL, TimeUnit.NANOSECONDS);
    }
    
    @Override
    public void handlerRemoved(final ChannelHandlerContext ctx) throws Exception {
        if (this.timerFuture != null) {
            this.timerFuture.cancel(false);
            this.timerFuture = null;
        }
    }
    
    @Override
    public void setAction(final Action action) {
        this.action = action;
    }
    
    @Override
    public void markForRemoval(final Http2Stream stream) {
        this.garbage.add(new Garbage(stream));
    }
    
    @Override
    public void run() {
        if (this.garbage.isEmpty() || this.action == null) {
            return;
        }
        final long time = System.nanoTime();
        while (true) {
            final Garbage next = this.garbage.peek();
            if (next == null) {
                break;
            }
            if (time - next.removalTime <= DefaultHttp2StreamRemovalPolicy.GARBAGE_COLLECTION_INTERVAL) {
                break;
            }
            this.garbage.remove();
            this.action.removeStream(next.stream);
        }
    }
    
    static {
        GARBAGE_COLLECTION_INTERVAL = TimeUnit.SECONDS.toNanos(5L);
    }
    
    private static final class Garbage
    {
        private final long removalTime;
        private final Http2Stream stream;
        
        Garbage(final Http2Stream stream) {
            this.removalTime = System.nanoTime();
            this.stream = stream;
        }
    }
}
