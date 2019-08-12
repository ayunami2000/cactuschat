// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.channel;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

public class ChannelHandlerAppender extends ChannelHandlerAdapter
{
    private final boolean selfRemoval;
    private final List<Entry> handlers;
    private boolean added;
    
    protected ChannelHandlerAppender() {
        this(true);
    }
    
    protected ChannelHandlerAppender(final boolean selfRemoval) {
        this.handlers = new ArrayList<Entry>();
        this.selfRemoval = selfRemoval;
    }
    
    public ChannelHandlerAppender(final Iterable<? extends ChannelHandler> handlers) {
        this(true, handlers);
    }
    
    public ChannelHandlerAppender(final ChannelHandler... handlers) {
        this(true, handlers);
    }
    
    public ChannelHandlerAppender(final boolean selfRemoval, final Iterable<? extends ChannelHandler> handlers) {
        this.handlers = new ArrayList<Entry>();
        this.selfRemoval = selfRemoval;
        this.add(handlers);
    }
    
    public ChannelHandlerAppender(final boolean selfRemoval, final ChannelHandler... handlers) {
        this.handlers = new ArrayList<Entry>();
        this.selfRemoval = selfRemoval;
        this.add(handlers);
    }
    
    protected final ChannelHandlerAppender add(final String name, final ChannelHandler handler) {
        if (handler == null) {
            throw new NullPointerException("handler");
        }
        if (this.added) {
            throw new IllegalStateException("added to the pipeline already");
        }
        this.handlers.add(new Entry(name, handler));
        return this;
    }
    
    protected final ChannelHandlerAppender add(final ChannelHandler handler) {
        return this.add(null, handler);
    }
    
    protected final ChannelHandlerAppender add(final Iterable<? extends ChannelHandler> handlers) {
        if (handlers == null) {
            throw new NullPointerException("handlers");
        }
        for (final ChannelHandler h : handlers) {
            if (h == null) {
                break;
            }
            this.add(h);
        }
        return this;
    }
    
    protected final ChannelHandlerAppender add(final ChannelHandler... handlers) {
        if (handlers == null) {
            throw new NullPointerException("handlers");
        }
        for (final ChannelHandler h : handlers) {
            if (h == null) {
                break;
            }
            this.add(h);
        }
        return this;
    }
    
    protected final <T extends ChannelHandler> T handlerAt(final int index) {
        return (T)this.handlers.get(index).handler;
    }
    
    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) throws Exception {
        this.added = true;
        final AbstractChannelHandlerContext dctx = (AbstractChannelHandlerContext)ctx;
        final DefaultChannelPipeline pipeline = (DefaultChannelPipeline)dctx.pipeline();
        String name = dctx.name();
        try {
            for (final Entry e : this.handlers) {
                final String oldName = name;
                if (e.name == null) {
                    name = pipeline.generateName(e.handler);
                }
                else {
                    name = e.name;
                }
                pipeline.addAfter(dctx.invoker, oldName, name, e.handler);
            }
        }
        finally {
            if (this.selfRemoval) {
                pipeline.remove(this);
            }
        }
    }
    
    private static final class Entry
    {
        final String name;
        final ChannelHandler handler;
        
        Entry(final String name, final ChannelHandler handler) {
            this.name = name;
            this.handler = handler;
        }
    }
}
