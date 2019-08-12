// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http.multipart;

import io.netty.handler.codec.Headers;
import io.netty.handler.codec.AsciiString;
import io.netty.handler.codec.ConvertibleHeaders;
import io.netty.util.ReferenceCounted;
import io.netty.buffer.ByteBufHolder;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.TextHeaders;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.HttpVersion;
import java.util.HashMap;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.buffer.Unpooled;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpHeaderUtil;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaderNames;
import java.io.IOException;
import java.io.File;
import java.util.Iterator;
import io.netty.util.internal.ThreadLocalRandom;
import java.util.ArrayList;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpConstants;
import io.netty.buffer.ByteBuf;
import java.util.ListIterator;
import java.util.List;
import java.nio.charset.Charset;
import io.netty.handler.codec.http.HttpRequest;
import java.util.regex.Pattern;
import java.util.Map;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.stream.ChunkedInput;

public class HttpPostRequestEncoder implements ChunkedInput<HttpContent>
{
    private static final Map<Pattern, String> percentEncodings;
    private final HttpDataFactory factory;
    private final HttpRequest request;
    private final Charset charset;
    private boolean isChunked;
    private final List<InterfaceHttpData> bodyListDatas;
    final List<InterfaceHttpData> multipartHttpDatas;
    private final boolean isMultipart;
    String multipartDataBoundary;
    String multipartMixedBoundary;
    private boolean headerFinalized;
    private final EncoderMode encoderMode;
    private boolean isLastChunk;
    private boolean isLastChunkSent;
    private FileUpload currentFileUpload;
    private boolean duringMixedMode;
    private long globalBodySize;
    private long globalProgress;
    private ListIterator<InterfaceHttpData> iterator;
    private ByteBuf currentBuffer;
    private InterfaceHttpData currentData;
    private boolean isKey;
    
    public HttpPostRequestEncoder(final HttpRequest request, final boolean multipart) throws ErrorDataEncoderException {
        this(new DefaultHttpDataFactory(16384L), request, multipart, HttpConstants.DEFAULT_CHARSET, EncoderMode.RFC1738);
    }
    
    public HttpPostRequestEncoder(final HttpDataFactory factory, final HttpRequest request, final boolean multipart) throws ErrorDataEncoderException {
        this(factory, request, multipart, HttpConstants.DEFAULT_CHARSET, EncoderMode.RFC1738);
    }
    
    public HttpPostRequestEncoder(final HttpDataFactory factory, final HttpRequest request, final boolean multipart, final Charset charset, final EncoderMode encoderMode) throws ErrorDataEncoderException {
        this.isKey = true;
        if (factory == null) {
            throw new NullPointerException("factory");
        }
        if (request == null) {
            throw new NullPointerException("request");
        }
        if (charset == null) {
            throw new NullPointerException("charset");
        }
        final HttpMethod method = request.method();
        if (!method.equals(HttpMethod.POST) && !method.equals(HttpMethod.PUT) && !method.equals(HttpMethod.PATCH) && !method.equals(HttpMethod.OPTIONS)) {
            throw new ErrorDataEncoderException("Cannot create a Encoder if not a POST");
        }
        this.request = request;
        this.charset = charset;
        this.factory = factory;
        this.bodyListDatas = new ArrayList<InterfaceHttpData>();
        this.isLastChunk = false;
        this.isLastChunkSent = false;
        this.isMultipart = multipart;
        this.multipartHttpDatas = new ArrayList<InterfaceHttpData>();
        this.encoderMode = encoderMode;
        if (this.isMultipart) {
            this.initDataMultipart();
        }
    }
    
    public void cleanFiles() {
        this.factory.cleanRequestHttpData(this.request);
    }
    
    public boolean isMultipart() {
        return this.isMultipart;
    }
    
    private void initDataMultipart() {
        this.multipartDataBoundary = getNewMultipartDelimiter();
    }
    
    private void initMixedMultipart() {
        this.multipartMixedBoundary = getNewMultipartDelimiter();
    }
    
    private static String getNewMultipartDelimiter() {
        return Long.toHexString(ThreadLocalRandom.current().nextLong()).toLowerCase();
    }
    
    public List<InterfaceHttpData> getBodyListAttributes() {
        return this.bodyListDatas;
    }
    
