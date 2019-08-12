// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec;

import java.util.Date;
import java.text.ParseException;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.text.DateFormat;
import io.netty.util.concurrent.FastThreadLocal;
import java.text.ParsePosition;
import java.util.NoSuchElementException;
import java.util.Collections;
import java.util.Collection;
import io.netty.util.internal.PlatformDependent;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Set;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.collection.IntObjectHashMap;
import java.util.Comparator;
import io.netty.util.collection.IntObjectMap;

public class DefaultHeaders<T> implements Headers<T>
{
    private static final int HASH_CODE_PRIME = 31;
    private static final int DEFAULT_BUCKET_SIZE = 17;
    private static final int DEFAULT_MAP_SIZE = 4;
    private static final NameConverter<Object> DEFAULT_NAME_CONVERTER;
    private final IntObjectMap<HeaderEntry> entries;
    private final IntObjectMap<HeaderEntry> tailEntries;
    private final HeaderEntry head;
    private final Comparator<? super T> keyComparator;
    private final Comparator<? super T> valueComparator;
    private final HashCodeGenerator<T> hashCodeGenerator;
    private final ValueConverter<T> valueConverter;
    private final NameConverter<T> nameConverter;
    private final int bucketSize;
    int size;
    
    public DefaultHeaders(final Comparator<? super T> keyComparator, final Comparator<? super T> valueComparator, final HashCodeGenerator<T> hashCodeGenerator, final ValueConverter<T> typeConverter) {
        this((Comparator<? super Object>)keyComparator, (Comparator<? super Object>)valueComparator, (HashCodeGenerator<Object>)hashCodeGenerator, (ValueConverter<Object>)typeConverter, DefaultHeaders.DEFAULT_NAME_CONVERTER);
    }
    
    public DefaultHeaders(final Comparator<? super T> keyComparator, final Comparator<? super T> valueComparator, final HashCodeGenerator<T> hashCodeGenerator, final ValueConverter<T> typeConverter, final NameConverter<T> nameConverter) {
        this(keyComparator, valueComparator, hashCodeGenerator, typeConverter, nameConverter, 17, 4);
    }
    
    public DefaultHeaders(final Comparator<? super T> keyComparator, final Comparator<? super T> valueComparator, final HashCodeGenerator<T> hashCodeGenerator, final ValueConverter<T> valueConverter, final NameConverter<T> nameConverter, final int bucketSize, final int initialMapSize) {
        if (keyComparator == null) {
            throw new NullPointerException("keyComparator");
        }
        if (valueComparator == null) {
            throw new NullPointerException("valueComparator");
        }
        if (hashCodeGenerator == null) {
            throw new NullPointerException("hashCodeGenerator");
        }
        if (valueConverter == null) {
            throw new NullPointerException("valueConverter");
        }
        if (nameConverter == null) {
            throw new NullPointerException("nameConverter");
        }
        if (bucketSize < 1) {
            throw new IllegalArgumentException("bucketSize must be a positive integer");
        }
        this.head = new HeaderEntry();
        final HeaderEntry head = this.head;
        final HeaderEntry head2 = this.head;
        final HeaderEntry head3 = this.head;
        head2.after = head3;
        head.before = head3;
        this.keyComparator = keyComparator;
        this.valueComparator = valueComparator;
        this.hashCodeGenerator = hashCodeGenerator;
        this.valueConverter = valueConverter;
        this.nameConverter = nameConverter;
        this.bucketSize = bucketSize;
        this.entries = new IntObjectHashMap<HeaderEntry>(initialMapSize);
        this.tailEntries = new IntObjectHashMap<HeaderEntry>(initialMapSize);
    }
    
    @Override
    public T get(final T name) {
        ObjectUtil.checkNotNull(name, "name");
        final int h = this.hashCodeGenerator.generateHashCode(name);
        final int i = this.index(h);
        for (HeaderEntry e = this.entries.get(i); e != null; e = e.next) {
            if (e.hash == h && this.keyComparator.compare((Object)e.name, (Object)name) == 0) {
                return e.value;
            }
        }
        return null;
    }
    
    @Override
    public T get(final T name, final T defaultValue) {
        final T value = this.get(name);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }
    
    @Override
    public T getAndRemove(final T name) {
        ObjectUtil.checkNotNull(name, "name");
        final int h = this.hashCodeGenerator.generateHashCode(name);
        final int i = this.index(h);
        HeaderEntry e = this.entries.get(i);
        if (e == null) {
            return null;
        }
        T value = null;
        while (e.hash == h && this.keyComparator.compare((Object)e.name, (Object)name) == 0) {
            if (value == null) {
                value = e.value;
            }
            e.remove();
            final HeaderEntry next = e.next;
            if (next == null) {
                this.entries.remove(i);
                this.tailEntries.remove(i);
                return value;
            }
            this.entries.put(i, next);
            e = next;
        }
        while (true) {
            final HeaderEntry next = e.next;
            if (next == null) {
                break;
            }
            if (next.hash == h && this.keyComparator.compare((Object)e.name, (Object)name) == 0) {
                if (value == null) {
                    value = next.value;
                }
                e.next = next.next;
                if (e.next == null) {
                    this.tailEntries.put(i, e);
                }
                next.remove();
            }
            else {
                e = next;
            }
        }
        return value;
    }
    
