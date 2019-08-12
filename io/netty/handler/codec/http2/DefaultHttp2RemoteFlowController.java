// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http2;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Arrays;
import java.util.Iterator;
import io.netty.util.internal.ObjectUtil;
import io.netty.channel.ChannelHandlerContext;
import java.util.Comparator;

public class DefaultHttp2RemoteFlowController implements Http2RemoteFlowController
{
    private static final Comparator<Http2Stream> WEIGHT_ORDER;
    private final Http2Connection connection;
    private int initialWindowSize;
    private ChannelHandlerContext ctx;
    private boolean needFlush;
    
    public DefaultHttp2RemoteFlowController(final Http2Connection connection) {
        this.initialWindowSize = 65535;
        this.connection = ObjectUtil.checkNotNull(connection, "connection");
        connection.connectionStream().setProperty(FlowState.class, new FlowState(connection.connectionStream(), this.initialWindowSize));
        connection.addListener(new Http2ConnectionAdapter() {
            @Override
            public void streamAdded(final Http2Stream stream) {
                stream.setProperty(FlowState.class, new FlowState(stream, 0));
            }
            
            @Override
            public void streamActive(final Http2Stream stream) {
                state(stream).window(DefaultHttp2RemoteFlowController.this.initialWindowSize);
            }
            
            @Override
            public void streamInactive(final Http2Stream stream) {
                state(stream).clear();
            }
            
            @Override
            public void priorityTreeParentChanged(final Http2Stream stream, final Http2Stream oldParent) {
                final Http2Stream parent = stream.parent();
                if (parent != null) {
                    final int delta = state(stream).streamableBytesForTree();
                    if (delta != 0) {
                        state(parent).incrementStreamableBytesForTree(delta);
                    }
                }
            }
            
            @Override
            public void priorityTreeParentChanging(final Http2Stream stream, final Http2Stream newParent) {
                final Http2Stream parent = stream.parent();
                if (parent != null) {
                    final int delta = -state(stream).streamableBytesForTree();
                    if (delta != 0) {
                        state(parent).incrementStreamableBytesForTree(delta);
                    }
                }
            }
        });
    }
    
    @Override
    public void initialWindowSize(final int newWindowSize) throws Http2Exception {
        if (newWindowSize < 0) {
            throw new IllegalArgumentException("Invalid initial window size: " + newWindowSize);
        }
        final int delta = newWindowSize - this.initialWindowSize;
        this.initialWindowSize = newWindowSize;
        for (final Http2Stream stream : this.connection.activeStreams()) {
            state(stream).incrementStreamWindow(delta);
        }
        if (delta > 0) {
            this.writePendingBytes();
        }
    }
    
    @Override
    public int initialWindowSize() {
        return this.initialWindowSize;
    }
    
    @Override
    public int windowSize(final Http2Stream stream) {
        return state(stream).window();
    }
    
    @Override
    public void incrementWindowSize(final ChannelHandlerContext ctx, final Http2Stream stream, final int delta) throws Http2Exception {
        if (stream.id() == 0) {
            this.connectionState().incrementStreamWindow(delta);
            this.writePendingBytes();
        }
        else {
            final FlowState state = state(stream);
            state.incrementStreamWindow(delta);
            state.writeBytes(state.writableWindow());
            this.flush();
        }
    }
    
    @Override
    public void sendFlowControlled(final ChannelHandlerContext ctx, final Http2Stream stream, final FlowControlled payload) {
        ObjectUtil.checkNotNull(ctx, "ctx");
        ObjectUtil.checkNotNull(payload, "payload");
        if (this.ctx != null && this.ctx != ctx) {
            throw new IllegalArgumentException("Writing data from multiple ChannelHandlerContexts is not supported");
        }
        this.ctx = ctx;
        try {
            final FlowState state = state(stream);
            state.newFrame(payload);
            state.writeBytes(state.writableWindow());
            this.flush();
        }
        catch (Throwable e) {
            payload.error(e);
        }
    }
    
    int streamableBytesForTree(final Http2Stream stream) {
        return state(stream).streamableBytesForTree();
    }
    
    private static FlowState state(final Http2Stream stream) {
        ObjectUtil.checkNotNull(stream, "stream");
        return stream.getProperty(FlowState.class);
    }
    
    private FlowState connectionState() {
        return state(this.connection.connectionStream());
    }
    
