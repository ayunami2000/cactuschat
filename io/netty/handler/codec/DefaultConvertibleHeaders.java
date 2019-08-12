// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec;

import java.util.TreeSet;
import java.util.Set;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

public class DefaultConvertibleHeaders<UnconvertedType, ConvertedType> extends DefaultHeaders<UnconvertedType> implements ConvertibleHeaders<UnconvertedType, ConvertedType>
{
    private final TypeConverter<UnconvertedType, ConvertedType> typeConverter;
    
    public DefaultConvertibleHeaders(final Comparator<? super UnconvertedType> keyComparator, final Comparator<? super UnconvertedType> valueComparator, final HashCodeGenerator<UnconvertedType> hashCodeGenerator, final Headers.ValueConverter<UnconvertedType> valueConverter, final TypeConverter<UnconvertedType, ConvertedType> typeConverter) {
        super(keyComparator, valueComparator, hashCodeGenerator, valueConverter);
        this.typeConverter = typeConverter;
    }
    
    public DefaultConvertibleHeaders(final Comparator<? super UnconvertedType> keyComparator, final Comparator<? super UnconvertedType> valueComparator, final HashCodeGenerator<UnconvertedType> hashCodeGenerator, final Headers.ValueConverter<UnconvertedType> valueConverter, final TypeConverter<UnconvertedType, ConvertedType> typeConverter, final NameConverter<UnconvertedType> nameConverter) {
        super(keyComparator, valueComparator, hashCodeGenerator, valueConverter, nameConverter);
        this.typeConverter = typeConverter;
    }
    
    @Override
    public ConvertedType getAndConvert(final UnconvertedType name) {
        return this.getAndConvert(name, null);
    }
    
    @Override
    public ConvertedType getAndConvert(final UnconvertedType name, final ConvertedType defaultValue) {
        final UnconvertedType v = this.get(name);
        if (v == null) {
            return defaultValue;
        }
        return this.typeConverter.toConvertedType(v);
    }
    
    @Override
    public ConvertedType getAndRemoveAndConvert(final UnconvertedType name) {
        return this.getAndRemoveAndConvert(name, null);
    }
    
    @Override
    public ConvertedType getAndRemoveAndConvert(final UnconvertedType name, final ConvertedType defaultValue) {
        final UnconvertedType v = this.getAndRemove(name);
        if (v == null) {
            return defaultValue;
        }
        return this.typeConverter.toConvertedType(v);
    }
    
    @Override
    public List<ConvertedType> getAllAndConvert(final UnconvertedType name) {
        final List<UnconvertedType> all = this.getAll(name);
        final List<ConvertedType> allConverted = new ArrayList<ConvertedType>(all.size());
        for (int i = 0; i < all.size(); ++i) {
            allConverted.add(this.typeConverter.toConvertedType(all.get(i)));
        }
        return allConverted;
    }
    
    @Override
    public List<ConvertedType> getAllAndRemoveAndConvert(final UnconvertedType name) {
        final List<UnconvertedType> all = this.getAllAndRemove(name);
        final List<ConvertedType> allConverted = new ArrayList<ConvertedType>(all.size());
        for (int i = 0; i < all.size(); ++i) {
            allConverted.add(this.typeConverter.toConvertedType(all.get(i)));
        }
        return allConverted;
    }
    
    @Override
    public List<Map.Entry<ConvertedType, ConvertedType>> entriesConverted() {
        final List<Map.Entry<UnconvertedType, UnconvertedType>> entries = this.entries();
        final List<Map.Entry<ConvertedType, ConvertedType>> entriesConverted = new ArrayList<Map.Entry<ConvertedType, ConvertedType>>(entries.size());
        for (int i = 0; i < entries.size(); ++i) {
            entriesConverted.add(new ConvertedEntry(entries.get(i)));
        }
        return entriesConverted;
    }
    
    @Override
    public Iterator<Map.Entry<ConvertedType, ConvertedType>> iteratorConverted() {
        return new ConvertedIterator();
    }
    
    @Override
    public Set<ConvertedType> namesAndConvert(final Comparator<ConvertedType> comparator) {
        final Set<UnconvertedType> names = this.names();
        final Set<ConvertedType> namesConverted = new TreeSet<ConvertedType>(comparator);
        for (final UnconvertedType unconverted : names) {
            namesConverted.add(this.typeConverter.toConvertedType(unconverted));
        }
        return namesConverted;
    }
    
    private final class ConvertedIterator implements Iterator<Map.Entry<ConvertedType, ConvertedType>>
    {
        private final Iterator<Map.Entry<UnconvertedType, UnconvertedType>> iter;
        
        private ConvertedIterator() {
            this.iter = DefaultConvertibleHeaders.this.iterator();
        }
        
        @Override
        public boolean hasNext() {
            return this.iter.hasNext();
        }
        
        @Override
        public Map.Entry<ConvertedType, ConvertedType> next() {
            final Map.Entry<UnconvertedType, UnconvertedType> next = this.iter.next();
            return new ConvertedEntry(next);
        }
        
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
    
    private final class ConvertedEntry implements Map.Entry<ConvertedType, ConvertedType>
    {
        private final Map.Entry<UnconvertedType, UnconvertedType> entry;
        private ConvertedType name;
        private ConvertedType value;
        
        ConvertedEntry(final Map.Entry<UnconvertedType, UnconvertedType> entry) {
            this.entry = entry;
        }
        
        @Override
        public ConvertedType getKey() {
            if (this.name == null) {
                this.name = DefaultConvertibleHeaders.this.typeConverter.toConvertedType(this.entry.getKey());
            }
            return this.name;
        }
        
        @Override
        public ConvertedType getValue() {
            if (this.value == null) {
                this.value = DefaultConvertibleHeaders.this.typeConverter.toConvertedType(this.entry.getValue());
            }
            return this.value;
        }
        
        @Override
        public ConvertedType setValue(final ConvertedType value) {
            final ConvertedType old = this.getValue();
            this.entry.setValue(DefaultConvertibleHeaders.this.typeConverter.toUnconvertedType(value));
            return old;
        }
        
        @Override
        public String toString() {
            return this.entry.toString();
        }
    }
}