    @Override
    public T getAndRemove(final T name, final T defaultValue) {
        final T value = this.getAndRemove(name);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }
    
    @Override
    public List<T> getAll(final T name) {
        ObjectUtil.checkNotNull(name, "name");
        final List<T> values = new ArrayList<T>(4);
        final int h = this.hashCodeGenerator.generateHashCode(name);
        final int i = this.index(h);
        for (HeaderEntry e = this.entries.get(i); e != null; e = e.next) {
            if (e.hash == h && this.keyComparator.compare((Object)e.name, (Object)name) == 0) {
                values.add(e.value);
            }
        }
        return values;
    }
    
    @Override
    public List<T> getAllAndRemove(final T name) {
        ObjectUtil.checkNotNull(name, "name");
        final int h = this.hashCodeGenerator.generateHashCode(name);
        final int i = this.index(h);
        HeaderEntry e = this.entries.get(i);
        if (e == null) {
            return null;
        }
        final List<T> values = new ArrayList<T>(4);
        while (e.hash == h && this.keyComparator.compare((Object)e.name, (Object)name) == 0) {
            values.add(e.value);
            e.remove();
            final HeaderEntry next = e.next;
            if (next == null) {
                this.entries.remove(i);
                this.tailEntries.remove(i);
                return values;
            }
            this.entries.put(i, next);
            e = next;
        }
        while (true) {
            final HeaderEntry next = e.next;
            if (next == null) {
                break;
            }
            if (next.hash == h && this.keyComparator.compare((Object)next.name, (Object)name) == 0) {
                values.add(next.value);
                e.next = next.next;
                if (e.next == null) {
                    this.tailEntries.put(i, e);
                }
                next.remove();
            }
            else {
                e = next;
            }
        }
        return values;
    }
    
    @Override
    public List<Map.Entry<T, T>> entries() {
        final int size = this.size();
        final List<Map.Entry<T, T>> localEntries = new ArrayList<Map.Entry<T, T>>(size);
        for (HeaderEntry e = this.head.after; e != this.head; e = e.after) {
            localEntries.add(e);
        }
        assert size == localEntries.size();
        return localEntries;
    }
    
    @Override
    public boolean contains(final T name) {
        return this.get(name) != null;
    }
    
    @Override
    public boolean contains(final T name, final T value) {
        return this.contains(name, value, this.keyComparator, this.valueComparator);
    }
    
    @Override
    public boolean containsObject(final T name, final Object value) {
        return this.contains(name, this.valueConverter.convertObject(ObjectUtil.checkNotNull(value, "value")));
    }
    
    @Override
    public boolean containsBoolean(final T name, final boolean value) {
        return this.contains(name, this.valueConverter.convertBoolean(ObjectUtil.checkNotNull(value, "value")));
    }
    
    @Override
    public boolean containsByte(final T name, final byte value) {
        return this.contains(name, this.valueConverter.convertByte(ObjectUtil.checkNotNull(value, "value")));
    }
    
    @Override
    public boolean containsChar(final T name, final char value) {
        return this.contains(name, this.valueConverter.convertChar(ObjectUtil.checkNotNull(value, "value")));
    }
    
    @Override
    public boolean containsShort(final T name, final short value) {
        return this.contains(name, this.valueConverter.convertShort(ObjectUtil.checkNotNull(value, "value")));
    }
    
    @Override
    public boolean containsInt(final T name, final int value) {
        return this.contains(name, this.valueConverter.convertInt(ObjectUtil.checkNotNull(value, "value")));
    }
    
    @Override
    public boolean containsLong(final T name, final long value) {
        return this.contains(name, this.valueConverter.convertLong(ObjectUtil.checkNotNull(value, "value")));
    }
    
    @Override
    public boolean containsFloat(final T name, final float value) {
        return this.contains(name, this.valueConverter.convertFloat(ObjectUtil.checkNotNull(value, "value")));
    }
    
    @Override
    public boolean containsDouble(final T name, final double value) {
        return this.contains(name, this.valueConverter.convertDouble(ObjectUtil.checkNotNull(value, "value")));
    }
    
    @Override
    public boolean containsTimeMillis(final T name, final long value) {
        return this.contains(name, this.valueConverter.convertTimeMillis(ObjectUtil.checkNotNull(value, "value")));
    }
    
