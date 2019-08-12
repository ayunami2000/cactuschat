// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.layout;

import java.util.zip.GZIPOutputStream;
import java.io.Serializable;
import java.util.Iterator;
import com.fasterxml.jackson.core.io.JsonStringEncoder;
import org.apache.logging.log4j.core.LogEvent;
import java.util.Collections;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;
import java.io.IOException;
import org.apache.logging.log4j.status.StatusLogger;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.apache.logging.log4j.core.net.Severity;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import java.nio.charset.StandardCharsets;
import org.apache.logging.log4j.core.util.KeyValuePair;
import java.math.BigDecimal;
import org.apache.logging.log4j.core.config.plugins.Plugin;

@Plugin(name = "GelfLayout", category = "Core", elementType = "layout", printObject = true)
public final class GelfLayout extends AbstractStringLayout
{
    private static final char C = ',';
    private static final int COMPRESSION_THRESHOLD = 1024;
    private static final char Q = '\"';
    private static final String QC = "\",";
    private static final String QU = "\"_";
    private static final long serialVersionUID = 1L;
    private static final BigDecimal TIME_DIVISOR;
    private final KeyValuePair[] additionalFields;
    private final int compressionThreshold;
    private final CompressionType compressionType;
    private final String host;
    
    public GelfLayout(final String host, final KeyValuePair[] additionalFields, final CompressionType compressionType, final int compressionThreshold) {
        super(StandardCharsets.UTF_8);
        this.host = host;
        this.additionalFields = additionalFields;
        this.compressionType = compressionType;
        this.compressionThreshold = compressionThreshold;
    }
    
    @PluginFactory
    public static GelfLayout createLayout(@PluginAttribute("host") final String host, @PluginElement("AdditionalField") final KeyValuePair[] additionalFields, @PluginAttribute(value = "compressionType", defaultString = "GZIP") final CompressionType compressionType, @PluginAttribute(value = "compressionThreshold", defaultInt = 1024) final int compressionThreshold) {
        return new GelfLayout(host, additionalFields, compressionType, compressionThreshold);
    }
    
    static int formatLevel(final Level level) {
        return Severity.getSeverity(level).getCode();
    }
    
    static String formatThrowable(final Throwable throwable) {
        final StringWriter sw = new StringWriter(2048);
        final PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }
    
    static String formatTimestamp(final long timeMillis) {
        return new BigDecimal(timeMillis).divide(GelfLayout.TIME_DIVISOR).toPlainString();
    }
    
    private byte[] compress(final byte[] bytes) {
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream(this.compressionThreshold / 8);
            try (final DeflaterOutputStream stream = this.compressionType.createDeflaterOutputStream(baos)) {
                if (stream == null) {
                    return bytes;
                }
                stream.write(bytes);
                stream.finish();
            }
            return baos.toByteArray();
        }
        catch (IOException e) {
            StatusLogger.getLogger().error(e);
            return bytes;
        }
    }
    
    @Override
    public Map<String, String> getContentFormat() {
        return Collections.emptyMap();
    }
    
    @Override
    public String getContentType() {
        return "application/json; charset=" + this.getCharset();
    }
    
    @Override
    public byte[] toByteArray(final LogEvent event) {
        final byte[] bytes = this.getBytes(this.toSerializable(event));
        return (bytes.length > this.compressionThreshold) ? this.compress(bytes) : bytes;
    }
    
    @Override
    public String toSerializable(final LogEvent event) {
        final StringBuilder builder = this.getStringBuilder();
        final JsonStringEncoder jsonEncoder = JsonStringEncoder.getInstance();
        builder.append('{');
        builder.append("\"version\":\"1.1\",");
        builder.append("\"host\":\"").append(jsonEncoder.quoteAsString(this.toNullSafeString(this.host))).append("\",");
        builder.append("\"timestamp\":").append(formatTimestamp(event.getTimeMillis())).append(',');
        builder.append("\"level\":").append(formatLevel(event.getLevel())).append(',');
        if (event.getThreadName() != null) {
            builder.append("\"_thread\":\"").append(jsonEncoder.quoteAsString(event.getThreadName())).append("\",");
        }
        if (event.getLoggerName() != null) {
            builder.append("\"_logger\":\"").append(jsonEncoder.quoteAsString(event.getLoggerName())).append("\",");
        }
        for (final KeyValuePair additionalField : this.additionalFields) {
            builder.append("\"_").append(jsonEncoder.quoteAsString(additionalField.getKey())).append("\":\"").append(jsonEncoder.quoteAsString(this.toNullSafeString(additionalField.getValue()))).append("\",");
        }
        for (final Map.Entry<String, String> entry : event.getContextMap().entrySet()) {
            builder.append("\"_").append(jsonEncoder.quoteAsString((String)entry.getKey())).append("\":\"").append(jsonEncoder.quoteAsString(this.toNullSafeString(entry.getValue()))).append("\",");
        }
        if (event.getThrown() != null) {
            builder.append("\"full_message\":\"").append(jsonEncoder.quoteAsString(formatThrowable(event.getThrown()))).append("\",");
        }
        builder.append("\"short_message\":\"").append(jsonEncoder.quoteAsString(this.toNullSafeString(event.getMessage().getFormattedMessage()))).append('\"');
        builder.append('}');
        return builder.toString();
    }
    
    private String toNullSafeString(final String s) {
        return (s == null) ? "" : s;
    }
    
    static {
        TIME_DIVISOR = new BigDecimal(1000);
    }
    
    public enum CompressionType
    {
        GZIP {
            @Override
            public DeflaterOutputStream createDeflaterOutputStream(final OutputStream os) throws IOException {
                return new GZIPOutputStream(os);
            }
        }, 
        ZLIB {
            @Override
            public DeflaterOutputStream createDeflaterOutputStream(final OutputStream os) throws IOException {
                return new DeflaterOutputStream(os);
            }
        }, 
        OFF {
            @Override
            public DeflaterOutputStream createDeflaterOutputStream(final OutputStream os) throws IOException {
                return null;
            }
        };
        
        public abstract DeflaterOutputStream createDeflaterOutputStream(final OutputStream p0) throws IOException;
    }
}
