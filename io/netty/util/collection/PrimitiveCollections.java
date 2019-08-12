// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.util.collection;

import java.util.NoSuchElementException;
import java.util.Iterator;
import java.util.Collection;
import io.netty.util.internal.EmptyArrays;
import java.util.Collections;

public final class PrimitiveCollections
{
    private static final IntObjectMap<Object> EMPTY_INT_OBJECT_MAP;
    
    private PrimitiveCollections() {
    }
    
    public static <V> IntObjectMap<V> emptyIntObjectMap() {
        return (IntObjectMap<V>)PrimitiveCollections.EMPTY_INT_OBJECT_MAP;
    }
    
    public static <V> IntObjectMap<V> unmodifiableIntObjectMap(final IntObjectMap<V> map) {
        return new UnmodifiableIntObjectMap<V>(map);
    }
    
    static {
        EMPTY_INT_OBJECT_MAP = new EmptyIntObjectMap();
    }
    
    private static final class EmptyIntObjectMap implements IntObjectMap<Object>
    {
        @Override
        public Object get(final int key) {
            return null;
        }
        
        @Override
        public Object put(final int key, final Object value) {
            throw new UnsupportedOperationException("put");
        }
        
        @Override
        public void putAll(final IntObjectMap<Object> sourceMap) {
            throw new UnsupportedOperationException("putAll");
        }
        
        @Override
        public Object remove(final int key) {
            throw new UnsupportedOperationException("remove");
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
        public void clear() {
        }
        
        @Override
        public boolean containsKey(final int key) {
            return false;
        }
        
        @Override
        public boolean containsValue(final Object value) {
            return false;
        }
        
        @Override
        public Iterable<Entry<Object>> entries() {
            return (Iterable<Entry<Object>>)Collections.emptySet();
        }
        
        @Override
        public int[] keys() {
            return EmptyArrays.EMPTY_INTS;
        }
        
        @Override
        public Object[] values(final Class<Object> clazz) {
            return EmptyArrays.EMPTY_OBJECTS;
        }
        
        @Override
        public Collection<Object> values() {
            return Collections.emptyList();
        }
    }
    
    private static final class UnmodifiableIntObjectMap<V> implements IntObjectMap<V>, Iterable<Entry<V>>
    {
        final IntObjectMap<V> map;
        
        UnmodifiableIntObjectMap(final IntObjectMap<V> map) {
            this.map = map;
        }
        
        @Override
        public V get(final int key) {
            return this.map.get(key);
        }
        
        @Override
        public V put(final int key, final V value) {
            throw new UnsupportedOperationException("put");
        }
        
        @Override
        public void putAll(final IntObjectMap<V> sourceMap) {
            throw new UnsupportedOperationException("putAll");
        }
        
        @Override
        public V remove(final int key) {
            throw new UnsupportedOperationException("remove");
        }
        
        @Override
        public int size() {
            return this.map.size();
        }
        
        @Override
        public boolean isEmpty() {
            return this.map.isEmpty();
        }
        
        @Override
        public void clear() {
            throw new UnsupportedOperationException("clear");
        }
        
        @Override
        public boolean containsKey(final int key) {
            return this.map.containsKey(key);
        }
        
        @Override
        public boolean containsValue(final V value) {
            return this.map.containsValue(value);
        }
        
        @Override
        public Iterable<Entry<V>> entries() {
            return this;
        }
        
        @Override
        public Iterator<Entry<V>> iterator() {
            return new IteratorImpl(this.map.entries().iterator());
        }
        
        @Override
        public int[] keys() {
            return this.map.keys();
        }
        
        @Override
        public V[] values(final Class<V> clazz) {
            return this.map.values(clazz);
        }
        
        @Override
        public Collection<V> values() {
            return this.map.values();
        }
        
        private class IteratorImpl implements Iterator<Entry<V>>
        {
            final Iterator<Entry<V>> iter;
            
            IteratorImpl(final Iterator<Entry<V>> iter) {
                this.iter = iter;
            }
            
            @Override
            public boolean hasNext() {
                return this.iter.hasNext();
            }
            
            @Override
            public Entry<V> next() {
                if (!this.hasNext()) {
                    throw new NoSuchElementException();
                }
                return new EntryImpl(this.iter.next());
            }
            
            @Override
            public void remove() {
                throw new UnsupportedOperationException("remove");
            }
        }
        
        private class EntryImpl implements Entry<V>
        {
            final Entry<V> entry;
            
            EntryImpl(final Entry<V> entry) {
                this.entry = entry;
            }
            
            @Override
            public int key() {
                return this.entry.key();
            }
            
            @Override
            public V value() {
                return this.entry.value();
            }
            
            @Override
            public void setValue(final V value) {
                throw new UnsupportedOperationException("setValue");
            }
        }
    }
}
