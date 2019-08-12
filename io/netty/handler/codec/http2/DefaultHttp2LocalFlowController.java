// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http2;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.util.Iterator;
import io.netty.util.internal.ObjectUtil;

public class DefaultHttp2LocalFlowController implements Http2LocalFlowController
{
    private static final int DEFAULT_COMPOSITE_EXCEPTION_SIZE = 4;
    public static final float DEFAULT_WINDOW_UPDATE_RATIO = 0.5f;
    private final Http2Connection connection;
    private final Http2FrameWriter frameWriter;
    private volatile float windowUpdateRatio;
    private volatile int initialWindowSize;
    
    public DefaultHttp2LocalFlowController(final Http2Connection connection, final Http2FrameWriter frameWriter) {
        this(connection, frameWriter, 0.5f);
    }
    
    public DefaultHttp2LocalFlowController(final Http2Connection connection, final Http2FrameWriter frameWriter, final float windowUpdateRatio) {
        this.initialWindowSize = 65535;
        this.connection = ObjectUtil.checkNotNull(connection, "connection");
        this.frameWriter = ObjectUtil.checkNotNull(frameWriter, "frameWriter");
        this.windowUpdateRatio(windowUpdateRatio);
        final Http2Stream connectionStream = connection.connectionStream();
        connectionStream.setProperty(FlowState.class, new FlowState(connectionStream, this.initialWindowSize));
        connection.addListener(new Http2ConnectionAdapter() {
            @Override
            public void streamAdded(final Http2Stream stream) {
                stream.setProperty(FlowState.class, new FlowState(stream, 0));
            }
            
            @Override
            public void streamActive(final Http2Stream stream) {
                DefaultHttp2LocalFlowController.this.state(stream).window(DefaultHttp2LocalFlowController.this.initialWindowSize);
            }
        });
    }
    
    @Override
    public void initialWindowSize(final int newWindowSize) throws Http2Exception {
        final int delta = newWindowSize - this.initialWindowSize;
        this.initialWindowSize = newWindowSize;
        Http2Exception.CompositeStreamException compositeException = null;
        for (final Http2Stream stream : this.connection.activeStreams()) {
            try {
                final FlowState state = this.state(stream);
                state.incrementFlowControlWindows(delta);
                state.incrementInitialStreamWindow(delta);
            }
            catch (Http2Exception.StreamException e) {
                if (compositeException == null) {
                    compositeException = new Http2Exception.CompositeStreamException(e.error(), 4);
                }
                compositeException.add(e);
            }
        }
        if (compositeException != null) {
            throw compositeException;
        }
    }
    
    @Override
    public int initialWindowSize() {
        return this.initialWindowSize;
    }
    
    @Override
    public int windowSize(final Http2Stream stream) {
        return this.state(stream).window();
    }
    
    @Override
    public void incrementWindowSize(final ChannelHandlerContext ctx, final Http2Stream stream, final int delta) throws Http2Exception {
        ObjectUtil.checkNotNull(ctx, "ctx");
        final FlowState state = this.state(stream);
        state.incrementInitialStreamWindow(delta);
        state.writeWindowUpdateIfNeeded(ctx);
    }
    
    @Override
    public void consumeBytes(final ChannelHandlerContext ctx, final Http2Stream stream, final int numBytes) throws Http2Exception {
        this.state(stream).consumeBytes(ctx, numBytes);
    }
    
    @Override
    public int unconsumedBytes(final Http2Stream stream) {
        return this.state(stream).unconsumedBytes();
    }
    
    private static void checkValidRatio(final float ratio) {
        if (Double.compare(ratio, 0.0) <= 0 || Double.compare(ratio, 1.0) >= 0) {
            throw new IllegalArgumentException("Invalid ratio: " + ratio);
        }
    }
    
    public void windowUpdateRatio(final float ratio) {
        checkValidRatio(ratio);
        this.windowUpdateRatio = ratio;
    }
    
    public float windowUpdateRatio() {
        return this.windowUpdateRatio;
    }
    
    public void windowUpdateRatio(final ChannelHandlerContext ctx, final Http2Stream stream, final float ratio) throws Http2Exception {
        checkValidRatio(ratio);
        final FlowState state = this.state(stream);
        state.windowUpdateRatio(ratio);
        state.writeWindowUpdateIfNeeded(ctx);
    }
    
    public float windowUpdateRatio(final Http2Stream stream) throws Http2Exception {
        return this.state(stream).windowUpdateRatio();
    }
    
    @Override
    public void receiveFlowControlledFrame(final ChannelHandlerContext ctx, final Http2Stream stream, final ByteBuf data, final int padding, final boolean endOfStream) throws Http2Exception {
        final int dataLength = data.readableBytes() + padding;
        this.connectionState().receiveFlowControlledFrame(dataLength);
        final FlowState state = this.state(stream);
        state.endOfStream(endOfStream);
        state.receiveFlowControlledFrame(dataLength);
    }
    
