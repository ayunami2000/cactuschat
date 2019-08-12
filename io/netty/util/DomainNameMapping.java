// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.util;

import io.netty.util.internal.StringUtil;
import java.util.Iterator;
import java.util.Locale;
import java.net.IDN;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class DomainNameMapping<V> implements Mapping<String, V>
{
    private static final Pattern DNS_WILDCARD_PATTERN;
    private final Map<String, V> map;
    private final V defaultValue;
    
    public DomainNameMapping(final V defaultValue) {
        this(4, defaultValue);
    }
    
    public DomainNameMapping(final int initialCapacity, final V defaultValue) {
        if (defaultValue == null) {
            throw new NullPointerException("defaultValue");
        }
        this.map = new LinkedHashMap<String, V>(initialCapacity);
        this.defaultValue = defaultValue;
    }
    
    public DomainNameMapping<V> add(final String hostname, final V output) {
        if (hostname == null) {
            throw new NullPointerException("input");
        }
        if (output == null) {
            throw new NullPointerException("output");
        }
        this.map.put(normalizeHostname(hostname), output);
        return this;
    }
    
    private static boolean matches(final String hostNameTemplate, final String hostName) {
        if (DomainNameMapping.DNS_WILDCARD_PATTERN.matcher(hostNameTemplate).matches()) {
            return hostNameTemplate.substring(2).equals(hostName) || hostName.endsWith(hostNameTemplate.substring(1));
        }
        return hostNameTemplate.equals(hostName);
    }
    
    private static String normalizeHostname(String hostname) {
        if (needsNormalization(hostname)) {
            hostname = IDN.toASCII(hostname, 1);
        }
        return hostname.toLowerCase(Locale.US);
    }
    
    private static boolean needsNormalization(final String hostname) {
        for (int length = hostname.length(), i = 0; i < length; ++i) {
            final int c = hostname.charAt(i);
            if (c > 127) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public V map(String input) {
        if (input != null) {
            input = normalizeHostname(input);
            for (final Map.Entry<String, V> entry : this.map.entrySet()) {
                if (matches(entry.getKey(), input)) {
                    return entry.getValue();
                }
            }
        }
        return this.defaultValue;
    }
    
    @Override
    public String toString() {
        return StringUtil.simpleClassName(this) + "(default: " + this.defaultValue + ", map: " + this.map + ')';
    }
    
    static {
        DNS_WILDCARD_PATTERN = Pattern.compile("^\\*\\..*");
    }
}
