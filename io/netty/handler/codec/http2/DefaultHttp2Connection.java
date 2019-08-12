// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http2;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Iterator;
import java.util.Collections;
import io.netty.util.internal.ObjectUtil;
import java.util.LinkedHashSet;
import io.netty.util.collection.IntObjectHashMap;
import java.util.HashSet;
import io.netty.util.collection.IntObjectMap;
import java.util.Set;

public class DefaultHttp2Connection implements Http2Connection
{
    private final Set<Listener> listeners;
    private final IntObjectMap<Http2Stream> streamMap;
    private final ConnectionStream connectionStream;
    private final Set<Http2Stream> activeStreams;
    private final DefaultEndpoint<Http2LocalFlowController> localEndpoint;
    private final DefaultEndpoint<Http2RemoteFlowController> remoteEndpoint;
    private final Http2StreamRemovalPolicy removalPolicy;
    
    public DefaultHttp2Connection(final boolean server) {
        this(server, Http2CodecUtil.immediateRemovalPolicy());
    }
    
    public DefaultHttp2Connection(final boolean server, final Http2StreamRemovalPolicy removalPolicy) {
        this.listeners = new HashSet<Listener>(4);
        this.streamMap = new IntObjectHashMap<Http2Stream>();
        this.connectionStream = new ConnectionStream();
        this.activeStreams = new LinkedHashSet<Http2Stream>();
        this.removalPolicy = ObjectUtil.checkNotNull(removalPolicy, "removalPolicy");
        this.localEndpoint = new DefaultEndpoint<Http2LocalFlowController>(server);
        this.remoteEndpoint = new DefaultEndpoint<Http2RemoteFlowController>(!server);
        removalPolicy.setAction(new Http2StreamRemovalPolicy.Action() {
            @Override
            public void removeStream(final Http2Stream stream) {
                DefaultHttp2Connection.this.removeStream((DefaultStream)stream);
            }
        });
        this.streamMap.put(this.connectionStream.id(), this.connectionStream);
    }
    
    @Override
    public void addListener(final Listener listener) {
        this.listeners.add(listener);
    }
    
    @Override
    public void removeListener(final Listener listener) {
        this.listeners.remove(listener);
    }
    
    @Override
    public boolean isServer() {
        return this.localEndpoint.isServer();
    }
    
    @Override
    public Http2Stream connectionStream() {
        return this.connectionStream;
    }
    
    @Override
    public Http2Stream requireStream(final int streamId) throws Http2Exception {
        final Http2Stream stream = this.stream(streamId);
        if (stream == null) {
            throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Stream does not exist %d", streamId);
        }
        return stream;
    }
    
    @Override
    public Http2Stream stream(final int streamId) {
        return this.streamMap.get(streamId);
    }
    
    @Override
    public int numActiveStreams() {
        return this.activeStreams.size();
    }
    
    @Override
    public Set<Http2Stream> activeStreams() {
        return Collections.unmodifiableSet((Set<? extends Http2Stream>)this.activeStreams);
    }
    
    @Override
    public void deactivate(final Http2Stream stream) {
        this.deactivateInternal((DefaultStream)stream);
    }
    
    @Override
    public Endpoint<Http2LocalFlowController> local() {
        return this.localEndpoint;
    }
    
    @Override
    public Endpoint<Http2RemoteFlowController> remote() {
        return this.remoteEndpoint;
    }
    
    @Override
    public boolean isGoAway() {
        return this.goAwaySent() || this.goAwayReceived();
    }
    
    @Override
    public Http2Stream createLocalStream(final int streamId) throws Http2Exception {
        return this.local().createStream(streamId);
    }
    
    @Override
    public Http2Stream createRemoteStream(final int streamId) throws Http2Exception {
        return this.remote().createStream(streamId);
    }
    
    @Override
    public boolean goAwayReceived() {
        return ((DefaultEndpoint<Http2FlowController>)this.localEndpoint).lastKnownStream >= 0;
    }
    
    @Override
    public void goAwayReceived(final int lastKnownStream) {
        ((DefaultEndpoint<Http2FlowController>)this.localEndpoint).lastKnownStream(lastKnownStream);
    }
    
    @Override
    public boolean goAwaySent() {
        return ((DefaultEndpoint<Http2FlowController>)this.remoteEndpoint).lastKnownStream >= 0;
    }
    
    @Override
    public void goAwaySent(final int lastKnownStream) {
        ((DefaultEndpoint<Http2FlowController>)this.remoteEndpoint).lastKnownStream(lastKnownStream);
    }
    
