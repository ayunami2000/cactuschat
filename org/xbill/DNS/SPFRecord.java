// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.util.List;

public class SPFRecord extends TXTBase
{
    private static final long serialVersionUID = -2100754352801658722L;
    
    SPFRecord() {
    }
    
    Record getObject() {
        return new SPFRecord();
    }
    
    public SPFRecord(final Name name, final int dclass, final long ttl, final List strings) {
        super(name, 99, dclass, ttl, strings);
    }
    
    public SPFRecord(final Name name, final int dclass, final long ttl, final String string) {
        super(name, 99, dclass, ttl, string);
    }
}
