// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.util.Iterator;
import java.io.IOException;
import java.util.TreeSet;
import java.io.Serializable;

final class TypeBitmap implements Serializable
{
    private static final long serialVersionUID = -125354057735389003L;
    private TreeSet types;
    
    private TypeBitmap() {
        this.types = new TreeSet();
    }
    
    public TypeBitmap(final int[] array) {
        this();
        for (int i = 0; i < array.length; ++i) {
            Type.check(array[i]);
            this.types.add(new Integer(array[i]));
        }
    }
    
    public TypeBitmap(final DNSInput in) throws WireParseException {
        this();
        final int lastbase = -1;
        while (in.remaining() > 0) {
            if (in.remaining() < 2) {
                throw new WireParseException("invalid bitmap descriptor");
            }
            final int mapbase = in.readU8();
            if (mapbase < lastbase) {
                throw new WireParseException("invalid ordering");
            }
            final int maplength = in.readU8();
            if (maplength > in.remaining()) {
                throw new WireParseException("invalid bitmap");
            }
            for (int i = 0; i < maplength; ++i) {
                final int current = in.readU8();
                if (current != 0) {
                    for (int j = 0; j < 8; ++j) {
                        if ((current & 1 << 7 - j) != 0x0) {
                            final int typecode = mapbase * 256 + i * 8 + j;
                            this.types.add(Mnemonic.toInteger(typecode));
                        }
                    }
                }
            }
        }
    }
    
    public TypeBitmap(final Tokenizer st) throws IOException {
        this();
        while (true) {
            final Tokenizer.Token t = st.get();
            if (!t.isString()) {
                st.unget();
                return;
            }
            final int typecode = Type.value(t.value);
            if (typecode < 0) {
                throw st.exception("Invalid type: " + t.value);
            }
            this.types.add(Mnemonic.toInteger(typecode));
        }
    }
    
    public int[] toArray() {
        final int[] array = new int[this.types.size()];
        int n = 0;
        final Iterator it = this.types.iterator();
        while (it.hasNext()) {
            array[n++] = it.next();
        }
        return array;
    }
    
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        for (final int t : this.types) {
            sb.append(Type.string(t));
            sb.append(' ');
        }
        return sb.substring(0, sb.length() - 1);
    }
    
    private static void mapToWire(final DNSOutput out, final TreeSet map, final int mapbase) {
        final int arraymax = map.last() & 0xFF;
        final int arraylength = arraymax / 8 + 1;
        final int[] array = new int[arraylength];
        out.writeU8(mapbase);
        out.writeU8(arraylength);
        for (final int typecode : map) {
            final int[] array2 = array;
            final int n = (typecode & 0xFF) / 8;
            array2[n] |= 1 << 7 - typecode % 8;
        }
        for (int j = 0; j < arraylength; ++j) {
            out.writeU8(array[j]);
        }
    }
    
    public void toWire(final DNSOutput out) {
        if (this.types.size() == 0) {
            return;
        }
        int mapbase = -1;
        final TreeSet map = new TreeSet();
        for (final int t : this.types) {
            final int base = t >> 8;
            if (base != mapbase) {
                if (map.size() > 0) {
                    mapToWire(out, map, mapbase);
                    map.clear();
                }
                mapbase = base;
            }
            map.add(new Integer(t));
        }
        mapToWire(out, map, mapbase);
    }
    
    public boolean empty() {
        return this.types.isEmpty();
    }
    
    public boolean contains(final int typecode) {
        return this.types.contains(Mnemonic.toInteger(typecode));
    }
}
