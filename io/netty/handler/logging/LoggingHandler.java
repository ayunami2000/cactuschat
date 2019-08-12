// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.logging;

import io.netty.util.internal.StringUtil;
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelPromise;
import java.net.SocketAddress;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.InternalLogLevel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;

@ChannelHandler.Sharable
public class LoggingHandler extends ChannelHandlerAdapter
{
    private static final LogLevel DEFAULT_LEVEL;
    private static final String NEWLINE;
    private static final String[] BYTE2HEX;
    private static final String[] HEXPADDING;
    private static final String[] BYTEPADDING;
    private static final char[] BYTE2CHAR;
    private static final String[] HEXDUMP_ROWPREFIXES;
    protected final InternalLogger logger;
    protected final InternalLogLevel internalLevel;
    private final LogLevel level;
    
    public LoggingHandler() {
        this(LoggingHandler.DEFAULT_LEVEL);
    }
    
    public LoggingHandler(final LogLevel level) {
        if (level == null) {
            throw new NullPointerException("level");
        }
        this.logger = InternalLoggerFactory.getInstance(this.getClass());
        this.level = level;
        this.internalLevel = level.toInternalLevel();
    }
    
    public LoggingHandler(final Class<?> clazz) {
        this(clazz, LoggingHandler.DEFAULT_LEVEL);
    }
    
    public LoggingHandler(final Class<?> clazz, final LogLevel level) {
        if (clazz == null) {
            throw new NullPointerException("clazz");
        }
        if (level == null) {
            throw new NullPointerException("level");
        }
        this.logger = InternalLoggerFactory.getInstance(clazz);
        this.level = level;
        this.internalLevel = level.toInternalLevel();
    }
    
    public LoggingHandler(final String name) {
        this(name, LoggingHandler.DEFAULT_LEVEL);
    }
    