    private void removeStream(final DefaultStream stream) {
        for (final Listener listener : this.listeners) {
            listener.streamRemoved(stream);
        }
        this.streamMap.remove(stream.id());
        stream.parent().removeChild(stream);
    }
    
    private void activateInternal(final DefaultStream stream) {
        if (this.activeStreams.add(stream)) {
            ((DefaultEndpoint<Http2FlowController>)stream.createdBy()).numActiveStreams++;
            for (final Listener listener : this.listeners) {
                listener.streamActive(stream);
            }
        }
    }
    
    private void deactivateInternal(final DefaultStream stream) {
        if (this.activeStreams.remove(stream)) {
            ((DefaultEndpoint<Http2FlowController>)stream.createdBy()).numActiveStreams--;
            for (final Listener listener : this.listeners) {
                listener.streamInactive(stream);
            }
            this.removalPolicy.markForRemoval(stream);
        }
    }
    
    private static IntObjectMap<DefaultStream> newChildMap() {
        return new IntObjectHashMap<DefaultStream>(4);
    }
    
    private void notifyParentChanged(final List<ParentChangedEvent> events) {
        for (int i = 0; i < events.size(); ++i) {
            final ParentChangedEvent event = events.get(i);
            for (final Listener l : this.listeners) {
                event.notifyListener(l);
            }
        }
    }
    
    private void notifyParentChanging(final Http2Stream stream, final Http2Stream newParent) {
        for (final Listener l : this.listeners) {
            l.priorityTreeParentChanging(stream, newParent);
        }
    }
    
    private class DefaultStream implements Http2Stream
    {
        private final int id;
        private State state;
        private short weight;
        private DefaultStream parent;
        private IntObjectMap<DefaultStream> children;
        private int totalChildWeights;
        private boolean resetSent;
        private PropertyMap data;
        
        DefaultStream(final int id) {
            this.state = State.IDLE;
            this.weight = 16;
            this.children = newChildMap();
            this.id = id;
            this.data = new LazyPropertyMap(this);
        }
        
        @Override
        public final int id() {
            return this.id;
        }
        
        @Override
        public final State state() {
            return this.state;
        }
        
        @Override
        public boolean isResetSent() {
            return this.resetSent;
        }
        
        @Override
        public Http2Stream resetSent() {
            this.resetSent = true;
            return this;
        }
        
        @Override
        public Object setProperty(final Object key, final Object value) {
            return this.data.put(key, value);
        }
        
        @Override
        public <V> V getProperty(final Object key) {
            return this.data.get(key);
        }
        
        @Override
        public <V> V removeProperty(final Object key) {
            return this.data.remove(key);
        }
        
        @Override
        public final boolean isRoot() {
            return this.parent == null;
        }
        
        @Override
        public final short weight() {
            return this.weight;
        }
        
        @Override
        public final int totalChildWeights() {
            return this.totalChildWeights;
        }
        
        @Override
        public final DefaultStream parent() {
            return this.parent;
        }
        
        @Override
        public final boolean isDescendantOf(final Http2Stream stream) {
            for (Http2Stream next = this.parent(); next != null; next = next.parent()) {
                if (next == stream) {
                    return true;
                }
            }
            return false;
        }
        
        @Override
        public final boolean isLeaf() {
            return this.numChildren() == 0;
        }
        
        @Override
        public final int numChildren() {
            return this.children.size();
        }
        
        @Override
        public final Collection<? extends Http2Stream> children() {
            return this.children.values();
        }
        
        @Override
        public final boolean hasChild(final int streamId) {
            return this.child(streamId) != null;
        }
        
        @Override
        public final Http2Stream child(final int streamId) {
            return this.children.get(streamId);
        }
        
        @Override
        public Http2Stream setPriority(final int parentStreamId, final short weight, final boolean exclusive) throws Http2Exception {
            if (weight < 1 || weight > 256) {
                throw new IllegalArgumentException(String.format("Invalid weight: %d.  Must be between %d and %d (inclusive).", weight, 1, 256));
            }
            DefaultStream newParent = (DefaultStream)DefaultHttp2Connection.this.stream(parentStreamId);
            if (newParent == null) {
                newParent = this.createdBy().createStream(parentStreamId);
            }
            else if (this == newParent) {
                throw new IllegalArgumentException("A stream cannot depend on itself");
            }
            this.weight(weight);
            if (newParent != this.parent() || exclusive) {
                List<ParentChangedEvent> events;
                if (newParent.isDescendantOf(this)) {
                    events = new ArrayList<ParentChangedEvent>(2 + (exclusive ? newParent.numChildren() : 0));
                    this.parent.takeChild(newParent, false, events);
                }
                else {
                    events = new ArrayList<ParentChangedEvent>(1 + (exclusive ? newParent.numChildren() : 0));
                }
                newParent.takeChild(this, exclusive, events);
                DefaultHttp2Connection.this.notifyParentChanged(events);
            }
            return this;
        }
        
