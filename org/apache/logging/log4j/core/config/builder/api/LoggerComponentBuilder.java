// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.config.builder.api;

public interface LoggerComponentBuilder extends ComponentBuilder<LoggerComponentBuilder>
{
    LoggerComponentBuilder add(final AppenderRefComponentBuilder p0);
    
    LoggerComponentBuilder add(final FilterComponentBuilder p0);
}
