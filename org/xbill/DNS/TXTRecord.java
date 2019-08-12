// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.util.List;

public class TXTRecord extends TXTBase
{
    private static final long serialVersionUID = -5780785764284221342L;
    
    TXTRecord() {
    }
    
    Record getObject() {
        return new TXTRecord();
    }
    
    public TXTRecord(final Name name, final int dclass, final long ttl, final List strings) {
        super(name, 16, dclass, ttl, strings);
    }
    
    public TXTRecord(final Name name, final int dclass, final long ttl, final String string) {
        super(name, 16, dclass, ttl, string);
    }
}
