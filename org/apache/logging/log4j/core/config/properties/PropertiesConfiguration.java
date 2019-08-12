// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.config.properties;

import java.io.IOException;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.builder.api.Component;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Reconfigurable;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;

public class PropertiesConfiguration extends BuiltConfiguration implements Reconfigurable
{
    private static final long serialVersionUID = 5198216024278070407L;
    
    public PropertiesConfiguration(final ConfigurationSource source, final Component root) {
        super(source, root);
    }
    
    @Override
    public Configuration reconfigure() {
        try {
            final ConfigurationSource source = this.getConfigurationSource().resetInputStream();
            if (source == null) {
                return null;
            }
            final PropertiesConfigurationFactory factory = new PropertiesConfigurationFactory();
            final PropertiesConfiguration config = factory.getConfiguration(source);
            return (config.root.getComponents().size() == 0) ? null : config;
        }
        catch (IOException ex) {
            PropertiesConfiguration.LOGGER.error("Cannot locate file {}: {}", new Object[] { this.getConfigurationSource(), ex });
            return null;
        }
    }
}