    @Override
    public boolean contains(final T name, final T value, final Comparator<? super T> comparator) {
        return this.contains(name, value, comparator, comparator);
    }
    
    @Override
    public boolean contains(final T name, final T value, final Comparator<? super T> keyComparator, final Comparator<? super T> valueComparator) {
        ObjectUtil.checkNotNull(name, "name");
        ObjectUtil.checkNotNull(value, "value");
        ObjectUtil.checkNotNull(keyComparator, "keyComparator");
        ObjectUtil.checkNotNull(valueComparator, "valueComparator");
        final int h = this.hashCodeGenerator.generateHashCode(name);
        final int i = this.index(h);
        for (HeaderEntry e = this.entries.get(i); e != null; e = e.next) {
            if (e.hash == h && keyComparator.compare((Object)e.name, (Object)name) == 0 && valueComparator.compare((Object)e.value, (Object)value) == 0) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean containsObject(final T name, final Object value, final Comparator<? super T> comparator) {
        return this.containsObject(name, value, comparator, comparator);
    }
    
    @Override
    public boolean containsObject(final T name, final Object value, final Comparator<? super T> keyComparator, final Comparator<? super T> valueComparator) {
        return this.contains(name, this.valueConverter.convertObject(ObjectUtil.checkNotNull(value, "value")), keyComparator, valueComparator);
    }
    
    @Override
    public int size() {
        return this.size;
    }
    
    @Override
    public boolean isEmpty() {
        return this.head == this.head.after;
    }
    
    @Override
    public Set<T> names() {
        final Set<T> names = new TreeSet<T>(this.keyComparator);
        for (HeaderEntry e = this.head.after; e != this.head; e = e.after) {
            names.add(e.name);
        }
        return names;
    }
    
    @Override
    public List<T> namesList() {
        final List<T> names = new ArrayList<T>(this.size());
        for (HeaderEntry e = this.head.after; e != this.head; e = e.after) {
            names.add(e.name);
        }
        return names;
    }
    
    @Override
    public Headers<T> add(T name, final T value) {
        name = this.convertName(name);
        ObjectUtil.checkNotNull(value, "value");
        final int h = this.hashCodeGenerator.generateHashCode(name);
        final int i = this.index(h);
        this.add0(h, i, name, value);
        return this;
    }
    
    @Override
    public Headers<T> add(T name, final Iterable<? extends T> values) {
        name = this.convertName(name);
        ObjectUtil.checkNotNull(values, "values");
        final int h = this.hashCodeGenerator.generateHashCode(name);
        final int i = this.index(h);
        for (final T v : values) {
            if (v == null) {
                break;
            }
            this.add0(h, i, name, v);
        }
        return this;
    }
    
    @Override
    public Headers<T> add(T name, final T... values) {
        name = this.convertName(name);
        ObjectUtil.checkNotNull(values, "values");
        final int h = this.hashCodeGenerator.generateHashCode(name);
        final int i = this.index(h);
        for (final T v : values) {
            if (v == null) {
                break;
            }
            this.add0(h, i, name, v);
        }
        return this;
    }
    
    @Override
    public Headers<T> addObject(final T name, final Object value) {
        return this.add(name, this.valueConverter.convertObject(ObjectUtil.checkNotNull(value, "value")));
    }
    
    @Override
    public Headers<T> addObject(T name, final Iterable<?> values) {
        name = this.convertName(name);
        ObjectUtil.checkNotNull(values, "values");
        final int h = this.hashCodeGenerator.generateHashCode(name);
        final int i = this.index(h);
        for (final Object o : values) {
            if (o == null) {
                break;
            }
            final T converted = this.valueConverter.convertObject(o);
            ObjectUtil.checkNotNull(converted, "converted");
            this.add0(h, i, name, converted);
        }
        return this;
    }
    
    @Override
    public Headers<T> addObject(T name, final Object... values) {
        name = this.convertName(name);
        ObjectUtil.checkNotNull(values, "values");
        final int h = this.hashCodeGenerator.generateHashCode(name);
        final int i = this.index(h);
        for (final Object o : values) {
            if (o == null) {
                break;
            }
            final T converted = this.valueConverter.convertObject(o);
            ObjectUtil.checkNotNull(converted, "converted");
            this.add0(h, i, name, converted);
        }
        return this;
    }
    
    @Override
    public Headers<T> addInt(final T name, final int value) {
        return this.add(name, this.valueConverter.convertInt(value));
    }
    
    @Override
    public Headers<T> addLong(final T name, final long value) {
        return this.add(name, this.valueConverter.convertLong(value));
    }
    
    @Override
    public Headers<T> addDouble(final T name, final double value) {
        return this.add(name, this.valueConverter.convertDouble(value));
    }
    
    @Override
    public Headers<T> addTimeMillis(final T name, final long value) {
        return this.add(name, this.valueConverter.convertTimeMillis(value));
    }
    
    @Override
    public Headers<T> addChar(final T name, final char value) {
        return this.add(name, this.valueConverter.convertChar(value));
    }
    
    @Override
    public Headers<T> addBoolean(final T name, final boolean value) {
        return this.add(name, this.valueConverter.convertBoolean(value));
    }
    
    @Override
    public Headers<T> addFloat(final T name, final float value) {
        return this.add(name, this.valueConverter.convertFloat(value));
    }
    
    @Override
    public Headers<T> addByte(final T name, final byte value) {
        return this.add(name, this.valueConverter.convertByte(value));
    }
    
    @Override
    public Headers<T> addShort(final T name, final short value) {
        return this.add(name, this.valueConverter.convertShort(value));
    }
    
    @Override
    public Headers<T> add(final Headers<T> headers) {
        ObjectUtil.checkNotNull(headers, "headers");
        this.add0(headers);
        return this;
    }
    
    @Override
    public Headers<T> set(T name, final T value) {
        name = this.convertName(name);
        ObjectUtil.checkNotNull(value, "value");
        final int h = this.hashCodeGenerator.generateHashCode(name);
        final int i = this.index(h);
        this.remove0(h, i, name);
        this.add0(h, i, name, value);
        return this;
    }
    
    @Override
    public Headers<T> set(T name, final Iterable<? extends T> values) {
        name = this.convertName(name);
        ObjectUtil.checkNotNull(values, "values");
        final int h = this.hashCodeGenerator.generateHashCode(name);
        final int i = this.index(h);
        this.remove0(h, i, name);
        for (final T v : values) {
            if (v == null) {
                break;
            }
            this.add0(h, i, name, v);
        }
        return this;
    }
    
    @Override
    public Headers<T> set(T name, final T... values) {
        name = this.convertName(name);
        ObjectUtil.checkNotNull(values, "values");
        final int h = this.hashCodeGenerator.generateHashCode(name);
        final int i = this.index(h);
        this.remove0(h, i, name);
        for (final T v : values) {
            if (v == null) {
                break;
            }
            this.add0(h, i, name, v);
        }
        return this;
    }
    
    @Override
    public Headers<T> setObject(final T name, final Object value) {
        return this.set(name, this.valueConverter.convertObject(ObjectUtil.checkNotNull(value, "value")));
    }
    
    @Override
    public Headers<T> setObject(T name, final Iterable<?> values) {
        name = this.convertName(name);
        ObjectUtil.checkNotNull(values, "values");
        final int h = this.hashCodeGenerator.generateHashCode(name);
        final int i = this.index(h);
        this.remove0(h, i, name);
        for (final Object o : values) {
            if (o == null) {
                break;
            }
            final T converted = this.valueConverter.convertObject(o);
            ObjectUtil.checkNotNull(converted, "converted");
            this.add0(h, i, name, converted);
        }
        return this;
    }
    
    @Override
    public Headers<T> setObject(T name, final Object... values) {
        name = this.convertName(name);
        ObjectUtil.checkNotNull(values, "values");
        final int h = this.hashCodeGenerator.generateHashCode(name);
        final int i = this.index(h);
        this.remove0(h, i, name);
        for (final Object o : values) {
            if (o == null) {
                break;
            }
            final T converted = this.valueConverter.convertObject(o);
            ObjectUtil.checkNotNull(converted, "converted");
            this.add0(h, i, name, converted);
        }
        return this;
    }
    
    @Override
    public Headers<T> setInt(final T name, final int value) {
        return this.set(name, this.valueConverter.convertInt(value));
    }
    
    @Override
    public Headers<T> setLong(final T name, final long value) {
        return this.set(name, this.valueConverter.convertLong(value));
    }
    
    @Override
    public Headers<T> setDouble(final T name, final double value) {
        return this.set(name, this.valueConverter.convertDouble(value));
    }
    
    @Override
    public Headers<T> setTimeMillis(final T name, final long value) {
        return this.set(name, this.valueConverter.convertTimeMillis(value));
    }
    
    @Override
    public Headers<T> setFloat(final T name, final float value) {
        return this.set(name, this.valueConverter.convertFloat(value));
    }
    
    @Override
    public Headers<T> setChar(final T name, final char value) {
        return this.set(name, this.valueConverter.convertChar(value));
    }
    
    @Override
    public Headers<T> setBoolean(final T name, final boolean value) {
        return this.set(name, this.valueConverter.convertBoolean(value));
    }
    
    @Override
    public Headers<T> setByte(final T name, final byte value) {
        return this.set(name, this.valueConverter.convertByte(value));
    }
    
    @Override
    public Headers<T> setShort(final T name, final short value) {
        return this.set(name, this.valueConverter.convertShort(value));
    }
    
    @Override
    public Headers<T> set(final Headers<T> headers) {
        ObjectUtil.checkNotNull(headers, "headers");
        this.clear();
        this.add0(headers);
        return this;
    }
    
    @Override
    public Headers<T> setAll(final Headers<T> headers) {
        ObjectUtil.checkNotNull(headers, "headers");
        if (headers instanceof DefaultHeaders) {
            final DefaultHeaders<T> m = (DefaultHeaders)headers;
            for (HeaderEntry e = m.head.after; e != m.head; e = e.after) {
                this.set(e.name, e.value);
            }
        }
        else {
            try {
                headers.forEachEntry(this.setAllVisitor());
            }
            catch (Exception ex) {
                PlatformDependent.throwException(ex);
            }
        }
        return this;
    }
    
    @Override
    public boolean remove(final T name) {
        ObjectUtil.checkNotNull(name, "name");
        final int h = this.hashCodeGenerator.generateHashCode(name);
        final int i = this.index(h);
        return this.remove0(h, i, name);
    }
    
    @Override
    public Headers<T> clear() {
        this.entries.clear();
        this.tailEntries.clear();
        final HeaderEntry head = this.head;
        final HeaderEntry head2 = this.head;
        final HeaderEntry head3 = this.head;
        head2.after = head3;
        head.before = head3;
        this.size = 0;
        return this;
    }
    
    @Override
    public Iterator<Map.Entry<T, T>> iterator() {
        return new KeyValueHeaderIterator();
    }
    
    @Override
    public Map.Entry<T, T> forEachEntry(final EntryVisitor<T> visitor) throws Exception {
        for (HeaderEntry e = this.head.after; e != this.head; e = e.after) {
            if (!visitor.visit(e)) {
                return e;
            }
        }
        return null;
    }
    
    @Override
    public T forEachName(final NameVisitor<T> visitor) throws Exception {
        for (HeaderEntry e = this.head.after; e != this.head; e = e.after) {
            if (!visitor.visit(e.name)) {
                return e.name;
            }
        }
        return null;
    }
    
    @Override
    public Boolean getBoolean(final T name) {
        final T v = this.get(name);
        if (v == null) {
            return null;
        }
        try {
            return this.valueConverter.convertToBoolean(v);
        }
        catch (Throwable ignored) {
            return null;
        }
    }
    
    @Override
    public boolean getBoolean(final T name, final boolean defaultValue) {
        final Boolean v = this.getBoolean(name);
        return (v == null) ? defaultValue : v;
    }
    
    @Override
    public Byte getByte(final T name) {
        final T v = this.get(name);
        if (v == null) {
            return null;
        }
        try {
            return this.valueConverter.convertToByte(v);
        }
        catch (Throwable ignored) {
            return null;
        }
    }
    
    @Override
    public byte getByte(final T name, final byte defaultValue) {
        final Byte v = this.getByte(name);
        return (v == null) ? defaultValue : v;
    }
    
    @Override
    public Character getChar(final T name) {
        final T v = this.get(name);
        if (v == null) {
            return null;
        }
        try {
            return this.valueConverter.convertToChar(v);
        }
        catch (Throwable ignored) {
            return null;
        }
    }
    
    @Override
    public char getChar(final T name, final char defaultValue) {
        final Character v = this.getChar(name);
        return (v == null) ? defaultValue : v;
    }
    
    @Override
    public Short getShort(final T name) {
        final T v = this.get(name);
        if (v == null) {
            return null;
        }
        try {
            return this.valueConverter.convertToShort(v);
        }
        catch (Throwable ignored) {
            return null;
        }
    }
    
    @Override
    public short getInt(final T name, final short defaultValue) {
        final Short v = this.getShort(name);
        return (v == null) ? defaultValue : v;
    }
    
    @Override
    public Integer getInt(final T name) {
        final T v = this.get(name);
        if (v == null) {
            return null;
        }
        try {
            return this.valueConverter.convertToInt(v);
        }
        catch (Throwable ignored) {
            return null;
        }
    }
    
    @Override
    public int getInt(final T name, final int defaultValue) {
        final Integer v = this.getInt(name);
        return (v == null) ? defaultValue : v;
    }
    
    @Override
    public Long getLong(final T name) {
        final T v = this.get(name);
        if (v == null) {
            return null;
        }
        try {
            return this.valueConverter.convertToLong(v);
        }
        catch (Throwable ignored) {
            return null;
        }
    }
    
    @Override
    public long getLong(final T name, final long defaultValue) {
        final Long v = this.getLong(name);
        return (v == null) ? defaultValue : v;
    }
    
    @Override
    public Float getFloat(final T name) {
        final T v = this.get(name);
        if (v == null) {
            return null;
        }
        try {
            return this.valueConverter.convertToFloat(v);
        }
        catch (Throwable ignored) {
            return null;
        }
    }
    
    @Override
    public float getFloat(final T name, final float defaultValue) {
        final Float v = this.getFloat(name);
        return (v == null) ? defaultValue : v;
    }
    
    @Override
    public Double getDouble(final T name) {
        final T v = this.get(name);
        if (v == null) {
            return null;
        }
        try {
            return this.valueConverter.convertToDouble(v);
        }
        catch (Throwable ignored) {
            return null;
        }
    }
    
    @Override
    public double getDouble(final T name, final double defaultValue) {
        final Double v = this.getDouble(name);
        return (v == null) ? defaultValue : v;
    }
    
    @Override
    public Long getTimeMillis(final T name) {
        final T v = this.get(name);
        if (v == null) {
            return null;
        }
        try {
            return this.valueConverter.convertToTimeMillis(v);
        }
        catch (Throwable ignored) {
            return null;
        }
    }
    
    @Override
    public long getTimeMillis(final T name, final long defaultValue) {
        final Long v = this.getTimeMillis(name);
        return (v == null) ? defaultValue : v;
    }
    
    @Override
    public Boolean getBooleanAndRemove(final T name) {
        final T v = this.getAndRemove(name);
        if (v == null) {
            return null;
        }
        try {
            return this.valueConverter.convertToBoolean(v);
        }
        catch (Throwable ignored) {
            return null;
        }
    }
    
    @Override
    public boolean getBooleanAndRemove(final T name, final boolean defaultValue) {
        final Boolean v = this.getBooleanAndRemove(name);
        return (v == null) ? defaultValue : v;
    }
    
    @Override
    public Byte getByteAndRemove(final T name) {
        final T v = this.getAndRemove(name);
        if (v == null) {
            return null;
        }
        try {
            return this.valueConverter.convertToByte(v);
        }
        catch (Throwable ignored) {
            return null;
        }
    }
    
    @Override
    public byte getByteAndRemove(final T name, final byte defaultValue) {
        final Byte v = this.getByteAndRemove(name);
        return (v == null) ? defaultValue : v;
    }
    
    @Override
    public Character getCharAndRemove(final T name) {
        final T v = this.getAndRemove(name);
        if (v == null) {
            return null;
        }
        try {
            return this.valueConverter.convertToChar(v);
        }
        catch (Throwable ignored) {
            return null;
        }
    }
    
    @Override
    public char getCharAndRemove(final T name, final char defaultValue) {
        final Character v = this.getCharAndRemove(name);
        return (v == null) ? defaultValue : v;
    }
    
    @Override
    public Short getShortAndRemove(final T name) {
        final T v = this.getAndRemove(name);
        if (v == null) {
            return null;
        }
        try {
            return this.valueConverter.convertToShort(v);
        }
        catch (Throwable ignored) {
            return null;
        }
    }
    
    @Override
    public short getShortAndRemove(final T name, final short defaultValue) {
        final Short v = this.getShortAndRemove(name);
        return (v == null) ? defaultValue : v;
    }
    
    @Override
    public Integer getIntAndRemove(final T name) {
        final T v = this.getAndRemove(name);
        if (v == null) {
            return null;
        }
        try {
            return this.valueConverter.convertToInt(v);
        }
        catch (Throwable ignored) {
            return null;
        }
    }
    
    @Override
    public int getIntAndRemove(final T name, final int defaultValue) {
        final Integer v = this.getIntAndRemove(name);
        return (v == null) ? defaultValue : v;
    }
    
    @Override
    public Long getLongAndRemove(final T name) {
        final T v = this.getAndRemove(name);
        if (v == null) {
            return null;
        }
        try {
            return this.valueConverter.convertToLong(v);
        }
        catch (Throwable ignored) {
            return null;
        }
    }
    
    @Override
    public long getLongAndRemove(final T name, final long defaultValue) {
        final Long v = this.getLongAndRemove(name);
        return (v == null) ? defaultValue : v;
    }
    
    @Override
    public Float getFloatAndRemove(final T name) {
        final T v = this.getAndRemove(name);
        if (v == null) {
            return null;
        }
        try {
            return this.valueConverter.convertToFloat(v);
        }
        catch (Throwable ignored) {
            return null;
        }
    }
    
    @Override
    public float getFloatAndRemove(final T name, final float defaultValue) {
        final Float v = this.getFloatAndRemove(name);
        return (v == null) ? defaultValue : v;
    }
    
    @Override
    public Double getDoubleAndRemove(final T name) {
        final T v = this.getAndRemove(name);
        if (v == null) {
            return null;
        }
        try {
            return this.valueConverter.convertToDouble(v);
        }
        catch (Throwable ignored) {
            return null;
        }
    }
    
    @Override
    public double getDoubleAndRemove(final T name, final double defaultValue) {
        final Double v = this.getDoubleAndRemove(name);
        return (v == null) ? defaultValue : v;
    }
    
    @Override
    public Long getTimeMillisAndRemove(final T name) {
        final T v = this.getAndRemove(name);
        if (v == null) {
            return null;
        }
        try {
            return this.valueConverter.convertToTimeMillis(v);
        }
        catch (Throwable ignored) {
            return null;
        }
    }
    
    @Override
    public long getTimeMillisAndRemove(final T name, final long defaultValue) {
        final Long v = this.getTimeMillisAndRemove(name);
        return (v == null) ? defaultValue : v;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof DefaultHeaders)) {
            return false;
        }
        final DefaultHeaders<T> h2 = (DefaultHeaders<T>)o;
        final List<T> namesList = this.namesList();
        final List<T> otherNamesList = h2.namesList();
        if (!equals(namesList, otherNamesList, this.keyComparator)) {
            return false;
        }
        final Set<T> names = new TreeSet<T>(this.keyComparator);
        names.addAll((Collection<? extends T>)namesList);
        for (final T name : names) {
            if (!equals((List<T>)this.getAll((T)name), h2.getAll(name), this.valueComparator)) {
                return false;
            }
        }
        return true;
    }
    
