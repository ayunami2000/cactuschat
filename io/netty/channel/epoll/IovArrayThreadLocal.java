// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.channel.epoll;

import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.util.concurrent.FastThreadLocal;

final class IovArrayThreadLocal
{
    private static final FastThreadLocal<IovArray> ARRAY;
    
    static IovArray get(final ChannelOutboundBuffer buffer) throws Exception {
        final IovArray array = IovArrayThreadLocal.ARRAY.get();
        array.clear();
        buffer.forEachFlushedMessage(array);
        return array;
    }
    
    static IovArray get(final CompositeByteBuf buf) throws Exception {
        final IovArray array = IovArrayThreadLocal.ARRAY.get();
        array.clear();
        array.add(buf);
        return array;
    }
    
    private IovArrayThreadLocal() {
    }
    
    static {
        ARRAY = new FastThreadLocal<IovArray>() {
            @Override
            protected IovArray initialValue() throws Exception {
                return new IovArray();
            }
            
            @Override
            protected void onRemoval(final IovArray value) throws Exception {
                value.release();
            }
        };
    }
}
