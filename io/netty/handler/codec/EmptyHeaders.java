// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec;

import java.util.Iterator;
import java.util.Set;
import java.util.Comparator;
import java.util.Map;
import java.util.Collections;
import java.util.List;

public class EmptyHeaders<T> implements Headers<T>
{
    @Override
    public T get(final T name) {
        return null;
    }
    
    @Override
    public T get(final T name, final T defaultValue) {
        return null;
    }
    
    @Override
    public T getAndRemove(final T name) {
        return null;
    }
    
    @Override
    public T getAndRemove(final T name, final T defaultValue) {
        return null;
    }
    
    @Override
    public List<T> getAll(final T name) {
        return Collections.emptyList();
    }
    
    @Override
    public List<T> getAllAndRemove(final T name) {
        return Collections.emptyList();
    }
    
    @Override
    public Boolean getBoolean(final T name) {
        return null;
    }
    
    @Override
    public boolean getBoolean(final T name, final boolean defaultValue) {
        return defaultValue;
    }
    
    @Override
    public Byte getByte(final T name) {
        return null;
    }
    
    @Override
    public byte getByte(final T name, final byte defaultValue) {
        return defaultValue;
    }
    
    @Override
    public Character getChar(final T name) {
        return null;
    }
    
    @Override
    public char getChar(final T name, final char defaultValue) {
        return defaultValue;
    }
    
    @Override
    public Short getShort(final T name) {
        return null;
    }
    
    @Override
    public short getInt(final T name, final short defaultValue) {
        return defaultValue;
    }
    
    @Override
    public Integer getInt(final T name) {
        return null;
    }
    
    @Override
    public int getInt(final T name, final int defaultValue) {
        return defaultValue;
    }
    
    @Override
    public Long getLong(final T name) {
        return null;
    }
    
    @Override
    public long getLong(final T name, final long defaultValue) {
        return defaultValue;
    }
    
    @Override
    public Float getFloat(final T name) {
        return null;
    }
    
    @Override
    public float getFloat(final T name, final float defaultValue) {
        return defaultValue;
    }
    
    @Override
    public Double getDouble(final T name) {
        return null;
    }
    
    @Override
    public double getDouble(final T name, final double defaultValue) {
        return defaultValue;
    }
    
    @Override
    public Long getTimeMillis(final T name) {
        return null;
    }
    
    @Override
    public long getTimeMillis(final T name, final long defaultValue) {
        return defaultValue;
    }
    
    @Override
    public Boolean getBooleanAndRemove(final T name) {
        return null;
    }
    
    @Override
    public boolean getBooleanAndRemove(final T name, final boolean defaultValue) {
        return defaultValue;
    }
    
    @Override
    public Byte getByteAndRemove(final T name) {
        return null;
    }
    
    @Override
    public byte getByteAndRemove(final T name, final byte defaultValue) {
        return defaultValue;
    }
    
    @Override
    public Character getCharAndRemove(final T name) {
        return null;
    }
    
    @Override
    public char getCharAndRemove(final T name, final char defaultValue) {
        return defaultValue;
    }
    
    @Override
    public Short getShortAndRemove(final T name) {
        return null;
    }
    
    @Override
    public short getShortAndRemove(final T name, final short defaultValue) {
        return defaultValue;
    }
    
    @Override
    public Integer getIntAndRemove(final T name) {
        return null;
    }
    
    @Override
    public int getIntAndRemove(final T name, final int defaultValue) {
        return defaultValue;
    }
    
    @Override
    public Long getLongAndRemove(final T name) {
        return null;
    }
    
    @Override
    public long getLongAndRemove(final T name, final long defaultValue) {
        return defaultValue;
    }
    
    @Override
    public Float getFloatAndRemove(final T name) {
        return null;
    }
    
    @Override
    public float getFloatAndRemove(final T name, final float defaultValue) {
        return defaultValue;
    }
    
    @Override
    public Double getDoubleAndRemove(final T name) {
        return null;
    }
    
    @Override
    public double getDoubleAndRemove(final T name, final double defaultValue) {
        return defaultValue;
    }
    
    @Override
    public Long getTimeMillisAndRemove(final T name) {
        return null;
    }
    
    @Override
    public long getTimeMillisAndRemove(final T name, final long defaultValue) {
        return defaultValue;
    }
    
    @Override
    public List<Map.Entry<T, T>> entries() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean contains(final T name) {
        return false;
    }
    
    @Override
    public boolean contains(final T name, final T value) {
        return false;
    }
    
    @Override
    public boolean containsObject(final T name, final Object value) {
        return false;
    }
    
    @Override
    public boolean containsBoolean(final T name, final boolean value) {
        return false;
    }
    
    @Override
    public boolean containsByte(final T name, final byte value) {
        return false;
    }
    
    @Override
    public boolean containsChar(final T name, final char value) {
        return false;
    }
    
    @Override
    public boolean containsShort(final T name, final short value) {
        return false;
    }
    
    @Override
    public boolean containsInt(final T name, final int value) {
        return false;
    }
    
    @Override
    public boolean containsLong(final T name, final long value) {
        return false;
    }
    
    @Override
    public boolean containsFloat(final T name, final float value) {
        return false;
    }
    
    @Override
    public boolean containsDouble(final T name, final double value) {
        return false;
    }
    
    @Override
    public boolean containsTimeMillis(final T name, final long value) {
        return false;
    }
    
    @Override
    public boolean contains(final T name, final T value, final Comparator<? super T> comparator) {
        return false;
    }
    
    @Override
    public boolean contains(final T name, final T value, final Comparator<? super T> keyComparator, final Comparator<? super T> valueComparator) {
        return false;
    }
    
