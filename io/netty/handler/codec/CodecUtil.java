// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec;

import io.netty.channel.ChannelHandlerAdapter;

final class CodecUtil
{
    static void ensureNotSharable(final ChannelHandlerAdapter handler) {
        if (handler.isSharable()) {
            throw new IllegalStateException("@Sharable annotation is not allowed");
        }
    }
    
    private CodecUtil() {
    }
}
