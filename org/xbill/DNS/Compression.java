// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

public class Compression
{
    private static final int TABLE_SIZE = 17;
    private static final int MAX_POINTER = 16383;
    private Entry[] table;
    private boolean verbose;
    
    public Compression() {
        this.verbose = Options.check("verbosecompression");
        this.table = new Entry[17];
    }
    
    public void add(final int pos, final Name name) {
        if (pos > 16383) {
            return;
        }
        final int row = (name.hashCode() & Integer.MAX_VALUE) % 17;
        final Entry entry = new Entry();
        entry.name = name;
        entry.pos = pos;
        entry.next = this.table[row];
        this.table[row] = entry;
        if (this.verbose) {
            System.err.println("Adding " + name + " at " + pos);
        }
    }
    
    public int get(final Name name) {
        final int row = (name.hashCode() & Integer.MAX_VALUE) % 17;
        int pos = -1;
        for (Entry entry = this.table[row]; entry != null; entry = entry.next) {
            if (entry.name.equals(name)) {
                pos = entry.pos;
            }
        }
        if (this.verbose) {
            System.err.println("Looking for " + name + ", found " + pos);
        }
        return pos;
    }
    
    private static class Entry
    {
        Name name;
        int pos;
        Entry next;
    }
}
