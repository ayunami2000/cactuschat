// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec;

import java.util.Iterator;
import io.netty.util.internal.StringUtil;
import java.text.ParseException;
import io.netty.util.internal.PlatformDependent;
import java.util.Comparator;

public class DefaultTextHeaders extends DefaultConvertibleHeaders<CharSequence, String> implements TextHeaders
{
    private static final HashCodeGenerator<CharSequence> CHARSEQUECE_CASE_INSENSITIVE_HASH_CODE_GENERATOR;
    private static final HashCodeGenerator<CharSequence> CHARSEQUECE_CASE_SENSITIVE_HASH_CODE_GENERATOR;
    private static final Headers.ValueConverter<CharSequence> CHARSEQUENCE_FROM_OBJECT_CONVERTER;
    private static final ConvertibleHeaders.TypeConverter<CharSequence, String> CHARSEQUENCE_TO_STRING_CONVERTER;
    private static final NameConverter<CharSequence> CHARSEQUENCE_IDENTITY_CONVERTER;
    private static final int DEFAULT_VALUE_SIZE = 10;
    private final ValuesComposer valuesComposer;
    
    public DefaultTextHeaders() {
        this(true);
    }
    
    public DefaultTextHeaders(final boolean ignoreCase) {
        this(ignoreCase, DefaultTextHeaders.CHARSEQUENCE_FROM_OBJECT_CONVERTER, DefaultTextHeaders.CHARSEQUENCE_IDENTITY_CONVERTER, false);
    }
    
    public DefaultTextHeaders(final boolean ignoreCase, final boolean singleHeaderFields) {
        this(ignoreCase, DefaultTextHeaders.CHARSEQUENCE_FROM_OBJECT_CONVERTER, DefaultTextHeaders.CHARSEQUENCE_IDENTITY_CONVERTER, singleHeaderFields);
    }
    
    protected DefaultTextHeaders(final boolean ignoreCase, final Headers.ValueConverter<CharSequence> valueConverter, final NameConverter<CharSequence> nameConverter) {
        this(ignoreCase, valueConverter, nameConverter, false);
    }
    
    public DefaultTextHeaders(final boolean ignoreCase, final Headers.ValueConverter<CharSequence> valueConverter, final NameConverter<CharSequence> nameConverter, final boolean singleHeaderFields) {
        super(comparator(ignoreCase), comparator(ignoreCase), ignoreCase ? DefaultTextHeaders.CHARSEQUECE_CASE_INSENSITIVE_HASH_CODE_GENERATOR : DefaultTextHeaders.CHARSEQUECE_CASE_SENSITIVE_HASH_CODE_GENERATOR, valueConverter, DefaultTextHeaders.CHARSEQUENCE_TO_STRING_CONVERTER, nameConverter);
        this.valuesComposer = (singleHeaderFields ? new SingleHeaderValuesComposer() : new MultipleFieldsValueComposer());
    }
    
    @Override
    public boolean contains(final CharSequence name, final CharSequence value, final boolean ignoreCase) {
        return this.contains((UnconvertedType)name, (UnconvertedType)value, (Comparator<? super UnconvertedType>)comparator(ignoreCase));
    }
    
    @Override
    public boolean containsObject(final CharSequence name, final Object value, final boolean ignoreCase) {
        return this.containsObject((UnconvertedType)name, value, (Comparator<? super UnconvertedType>)comparator(ignoreCase));
    }
    
    @Override
    public TextHeaders add(final CharSequence name, final CharSequence value) {
        return this.valuesComposer.add(name, value);
    }
    
    @Override
    public TextHeaders add(final CharSequence name, final Iterable<? extends CharSequence> values) {
        return this.valuesComposer.add(name, values);
    }
    
    @Override
    public TextHeaders add(final CharSequence name, final CharSequence... values) {
        return this.valuesComposer.add(name, values);
    }
    
    @Override
    public TextHeaders addObject(final CharSequence name, final Object value) {
        return this.valuesComposer.addObject(name, value);
    }
    
    @Override
    public TextHeaders addObject(final CharSequence name, final Iterable<?> values) {
        return this.valuesComposer.addObject(name, values);
    }
    
