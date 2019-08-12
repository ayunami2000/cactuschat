// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http2;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.PlatformDependent;
import io.netty.handler.codec.Headers;
import java.util.Map;
import io.netty.handler.codec.TextHeaders;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.util.collection.IntObjectMap;
import io.netty.handler.codec.AsciiString;

public final class InboundHttp2ToHttpPriorityAdapter extends InboundHttp2ToHttpAdapter
{
    private static final AsciiString OUT_OF_MESSAGE_SEQUENCE_METHOD;
    private static final AsciiString OUT_OF_MESSAGE_SEQUENCE_PATH;
    private static final AsciiString OUT_OF_MESSAGE_SEQUENCE_RETURN_CODE;
    private final IntObjectMap<HttpHeaders> outOfMessageFlowHeaders;
    
    InboundHttp2ToHttpPriorityAdapter(final Builder builder) {
        super(builder);
        this.outOfMessageFlowHeaders = new IntObjectHashMap<HttpHeaders>();
    }
    
    @Override
    protected void removeMessage(final int streamId) {
        super.removeMessage(streamId);
        this.outOfMessageFlowHeaders.remove(streamId);
    }
    
    private static HttpHeaders getActiveHeaders(final FullHttpMessage msg) {
        return msg.content().isReadable() ? msg.trailingHeaders() : msg.headers();
    }
    
    private void importOutOfMessageFlowHeaders(final int streamId, final HttpHeaders headers) {
        final HttpHeaders outOfMessageFlowHeader = this.outOfMessageFlowHeaders.get(streamId);
        if (outOfMessageFlowHeader == null) {
            this.outOfMessageFlowHeaders.put(streamId, headers);
        }
        else {
            outOfMessageFlowHeader.setAll((TextHeaders)headers);
        }
    }
    
    private void exportOutOfMessageFlowHeaders(final int streamId, final HttpHeaders headers) {
        final HttpHeaders outOfMessageFlowHeader = this.outOfMessageFlowHeaders.get(streamId);
        if (outOfMessageFlowHeader != null) {
            headers.setAll((TextHeaders)outOfMessageFlowHeader);
        }
    }
    
    private static void removePriorityRelatedHeaders(final HttpHeaders headers) {
        ((Headers<AsciiString>)headers).remove(HttpUtil.ExtensionHeaderNames.STREAM_DEPENDENCY_ID.text());
        ((Headers<AsciiString>)headers).remove(HttpUtil.ExtensionHeaderNames.STREAM_WEIGHT.text());
    }
    
    private void initializePseudoHeaders(final Http2Headers headers) {
        if (this.connection.isServer()) {
            headers.method(InboundHttp2ToHttpPriorityAdapter.OUT_OF_MESSAGE_SEQUENCE_METHOD).path(InboundHttp2ToHttpPriorityAdapter.OUT_OF_MESSAGE_SEQUENCE_PATH);
        }
        else {
            headers.status(InboundHttp2ToHttpPriorityAdapter.OUT_OF_MESSAGE_SEQUENCE_RETURN_CODE);
        }
    }
    
    private static void addHttpHeadersToHttp2Headers(final HttpHeaders httpHeaders, final Http2Headers http2Headers) {
        try {
            httpHeaders.forEachEntry(new TextHeaders.EntryVisitor() {
                @Override
                public boolean visit(final Map.Entry<CharSequence, CharSequence> entry) throws Exception {
                    http2Headers.add(AsciiString.of(entry.getKey()), AsciiString.of(entry.getValue()));
                    return true;
                }
            });
        }
        catch (Exception ex) {
            PlatformDependent.throwException(ex);
        }
    }
    
    @Override
    protected void fireChannelRead(final ChannelHandlerContext ctx, final FullHttpMessage msg, final int streamId) {
        this.exportOutOfMessageFlowHeaders(streamId, getActiveHeaders(msg));
        super.fireChannelRead(ctx, msg, streamId);
    }
    