    public LoggingHandler(final String name, final LogLevel level) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        if (level == null) {
            throw new NullPointerException("level");
        }
        this.logger = InternalLoggerFactory.getInstance(name);
        this.level = level;
        this.internalLevel = level.toInternalLevel();
    }
    
    public LogLevel level() {
        return this.level;
    }
    
    @Override
    public void channelRegistered(final ChannelHandlerContext ctx) throws Exception {
        if (this.logger.isEnabled(this.internalLevel)) {
            this.logger.log(this.internalLevel, this.format(ctx, "REGISTERED"));
        }
        ctx.fireChannelRegistered();
    }
    
    @Override
    public void channelUnregistered(final ChannelHandlerContext ctx) throws Exception {
        if (this.logger.isEnabled(this.internalLevel)) {
            this.logger.log(this.internalLevel, this.format(ctx, "UNREGISTERED"));
        }
        ctx.fireChannelUnregistered();
    }
    
    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        if (this.logger.isEnabled(this.internalLevel)) {
            this.logger.log(this.internalLevel, this.format(ctx, "ACTIVE"));
        }
        ctx.fireChannelActive();
    }
    
    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        if (this.logger.isEnabled(this.internalLevel)) {
            this.logger.log(this.internalLevel, this.format(ctx, "INACTIVE"));
        }
        ctx.fireChannelInactive();
    }
    
    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        if (this.logger.isEnabled(this.internalLevel)) {
            this.logger.log(this.internalLevel, this.format(ctx, "EXCEPTION", cause), cause);
        }
        ctx.fireExceptionCaught(cause);
    }
    
    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) throws Exception {
        if (this.logger.isEnabled(this.internalLevel)) {
            this.logger.log(this.internalLevel, this.format(ctx, "USER_EVENT", evt));
        }
        ctx.fireUserEventTriggered(evt);
    }
    
    @Override
    public void bind(final ChannelHandlerContext ctx, final SocketAddress localAddress, final ChannelPromise promise) throws Exception {
        if (this.logger.isEnabled(this.internalLevel)) {
            this.logger.log(this.internalLevel, this.format(ctx, "BIND", localAddress));
        }
        ctx.bind(localAddress, promise);
    }
    
    @Override
    public void connect(final ChannelHandlerContext ctx, final SocketAddress remoteAddress, final SocketAddress localAddress, final ChannelPromise promise) throws Exception {
        if (this.logger.isEnabled(this.internalLevel)) {
            this.logger.log(this.internalLevel, this.format(ctx, "CONNECT", remoteAddress, localAddress));
        }
        ctx.connect(remoteAddress, localAddress, promise);
    }
    
    @Override
    public void disconnect(final ChannelHandlerContext ctx, final ChannelPromise promise) throws Exception {
        if (this.logger.isEnabled(this.internalLevel)) {
            this.logger.log(this.internalLevel, this.format(ctx, "DISCONNECT"));
        }
        ctx.disconnect(promise);
    }
    
    @Override
    public void close(final ChannelHandlerContext ctx, final ChannelPromise promise) throws Exception {
        if (this.logger.isEnabled(this.internalLevel)) {
            this.logger.log(this.internalLevel, this.format(ctx, "CLOSE"));
        }
        ctx.close(promise);
    }
    
    @Override
    public void deregister(final ChannelHandlerContext ctx, final ChannelPromise promise) throws Exception {
        if (this.logger.isEnabled(this.internalLevel)) {
            this.logger.log(this.internalLevel, this.format(ctx, "DEREGISTER"));
        }
        ctx.deregister(promise);
    }
    
    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        if (this.logger.isEnabled(this.internalLevel)) {
            this.logger.log(this.internalLevel, this.format(ctx, "RECEIVED", msg));
        }
        ctx.fireChannelRead(msg);
    }
    
    @Override
    public void write(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise promise) throws Exception {
        if (this.logger.isEnabled(this.internalLevel)) {
            this.logger.log(this.internalLevel, this.format(ctx, "WRITE", msg));
        }
        ctx.write(msg, promise);
    }
    
    @Override
    public void flush(final ChannelHandlerContext ctx) throws Exception {
        if (this.logger.isEnabled(this.internalLevel)) {
            this.logger.log(this.internalLevel, this.format(ctx, "FLUSH"));
        }
        ctx.flush();
    }
    
    protected String format(final ChannelHandlerContext ctx, final String eventName) {
        final String chStr = ctx.channel().toString();
        return new StringBuilder(chStr.length() + 1 + eventName.length()).append(chStr).append(' ').append(eventName).toString();
    }
    
    protected String format(final ChannelHandlerContext ctx, final String eventName, final Object arg) {
        if (arg instanceof ByteBuf) {
            return formatByteBuf(ctx, eventName, (ByteBuf)arg);
        }
        if (arg instanceof ByteBufHolder) {
            return formatByteBufHolder(ctx, eventName, (ByteBufHolder)arg);
        }
        return formatSimple(ctx, eventName, arg);
    }
    
    protected String format(final ChannelHandlerContext ctx, final String eventName, final Object firstArg, final Object secondArg) {
        if (secondArg == null) {
            return formatSimple(ctx, eventName, firstArg);
        }
        final String chStr = ctx.channel().toString();
        final String arg1Str = String.valueOf(firstArg);
        final String arg2Str = secondArg.toString();
        final StringBuilder buf = new StringBuilder(chStr.length() + 1 + eventName + 2 + arg1Str.length() + 2 + arg2Str.length());
        buf.append(chStr).append(' ').append(eventName).append(": ").append(arg1Str).append(", ").append(arg2Str);
        return buf.toString();
    }
    
    private static String formatByteBuf(final ChannelHandlerContext ctx, final String eventName, final ByteBuf msg) {
        final String chStr = ctx.channel().toString();
        final int length = msg.readableBytes();
        if (length == 0) {
            final StringBuilder buf = new StringBuilder(chStr.length() + 1 + eventName.length() + 4);
            buf.append(chStr).append(' ').append(eventName).append(": 0B");
            return buf.toString();
        }
        final int rows = length / 16 + ((length % 15 != 0) ? 1 : 0) + 4;
        final StringBuilder buf2 = new StringBuilder(chStr.length() + 1 + eventName.length() + 2 + 10 + 1 + 2 + rows * 80);
        buf2.append(chStr).append(' ').append(eventName).append(": ").append(length).append('B');
        appendHexDump(buf2, msg);
        return buf2.toString();
    }
    
    private static String formatByteBufHolder(final ChannelHandlerContext ctx, final String eventName, final ByteBufHolder msg) {
        final String chStr = ctx.channel().toString();
        final String msgStr = msg.toString();
        final ByteBuf content = msg.content();
        final int length = content.readableBytes();
        if (length == 0) {
            final StringBuilder buf = new StringBuilder(chStr.length() + 1 + eventName.length() + 2 + msgStr.length() + 4);
            buf.append(chStr).append(' ').append(eventName).append(", ").append(msgStr).append(", 0B");
            return buf.toString();
        }
        final int rows = length / 16 + ((length % 15 != 0) ? 1 : 0) + 4;
        final StringBuilder buf2 = new StringBuilder(chStr.length() + 1 + eventName.length() + 2 + msgStr.length() + 2 + 10 + 1 + 2 + rows * 80);
        buf2.append(chStr).append(' ').append(eventName).append(": ").append(msgStr).append(", ").append(length).append('B');
        appendHexDump(buf2, content);
        return buf2.toString();
    }
    
    protected static void appendHexDump(final StringBuilder dump, final ByteBuf buf) {
        dump.append(LoggingHandler.NEWLINE + "         +-------------------------------------------------+" + LoggingHandler.NEWLINE + "         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |" + LoggingHandler.NEWLINE + "+--------+-------------------------------------------------+----------------+");
        final int startIndex = buf.readerIndex();
        final int endIndex = buf.writerIndex();
        final int length = endIndex - startIndex;
        final int fullRows = length >>> 4;
        final int remainder = length & 0xF;
        for (int row = 0; row < fullRows; ++row) {
            final int rowStartIndex = row << 4;
            appendHexDumpRowPrefix(dump, row, rowStartIndex);
            final int rowEndIndex = rowStartIndex + 16;
            for (int j = rowStartIndex; j < rowEndIndex; ++j) {
                dump.append(LoggingHandler.BYTE2HEX[buf.getUnsignedByte(j)]);
            }
            dump.append(" |");
            for (int j = rowStartIndex; j < rowEndIndex; ++j) {
                dump.append(LoggingHandler.BYTE2CHAR[buf.getUnsignedByte(j)]);
            }
            dump.append('|');
        }
        if (remainder != 0) {
            final int rowStartIndex2 = fullRows << 4;
            appendHexDumpRowPrefix(dump, fullRows, rowStartIndex2);
            final int rowEndIndex2 = rowStartIndex2 + remainder;
            for (int i = rowStartIndex2; i < rowEndIndex2; ++i) {
                dump.append(LoggingHandler.BYTE2HEX[buf.getUnsignedByte(i)]);
            }
            dump.append(LoggingHandler.HEXPADDING[remainder]);
            dump.append(" |");
            for (int i = rowStartIndex2; i < rowEndIndex2; ++i) {
                dump.append(LoggingHandler.BYTE2CHAR[buf.getUnsignedByte(i)]);
            }
            dump.append(LoggingHandler.BYTEPADDING[remainder]);
            dump.append('|');
        }
        dump.append(LoggingHandler.NEWLINE + "+--------+-------------------------------------------------+----------------+");
    }
    
    private static void appendHexDumpRowPrefix(final StringBuilder dump, final int row, final int rowStartIndex) {
        if (row < LoggingHandler.HEXDUMP_ROWPREFIXES.length) {
            dump.append(LoggingHandler.HEXDUMP_ROWPREFIXES[row]);
        }
        else {
            dump.append(LoggingHandler.NEWLINE);
            dump.append(Long.toHexString(((long)rowStartIndex & 0xFFFFFFFFL) | 0x100000000L));
            dump.setCharAt(dump.length() - 9, '|');
            dump.append('|');
        }
    }
    
    private static String formatSimple(final ChannelHandlerContext ctx, final String eventName, final Object msg) {
        final String chStr = ctx.channel().toString();
        final String msgStr = String.valueOf(msg);
        final StringBuilder buf = new StringBuilder(chStr.length() + 1 + eventName.length() + 2 + msgStr.length());
        return buf.append(chStr).append(' ').append(eventName).append(": ").append(msgStr).toString();
    }
    
    static {
        DEFAULT_LEVEL = LogLevel.DEBUG;
        NEWLINE = StringUtil.NEWLINE;
        BYTE2HEX = new String[256];
        HEXPADDING = new String[16];
        BYTEPADDING = new String[16];
        BYTE2CHAR = new char[256];
        HEXDUMP_ROWPREFIXES = new String[4096];
        for (int i = 0; i < LoggingHandler.BYTE2HEX.length; ++i) {
            LoggingHandler.BYTE2HEX[i] = ' ' + StringUtil.byteToHexStringPadded(i);
        }
        for (int i = 0; i < LoggingHandler.HEXPADDING.length; ++i) {
            final int padding = LoggingHandler.HEXPADDING.length - i;
            final StringBuilder buf = new StringBuilder(padding * 3);
            for (int j = 0; j < padding; ++j) {
                buf.append("   ");
            }
            LoggingHandler.HEXPADDING[i] = buf.toString();
        }
        for (int i = 0; i < LoggingHandler.BYTEPADDING.length; ++i) {
            final int padding = LoggingHandler.BYTEPADDING.length - i;
            final StringBuilder buf = new StringBuilder(padding);
            for (int j = 0; j < padding; ++j) {
                buf.append(' ');
            }
            LoggingHandler.BYTEPADDING[i] = buf.toString();
        }
        for (int i = 0; i < LoggingHandler.BYTE2CHAR.length; ++i) {
            if (i <= 31 || i >= 127) {
                LoggingHandler.BYTE2CHAR[i] = '.';
            }
            else {
                LoggingHandler.BYTE2CHAR[i] = (char)i;
            }
        }
        for (int i = 0; i < LoggingHandler.HEXDUMP_ROWPREFIXES.length; ++i) {
            final StringBuilder buf2 = new StringBuilder(12);
            buf2.append(LoggingHandler.NEWLINE);
            buf2.append(Long.toHexString(((long)(i << 4) & 0xFFFFFFFFL) | 0x100000000L));
            buf2.setCharAt(buf2.length() - 9, '|');
            buf2.append('|');
            LoggingHandler.HEXDUMP_ROWPREFIXES[i] = buf2.toString();
        }
    }
}
