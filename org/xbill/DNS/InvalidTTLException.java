// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

public class InvalidTTLException extends IllegalArgumentException
{
    public InvalidTTLException(final long ttl) {
        super("Invalid DNS TTL: " + ttl);
    }
}
