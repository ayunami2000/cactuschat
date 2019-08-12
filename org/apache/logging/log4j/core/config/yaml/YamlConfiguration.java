// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.config.yaml;

import java.io.IOException;
import org.apache.logging.log4j.core.config.Configuration;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.json.JsonConfiguration;

public class YamlConfiguration extends JsonConfiguration
{
    private static final long serialVersionUID = 1L;
    
    public YamlConfiguration(final ConfigurationSource configSource) {
        super(configSource);
    }
    
    @Override
    protected ObjectMapper getObjectMapper() {
        return new ObjectMapper((JsonFactory)new YAMLFactory()).configure(JsonParser.Feature.ALLOW_COMMENTS, true);
    }
    
    @Override
    public Configuration reconfigure() {
        try {
            final ConfigurationSource source = this.getConfigurationSource().resetInputStream();
            if (source == null) {
                return null;
            }
            return new YamlConfiguration(source);
        }
        catch (IOException ex) {
            YamlConfiguration.LOGGER.error("Cannot locate file {}", new Object[] { this.getConfigurationSource(), ex });
            return null;
        }
    }
}