    @Override
    public boolean containsObject(final T name, final Object value, final Comparator<? super T> comparator) {
        return false;
    }
    
    @Override
    public boolean containsObject(final T name, final Object value, final Comparator<? super T> keyComparator, final Comparator<? super T> valueComparator) {
        return false;
    }
    
    @Override
    public int size() {
        return 0;
    }
    
    @Override
    public boolean isEmpty() {
        return true;
    }
    
    @Override
    public Set<T> names() {
        return Collections.emptySet();
    }
    
    @Override
    public List<T> namesList() {
        return Collections.emptyList();
    }
    
    @Override
    public Headers<T> add(final T name, final T value) {
        throw new UnsupportedOperationException("read only");
    }
    
    @Override
    public Headers<T> add(final T name, final Iterable<? extends T> values) {
        throw new UnsupportedOperationException("read only");
    }
    
    @Override
    public Headers<T> add(final T name, final T... values) {
        throw new UnsupportedOperationException("read only");
    }
    
    @Override
    public Headers<T> addObject(final T name, final Object value) {
        throw new UnsupportedOperationException("read only");
    }
    
    @Override
    public Headers<T> addObject(final T name, final Iterable<?> values) {
        throw new UnsupportedOperationException("read only");
    }
    
    @Override
    public Headers<T> addObject(final T name, final Object... values) {
        throw new UnsupportedOperationException("read only");
    }
    
    @Override
    public Headers<T> addBoolean(final T name, final boolean value) {
        throw new UnsupportedOperationException("read only");
    }
    
    @Override
    public Headers<T> addByte(final T name, final byte value) {
        throw new UnsupportedOperationException("read only");
    }
    
    @Override
    public Headers<T> addChar(final T name, final char value) {
        throw new UnsupportedOperationException("read only");
    }
    
    @Override
    public Headers<T> addShort(final T name, final short value) {
        throw new UnsupportedOperationException("read only");
    }
    
    @Override
    public Headers<T> addInt(final T name, final int value) {
        throw new UnsupportedOperationException("read only");
    }
    
    @Override
    public Headers<T> addLong(final T name, final long value) {
        throw new UnsupportedOperationException("read only");
    }
    
    @Override
    public Headers<T> addFloat(final T name, final float value) {
        throw new UnsupportedOperationException("read only");
    }
    
    @Override
    public Headers<T> addDouble(final T name, final double value) {
        throw new UnsupportedOperationException("read only");
    }
    
    @Override
    public Headers<T> addTimeMillis(final T name, final long value) {
        throw new UnsupportedOperationException("read only");
    }
    
    @Override
    public Headers<T> add(final Headers<T> headers) {
        throw new UnsupportedOperationException("read only");
    }
    
    @Override
    public Headers<T> set(final T name, final T value) {
        throw new UnsupportedOperationException("read only");
    }
    
    @Override
    public Headers<T> set(final T name, final Iterable<? extends T> values) {
        throw new UnsupportedOperationException("read only");
    }
    
    @Override
    public Headers<T> set(final T name, final T... values) {
        throw new UnsupportedOperationException("read only");
    }
    
    @Override
    public Headers<T> setObject(final T name, final Object value) {
        throw new UnsupportedOperationException("read only");
    }
    
    @Override
    public Headers<T> setObject(final T name, final Iterable<?> values) {
        throw new UnsupportedOperationException("read only");
    }
    
    @Override
    public Headers<T> setObject(final T name, final Object... values) {
        throw new UnsupportedOperationException("read only");
    }
    
    @Override
    public Headers<T> setBoolean(final T name, final boolean value) {
        throw new UnsupportedOperationException("read only");
    }
    
    @Override
    public Headers<T> setByte(final T name, final byte value) {
        throw new UnsupportedOperationException("read only");
    }
    
    @Override
    public Headers<T> setChar(final T name, final char value) {
        throw new UnsupportedOperationException("read only");
    }
    
    @Override
    public Headers<T> setShort(final T name, final short value) {
        throw new UnsupportedOperationException("read only");
    }
    
    @Override
    public Headers<T> setInt(final T name, final int value) {
        throw new UnsupportedOperationException("read only");
    }
    
    @Override
    public Headers<T> setLong(final T name, final long value) {
        throw new UnsupportedOperationException("read only");
    }
    
    @Override
    public Headers<T> setFloat(final T name, final float value) {
        throw new UnsupportedOperationException("read only");
    }
    
    @Override
    public Headers<T> setDouble(final T name, final double value) {
        throw new UnsupportedOperationException("read only");
    }
    
    @Override
    public Headers<T> setTimeMillis(final T name, final long value) {
        throw new UnsupportedOperationException("read only");
    }
    
    @Override
    public Headers<T> set(final Headers<T> headers) {
        throw new UnsupportedOperationException("read only");
    }
    
    @Override
    public Headers<T> setAll(final Headers<T> headers) {
        throw new UnsupportedOperationException("read only");
    }
    
    @Override
    public boolean remove(final T name) {
        return false;
    }
    
    @Override
    public Headers<T> clear() {
        return this;
    }
    
    @Override
    public Iterator<Map.Entry<T, T>> iterator() {
        return this.entries().iterator();
    }
    
    @Override
    public Map.Entry<T, T> forEachEntry(final EntryVisitor<T> visitor) throws Exception {
        return null;
    }
    
    @Override
    public T forEachName(final NameVisitor<T> visitor) throws Exception {
        return null;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof Headers)) {
            return false;
        }
        final Headers<?> rhs = (Headers<?>)o;
        return this.isEmpty() && rhs.isEmpty();
    }
    
    @Override
    public int hashCode() {
        return 1;
    }
    
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + '[' + ']';
    }
}
