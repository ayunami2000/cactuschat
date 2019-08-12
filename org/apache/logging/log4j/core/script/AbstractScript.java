// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.script;

public abstract class AbstractScript
{
    protected static final String DEFAULT_LANGUAGE = "JavaScript";
    private final String language;
    private final String scriptText;
    private final String name;
    
    public AbstractScript(final String name, final String language, final String scriptText) {
        this.language = language;
        this.scriptText = scriptText;
        this.name = ((name == null) ? this.toString() : name);
    }
    
    public String getLanguage() {
        return this.language;
    }
    
    public String getScriptText() {
        return this.scriptText;
    }
    
    public String getName() {
        return this.name;
    }
}
