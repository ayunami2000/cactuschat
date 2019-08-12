// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.layout;

import org.apache.commons.csv.QuoteMode;
import org.apache.logging.log4j.core.util.StringEncoder;
import java.nio.charset.Charset;
import org.apache.commons.csv.CSVFormat;

public abstract class AbstractCsvLayout extends AbstractStringLayout
{
    protected static final String DEFAULT_CHARSET = "UTF-8";
    protected static final String DEFAULT_FORMAT = "Default";
    private static final String CONTENT_TYPE = "text/csv";
    private static final long serialVersionUID = 1L;
    private final CSVFormat format;
    
    protected AbstractCsvLayout(final Charset charset, final CSVFormat csvFormat, final String header, final String footer) {
        super(charset, StringEncoder.toBytes(header, charset), StringEncoder.toBytes(footer, charset));
        this.format = csvFormat;
    }
    
    protected static CSVFormat createFormat(final String format, final Character delimiter, final Character escape, final Character quote, final QuoteMode quoteMode, final String nullString, final String recordSeparator) {
        CSVFormat csvFormat = CSVFormat.valueOf(format);
        if (delimiter != null) {
            csvFormat = csvFormat.withDelimiter((char)delimiter);
        }
        if (escape != null) {
            csvFormat = csvFormat.withEscape(escape);
        }
        if (quote != null) {
            csvFormat = csvFormat.withQuote(quote);
        }
        if (quoteMode != null) {
            csvFormat = csvFormat.withQuoteMode(quoteMode);
        }
        if (nullString != null) {
            csvFormat = csvFormat.withNullString(nullString);
        }
        if (recordSeparator != null) {
            csvFormat = csvFormat.withRecordSeparator(recordSeparator);
        }
        return csvFormat;
    }
    
    @Override
    public String getContentType() {
        return "text/csv; charset=" + this.getCharset();
    }
    
    public CSVFormat getFormat() {
        return this.format;
    }
}