    public void setBodyHttpDatas(final List<InterfaceHttpData> datas) throws ErrorDataEncoderException {
        if (datas == null) {
            throw new NullPointerException("datas");
        }
        this.globalBodySize = 0L;
        this.bodyListDatas.clear();
        this.currentFileUpload = null;
        this.duringMixedMode = false;
        this.multipartHttpDatas.clear();
        for (final InterfaceHttpData data : datas) {
            this.addBodyHttpData(data);
        }
    }
    
    public void addBodyAttribute(final String name, final String value) throws ErrorDataEncoderException {
        if (name == null) {
            throw new NullPointerException("name");
        }
        String svalue;
        if ((svalue = value) == null) {
            svalue = "";
        }
        final Attribute data = this.factory.createAttribute(this.request, name, svalue);
        this.addBodyHttpData(data);
    }
    
    public void addBodyFileUpload(final String name, final File file, final String contentType, final boolean isText) throws ErrorDataEncoderException {
        if (name == null) {
            throw new NullPointerException("name");
        }
        if (file == null) {
            throw new NullPointerException("file");
        }
        String scontentType = contentType;
        String contentTransferEncoding = null;
        if (contentType == null) {
            if (isText) {
                scontentType = HttpPostBodyUtil.DEFAULT_TEXT_CONTENT_TYPE;
            }
            else {
                scontentType = HttpPostBodyUtil.DEFAULT_BINARY_CONTENT_TYPE;
            }
        }
        if (!isText) {
            contentTransferEncoding = HttpPostBodyUtil.TransferEncodingMechanism.BINARY.value();
        }
        final FileUpload fileUpload = this.factory.createFileUpload(this.request, name, file.getName(), scontentType, contentTransferEncoding, null, file.length());
        try {
            fileUpload.setContent(file);
        }
        catch (IOException e) {
            throw new ErrorDataEncoderException(e);
        }
        this.addBodyHttpData(fileUpload);
    }
    
    public void addBodyFileUploads(final String name, final File[] file, final String[] contentType, final boolean[] isText) throws ErrorDataEncoderException {
        if (file.length != contentType.length && file.length != isText.length) {
            throw new NullPointerException("Different array length");
        }
        for (int i = 0; i < file.length; ++i) {
            this.addBodyFileUpload(name, file[i], contentType[i], isText[i]);
        }
    }
    
