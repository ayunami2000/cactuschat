// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.pattern;

import java.util.Iterator;
import java.util.Set;
import java.util.Map;
import java.util.Collection;
import java.util.TreeSet;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;

@Plugin(name = "MdcPatternConverter", category = "Converter")
@ConverterKeys({ "X", "mdc", "MDC" })
public final class MdcPatternConverter extends LogEventPatternConverter
{
    private final String key;
    private final String[] keys;
    private final boolean full;
    
    private MdcPatternConverter(final String[] options) {
        super((options != null && options.length > 0) ? ("MDC{" + options[0] + '}') : "MDC", "mdc");
        if (options != null && options.length > 0) {
            this.full = false;
            if (options[0].indexOf(44) > 0) {
                this.keys = options[0].split(",");
                this.key = null;
            }
            else {
                this.keys = null;
                this.key = options[0];
            }
        }
        else {
            this.full = true;
            this.key = null;
            this.keys = null;
        }
    }
    
    public static MdcPatternConverter newInstance(final String[] options) {
        return new MdcPatternConverter(options);
    }
    
    @Override
    public void format(final LogEvent event, final StringBuilder toAppendTo) {
        final Map<String, String> contextMap = event.getContextMap();
        if (this.full) {
            if (contextMap == null || contextMap.isEmpty()) {
                toAppendTo.append("{}");
                return;
            }
            final StringBuilder sb = new StringBuilder("{");
            final Set<String> eventKeys = new TreeSet<String>(contextMap.keySet());
            for (final String eventKey : eventKeys) {
                if (sb.length() > 1) {
                    sb.append(", ");
                }
                sb.append(eventKey).append('=').append(contextMap.get(eventKey));
            }
            sb.append('}');
            toAppendTo.append((CharSequence)sb);
        }
        else if (this.keys != null) {
            if (contextMap == null || contextMap.isEmpty()) {
                toAppendTo.append("{}");
                return;
            }
            final StringBuilder sb = new StringBuilder("{");
            for (String key : this.keys) {
                key = key.trim();
                if (contextMap.containsKey(key)) {
                    if (sb.length() > 1) {
                        sb.append(", ");
                    }
                    sb.append(key).append('=').append(contextMap.get(key));
                }
            }
            sb.append('}');
            toAppendTo.append((CharSequence)sb);
        }
        else if (contextMap != null) {
            final Object val = contextMap.get(this.key);
            if (val != null) {
                toAppendTo.append(val);
            }
        }
    }
}
