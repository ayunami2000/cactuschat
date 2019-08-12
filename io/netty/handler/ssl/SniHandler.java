// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.ssl;

import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.buffer.ByteBufUtil;
import io.netty.util.CharsetUtil;
import io.netty.channel.ChannelHandler;
import java.util.Locale;
import java.net.IDN;
import java.util.List;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.DomainNameMapping;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.handler.codec.ByteToMessageDecoder;

public class SniHandler extends ByteToMessageDecoder
{
    private static final InternalLogger logger;
    private final DomainNameMapping<SslContext> mapping;
    private boolean handshaken;
    private volatile String hostname;
    private volatile SslContext selectedContext;
    
    public SniHandler(final DomainNameMapping<? extends SslContext> mapping) {
        if (mapping == null) {
            throw new NullPointerException("mapping");
        }
        this.mapping = (DomainNameMapping<SslContext>)mapping;
        this.handshaken = false;
    }
    
    public String hostname() {
        return this.hostname;
    }
    
    public SslContext sslContext() {
        return this.selectedContext;
    }
    
    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) throws Exception {
        if (!this.handshaken && in.readableBytes() >= 5) {
            String hostname = this.sniHostNameFromHandshakeInfo(in);
            if (hostname != null) {
                hostname = IDN.toASCII(hostname, 1).toLowerCase(Locale.US);
            }
            this.hostname = hostname;
            this.selectedContext = this.mapping.map(hostname);
        }
        if (this.handshaken) {
            final SslHandler sslHandler = this.selectedContext.newHandler(ctx.alloc());
            ctx.pipeline().replace(this, SslHandler.class.getName(), sslHandler);
        }
    }
    
    private String sniHostNameFromHandshakeInfo(final ByteBuf in) {
        final int readerIndex = in.readerIndex();
        try {
            final int command = in.getUnsignedByte(readerIndex);
            switch (command) {
                case 20:
                case 21:
                case 23: {
                    return null;
                }
                case 22: {
                    final int majorVersion = in.getUnsignedByte(readerIndex + 1);
                    if (majorVersion != 3) {
                        this.handshaken = true;
                        return null;
                    }
                    final int packetLength = in.getUnsignedShort(readerIndex + 3) + 5;
                    if (in.readableBytes() >= packetLength) {
                        int offset = readerIndex + 43;
                        final int sessionIdLength = in.getUnsignedByte(offset);
                        offset += sessionIdLength + 1;
                        final int cipherSuitesLength = in.getUnsignedShort(offset);
                        offset += cipherSuitesLength + 2;
                        final int compressionMethodLength = in.getUnsignedByte(offset);
                        offset += compressionMethodLength + 1;
                        final int extensionsLength = in.getUnsignedShort(offset);
                        offset += 2;
                        final int extensionsLimit = offset + extensionsLength;
                        while (offset < extensionsLimit) {
                            final int extensionType = in.getUnsignedShort(offset);
                            offset += 2;
                            final int extensionLength = in.getUnsignedShort(offset);
                            offset += 2;
                            if (extensionType == 0) {
                                this.handshaken = true;
                                final int serverNameType = in.getUnsignedByte(offset + 2);
                                if (serverNameType == 0) {
                                    final int serverNameLength = in.getUnsignedShort(offset + 3);
                                    return in.toString(offset + 5, serverNameLength, CharsetUtil.UTF_8);
                                }
                                return null;
                            }
                            else {
                                offset += extensionLength;
                            }
                        }
                        this.handshaken = true;
                        return null;
                    }
                    return null;
                }
                default: {
                    this.handshaken = true;
                    return null;
                }
            }
        }
        catch (Throwable e) {
            if (SniHandler.logger.isDebugEnabled()) {
                SniHandler.logger.debug("Unexpected client hello packet: " + ByteBufUtil.hexDump(in), e);
            }
            this.handshaken = true;
            return null;
        }
    }
    
    static {
        logger = InternalLoggerFactory.getInstance(SniHandler.class);
    }
}
