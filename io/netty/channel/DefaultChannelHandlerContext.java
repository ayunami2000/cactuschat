// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.channel;

final class DefaultChannelHandlerContext extends AbstractChannelHandlerContext
{
    private final ChannelHandler handler;
    
    DefaultChannelHandlerContext(final DefaultChannelPipeline pipeline, final ChannelHandlerInvoker invoker, final String name, final ChannelHandler handler) {
        super(pipeline, invoker, name, AbstractChannelHandlerContext.skipFlags(checkNull(handler)));
        this.handler = handler;
    }
    
    private static ChannelHandler checkNull(final ChannelHandler handler) {
        if (handler == null) {
            throw new NullPointerException("handler");
        }
        return handler;
    }
    
    @Override
    public ChannelHandler handler() {
        return this.handler;
    }
}
