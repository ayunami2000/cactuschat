// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http;

import io.netty.handler.codec.AsciiString;
import io.netty.handler.codec.Headers;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.buffer.ByteBufProcessor;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderResult;
import java.util.List;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.AppendableCharSequence;
import io.netty.handler.codec.ByteToMessageDecoder;

public abstract class HttpObjectDecoder extends ByteToMessageDecoder
{
    private static final String EMPTY_VALUE = "";
    private final int maxChunkSize;
    private final boolean chunkedSupported;
    protected final boolean validateHeaders;
    private final HeaderParser headerParser;
    private final LineParser lineParser;
    private HttpMessage message;
    private long chunkSize;
    private long contentLength;
    private volatile boolean resetRequested;
    private CharSequence name;
    private CharSequence value;
    private LastHttpContent trailer;
    private State currentState;
    
    protected HttpObjectDecoder() {
        this(4096, 8192, 8192, true);
    }
    
    protected HttpObjectDecoder(final int maxInitialLineLength, final int maxHeaderSize, final int maxChunkSize, final boolean chunkedSupported) {
        this(maxInitialLineLength, maxHeaderSize, maxChunkSize, chunkedSupported, true);
    }
    
    protected HttpObjectDecoder(final int maxInitialLineLength, final int maxHeaderSize, final int maxChunkSize, final boolean chunkedSupported, final boolean validateHeaders) {
        this.contentLength = Long.MIN_VALUE;
        this.currentState = State.SKIP_CONTROL_CHARS;
        if (maxInitialLineLength <= 0) {
            throw new IllegalArgumentException("maxInitialLineLength must be a positive integer: " + maxInitialLineLength);
        }
        if (maxHeaderSize <= 0) {
            throw new IllegalArgumentException("maxHeaderSize must be a positive integer: " + maxHeaderSize);
        }
        if (maxChunkSize <= 0) {
            throw new IllegalArgumentException("maxChunkSize must be a positive integer: " + maxChunkSize);
        }
        this.maxChunkSize = maxChunkSize;
        this.chunkedSupported = chunkedSupported;
        this.validateHeaders = validateHeaders;
        final AppendableCharSequence seq = new AppendableCharSequence(128);
        this.lineParser = new LineParser(seq, maxInitialLineLength);
        this.headerParser = new HeaderParser(seq, maxHeaderSize);
    }
    
    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf buffer, final List<Object> out) throws Exception {
        if (this.resetRequested) {
            this.resetNow();
        }
        switch (this.currentState) {
            case SKIP_CONTROL_CHARS: {
                if (!skipControlCharacters(buffer)) {
                    return;
                }
                this.currentState = State.READ_INITIAL;
            }
            case READ_INITIAL: {
                try {
                    final AppendableCharSequence line = this.lineParser.parse(buffer);
                    if (line == null) {
                        return;
                    }
                    final String[] initialLine = splitInitialLine(line);
                    if (initialLine.length < 3) {
                        this.currentState = State.SKIP_CONTROL_CHARS;
                        return;
                    }
                    this.message = this.createMessage(initialLine);
                    this.currentState = State.READ_HEADER;
                }
                catch (Exception e) {
                    out.add(this.invalidMessage(e));
                }
            }
            case READ_HEADER: {
                try {
                    final State nextState = this.readHeaders(buffer);
                    if (nextState == null) {
                        return;
                    }
                    this.currentState = nextState;
                    switch (nextState) {
                        case SKIP_CONTROL_CHARS: {
                            out.add(this.message);
                            out.add(LastHttpContent.EMPTY_LAST_CONTENT);
                            this.resetNow();
                        }
                        case READ_CHUNK_SIZE: {
                            if (!this.chunkedSupported) {
                                throw new IllegalArgumentException("Chunked messages not supported");
                            }
                            out.add(this.message);
                        }
                        default: {
                            final long contentLength = this.contentLength();
                            if (contentLength == 0L || (contentLength == -1L && this.isDecodingRequest())) {
                                out.add(this.message);
                                out.add(LastHttpContent.EMPTY_LAST_CONTENT);
                                this.resetNow();
                                return;
                            }
                            assert nextState == State.READ_VARIABLE_LENGTH_CONTENT;
                            out.add(this.message);
                            if (nextState == State.READ_FIXED_LENGTH_CONTENT) {
                                this.chunkSize = contentLength;
                            }
                        }
                    }
                }
                catch (Exception e) {
                    out.add(this.invalidMessage(e));
                }
            }
            case READ_VARIABLE_LENGTH_CONTENT: {
                final int toRead = Math.min(buffer.readableBytes(), this.maxChunkSize);
                if (toRead > 0) {
                    final ByteBuf content = buffer.readSlice(toRead).retain();
                    out.add(new DefaultHttpContent(content));
                }
            }
            case READ_FIXED_LENGTH_CONTENT: {
                final int readLimit = buffer.readableBytes();
                if (readLimit == 0) {
                    return;
                }
                int toRead2 = Math.min(readLimit, this.maxChunkSize);
                if (toRead2 > this.chunkSize) {
                    toRead2 = (int)this.chunkSize;
                }
                final ByteBuf content2 = buffer.readSlice(toRead2).retain();
                this.chunkSize -= toRead2;
                if (this.chunkSize == 0L) {
                    out.add(new DefaultLastHttpContent(content2, this.validateHeaders));
                    this.resetNow();
                }
                else {
                    out.add(new DefaultHttpContent(content2));
                }
            }
            case READ_CHUNK_SIZE: {
                try {
                    final AppendableCharSequence line = this.lineParser.parse(buffer);
                    if (line == null) {
                        return;
                    }
                    final int chunkSize = getChunkSize(line.toString());
                    this.chunkSize = chunkSize;
                    if (chunkSize == 0) {
                        this.currentState = State.READ_CHUNK_FOOTER;
                        return;
                    }
                    this.currentState = State.READ_CHUNKED_CONTENT;
                }
                catch (Exception e) {
                    out.add(this.invalidChunk(e));
                }
            }
            case READ_CHUNKED_CONTENT: {
                assert this.chunkSize <= 2147483647L;
                int toRead = Math.min((int)this.chunkSize, this.maxChunkSize);
                toRead = Math.min(toRead, buffer.readableBytes());
                if (toRead == 0) {
                    return;
                }
                final HttpContent chunk = new DefaultHttpContent(buffer.readSlice(toRead).retain());
                this.chunkSize -= toRead;
                out.add(chunk);
                if (this.chunkSize != 0L) {
                    return;
                }
                this.currentState = State.READ_CHUNK_DELIMITER;
            }
            case READ_CHUNK_DELIMITER: {
                final int wIdx = buffer.writerIndex();
                int rIdx = buffer.readerIndex();
                while (wIdx > rIdx) {
                    final byte next = buffer.getByte(rIdx++);
                    if (next == 10) {
                        this.currentState = State.READ_CHUNK_SIZE;
                        break;
                    }
                }
                buffer.readerIndex(rIdx);
            }
            case READ_CHUNK_FOOTER: {
                try {
                    final LastHttpContent trailer = this.readTrailingHeaders(buffer);
                    if (trailer == null) {
                        return;
                    }
                    out.add(trailer);
                    this.resetNow();
                }
                catch (Exception e) {
                    out.add(this.invalidChunk(e));
                }
            }
            case BAD_MESSAGE: {
                buffer.skipBytes(buffer.readableBytes());
                break;
            }
            case UPGRADED: {
                final int readableBytes = buffer.readableBytes();
                if (readableBytes > 0) {
                    out.add(buffer.readBytes(readableBytes));
                    break;
                }
                break;
            }
        }
    }
    
    @Override
    protected void decodeLast(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) throws Exception {
        this.decode(ctx, in, out);
        if (this.message != null) {
            final boolean chunked = HttpHeaderUtil.isTransferEncodingChunked(this.message);
            if (this.currentState == State.READ_VARIABLE_LENGTH_CONTENT && !in.isReadable() && !chunked) {
                out.add(LastHttpContent.EMPTY_LAST_CONTENT);
                this.reset();
                return;
            }
            final boolean prematureClosure = this.isDecodingRequest() || chunked || this.contentLength() > 0L;
            this.resetNow();
            if (!prematureClosure) {
                out.add(LastHttpContent.EMPTY_LAST_CONTENT);
            }
        }
    }
    
    protected boolean isContentAlwaysEmpty(final HttpMessage msg) {
        if (msg instanceof HttpResponse) {
            final HttpResponse res = (HttpResponse)msg;
            final int code = res.status().code();
            if (code >= 100 && code < 200) {
                return code != 101 || ((Headers<AsciiString>)res.headers()).contains(HttpHeaderNames.SEC_WEBSOCKET_ACCEPT);
            }
            switch (code) {
                case 204:
                case 205:
                case 304: {
                    return true;
                }
            }
        }
        return false;
    }
    
    public void reset() {
        this.resetRequested = true;
    }
    
    private void resetNow() {
        final HttpMessage message = this.message;
        this.message = null;
        this.name = null;
        this.value = null;
        this.contentLength = Long.MIN_VALUE;
        this.lineParser.reset();
        this.headerParser.reset();
        this.trailer = null;
        if (!this.isDecodingRequest()) {
            final HttpResponse res = (HttpResponse)message;
            if (res != null && res.status().code() == 101) {
                this.currentState = State.UPGRADED;
                return;
            }
        }
        this.currentState = State.SKIP_CONTROL_CHARS;
    }
    
    private HttpMessage invalidMessage(final Exception cause) {
        this.currentState = State.BAD_MESSAGE;
        if (this.message != null) {
            this.message.setDecoderResult(DecoderResult.failure(cause));
        }
        else {
            (this.message = this.createInvalidMessage()).setDecoderResult(DecoderResult.failure(cause));
        }
        final HttpMessage ret = this.message;
        this.message = null;
        return ret;
    }
    
    private HttpContent invalidChunk(final Exception cause) {
        this.currentState = State.BAD_MESSAGE;
        final HttpContent chunk = new DefaultLastHttpContent(Unpooled.EMPTY_BUFFER);
        chunk.setDecoderResult(DecoderResult.failure(cause));
        this.message = null;
        this.trailer = null;
        return chunk;
    }
    
    private static boolean skipControlCharacters(final ByteBuf buffer) {
        boolean skiped = false;
        final int wIdx = buffer.writerIndex();
        int rIdx = buffer.readerIndex();
        while (wIdx > rIdx) {
            final int c = buffer.getUnsignedByte(rIdx++);
            if (!Character.isISOControl(c) && !Character.isWhitespace(c)) {
                --rIdx;
                skiped = true;
                break;
            }
        }
        buffer.readerIndex(rIdx);
        return skiped;
    }
    
    private State readHeaders(final ByteBuf buffer) {
        final HttpMessage message = this.message;
        final HttpHeaders headers = message.headers();
        AppendableCharSequence line = this.headerParser.parse(buffer);
        if (line == null) {
            return null;
        }
        if (line.length() > 0) {
            do {
                final char firstChar = line.charAt(0);
                if (this.name != null && (firstChar == ' ' || firstChar == '\t')) {
                    final StringBuilder buf = new StringBuilder(this.value.length() + line.length() + 1);
                    buf.append(this.value).append(' ').append(line.toString().trim());
                    this.value = buf.toString();
                }
                else {
                    if (this.name != null) {
                        headers.add(this.name, this.value);
                    }
                    this.splitHeader(line);
                }
                line = this.headerParser.parse(buffer);
                if (line == null) {
                    return null;
                }
            } while (line.length() > 0);
        }
        if (this.name != null) {
            headers.add(this.name, this.value);
        }
        this.name = null;
        this.value = null;
        State nextState;
        if (this.isContentAlwaysEmpty(message)) {
            HttpHeaderUtil.setTransferEncodingChunked(message, false);
            nextState = State.SKIP_CONTROL_CHARS;
        }
        else if (HttpHeaderUtil.isTransferEncodingChunked(message)) {
            nextState = State.READ_CHUNK_SIZE;
        }
        else if (this.contentLength() >= 0L) {
            nextState = State.READ_FIXED_LENGTH_CONTENT;
        }
        else {
            nextState = State.READ_VARIABLE_LENGTH_CONTENT;
        }
        return nextState;
    }
    
    private long contentLength() {
        if (this.contentLength == Long.MIN_VALUE) {
            this.contentLength = HttpHeaderUtil.getContentLength(this.message, -1L);
        }
        return this.contentLength;
    }
    
    private LastHttpContent readTrailingHeaders(final ByteBuf buffer) {
        AppendableCharSequence line = this.headerParser.parse(buffer);
        if (line == null) {
            return null;
        }
        CharSequence lastHeader = null;
        if (line.length() > 0) {
            LastHttpContent trailer = this.trailer;
            if (trailer == null) {
                final DefaultLastHttpContent trailer2 = new DefaultLastHttpContent(Unpooled.EMPTY_BUFFER, this.validateHeaders);
                this.trailer = trailer2;
                trailer = trailer2;
            }
            do {
                final char firstChar = line.charAt(0);
                if (lastHeader != null && (firstChar == ' ' || firstChar == '\t')) {
                    final List<CharSequence> current = trailer.trailingHeaders().getAll(lastHeader);
                    if (!current.isEmpty()) {
                        final int lastPos = current.size() - 1;
                        final String lineTrimmed = line.toString().trim();
                        final CharSequence currentLastPos = current.get(lastPos);
                        final StringBuilder b = new StringBuilder(currentLastPos.length() + lineTrimmed.length());
                        b.append(currentLastPos).append(lineTrimmed);
                        current.set(lastPos, b.toString());
                    }
                }
                else {
                    this.splitHeader(line);
                    final CharSequence headerName = this.name;
                    if (!HttpHeaderNames.CONTENT_LENGTH.equalsIgnoreCase(headerName) && !HttpHeaderNames.TRANSFER_ENCODING.equalsIgnoreCase(headerName) && !HttpHeaderNames.TRAILER.equalsIgnoreCase(headerName)) {
                        trailer.trailingHeaders().add(headerName, this.value);
                    }
                    lastHeader = this.name;
                    this.name = null;
                    this.value = null;
                }
                line = this.headerParser.parse(buffer);
                if (line == null) {
                    return null;
                }
            } while (line.length() > 0);
            this.trailer = null;
            return trailer;
        }
        return LastHttpContent.EMPTY_LAST_CONTENT;
    }
    
    protected abstract boolean isDecodingRequest();
    
    protected abstract HttpMessage createMessage(final String[] p0) throws Exception;
    
    protected abstract HttpMessage createInvalidMessage();
    
    private static int getChunkSize(String hex) {
        hex = hex.trim();
        for (int i = 0; i < hex.length(); ++i) {
            final char c = hex.charAt(i);
            if (c == ';' || Character.isWhitespace(c) || Character.isISOControl(c)) {
                hex = hex.substring(0, i);
                break;
            }
        }
        return Integer.parseInt(hex, 16);
    }
    
    private static String[] splitInitialLine(final AppendableCharSequence sb) {
        final int aStart = findNonWhitespace(sb, 0);
        final int aEnd = findWhitespace(sb, aStart);
        final int bStart = findNonWhitespace(sb, aEnd);
        final int bEnd = findWhitespace(sb, bStart);
        final int cStart = findNonWhitespace(sb, bEnd);
        final int cEnd = findEndOfString(sb);
        return new String[] { sb.substring(aStart, aEnd), sb.substring(bStart, bEnd), (cStart < cEnd) ? sb.substring(cStart, cEnd) : "" };
    }
    
    private void splitHeader(final AppendableCharSequence sb) {
        int length;
        int nameEnd;
        int nameStart;
        for (length = sb.length(), nameStart = (nameEnd = findNonWhitespace(sb, 0)); nameEnd < length; ++nameEnd) {
            final char ch = sb.charAt(nameEnd);
            if (ch == ':') {
                break;
            }
            if (Character.isWhitespace(ch)) {
                break;
            }
        }
        int colonEnd;
        for (colonEnd = nameEnd; colonEnd < length; ++colonEnd) {
            if (sb.charAt(colonEnd) == ':') {
                ++colonEnd;
                break;
            }
        }
        this.name = sb.substring(nameStart, nameEnd);
        final int valueStart = findNonWhitespace(sb, colonEnd);
        if (valueStart == length) {
            this.value = "";
        }
        else {
            final int valueEnd = findEndOfString(sb);
            this.value = sb.substring(valueStart, valueEnd);
        }
    }
    
    private static int findNonWhitespace(final CharSequence sb, final int offset) {
        int result;
        for (result = offset; result < sb.length() && Character.isWhitespace(sb.charAt(result)); ++result) {}
        return result;
    }
    
    private static int findWhitespace(final CharSequence sb, final int offset) {
        int result;
        for (result = offset; result < sb.length() && !Character.isWhitespace(sb.charAt(result)); ++result) {}
        return result;
    }
    
    private static int findEndOfString(final CharSequence sb) {
        int result;
        for (result = sb.length(); result > 0 && Character.isWhitespace(sb.charAt(result - 1)); --result) {}
        return result;
    }
    
    private enum State
    {
        SKIP_CONTROL_CHARS, 
        READ_INITIAL, 
        READ_HEADER, 
        READ_VARIABLE_LENGTH_CONTENT, 
        READ_FIXED_LENGTH_CONTENT, 
        READ_CHUNK_SIZE, 
        READ_CHUNKED_CONTENT, 
        READ_CHUNK_DELIMITER, 
        READ_CHUNK_FOOTER, 
        BAD_MESSAGE, 
        UPGRADED;
    }
    
    private static class HeaderParser implements ByteBufProcessor
    {
        private final AppendableCharSequence seq;
        private final int maxLength;
        private int size;
        
        HeaderParser(final AppendableCharSequence seq, final int maxLength) {
            this.seq = seq;
            this.maxLength = maxLength;
        }
        
        public AppendableCharSequence parse(final ByteBuf buffer) {
            this.seq.reset();
            final int i = buffer.forEachByte(this);
            if (i == -1) {
                return null;
            }
            buffer.readerIndex(i + 1);
            return this.seq;
        }
        
        public void reset() {
            this.size = 0;
        }
        
        @Override
        public boolean process(final byte value) throws Exception {
            final char nextByte = (char)value;
            if (nextByte == '\r') {
                return true;
            }
            if (nextByte == '\n') {
                return false;
            }
            if (this.size >= this.maxLength) {
                throw this.newException(this.maxLength);
            }
            ++this.size;
            this.seq.append(nextByte);
            return true;
        }
        
        protected TooLongFrameException newException(final int maxLength) {
            return new TooLongFrameException("HTTP header is larger than " + maxLength + " bytes.");
        }
    }
    
    private static final class LineParser extends HeaderParser
    {
        LineParser(final AppendableCharSequence seq, final int maxLength) {
            super(seq, maxLength);
        }
        
        @Override
        public AppendableCharSequence parse(final ByteBuf buffer) {
            this.reset();
            return super.parse(buffer);
        }
        
        @Override
        protected TooLongFrameException newException(final int maxLength) {
            return new TooLongFrameException("An HTTP line is larger than " + maxLength + " bytes.");
        }
    }
}
