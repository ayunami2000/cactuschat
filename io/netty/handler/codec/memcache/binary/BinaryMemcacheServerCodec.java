// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.memcache.binary;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAppender;

public class BinaryMemcacheServerCodec extends ChannelHandlerAppender
{
    public BinaryMemcacheServerCodec() {
        this(8192);
    }
    
    public BinaryMemcacheServerCodec(final int decodeChunkSize) {
        this.add(new BinaryMemcacheRequestDecoder(decodeChunkSize));
        this.add(new BinaryMemcacheResponseEncoder());
    }
}
