// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.appender.rolling;

import java.util.Arrays;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;

@Plugin(name = "Policies", category = "Core", printObject = true)
public final class CompositeTriggeringPolicy implements TriggeringPolicy
{
    private final TriggeringPolicy[] triggeringPolicy;
    
    private CompositeTriggeringPolicy(final TriggeringPolicy... policies) {
        this.triggeringPolicy = policies;
    }
    
    public TriggeringPolicy[] getTriggeringPolicies() {
        return this.triggeringPolicy;
    }
    
    @Override
    public void initialize(final RollingFileManager manager) {
        for (final TriggeringPolicy policy : this.triggeringPolicy) {
            policy.initialize(manager);
        }
    }
    
    @Override
    public boolean isTriggeringEvent(final LogEvent event) {
        for (final TriggeringPolicy policy : this.triggeringPolicy) {
            if (policy.isTriggeringEvent(event)) {
                return true;
            }
        }
        return false;
    }
    
    @PluginFactory
    public static CompositeTriggeringPolicy createPolicy(@PluginElement("Policies") final TriggeringPolicy... policies) {
        return new CompositeTriggeringPolicy(policies);
    }
    
    @Override
    public String toString() {
        return "CompositeTriggeringPolicy(policies=" + Arrays.toString(this.triggeringPolicy) + ")";
    }
}