        @Override
        public Http2Stream open(final boolean halfClosed) throws Http2Exception {
            switch (this.state) {
                case IDLE: {
                    this.state = (halfClosed ? (this.isLocal() ? State.HALF_CLOSED_LOCAL : State.HALF_CLOSED_REMOTE) : State.OPEN);
                    break;
                }
                case RESERVED_LOCAL: {
                    this.state = State.HALF_CLOSED_REMOTE;
                    break;
                }
                case RESERVED_REMOTE: {
                    this.state = State.HALF_CLOSED_LOCAL;
                    break;
                }
                default: {
                    throw Http2Exception.streamError(this.id, Http2Error.PROTOCOL_ERROR, "Attempting to open a stream in an invalid state: " + this.state, new Object[0]);
                }
            }
            DefaultHttp2Connection.this.activateInternal(this);
            return this;
        }
        
        @Override
        public Http2Stream close() {
            if (this.state == State.CLOSED) {
                return this;
            }
            this.state = State.CLOSED;
            DefaultHttp2Connection.this.deactivateInternal(this);
            return this;
        }
        
        @Override
        public Http2Stream closeLocalSide() {
            switch (this.state) {
                case OPEN: {
                    this.state = State.HALF_CLOSED_LOCAL;
                    this.notifyHalfClosed(this);
                    break;
                }
                case HALF_CLOSED_LOCAL: {
                    break;
                }
                default: {
                    this.close();
                    break;
                }
            }
            return this;
        }
        
        @Override
        public Http2Stream closeRemoteSide() {
            switch (this.state) {
                case OPEN: {
                    this.state = State.HALF_CLOSED_REMOTE;
                    this.notifyHalfClosed(this);
                    break;
                }
                case HALF_CLOSED_REMOTE: {
                    break;
                }
                default: {
                    this.close();
                    break;
                }
            }
            return this;
        }
        
        private void notifyHalfClosed(final Http2Stream stream) {
            for (final Listener listener : DefaultHttp2Connection.this.listeners) {
                listener.streamHalfClosed(stream);
            }
        }
        
        @Override
        public final boolean remoteSideOpen() {
            return this.state == State.HALF_CLOSED_LOCAL || this.state == State.OPEN || this.state == State.RESERVED_REMOTE;
        }
        
        @Override
        public final boolean localSideOpen() {
            return this.state == State.HALF_CLOSED_REMOTE || this.state == State.OPEN || this.state == State.RESERVED_LOCAL;
        }
        
        final DefaultEndpoint<? extends Http2FlowController> createdBy() {
            return (DefaultEndpoint<? extends Http2FlowController>)(DefaultHttp2Connection.this.localEndpoint.createdStreamId(this.id) ? DefaultHttp2Connection.this.localEndpoint : DefaultHttp2Connection.this.remoteEndpoint);
        }
        
        final boolean isLocal() {
            return DefaultHttp2Connection.this.localEndpoint.createdStreamId(this.id);
        }
        
        final void weight(final short weight) {
            if (weight != this.weight) {
                if (this.parent != null) {
                    final int delta = weight - this.weight;
                    final DefaultStream parent = this.parent;
                    parent.totalChildWeights += delta;
                }
                final short oldWeight = this.weight;
                this.weight = weight;
                for (final Listener l : DefaultHttp2Connection.this.listeners) {
                    l.onWeightChanged(this, oldWeight);
                }
            }
        }
        
        final IntObjectMap<DefaultStream> removeAllChildren() {
            this.totalChildWeights = 0;
            final IntObjectMap<DefaultStream> prevChildren = this.children;
            this.children = newChildMap();
            return prevChildren;
        }
        
        final void takeChild(final DefaultStream child, final boolean exclusive, final List<ParentChangedEvent> events) {
            final DefaultStream oldParent = child.parent();
            events.add(new ParentChangedEvent(child, oldParent));
            DefaultHttp2Connection.this.notifyParentChanging(child, this);
            child.parent = this;
            if (exclusive && !this.children.isEmpty()) {
                for (final DefaultStream grandchild : this.removeAllChildren().values()) {
                    child.takeChild(grandchild, false, events);
                }
            }
            if (this.children.put(child.id(), child) == null) {
                this.totalChildWeights += child.weight();
            }
            if (oldParent != null && oldParent.children.remove(child.id()) != null) {
                final DefaultStream defaultStream = oldParent;
                defaultStream.totalChildWeights -= child.weight();
            }
        }
        