    public void addBodyHttpData(final InterfaceHttpData data) throws ErrorDataEncoderException {
        if (this.headerFinalized) {
            throw new ErrorDataEncoderException("Cannot add value once finalized");
        }
        if (data == null) {
            throw new NullPointerException("data");
        }
        this.bodyListDatas.add(data);
        if (!this.isMultipart) {
            if (data instanceof Attribute) {
                final Attribute attribute = (Attribute)data;
                try {
                    final String key = this.encodeAttribute(attribute.getName(), this.charset);
                    final String value = this.encodeAttribute(attribute.getValue(), this.charset);
                    final Attribute newattribute = this.factory.createAttribute(this.request, key, value);
                    this.multipartHttpDatas.add(newattribute);
                    this.globalBodySize += newattribute.getName().length() + 1 + newattribute.length() + 1L;
                }
                catch (IOException e) {
                    throw new ErrorDataEncoderException(e);
                }
            }
            else if (data instanceof FileUpload) {
                final FileUpload fileUpload = (FileUpload)data;
                final String key = this.encodeAttribute(fileUpload.getName(), this.charset);
                final String value = this.encodeAttribute(fileUpload.getFilename(), this.charset);
                final Attribute newattribute = this.factory.createAttribute(this.request, key, value);
                this.multipartHttpDatas.add(newattribute);
                this.globalBodySize += newattribute.getName().length() + 1 + newattribute.length() + 1L;
            }
            return;
        }
        if (data instanceof Attribute) {
            if (this.duringMixedMode) {
                final InternalAttribute internal = new InternalAttribute(this.charset);
                internal.addValue("\r\n--" + this.multipartMixedBoundary + "--");
                this.multipartHttpDatas.add(internal);
                this.multipartMixedBoundary = null;
                this.currentFileUpload = null;
                this.duringMixedMode = false;
            }
            final InternalAttribute internal = new InternalAttribute(this.charset);
            if (!this.multipartHttpDatas.isEmpty()) {
                internal.addValue("\r\n");
            }
            internal.addValue("--" + this.multipartDataBoundary + "\r\n");
            final Attribute attribute2 = (Attribute)data;
            internal.addValue((Object)HttpHeaderNames.CONTENT_DISPOSITION + ": " + (Object)HttpHeaderValues.FORM_DATA + "; " + (Object)HttpHeaderValues.NAME + "=\"" + attribute2.getName() + "\"\r\n");
            final Charset localcharset = attribute2.getCharset();
            if (localcharset != null) {
                internal.addValue((Object)HttpHeaderNames.CONTENT_TYPE + ": " + HttpPostBodyUtil.DEFAULT_TEXT_CONTENT_TYPE + "; " + (Object)HttpHeaderValues.CHARSET + '=' + localcharset + "\r\n");
            }
            internal.addValue("\r\n");
            this.multipartHttpDatas.add(internal);
            this.multipartHttpDatas.add(data);
            this.globalBodySize += attribute2.length() + internal.size();
        }
        else if (data instanceof FileUpload) {
            final FileUpload fileUpload = (FileUpload)data;
            InternalAttribute internal2 = new InternalAttribute(this.charset);
            if (!this.multipartHttpDatas.isEmpty()) {
                internal2.addValue("\r\n");
            }
            boolean localMixed;
            if (this.duringMixedMode) {
                if (this.currentFileUpload != null && this.currentFileUpload.getName().equals(fileUpload.getName())) {
                    localMixed = true;
                }
                else {
                    internal2.addValue("--" + this.multipartMixedBoundary + "--");
                    this.multipartHttpDatas.add(internal2);
                    this.multipartMixedBoundary = null;
                    internal2 = new InternalAttribute(this.charset);
                    internal2.addValue("\r\n");
                    localMixed = false;
                    this.currentFileUpload = fileUpload;
                    this.duringMixedMode = false;
                }
            }
            else if (this.encoderMode != EncoderMode.HTML5 && this.currentFileUpload != null && this.currentFileUpload.getName().equals(fileUpload.getName())) {
                this.initMixedMultipart();
                final InternalAttribute pastAttribute = this.multipartHttpDatas.get(this.multipartHttpDatas.size() - 2);
                this.globalBodySize -= pastAttribute.size();
                final StringBuilder replacement = new StringBuilder(139 + this.multipartDataBoundary.length() + this.multipartMixedBoundary.length() * 2 + fileUpload.getFilename().length() + fileUpload.getName().length()).append("--").append(this.multipartDataBoundary).append("\r\n").append(HttpHeaderNames.CONTENT_DISPOSITION).append(": ").append(HttpHeaderValues.FORM_DATA).append("; ").append(HttpHeaderValues.NAME).append("=\"").append(fileUpload.getName()).append("\"\r\n").append(HttpHeaderNames.CONTENT_TYPE).append(": ").append(HttpHeaderValues.MULTIPART_MIXED).append("; ").append(HttpHeaderValues.BOUNDARY).append('=').append(this.multipartMixedBoundary).append("\r\n\r\n").append("--").append(this.multipartMixedBoundary).append("\r\n").append(HttpHeaderNames.CONTENT_DISPOSITION).append(": ").append(HttpHeaderValues.ATTACHMENT).append("; ").append(HttpHeaderValues.FILENAME).append("=\"").append(fileUpload.getFilename()).append("\"\r\n");
                pastAttribute.setValue(replacement.toString(), 1);
                pastAttribute.setValue("", 2);
                this.globalBodySize += pastAttribute.size();
                localMixed = true;
                this.duringMixedMode = true;
            }
            else {
                localMixed = false;
                this.currentFileUpload = fileUpload;
                this.duringMixedMode = false;
            }
            if (localMixed) {
                internal2.addValue("--" + this.multipartMixedBoundary + "\r\n");
                internal2.addValue((Object)HttpHeaderNames.CONTENT_DISPOSITION + ": " + (Object)HttpHeaderValues.ATTACHMENT + "; " + (Object)HttpHeaderValues.FILENAME + "=\"" + fileUpload.getFilename() + "\"\r\n");
            }
            else {
                internal2.addValue("--" + this.multipartDataBoundary + "\r\n");
                internal2.addValue((Object)HttpHeaderNames.CONTENT_DISPOSITION + ": " + (Object)HttpHeaderValues.FORM_DATA + "; " + (Object)HttpHeaderValues.NAME + "=\"" + fileUpload.getName() + "\"; " + (Object)HttpHeaderValues.FILENAME + "=\"" + fileUpload.getFilename() + "\"\r\n");
            }
            internal2.addValue((Object)HttpHeaderNames.CONTENT_TYPE + ": " + fileUpload.getContentType());
            final String contentTransferEncoding = fileUpload.getContentTransferEncoding();
            if (contentTransferEncoding != null && contentTransferEncoding.equals(HttpPostBodyUtil.TransferEncodingMechanism.BINARY.value())) {
                internal2.addValue("\r\n" + (Object)HttpHeaderNames.CONTENT_TRANSFER_ENCODING + ": " + HttpPostBodyUtil.TransferEncodingMechanism.BINARY.value() + "\r\n\r\n");
            }
            else if (fileUpload.getCharset() != null) {
                internal2.addValue("; " + (Object)HttpHeaderValues.CHARSET + '=' + fileUpload.getCharset() + "\r\n\r\n");
            }
            else {
                internal2.addValue("\r\n\r\n");
            }
            this.multipartHttpDatas.add(internal2);
            this.multipartHttpDatas.add(data);
            this.globalBodySize += fileUpload.length() + internal2.size();
        }
    }
    
