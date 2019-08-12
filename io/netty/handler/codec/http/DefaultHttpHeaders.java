// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http;

import io.netty.handler.codec.AsciiString;
import java.util.Calendar;
import java.util.Date;
import io.netty.handler.codec.TextHeaders;
import io.netty.handler.codec.Headers;
import io.netty.handler.codec.DefaultHeaders;
import io.netty.handler.codec.DefaultTextHeaders;

public class DefaultHttpHeaders extends DefaultTextHeaders implements HttpHeaders
{
    private static final int HIGHEST_INVALID_NAME_CHAR_MASK = -64;
    private static final int HIGHEST_INVALID_VALUE_CHAR_MASK = -16;
    private static final byte[] LOOKUP_TABLE;
    private static final HttpHeadersValidationConverter VALIDATE_OBJECT_CONVERTER;
    private static final HttpHeadersValidationConverter NO_VALIDATE_OBJECT_CONVERTER;
    private static final HttpHeadersNameConverter VALIDATE_NAME_CONVERTER;
    private static final HttpHeadersNameConverter NO_VALIDATE_NAME_CONVERTER;
    
    public DefaultHttpHeaders() {
        this(true);
    }
    
    public DefaultHttpHeaders(final boolean validate) {
        this(true, validate ? DefaultHttpHeaders.VALIDATE_NAME_CONVERTER : DefaultHttpHeaders.NO_VALIDATE_NAME_CONVERTER, false);
    }
    
    protected DefaultHttpHeaders(final boolean validate, final boolean singleHeaderFields) {
        this(true, validate ? DefaultHttpHeaders.VALIDATE_NAME_CONVERTER : DefaultHttpHeaders.NO_VALIDATE_NAME_CONVERTER, singleHeaderFields);
    }
    
    protected DefaultHttpHeaders(final boolean validate, final NameConverter<CharSequence> nameConverter, final boolean singleHeaderFields) {
        super(true, validate ? DefaultHttpHeaders.VALIDATE_OBJECT_CONVERTER : DefaultHttpHeaders.NO_VALIDATE_OBJECT_CONVERTER, nameConverter, singleHeaderFields);
    }
    
    @Override
    public HttpHeaders add(final CharSequence name, final CharSequence value) {
        super.add(name, value);
        return this;
    }
    
    @Override
    public HttpHeaders add(final CharSequence name, final Iterable<? extends CharSequence> values) {
        super.add(name, values);
        return this;
    }
    
    @Override
    public HttpHeaders add(final CharSequence name, final CharSequence... values) {
        super.add(name, values);
        return this;
    }
    
    @Override
    public HttpHeaders addObject(final CharSequence name, final Object value) {
        super.addObject(name, value);
        return this;
    }
    
    @Override
    public HttpHeaders addObject(final CharSequence name, final Iterable<?> values) {
        super.addObject(name, values);
        return this;
    }
    
    @Override
    public HttpHeaders addObject(final CharSequence name, final Object... values) {
        super.addObject(name, values);
        return this;
    }
    
    @Override
    public HttpHeaders addBoolean(final CharSequence name, final boolean value) {
        super.addBoolean(name, value);
        return this;
    }
    
    @Override
    public HttpHeaders addChar(final CharSequence name, final char value) {
        super.addChar(name, value);
        return this;
    }
    
    @Override
    public HttpHeaders addByte(final CharSequence name, final byte value) {
        super.addByte(name, value);
        return this;
    }
    
    @Override
    public HttpHeaders addShort(final CharSequence name, final short value) {
        super.addShort(name, value);
        return this;
    }
    
    @Override
    public HttpHeaders addInt(final CharSequence name, final int value) {
        super.addInt(name, value);
        return this;
    }
    
    @Override
    public HttpHeaders addLong(final CharSequence name, final long value) {
        super.addLong(name, value);
        return this;
    }
    
    @Override
    public HttpHeaders addFloat(final CharSequence name, final float value) {
        super.addFloat(name, value);
        return this;
    }
    
    @Override
    public HttpHeaders addDouble(final CharSequence name, final double value) {
        super.addDouble(name, value);
        return this;
    }
    
    @Override
    public HttpHeaders addTimeMillis(final CharSequence name, final long value) {
        super.addTimeMillis(name, value);
        return this;
    }
    