        final void removeChild(final DefaultStream child) {
            if (this.children.remove(child.id()) != null) {
                final List<ParentChangedEvent> events = new ArrayList<ParentChangedEvent>(1 + child.children.size());
                events.add(new ParentChangedEvent(child, child.parent()));
                DefaultHttp2Connection.this.notifyParentChanging(child, null);
                child.parent = null;
                this.totalChildWeights -= child.weight();
                for (final DefaultStream grandchild : child.children.values()) {
                    this.takeChild(grandchild, false, events);
                }
                DefaultHttp2Connection.this.notifyParentChanged(events);
            }
        }
    }
    
    private static final class DefaultProperyMap implements PropertyMap
    {
        private final Map<Object, Object> data;
        
        DefaultProperyMap(final int initialSize) {
            this.data = new HashMap<Object, Object>(initialSize);
        }
        
        @Override
        public Object put(final Object key, final Object value) {
            return this.data.put(key, value);
        }
        
        @Override
        public <V> V get(final Object key) {
            return (V)this.data.get(key);
        }
        
        @Override
        public <V> V remove(final Object key) {
            return (V)this.data.remove(key);
        }
    }
    
    private static final class LazyPropertyMap implements PropertyMap
    {
        private static final int DEFAULT_INITIAL_SIZE = 4;
        private final DefaultStream stream;
        
        LazyPropertyMap(final DefaultStream stream) {
            this.stream = stream;
        }
        
        @Override
        public Object put(final Object key, final Object value) {
            this.stream.data = new DefaultProperyMap(4);
            return this.stream.data.put(key, value);
        }
        
        @Override
        public <V> V get(final Object key) {
            this.stream.data = new DefaultProperyMap(4);
            return this.stream.data.get(key);
        }
        
        @Override
        public <V> V remove(final Object key) {
            this.stream.data = new DefaultProperyMap(4);
            return this.stream.data.remove(key);
        }
    }
    
    private static final class ParentChangedEvent
    {
        private final Http2Stream stream;
        private final Http2Stream oldParent;
        
        ParentChangedEvent(final Http2Stream stream, final Http2Stream oldParent) {
            this.stream = stream;
            this.oldParent = oldParent;
        }
        
        public void notifyListener(final Listener l) {
            l.priorityTreeParentChanged(this.stream, this.oldParent);
        }
    }
    
    private final class ConnectionStream extends DefaultStream
    {
        ConnectionStream() {
            super(0);
        }
        
