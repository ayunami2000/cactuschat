// 
// Decompiled by Procyon v0.5.36
// 

package jline.console.history;

import java.util.Iterator;
import java.util.ListIterator;

public interface History extends Iterable<Entry>
{
    int size();
    
    boolean isEmpty();
    
    int index();
    
    void clear();
    
    CharSequence get(final int p0);
    
    void add(final CharSequence p0);
    
    void set(final int p0, final CharSequence p1);
    
    CharSequence remove(final int p0);
    
    CharSequence removeFirst();
    
    CharSequence removeLast();
    
    void replace(final CharSequence p0);
    
    ListIterator<Entry> entries(final int p0);
    
    ListIterator<Entry> entries();
    
    Iterator<Entry> iterator();
    
    CharSequence current();
    
    boolean previous();
    
    boolean next();
    
    boolean moveToFirst();
    
    boolean moveToLast();
    
    boolean moveTo(final int p0);
    
    void moveToEnd();
    
    public interface Entry
    {
        int index();
        
        CharSequence value();
    }
}