    public HttpRequest finalizeRequest() throws ErrorDataEncoderException {
        if (this.headerFinalized) {
            throw new ErrorDataEncoderException("Header already encoded");
        }
        if (this.isMultipart) {
            final InternalAttribute internal = new InternalAttribute(this.charset);
            if (this.duringMixedMode) {
                internal.addValue("\r\n--" + this.multipartMixedBoundary + "--");
            }
            internal.addValue("\r\n--" + this.multipartDataBoundary + "--\r\n");
            this.multipartHttpDatas.add(internal);
            this.multipartMixedBoundary = null;
            this.currentFileUpload = null;
            this.duringMixedMode = false;
            this.globalBodySize += internal.size();
        }
        this.headerFinalized = true;
        final HttpHeaders headers = this.request.headers();
        final List<String> contentTypes = ((ConvertibleHeaders<AsciiString, String>)headers).getAllAndConvert(HttpHeaderNames.CONTENT_TYPE);
        final List<CharSequence> transferEncoding = (List<CharSequence>)((Headers<AsciiString>)headers).getAll(HttpHeaderNames.TRANSFER_ENCODING);
        if (contentTypes != null) {
            ((Headers<AsciiString>)headers).remove(HttpHeaderNames.CONTENT_TYPE);
            for (final String contentType : contentTypes) {
                final String lowercased = contentType.toLowerCase();
                if (!lowercased.startsWith(HttpHeaderValues.MULTIPART_FORM_DATA.toString())) {
                    if (lowercased.startsWith(HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())) {
                        continue;
                    }
                    headers.add((CharSequence)HttpHeaderNames.CONTENT_TYPE, (CharSequence)contentType);
                }
            }
        }
        if (this.isMultipart) {
            final String value = (Object)HttpHeaderValues.MULTIPART_FORM_DATA + "; " + (Object)HttpHeaderValues.BOUNDARY + '=' + this.multipartDataBoundary;
            headers.add((CharSequence)HttpHeaderNames.CONTENT_TYPE, (CharSequence)value);
        }
        else {
            headers.add((CharSequence)HttpHeaderNames.CONTENT_TYPE, (CharSequence)HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED);
        }
        long realSize = this.globalBodySize;
        if (this.isMultipart) {
            this.iterator = this.multipartHttpDatas.listIterator();
        }
        else {
            --realSize;
            this.iterator = this.multipartHttpDatas.listIterator();
        }
        headers.set((CharSequence)HttpHeaderNames.CONTENT_LENGTH, (CharSequence)String.valueOf(realSize));
        if (realSize > 8096L || this.isMultipart) {
            this.isChunked = true;
            if (transferEncoding != null) {
                ((Headers<AsciiString>)headers).remove(HttpHeaderNames.TRANSFER_ENCODING);
                for (final CharSequence v : transferEncoding) {
                    if (HttpHeaderValues.CHUNKED.equalsIgnoreCase(v)) {
                        continue;
                    }
                    headers.add((CharSequence)HttpHeaderNames.TRANSFER_ENCODING, v);
                }
            }
            HttpHeaderUtil.setTransferEncodingChunked(this.request, true);
            return new WrappedHttpRequest(this.request);
        }
        final HttpContent chunk = this.nextChunk();
        if (this.request instanceof FullHttpRequest) {
            final FullHttpRequest fullRequest = (FullHttpRequest)this.request;
            final ByteBuf chunkContent = chunk.content();
            if (fullRequest.content() != chunkContent) {
                fullRequest.content().clear().writeBytes(chunkContent);
                chunkContent.release();
            }
            return fullRequest;
        }
        return new WrappedFullHttpRequest(this.request, chunk);
    }
    
