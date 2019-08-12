// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec;

import java.util.Iterator;
import java.util.Set;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public interface Headers<T> extends Iterable<Map.Entry<T, T>>
{
    T get(final T p0);
    
    T get(final T p0, final T p1);
    
    T getAndRemove(final T p0);
    
    T getAndRemove(final T p0, final T p1);
    
    List<T> getAll(final T p0);
    
    List<T> getAllAndRemove(final T p0);
    
    Boolean getBoolean(final T p0);
    
    boolean getBoolean(final T p0, final boolean p1);
    
    Byte getByte(final T p0);
    
    byte getByte(final T p0, final byte p1);
    
    Character getChar(final T p0);
    
    char getChar(final T p0, final char p1);
    
    Short getShort(final T p0);
    
    short getInt(final T p0, final short p1);
    
    Integer getInt(final T p0);
    
    int getInt(final T p0, final int p1);
    
    Long getLong(final T p0);
    
    long getLong(final T p0, final long p1);
    
    Float getFloat(final T p0);
    
    float getFloat(final T p0, final float p1);
    
    Double getDouble(final T p0);
    
    double getDouble(final T p0, final double p1);
    
    Long getTimeMillis(final T p0);
    
    long getTimeMillis(final T p0, final long p1);
    
    Boolean getBooleanAndRemove(final T p0);
    
    boolean getBooleanAndRemove(final T p0, final boolean p1);
    
    Byte getByteAndRemove(final T p0);
    
    byte getByteAndRemove(final T p0, final byte p1);
    
    Character getCharAndRemove(final T p0);
    
    char getCharAndRemove(final T p0, final char p1);
    
    Short getShortAndRemove(final T p0);
    
    short getShortAndRemove(final T p0, final short p1);
    
    Integer getIntAndRemove(final T p0);
    
    int getIntAndRemove(final T p0, final int p1);
    
    Long getLongAndRemove(final T p0);
    
    long getLongAndRemove(final T p0, final long p1);
    
    Float getFloatAndRemove(final T p0);
    
    float getFloatAndRemove(final T p0, final float p1);
    
    Double getDoubleAndRemove(final T p0);
    
    double getDoubleAndRemove(final T p0, final double p1);
    
    Long getTimeMillisAndRemove(final T p0);
    
    long getTimeMillisAndRemove(final T p0, final long p1);
    
    List<Map.Entry<T, T>> entries();
    
    boolean contains(final T p0);
    
    boolean contains(final T p0, final T p1);
    
    boolean containsObject(final T p0, final Object p1);
    
    boolean containsBoolean(final T p0, final boolean p1);
    
    boolean containsByte(final T p0, final byte p1);
    
    boolean containsChar(final T p0, final char p1);
    
    boolean containsShort(final T p0, final short p1);
    
    boolean containsInt(final T p0, final int p1);
    
    boolean containsLong(final T p0, final long p1);
    
    boolean containsFloat(final T p0, final float p1);
    
    boolean containsDouble(final T p0, final double p1);
    
    boolean containsTimeMillis(final T p0, final long p1);
    
    boolean contains(final T p0, final T p1, final Comparator<? super T> p2);
    
    boolean contains(final T p0, final T p1, final Comparator<? super T> p2, final Comparator<? super T> p3);
    
    boolean containsObject(final T p0, final Object p1, final Comparator<? super T> p2);
    
    boolean containsObject(final T p0, final Object p1, final Comparator<? super T> p2, final Comparator<? super T> p3);
    
    int size();
    
    boolean isEmpty();
    
    Set<T> names();
    
    List<T> namesList();
    
    Headers<T> add(final T p0, final T p1);
    
    Headers<T> add(final T p0, final Iterable<? extends T> p1);
    
    Headers<T> add(final T p0, final T... p1);
    
    Headers<T> addObject(final T p0, final Object p1);
    
    Headers<T> addObject(final T p0, final Iterable<?> p1);
    
    Headers<T> addObject(final T p0, final Object... p1);
    
    Headers<T> addBoolean(final T p0, final boolean p1);
    
    Headers<T> addByte(final T p0, final byte p1);
    
    Headers<T> addChar(final T p0, final char p1);
    
    Headers<T> addShort(final T p0, final short p1);
    
    Headers<T> addInt(final T p0, final int p1);
    
    Headers<T> addLong(final T p0, final long p1);
    
    Headers<T> addFloat(final T p0, final float p1);
    
    Headers<T> addDouble(final T p0, final double p1);
    
    Headers<T> addTimeMillis(final T p0, final long p1);
    
    Headers<T> add(final Headers<T> p0);
    
    Headers<T> set(final T p0, final T p1);
    
    Headers<T> set(final T p0, final Iterable<? extends T> p1);
    
    Headers<T> set(final T p0, final T... p1);
    
    Headers<T> setObject(final T p0, final Object p1);
    
    Headers<T> setObject(final T p0, final Iterable<?> p1);
    
    Headers<T> setObject(final T p0, final Object... p1);
    
    Headers<T> setBoolean(final T p0, final boolean p1);
    
    Headers<T> setByte(final T p0, final byte p1);
    
    Headers<T> setChar(final T p0, final char p1);
    
    Headers<T> setShort(final T p0, final short p1);
    
    Headers<T> setInt(final T p0, final int p1);
    
    Headers<T> setLong(final T p0, final long p1);
    
    Headers<T> setFloat(final T p0, final float p1);
    
    Headers<T> setDouble(final T p0, final double p1);
    
    Headers<T> setTimeMillis(final T p0, final long p1);
    
    Headers<T> set(final Headers<T> p0);
    
    Headers<T> setAll(final Headers<T> p0);
    
    boolean remove(final T p0);
    
    Headers<T> clear();
    
    Iterator<Map.Entry<T, T>> iterator();
    
    Map.Entry<T, T> forEachEntry(final EntryVisitor<T> p0) throws Exception;
    
    T forEachName(final NameVisitor<T> p0) throws Exception;
    
    public interface ValueConverter<T>
    {
        T convertObject(final Object p0);
        
        T convertBoolean(final boolean p0);
        
        boolean convertToBoolean(final T p0);
        
        T convertByte(final byte p0);
        
        byte convertToByte(final T p0);
        
        T convertChar(final char p0);
        
        char convertToChar(final T p0);
        
        T convertShort(final short p0);
        
        short convertToShort(final T p0);
        
        T convertInt(final int p0);
        
        int convertToInt(final T p0);
        
        T convertLong(final long p0);
        
        long convertToLong(final T p0);
        
        T convertTimeMillis(final long p0);
        
        long convertToTimeMillis(final T p0);
        
        T convertFloat(final float p0);
        
        float convertToFloat(final T p0);
        
        T convertDouble(final double p0);
        
        double convertToDouble(final T p0);
    }
    
    public interface NameVisitor<T>
    {
        boolean visit(final T p0) throws Exception;
    }
    
    public interface EntryVisitor<T>
    {
        boolean visit(final Map.Entry<T, T> p0) throws Exception;
    }
}