    @Override
    public TextHeaders addObject(final CharSequence name, final Object... values) {
        return this.valuesComposer.addObject(name, values);
    }
    
    @Override
    public TextHeaders addBoolean(final CharSequence name, final boolean value) {
        super.addBoolean((UnconvertedType)name, value);
        return this;
    }
    
    @Override
    public TextHeaders addChar(final CharSequence name, final char value) {
        super.addChar((UnconvertedType)name, value);
        return this;
    }
    
    @Override
    public TextHeaders addByte(final CharSequence name, final byte value) {
        super.addByte((UnconvertedType)name, value);
        return this;
    }
    
    @Override
    public TextHeaders addShort(final CharSequence name, final short value) {
        super.addShort((UnconvertedType)name, value);
        return this;
    }
    
    @Override
    public TextHeaders addInt(final CharSequence name, final int value) {
        super.addInt((UnconvertedType)name, value);
        return this;
    }
    
    @Override
    public TextHeaders addLong(final CharSequence name, final long value) {
        super.addLong((UnconvertedType)name, value);
        return this;
    }
    
    @Override
    public TextHeaders addFloat(final CharSequence name, final float value) {
        super.addFloat((UnconvertedType)name, value);
        return this;
    }
    
    @Override
    public TextHeaders addDouble(final CharSequence name, final double value) {
        super.addDouble((UnconvertedType)name, value);
        return this;
    }
    
    @Override
    public TextHeaders addTimeMillis(final CharSequence name, final long value) {
        super.addTimeMillis((UnconvertedType)name, value);
        return this;
    }
    
    @Override
    public TextHeaders add(final TextHeaders headers) {
        super.add((Headers<UnconvertedType>)headers);
        return this;
    }
    
    @Override
    public TextHeaders set(final CharSequence name, final CharSequence value) {
        super.set((UnconvertedType)name, (UnconvertedType)value);
        return this;
    }
    
    @Override
    public TextHeaders set(final CharSequence name, final Iterable<? extends CharSequence> values) {
        return this.valuesComposer.set(name, values);
    }
    
    @Override
    public TextHeaders set(final CharSequence name, final CharSequence... values) {
        return this.valuesComposer.set(name, values);
    }
    
    @Override
    public TextHeaders setObject(final CharSequence name, final Object value) {
        super.setObject((UnconvertedType)name, value);
        return this;
    }
    
    @Override
    public TextHeaders setObject(final CharSequence name, final Iterable<?> values) {
        return this.valuesComposer.setObject(name, values);
    }
    
    @Override
    public TextHeaders setObject(final CharSequence name, final Object... values) {
        return this.valuesComposer.setObject(name, values);
    }
    
    @Override
    public TextHeaders setBoolean(final CharSequence name, final boolean value) {
        super.setBoolean((UnconvertedType)name, value);
        return this;
    }
    
    @Override
    public TextHeaders setChar(final CharSequence name, final char value) {
        super.setChar((UnconvertedType)name, value);
        return this;
    }
    
    @Override
    public TextHeaders setByte(final CharSequence name, final byte value) {
        super.setByte((UnconvertedType)name, value);
        return this;
    }
    
    @Override
    public TextHeaders setShort(final CharSequence name, final short value) {
        super.setShort((UnconvertedType)name, value);
        return this;
    }
    
    @Override
    public TextHeaders setInt(final CharSequence name, final int value) {
        super.setInt((UnconvertedType)name, value);
        return this;
    }
    
    @Override
    public TextHeaders setLong(final CharSequence name, final long value) {
        super.setLong((UnconvertedType)name, value);
        return this;
    }
    
    @Override
    public TextHeaders setFloat(final CharSequence name, final float value) {
        super.setFloat((UnconvertedType)name, value);
        return this;
    }
    
    @Override
    public TextHeaders setDouble(final CharSequence name, final double value) {
        super.setDouble((UnconvertedType)name, value);
        return this;
    }
    
    @Override
    public TextHeaders setTimeMillis(final CharSequence name, final long value) {
        super.setTimeMillis((UnconvertedType)name, value);
        return this;
    }
    
