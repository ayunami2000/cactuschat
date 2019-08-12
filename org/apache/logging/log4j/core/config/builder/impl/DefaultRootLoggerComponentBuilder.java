// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.config.builder.impl;

import org.apache.logging.log4j.core.config.builder.api.FilterComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.AppenderRefComponentBuilder;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.builder.api.RootLoggerComponentBuilder;

class DefaultRootLoggerComponentBuilder extends DefaultComponentAndConfigurationBuilder<RootLoggerComponentBuilder> implements RootLoggerComponentBuilder
{
    public DefaultRootLoggerComponentBuilder(final DefaultConfigurationBuilder<? extends Configuration> builder, final String level) {
        super(builder, "", "Root");
        this.addAttribute("level", level);
    }
    
    public DefaultRootLoggerComponentBuilder(final DefaultConfigurationBuilder<? extends Configuration> builder, final String level, final String type) {
        super(builder, "", type);
        this.addAttribute("level", level);
    }
    
    @Override
    public RootLoggerComponentBuilder add(final AppenderRefComponentBuilder builder) {
        return ((DefaultComponentBuilder<RootLoggerComponentBuilder, CB>)this).addComponent(builder);
    }
    
    @Override
    public RootLoggerComponentBuilder add(final FilterComponentBuilder builder) {
        return ((DefaultComponentBuilder<RootLoggerComponentBuilder, CB>)this).addComponent(builder);
    }
}
