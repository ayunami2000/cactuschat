// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec.http.websocketx.extensions;

import java.util.Collections;
import java.util.Map;

public final class WebSocketExtensionData
{
    private final String name;
    private final Map<String, String> parameters;
    
    public WebSocketExtensionData(final String name, final Map<String, String> parameters) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        if (parameters == null) {
            throw new NullPointerException("parameters");
        }
        this.name = name;
        this.parameters = Collections.unmodifiableMap((Map<? extends String, ? extends String>)parameters);
    }
    
    public String name() {
        return this.name;
    }
    
    public Map<String, String> parameters() {
        return this.parameters;
    }
}
