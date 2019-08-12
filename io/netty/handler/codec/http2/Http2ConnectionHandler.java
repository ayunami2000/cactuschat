// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http2;

import java.util.List;
import java.util.Iterator;
import java.util.Collection;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.channel.ChannelPromise;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.ObjectUtil;
import io.netty.channel.ChannelFutureListener;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.ByteToMessageDecoder;

public class Http2ConnectionHandler extends ByteToMessageDecoder implements Http2LifecycleManager
{
    private final Http2ConnectionDecoder decoder;
    private final Http2ConnectionEncoder encoder;
    private ByteBuf clientPrefaceString;
    private boolean prefaceSent;
    private ChannelFutureListener closeListener;
    
    public Http2ConnectionHandler(final boolean server, final Http2FrameListener listener) {
        this(new DefaultHttp2Connection(server), listener);
    }
    
    public Http2ConnectionHandler(final Http2Connection connection, final Http2FrameListener listener) {
        this(connection, new DefaultHttp2FrameReader(), new DefaultHttp2FrameWriter(), listener);
    }
    
    public Http2ConnectionHandler(final Http2Connection connection, final Http2FrameReader frameReader, final Http2FrameWriter frameWriter, final Http2FrameListener listener) {
        this(DefaultHttp2ConnectionDecoder.newBuilder().connection(connection).frameReader(frameReader).listener(listener), DefaultHttp2ConnectionEncoder.newBuilder().connection(connection).frameWriter(frameWriter));
    }
    
    public Http2ConnectionHandler(final Http2ConnectionDecoder.Builder decoderBuilder, final Http2ConnectionEncoder.Builder encoderBuilder) {
        ObjectUtil.checkNotNull(decoderBuilder, "decoderBuilder");
        ObjectUtil.checkNotNull(encoderBuilder, "encoderBuilder");
        if (encoderBuilder.lifecycleManager() != decoderBuilder.lifecycleManager()) {
            throw new IllegalArgumentException("Encoder and Decoder must share a lifecycle manager");
        }
        if (encoderBuilder.lifecycleManager() == null) {
            encoderBuilder.lifecycleManager(this);
            decoderBuilder.lifecycleManager(this);
        }
        decoderBuilder.encoder(this.encoder = ObjectUtil.checkNotNull(encoderBuilder.build(), "encoder"));
        this.decoder = ObjectUtil.checkNotNull(decoderBuilder.build(), "decoder");
        ObjectUtil.checkNotNull(this.encoder.connection(), "encoder.connection");
        if (this.encoder.connection() != this.decoder.connection()) {
            throw new IllegalArgumentException("Encoder and Decoder do not share the same connection object");
        }
        this.clientPrefaceString = clientPrefaceString(this.encoder.connection());
    }
    
    public Http2Connection connection() {
        return this.encoder.connection();
    }
    
    public Http2ConnectionDecoder decoder() {
        return this.decoder;
    }
    
    public Http2ConnectionEncoder encoder() {
        return this.encoder;
    }
    
