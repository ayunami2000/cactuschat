// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.io.IOException;

class EmptyRecord extends Record
{
    private static final long serialVersionUID = 3601852050646429582L;
    
    Record getObject() {
        return new EmptyRecord();
    }
    
    void rrFromWire(final DNSInput in) throws IOException {
    }
    
    void rdataFromString(final Tokenizer st, final Name origin) throws IOException {
    }
    
    String rrToString() {
        return "";
    }
    
    void rrToWire(final DNSOutput out, final Compression c, final boolean canonical) {
    }
}