    private FlowState connectionState() {
        return this.state(this.connection.connectionStream());
    }
    
    private FlowState state(final Http2Stream stream) {
        ObjectUtil.checkNotNull(stream, "stream");
        return stream.getProperty(FlowState.class);
    }
    
    private final class FlowState
    {
        private final Http2Stream stream;
        private int window;
        private int processedWindow;
        private volatile int initialStreamWindowSize;
        private volatile float streamWindowUpdateRatio;
        private int lowerBound;
        private boolean endOfStream;
        
        FlowState(final Http2Stream stream, final int initialWindowSize) {
            this.stream = stream;
            this.window(initialWindowSize);
            this.streamWindowUpdateRatio = DefaultHttp2LocalFlowController.this.windowUpdateRatio;
        }
        
        int window() {
            return this.window;
        }
        
        void window(final int initialWindowSize) {
            this.initialStreamWindowSize = initialWindowSize;
            this.processedWindow = initialWindowSize;
            this.window = initialWindowSize;
        }
        
        void endOfStream(final boolean endOfStream) {
            this.endOfStream = endOfStream;
        }
        
        float windowUpdateRatio() {
            return this.streamWindowUpdateRatio;
        }
        
        void windowUpdateRatio(final float ratio) {
            this.streamWindowUpdateRatio = ratio;
        }
        
        void incrementInitialStreamWindow(int delta) {
            final int newValue = (int)Math.min(2147483647L, Math.max(0L, this.initialStreamWindowSize + (long)delta));
            delta = newValue - this.initialStreamWindowSize;
            this.initialStreamWindowSize += delta;
        }
        
        void incrementFlowControlWindows(final int delta) throws Http2Exception {
            if (delta > 0 && this.window > Integer.MAX_VALUE - delta) {
                throw Http2Exception.streamError(this.stream.id(), Http2Error.FLOW_CONTROL_ERROR, "Flow control window overflowed for stream: %d", this.stream.id());
            }
            this.window += delta;
            this.processedWindow += delta;
            this.lowerBound = ((delta < 0) ? delta : 0);
        }
        
        void receiveFlowControlledFrame(final int dataLength) throws Http2Exception {
            assert dataLength >= 0;
            this.window -= dataLength;
            if (this.window < this.lowerBound) {
                throw Http2Exception.streamError(this.stream.id(), Http2Error.FLOW_CONTROL_ERROR, "Flow control window exceeded for stream: %d", this.stream.id());
            }
        }
        
        void returnProcessedBytes(final int delta) throws Http2Exception {
            if (this.processedWindow - delta < this.window) {
                throw Http2Exception.streamError(this.stream.id(), Http2Error.INTERNAL_ERROR, "Attempting to return too many bytes for stream %d", this.stream.id());
            }
            this.processedWindow -= delta;
        }
        
        void consumeBytes(final ChannelHandlerContext ctx, final int numBytes) throws Http2Exception {
            if (this.stream.id() == 0) {
                throw new UnsupportedOperationException("Returning bytes for the connection window is not supported");
            }
            if (numBytes <= 0) {
                throw new IllegalArgumentException("numBytes must be positive");
            }
            final FlowState connectionState = DefaultHttp2LocalFlowController.this.connectionState();
            connectionState.returnProcessedBytes(numBytes);
            connectionState.writeWindowUpdateIfNeeded(ctx);
            this.returnProcessedBytes(numBytes);
            this.writeWindowUpdateIfNeeded(ctx);
        }
        
        int unconsumedBytes() {
            return this.processedWindow - this.window;
        }
        
        void writeWindowUpdateIfNeeded(final ChannelHandlerContext ctx) throws Http2Exception {
            if (this.endOfStream || this.initialStreamWindowSize <= 0) {
                return;
            }
            final int threshold = (int)(this.initialStreamWindowSize * this.streamWindowUpdateRatio);
            if (this.processedWindow <= threshold) {
                this.writeWindowUpdate(ctx);
            }
        }
        
        void writeWindowUpdate(final ChannelHandlerContext ctx) throws Http2Exception {
            final int deltaWindowSize = this.initialStreamWindowSize - this.processedWindow;
            try {
                this.incrementFlowControlWindows(deltaWindowSize);
            }
            catch (Throwable t) {
                throw Http2Exception.connectionError(Http2Error.INTERNAL_ERROR, t, "Attempting to return too many bytes for stream %d", this.stream.id());
            }
            DefaultHttp2LocalFlowController.this.frameWriter.writeWindowUpdate(ctx, this.stream.id(), deltaWindowSize, ctx.newPromise());
            ctx.flush();
        }
    }
}