        @Override
        public Http2Stream setPriority(final int parentStreamId, final short weight, final boolean exclusive) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public Http2Stream open(final boolean halfClosed) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public Http2Stream close() {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public Http2Stream closeLocalSide() {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public Http2Stream closeRemoteSide() {
            throw new UnsupportedOperationException();
        }
    }
    
    private final class DefaultEndpoint<F extends Http2FlowController> implements Endpoint<F>
    {
        private final boolean server;
        private int nextStreamId;
        private int lastStreamCreated;
        private int lastKnownStream;
        private boolean pushToAllowed;
        private F flowController;
        private int maxStreams;
        private int numActiveStreams;
        
        DefaultEndpoint(final boolean server) {
            this.lastKnownStream = -1;
            this.pushToAllowed = true;
            this.server = server;
            this.nextStreamId = (server ? 2 : 1);
            this.pushToAllowed = !server;
            this.maxStreams = Integer.MAX_VALUE;
        }
        
        @Override
        public int nextStreamId() {
            return (this.nextStreamId > 1) ? this.nextStreamId : (this.nextStreamId + 2);
        }
        
        @Override
        public boolean createdStreamId(final int streamId) {
            final boolean even = (streamId & 0x1) == 0x0;
            return this.server == even;
        }
        
        @Override
        public boolean acceptingNewStreams() {
            return this.nextStreamId() > 0 && this.numActiveStreams + 1 <= this.maxStreams;
        }
        
        @Override
        public DefaultStream createStream(final int streamId) throws Http2Exception {
            this.checkNewStreamAllowed(streamId);
            final DefaultStream stream = new DefaultStream(streamId);
            this.nextStreamId = streamId + 2;
            this.lastStreamCreated = streamId;
            this.addStream(stream);
            return stream;
        }
        
        @Override
        public boolean isServer() {
            return this.server;
        }
        
        @Override
        public DefaultStream reservePushStream(final int streamId, final Http2Stream parent) throws Http2Exception {
            if (parent == null) {
                throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Parent stream missing", new Object[0]);
            }
            Label_0070: {
                if (this.isLocal()) {
                    if (parent.localSideOpen()) {
                        break Label_0070;
                    }
                }
                else if (parent.remoteSideOpen()) {
                    break Label_0070;
                }
                throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Stream %d is not open for sending push promise", parent.id());
            }
            if (!this.opposite().allowPushTo()) {
                throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Server push not allowed to opposite endpoint.", new Object[0]);
            }
            this.checkNewStreamAllowed(streamId);
            final DefaultStream stream = new DefaultStream(streamId);
            stream.state = (this.isLocal() ? Http2Stream.State.RESERVED_LOCAL : Http2Stream.State.RESERVED_REMOTE);
            this.nextStreamId = streamId + 2;
            this.lastStreamCreated = streamId;
            this.addStream(stream);
            return stream;
        }
        
        private void addStream(final DefaultStream stream) {
            DefaultHttp2Connection.this.streamMap.put(stream.id(), stream);
            final List<ParentChangedEvent> events = new ArrayList<ParentChangedEvent>(1);
            DefaultHttp2Connection.this.connectionStream.takeChild(stream, false, events);
            for (final Listener listener : DefaultHttp2Connection.this.listeners) {
                listener.streamAdded(stream);
            }
            DefaultHttp2Connection.this.notifyParentChanged(events);
        }
        
        @Override
        public void allowPushTo(final boolean allow) {
            if (allow && this.server) {
                throw new IllegalArgumentException("Servers do not allow push");
            }
            this.pushToAllowed = allow;
        }
        
        @Override
        public boolean allowPushTo() {
            return this.pushToAllowed;
        }
        
        @Override
        public int numActiveStreams() {
            return this.numActiveStreams;
        }
        
        @Override
        public int maxStreams() {
            return this.maxStreams;
        }
        
        @Override
        public void maxStreams(final int maxStreams) {
            this.maxStreams = maxStreams;
        }
        
        @Override
        public int lastStreamCreated() {
            return this.lastStreamCreated;
        }
        
        @Override
        public int lastKnownStream() {
            return (this.lastKnownStream >= 0) ? this.lastKnownStream : this.lastStreamCreated;
        }
        
        private void lastKnownStream(final int lastKnownStream) {
            final boolean alreadyNotified = DefaultHttp2Connection.this.isGoAway();
            this.lastKnownStream = lastKnownStream;
            if (!alreadyNotified) {
                this.notifyGoingAway();
            }
        }
        
        private void notifyGoingAway() {
            for (final Listener listener : DefaultHttp2Connection.this.listeners) {
                listener.goingAway();
            }
        }
        
        @Override
        public F flowController() {
            return this.flowController;
        }
        
        @Override
        public void flowController(final F flowController) {
            this.flowController = ObjectUtil.checkNotNull(flowController, "flowController");
        }
        
        @Override
        public Endpoint<? extends Http2FlowController> opposite() {
            return this.isLocal() ? DefaultHttp2Connection.this.remoteEndpoint : DefaultHttp2Connection.this.localEndpoint;
        }
        
        private void checkNewStreamAllowed(final int streamId) throws Http2Exception {
            if (DefaultHttp2Connection.this.isGoAway()) {
                throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Cannot create a stream since the connection is going away", new Object[0]);
            }
            this.verifyStreamId(streamId);
            if (!this.acceptingNewStreams()) {
                throw Http2Exception.connectionError(Http2Error.REFUSED_STREAM, "Maximum streams exceeded for this endpoint.", new Object[0]);
            }
        }
        
        private void verifyStreamId(final int streamId) throws Http2Exception {
            if (streamId < 0) {
                throw new Http2NoMoreStreamIdsException();
            }
            if (streamId < this.nextStreamId) {
                throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Request stream %d is behind the next expected stream %d", streamId, this.nextStreamId);
            }
            if (!this.createdStreamId(streamId)) {
                throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Request stream %d is not correct for %s connection", streamId, this.server ? "server" : "client");
            }
        }
        
        private boolean isLocal() {
            return this == DefaultHttp2Connection.this.localEndpoint;
        }
    }
    
    private interface PropertyMap
    {
        Object put(final Object p0, final Object p1);
        
         <V> V get(final Object p0);
        
         <V> V remove(final Object p0);
    }
}
