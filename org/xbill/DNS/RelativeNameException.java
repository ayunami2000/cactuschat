// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

public class RelativeNameException extends IllegalArgumentException
{
    public RelativeNameException(final Name name) {
        super("'" + name + "' is not an absolute name");
    }
    
    public RelativeNameException(final String s) {
        super(s);
    }
}
