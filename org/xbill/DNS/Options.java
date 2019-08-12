// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Map;

public final class Options
{
    private static Map table;
    
    private Options() {
    }
    
    public static void refresh() {
        final String s = System.getProperty("dnsjava.options");
        if (s != null) {
            final StringTokenizer st = new StringTokenizer(s, ",");
            while (st.hasMoreTokens()) {
                final String token = st.nextToken();
                final int index = token.indexOf(61);
                if (index == -1) {
                    set(token);
                }
                else {
                    final String option = token.substring(0, index);
                    final String value = token.substring(index + 1);
                    set(option, value);
                }
            }
        }
    }
    
    public static void clear() {
        Options.table = null;
    }
    
    public static void set(final String option) {
        if (Options.table == null) {
            Options.table = new HashMap();
        }
        Options.table.put(option.toLowerCase(), "true");
    }
    
    public static void set(final String option, final String value) {
        if (Options.table == null) {
            Options.table = new HashMap();
        }
        Options.table.put(option.toLowerCase(), value.toLowerCase());
    }
    
    public static void unset(final String option) {
        if (Options.table == null) {
            return;
        }
        Options.table.remove(option.toLowerCase());
    }
    
    public static boolean check(final String option) {
        return Options.table != null && Options.table.get(option.toLowerCase()) != null;
    }
    
    public static String value(final String option) {
        if (Options.table == null) {
            return null;
        }
        return Options.table.get(option.toLowerCase());
    }
    
    public static int intValue(final String option) {
        final String s = value(option);
        if (s != null) {
            try {
                final int val = Integer.parseInt(s);
                if (val > 0) {
                    return val;
                }
            }
            catch (NumberFormatException ex) {}
        }
        return -1;
    }
    
    static {
        try {
            refresh();
        }
        catch (SecurityException ex) {}
    }
}
