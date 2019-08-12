// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec;

import java.text.ParseException;
import io.netty.util.internal.PlatformDependent;
import java.util.Comparator;

public class DefaultBinaryHeaders extends DefaultHeaders<AsciiString> implements BinaryHeaders
{
    private static final HashCodeGenerator<AsciiString> ASCII_HASH_CODE_GENERATOR;
    private static final Headers.ValueConverter<AsciiString> OBJECT_TO_ASCII;
    private static final NameConverter<AsciiString> ASCII_TO_LOWER_CONVERTER;
    private static final NameConverter<AsciiString> ASCII_IDENTITY_CONVERTER;
    
    public DefaultBinaryHeaders() {
        this(false);
    }
    
    public DefaultBinaryHeaders(final boolean forceKeyToLower) {
        super(AsciiString.CASE_INSENSITIVE_ORDER, AsciiString.CASE_INSENSITIVE_ORDER, DefaultBinaryHeaders.ASCII_HASH_CODE_GENERATOR, DefaultBinaryHeaders.OBJECT_TO_ASCII, forceKeyToLower ? DefaultBinaryHeaders.ASCII_TO_LOWER_CONVERTER : DefaultBinaryHeaders.ASCII_IDENTITY_CONVERTER);
    }
    
    @Override
    public BinaryHeaders add(final AsciiString name, final AsciiString value) {
        super.add(name, value);
        return this;
    }
    
    @Override
    public BinaryHeaders add(final AsciiString name, final Iterable<? extends AsciiString> values) {
        super.add(name, values);
        return this;
    }
    
    @Override
    public BinaryHeaders add(final AsciiString name, final AsciiString... values) {
        super.add(name, values);
        return this;
    }
    
    @Override
    public BinaryHeaders addObject(final AsciiString name, final Object value) {
        super.addObject(name, value);
        return this;
    }
    
    @Override
    public BinaryHeaders addObject(final AsciiString name, final Iterable<?> values) {
        super.addObject(name, values);
        return this;
    }
    
    @Override
    public BinaryHeaders addObject(final AsciiString name, final Object... values) {
        super.addObject(name, values);
        return this;
    }
    
    @Override
    public BinaryHeaders addBoolean(final AsciiString name, final boolean value) {
        super.addBoolean(name, value);
        return this;
    }
    
    @Override
    public BinaryHeaders addChar(final AsciiString name, final char value) {
        super.addChar(name, value);
        return this;
    }
    
    @Override
    public BinaryHeaders addByte(final AsciiString name, final byte value) {
        super.addByte(name, value);
        return this;
    }
    
    @Override
    public BinaryHeaders addShort(final AsciiString name, final short value) {
        super.addShort(name, value);
        return this;
    }
    
    @Override
    public BinaryHeaders addInt(final AsciiString name, final int value) {
        super.addInt(name, value);
        return this;
    }
    
    @Override
    public BinaryHeaders addLong(final AsciiString name, final long value) {
        super.addLong(name, value);
        return this;
    }
    
    @Override
    public BinaryHeaders addFloat(final AsciiString name, final float value) {
        super.addFloat(name, value);
        return this;
    }
    
    @Override
    public BinaryHeaders addDouble(final AsciiString name, final double value) {
        super.addDouble(name, value);
        return this;
    }
    
    @Override
    public BinaryHeaders addTimeMillis(final AsciiString name, final long value) {
        super.addTimeMillis(name, value);
        return this;
    }
    
    @Override
    public BinaryHeaders add(final BinaryHeaders headers) {
        super.add(headers);
        return this;
    }
    
    @Override
    public BinaryHeaders set(final AsciiString name, final AsciiString value) {
        super.set(name, value);
        return this;
    }
    
    @Override
    public BinaryHeaders set(final AsciiString name, final Iterable<? extends AsciiString> values) {
        super.set(name, values);
        return this;
    }
    
    @Override
    public BinaryHeaders set(final AsciiString name, final AsciiString... values) {
        super.set(name, values);
        return this;
    }
    
    @Override
    public BinaryHeaders setObject(final AsciiString name, final Object value) {
        super.setObject(name, value);
        return this;
    }
    
    @Override
    public BinaryHeaders setObject(final AsciiString name, final Iterable<?> values) {
        super.setObject(name, values);
        return this;
    }
    
    @Override
    public BinaryHeaders setObject(final AsciiString name, final Object... values) {
        super.setObject(name, values);
        return this;
    }
    
    @Override
    public BinaryHeaders setBoolean(final AsciiString name, final boolean value) {
        super.setBoolean(name, value);
        return this;
    }
    