    private static <T> boolean equals(final List<T> lhs, final List<T> rhs, final Comparator<? super T> comparator) {
        final int lhsSize = lhs.size();
        if (lhsSize != rhs.size()) {
            return false;
        }
        Collections.sort(lhs, comparator);
        Collections.sort(rhs, comparator);
        for (int i = 0; i < lhsSize; ++i) {
            if (comparator.compare((Object)lhs.get(i), (Object)rhs.get(i)) != 0) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        int result = 1;
        for (final T name : this.names()) {
            result = 31 * result + name.hashCode();
            final List<T> values = this.getAll(name);
            Collections.sort(values, this.valueComparator);
            for (int i = 0; i < values.size(); ++i) {
                result = 31 * result + this.hashCodeGenerator.generateHashCode(values.get(i));
            }
        }
        return result;
    }
    
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(this.getClass().getSimpleName()).append('[');
        for (final T name : this.names()) {
            final List<T> values = this.getAll(name);
            Collections.sort(values, this.valueComparator);
            for (int i = 0; i < values.size(); ++i) {
                builder.append(name).append(": ").append(values.get(i)).append(", ");
            }
        }
        if (builder.length() > 2) {
            builder.setLength(builder.length() - 2);
        }
        return builder.append(']').toString();
    }
    
