// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

abstract class TXTBase extends Record
{
    private static final long serialVersionUID = -4319510507246305931L;
    protected List strings;
    
    protected TXTBase() {
    }
    
    protected TXTBase(final Name name, final int type, final int dclass, final long ttl) {
        super(name, type, dclass, ttl);
    }
    
    protected TXTBase(final Name name, final int type, final int dclass, final long ttl, final List strings) {
        super(name, type, dclass, ttl);
        if (strings == null) {
            throw new IllegalArgumentException("strings must not be null");
        }
        this.strings = new ArrayList(strings.size());
        final Iterator it = strings.iterator();
        try {
            while (it.hasNext()) {
                final String s = it.next();
                this.strings.add(Record.byteArrayFromString(s));
            }
        }
        catch (TextParseException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
    
    protected TXTBase(final Name name, final int type, final int dclass, final long ttl, final String string) {
        this(name, type, dclass, ttl, Collections.singletonList(string));
    }
    
    void rrFromWire(final DNSInput in) throws IOException {
        this.strings = new ArrayList(2);
        while (in.remaining() > 0) {
            final byte[] b = in.readCountedString();
            this.strings.add(b);
        }
    }
    
    void rdataFromString(final Tokenizer st, final Name origin) throws IOException {
        this.strings = new ArrayList(2);
        while (true) {
            final Tokenizer.Token t = st.get();
            if (!t.isString()) {
                break;
            }
            try {
                this.strings.add(Record.byteArrayFromString(t.value));
            }
            catch (TextParseException e) {
                throw st.exception(e.getMessage());
            }
        }
        st.unget();
    }
    
    String rrToString() {
        final StringBuffer sb = new StringBuffer();
        final Iterator it = this.strings.iterator();
        while (it.hasNext()) {
            final byte[] array = it.next();
            sb.append(Record.byteArrayToString(array, true));
            if (it.hasNext()) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }
    
    public List getStrings() {
        final List list = new ArrayList(this.strings.size());
        for (int i = 0; i < this.strings.size(); ++i) {
            list.add(Record.byteArrayToString(this.strings.get(i), false));
        }
        return list;
    }
    
    public List getStringsAsByteArrays() {
        return this.strings;
    }
    
    void rrToWire(final DNSOutput out, final Compression c, final boolean canonical) {
        for (final byte[] b : this.strings) {
            out.writeCountedString(b);
        }
    }
}
