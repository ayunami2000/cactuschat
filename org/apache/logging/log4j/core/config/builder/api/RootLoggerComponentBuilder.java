// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.config.builder.api;

public interface RootLoggerComponentBuilder extends ComponentBuilder<RootLoggerComponentBuilder>
{
    RootLoggerComponentBuilder add(final AppenderRefComponentBuilder p0);
    
    RootLoggerComponentBuilder add(final FilterComponentBuilder p0);
}
