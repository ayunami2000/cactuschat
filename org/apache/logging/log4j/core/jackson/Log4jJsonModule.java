// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.jackson;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;

class Log4jJsonModule extends SimpleModule
{
    private static final long serialVersionUID = 1L;
    
    Log4jJsonModule() {
        super(Log4jJsonModule.class.getName(), new Version(2, 0, 0, (String)null, (String)null, (String)null));
        new Initializers.SimpleModuleInitializer().initialize(this);
    }
    
    public void setupModule(final Module.SetupContext context) {
        super.setupModule(context);
        new Initializers.SetupContextInitializer().setupModule(context);
    }
}
