// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.config;

import java.util.Map;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import java.io.Serializable;
import org.apache.logging.log4j.core.net.Advertiser;

@Plugin(name = "default", category = "Core", elementType = "advertiser", printObject = false)
public class DefaultAdvertiser implements Advertiser, Serializable
{
    private static final long serialVersionUID = 1L;
    
    @Override
    public Object advertise(final Map<String, String> properties) {
        return null;
    }
    
    @Override
    public void unadvertise(final Object advertisedObject) {
    }
}