    protected ValueConverter<T> valueConverter() {
        return this.valueConverter;
    }
    
    private T convertName(final T name) {
        return this.nameConverter.convertName(ObjectUtil.checkNotNull(name, "name"));
    }
    
    private int index(final int hash) {
        return Math.abs(hash % this.bucketSize);
    }
    
    private void add0(final Headers<T> headers) {
        if (headers.isEmpty()) {
            return;
        }
        if (headers instanceof DefaultHeaders) {
            final DefaultHeaders<T> m = (DefaultHeaders)headers;
            for (HeaderEntry e = m.head.after; e != m.head; e = e.after) {
                this.add(e.name, e.value);
            }
        }
        else {
            try {
                headers.forEachEntry(this.addAllVisitor());
            }
            catch (Exception ex) {
                PlatformDependent.throwException(ex);
            }
        }
    }
    
    private void add0(final int h, final int i, final T name, final T value) {
        final HeaderEntry newEntry = new HeaderEntry(h, name, value);
        final HeaderEntry oldTail = this.tailEntries.get(i);
        if (oldTail == null) {
            this.entries.put(i, newEntry);
        }
        else {
            oldTail.next = newEntry;
        }
        this.tailEntries.put(i, newEntry);
        newEntry.addBefore(this.head);
    }
    
