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

@Plugin(name = "equals", category = "Converter")
@ConverterKeys({ "equals" })
public final class EqualsReplacementConverter extends LogEventPatternConverter
{
    private final List<PatternFormatter> formatters;
    private final String substitution;
    private final String testString;
    
    public static EqualsReplacementConverter newInstance(final Configuration config, final String[] options) {
        if (options.length != 3) {
            EqualsReplacementConverter.LOGGER.error("Incorrect number of options on equals. Expected 3 received " + options.length);
            return null;
        }
        if (options[0] == null) {
            EqualsReplacementConverter.LOGGER.error("No pattern supplied on equals");
            return null;
        }
        if (options[1] == null) {
            EqualsReplacementConverter.LOGGER.error("No test string supplied on equals");
            return null;
        }
        if (options[2] == null) {
            EqualsReplacementConverter.LOGGER.error("No substitution supplied on equals");
            return null;
        }
        final String p = options[1];
        final PatternParser parser = PatternLayout.createPatternParser(config);
        final List<PatternFormatter> formatters = parser.parse(options[0]);
        return new EqualsReplacementConverter(formatters, p, options[2]);
    }
    
    private EqualsReplacementConverter(final List<PatternFormatter> formatters, final String testString, final String substitution) {
        super("equals", "equals");
        this.testString = testString;
        this.substitution = substitution;
        this.formatters = formatters;
    }
    
    @Override
    public void format(final LogEvent event, final StringBuilder toAppendTo) {
        final StringBuilder buf = new StringBuilder();
        for (final PatternFormatter formatter : this.formatters) {
            formatter.format(event, buf);
        }
        final String string = buf.toString();
        toAppendTo.append(this.testString.equals(string) ? this.substitution : string);
    }
}
