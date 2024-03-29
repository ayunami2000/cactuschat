// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.layout;

import java.nio.charset.StandardCharsets;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.util.Builder;
import java.io.Serializable;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import java.util.Date;
import java.util.Iterator;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.io.Reader;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.util.Transform;
import org.apache.logging.log4j.core.util.Constants;
import org.apache.logging.log4j.core.LogEvent;
import java.lang.management.ManagementFactory;
import java.nio.charset.Charset;
import org.apache.logging.log4j.core.config.plugins.Plugin;

@Plugin(name = "HtmlLayout", category = "Core", elementType = "layout", printObject = true)
public final class HtmlLayout extends AbstractStringLayout
{
    public static final String DEFAULT_FONT_FAMILY = "arial,sans-serif";
    private static final long serialVersionUID = 1L;
    private static final String TRACE_PREFIX = "<br />&nbsp;&nbsp;&nbsp;&nbsp;";
    private static final String REGEXP;
    private static final String DEFAULT_TITLE = "Log4j Log Messages";
    private static final String DEFAULT_CONTENT_TYPE = "text/html";
    private final long jvmStartTime;
    private final boolean locationInfo;
    private final String title;
    private final String contentType;
    private final String font;
    private final String fontSize;
    private final String headerSize;
    
    private HtmlLayout(final boolean locationInfo, final String title, final String contentType, final Charset charset, final String font, final String fontSize, final String headerSize) {
        super(charset);
        this.jvmStartTime = ManagementFactory.getRuntimeMXBean().getStartTime();
        this.locationInfo = locationInfo;
        this.title = title;
        this.contentType = this.addCharsetToContentType(contentType);
        this.font = font;
        this.fontSize = fontSize;
        this.headerSize = headerSize;
    }
    
    private String addCharsetToContentType(final String contentType) {
        if (contentType == null) {
            return "text/html; charset=" + this.getCharset();
        }
        return contentType.contains("charset") ? contentType : (contentType + "; charset=" + this.getCharset());
    }
    
    @Override
    public String toSerializable(final LogEvent event) {
        final StringBuilder sbuf = this.getStringBuilder();
        sbuf.append(Constants.LINE_SEPARATOR).append("<tr>").append(Constants.LINE_SEPARATOR);
        sbuf.append("<td>");
        sbuf.append(event.getTimeMillis() - this.jvmStartTime);
        sbuf.append("</td>").append(Constants.LINE_SEPARATOR);
        final String escapedThread = Transform.escapeHtmlTags(event.getThreadName());
        sbuf.append("<td title=\"").append(escapedThread).append(" thread\">");
        sbuf.append(escapedThread);
        sbuf.append("</td>").append(Constants.LINE_SEPARATOR);
        sbuf.append("<td title=\"Level\">");
        if (event.getLevel().equals(Level.DEBUG)) {
            sbuf.append("<font color=\"#339933\">");
            sbuf.append(Transform.escapeHtmlTags(String.valueOf(event.getLevel())));
            sbuf.append("</font>");
        }
        else if (event.getLevel().isMoreSpecificThan(Level.WARN)) {
            sbuf.append("<font color=\"#993300\"><strong>");
            sbuf.append(Transform.escapeHtmlTags(String.valueOf(event.getLevel())));
            sbuf.append("</strong></font>");
        }
        else {
            sbuf.append(Transform.escapeHtmlTags(String.valueOf(event.getLevel())));
        }
        sbuf.append("</td>").append(Constants.LINE_SEPARATOR);
        String escapedLogger = Transform.escapeHtmlTags(event.getLoggerName());
        if (escapedLogger.isEmpty()) {
            escapedLogger = "root";
        }
        sbuf.append("<td title=\"").append(escapedLogger).append(" logger\">");
        sbuf.append(escapedLogger);
        sbuf.append("</td>").append(Constants.LINE_SEPARATOR);
        if (this.locationInfo) {
            final StackTraceElement element = event.getSource();
            sbuf.append("<td>");
            sbuf.append(Transform.escapeHtmlTags(element.getFileName()));
            sbuf.append(':');
            sbuf.append(element.getLineNumber());
            sbuf.append("</td>").append(Constants.LINE_SEPARATOR);
        }
        sbuf.append("<td title=\"Message\">");
        sbuf.append(Transform.escapeHtmlTags(event.getMessage().getFormattedMessage()).replaceAll(HtmlLayout.REGEXP, "<br />"));
        sbuf.append("</td>").append(Constants.LINE_SEPARATOR);
        sbuf.append("</tr>").append(Constants.LINE_SEPARATOR);
        if (event.getContextStack() != null && !event.getContextStack().isEmpty()) {
            sbuf.append("<tr><td bgcolor=\"#EEEEEE\" style=\"font-size : ").append(this.fontSize);
            sbuf.append(";\" colspan=\"6\" ");
            sbuf.append("title=\"Nested Diagnostic Context\">");
            sbuf.append("NDC: ").append(Transform.escapeHtmlTags(event.getContextStack().toString()));
            sbuf.append("</td></tr>").append(Constants.LINE_SEPARATOR);
        }
        if (event.getContextMap() != null && !event.getContextMap().isEmpty()) {
            sbuf.append("<tr><td bgcolor=\"#EEEEEE\" style=\"font-size : ").append(this.fontSize);
            sbuf.append(";\" colspan=\"6\" ");
            sbuf.append("title=\"Mapped Diagnostic Context\">");
            sbuf.append("MDC: ").append(Transform.escapeHtmlTags(event.getContextMap().toString()));
            sbuf.append("</td></tr>").append(Constants.LINE_SEPARATOR);
        }
        final Throwable throwable = event.getThrown();
        if (throwable != null) {
            sbuf.append("<tr><td bgcolor=\"#993300\" style=\"color:White; font-size : ").append(this.fontSize);
            sbuf.append(";\" colspan=\"6\">");
            this.appendThrowableAsHtml(throwable, sbuf);
            sbuf.append("</td></tr>").append(Constants.LINE_SEPARATOR);
        }
        return sbuf.toString();
    }
    