    public void onHttpClientUpgrade() throws Http2Exception {
        if (this.connection().isServer()) {
            throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Client-side HTTP upgrade requested for a server", new Object[0]);
        }
        if (this.prefaceSent || this.decoder.prefaceReceived()) {
            throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "HTTP upgrade must occur before HTTP/2 preface is sent or received", new Object[0]);
        }
        this.connection().createLocalStream(1).open(true);
    }
    
    public void onHttpServerUpgrade(final Http2Settings settings) throws Http2Exception {
        if (!this.connection().isServer()) {
            throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Server-side HTTP upgrade requested for a client", new Object[0]);
        }
        if (this.prefaceSent || this.decoder.prefaceReceived()) {
            throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "HTTP upgrade must occur before HTTP/2 preface is sent or received", new Object[0]);
        }
        this.encoder.remoteSettings(settings);
        this.connection().createRemoteStream(1).open(true);
    }
    
    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        this.sendPreface(ctx);
        super.channelActive(ctx);
    }
    
    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) throws Exception {
        this.sendPreface(ctx);
    }
    
    @Override
    protected void handlerRemoved0(final ChannelHandlerContext ctx) throws Exception {
        this.dispose();
    }
    
    @Override
    public void close(final ChannelHandlerContext ctx, final ChannelPromise promise) throws Exception {
        if (!ctx.channel().isActive()) {
            ctx.close(promise);
            return;
        }
        final ChannelFuture future = this.writeGoAway(ctx, null);
        if (this.connection().numActiveStreams() == 0) {
            future.addListener((GenericFutureListener<? extends Future<? super Void>>)new ClosingChannelFutureListener(ctx, promise));
        }
        else {
            this.closeListener = new ClosingChannelFutureListener(ctx, promise);
        }
    }
    
    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        final ChannelFuture future = ctx.newSucceededFuture();
        final Collection<Http2Stream> streams = this.connection().activeStreams();
        for (final Http2Stream s : streams.toArray(new Http2Stream[streams.size()])) {
            this.closeStream(s, future);
        }
        super.channelInactive(ctx);
    }
    
    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        if (Http2CodecUtil.getEmbeddedHttp2Exception(cause) != null) {
            this.onException(ctx, cause);
        }
        else {
            super.exceptionCaught(ctx, cause);
        }
    }
    
    @Override
    public void closeLocalSide(final Http2Stream stream, final ChannelFuture future) {
        switch (stream.state()) {
            case HALF_CLOSED_LOCAL:
            case OPEN: {
                stream.closeLocalSide();
                break;
            }
            default: {
                this.closeStream(stream, future);
                break;
            }
        }
    }
    
    @Override
    public void closeRemoteSide(final Http2Stream stream, final ChannelFuture future) {
        switch (stream.state()) {
            case OPEN:
            case HALF_CLOSED_REMOTE: {
                stream.closeRemoteSide();
                break;
            }
            default: {
                this.closeStream(stream, future);
                break;
            }
        }
    }
    
    @Override
    public void closeStream(final Http2Stream stream, final ChannelFuture future) {
        stream.close();
        future.addListener((GenericFutureListener<? extends Future<? super Void>>)new ChannelFutureListener() {
            @Override
            public void operationComplete(final ChannelFuture future) throws Exception {
                Http2ConnectionHandler.this.connection().deactivate(stream);
                if (Http2ConnectionHandler.this.closeListener != null && Http2ConnectionHandler.this.connection().numActiveStreams() == 0) {
                    Http2ConnectionHandler.this.closeListener.operationComplete(future);
                }
            }
        });
    }
    
    @Override
    public void onException(final ChannelHandlerContext ctx, final Throwable cause) {
        final Http2Exception embedded = Http2CodecUtil.getEmbeddedHttp2Exception(cause);
        if (Http2Exception.isStreamError(embedded)) {
            this.onStreamError(ctx, cause, (Http2Exception.StreamException)embedded);
        }
        else if (embedded instanceof Http2Exception.CompositeStreamException) {
            final Http2Exception.CompositeStreamException compositException = (Http2Exception.CompositeStreamException)embedded;
            for (final Http2Exception.StreamException streamException : compositException) {
                this.onStreamError(ctx, cause, streamException);
            }
        }
        else {
            this.onConnectionError(ctx, cause, embedded);
        }
    }
    
    protected void onConnectionError(final ChannelHandlerContext ctx, final Throwable cause, Http2Exception http2Ex) {
        if (http2Ex == null) {
            http2Ex = new Http2Exception(Http2Error.INTERNAL_ERROR, cause.getMessage(), cause);
        }
        this.writeGoAway(ctx, http2Ex).addListener((GenericFutureListener<? extends Future<? super Void>>)new ClosingChannelFutureListener(ctx, ctx.newPromise()));
    }
    
    protected void onStreamError(final ChannelHandlerContext ctx, final Throwable cause, final Http2Exception.StreamException http2Ex) {
        this.writeRstStream(ctx, http2Ex.streamId(), http2Ex.error().code(), ctx.newPromise());
    }
    
    protected Http2FrameWriter frameWriter() {
        return this.encoder().frameWriter();
    }
    
    @Override
    public ChannelFuture writeRstStream(final ChannelHandlerContext ctx, final int streamId, final long errorCode, final ChannelPromise promise) {
        final Http2Stream stream = this.connection().stream(streamId);
        final ChannelFuture future = this.frameWriter().writeRstStream(ctx, streamId, errorCode, promise);
        ctx.flush();
        if (stream != null) {
            stream.resetSent();
            this.closeStream(stream, promise);
        }
        return future;
    }
    
    @Override
    public ChannelFuture writeGoAway(final ChannelHandlerContext ctx, final int lastStreamId, final long errorCode, final ByteBuf debugData, final ChannelPromise promise) {
        final Http2Connection connection = this.connection();
        if (connection.isGoAway()) {
            debugData.release();
            return ctx.newSucceededFuture();
        }
        final ChannelFuture future = this.frameWriter().writeGoAway(ctx, lastStreamId, errorCode, debugData, promise);
        ctx.flush();
        connection.goAwaySent(lastStreamId);
        return future;
    }
    
    private ChannelFuture writeGoAway(final ChannelHandlerContext ctx, final Http2Exception cause) {
        final Http2Connection connection = this.connection();
        if (connection.isGoAway()) {
            return ctx.newSucceededFuture();
        }
        final long errorCode = (cause != null) ? cause.error().code() : Http2Error.NO_ERROR.code();
        final ByteBuf debugData = Http2CodecUtil.toByteBuf(ctx, cause);
        final int lastKnownStream = connection.remote().lastStreamCreated();
        return this.writeGoAway(ctx, lastKnownStream, errorCode, debugData, ctx.newPromise());
    }
    
    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) throws Exception {
        try {
            if (!this.readClientPrefaceString(in)) {
                return;
            }
            this.decoder.decodeFrame(ctx, in, out);
        }
        catch (Throwable e) {
            this.onException(ctx, e);
        }
    }
    
    private void sendPreface(final ChannelHandlerContext ctx) {
        if (this.prefaceSent || !ctx.channel().isActive()) {
            return;
        }
        this.prefaceSent = true;
        if (!this.connection().isServer()) {
            ctx.write(Http2CodecUtil.connectionPrefaceBuf()).addListener((GenericFutureListener<? extends Future<? super Void>>)ChannelFutureListener.CLOSE_ON_FAILURE);
        }
        this.encoder.writeSettings(ctx, this.decoder.localSettings(), ctx.newPromise()).addListener((GenericFutureListener<? extends Future<? super Void>>)ChannelFutureListener.CLOSE_ON_FAILURE);
    }
    
    private void dispose() {
        this.encoder.close();
        this.decoder.close();
        if (this.clientPrefaceString != null) {
            this.clientPrefaceString.release();
            this.clientPrefaceString = null;
        }
    }
    
    private boolean readClientPrefaceString(final ByteBuf in) throws Http2Exception {
        if (this.clientPrefaceString == null) {
            return true;
        }
        final int prefaceRemaining = this.clientPrefaceString.readableBytes();
        final int bytesRead = Math.min(in.readableBytes(), prefaceRemaining);
        final ByteBuf sourceSlice = in.readSlice(bytesRead);
        final ByteBuf prefaceSlice = this.clientPrefaceString.readSlice(bytesRead);
        if (bytesRead == 0 || !prefaceSlice.equals(sourceSlice)) {
            throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "HTTP/2 client preface string missing or corrupt.", new Object[0]);
        }
        if (!this.clientPrefaceString.isReadable()) {
            this.clientPrefaceString.release();
            this.clientPrefaceString = null;
            return true;
        }
        return false;
    }
    
    private static ByteBuf clientPrefaceString(final Http2Connection connection) {
        return connection.isServer() ? Http2CodecUtil.connectionPrefaceBuf() : null;
    }
    
    private static final class ClosingChannelFutureListener implements ChannelFutureListener
    {
        private final ChannelHandlerContext ctx;
        private final ChannelPromise promise;
        
        ClosingChannelFutureListener(final ChannelHandlerContext ctx, final ChannelPromise promise) {
            this.ctx = ctx;
            this.promise = promise;
        }
        
        @Override
        public void operationComplete(final ChannelFuture sentGoAwayFuture) throws Exception {
            this.ctx.close(this.promise);
        }
    }
}