    @Override
    public TextHeaders set(final TextHeaders headers) {
        super.set((Headers<UnconvertedType>)headers);
        return this;
    }
    
    @Override
    public TextHeaders setAll(final TextHeaders headers) {
        super.setAll((Headers<UnconvertedType>)headers);
        return this;
    }
    
    @Override
    public TextHeaders clear() {
        super.clear();
        return this;
    }
    
    private static Comparator<CharSequence> comparator(final boolean ignoreCase) {
        return ignoreCase ? AsciiString.CHARSEQUENCE_CASE_INSENSITIVE_ORDER : AsciiString.CHARSEQUENCE_CASE_SENSITIVE_ORDER;
    }
    
    static {
        CHARSEQUECE_CASE_INSENSITIVE_HASH_CODE_GENERATOR = new HashCodeGenerator<CharSequence>() {
            @Override
            public int generateHashCode(final CharSequence name) {
                return AsciiString.caseInsensitiveHashCode(name);
            }
        };
        CHARSEQUECE_CASE_SENSITIVE_HASH_CODE_GENERATOR = new HashCodeGenerator<CharSequence>() {
            @Override
            public int generateHashCode(final CharSequence name) {
                return name.hashCode();
            }
        };
        CHARSEQUENCE_FROM_OBJECT_CONVERTER = new DefaultTextValueTypeConverter();
        CHARSEQUENCE_TO_STRING_CONVERTER = new ConvertibleHeaders.TypeConverter<CharSequence, String>() {
            @Override
            public String toConvertedType(final CharSequence value) {
                return value.toString();
            }
            
            @Override
            public CharSequence toUnconvertedType(final String value) {
                return value;
            }
        };
        CHARSEQUENCE_IDENTITY_CONVERTER = new IdentityNameConverter<CharSequence>();
    }
    
    public static class DefaultTextValueTypeConverter implements Headers.ValueConverter<CharSequence>
    {
        @Override
        public CharSequence convertObject(final Object value) {
            if (value instanceof CharSequence) {
                return (CharSequence)value;
            }
            return value.toString();
        }
        
        @Override
        public CharSequence convertInt(final int value) {
            return String.valueOf(value);
        }
        
        @Override
        public CharSequence convertLong(final long value) {
            return String.valueOf(value);
        }
        
        @Override
        public CharSequence convertDouble(final double value) {
            return String.valueOf(value);
        }
        
        @Override
        public CharSequence convertChar(final char value) {
            return String.valueOf(value);
        }
        
        @Override
        public CharSequence convertBoolean(final boolean value) {
            return String.valueOf(value);
        }
        
        @Override
        public CharSequence convertFloat(final float value) {
            return String.valueOf(value);
        }
        
        @Override
        public boolean convertToBoolean(final CharSequence value) {
            return Boolean.parseBoolean(value.toString());
        }
        
        @Override
        public CharSequence convertByte(final byte value) {
            return String.valueOf(value);
        }
        
        @Override
        public byte convertToByte(final CharSequence value) {
            return Byte.valueOf(value.toString());
        }
        
        @Override
        public char convertToChar(final CharSequence value) {
            return value.charAt(0);
        }
        
        @Override
        public CharSequence convertShort(final short value) {
            return String.valueOf(value);
        }
        
        @Override
        public short convertToShort(final CharSequence value) {
            return Short.valueOf(value.toString());
        }
        
        @Override
        public int convertToInt(final CharSequence value) {
            return Integer.parseInt(value.toString());
        }
        
        @Override
        public long convertToLong(final CharSequence value) {
            return Long.parseLong(value.toString());
        }
        
        @Override
        public AsciiString convertTimeMillis(final long value) {
            return new AsciiString(String.valueOf(value));
        }
        
        @Override
        public long convertToTimeMillis(final CharSequence value) {
            try {
                return HeaderDateFormat.get().parse(value.toString());
            }
            catch (ParseException e) {
                PlatformDependent.throwException(e);
                return 0L;
            }
        }
        
        @Override
        public float convertToFloat(final CharSequence value) {
            return Float.valueOf(value.toString());
        }
        