    private int connectionWindow() {
        return this.connectionState().window();
    }
    
    private void flush() {
        if (this.needFlush) {
            this.ctx.flush();
            this.needFlush = false;
        }
    }
    
    private void writePendingBytes() {
        final Http2Stream connectionStream = this.connection.connectionStream();
        final int connectionWindow = state(connectionStream).window();
        if (connectionWindow > 0) {
            this.writeChildren(connectionStream, connectionWindow);
            for (final Http2Stream stream : this.connection.activeStreams()) {
                writeChildNode(state(stream));
            }
            this.flush();
        }
    }
    
    private int writeChildren(final Http2Stream parent, int connectionWindow) {
        FlowState state = state(parent);
        if (state.streamableBytesForTree() <= 0) {
            return 0;
        }
        int bytesAllocated = 0;
        if (state.streamableBytesForTree() <= connectionWindow) {
            for (final Http2Stream child : parent.children()) {
                state = state(child);
                final int bytesForChild = state.streamableBytes();
                if (bytesForChild > 0 || state.hasFrame()) {
                    state.allocate(bytesForChild);
                    writeChildNode(state);
                    bytesAllocated += bytesForChild;
                    connectionWindow -= bytesForChild;
                }
                final int childBytesAllocated = this.writeChildren(child, connectionWindow);
                bytesAllocated += childBytesAllocated;
                connectionWindow -= childBytesAllocated;
            }
            return bytesAllocated;
        }
        final Http2Stream[] children = parent.children().toArray(new Http2Stream[parent.numChildren()]);
        Arrays.sort(children, DefaultHttp2RemoteFlowController.WEIGHT_ORDER);
        int totalWeight = parent.totalChildWeights();
        int nextTail;
        for (int tail = children.length; tail > 0; tail = nextTail) {
            int head = 0;
            nextTail = 0;
            int nextTotalWeight = 0;
            int nextConnectionWindow;
            for (nextConnectionWindow = connectionWindow; head < tail && nextConnectionWindow > 0; ++head) {
                final Http2Stream child2 = children[head];
                state = state(child2);
                final int weight = child2.weight();
                final double weightRatio = weight / (double)totalWeight;
                int bytesForTree = Math.min(nextConnectionWindow, (int)Math.ceil(connectionWindow * weightRatio));
                final int bytesForChild2 = Math.min(state.streamableBytes(), bytesForTree);
                if (bytesForChild2 > 0 || state.hasFrame()) {
                    state.allocate(bytesForChild2);
                    bytesAllocated += bytesForChild2;
                    nextConnectionWindow -= bytesForChild2;
                    bytesForTree -= bytesForChild2;
                    if (state.streamableBytesForTree() - bytesForChild2 > 0) {
                        children[nextTail++] = child2;
                        nextTotalWeight += weight;
                    }
                    if (state.streamableBytes() - bytesForChild2 == 0) {
                        writeChildNode(state);
                    }
                }
                if (bytesForTree > 0) {
                    final int childBytesAllocated2 = this.writeChildren(child2, bytesForTree);
                    bytesAllocated += childBytesAllocated2;
                    nextConnectionWindow -= childBytesAllocated2;
                }
            }
            connectionWindow = nextConnectionWindow;
            totalWeight = nextTotalWeight;
        }
        return bytesAllocated;
    }
    
    private static void writeChildNode(final FlowState state) {
        state.writeBytes(state.allocated());
        state.resetAllocated();
    }
    
    static /* synthetic */ boolean access$476(final DefaultHttp2RemoteFlowController x0, final int x1) {
        return x0.needFlush = ((byte)((x0.needFlush ? 1 : 0) | x1) != 0);
    }
    
    static {
        WEIGHT_ORDER = new Comparator<Http2Stream>() {
            @Override
            public int compare(final Http2Stream o1, final Http2Stream o2) {
                return o2.weight() - o1.weight();
            }
        };
    }
    
    final class FlowState
    {
        private final Queue<Frame> pendingWriteQueue;
        private final Http2Stream stream;
        private int window;
        private int pendingBytes;
        private int streamableBytesForTree;
        private int allocated;
        
        FlowState(final Http2Stream stream, final int initialWindowSize) {
            this.stream = stream;
            this.window(initialWindowSize);
            this.pendingWriteQueue = new ArrayDeque<Frame>(2);
        }
        
