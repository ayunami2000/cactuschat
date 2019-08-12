// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.util.Map;
import java.util.HashMap;

class Mnemonic
{
    private static Integer[] cachedInts;
    static final int CASE_SENSITIVE = 1;
    static final int CASE_UPPER = 2;
    static final int CASE_LOWER = 3;
    private HashMap strings;
    private HashMap values;
    private String description;
    private int wordcase;
    private String prefix;
    private int max;
    private boolean numericok;
    
    public Mnemonic(final String description, final int wordcase) {
        this.description = description;
        this.wordcase = wordcase;
        this.strings = new HashMap();
        this.values = new HashMap();
        this.max = Integer.MAX_VALUE;
    }
    
    public void setMaximum(final int max) {
        this.max = max;
    }
    
    public void setPrefix(final String prefix) {
        this.prefix = this.sanitize(prefix);
    }
    
    public void setNumericAllowed(final boolean numeric) {
        this.numericok = numeric;
    }
    
    public static Integer toInteger(final int val) {
        if (val >= 0 && val < Mnemonic.cachedInts.length) {
            return Mnemonic.cachedInts[val];
        }
        return new Integer(val);
    }
    
    public void check(final int val) {
        if (val < 0 || val > this.max) {
            throw new IllegalArgumentException(this.description + " " + val + "is out of range");
        }
    }
    
    private String sanitize(final String str) {
        if (this.wordcase == 2) {
            return str.toUpperCase();
        }
        if (this.wordcase == 3) {
            return str.toLowerCase();
        }
        return str;
    }
    
    private int parseNumeric(final String s) {
        try {
            final int val = Integer.parseInt(s);
            if (val >= 0 && val <= this.max) {
                return val;
            }
        }
        catch (NumberFormatException ex) {}
        return -1;
    }
    
    public void add(final int val, String str) {
        this.check(val);
        final Integer value = toInteger(val);
        str = this.sanitize(str);
        this.strings.put(str, value);
        this.values.put(value, str);
    }
    
    public void addAlias(final int val, String str) {
        this.check(val);
        final Integer value = toInteger(val);
        str = this.sanitize(str);
        this.strings.put(str, value);
    }
    
    public void addAll(final Mnemonic source) {
        if (this.wordcase != source.wordcase) {
            throw new IllegalArgumentException(source.description + ": wordcases do not match");
        }
        this.strings.putAll(source.strings);
        this.values.putAll(source.values);
    }
    
    public String getText(final int val) {
        this.check(val);
        String str = this.values.get(toInteger(val));
        if (str != null) {
            return str;
        }
        str = Integer.toString(val);
        if (this.prefix != null) {
            return this.prefix + str;
        }
        return str;
    }
    
    public int getValue(String str) {
        str = this.sanitize(str);
        final Integer value = this.strings.get(str);
        if (value != null) {
            return value;
        }
        if (this.prefix != null && str.startsWith(this.prefix)) {
            final int val = this.parseNumeric(str.substring(this.prefix.length()));
            if (val >= 0) {
                return val;
            }
        }
        if (this.numericok) {
            return this.parseNumeric(str);
        }
        return -1;
    }
    
    static {
        Mnemonic.cachedInts = new Integer[64];
        for (int i = 0; i < Mnemonic.cachedInts.length; ++i) {
            Mnemonic.cachedInts[i] = new Integer(i);
        }
    }
}
