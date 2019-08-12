// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.config;

public class DefaultConfiguration extends AbstractConfiguration
{
    public static final String DEFAULT_NAME = "Default";
    public static final String DEFAULT_LEVEL = "org.apache.logging.log4j.level";
    public static final String DEFAULT_PATTERN = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n";
    private static final long serialVersionUID = 1L;
    
    public DefaultConfiguration() {
        super(ConfigurationSource.NULL_SOURCE);
        this.setToDefault();
    }
    
    @Override
    protected void doConfigure() {
    }
}
