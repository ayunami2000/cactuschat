// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http2;

import io.netty.handler.codec.ConvertibleHeaders;
import java.util.HashMap;
import io.netty.handler.codec.BinaryHeaders;
import java.util.HashSet;
import io.netty.handler.codec.http.HttpHeaderValues;
import java.util.Map;
import io.netty.handler.codec.TextHeaders;
import io.netty.handler.codec.http.HttpResponse;
import java.net.URI;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpHeaderUtil;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.Headers;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.util.internal.ObjectUtil;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.AsciiString;
import java.util.regex.Pattern;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpMethod;
import java.util.Set;

public final class HttpUtil
{
    private static final Set<CharSequence> HTTP_TO_HTTP2_HEADER_BLACKLIST;
    public static final HttpMethod OUT_OF_MESSAGE_SEQUENCE_METHOD;
    public static final String OUT_OF_MESSAGE_SEQUENCE_PATH = "";
    public static final HttpResponseStatus OUT_OF_MESSAGE_SEQUENCE_RETURN_CODE;
    private static final Pattern AUTHORITY_REPLACEMENT_PATTERN;
    
    private HttpUtil() {
    }
    
    public static HttpResponseStatus parseStatus(final AsciiString status) throws Http2Exception {
        HttpResponseStatus result;
        try {
            result = HttpResponseStatus.parseLine(status);
            if (result == HttpResponseStatus.SWITCHING_PROTOCOLS) {
                throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Invalid HTTP/2 status code '%d'", result.code());
            }
        }
        catch (Http2Exception e) {
            throw e;
        }
        catch (Throwable t) {
            throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, t, "Unrecognized HTTP status code '%s' encountered in translation to HTTP/1.x", status);
        }
        return result;
    }
    
    public static FullHttpResponse toHttpResponse(final int streamId, final Http2Headers http2Headers, final boolean validateHttpHeaders) throws Http2Exception {
        final HttpResponseStatus status = parseStatus(http2Headers.status());
        final FullHttpResponse msg = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, validateHttpHeaders);
        addHttp2ToHttpHeaders(streamId, http2Headers, msg, false);
        return msg;
    }
    
    public static FullHttpRequest toHttpRequest(final int streamId, final Http2Headers http2Headers, final boolean validateHttpHeaders) throws Http2Exception {
        final AsciiString method = ObjectUtil.checkNotNull(http2Headers.method(), "method header cannot be null in conversion to HTTP/1.x");
        final AsciiString path = ObjectUtil.checkNotNull(http2Headers.path(), "path header cannot be null in conversion to HTTP/1.x");
        final FullHttpRequest msg = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.valueOf(method.toString()), path.toString(), validateHttpHeaders);
        addHttp2ToHttpHeaders(streamId, http2Headers, msg, false);
        return msg;
    }
    
    public static void addHttp2ToHttpHeaders(final int streamId, final Http2Headers sourceHeaders, final FullHttpMessage destinationMessage, final boolean addToTrailer) throws Http2Exception {
        final HttpHeaders headers = addToTrailer ? destinationMessage.trailingHeaders() : destinationMessage.headers();
        final boolean request = destinationMessage instanceof HttpRequest;
        final Http2ToHttpHeaderTranslator visitor = new Http2ToHttpHeaderTranslator(streamId, headers, request);
        try {
            sourceHeaders.forEachEntry(visitor);
        }
        catch (Http2Exception ex) {
            throw ex;
        }
        catch (Throwable t) {
            throw Http2Exception.streamError(streamId, Http2Error.PROTOCOL_ERROR, t, "HTTP/2 to HTTP/1.x headers conversion error", new Object[0]);
        }
        ((Headers<AsciiString>)headers).remove(HttpHeaderNames.TRANSFER_ENCODING);
        ((Headers<AsciiString>)headers).remove(HttpHeaderNames.TRAILER);
        if (!addToTrailer) {
            headers.setInt((CharSequence)ExtensionHeaderNames.STREAM_ID.text(), streamId);
            HttpHeaderUtil.setKeepAlive(destinationMessage, true);
        }
    }
    
    public static Http2Headers toHttp2Headers(final FullHttpMessage in) throws Exception {
        final Http2Headers out = new DefaultHttp2Headers();
        final HttpHeaders inHeaders = in.headers();
        if (in instanceof HttpRequest) {
            final HttpRequest request = (HttpRequest)in;
            out.path(new AsciiString(request.uri()));
            out.method(new AsciiString(request.method().toString()));
            String value = ((ConvertibleHeaders<AsciiString, String>)inHeaders).getAndConvert(HttpHeaderNames.HOST);
            if (value != null) {
                final URI hostUri = URI.create(value);
                value = hostUri.getAuthority();
                if (value != null) {
                    out.authority(new AsciiString(HttpUtil.AUTHORITY_REPLACEMENT_PATTERN.matcher(value).replaceFirst("")));
                }
                value = hostUri.getScheme();
                if (value != null) {
                    out.scheme(new AsciiString(value));
                }
            }
            CharSequence cValue = ((Headers<AsciiString>)inHeaders).get(ExtensionHeaderNames.AUTHORITY.text());
            if (cValue != null) {
                out.authority(AsciiString.of(cValue));
            }
            cValue = ((Headers<AsciiString>)inHeaders).get(ExtensionHeaderNames.SCHEME.text());
            if (cValue != null) {
                out.scheme(AsciiString.of(cValue));
            }
        }
        else if (in instanceof HttpResponse) {
            final HttpResponse response = (HttpResponse)in;
            out.status(new AsciiString(Integer.toString(response.status().code())));
        }
        inHeaders.forEachEntry(new TextHeaders.EntryVisitor() {
            @Override
            public boolean visit(final Map.Entry<CharSequence, CharSequence> entry) throws Exception {
                final AsciiString aName = AsciiString.of(entry.getKey()).toLowerCase();
                if (!HttpUtil.HTTP_TO_HTTP2_HEADER_BLACKLIST.contains(aName)) {
                    final AsciiString aValue = AsciiString.of(entry.getValue());
                    if (!aName.equalsIgnoreCase(HttpHeaderNames.TE) || aValue.equalsIgnoreCase(HttpHeaderValues.TRAILERS)) {
                        out.add(aName, aValue);
                    }
                }
                return true;
            }
        });
        return out;
    }
    
    static {
        HTTP_TO_HTTP2_HEADER_BLACKLIST = new HashSet<CharSequence>() {
            private static final long serialVersionUID = -5678614530214167043L;
            
            {
                ((HashSet<AsciiString>)this).add(HttpHeaderNames.CONNECTION);
                ((HashSet<AsciiString>)this).add(HttpHeaderNames.KEEP_ALIVE);
                ((HashSet<AsciiString>)this).add(HttpHeaderNames.PROXY_CONNECTION);
                ((HashSet<AsciiString>)this).add(HttpHeaderNames.TRANSFER_ENCODING);
                ((HashSet<AsciiString>)this).add(HttpHeaderNames.HOST);
                ((HashSet<AsciiString>)this).add(HttpHeaderNames.UPGRADE);
                ((HashSet<AsciiString>)this).add(ExtensionHeaderNames.STREAM_ID.text());
                ((HashSet<AsciiString>)this).add(ExtensionHeaderNames.AUTHORITY.text());
                ((HashSet<AsciiString>)this).add(ExtensionHeaderNames.SCHEME.text());
                ((HashSet<AsciiString>)this).add(ExtensionHeaderNames.PATH.text());
            }
        };
        OUT_OF_MESSAGE_SEQUENCE_METHOD = HttpMethod.OPTIONS;
        OUT_OF_MESSAGE_SEQUENCE_RETURN_CODE = HttpResponseStatus.OK;
        AUTHORITY_REPLACEMENT_PATTERN = Pattern.compile("^.*@");
    }
    
    public enum ExtensionHeaderNames
    {
        STREAM_ID("x-http2-stream-id"), 
        AUTHORITY("x-http2-authority"), 
        SCHEME("x-http2-scheme"), 
        PATH("x-http2-path"), 
        STREAM_PROMISE_ID("x-http2-stream-promise-id"), 
        STREAM_DEPENDENCY_ID("x-http2-stream-dependency-id"), 
        STREAM_WEIGHT("x-http2-stream-weight");
        
        private final AsciiString text;
        
        private ExtensionHeaderNames(final String text) {
            this.text = new AsciiString(text);
        }
        
        public AsciiString text() {
            return this.text;
        }
    }
    
    private static final class Http2ToHttpHeaderTranslator implements BinaryHeaders.EntryVisitor
    {
        private static final Map<AsciiString, AsciiString> REQUEST_HEADER_TRANSLATIONS;
        private static final Map<AsciiString, AsciiString> RESPONSE_HEADER_TRANSLATIONS;
        private final int streamId;
        private final HttpHeaders output;
        private final Map<AsciiString, AsciiString> translations;
        
        Http2ToHttpHeaderTranslator(final int streamId, final HttpHeaders output, final boolean request) {
            this.streamId = streamId;
            this.output = output;
            this.translations = (request ? Http2ToHttpHeaderTranslator.REQUEST_HEADER_TRANSLATIONS : Http2ToHttpHeaderTranslator.RESPONSE_HEADER_TRANSLATIONS);
        }
        
        @Override
        public boolean visit(final Map.Entry<AsciiString, AsciiString> entry) throws Http2Exception {
            final AsciiString name = entry.getKey();
            final AsciiString value = entry.getValue();
            AsciiString translatedName = this.translations.get(name);
            if (translatedName != null || !Http2Headers.PseudoHeaderName.isPseudoHeader(name)) {
                if (translatedName == null) {
                    translatedName = name;
                }
                if (translatedName.isEmpty() || translatedName.charAt(0) == ':') {
                    throw Http2Exception.streamError(this.streamId, Http2Error.PROTOCOL_ERROR, "Invalid HTTP/2 header '%s' encountered in translation to HTTP/1.x", translatedName);
                }
                this.output.add((CharSequence)translatedName, (CharSequence)value);
            }
            return true;
        }
        
        static {
            REQUEST_HEADER_TRANSLATIONS = new HashMap<AsciiString, AsciiString>();
            (RESPONSE_HEADER_TRANSLATIONS = new HashMap<AsciiString, AsciiString>()).put(Http2Headers.PseudoHeaderName.AUTHORITY.value(), ExtensionHeaderNames.AUTHORITY.text());
            Http2ToHttpHeaderTranslator.RESPONSE_HEADER_TRANSLATIONS.put(Http2Headers.PseudoHeaderName.SCHEME.value(), ExtensionHeaderNames.SCHEME.text());
            Http2ToHttpHeaderTranslator.REQUEST_HEADER_TRANSLATIONS.putAll(Http2ToHttpHeaderTranslator.RESPONSE_HEADER_TRANSLATIONS);
            Http2ToHttpHeaderTranslator.RESPONSE_HEADER_TRANSLATIONS.put(Http2Headers.PseudoHeaderName.PATH.value(), ExtensionHeaderNames.PATH.text());
        }
    }
}