    public boolean isChunked() {
        return this.isChunked;
    }
    
    private String encodeAttribute(final String s, final Charset charset) throws ErrorDataEncoderException {
        if (s == null) {
            return "";
        }
        try {
            String encoded = URLEncoder.encode(s, charset.name());
            if (this.encoderMode == EncoderMode.RFC3986) {
                for (final Map.Entry<Pattern, String> entry : HttpPostRequestEncoder.percentEncodings.entrySet()) {
                    final String replacement = entry.getValue();
                    encoded = entry.getKey().matcher(encoded).replaceAll(replacement);
                }
            }
            return encoded;
        }
        catch (UnsupportedEncodingException e) {
            throw new ErrorDataEncoderException(charset.name(), e);
        }
    }
    
    private ByteBuf fillByteBuf() {
        final int length = this.currentBuffer.readableBytes();
        if (length > 8096) {
            final ByteBuf slice = this.currentBuffer.slice(this.currentBuffer.readerIndex(), 8096);
            this.currentBuffer.retain();
            this.currentBuffer.skipBytes(8096);
            return slice;
        }
        final ByteBuf slice = this.currentBuffer;
        this.currentBuffer = null;
        return slice;
    }
    
    private HttpContent encodeNextChunkMultipart(final int sizeleft) throws ErrorDataEncoderException {
        if (this.currentData == null) {
            return null;
        }
        ByteBuf buffer = null;
        if (this.currentData instanceof InternalAttribute) {
            buffer = ((InternalAttribute)this.currentData).toByteBuf();
            this.currentData = null;
        }
        else {
            Label_0102: {
                if (this.currentData instanceof Attribute) {
                    try {
                        buffer = ((Attribute)this.currentData).getChunk(sizeleft);
                        break Label_0102;
                    }
                    catch (IOException e) {
                        throw new ErrorDataEncoderException(e);
                    }
                }
                try {
                    buffer = ((HttpData)this.currentData).getChunk(sizeleft);
                }
                catch (IOException e) {
                    throw new ErrorDataEncoderException(e);
                }
            }
            if (buffer.capacity() == 0) {
                this.currentData = null;
                return null;
            }
        }
        if (this.currentBuffer == null) {
            this.currentBuffer = buffer;
        }
        else {
            this.currentBuffer = Unpooled.wrappedBuffer(this.currentBuffer, buffer);
        }
        if (this.currentBuffer.readableBytes() < 8096) {
            this.currentData = null;
            return null;
        }
        buffer = this.fillByteBuf();
        return new DefaultHttpContent(buffer);
    }
    
    private HttpContent encodeNextChunkUrlEncoded(final int sizeleft) throws ErrorDataEncoderException {
        if (this.currentData == null) {
            return null;
        }
        int size = sizeleft;
        if (this.isKey) {
            final String key = this.currentData.getName();
            ByteBuf buffer = Unpooled.wrappedBuffer(key.getBytes());
            this.isKey = false;
            if (this.currentBuffer == null) {
                this.currentBuffer = Unpooled.wrappedBuffer(buffer, Unpooled.wrappedBuffer("=".getBytes()));
                size -= buffer.readableBytes() + 1;
            }
            else {
                this.currentBuffer = Unpooled.wrappedBuffer(this.currentBuffer, buffer, Unpooled.wrappedBuffer("=".getBytes()));
                size -= buffer.readableBytes() + 1;
            }
            if (this.currentBuffer.readableBytes() >= 8096) {
                buffer = this.fillByteBuf();
                return new DefaultHttpContent(buffer);
            }
        }
        ByteBuf buffer;
        try {
            buffer = ((HttpData)this.currentData).getChunk(size);
        }
        catch (IOException e) {
            throw new ErrorDataEncoderException(e);
        }
        ByteBuf delimiter = null;
        if (buffer.readableBytes() < size) {
            this.isKey = true;
            delimiter = (this.iterator.hasNext() ? Unpooled.wrappedBuffer("&".getBytes()) : null);
        }
        if (buffer.capacity() == 0) {
            this.currentData = null;
            if (this.currentBuffer == null) {
                this.currentBuffer = delimiter;
            }
            else if (delimiter != null) {
                this.currentBuffer = Unpooled.wrappedBuffer(this.currentBuffer, delimiter);
            }
            if (this.currentBuffer.readableBytes() >= 8096) {
                buffer = this.fillByteBuf();
                return new DefaultHttpContent(buffer);
            }
            return null;
        }
        else {
            if (this.currentBuffer == null) {
                if (delimiter != null) {
                    this.currentBuffer = Unpooled.wrappedBuffer(buffer, delimiter);
                }
                else {
                    this.currentBuffer = buffer;
                }
            }
            else if (delimiter != null) {
                this.currentBuffer = Unpooled.wrappedBuffer(this.currentBuffer, buffer, delimiter);
            }
            else {
                this.currentBuffer = Unpooled.wrappedBuffer(this.currentBuffer, buffer);
            }
            if (this.currentBuffer.readableBytes() < 8096) {
                this.currentData = null;
                this.isKey = true;
                return null;
            }
            buffer = this.fillByteBuf();
            return new DefaultHttpContent(buffer);
        }
    }
    
