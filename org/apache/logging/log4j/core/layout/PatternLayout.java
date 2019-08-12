// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.layout;

import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.util.Builder;
import java.io.Serializable;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.core.LogEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import org.apache.logging.log4j.core.pattern.PatternParser;
import org.apache.logging.log4j.core.util.StringEncoder;
import java.nio.charset.Charset;
import org.apache.logging.log4j.core.pattern.RegexReplacement;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.pattern.PatternFormatter;
import org.apache.logging.log4j.core.config.plugins.Plugin;

@Plugin(name = "PatternLayout", category = "Core", elementType = "layout", printObject = true)
public final class PatternLayout extends AbstractStringLayout
{
    public static final String DEFAULT_CONVERSION_PATTERN = "%m%n";
    public static final String TTCC_CONVERSION_PATTERN = "%r [%t] %p %c %x - %m%n";
    public static final String SIMPLE_CONVERSION_PATTERN = "%d [%t] %p %c - %m%n";
    public static final String KEY = "Converter";
    private static final long serialVersionUID = 1L;
    private final PatternFormatter[] formatters;
    private final String conversionPattern;
    private final PatternSelector patternSelector;
    private final Serializer serializer;
    private final Configuration config;
    private final RegexReplacement replace;
    private final boolean alwaysWriteExceptions;
    private final boolean noConsoleNoAnsi;
    
    private PatternLayout(final Configuration config, final RegexReplacement replace, final String pattern, final PatternSelector patternSelector, final Charset charset, final boolean alwaysWriteExceptions, final boolean noConsoleNoAnsi, final String header, final String footer) {
        super(charset, StringEncoder.toBytes(header, charset), StringEncoder.toBytes(footer, charset));
        this.replace = replace;
        this.conversionPattern = pattern;
        this.patternSelector = patternSelector;
        this.config = config;
        this.alwaysWriteExceptions = alwaysWriteExceptions;
        this.noConsoleNoAnsi = noConsoleNoAnsi;
        if (patternSelector == null) {
            this.serializer = new PatternSerializer();
            final PatternParser parser = createPatternParser(config);
            try {
                final List<PatternFormatter> list = parser.parse((pattern == null) ? "%m%n" : pattern, this.alwaysWriteExceptions, this.noConsoleNoAnsi);
                this.formatters = list.toArray(new PatternFormatter[0]);
            }
            catch (RuntimeException ex) {
                throw new IllegalArgumentException("Cannot parse pattern '" + pattern + "'", ex);
            }
        }
        else {
            this.formatters = null;
            this.serializer = new PatternSelectorSerializer();
        }
    }
    
    private byte[] strSubstitutorReplace(final byte... b) {
        if (b != null && this.config != null) {
            return this.getBytes(this.config.getStrSubstitutor().replace(new String(b, this.getCharset())));
        }
        return b;
    }
    
    @Override
    public byte[] getHeader() {
        return this.strSubstitutorReplace(super.getHeader());
    }
    
    @Override
    public byte[] getFooter() {
        return this.strSubstitutorReplace(super.getFooter());
    }
    
    public String getConversionPattern() {
        return this.conversionPattern;
    }
    
    @Override
    public Map<String, String> getContentFormat() {
        final Map<String, String> result = new HashMap<String, String>();
        result.put("structured", "false");
        result.put("formatType", "conversion");
        result.put("format", this.conversionPattern);
        return result;
    }
    
    @Override
    public String toSerializable(final LogEvent event) {
        return this.serializer.toSerializable(event);
    }
    
    public static PatternParser createPatternParser(final Configuration config) {
        if (config == null) {
            return new PatternParser(config, "Converter", LogEventPatternConverter.class);
        }
        PatternParser parser = config.getComponent("Converter");
        if (parser == null) {
            parser = new PatternParser(config, "Converter", LogEventPatternConverter.class);
            config.addComponent("Converter", parser);
            parser = config.getComponent("Converter");
        }
        return parser;
    }
    
    @Override
    public String toString() {
        return (this.patternSelector == null) ? this.conversionPattern : this.patternSelector.toString();
    }
    
