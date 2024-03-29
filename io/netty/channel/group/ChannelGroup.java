// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.channel.group;

import io.netty.channel.ChannelId;
import io.netty.channel.Channel;
import java.util.Set;

public interface ChannelGroup extends Set<Channel>, Comparable<ChannelGroup>
{
    String name();
    
    Channel find(final ChannelId p0);
    
    ChannelGroupFuture write(final Object p0);
    
    ChannelGroupFuture write(final Object p0, final ChannelMatcher p1);
    
    ChannelGroup flush();
    
    ChannelGroup flush(final ChannelMatcher p0);
    
    ChannelGroupFuture writeAndFlush(final Object p0);
    
    ChannelGroupFuture writeAndFlush(final Object p0, final ChannelMatcher p1);
    
    ChannelGroupFuture disconnect();
    
    ChannelGroupFuture disconnect(final ChannelMatcher p0);
    
    ChannelGroupFuture close();
    
    ChannelGroupFuture close(final ChannelMatcher p0);
    
    ChannelGroupFuture deregister();
    
    ChannelGroupFuture deregister(final ChannelMatcher p0);
}