    @Override
    public void close() throws Exception {
    }
    
    @Override
    public HttpContent readChunk(final ChannelHandlerContext ctx) throws Exception {
        if (this.isLastChunkSent) {
            return null;
        }
        final HttpContent nextChunk = this.nextChunk();
        this.globalProgress += nextChunk.content().readableBytes();
        return nextChunk;
    }
    
    private HttpContent nextChunk() throws ErrorDataEncoderException {
        if (this.isLastChunk) {
            this.isLastChunkSent = true;
            return LastHttpContent.EMPTY_LAST_CONTENT;
        }
        int size = 8096;
        if (this.currentBuffer != null) {
            size -= this.currentBuffer.readableBytes();
        }
        if (size <= 0) {
            final ByteBuf buffer = this.fillByteBuf();
            return new DefaultHttpContent(buffer);
        }
        if (this.currentData != null) {
            if (this.isMultipart) {
                final HttpContent chunk = this.encodeNextChunkMultipart(size);
                if (chunk != null) {
                    return chunk;
                }
            }
            else {
                final HttpContent chunk = this.encodeNextChunkUrlEncoded(size);
                if (chunk != null) {
                    return chunk;
                }
            }
            size = 8096 - this.currentBuffer.readableBytes();
        }
        if (!this.iterator.hasNext()) {
            this.isLastChunk = true;
            final ByteBuf buffer = this.currentBuffer;
            this.currentBuffer = null;
            return new DefaultHttpContent(buffer);
        }
        while (size > 0 && this.iterator.hasNext()) {
            this.currentData = this.iterator.next();
            HttpContent chunk;
            if (this.isMultipart) {
                chunk = this.encodeNextChunkMultipart(size);
            }
            else {
                chunk = this.encodeNextChunkUrlEncoded(size);
            }
            if (chunk != null) {
                return chunk;
            }
            size = 8096 - this.currentBuffer.readableBytes();
        }
        this.isLastChunk = true;
        if (this.currentBuffer == null) {
            this.isLastChunkSent = true;
            return LastHttpContent.EMPTY_LAST_CONTENT;
        }
        final ByteBuf buffer = this.currentBuffer;
        this.currentBuffer = null;
        return new DefaultHttpContent(buffer);
    }
    
    @Override
    public boolean isEndOfInput() throws Exception {
        return this.isLastChunkSent;
    }
    
    @Override
    public long length() {
        return this.isMultipart ? this.globalBodySize : (this.globalBodySize - 1L);
    }
    
    @Override
    public long progress() {
        return this.globalProgress;
    }
    
    static {
        (percentEncodings = new HashMap<Pattern, String>()).put(Pattern.compile("\\*"), "%2A");
        HttpPostRequestEncoder.percentEncodings.put(Pattern.compile("\\+"), "%20");
        HttpPostRequestEncoder.percentEncodings.put(Pattern.compile("%7E"), "~");
    }
    
    public enum EncoderMode
    {
        RFC1738, 
        RFC3986, 
        HTML5;
    }
    
