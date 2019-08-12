// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.io.IOException;
import java.util.BitSet;

public class NXTRecord extends Record
{
    private static final long serialVersionUID = -8851454400765507520L;
    private Name next;
    private BitSet bitmap;
    
    NXTRecord() {
    }
    
    Record getObject() {
        return new NXTRecord();
    }
    
    public NXTRecord(final Name name, final int dclass, final long ttl, final Name next, final BitSet bitmap) {
        super(name, 30, dclass, ttl);
        this.next = Record.checkName("next", next);
        this.bitmap = bitmap;
    }
    
    void rrFromWire(final DNSInput in) throws IOException {
        this.next = new Name(in);
        this.bitmap = new BitSet();
        for (int bitmapLength = in.remaining(), i = 0; i < bitmapLength; ++i) {
            final int t = in.readU8();
            for (int j = 0; j < 8; ++j) {
                if ((t & 1 << 7 - j) != 0x0) {
                    this.bitmap.set(i * 8 + j);
                }
            }
        }
    }
    
    void rdataFromString(final Tokenizer st, final Name origin) throws IOException {
        this.next = st.getName(origin);
        this.bitmap = new BitSet();
        while (true) {
            final Tokenizer.Token t = st.get();
            if (!t.isString()) {
                st.unget();
                return;
            }
            final int typecode = Type.value(t.value, true);
            if (typecode <= 0 || typecode > 128) {
                throw st.exception("Invalid type: " + t.value);
            }
            this.bitmap.set(typecode);
        }
    }
    
    String rrToString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(this.next);
        final int length = this.bitmap.length();
        for (short i = 0; i < length; ++i) {
            if (this.bitmap.get(i)) {
                sb.append(" ");
                sb.append(Type.string(i));
            }
        }
        return sb.toString();
    }
    
    public Name getNext() {
        return this.next;
    }
    
    public BitSet getBitmap() {
        return this.bitmap;
    }
    
    void rrToWire(final DNSOutput out, final Compression c, final boolean canonical) {
        this.next.toWire(out, null, canonical);
        final int length = this.bitmap.length();
        int i = 0;
        int t = 0;
        while (i < length) {
            t |= (this.bitmap.get(i) ? (1 << 7 - i % 8) : 0);
            if (i % 8 == 7 || i == length - 1) {
                out.writeU8(t);
                t = 0;
            }
            ++i;
        }
    }
}