    private boolean remove0(final int h, final int i, final T name) {
        HeaderEntry e = this.entries.get(i);
        if (e == null) {
            return false;
        }
        boolean removed = false;
        while (e.hash == h && this.keyComparator.compare((Object)e.name, (Object)name) == 0) {
            e.remove();
            final HeaderEntry next = e.next;
            if (next == null) {
                this.entries.remove(i);
                this.tailEntries.remove(i);
                return true;
            }
            this.entries.put(i, next);
            e = next;
            removed = true;
        }
        while (true) {
            final HeaderEntry next = e.next;
            if (next == null) {
                break;
            }
            if (next.hash == h && this.keyComparator.compare((Object)next.name, (Object)name) == 0) {
                e.next = next.next;
                if (e.next == null) {
                    this.tailEntries.put(i, e);
                }
                next.remove();
                removed = true;
            }
            else {
                e = next;
            }
        }
        return removed;
    }
    
    private EntryVisitor<T> setAllVisitor() {
        return new EntryVisitor<T>() {
            @Override
            public boolean visit(final Map.Entry<T, T> entry) {
                DefaultHeaders.this.set(entry.getKey(), entry.getValue());
                return true;
            }
        };
    }
    
    private EntryVisitor<T> addAllVisitor() {
        return new EntryVisitor<T>() {
            @Override
            public boolean visit(final Map.Entry<T, T> entry) {
                DefaultHeaders.this.add(entry.getKey(), entry.getValue());
                return true;
            }
        };
    }
    
