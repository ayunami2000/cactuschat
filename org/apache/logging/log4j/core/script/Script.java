// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.script;

import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.config.plugins.PluginValue;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.plugins.Plugin;

@Plugin(name = "Script", category = "Core", printObject = true)
public class Script extends AbstractScript
{
    private static final Logger logger;
    
    public Script(final String name, final String language, final String scriptText) {
        super(name, language, scriptText);
    }
    
    @PluginFactory
    public static Script createScript(@PluginAttribute("name") final String name, @PluginAttribute("language") String language, @PluginValue("scriptText") final String scriptText) {
        if (language == null) {
            Script.logger.info("No script language supplied, defaulting to {}", new Object[] { "JavaScript" });
            language = "JavaScript";
        }
        if (scriptText == null) {
            Script.logger.error("No scriptText attribute provided for ScriptFile {}", new Object[] { name });
            return null;
        }
        return new Script(name, language, scriptText);
    }
    
    @Override
    public String toString() {
        return (this.getName() != null) ? this.getName() : super.toString();
    }
    
    static {
        logger = StatusLogger.getLogger();
    }
}
