// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

public class InvalidDClassException extends IllegalArgumentException
{
    public InvalidDClassException(final int dclass) {
        super("Invalid DNS class: " + dclass);
    }
}