    public static class ErrorDataEncoderException extends Exception
    {
        private static final long serialVersionUID = 5020247425493164465L;
        
        public ErrorDataEncoderException() {
        }
        
        public ErrorDataEncoderException(final String msg) {
            super(msg);
        }
        
        public ErrorDataEncoderException(final Throwable cause) {
            super(cause);
        }
        
        public ErrorDataEncoderException(final String msg, final Throwable cause) {
            super(msg, cause);
        }
    }
    
    private static class WrappedHttpRequest implements HttpRequest
    {
        private final HttpRequest request;
        
        WrappedHttpRequest(final HttpRequest request) {
            this.request = request;
        }
        
        @Override
        public HttpRequest setProtocolVersion(final HttpVersion version) {
            this.request.setProtocolVersion(version);
            return this;
        }
        
        @Override
        public HttpRequest setMethod(final HttpMethod method) {
            this.request.setMethod(method);
            return this;
        }
        
        @Override
        public HttpRequest setUri(final String uri) {
            this.request.setUri(uri);
            return this;
        }
        
        @Override
        public HttpMethod method() {
            return this.request.method();
        }
        
        @Override
        public String uri() {
            return this.request.uri();
        }
        
        @Override
        public HttpVersion protocolVersion() {
            return this.request.protocolVersion();
        }
        
        @Override
        public HttpHeaders headers() {
            return this.request.headers();
        }
        
        @Override
        public DecoderResult decoderResult() {
            return this.request.decoderResult();
        }
        
        @Override
        public void setDecoderResult(final DecoderResult result) {
            this.request.setDecoderResult(result);
        }
    }
    
    private static final class WrappedFullHttpRequest extends WrappedHttpRequest implements FullHttpRequest
    {
        private final HttpContent content;
        
        private WrappedFullHttpRequest(final HttpRequest request, final HttpContent content) {
            super(request);
            this.content = content;
        }
        
        @Override
        public FullHttpRequest setProtocolVersion(final HttpVersion version) {
            super.setProtocolVersion(version);
            return this;
        }
        
        @Override
        public FullHttpRequest setMethod(final HttpMethod method) {
            super.setMethod(method);
            return this;
        }
        
        @Override
        public FullHttpRequest setUri(final String uri) {
            super.setUri(uri);
            return this;
        }
        
        private FullHttpRequest copy(final boolean copyContent, final ByteBuf newContent) {
            final DefaultFullHttpRequest copy = new DefaultFullHttpRequest(this.protocolVersion(), this.method(), this.uri(), copyContent ? this.content().copy() : ((newContent == null) ? Unpooled.buffer(0) : newContent));
            copy.headers().set((TextHeaders)this.headers());
            copy.trailingHeaders().set((TextHeaders)this.trailingHeaders());
            return copy;
        }
        
        @Override
        public FullHttpRequest copy(final ByteBuf newContent) {
            return this.copy(false, newContent);
        }
        
        @Override
        public FullHttpRequest copy() {
            return this.copy(true, null);
        }
        
        @Override
        public FullHttpRequest duplicate() {
            final DefaultFullHttpRequest duplicate = new DefaultFullHttpRequest(this.protocolVersion(), this.method(), this.uri(), this.content().duplicate());
            duplicate.headers().set((TextHeaders)this.headers());
            duplicate.trailingHeaders().set((TextHeaders)this.trailingHeaders());
            return duplicate;
        }
        
        @Override
        public FullHttpRequest retain(final int increment) {
            this.content.retain(increment);
            return this;
        }
        
        @Override
        public FullHttpRequest retain() {
            this.content.retain();
            return this;
        }
        
        @Override
        public FullHttpRequest touch() {
            this.content.touch();
            return this;
        }
        
        @Override
        public FullHttpRequest touch(final Object hint) {
            this.content.touch(hint);
            return this;
        }
        
        @Override
        public ByteBuf content() {
            return this.content.content();
        }
        
        @Override
        public HttpHeaders trailingHeaders() {
            if (this.content instanceof LastHttpContent) {
                return ((LastHttpContent)this.content).trailingHeaders();
            }
            return EmptyHttpHeaders.INSTANCE;
        }
        
        @Override
        public int refCnt() {
            return this.content.refCnt();
        }
        
        @Override
        public boolean release() {
            return this.content.release();
        }
        
        @Override
        public boolean release(final int decrement) {
            return this.content.release(decrement);
        }
    }
}
