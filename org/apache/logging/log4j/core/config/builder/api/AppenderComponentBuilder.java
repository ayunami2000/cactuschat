// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.config.builder.api;

public interface AppenderComponentBuilder extends ComponentBuilder<AppenderComponentBuilder>
{
    AppenderComponentBuilder add(final LayoutComponentBuilder p0);
    
    AppenderComponentBuilder add(final FilterComponentBuilder p0);
    
    String getName();
}