    @Override
    public BinaryHeaders setChar(final AsciiString name, final char value) {
        super.setChar(name, value);
        return this;
    }
    
    @Override
    public BinaryHeaders setByte(final AsciiString name, final byte value) {
        super.setByte(name, value);
        return this;
    }
    
    @Override
    public BinaryHeaders setShort(final AsciiString name, final short value) {
        super.setShort(name, value);
        return this;
    }
    
    @Override
    public BinaryHeaders setInt(final AsciiString name, final int value) {
        super.setInt(name, value);
        return this;
    }
    
    @Override
    public BinaryHeaders setLong(final AsciiString name, final long value) {
        super.setLong(name, value);
        return this;
    }
    
    @Override
    public BinaryHeaders setFloat(final AsciiString name, final float value) {
        super.setFloat(name, value);
        return this;
    }
    
    @Override
    public BinaryHeaders setDouble(final AsciiString name, final double value) {
        super.setDouble(name, value);
        return this;
    }
    
    @Override
    public BinaryHeaders setTimeMillis(final AsciiString name, final long value) {
        super.setTimeMillis(name, value);
        return this;
    }
    
    @Override
    public BinaryHeaders set(final BinaryHeaders headers) {
        super.set(headers);
        return this;
    }
    
    @Override
    public BinaryHeaders setAll(final BinaryHeaders headers) {
        super.setAll(headers);
        return this;
    }
    
    @Override
    public BinaryHeaders clear() {
        super.clear();
        return this;
    }
    
    static {
        ASCII_HASH_CODE_GENERATOR = new HashCodeGenerator<AsciiString>() {
            @Override
            public int generateHashCode(final AsciiString name) {
                return AsciiString.caseInsensitiveHashCode(name);
            }
        };
        OBJECT_TO_ASCII = new Headers.ValueConverter<AsciiString>() {
            @Override
            public AsciiString convertObject(final Object value) {
                if (value instanceof AsciiString) {
                    return (AsciiString)value;
                }
                if (value instanceof CharSequence) {
                    return new AsciiString((CharSequence)value);
                }
                return new AsciiString(value.toString());
            }
            
            @Override
            public AsciiString convertInt(final int value) {
                return new AsciiString(String.valueOf(value));
            }
            
            @Override
            public AsciiString convertLong(final long value) {
                return new AsciiString(String.valueOf(value));
            }
            
            @Override
            public AsciiString convertDouble(final double value) {
                return new AsciiString(String.valueOf(value));
            }
            
            @Override
            public AsciiString convertChar(final char value) {
                return new AsciiString(String.valueOf(value));
            }
            
            @Override
            public AsciiString convertBoolean(final boolean value) {
                return new AsciiString(String.valueOf(value));
            }
            
            @Override
            public AsciiString convertFloat(final float value) {
                return new AsciiString(String.valueOf(value));
            }
            
            @Override
            public int convertToInt(final AsciiString value) {
                return value.parseInt();
            }
            
            @Override
            public long convertToLong(final AsciiString value) {
                return value.parseLong();
            }
            
            @Override
            public AsciiString convertTimeMillis(final long value) {
                return new AsciiString(String.valueOf(value));
            }
            
            @Override
            public long convertToTimeMillis(final AsciiString value) {
                try {
                    return HeaderDateFormat.get().parse(value.toString());
                }
                catch (ParseException e) {
                    PlatformDependent.throwException(e);
                    return 0L;
                }
            }
            
            @Override
            public double convertToDouble(final AsciiString value) {
                return value.parseDouble();
            }
            
            @Override
            public char convertToChar(final AsciiString value) {
                return value.charAt(0);
            }
            
            @Override
            public boolean convertToBoolean(final AsciiString value) {
                return value.byteAt(0) != 0;
            }
            
            @Override
            public float convertToFloat(final AsciiString value) {
                return value.parseFloat();
            }
            
            @Override
            public AsciiString convertShort(final short value) {
                return new AsciiString(String.valueOf(value));
            }
            
            @Override
            public short convertToShort(final AsciiString value) {
                return value.parseShort();
            }
            
            @Override
            public AsciiString convertByte(final byte value) {
                return new AsciiString(String.valueOf(value));
            }
            
            @Override
            public byte convertToByte(final AsciiString value) {
                return value.byteAt(0);
            }
        };
        ASCII_TO_LOWER_CONVERTER = new NameConverter<AsciiString>() {
            @Override
            public AsciiString convertName(final AsciiString name) {
                return name.toLowerCase();
            }
        };
        ASCII_IDENTITY_CONVERTER = new NameConverter<AsciiString>() {
            @Override
            public AsciiString convertName(final AsciiString name) {
                return name;
            }
        };
    }
}
