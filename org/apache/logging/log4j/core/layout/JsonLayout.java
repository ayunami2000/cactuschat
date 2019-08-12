// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.layout;

import org.apache.logging.log4j.core.LogEvent;
import java.nio.charset.StandardCharsets;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import java.util.HashMap;
import java.util.Map;
import java.nio.charset.Charset;
import org.apache.logging.log4j.core.config.plugins.Plugin;

@Plugin(name = "JsonLayout", category = "Core", elementType = "layout", printObject = true)
public final class JsonLayout extends AbstractJacksonLayout
{
    static final String CONTENT_TYPE = "application/json";
    private static final long serialVersionUID = 1L;
    
    protected JsonLayout(final boolean locationInfo, final boolean properties, final boolean complete, final boolean compact, final boolean eventEol, final Charset charset) {
        super(new JacksonFactory.JSON().newWriter(locationInfo, properties, compact), charset, compact, complete, eventEol);
    }
    
    @Override
    public byte[] getHeader() {
        if (!this.complete) {
            return null;
        }
        final StringBuilder buf = new StringBuilder();
        buf.append('[');
        buf.append(this.eol);
        return this.getBytes(buf.toString());
    }
    
    @Override
    public byte[] getFooter() {
        if (!this.complete) {
            return null;
        }
        return this.getBytes(this.eol + ']' + this.eol);
    }
    
    @Override
    public Map<String, String> getContentFormat() {
        final Map<String, String> result = new HashMap<String, String>();
        result.put("version", "2.0");
        return result;
    }
    
    @Override
    public String getContentType() {
        return "application/json; charset=" + this.getCharset();
    }
    
    @PluginFactory
    public static AbstractJacksonLayout createLayout(@PluginAttribute(value = "locationInfo", defaultBoolean = false) final boolean locationInfo, @PluginAttribute(value = "properties", defaultBoolean = false) final boolean properties, @PluginAttribute(value = "complete", defaultBoolean = false) final boolean complete, @PluginAttribute(value = "compact", defaultBoolean = false) final boolean compact, @PluginAttribute(value = "eventEol", defaultBoolean = false) final boolean eventEol, @PluginAttribute(value = "charset", defaultString = "UTF-8") final Charset charset) {
        return new JsonLayout(locationInfo, properties, complete, compact, eventEol, charset);
    }
    
    public static AbstractJacksonLayout createDefaultLayout() {
        return new JsonLayout(false, false, false, false, false, StandardCharsets.UTF_8);
    }
}