        @Override
        public double convertToDouble(final CharSequence value) {
            return Double.valueOf(value.toString());
        }
    }
    
    private final class MultipleFieldsValueComposer implements ValuesComposer
    {
        @Override
        public TextHeaders add(final CharSequence name, final CharSequence value) {
            DefaultHeaders.this.add((UnconvertedType)name, (UnconvertedType)value);
            return DefaultTextHeaders.this;
        }
        
        @Override
        public TextHeaders add(final CharSequence name, final CharSequence... values) {
            DefaultHeaders.this.add((UnconvertedType)name, (UnconvertedType[])values);
            return DefaultTextHeaders.this;
        }
        
        @Override
        public TextHeaders add(final CharSequence name, final Iterable<? extends CharSequence> values) {
            DefaultHeaders.this.add((UnconvertedType)name, values);
            return DefaultTextHeaders.this;
        }
        
        @Override
        public TextHeaders addObject(final CharSequence name, final Iterable<?> values) {
            DefaultHeaders.this.addObject((UnconvertedType)name, values);
            return DefaultTextHeaders.this;
        }
        
        @Override
        public TextHeaders addObject(final CharSequence name, final Object... values) {
            DefaultHeaders.this.addObject((UnconvertedType)name, values);
            return DefaultTextHeaders.this;
        }
        
        @Override
        public TextHeaders set(final CharSequence name, final CharSequence... values) {
            DefaultHeaders.this.set((UnconvertedType)name, (UnconvertedType[])values);
            return DefaultTextHeaders.this;
        }
        
        @Override
        public TextHeaders set(final CharSequence name, final Iterable<? extends CharSequence> values) {
            DefaultHeaders.this.set((UnconvertedType)name, values);
            return DefaultTextHeaders.this;
        }
        
        @Override
        public TextHeaders setObject(final CharSequence name, final Object... values) {
            DefaultHeaders.this.setObject((UnconvertedType)name, values);
            return DefaultTextHeaders.this;
        }
        
        @Override
        public TextHeaders setObject(final CharSequence name, final Iterable<?> values) {
            DefaultHeaders.this.setObject((UnconvertedType)name, values);
            return DefaultTextHeaders.this;
        }
    }
    
    private final class SingleHeaderValuesComposer implements ValuesComposer
    {
        private final Headers.ValueConverter<CharSequence> valueConverter;
        private CsvValueEscaper<Object> objectEscaper;
        private CsvValueEscaper<CharSequence> charSequenceEscaper;
        
        private SingleHeaderValuesComposer() {
            this.valueConverter = ((DefaultHeaders<CharSequence>)DefaultTextHeaders.this).valueConverter();
        }
        
        private CsvValueEscaper<Object> objectEscaper() {
            if (this.objectEscaper == null) {
                this.objectEscaper = new CsvValueEscaper<Object>() {
                    @Override
                    public CharSequence escape(final Object value) {
                        return StringUtil.escapeCsv(SingleHeaderValuesComposer.this.valueConverter.convertObject(value));
                    }
                };
            }
            return this.objectEscaper;
        }
        
        private CsvValueEscaper<CharSequence> charSequenceEscaper() {
            if (this.charSequenceEscaper == null) {
                this.charSequenceEscaper = new CsvValueEscaper<CharSequence>() {
                    @Override
                    public CharSequence escape(final CharSequence value) {
                        return StringUtil.escapeCsv(value);
                    }
                };
            }
            return this.charSequenceEscaper;
        }
        
        @Override
        public TextHeaders add(final CharSequence name, final CharSequence value) {
            return this.addEscapedValue(name, StringUtil.escapeCsv(value));
        }
        
        @Override
        public TextHeaders add(final CharSequence name, final CharSequence... values) {
            return this.addEscapedValue(name, this.commaSeparate(this.charSequenceEscaper(), values));
        }
        
        @Override
        public TextHeaders add(final CharSequence name, final Iterable<? extends CharSequence> values) {
            return this.addEscapedValue(name, this.commaSeparate(this.charSequenceEscaper(), values));
        }
        
