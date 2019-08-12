// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.dns;

import io.netty.channel.DefaultAddressedEnvelope;
import io.netty.util.CharsetUtil;
import io.netty.handler.codec.CorruptedFrameException;
import java.net.InetSocketAddress;
import io.netty.buffer.ByteBuf;
import java.util.List;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;

@ChannelHandler.Sharable
public class DnsResponseDecoder extends MessageToMessageDecoder<DatagramPacket>
{
    @Override
    protected void decode(final ChannelHandlerContext ctx, final DatagramPacket packet, final List<Object> out) throws Exception {
        final ByteBuf buf = ((DefaultAddressedEnvelope<ByteBuf, A>)packet).content();
        final int id = buf.readUnsignedShort();
        final DnsResponse response = new DnsResponse(id, ((DefaultAddressedEnvelope<M, InetSocketAddress>)packet).sender());
        final DnsResponseHeader header = response.header();
        final int flags = buf.readUnsignedShort();
        header.setType(flags >> 15);
        header.setOpcode(flags >> 11 & 0xF);
        header.setRecursionDesired((flags >> 8 & 0x1) == 0x1);
        header.setAuthoritativeAnswer((flags >> 10 & 0x1) == 0x1);
        header.setTruncated((flags >> 9 & 0x1) == 0x1);
        header.setRecursionAvailable((flags >> 7 & 0x1) == 0x1);
        header.setZ(flags >> 4 & 0x7);
        header.setResponseCode(DnsResponseCode.valueOf(flags & 0xF));
        final int questions = buf.readUnsignedShort();
        final int answers = buf.readUnsignedShort();
        final int authorities = buf.readUnsignedShort();
        final int additionals = buf.readUnsignedShort();
        for (int i = 0; i < questions; ++i) {
            response.addQuestion(decodeQuestion(buf));
        }
        if (header.responseCode() != DnsResponseCode.NOERROR) {
            out.add(response);
            return;
        }
        boolean release = true;
        try {
            for (int j = 0; j < answers; ++j) {
                response.addAnswer(decodeResource(buf));
            }
            for (int j = 0; j < authorities; ++j) {
                response.addAuthorityResource(decodeResource(buf));
            }
            for (int j = 0; j < additionals; ++j) {
                response.addAdditionalResource(decodeResource(buf));
            }
            out.add(response);
            release = false;
        }
        finally {
            if (release) {
                releaseDnsResources(response.answers());
                releaseDnsResources(response.authorityResources());
                releaseDnsResources(response.additionalResources());
            }
        }
    }
    
    private static void releaseDnsResources(final List<DnsResource> resources) {
        for (int size = resources.size(), i = 0; i < size; ++i) {
            final DnsResource resource = resources.get(i);
            resource.release();
        }
    }
    
    private static String readName(final ByteBuf buf) {
        int position = -1;
        int checked = 0;
        final int length = buf.writerIndex();
        final StringBuilder name = new StringBuilder();
        for (int len = buf.readUnsignedByte(); buf.isReadable() && len != 0; len = buf.readUnsignedByte()) {
            final boolean pointer = (len & 0xC0) == 0xC0;
            if (pointer) {
                if (position == -1) {
                    position = buf.readerIndex() + 1;
                }
                buf.readerIndex((len & 0x3F) << 8 | buf.readUnsignedByte());
                checked += 2;
                if (checked >= length) {
                    throw new CorruptedFrameException("name contains a loop.");
                }
            }
            else {
                name.append(buf.toString(buf.readerIndex(), len, CharsetUtil.UTF_8)).append('.');
                buf.skipBytes(len);
            }
        }
        if (position != -1) {
            buf.readerIndex(position);
        }
        if (name.length() == 0) {
            return "";
        }
        return name.substring(0, name.length() - 1);
    }
    
    private static DnsQuestion decodeQuestion(final ByteBuf buf) {
        final String name = readName(buf);
        final DnsType type = DnsType.valueOf(buf.readUnsignedShort());
        final DnsClass qClass = DnsClass.valueOf(buf.readUnsignedShort());
        return new DnsQuestion(name, type, qClass);
    }
    
    private static DnsResource decodeResource(final ByteBuf buf) {
        final String name = readName(buf);
        final DnsType type = DnsType.valueOf(buf.readUnsignedShort());
        final DnsClass aClass = DnsClass.valueOf(buf.readUnsignedShort());
        final long ttl = buf.readUnsignedInt();
        final int len = buf.readUnsignedShort();
        final int readerIndex = buf.readerIndex();
        final ByteBuf payload = buf.duplicate().setIndex(readerIndex, readerIndex + len).retain();
        buf.readerIndex(readerIndex + len);
        return new DnsResource(name, type, aClass, ttl, payload);
    }
}