    @Override
    public HttpHeaders add(final TextHeaders headers) {
        super.add(headers);
        return this;
    }
    
    @Override
    public HttpHeaders set(final CharSequence name, final CharSequence value) {
        super.set(name, value);
        return this;
    }
    
    @Override
    public HttpHeaders set(final CharSequence name, final Iterable<? extends CharSequence> values) {
        super.set(name, values);
        return this;
    }
    
    @Override
    public HttpHeaders set(final CharSequence name, final CharSequence... values) {
        super.set(name, values);
        return this;
    }
    
    @Override
    public HttpHeaders setObject(final CharSequence name, final Object value) {
        super.setObject(name, value);
        return this;
    }
    
    @Override
    public HttpHeaders setObject(final CharSequence name, final Iterable<?> values) {
        super.setObject(name, values);
        return this;
    }
    
    @Override
    public HttpHeaders setObject(final CharSequence name, final Object... values) {
        super.setObject(name, values);
        return this;
    }
    
    @Override
    public HttpHeaders setBoolean(final CharSequence name, final boolean value) {
        super.setBoolean(name, value);
        return this;
    }
    
    @Override
    public HttpHeaders setChar(final CharSequence name, final char value) {
        super.setChar(name, value);
        return this;
    }
    
    @Override
    public HttpHeaders setByte(final CharSequence name, final byte value) {
        super.setByte(name, value);
        return this;
    }
    
    @Override
    public HttpHeaders setShort(final CharSequence name, final short value) {
        super.setShort(name, value);
        return this;
    }
    
    @Override
    public HttpHeaders setInt(final CharSequence name, final int value) {
        super.setInt(name, value);
        return this;
    }
    
    @Override
    public HttpHeaders setLong(final CharSequence name, final long value) {
        super.setLong(name, value);
        return this;
    }
    
    @Override
    public HttpHeaders setFloat(final CharSequence name, final float value) {
        super.setFloat(name, value);
        return this;
    }
    
    @Override
    public HttpHeaders setDouble(final CharSequence name, final double value) {
        super.setDouble(name, value);
        return this;
    }
    
    @Override
    public HttpHeaders setTimeMillis(final CharSequence name, final long value) {
        super.setTimeMillis(name, value);
        return this;
    }
    
    @Override
    public HttpHeaders set(final TextHeaders headers) {
        super.set(headers);
        return this;
    }
    
    @Override
    public HttpHeaders setAll(final TextHeaders headers) {
        super.setAll(headers);
        return this;
    }
    
    @Override
    public HttpHeaders clear() {
        super.clear();
        return this;
    }
    
    static {
        (LOOKUP_TABLE = new byte[64])[9] = -1;
        DefaultHttpHeaders.LOOKUP_TABLE[10] = -1;
        DefaultHttpHeaders.LOOKUP_TABLE[11] = -1;
        DefaultHttpHeaders.LOOKUP_TABLE[12] = -1;
        DefaultHttpHeaders.LOOKUP_TABLE[32] = -1;
        DefaultHttpHeaders.LOOKUP_TABLE[44] = -1;
        DefaultHttpHeaders.LOOKUP_TABLE[58] = -1;
        DefaultHttpHeaders.LOOKUP_TABLE[59] = -1;
        DefaultHttpHeaders.LOOKUP_TABLE[61] = -1;
        VALIDATE_OBJECT_CONVERTER = new HttpHeadersValidationConverter(true);
        NO_VALIDATE_OBJECT_CONVERTER = new HttpHeadersValidationConverter(false);
        VALIDATE_NAME_CONVERTER = new HttpHeadersNameConverter(true);
        NO_VALIDATE_NAME_CONVERTER = new HttpHeadersNameConverter(false);
    }
    
    private static final class HttpHeadersValidationConverter extends DefaultTextValueTypeConverter
    {
        private final boolean validate;
        
        HttpHeadersValidationConverter(final boolean validate) {
            this.validate = validate;
        }
        
