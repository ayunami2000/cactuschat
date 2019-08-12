// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.config.plugins.convert;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URISyntaxException;
import java.net.URI;
import java.security.Security;
import java.security.Provider;
import java.util.regex.Pattern;
import org.apache.logging.log4j.Level;
import java.io.File;
import org.apache.logging.log4j.core.appender.rolling.action.Duration;
import org.apache.logging.log4j.core.util.CronExpression;
import org.apache.logging.log4j.core.util.Loader;
import java.nio.charset.Charset;
import javax.xml.bind.DatatypeConverter;
import java.math.BigInteger;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import java.math.BigDecimal;
import org.apache.logging.log4j.status.StatusLogger;
import java.lang.reflect.Type;
import org.apache.logging.log4j.Logger;

public final class TypeConverters
{
    public static final String CATEGORY = "TypeConverter";
    private static final Logger LOGGER;
    
    public static Object convert(final String s, final Class<?> clazz, final Object defaultValue) {
        final TypeConverter<?> converter = TypeConverterRegistry.getInstance().findCompatibleConverter(clazz);
        if (s == null) {
            return parseDefaultValue(converter, defaultValue);
        }
        try {
            return converter.convert(s);
        }
        catch (Exception e) {
            TypeConverters.LOGGER.warn("Error while converting string [{}] to type [{}]. Using default value [{}].", new Object[] { s, clazz, defaultValue, e });
            return parseDefaultValue(converter, defaultValue);
        }
    }
    
    private static Object parseDefaultValue(final TypeConverter<?> converter, final Object defaultValue) {
        if (defaultValue == null) {
            return null;
        }
        if (!(defaultValue instanceof String)) {
            return defaultValue;
        }
        try {
            return converter.convert((String)defaultValue);
        }
        catch (Exception e) {
            TypeConverters.LOGGER.debug("Can't parse default value [{}] for type [{}].", new Object[] { defaultValue, converter.getClass(), e });
            return null;
        }
    }
    
    static {
        LOGGER = StatusLogger.getLogger();
    }
    
    @Plugin(name = "BigDecimal", category = "TypeConverter")
    public static class BigDecimalConverter implements TypeConverter<BigDecimal>
    {
        @Override
        public BigDecimal convert(final String s) {
            return new BigDecimal(s);
        }
    }
    
    @Plugin(name = "BigInteger", category = "TypeConverter")
    public static class BigIntegerConverter implements TypeConverter<BigInteger>
    {
        @Override
        public BigInteger convert(final String s) {
            return new BigInteger(s);
        }
    }
    
    @Plugin(name = "Boolean", category = "TypeConverter")
    public static class BooleanConverter implements TypeConverter<Boolean>
    {
        @Override
        public Boolean convert(final String s) {
            return Boolean.valueOf(s);
        }
    }
    
    @Plugin(name = "ByteArray", category = "TypeConverter")
    public static class ByteArrayConverter implements TypeConverter<byte[]>
    {
        private static final String PREFIX_0x = "0x";
        private static final String PREFIX_BASE64 = "Base64:";
        
        @Override
        public byte[] convert(final String value) {
            byte[] bytes;
            if (value == null || value.isEmpty()) {
                bytes = new byte[0];
            }
            else if (value.startsWith("Base64:")) {
                final String lexicalXSDBase64Binary = value.substring("Base64:".length());
                bytes = DatatypeConverter.parseBase64Binary(lexicalXSDBase64Binary);
            }
            else if (value.startsWith("0x")) {
                final String lexicalXSDHexBinary = value.substring("0x".length());
                bytes = DatatypeConverter.parseHexBinary(lexicalXSDHexBinary);
            }
            else {
                bytes = value.getBytes(Charset.defaultCharset());
            }
            return bytes;
        }
    }
    
    @Plugin(name = "Byte", category = "TypeConverter")
    public static class ByteConverter implements TypeConverter<Byte>
    {
        @Override
        public Byte convert(final String s) {
            return Byte.valueOf(s);
        }
    }
    