    @Override
    public String getContentType() {
        return this.contentType;
    }
    
    private void appendThrowableAsHtml(final Throwable throwable, final StringBuilder sbuf) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        try {
            throwable.printStackTrace(pw);
        }
        catch (RuntimeException ex2) {}
        pw.flush();
        final LineNumberReader reader = new LineNumberReader(new StringReader(sw.toString()));
        final ArrayList<String> lines = new ArrayList<String>();
        try {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                lines.add(line);
            }
        }
        catch (IOException ex) {
            if (ex instanceof InterruptedIOException) {
                Thread.currentThread().interrupt();
            }
            lines.add(ex.toString());
        }
        boolean first = true;
        for (final String line2 : lines) {
            if (!first) {
                sbuf.append("<br />&nbsp;&nbsp;&nbsp;&nbsp;");
            }
            else {
                first = false;
            }
            sbuf.append(Transform.escapeHtmlTags(line2));
            sbuf.append(Constants.LINE_SEPARATOR);
        }
    }
    
    @Override
    public byte[] getHeader() {
        final StringBuilder sbuf = new StringBuilder();
        sbuf.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" ");
        sbuf.append("\"http://www.w3.org/TR/html4/loose.dtd\">");
        sbuf.append(Constants.LINE_SEPARATOR);
        sbuf.append("<html>").append(Constants.LINE_SEPARATOR);
        sbuf.append("<head>").append(Constants.LINE_SEPARATOR);
        sbuf.append("<meta charset=\"").append(this.getCharset()).append("\"/>").append(Constants.LINE_SEPARATOR);
        sbuf.append("<title>").append(this.title).append("</title>").append(Constants.LINE_SEPARATOR);
        sbuf.append("<style type=\"text/css\">").append(Constants.LINE_SEPARATOR);
        sbuf.append("<!--").append(Constants.LINE_SEPARATOR);
        sbuf.append("body, table {font-family:").append(this.font).append("; font-size: ");
        sbuf.append(this.headerSize).append(";}").append(Constants.LINE_SEPARATOR);
        sbuf.append("th {background: #336699; color: #FFFFFF; text-align: left;}").append(Constants.LINE_SEPARATOR);
        sbuf.append("-->").append(Constants.LINE_SEPARATOR);
        sbuf.append("</style>").append(Constants.LINE_SEPARATOR);
        sbuf.append("</head>").append(Constants.LINE_SEPARATOR);
        sbuf.append("<body bgcolor=\"#FFFFFF\" topmargin=\"6\" leftmargin=\"6\">").append(Constants.LINE_SEPARATOR);
        sbuf.append("<hr size=\"1\" noshade=\"noshade\">").append(Constants.LINE_SEPARATOR);
        sbuf.append("Log session start time " + new Date() + "<br>").append(Constants.LINE_SEPARATOR);
        sbuf.append("<br>").append(Constants.LINE_SEPARATOR);
        sbuf.append("<table cellspacing=\"0\" cellpadding=\"4\" border=\"1\" bordercolor=\"#224466\" width=\"100%\">");
        sbuf.append(Constants.LINE_SEPARATOR);
        sbuf.append("<tr>").append(Constants.LINE_SEPARATOR);
        sbuf.append("<th>Time</th>").append(Constants.LINE_SEPARATOR);
        sbuf.append("<th>Thread</th>").append(Constants.LINE_SEPARATOR);
        sbuf.append("<th>Level</th>").append(Constants.LINE_SEPARATOR);
        sbuf.append("<th>Logger</th>").append(Constants.LINE_SEPARATOR);
        if (this.locationInfo) {
            sbuf.append("<th>File:Line</th>").append(Constants.LINE_SEPARATOR);
        }
        sbuf.append("<th>Message</th>").append(Constants.LINE_SEPARATOR);
        sbuf.append("</tr>").append(Constants.LINE_SEPARATOR);
        return sbuf.toString().getBytes(this.getCharset());
    }
    
    @Override
    public byte[] getFooter() {
        final StringBuilder sbuf = new StringBuilder();
        sbuf.append("</table>").append(Constants.LINE_SEPARATOR);
        sbuf.append("<br>").append(Constants.LINE_SEPARATOR);
        sbuf.append("</body></html>");
        return this.getBytes(sbuf.toString());
    }
    
    @PluginFactory
    public static HtmlLayout createLayout(@PluginAttribute(value = "locationInfo", defaultBoolean = false) final boolean locationInfo, @PluginAttribute(value = "title", defaultString = "Log4j Log Messages") final String title, @PluginAttribute("contentType") String contentType, @PluginAttribute(value = "charset", defaultString = "UTF-8") final Charset charset, @PluginAttribute("fontSize") String fontSize, @PluginAttribute(value = "fontName", defaultString = "arial,sans-serif") final String font) {
        final FontSize fs = FontSize.getFontSize(fontSize);
        fontSize = fs.getFontSize();
        final String headerSize = fs.larger().getFontSize();
        if (contentType == null) {
            contentType = "text/html; charset=" + charset;
        }
        return new HtmlLayout(locationInfo, title, contentType, charset, font, fontSize, headerSize);
    }
    
    public static HtmlLayout createDefaultLayout() {
        return newBuilder().build();
    }
    
    @PluginBuilderFactory
    public static Builder newBuilder() {
        return new Builder();
    }
    
    static {
        REGEXP = (Constants.LINE_SEPARATOR.equals("\n") ? "\n" : (Constants.LINE_SEPARATOR + "|\n"));
    }
    
    public enum FontSize
    {
        SMALLER("smaller"), 
        XXSMALL("xx-small"), 
        XSMALL("x-small"), 
        SMALL("small"), 
        MEDIUM("medium"), 
        LARGE("large"), 
        XLARGE("x-large"), 
        XXLARGE("xx-large"), 
        LARGER("larger");
        
        private final String size;
        
        private FontSize(final String size) {
            this.size = size;
        }
        
        public String getFontSize() {
            return this.size;
        }
        
        public static FontSize getFontSize(final String size) {
            for (final FontSize fontSize : values()) {
                if (fontSize.size.equals(size)) {
                    return fontSize;
                }
            }
            return FontSize.SMALL;
        }
        
        public FontSize larger() {
            return (this.ordinal() < FontSize.XXLARGE.ordinal()) ? values()[this.ordinal() + 1] : this;
        }
    }
    
    public static class Builder implements org.apache.logging.log4j.core.util.Builder<HtmlLayout>
    {
        @PluginBuilderAttribute
        private boolean locationInfo;
        @PluginBuilderAttribute
        private String title;
        @PluginBuilderAttribute
        private String contentType;
        @PluginBuilderAttribute
        private Charset charset;
        @PluginBuilderAttribute
        private FontSize fontSize;
        @PluginBuilderAttribute
        private String fontName;
        
        private Builder() {
            this.locationInfo = false;
            this.title = "Log4j Log Messages";
            this.contentType = null;
            this.charset = StandardCharsets.UTF_8;
            this.fontSize = FontSize.SMALL;
            this.fontName = "arial,sans-serif";
        }
        
        public Builder withLocationInfo(final boolean locationInfo) {
            this.locationInfo = locationInfo;
            return this;
        }
        
        public Builder withTitle(final String title) {
            this.title = title;
            return this;
        }
        
        public Builder withContentType(final String contentType) {
            this.contentType = contentType;
            return this;
        }
        
        public Builder withCharset(final Charset charset) {
            this.charset = charset;
            return this;
        }
        
        public Builder withFontSize(final FontSize fontSize) {
            this.fontSize = fontSize;
            return this;
        }
        
        public Builder withFontName(final String fontName) {
            this.fontName = fontName;
            return this;
        }
        
        @Override
        public HtmlLayout build() {
            if (this.contentType == null) {
                this.contentType = "text/html; charset=" + this.charset;
            }
            return new HtmlLayout(this.locationInfo, this.title, this.contentType, this.charset, this.fontName, this.fontSize.getFontSize(), this.fontSize.larger().getFontSize(), null);
        }
    }
}
