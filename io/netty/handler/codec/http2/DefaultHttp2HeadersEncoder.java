// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http2;

import java.io.IOException;
import io.netty.handler.codec.Headers;
import java.util.Map;
import java.io.OutputStream;
import io.netty.handler.codec.BinaryHeaders;
import io.netty.handler.codec.AsciiString;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.ByteBuf;
import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.Collections;
import java.util.Set;
import java.io.ByteArrayOutputStream;
import com.twitter.hpack.Encoder;

public class DefaultHttp2HeadersEncoder implements Http2HeadersEncoder, Configuration
{
    private final Encoder encoder;
    private final ByteArrayOutputStream tableSizeChangeOutput;
    private final Set<String> sensitiveHeaders;
    private final Http2HeaderTable headerTable;
    
    public DefaultHttp2HeadersEncoder() {
        this(4096, Collections.emptySet());
    }
    
    public DefaultHttp2HeadersEncoder(final int maxHeaderTableSize, final Set<String> sensitiveHeaders) {
        this.tableSizeChangeOutput = new ByteArrayOutputStream();
        this.sensitiveHeaders = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        this.encoder = new Encoder(maxHeaderTableSize);
        this.sensitiveHeaders.addAll(sensitiveHeaders);
        this.headerTable = new Http2HeaderTableEncoder();
    }
    
    @Override
    public void encodeHeaders(final Http2Headers headers, final ByteBuf buffer) throws Http2Exception {
        final OutputStream stream = new ByteBufOutputStream(buffer);
        try {
            if (headers.size() > this.headerTable.maxHeaderListSize()) {
                throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Number of headers (%d) exceeds maxHeaderListSize (%d)", headers.size(), this.headerTable.maxHeaderListSize());
            }
            if (this.tableSizeChangeOutput.size() > 0) {
                buffer.writeBytes(this.tableSizeChangeOutput.toByteArray());
                this.tableSizeChangeOutput.reset();
            }
            for (final Http2Headers.PseudoHeaderName pseudoHeader : Http2Headers.PseudoHeaderName.values()) {
                final AsciiString name = pseudoHeader.value();
                final AsciiString value = headers.get(name);
                if (value != null) {
                    this.encodeHeader(name, value, stream);
                }
            }
            headers.forEachEntry(new BinaryHeaders.EntryVisitor() {
                @Override
                public boolean visit(final Map.Entry<AsciiString, AsciiString> entry) throws Exception {
                    final AsciiString name = entry.getKey();
                    final AsciiString value = entry.getValue();
                    if (!Http2Headers.PseudoHeaderName.isPseudoHeader(name)) {
                        DefaultHttp2HeadersEncoder.this.encodeHeader(name, value, stream);
                    }
                    return true;
                }
            });
        }
        catch (Http2Exception e) {
            throw e;
        }
        catch (Throwable t) {
            throw Http2Exception.connectionError(Http2Error.COMPRESSION_ERROR, t, "Failed encoding headers block: %s", t.getMessage());
        }
        finally {
            try {
                stream.close();
            }
            catch (IOException e2) {
                throw Http2Exception.connectionError(Http2Error.INTERNAL_ERROR, e2, e2.getMessage(), new Object[0]);
            }
        }
    }
    
    @Override
    public Http2HeaderTable headerTable() {
        return this.headerTable;
    }
    
    @Override
    public Configuration configuration() {
        return this;
    }
    
    private void encodeHeader(final AsciiString key, final AsciiString value, final OutputStream stream) throws IOException {
        final boolean sensitive = this.sensitiveHeaders.contains(key.toString());
        this.encoder.encodeHeader(stream, key.array(), value.array(), sensitive);
    }
    
    private final class Http2HeaderTableEncoder extends DefaultHttp2HeaderTableListSize implements Http2HeaderTable
    {
        @Override
        public void maxHeaderTableSize(final int max) throws Http2Exception {
            if (max < 0) {
                throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Header Table Size must be non-negative but was %d", max);
            }
            try {
                DefaultHttp2HeadersEncoder.this.encoder.setMaxHeaderTableSize((OutputStream)DefaultHttp2HeadersEncoder.this.tableSizeChangeOutput, max);
            }
            catch (IOException e) {
                throw new Http2Exception(Http2Error.COMPRESSION_ERROR, e.getMessage(), e);
            }
            catch (Throwable t) {
                throw new Http2Exception(Http2Error.PROTOCOL_ERROR, t.getMessage(), t);
            }
        }
        
        @Override
        public int maxHeaderTableSize() {
            return DefaultHttp2HeadersEncoder.this.encoder.getMaxHeaderTableSize();
        }
    }
}
