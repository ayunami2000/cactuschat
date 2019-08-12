// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

public class InvalidTypeException extends IllegalArgumentException
{
    public InvalidTypeException(final int type) {
        super("Invalid DNS type: " + type);
    }
}