        @Override
        public TextHeaders addObject(final CharSequence name, final Iterable<?> values) {
            return this.addEscapedValue(name, this.commaSeparate(this.objectEscaper(), values));
        }
        
        @Override
        public TextHeaders addObject(final CharSequence name, final Object... values) {
            return this.addEscapedValue(name, this.commaSeparate(this.objectEscaper(), values));
        }
        
        @Override
        public TextHeaders set(final CharSequence name, final CharSequence... values) {
            DefaultHeaders.this.set((UnconvertedType)name, (UnconvertedType)this.commaSeparate(this.charSequenceEscaper(), values));
            return DefaultTextHeaders.this;
        }
        
        @Override
        public TextHeaders set(final CharSequence name, final Iterable<? extends CharSequence> values) {
            DefaultHeaders.this.set((UnconvertedType)name, (UnconvertedType)this.commaSeparate(this.charSequenceEscaper(), values));
            return DefaultTextHeaders.this;
        }
        
        @Override
        public TextHeaders setObject(final CharSequence name, final Object... values) {
            DefaultHeaders.this.set((UnconvertedType)name, (UnconvertedType)this.commaSeparate(this.objectEscaper(), values));
            return DefaultTextHeaders.this;
        }
        
        @Override
        public TextHeaders setObject(final CharSequence name, final Iterable<?> values) {
            DefaultHeaders.this.set((UnconvertedType)name, (UnconvertedType)this.commaSeparate(this.objectEscaper(), values));
            return DefaultTextHeaders.this;
        }
        
        private TextHeaders addEscapedValue(final CharSequence name, final CharSequence escapedValue) {
            final CharSequence currentValue = (CharSequence)DefaultHeaders.this.get(name);
            if (currentValue == null) {
                DefaultHeaders.this.add((UnconvertedType)name, (UnconvertedType)escapedValue);
            }
            else {
                DefaultHeaders.this.set((UnconvertedType)name, (UnconvertedType)this.commaSeparateEscapedValues(currentValue, escapedValue));
            }
            return DefaultTextHeaders.this;
        }
        
        private <T> CharSequence commaSeparate(final CsvValueEscaper<T> escaper, final T... values) {
            final StringBuilder sb = new StringBuilder(values.length * 10);
            if (values.length > 0) {
                final int end = values.length - 1;
                for (int i = 0; i < end; ++i) {
                    sb.append(escaper.escape(values[i])).append(',');
                }
                sb.append(escaper.escape(values[end]));
            }
            return sb;
        }
        
        private <T> CharSequence commaSeparate(final CsvValueEscaper<T> escaper, final Iterable<? extends T> values) {
            final StringBuilder sb = new StringBuilder();
            final Iterator<? extends T> iterator = values.iterator();
            if (iterator.hasNext()) {
                T next = (T)iterator.next();
                while (iterator.hasNext()) {
                    sb.append(escaper.escape(next)).append(',');
                    next = (T)iterator.next();
                }
                sb.append(escaper.escape(next));
            }
            return sb;
        }
        
        private CharSequence commaSeparateEscapedValues(final CharSequence currentValue, final CharSequence value) {
            return new StringBuilder(currentValue.length() + 1 + value.length()).append(currentValue).append(',').append(value);
        }
    }
    
    private interface CsvValueEscaper<T>
    {
        CharSequence escape(final T p0);
    }
    
    private interface ValuesComposer
    {
        TextHeaders add(final CharSequence p0, final CharSequence p1);
        
        TextHeaders add(final CharSequence p0, final CharSequence... p1);
        
        TextHeaders add(final CharSequence p0, final Iterable<? extends CharSequence> p1);
        
        TextHeaders addObject(final CharSequence p0, final Iterable<?> p1);
        
        TextHeaders addObject(final CharSequence p0, final Object... p1);
        
        TextHeaders set(final CharSequence p0, final CharSequence... p1);
        
        TextHeaders set(final CharSequence p0, final Iterable<? extends CharSequence> p1);
        
        TextHeaders setObject(final CharSequence p0, final Object... p1);
        
        TextHeaders setObject(final CharSequence p0, final Iterable<?> p1);
    }
}
