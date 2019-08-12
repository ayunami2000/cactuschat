// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec;

import java.util.Set;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.List;

public interface ConvertibleHeaders<UnconvertedType, ConvertedType> extends Headers<UnconvertedType>
{
    ConvertedType getAndConvert(final UnconvertedType p0);
    
    ConvertedType getAndConvert(final UnconvertedType p0, final ConvertedType p1);
    
    ConvertedType getAndRemoveAndConvert(final UnconvertedType p0);
    
    ConvertedType getAndRemoveAndConvert(final UnconvertedType p0, final ConvertedType p1);
    
    List<ConvertedType> getAllAndConvert(final UnconvertedType p0);
    
    List<ConvertedType> getAllAndRemoveAndConvert(final UnconvertedType p0);
    
    List<Map.Entry<ConvertedType, ConvertedType>> entriesConverted();
    
    Iterator<Map.Entry<ConvertedType, ConvertedType>> iteratorConverted();
    
    Set<ConvertedType> namesAndConvert(final Comparator<ConvertedType> p0);
    
    public interface TypeConverter<UnconvertedType, ConvertedType>
    {
        ConvertedType toConvertedType(final UnconvertedType p0);
        
        UnconvertedType toUnconvertedType(final ConvertedType p0);
    }
}