    @Override
    protected FullHttpMessage processHeadersBegin(final ChannelHandlerContext ctx, final int streamId, final Http2Headers headers, final boolean endOfStream, final boolean allowAppend, final boolean appendToTrailer) throws Http2Exception {
        final FullHttpMessage msg = super.processHeadersBegin(ctx, streamId, headers, endOfStream, allowAppend, appendToTrailer);
        if (msg != null) {
            this.exportOutOfMessageFlowHeaders(streamId, getActiveHeaders(msg));
        }
        return msg;
    }
    
    @Override
    public void priorityTreeParentChanged(final Http2Stream stream, final Http2Stream oldParent) {
        final Http2Stream parent = stream.parent();
        final FullHttpMessage msg = this.messageMap.get(stream.id());
        if (msg == null) {
            if (parent != null && !parent.equals(this.connection.connectionStream())) {
                final HttpHeaders headers = new DefaultHttpHeaders();
                headers.setInt((CharSequence)HttpUtil.ExtensionHeaderNames.STREAM_DEPENDENCY_ID.text(), parent.id());
                this.importOutOfMessageFlowHeaders(stream.id(), headers);
            }
        }
        else if (parent == null) {
            removePriorityRelatedHeaders(msg.headers());
            removePriorityRelatedHeaders(msg.trailingHeaders());
        }
        else if (!parent.equals(this.connection.connectionStream())) {
            final HttpHeaders headers = getActiveHeaders(msg);
            headers.setInt((CharSequence)HttpUtil.ExtensionHeaderNames.STREAM_DEPENDENCY_ID.text(), parent.id());
        }
    }
    
    @Override
    public void onWeightChanged(final Http2Stream stream, final short oldWeight) {
        final FullHttpMessage msg = this.messageMap.get(stream.id());
        HttpHeaders headers;
        if (msg == null) {
            headers = new DefaultHttpHeaders();
            this.importOutOfMessageFlowHeaders(stream.id(), headers);
        }
        else {
            headers = getActiveHeaders(msg);
        }
        headers.setShort((CharSequence)HttpUtil.ExtensionHeaderNames.STREAM_WEIGHT.text(), stream.weight());
    }
    
    @Override
    public void onPriorityRead(final ChannelHandlerContext ctx, final int streamId, final int streamDependency, final short weight, final boolean exclusive) throws Http2Exception {
        FullHttpMessage msg = this.messageMap.get(streamId);
        if (msg == null) {
            final HttpHeaders httpHeaders = this.outOfMessageFlowHeaders.remove(streamId);
            if (httpHeaders == null) {
                throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Priority Frame recieved for unknown stream id %d", streamId);
            }
            final Http2Headers http2Headers = new DefaultHttp2Headers();
            this.initializePseudoHeaders(http2Headers);
            addHttpHeadersToHttp2Headers(httpHeaders, http2Headers);
            msg = this.newMessage(streamId, http2Headers, this.validateHttpHeaders);
            this.fireChannelRead(ctx, msg, streamId);
        }
    }
    
    static {
        OUT_OF_MESSAGE_SEQUENCE_METHOD = new AsciiString(HttpUtil.OUT_OF_MESSAGE_SEQUENCE_METHOD.toString());
        OUT_OF_MESSAGE_SEQUENCE_PATH = new AsciiString("");
        OUT_OF_MESSAGE_SEQUENCE_RETURN_CODE = new AsciiString(HttpUtil.OUT_OF_MESSAGE_SEQUENCE_RETURN_CODE.toString());
    }
    
    public static final class Builder extends InboundHttp2ToHttpAdapter.Builder
    {
        public Builder(final Http2Connection connection) {
            super(connection);
        }
        
        @Override
        public InboundHttp2ToHttpPriorityAdapter build() {
            final InboundHttp2ToHttpPriorityAdapter instance = new InboundHttp2ToHttpPriorityAdapter(this);
            instance.connection.addListener(instance);
            return instance;
        }
    }
}
