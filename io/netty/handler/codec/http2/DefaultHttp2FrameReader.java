// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http2;

import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class DefaultHttp2FrameReader implements Http2FrameReader, Http2FrameSizePolicy, Configuration
{
    private final Http2HeadersDecoder headersDecoder;
    private State state;
    private byte frameType;
    private int streamId;
    private Http2Flags flags;
    private int payloadLength;
    private HeadersContinuation headersContinuation;
    private int maxFrameSize;
    
    public DefaultHttp2FrameReader() {
        this(new DefaultHttp2HeadersDecoder());
    }
    
    public DefaultHttp2FrameReader(final Http2HeadersDecoder headersDecoder) {
        this.state = State.FRAME_HEADER;
        this.headersDecoder = headersDecoder;
        this.maxFrameSize = 16384;
    }
    
    @Override
    public Http2HeaderTable headerTable() {
        return this.headersDecoder.configuration().headerTable();
    }
    
    @Override
    public Configuration configuration() {
        return this;
    }
    
    @Override
    public Http2FrameSizePolicy frameSizePolicy() {
        return this;
    }
    
    @Override
    public void maxFrameSize(final int max) throws Http2Exception {
        if (!Http2CodecUtil.isMaxFrameSizeValid(max)) {
            throw Http2Exception.streamError(this.streamId, Http2Error.FRAME_SIZE_ERROR, "Invalid MAX_FRAME_SIZE specified in sent settings: %d", max);
        }
        this.maxFrameSize = max;
    }
    
    @Override
    public int maxFrameSize() {
        return this.maxFrameSize;
    }
    
    @Override
    public void close() {
        if (this.headersContinuation != null) {
            this.headersContinuation.close();
        }
    }
    
    @Override
    public void readFrame(final ChannelHandlerContext ctx, final ByteBuf input, final Http2FrameListener listener) throws Http2Exception {
        try {
            while (input.isReadable()) {
                switch (this.state) {
                    case FRAME_HEADER: {
                        this.processHeaderState(input);
                        if (this.state == State.FRAME_HEADER) {
                            return;
                        }
                    }
                    case FRAME_PAYLOAD: {
                        this.processPayloadState(ctx, input, listener);
                        if (this.state == State.FRAME_PAYLOAD) {
                            return;
                        }
                        continue;
                    }
                    case ERROR: {
                        input.skipBytes(input.readableBytes());
                    }
                    default: {
                        throw new IllegalStateException("Should never get here");
                    }
                }
            }
        }
        catch (Http2Exception e) {
            this.state = State.ERROR;
            throw e;
        }
        catch (RuntimeException e2) {
            this.state = State.ERROR;
            throw e2;
        }
        catch (Error e3) {
            this.state = State.ERROR;
            throw e3;
        }
    }
    
    private void processHeaderState(final ByteBuf in) throws Http2Exception {
        if (in.readableBytes() < 9) {
            return;
        }
        this.payloadLength = in.readUnsignedMedium();
        if (this.payloadLength > this.maxFrameSize) {
            throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Frame length: %d exceeds maximum: %d", this.payloadLength, this.maxFrameSize);
        }
        this.frameType = in.readByte();
        this.flags = new Http2Flags(in.readUnsignedByte());
        this.streamId = Http2CodecUtil.readUnsignedInt(in);
        switch (this.frameType) {
            case 0: {
                this.verifyDataFrame();
                break;
            }
            case 1: {
                this.verifyHeadersFrame();
                break;
            }
            case 2: {
                this.verifyPriorityFrame();
                break;
            }
            case 3: {
                this.verifyRstStreamFrame();
                break;
            }
            case 4: {
                this.verifySettingsFrame();
                break;
            }
            case 5: {
                this.verifyPushPromiseFrame();
                break;
            }
            case 6: {
                this.verifyPingFrame();
                break;
            }
            case 7: {
                this.verifyGoAwayFrame();
                break;
            }
            case 8: {
                this.verifyWindowUpdateFrame();
                break;
            }
            case 9: {
                this.verifyContinuationFrame();
                break;
            }
        }
        this.state = State.FRAME_PAYLOAD;
    }
    
    private void processPayloadState(final ChannelHandlerContext ctx, final ByteBuf in, final Http2FrameListener listener) throws Http2Exception {
        if (in.readableBytes() < this.payloadLength) {
            return;
        }
        final ByteBuf payload = in.readSlice(this.payloadLength);
        switch (this.frameType) {
            case 0: {
                this.readDataFrame(ctx, payload, listener);
                break;
            }
            case 1: {
                this.readHeadersFrame(ctx, payload, listener);
                break;
            }
            case 2: {
                this.readPriorityFrame(ctx, payload, listener);
                break;
            }
            case 3: {
                this.readRstStreamFrame(ctx, payload, listener);
                break;
            }
            case 4: {
                this.readSettingsFrame(ctx, payload, listener);
                break;
            }
            case 5: {
                this.readPushPromiseFrame(ctx, payload, listener);
                break;
            }
            case 6: {
                this.readPingFrame(ctx, payload, listener);
                break;
            }
            case 7: {
                readGoAwayFrame(ctx, payload, listener);
                break;
            }
            case 8: {
                this.readWindowUpdateFrame(ctx, payload, listener);
                break;
            }
            case 9: {
                this.readContinuationFrame(payload, listener);
                break;
            }
            default: {
                this.readUnknownFrame(ctx, payload, listener);
                break;
            }
        }
        this.state = State.FRAME_HEADER;
    }
    
    private void verifyDataFrame() throws Http2Exception {
        this.verifyNotProcessingHeaders();
        this.verifyPayloadLength(this.payloadLength);
        if (this.payloadLength < this.flags.getPaddingPresenceFieldLength()) {
            throw Http2Exception.streamError(this.streamId, Http2Error.FRAME_SIZE_ERROR, "Frame length %d too small.", this.payloadLength);
        }
    }
    
    private void verifyHeadersFrame() throws Http2Exception {
        this.verifyNotProcessingHeaders();
        this.verifyPayloadLength(this.payloadLength);
        final int requiredLength = this.flags.getPaddingPresenceFieldLength() + this.flags.getNumPriorityBytes();
        if (this.payloadLength < requiredLength) {
            throw Http2Exception.streamError(this.streamId, Http2Error.FRAME_SIZE_ERROR, "Frame length too small." + this.payloadLength, new Object[0]);
        }
    }
    
    private void verifyPriorityFrame() throws Http2Exception {
        this.verifyNotProcessingHeaders();
        if (this.payloadLength != 5) {
            throw Http2Exception.streamError(this.streamId, Http2Error.FRAME_SIZE_ERROR, "Invalid frame length %d.", this.payloadLength);
        }
    }
    
    private void verifyRstStreamFrame() throws Http2Exception {
        this.verifyNotProcessingHeaders();
        if (this.payloadLength != 4) {
            throw Http2Exception.streamError(this.streamId, Http2Error.FRAME_SIZE_ERROR, "Invalid frame length %d.", this.payloadLength);
        }
    }
    
    private void verifySettingsFrame() throws Http2Exception {
        this.verifyNotProcessingHeaders();
        this.verifyPayloadLength(this.payloadLength);
        if (this.streamId != 0) {
            throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "A stream ID must be zero.", new Object[0]);
        }
        if (this.flags.ack() && this.payloadLength > 0) {
            throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Ack settings frame must have an empty payload.", new Object[0]);
        }
        if (this.payloadLength % 6 > 0) {
            throw Http2Exception.connectionError(Http2Error.FRAME_SIZE_ERROR, "Frame length %d invalid.", this.payloadLength);
        }
    }
    
    private void verifyPushPromiseFrame() throws Http2Exception {
        this.verifyNotProcessingHeaders();
        this.verifyPayloadLength(this.payloadLength);
        final int minLength = this.flags.getPaddingPresenceFieldLength() + 4;
        if (this.payloadLength < minLength) {
            throw Http2Exception.streamError(this.streamId, Http2Error.FRAME_SIZE_ERROR, "Frame length %d too small.", this.payloadLength);
        }
    }
    
    private void verifyPingFrame() throws Http2Exception {
        this.verifyNotProcessingHeaders();
        if (this.streamId != 0) {
            throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "A stream ID must be zero.", new Object[0]);
        }
        if (this.payloadLength != 8) {
            throw Http2Exception.connectionError(Http2Error.FRAME_SIZE_ERROR, "Frame length %d incorrect size for ping.", this.payloadLength);
        }
    }
    
    private void verifyGoAwayFrame() throws Http2Exception {
        this.verifyNotProcessingHeaders();
        this.verifyPayloadLength(this.payloadLength);
        if (this.streamId != 0) {
            throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "A stream ID must be zero.", new Object[0]);
        }
        if (this.payloadLength < 8) {
            throw Http2Exception.connectionError(Http2Error.FRAME_SIZE_ERROR, "Frame length %d too small.", this.payloadLength);
        }
    }
    
    private void verifyWindowUpdateFrame() throws Http2Exception {
        this.verifyNotProcessingHeaders();
        verifyStreamOrConnectionId(this.streamId, "Stream ID");
        if (this.payloadLength != 4) {
            throw Http2Exception.streamError(this.streamId, Http2Error.FRAME_SIZE_ERROR, "Invalid frame length %d.", this.payloadLength);
        }
    }
    
    private void verifyContinuationFrame() throws Http2Exception {
        this.verifyPayloadLength(this.payloadLength);
        if (this.headersContinuation == null) {
            throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Received %s frame but not currently processing headers.", this.frameType);
        }
        if (this.streamId != this.headersContinuation.getStreamId()) {
            throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Continuation stream ID does not match pending headers. Expected %d, but received %d.", this.headersContinuation.getStreamId(), this.streamId);
        }
        if (this.payloadLength < this.flags.getPaddingPresenceFieldLength()) {
            throw Http2Exception.streamError(this.streamId, Http2Error.FRAME_SIZE_ERROR, "Frame length %d too small for padding.", this.payloadLength);
        }
    }
    
    private void readDataFrame(final ChannelHandlerContext ctx, final ByteBuf payload, final Http2FrameListener listener) throws Http2Exception {
        final short padding = this.readPadding(payload);
        final int dataLength = payload.readableBytes() - padding;
        if (dataLength < 0) {
            throw Http2Exception.streamError(this.streamId, Http2Error.FRAME_SIZE_ERROR, "Frame payload too small for padding.", new Object[0]);
        }
        final ByteBuf data = payload.readSlice(dataLength);
        listener.onDataRead(ctx, this.streamId, data, padding, this.flags.endOfStream());
        payload.skipBytes(payload.readableBytes());
    }
    
    private void readHeadersFrame(final ChannelHandlerContext ctx, final ByteBuf payload, final Http2FrameListener listener) throws Http2Exception {
        final int headersStreamId = this.streamId;
        final Http2Flags headersFlags = this.flags;
        final int padding = this.readPadding(payload);
        if (this.flags.priorityPresent()) {
            final long word1 = payload.readUnsignedInt();
            final boolean exclusive = (word1 & 0x80000000L) != 0x0L;
            final int streamDependency = (int)(word1 & 0x7FFFFFFFL);
            final short weight = (short)(payload.readUnsignedByte() + 1);
            final ByteBuf fragment = payload.readSlice(payload.readableBytes() - padding);
            (this.headersContinuation = new HeadersContinuation() {
                public int getStreamId() {
                    return headersStreamId;
                }
                
                public void processFragment(final boolean endOfHeaders, final ByteBuf fragment, final Http2FrameListener listener) throws Http2Exception {
                    final HeadersBlockBuilder hdrBlockBuilder = this.headersBlockBuilder();
                    hdrBlockBuilder.addFragment(fragment, ctx.alloc(), endOfHeaders);
                    if (endOfHeaders) {
                        listener.onHeadersRead(ctx, headersStreamId, hdrBlockBuilder.headers(), streamDependency, weight, exclusive, padding, headersFlags.endOfStream());
                        this.close();
                    }
                }
            }).processFragment(this.flags.endOfHeaders(), fragment, listener);
            return;
        }
        this.headersContinuation = new HeadersContinuation() {
            public int getStreamId() {
                return headersStreamId;
            }
            
            public void processFragment(final boolean endOfHeaders, final ByteBuf fragment, final Http2FrameListener listener) throws Http2Exception {
                final HeadersBlockBuilder hdrBlockBuilder = this.headersBlockBuilder();
                hdrBlockBuilder.addFragment(fragment, ctx.alloc(), endOfHeaders);
                if (endOfHeaders) {
                    listener.onHeadersRead(ctx, headersStreamId, hdrBlockBuilder.headers(), padding, headersFlags.endOfStream());
                    this.close();
                }
            }
        };
        final ByteBuf fragment2 = payload.readSlice(payload.readableBytes() - padding);
        this.headersContinuation.processFragment(this.flags.endOfHeaders(), fragment2, listener);
    }
    
    private void readPriorityFrame(final ChannelHandlerContext ctx, final ByteBuf payload, final Http2FrameListener listener) throws Http2Exception {
        final long word1 = payload.readUnsignedInt();
        final boolean exclusive = (word1 & 0x80000000L) != 0x0L;
        final int streamDependency = (int)(word1 & 0x7FFFFFFFL);
        final short weight = (short)(payload.readUnsignedByte() + 1);
        listener.onPriorityRead(ctx, this.streamId, streamDependency, weight, exclusive);
    }
    
    private void readRstStreamFrame(final ChannelHandlerContext ctx, final ByteBuf payload, final Http2FrameListener listener) throws Http2Exception {
        final long errorCode = payload.readUnsignedInt();
        listener.onRstStreamRead(ctx, this.streamId, errorCode);
    }
    
    private void readSettingsFrame(final ChannelHandlerContext ctx, final ByteBuf payload, final Http2FrameListener listener) throws Http2Exception {
        if (this.flags.ack()) {
            listener.onSettingsAckRead(ctx);
        }
        else {
            final int numSettings = this.payloadLength / 6;
            final Http2Settings settings = new Http2Settings();
            for (int index = 0; index < numSettings; ++index) {
                final int id = payload.readUnsignedShort();
                final long value = payload.readUnsignedInt();
                try {
                    settings.put(id, value);
                }
                catch (IllegalArgumentException e) {
                    switch (id) {
                        case 5: {
                            throw Http2Exception.connectionError(Http2Error.FRAME_SIZE_ERROR, e, e.getMessage(), new Object[0]);
                        }
                        case 4: {
                            throw Http2Exception.connectionError(Http2Error.FLOW_CONTROL_ERROR, e, e.getMessage(), new Object[0]);
                        }
                        default: {
                            throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, e, e.getMessage(), new Object[0]);
                        }
                    }
                }
            }
            listener.onSettingsRead(ctx, settings);
        }
    }
    
    private void readPushPromiseFrame(final ChannelHandlerContext ctx, final ByteBuf payload, final Http2FrameListener listener) throws Http2Exception {
        final int pushPromiseStreamId = this.streamId;
        final int padding = this.readPadding(payload);
        final int promisedStreamId = Http2CodecUtil.readUnsignedInt(payload);
        this.headersContinuation = new HeadersContinuation() {
            public int getStreamId() {
                return pushPromiseStreamId;
            }
            
            public void processFragment(final boolean endOfHeaders, final ByteBuf fragment, final Http2FrameListener listener) throws Http2Exception {
                this.headersBlockBuilder().addFragment(fragment, ctx.alloc(), endOfHeaders);
                if (endOfHeaders) {
                    final Http2Headers headers = this.headersBlockBuilder().headers();
                    listener.onPushPromiseRead(ctx, pushPromiseStreamId, promisedStreamId, headers, padding);
                    this.close();
                }
            }
        };
        final ByteBuf fragment = payload.readSlice(payload.readableBytes() - padding);
        this.headersContinuation.processFragment(this.flags.endOfHeaders(), fragment, listener);
    }
    
    private void readPingFrame(final ChannelHandlerContext ctx, final ByteBuf payload, final Http2FrameListener listener) throws Http2Exception {
        final ByteBuf data = payload.readSlice(payload.readableBytes());
        if (this.flags.ack()) {
            listener.onPingAckRead(ctx, data);
        }
        else {
            listener.onPingRead(ctx, data);
        }
    }
    
    private static void readGoAwayFrame(final ChannelHandlerContext ctx, final ByteBuf payload, final Http2FrameListener listener) throws Http2Exception {
        final int lastStreamId = Http2CodecUtil.readUnsignedInt(payload);
        final long errorCode = payload.readUnsignedInt();
        final ByteBuf debugData = payload.readSlice(payload.readableBytes());
        listener.onGoAwayRead(ctx, lastStreamId, errorCode, debugData);
    }
    
    private void readWindowUpdateFrame(final ChannelHandlerContext ctx, final ByteBuf payload, final Http2FrameListener listener) throws Http2Exception {
        final int windowSizeIncrement = Http2CodecUtil.readUnsignedInt(payload);
        if (windowSizeIncrement == 0) {
            throw Http2Exception.streamError(this.streamId, Http2Error.PROTOCOL_ERROR, "Received WINDOW_UPDATE with delta 0 for stream: %d", this.streamId);
        }
        listener.onWindowUpdateRead(ctx, this.streamId, windowSizeIncrement);
    }
    
    private void readContinuationFrame(final ByteBuf payload, final Http2FrameListener listener) throws Http2Exception {
        final ByteBuf continuationFragment = payload.readSlice(payload.readableBytes());
        this.headersContinuation.processFragment(this.flags.endOfHeaders(), continuationFragment, listener);
    }
    
    private void readUnknownFrame(final ChannelHandlerContext ctx, ByteBuf payload, final Http2FrameListener listener) {
        payload = payload.readSlice(payload.readableBytes());
        listener.onUnknownFrame(ctx, this.frameType, this.streamId, this.flags, payload);
    }
    
    private short readPadding(final ByteBuf payload) {
        if (!this.flags.paddingPresent()) {
            return 0;
        }
        return payload.readUnsignedByte();
    }
    
    private void verifyNotProcessingHeaders() throws Http2Exception {
        if (this.headersContinuation != null) {
            throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Received frame of type %s while processing headers.", this.frameType);
        }
    }
    
    private void verifyPayloadLength(final int payloadLength) throws Http2Exception {
        if (payloadLength > this.maxFrameSize) {
            throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Total payload length %d exceeds max frame length.", payloadLength);
        }
    }
    
    private static void verifyStreamOrConnectionId(final int streamId, final String argumentName) throws Http2Exception {
        if (streamId < 0) {
            throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "%s must be >= 0", argumentName);
        }
    }
    
    private enum State
    {
        FRAME_HEADER, 
        FRAME_PAYLOAD, 
        ERROR;
    }
    
    private abstract class HeadersContinuation
    {
        private final HeadersBlockBuilder builder;
        
        private HeadersContinuation() {
            this.builder = new HeadersBlockBuilder();
        }
        
        abstract int getStreamId();
        
        abstract void processFragment(final boolean p0, final ByteBuf p1, final Http2FrameListener p2) throws Http2Exception;
        
        final HeadersBlockBuilder headersBlockBuilder() {
            return this.builder;
        }
        
        final void close() {
            this.builder.close();
        }
    }
    
    protected class HeadersBlockBuilder
    {
        private ByteBuf headerBlock;
        
        final void addFragment(final ByteBuf fragment, final ByteBufAllocator alloc, final boolean endOfHeaders) {
            if (this.headerBlock == null) {
                if (endOfHeaders) {
                    this.headerBlock = fragment.retain();
                }
                else {
                    (this.headerBlock = alloc.buffer(fragment.readableBytes())).writeBytes(fragment);
                }
                return;
            }
            if (this.headerBlock.isWritable(fragment.readableBytes())) {
                this.headerBlock.writeBytes(fragment);
            }
            else {
                final ByteBuf buf = alloc.buffer(this.headerBlock.readableBytes() + fragment.readableBytes());
                buf.writeBytes(this.headerBlock);
                buf.writeBytes(fragment);
                this.headerBlock.release();
                this.headerBlock = buf;
            }
        }
        
        Http2Headers headers() throws Http2Exception {
            try {
                return DefaultHttp2FrameReader.this.headersDecoder.decodeHeaders(this.headerBlock);
            }
            finally {
                this.close();
            }
        }
        
        void close() {
            if (this.headerBlock != null) {
                this.headerBlock.release();
                this.headerBlock = null;
            }
            DefaultHttp2FrameReader.this.headersContinuation = null;
        }
    }
}