    static {
        DEFAULT_NAME_CONVERTER = new IdentityNameConverter<Object>();
    }
    
    public static final class IdentityNameConverter<T> implements NameConverter<T>
    {
        @Override
        public T convertName(final T name) {
            return name;
        }
    }
    
    private final class HeaderEntry implements Map.Entry<T, T>
    {
        final int hash;
        final T name;
        T value;
        HeaderEntry next;
        HeaderEntry before;
        HeaderEntry after;
        
        HeaderEntry(final int hash, final T name, final T value) {
            this.hash = hash;
            this.name = name;
            this.value = value;
        }
        
        HeaderEntry() {
            this.hash = -1;
            this.name = null;
            this.value = null;
        }
        
        void remove() {
            this.before.after = this.after;
            this.after.before = this.before;
            final DefaultHeaders this$0 = DefaultHeaders.this;
            --this$0.size;
        }
        
        void addBefore(final HeaderEntry e) {
            this.after = e;
            this.before = e.before;
            this.before.after = this;
            this.after.before = this;
            final DefaultHeaders this$0 = DefaultHeaders.this;
            ++this$0.size;
        }
        
        @Override
        public T getKey() {
            return this.name;
        }
        
        @Override
        public T getValue() {
            return this.value;
        }
        
        @Override
        public T setValue(final T value) {
            ObjectUtil.checkNotNull(value, "value");
            final T oldValue = this.value;
            this.value = value;
            return oldValue;
        }
        
