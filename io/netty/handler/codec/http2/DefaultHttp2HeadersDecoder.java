// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http2;

import java.io.InputStream;
import java.io.IOException;
import io.netty.handler.codec.AsciiString;
import com.twitter.hpack.HeaderListener;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBuf;
import com.twitter.hpack.Decoder;

public class DefaultHttp2HeadersDecoder implements Http2HeadersDecoder, Configuration
{
    private final Decoder decoder;
    private final Http2HeaderTable headerTable;
    
    public DefaultHttp2HeadersDecoder() {
        this(8192, 4096);
    }
    
    public DefaultHttp2HeadersDecoder(final int maxHeaderSize, final int maxHeaderTableSize) {
        this.decoder = new Decoder(maxHeaderSize, maxHeaderTableSize);
        this.headerTable = new Http2HeaderTableDecoder();
    }
    
    @Override
    public Http2HeaderTable headerTable() {
        return this.headerTable;
    }
    
    @Override
    public Configuration configuration() {
        return this;
    }
    
    @Override
    public Http2Headers decodeHeaders(final ByteBuf headerBlock) throws Http2Exception {
        final InputStream in = new ByteBufInputStream(headerBlock);
        try {
            final Http2Headers headers = new DefaultHttp2Headers();
            final HeaderListener listener = (HeaderListener)new HeaderListener() {
                public void addHeader(final byte[] key, final byte[] value, final boolean sensitive) {
                    headers.add(new AsciiString(key, false), new AsciiString(value, false));
                }
            };
            this.decoder.decode(in, listener);
            final boolean truncated = this.decoder.endHeaderBlock();
            if (truncated) {}
            if (headers.size() > this.headerTable.maxHeaderListSize()) {
                throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Number of headers (%d) exceeds maxHeaderListSize (%d)", headers.size(), this.headerTable.maxHeaderListSize());
            }
            return headers;
        }
        catch (IOException e) {
            throw Http2Exception.connectionError(Http2Error.COMPRESSION_ERROR, e, e.getMessage(), new Object[0]);
        }
        catch (Http2Exception e2) {
            throw e2;
        }
        catch (Throwable e3) {
            throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, e3, e3.getMessage(), new Object[0]);
        }
        finally {
            try {
                in.close();
            }
            catch (IOException e4) {
                throw Http2Exception.connectionError(Http2Error.INTERNAL_ERROR, e4, e4.getMessage(), new Object[0]);
            }
        }
    }
    
    private final class Http2HeaderTableDecoder extends DefaultHttp2HeaderTableListSize implements Http2HeaderTable
    {
        @Override
        public void maxHeaderTableSize(final int max) throws Http2Exception {
            if (max < 0) {
                throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Header Table Size must be non-negative but was %d", max);
            }
            try {
                DefaultHttp2HeadersDecoder.this.decoder.setMaxHeaderTableSize(max);
            }
            catch (Throwable t) {
                throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, t.getMessage(), t);
            }
        }
        
        @Override
        public int maxHeaderTableSize() {
            return DefaultHttp2HeadersDecoder.this.decoder.getMaxHeaderTableSize();
        }
    }
}
