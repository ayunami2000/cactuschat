// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.util;

import javax.naming.NamingException;
import javax.naming.Context;

public final class JndiCloser
{
    private JndiCloser() {
    }
    
    public static void close(final Context context) throws NamingException {
        if (context != null) {
            context.close();
        }
    }
    
    public static void closeSilently(final Context context) {
        try {
            close(context);
        }
        catch (NamingException ex) {}
    }
}