        @Override
        public CharSequence convertObject(final Object value) {
            if (value == null) {
                throw new NullPointerException("value");
            }
            CharSequence seq;
            if (value instanceof CharSequence) {
                seq = (CharSequence)value;
            }
            else if (value instanceof Number) {
                seq = value.toString();
            }
            else if (value instanceof Date) {
                seq = HttpHeaderDateFormat.get().format((Date)value);
            }
            else if (value instanceof Calendar) {
                seq = HttpHeaderDateFormat.get().format(((Calendar)value).getTime());
            }
            else {
                seq = value.toString();
            }
            if (this.validate) {
                if (value instanceof AsciiString) {
                    validateValue((AsciiString)seq);
                }
                else {
                    validateValue(seq);
                }
            }
            return seq;
        }
        
        private static void validateValue(final AsciiString seq) {
            int state = 0;
            final int start = seq.arrayOffset();
            final int end = start + seq.length();
            final byte[] array = seq.array();
            for (int index = start; index < end; ++index) {
                state = validateValueChar(seq, state, (char)(array[index] & 0xFF));
            }
            if (state != 0) {
                throw new IllegalArgumentException("a header value must not end with '\\r' or '\\n':" + (Object)seq);
            }
        }
        
        private static void validateValue(final CharSequence seq) {
            int state = 0;
            for (int index = 0; index < seq.length(); ++index) {
                state = validateValueChar(seq, state, seq.charAt(index));
            }
            if (state != 0) {
                throw new IllegalArgumentException("a header value must not end with '\\r' or '\\n':" + (Object)seq);
            }
        }
        
        private static int validateValueChar(final CharSequence seq, int state, final char character) {
            if ((character & 0xFFFFFFF0) == 0x0) {
                switch (character) {
                    case '\0': {
                        throw new IllegalArgumentException("a header value contains a prohibited character '\u0000': " + (Object)seq);
                    }
                    case '\u000b': {
                        throw new IllegalArgumentException("a header value contains a prohibited character '\\v': " + (Object)seq);
                    }
                    case '\f': {
                        throw new IllegalArgumentException("a header value contains a prohibited character '\\f': " + (Object)seq);
                    }
                }
            }
            Label_0300: {
                switch (state) {
                    case 0: {
                        switch (character) {
                            case '\r': {
                                state = 1;
                                break;
                            }
                            case '\n': {
                                state = 2;
                                break;
                            }
                        }
                        break;
                    }
                    case 1: {
                        switch (character) {
                            case '\n': {
                                state = 2;
                                break Label_0300;
                            }
                            default: {
                                throw new IllegalArgumentException("only '\\n' is allowed after '\\r': " + (Object)seq);
                            }
                        }
                        break;
                    }
                    case 2: {
                        switch (character) {
                            case '\t':
                            case ' ': {
                                state = 0;
                                break Label_0300;
                            }
                            default: {
                                throw new IllegalArgumentException("only ' ' and '\\t' are allowed after '\\n': " + (Object)seq);
                            }
                        }
                        break;
                    }
                }
            }
            return state;
        }
    }
    
    static class HttpHeadersNameConverter implements NameConverter<CharSequence>
    {
        protected final boolean validate;
        
        HttpHeadersNameConverter(final boolean validate) {
            this.validate = validate;
        }
        
        @Override
        public CharSequence convertName(final CharSequence name) {
            if (this.validate) {
                if (name instanceof AsciiString) {
                    validateName((AsciiString)name);
                }
                else {
                    validateName(name);
                }
            }
            return name;
        }
        
        private static void validateName(final AsciiString name) {
            final int start = name.arrayOffset();
            final int end = start + name.length();
            final byte[] array = name.array();
            for (int index = start; index < end; ++index) {
                final byte b = array[index];
                if (b < 0) {
                    throw new IllegalArgumentException("a header name cannot contain non-ASCII characters: " + (Object)name);
                }
                validateNameChar(name, b);
            }
        }
        
        private static void validateName(final CharSequence name) {
            for (int index = 0; index < name.length(); ++index) {
                final char character = name.charAt(index);
                if (character > '\u007f') {
                    throw new IllegalArgumentException("a header name cannot contain non-ASCII characters: " + (Object)name);
                }
                validateNameChar(name, character);
            }
        }
        
        private static void validateNameChar(final CharSequence name, final int character) {
            if ((character & 0xFFFFFFC0) == 0x0 && DefaultHttpHeaders.LOOKUP_TABLE[character] != 0) {
                throw new IllegalArgumentException("a header name cannot contain the following prohibited characters: =,;: \\t\\r\\n\\v\\f: " + (Object)name);
            }
        }
    }
}
