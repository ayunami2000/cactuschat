// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec;

import java.util.Set;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Collections;
import java.util.List;

public class EmptyConvertibleHeaders<UnconvertedType, ConvertedType> extends EmptyHeaders<UnconvertedType> implements ConvertibleHeaders<UnconvertedType, ConvertedType>
{
    @Override
    public ConvertedType getAndConvert(final UnconvertedType name) {
        return null;
    }
    
    @Override
    public ConvertedType getAndConvert(final UnconvertedType name, final ConvertedType defaultValue) {
        return defaultValue;
    }
    
    @Override
    public ConvertedType getAndRemoveAndConvert(final UnconvertedType name) {
        return null;
    }
    
    @Override
    public ConvertedType getAndRemoveAndConvert(final UnconvertedType name, final ConvertedType defaultValue) {
        return defaultValue;
    }
    
    @Override
    public List<ConvertedType> getAllAndConvert(final UnconvertedType name) {
        return Collections.emptyList();
    }
    
    @Override
    public List<ConvertedType> getAllAndRemoveAndConvert(final UnconvertedType name) {
        return Collections.emptyList();
    }
    
    @Override
    public List<Map.Entry<ConvertedType, ConvertedType>> entriesConverted() {
        return Collections.emptyList();
    }
    
    @Override
    public Iterator<Map.Entry<ConvertedType, ConvertedType>> iteratorConverted() {
        return this.entriesConverted().iterator();
    }
    
    @Override
    public Set<ConvertedType> namesAndConvert(final Comparator<ConvertedType> comparator) {
        return Collections.emptySet();
    }
}
