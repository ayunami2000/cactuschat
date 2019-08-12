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

@Plugin(name = "notEmpty", category = "Converter")
@ConverterKeys({ "notEmpty", "varsNotEmpty", "variablesNotEmpty" })
public final class VariablesNotEmptyReplacementConverter extends LogEventPatternConverter
{
    private final List<PatternFormatter> formatters;
    
    private VariablesNotEmptyReplacementConverter(final List<PatternFormatter> formatters) {
        super("notEmpty", "notEmpty");
        this.formatters = formatters;
    }
    
    public static VariablesNotEmptyReplacementConverter newInstance(final Configuration config, final String[] options) {
        if (options.length != 1) {
            VariablesNotEmptyReplacementConverter.LOGGER.error("Incorrect number of options on varsNotEmpty. Expected 1 received " + options.length);
            return null;
        }
        if (options[0] == null) {
            VariablesNotEmptyReplacementConverter.LOGGER.error("No pattern supplied on varsNotEmpty");
            return null;
        }
        final PatternParser parser = PatternLayout.createPatternParser(config);
        final List<PatternFormatter> formatters = parser.parse(options[0]);
        return new VariablesNotEmptyReplacementConverter(formatters);
    }
    
    @Override
    public void format(final LogEvent event, final StringBuilder toAppendTo) {
        final StringBuilder buf = new StringBuilder();
        int emptyVars = 0;
        int vars = 0;
        for (final PatternFormatter formatter : this.formatters) {
            final int start = buf.length();
            formatter.format(event, buf);
            final boolean isVariable = formatter.getConverter().isVariable();
            vars += (isVariable ? 1 : 0);
            if (isVariable && buf.length() - start == 0) {
                ++emptyVars;
            }
        }
        if (vars > 0 && emptyVars != vars) {
            toAppendTo.append(buf.toString());
        }
    }
}