        int window() {
            return this.window;
        }
        
        void window(final int initialWindowSize) {
            this.window = initialWindowSize;
        }
        
        void allocate(final int bytes) {
            this.allocated += bytes;
        }
        
        int allocated() {
            return this.allocated;
        }
        
        void resetAllocated() {
            this.allocated = 0;
        }
        
        int incrementStreamWindow(final int delta) throws Http2Exception {
            if (delta > 0 && Integer.MAX_VALUE - delta < this.window) {
                throw Http2Exception.streamError(this.stream.id(), Http2Error.FLOW_CONTROL_ERROR, "Window size overflow for stream: %d", this.stream.id());
            }
            final int previouslyStreamable = this.streamableBytes();
            this.window += delta;
            final int streamableDelta = this.streamableBytes() - previouslyStreamable;
            if (streamableDelta != 0) {
                this.incrementStreamableBytesForTree(streamableDelta);
            }
            return this.window;
        }
        
        int writableWindow() {
            return Math.min(this.window, DefaultHttp2RemoteFlowController.this.connectionWindow());
        }
        
        int streamableBytes() {
            return Math.max(0, Math.min(this.pendingBytes, this.window));
        }
        
        int streamableBytesForTree() {
            return this.streamableBytesForTree;
        }
        
        Frame newFrame(final FlowControlled payload) {
            final Frame frame = new Frame(payload);
            this.pendingWriteQueue.offer(frame);
            return frame;
        }
        
        boolean hasFrame() {
            return !this.pendingWriteQueue.isEmpty();
        }
        
        Frame peek() {
            return this.pendingWriteQueue.peek();
        }
        
        void clear() {
            while (true) {
                final Frame frame = this.pendingWriteQueue.poll();
                if (frame == null) {
                    break;
                }
                frame.writeError(Http2Exception.streamError(this.stream.id(), Http2Error.INTERNAL_ERROR, "Stream closed before write could take place", new Object[0]));
            }
        }
        
        int writeBytes(final int bytes) {
            int bytesAttempted = 0;
            while (this.hasFrame()) {
                final int maxBytes = Math.min(bytes - bytesAttempted, this.writableWindow());
                bytesAttempted += this.peek().write(maxBytes);
                if (bytes - bytesAttempted <= 0) {
                    break;
                }
            }
            return bytesAttempted;
        }
        
        void incrementStreamableBytesForTree(final int numBytes) {
            this.streamableBytesForTree += numBytes;
            if (!this.stream.isRoot()) {
                state(this.stream.parent()).incrementStreamableBytesForTree(numBytes);
            }
        }
        
        private final class Frame
        {
            final FlowControlled payload;
            
            Frame(final FlowControlled payload) {
                this.payload = payload;
                this.incrementPendingBytes(payload.size());
            }
            
            private void incrementPendingBytes(final int numBytes) {
                final int previouslyStreamable = FlowState.this.streamableBytes();
                FlowState.this.pendingBytes += numBytes;
                final int delta = FlowState.this.streamableBytes() - previouslyStreamable;
                if (delta != 0) {
                    FlowState.this.incrementStreamableBytesForTree(delta);
                }
            }
            
            int write(final int allowedBytes) {
                final int before = this.payload.size();
                DefaultHttp2RemoteFlowController.access$476(DefaultHttp2RemoteFlowController.this, this.payload.write(Math.max(0, allowedBytes)) ? 1 : 0);
                final int writtenBytes = before - this.payload.size();
                try {
                    DefaultHttp2RemoteFlowController.this.connectionState().incrementStreamWindow(-writtenBytes);
                    FlowState.this.incrementStreamWindow(-writtenBytes);
                }
                catch (Http2Exception e) {
                    throw new RuntimeException("Invalid window state when writing frame: " + e.getMessage(), e);
                }
                this.decrementPendingBytes(writtenBytes);
                if (this.payload.size() == 0) {
                    FlowState.this.pendingWriteQueue.remove();
                }
                return writtenBytes;
            }
            
            void writeError(final Http2Exception cause) {
                this.decrementPendingBytes(this.payload.size());
                this.payload.error(cause);
            }
            
            void decrementPendingBytes(final int bytes) {
                this.incrementPendingBytes(-bytes);
            }
        }
    }
}
