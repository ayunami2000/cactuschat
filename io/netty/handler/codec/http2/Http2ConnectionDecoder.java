// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http2;

import java.util.List;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.io.Closeable;

public interface Http2ConnectionDecoder extends Closeable
{
    Http2Connection connection();
    
    Http2LocalFlowController flowController();
    
    Http2FrameListener listener();
    
    void decodeFrame(final ChannelHandlerContext p0, final ByteBuf p1, final List<Object> p2) throws Http2Exception;
    
    Http2Settings localSettings();
    
    void localSettings(final Http2Settings p0) throws Http2Exception;
    
    boolean prefaceReceived();
    
    void close();
    
    public interface Builder
    {
        Builder connection(final Http2Connection p0);
        
        Builder lifecycleManager(final Http2LifecycleManager p0);
        
        Http2LifecycleManager lifecycleManager();
        
        Builder frameReader(final Http2FrameReader p0);
        
        Builder listener(final Http2FrameListener p0);
        
        Builder encoder(final Http2ConnectionEncoder p0);
        
        Http2ConnectionDecoder build();
    }
}
