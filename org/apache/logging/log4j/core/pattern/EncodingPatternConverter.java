// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.pattern;

import java.util.Iterator;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.config.Configuration;
import java.util.List;
import org.apache.logging.log4j.core.config.plugins.Plugin;

@Plugin(name = "encode", category = "Converter")
@ConverterKeys({ "enc", "encode" })
public final class EncodingPatternConverter extends LogEventPatternConverter
{
    private final List<PatternFormatter> formatters;
    
    private EncodingPatternConverter(final List<PatternFormatter> formatters) {
        super("encode", "encode");
        this.formatters = formatters;
    }
    
    public static EncodingPatternConverter newInstance(final Configuration config, final String[] options) {
        if (options.length != 1) {
            EncodingPatternConverter.LOGGER.error("Incorrect number of options on escape. Expected 1, received " + options.length);
            return null;
        }
        if (options[0] == null) {
            EncodingPatternConverter.LOGGER.error("No pattern supplied on escape");
            return null;
        }
        final PatternParser parser = PatternLayout.createPatternParser(config);
        final List<PatternFormatter> formatters = parser.parse(options[0]);
        return new EncodingPatternConverter(formatters);
    }
    
    @Override
    public void format(final LogEvent event, final StringBuilder toAppendTo) {
        final StringBuilder buf = new StringBuilder();
        for (final PatternFormatter formatter : this.formatters) {
            formatter.format(event, buf);
        }
        for (int i = 0; i < buf.length(); ++i) {
            final char c = buf.charAt(i);
            switch (c) {
                case '\r': {
                    toAppendTo.append("\\r");
                    break;
                }
                case '\n': {
                    toAppendTo.append("\\n");
                    break;
                }
                case '&': {
                    toAppendTo.append("&amp;");
                    break;
                }
                case '<': {
                    toAppendTo.append("&lt;");
                    break;
                }
                case '>': {
                    toAppendTo.append("&gt;");
                    break;
                }
                case '\"': {
                    toAppendTo.append("&quot;");
                    break;
                }
                case '\'': {
                    toAppendTo.append("&apos;");
                    break;
                }
                case '/': {
                    toAppendTo.append("&#x2F;");
                    break;
                }
                default: {
                    toAppendTo.append(c);
                    break;
                }
            }
        }
    }
}
