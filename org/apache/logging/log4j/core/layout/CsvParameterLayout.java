// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.layout;

import java.io.Serializable;
import org.apache.logging.log4j.message.Message;
import java.io.IOException;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.commons.csv.QuoteMode;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.commons.csv.CSVFormat;
import java.nio.charset.Charset;
import org.apache.logging.log4j.core.config.plugins.Plugin;

@Plugin(name = "CsvParameterLayout", category = "Core", elementType = "layout", printObject = true)
public class CsvParameterLayout extends AbstractCsvLayout
{
    private static final long serialVersionUID = 1L;
    
    public static AbstractCsvLayout createDefaultLayout() {
        return new CsvParameterLayout(Charset.forName("UTF-8"), CSVFormat.valueOf("Default"), null, null);
    }
    
    public static AbstractCsvLayout createLayout(final CSVFormat format) {
        return new CsvParameterLayout(Charset.forName("UTF-8"), format, null, null);
    }
    
    @PluginFactory
    public static AbstractCsvLayout createLayout(@PluginAttribute(value = "format", defaultString = "Default") final String format, @PluginAttribute("delimiter") final Character delimiter, @PluginAttribute("escape") final Character escape, @PluginAttribute("quote") final Character quote, @PluginAttribute("quoteMode") final QuoteMode quoteMode, @PluginAttribute("nullString") final String nullString, @PluginAttribute("recordSeparator") final String recordSeparator, @PluginAttribute(value = "charset", defaultString = "UTF-8") final Charset charset, @PluginAttribute("header") final String header, @PluginAttribute("footer") final String footer) {
        final CSVFormat csvFormat = AbstractCsvLayout.createFormat(format, delimiter, escape, quote, quoteMode, nullString, recordSeparator);
        return new CsvParameterLayout(charset, csvFormat, header, footer);
    }
    
    public CsvParameterLayout(final Charset charset, final CSVFormat csvFormat, final String header, final String footer) {
        super(charset, csvFormat, header, footer);
    }
    
    @Override
    public String toSerializable(final LogEvent event) {
        final Message message = event.getMessage();
        final Object[] parameters = message.getParameters();
        final StringBuilder buffer = this.getStringBuilder();
        try (final CSVPrinter printer = new CSVPrinter((Appendable)buffer, this.getFormat())) {
            printer.printRecord(parameters);
            return buffer.toString();
        }
        catch (IOException e) {
            StatusLogger.getLogger().error(message, e);
            return this.getFormat().getCommentMarker() + " " + e;
        }
    }
}