    @Plugin(name = "Character", category = "TypeConverter")
    public static class CharacterConverter implements TypeConverter<Character>
    {
        @Override
        public Character convert(final String s) {
            if (s.length() != 1) {
                throw new IllegalArgumentException("Character string must be of length 1: " + s);
            }
            return s.toCharArray()[0];
        }
    }
    
    @Plugin(name = "CharacterArray", category = "TypeConverter")
    public static class CharArrayConverter implements TypeConverter<char[]>
    {
        @Override
        public char[] convert(final String s) {
            return s.toCharArray();
        }
    }
    
    @Plugin(name = "Charset", category = "TypeConverter")
    public static class CharsetConverter implements TypeConverter<Charset>
    {
        @Override
        public Charset convert(final String s) {
            return Charset.forName(s);
        }
    }
    
    @Plugin(name = "Class", category = "TypeConverter")
    public static class ClassConverter implements TypeConverter<Class<?>>
    {
        @Override
        public Class<?> convert(final String s) throws ClassNotFoundException {
            return Loader.loadClass(s);
        }
    }
    
    @Plugin(name = "CronExpression", category = "TypeConverter")
    public static class CronExpressionConverter implements TypeConverter<CronExpression>
    {
        @Override
        public CronExpression convert(final String s) throws Exception {
            return new CronExpression(s);
        }
    }
    
    @Plugin(name = "Double", category = "TypeConverter")
    public static class DoubleConverter implements TypeConverter<Double>
    {
        @Override
        public Double convert(final String s) {
            return Double.valueOf(s);
        }
    }
    
    @Plugin(name = "Duration", category = "TypeConverter")
    public static class DurationConverter implements TypeConverter<Duration>
    {
        @Override
        public Duration convert(final String s) {
            return Duration.parse(s);
        }
    }
    
    @Plugin(name = "File", category = "TypeConverter")
    public static class FileConverter implements TypeConverter<File>
    {
        @Override
        public File convert(final String s) {
            return new File(s);
        }
    }
    
    @Plugin(name = "Float", category = "TypeConverter")
    public static class FloatConverter implements TypeConverter<Float>
    {
        @Override
        public Float convert(final String s) {
            return Float.valueOf(s);
        }
    }
    
    @Plugin(name = "Integer", category = "TypeConverter")
    public static class IntegerConverter implements TypeConverter<Integer>
    {
        @Override
        public Integer convert(final String s) {
            return Integer.valueOf(s);
        }
    }
    
    @Plugin(name = "Level", category = "TypeConverter")
    public static class LevelConverter implements TypeConverter<Level>
    {
        @Override
        public Level convert(final String s) {
            return Level.valueOf(s);
        }
    }
    
    @Plugin(name = "Long", category = "TypeConverter")
    public static class LongConverter implements TypeConverter<Long>
    {
        @Override
        public Long convert(final String s) {
            return Long.valueOf(s);
        }
    }
    
    @Plugin(name = "Pattern", category = "TypeConverter")
    public static class PatternConverter implements TypeConverter<Pattern>
    {
        @Override
        public Pattern convert(final String s) {
            return Pattern.compile(s);
        }
    }
    
    @Plugin(name = "SecurityProvider", category = "TypeConverter")
    public static class SecurityProviderConverter implements TypeConverter<Provider>
    {
        @Override
        public Provider convert(final String s) {
            return Security.getProvider(s);
        }
    }
    
    @Plugin(name = "Short", category = "TypeConverter")
    public static class ShortConverter implements TypeConverter<Short>
    {
        @Override
        public Short convert(final String s) {
            return Short.valueOf(s);
        }
    }
    
    @Plugin(name = "String", category = "TypeConverter")
    public static class StringConverter implements TypeConverter<String>
    {
        @Override
        public String convert(final String s) {
            return s;
        }
    }
    
    @Plugin(name = "URI", category = "TypeConverter")
    public static class UriConverter implements TypeConverter<URI>
    {
        @Override
        public URI convert(final String s) throws URISyntaxException {
            return new URI(s);
        }
    }
    
    @Plugin(name = "URL", category = "TypeConverter")
    public static class UrlConverter implements TypeConverter<URL>
    {
        @Override
        public URL convert(final String s) throws MalformedURLException {
            return new URL(s);
        }
    }
}
