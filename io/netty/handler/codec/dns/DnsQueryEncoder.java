// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.dns;

import io.netty.util.internal.StringUtil;
import java.nio.charset.Charset;
import java.util.Iterator;
import io.netty.buffer.ByteBuf;
import java.net.InetSocketAddress;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import java.util.List;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.MessageToMessageEncoder;

@ChannelHandler.Sharable
public class DnsQueryEncoder extends MessageToMessageEncoder<DnsQuery>
{
    @Override
    protected void encode(final ChannelHandlerContext ctx, final DnsQuery query, final List<Object> out) throws Exception {
        final ByteBuf buf = ctx.alloc().buffer();
        encodeHeader(query.header(), buf);
        final List<DnsQuestion> questions = query.questions();
        for (final DnsQuestion question : questions) {
            encodeQuestion(question, CharsetUtil.US_ASCII, buf);
        }
        for (final DnsResource resource : query.additionalResources()) {
            encodeResource(resource, CharsetUtil.US_ASCII, buf);
        }
        out.add(new DatagramPacket(buf, query.recipient(), null));
    }
    
    private static void encodeHeader(final DnsHeader header, final ByteBuf buf) {
        buf.writeShort(header.id());
        int flags = 0;
        flags |= header.type() << 15;
        flags |= header.opcode() << 14;
        flags |= (header.isRecursionDesired() ? 256 : 0);
        buf.writeShort(flags);
        buf.writeShort(header.questionCount());
        buf.writeShort(0);
        buf.writeShort(0);
        buf.writeShort(header.additionalResourceCount());
    }
    
    private static void encodeQuestion(final DnsQuestion question, final Charset charset, final ByteBuf buf) {
        encodeName(question.name(), charset, buf);
        buf.writeShort(question.type().intValue());
        buf.writeShort(question.dnsClass().intValue());
    }
    
    private static void encodeResource(final DnsResource resource, final Charset charset, final ByteBuf buf) {
        encodeName(resource.name(), charset, buf);
        buf.writeShort(resource.type().intValue());
        buf.writeShort(resource.dnsClass().intValue());
        buf.writeInt((int)resource.timeToLive());
        final ByteBuf content = resource.content();
        final int contentLen = content.readableBytes();
        buf.writeShort(contentLen);
        buf.writeBytes(content, content.readerIndex(), contentLen);
    }
    
    private static void encodeName(final String name, final Charset charset, final ByteBuf buf) {
        final String[] arr$;
        final String[] parts = arr$ = StringUtil.split(name, '.');
        for (final String part : arr$) {
            final int partLen = part.length();
            if (partLen != 0) {
                buf.writeByte(partLen);
                buf.writeBytes(part.getBytes(charset));
            }
        }
        buf.writeByte(0);
    }
}
