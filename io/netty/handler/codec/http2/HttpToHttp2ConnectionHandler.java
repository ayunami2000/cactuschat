// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http2;

import io.netty.handler.codec.AsciiString;
import io.netty.handler.codec.Headers;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.channel.ChannelPromise;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaders;

public class HttpToHttp2ConnectionHandler extends Http2ConnectionHandler
{
    public HttpToHttp2ConnectionHandler(final boolean server, final Http2FrameListener listener) {
        super(server, listener);
    }
    
    public HttpToHttp2ConnectionHandler(final Http2Connection connection, final Http2FrameListener listener) {
        super(connection, listener);
    }
    
    public HttpToHttp2ConnectionHandler(final Http2Connection connection, final Http2FrameReader frameReader, final Http2FrameWriter frameWriter, final Http2FrameListener listener) {
        super(connection, frameReader, frameWriter, listener);
    }
    
    public HttpToHttp2ConnectionHandler(final Http2ConnectionDecoder.Builder decoderBuilder, final Http2ConnectionEncoder.Builder encoderBuilder) {
        super(decoderBuilder, encoderBuilder);
    }
    
    private int getStreamId(final HttpHeaders httpHeaders) throws Exception {
        return ((Headers<AsciiString>)httpHeaders).getInt(HttpUtil.ExtensionHeaderNames.STREAM_ID.text(), this.connection().local().nextStreamId());
    }
    
    @Override
    public void write(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise promise) {
        if (msg instanceof FullHttpMessage) {
            final FullHttpMessage httpMsg = (FullHttpMessage)msg;
            final boolean hasData = httpMsg.content().isReadable();
            boolean httpMsgNeedRelease = true;
            Http2CodecUtil.SimpleChannelPromiseAggregator promiseAggregator = null;
            try {
                final int streamId = this.getStreamId(httpMsg.headers());
                final Http2Headers http2Headers = HttpUtil.toHttp2Headers(httpMsg);
                final Http2ConnectionEncoder encoder = this.encoder();
                if (hasData) {
                    promiseAggregator = new Http2CodecUtil.SimpleChannelPromiseAggregator(promise, ctx.channel(), ctx.executor());
                    encoder.writeHeaders(ctx, streamId, http2Headers, 0, false, promiseAggregator.newPromise());
                    httpMsgNeedRelease = false;
                    encoder.writeData(ctx, streamId, httpMsg.content(), 0, true, promiseAggregator.newPromise());
                    promiseAggregator.doneAllocatingPromises();
                }
                else {
                    encoder.writeHeaders(ctx, streamId, http2Headers, 0, true, promise);
                }
            }
            catch (Throwable t) {
                if (promiseAggregator == null) {
                    promise.tryFailure(t);
                }
                else {
                    promiseAggregator.setFailure(t);
                }
            }
            finally {
                if (httpMsgNeedRelease) {
                    httpMsg.release();
                }
            }
        }
        else {
            ctx.write(msg, promise);
        }
    }
}
