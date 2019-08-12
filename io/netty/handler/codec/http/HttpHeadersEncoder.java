// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http;

import io.netty.handler.codec.AsciiString;
import java.util.Map;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.TextHeaders;

final class HttpHeadersEncoder implements TextHeaders.EntryVisitor
{
    private final ByteBuf buf;
    
    HttpHeadersEncoder(final ByteBuf buf) {
        this.buf = buf;
    }
    
    @Override
    public boolean visit(final Map.Entry<CharSequence, CharSequence> entry) throws Exception {
        final CharSequence name = entry.getKey();
        final CharSequence value = entry.getValue();
        final ByteBuf buf = this.buf;
        final int nameLen = name.length();
        final int valueLen = value.length();
        final int entryLen = nameLen + valueLen + 4;
        int offset = buf.writerIndex();
        buf.ensureWritable(entryLen);
        writeAscii(buf, offset, name, nameLen);
        offset += nameLen;
        buf.setByte(offset++, 58);
        buf.setByte(offset++, 32);
        writeAscii(buf, offset, value, valueLen);
        offset += valueLen;
        buf.setByte(offset++, 13);
        buf.setByte(offset++, 10);
        buf.writerIndex(offset);
        return true;
    }
    
    private static void writeAscii(final ByteBuf buf, final int offset, final CharSequence value, final int valueLen) {
        if (value instanceof AsciiString) {
            writeAsciiString(buf, offset, (AsciiString)value, valueLen);
        }
        else {
            writeCharSequence(buf, offset, value, valueLen);
        }
    }
    
    private static void writeAsciiString(final ByteBuf buf, final int offset, final AsciiString value, final int valueLen) {
        value.copy(0, buf, offset, valueLen);
    }
    
    private static void writeCharSequence(final ByteBuf buf, int offset, final CharSequence value, final int valueLen) {
        for (int i = 0; i < valueLen; ++i) {
            buf.setByte(offset++, c2b(value.charAt(i)));
        }
    }
    
    private static int c2b(final char ch) {
        return (ch < '\u0100') ? ((byte)ch) : 63;
    }
}
