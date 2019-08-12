// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.config;

import org.apache.logging.log4j.Level;

public class NullConfiguration extends AbstractConfiguration
{
    public static final String NULL_NAME = "Null";
    private static final long serialVersionUID = 1L;
    
    public NullConfiguration() {
        super(ConfigurationSource.NULL_SOURCE);
        this.setName("Null");
        final LoggerConfig root = this.getRootLogger();
        root.setLevel(Level.OFF);
    }
}
