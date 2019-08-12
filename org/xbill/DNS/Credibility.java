// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

public final class Credibility
{
    public static final int HINT = 0;
    public static final int ADDITIONAL = 1;
    public static final int GLUE = 2;
    public static final int NONAUTH_AUTHORITY = 3;
    public static final int NONAUTH_ANSWER = 3;
    public static final int AUTH_AUTHORITY = 4;
    public static final int AUTH_ANSWER = 4;
    public static final int ZONE = 5;
    public static final int NORMAL = 3;
    public static final int ANY = 1;
    
    private Credibility() {
    }
}
