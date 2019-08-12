// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.util;

import java.util.Map;

public final class StringBuilders
{
    private StringBuilders() {
    }
    
    public static StringBuilder appendDqValue(final StringBuilder sb, final Object value) {
        return sb.append('\"').append(value).append('\"');
    }
    
    public static StringBuilder appendKeyDqValue(final StringBuilder sb, final Map.Entry<String, String> entry) {
        return appendKeyDqValue(sb, entry.getKey(), entry.getValue());
    }
    
    public static StringBuilder appendKeyDqValue(final StringBuilder sb, final String key, final Object value) {
        return sb.append(key).append('=').append('\"').append(value).append('\"');
    }
}