        @Override
        public String toString() {
            return new StringBuilder().append(this.name).append('=').append(this.value).toString();
        }
    }
    
    protected final class KeyValueHeaderIterator implements Iterator<Map.Entry<T, T>>
    {
        private HeaderEntry current;
        
        protected KeyValueHeaderIterator() {
            this.current = DefaultHeaders.this.head;
        }
        
        @Override
        public boolean hasNext() {
            return this.current.after != DefaultHeaders.this.head;
        }
        
        @Override
        public Map.Entry<T, T> next() {
            this.current = this.current.after;
            if (this.current == DefaultHeaders.this.head) {
                throw new NoSuchElementException();
            }
            return this.current;
        }
        
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
    
    static final class HeaderDateFormat
    {
        private static final ParsePosition parsePos;
        private static final FastThreadLocal<HeaderDateFormat> dateFormatThreadLocal;
        private final DateFormat dateFormat1;
        private final DateFormat dateFormat2;
        private final DateFormat dateFormat3;
        
        static HeaderDateFormat get() {
            return HeaderDateFormat.dateFormatThreadLocal.get();
        }
        
        private HeaderDateFormat() {
            this.dateFormat1 = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
            this.dateFormat2 = new SimpleDateFormat("E, dd-MMM-yy HH:mm:ss z", Locale.ENGLISH);
            this.dateFormat3 = new SimpleDateFormat("E MMM d HH:mm:ss yyyy", Locale.ENGLISH);
            final TimeZone tz = TimeZone.getTimeZone("GMT");
            this.dateFormat1.setTimeZone(tz);
            this.dateFormat2.setTimeZone(tz);
            this.dateFormat3.setTimeZone(tz);
        }
        
        long parse(final String text) throws ParseException {
            Date date = this.dateFormat1.parse(text, HeaderDateFormat.parsePos);
            if (date == null) {
                date = this.dateFormat2.parse(text, HeaderDateFormat.parsePos);
            }
            if (date == null) {
                date = this.dateFormat3.parse(text, HeaderDateFormat.parsePos);
            }
            if (date == null) {
                throw new ParseException(text, 0);
            }
            return date.getTime();
        }
        
        long parse(final String text, final long defaultValue) {
            Date date = this.dateFormat1.parse(text, HeaderDateFormat.parsePos);
            if (date == null) {
                date = this.dateFormat2.parse(text, HeaderDateFormat.parsePos);
            }
            if (date == null) {
                date = this.dateFormat3.parse(text, HeaderDateFormat.parsePos);
            }
            if (date == null) {
                return defaultValue;
            }
            return date.getTime();
        }
        
        static {
            parsePos = new ParsePosition(0);
            dateFormatThreadLocal = new FastThreadLocal<HeaderDateFormat>() {
                @Override
                protected HeaderDateFormat initialValue() {
                    return new HeaderDateFormat();
                }
            };
        }
    }
    
    public interface NameConverter<T>
    {
        T convertName(final T p0);
    }
    
    public interface HashCodeGenerator<T>
    {
        int generateHashCode(final T p0);
    }
}