    @PluginFactory
    public static PatternLayout createLayout(@PluginAttribute(value = "pattern", defaultString = "%m%n") final String pattern, @PluginElement("PatternSelector") final PatternSelector patternSelector, @PluginConfiguration final Configuration config, @PluginElement("Replace") final RegexReplacement replace, @PluginAttribute("charset") final Charset charset, @PluginAttribute(value = "alwaysWriteExceptions", defaultBoolean = true) final boolean alwaysWriteExceptions, @PluginAttribute(value = "noConsoleNoAnsi", defaultBoolean = false) final boolean noConsoleNoAnsi, @PluginAttribute("header") final String header, @PluginAttribute("footer") final String footer) {
        return newBuilder().withPattern(pattern).withPatternSelector(patternSelector).withConfiguration(config).withRegexReplacement(replace).withCharset(charset).withAlwaysWriteExceptions(alwaysWriteExceptions).withNoConsoleNoAnsi(noConsoleNoAnsi).withHeader(header).withFooter(footer).build();
    }
    
    public static PatternLayout createDefaultLayout() {
        return newBuilder().build();
    }
    
    public static PatternLayout createDefaultLayout(final Configuration configuration) {
        return newBuilder().withConfiguration(configuration).build();
    }
    
    @PluginBuilderFactory
    public static Builder newBuilder() {
        return new Builder();
    }
    
    private class PatternSerializer implements Serializer
    {
        @Override
        public String toSerializable(final LogEvent event) {
            final StringBuilder buf = PatternLayout.this.getStringBuilder();
            for (int len = PatternLayout.this.formatters.length, i = 0; i < len; ++i) {
                PatternLayout.this.formatters[i].format(event, buf);
            }
            String str = buf.toString();
            if (PatternLayout.this.replace != null) {
                str = PatternLayout.this.replace.format(str);
            }
            return str;
        }
    }
    
    private class PatternSelectorSerializer implements Serializer
    {
        @Override
        public String toSerializable(final LogEvent event) {
            final StringBuilder buf = PatternLayout.this.getStringBuilder();
            final PatternFormatter[] formatters = PatternLayout.this.patternSelector.getFormatters(event);
            for (int len = formatters.length, i = 0; i < len; ++i) {
                formatters[i].format(event, buf);
            }
            String str = buf.toString();
            if (PatternLayout.this.replace != null) {
                str = PatternLayout.this.replace.format(str);
            }
            return str;
        }
    }
    
    public static class Builder implements org.apache.logging.log4j.core.util.Builder<PatternLayout>
    {
        @PluginBuilderAttribute
        private String pattern;
        @PluginElement("PatternSelector")
        private PatternSelector patternSelector;
        @PluginConfiguration
        private Configuration configuration;
        @PluginElement("Replace")
        private RegexReplacement regexReplacement;
        @PluginBuilderAttribute
        private Charset charset;
        @PluginBuilderAttribute
        private boolean alwaysWriteExceptions;
        @PluginBuilderAttribute
        private boolean noConsoleNoAnsi;
        @PluginBuilderAttribute
        private String header;
        @PluginBuilderAttribute
        private String footer;
        
        private Builder() {
            this.pattern = "%m%n";
            this.patternSelector = null;
            this.configuration = null;
            this.regexReplacement = null;
            this.charset = Charset.defaultCharset();
            this.alwaysWriteExceptions = true;
            this.noConsoleNoAnsi = false;
            this.header = null;
            this.footer = null;
        }
        
        public Builder withPattern(final String pattern) {
            this.pattern = pattern;
            return this;
        }
        
        public Builder withPatternSelector(final PatternSelector patternSelector) {
            this.patternSelector = patternSelector;
            return this;
        }
        
        public Builder withConfiguration(final Configuration configuration) {
            this.configuration = configuration;
            return this;
        }
        
        public Builder withRegexReplacement(final RegexReplacement regexReplacement) {
            this.regexReplacement = regexReplacement;
            return this;
        }
        
        public Builder withCharset(final Charset charset) {
            if (charset != null) {
                this.charset = charset;
            }
            return this;
        }
        
        public Builder withAlwaysWriteExceptions(final boolean alwaysWriteExceptions) {
            this.alwaysWriteExceptions = alwaysWriteExceptions;
            return this;
        }
        
        public Builder withNoConsoleNoAnsi(final boolean noConsoleNoAnsi) {
            this.noConsoleNoAnsi = noConsoleNoAnsi;
            return this;
        }
        
        public Builder withHeader(final String header) {
            this.header = header;
            return this;
        }
        
        public Builder withFooter(final String footer) {
            this.footer = footer;
            return this;
        }
        
        @Override
        public PatternLayout build() {
            if (this.configuration == null) {
                this.configuration = new DefaultConfiguration();
            }
            return new PatternLayout(this.configuration, this.regexReplacement, this.pattern, this.patternSelector, this.charset, this.alwaysWriteExceptions, this.noConsoleNoAnsi, this.header, this.footer, null);
        }
    }
    
    private interface Serializer
    {
        String toSerializable(final LogEvent p0);
    }
}
