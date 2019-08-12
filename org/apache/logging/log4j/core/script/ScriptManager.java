// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.script;

import javax.script.ScriptException;
import javax.script.CompiledScript;
import org.apache.logging.log4j.status.StatusLogger;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.script.Bindings;
import java.io.File;
import java.nio.file.Path;
import javax.script.ScriptEngine;
import java.util.Iterator;
import java.util.List;
import javax.script.Compilable;
import javax.script.ScriptEngineFactory;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.core.util.WatchManager;
import java.util.concurrent.ConcurrentMap;
import javax.script.ScriptEngineManager;
import org.apache.logging.log4j.Logger;
import java.io.Serializable;
import org.apache.logging.log4j.core.util.FileWatcher;

public class ScriptManager implements FileWatcher, Serializable
{
    private static final long serialVersionUID = -2534169384971965196L;
    private static final String KEY_THREADING = "THREADING";
    private static final Logger logger;
    private final ScriptEngineManager manager;
    private final ConcurrentMap<String, ScriptRunner> scripts;
    private final String languages;
    private final WatchManager watchManager;
    private static final SecurityManager SECURITY_MANAGER;
    
    public ScriptManager(final WatchManager watchManager) {
        this.manager = new ScriptEngineManager();
        this.scripts = new ConcurrentHashMap<String, ScriptRunner>();
        this.watchManager = watchManager;
        final List<ScriptEngineFactory> factories = this.manager.getEngineFactories();
        if (ScriptManager.logger.isDebugEnabled()) {
            final StringBuilder sb = new StringBuilder();
            ScriptManager.logger.debug("Installed script engines");
            for (final ScriptEngineFactory factory : factories) {
                String threading = (String)factory.getParameter("THREADING");
                if (threading == null) {
                    threading = "Not Thread Safe";
                }
                final StringBuilder names = new StringBuilder();
                for (final String name : factory.getNames()) {
                    if (names.length() > 0) {
                        names.append(", ");
                    }
                    names.append(name);
                }
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append((CharSequence)names);
                final boolean compiled = factory.getScriptEngine() instanceof Compilable;
                ScriptManager.logger.debug(factory.getEngineName() + " Version: " + factory.getEngineVersion() + ", Language: " + factory.getLanguageName() + ", Threading: " + threading + ", Compile: " + compiled + ", Names: {" + names.toString() + "}");
            }
            this.languages = sb.toString();
        }
        else {
            final StringBuilder names2 = new StringBuilder();
            for (final ScriptEngineFactory factory : factories) {
                for (final String name2 : factory.getNames()) {
                    if (names2.length() > 0) {
                        names2.append(", ");
                    }
                    names2.append(name2);
                }
            }
            this.languages = names2.toString();
        }
    }
    
    public void addScript(final AbstractScript script) {
        final ScriptEngine engine = this.manager.getEngineByName(script.getLanguage());
        if (engine == null) {
            ScriptManager.logger.error("No ScriptEngine found for language " + script.getLanguage() + ". Available languages are: " + this.languages);
            return;
        }
        if (engine.getFactory().getParameter("THREADING") == null) {
            this.scripts.put(script.getName(), new ThreadLocalScriptRunner(script));
        }
        else {
            this.scripts.put(script.getName(), new MainScriptRunner(engine, script));
        }
        if (script instanceof ScriptFile) {
            final ScriptFile scriptFile = (ScriptFile)script;
            final Path path = scriptFile.getPath();
            if (scriptFile.isWatched() && path != null) {
                this.watchManager.watchFile(path.toFile(), this);
            }
        }
    }
    
    public AbstractScript getScript(final String name) {
        final ScriptRunner runner = this.scripts.get(name);
        return (runner != null) ? runner.getScript() : null;
    }
    
    @Override
    public void fileModified(final File file) {
        final ScriptRunner runner = this.scripts.get(file.toString());
        if (runner == null) {
            ScriptManager.logger.info("{} is not a running script");
            return;
        }
        final ScriptEngine engine = runner.getScriptEngine();
        final AbstractScript script = runner.getScript();
        if (engine.getFactory().getParameter("THREADING") == null) {
            this.scripts.put(script.getName(), new ThreadLocalScriptRunner(script));
        }
        else {
            this.scripts.put(script.getName(), new MainScriptRunner(engine, script));
        }
    }
    
    public Object execute(final String name, final Bindings bindings) {
        final ScriptRunner scriptRunner = this.scripts.get(name);
        if (scriptRunner == null) {
            ScriptManager.logger.warn("No script named {} could be found");
            return null;
        }
        return AccessController.doPrivileged((PrivilegedAction<Object>)new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                return scriptRunner.execute(bindings);
            }
        });
    }
    
    static {
        logger = StatusLogger.getLogger();
        SECURITY_MANAGER = System.getSecurityManager();
    }
    
    private class MainScriptRunner implements ScriptRunner
    {
        private final AbstractScript script;
        private final CompiledScript compiledScript;
        private final ScriptEngine scriptEngine;
        
        public MainScriptRunner(final ScriptEngine scriptEngine, final AbstractScript script) {
            this.script = script;
            this.scriptEngine = scriptEngine;
            CompiledScript compiled = null;
            if (scriptEngine instanceof Compilable) {
                ScriptManager.logger.debug("Script {} is compilable", new Object[] { script.getName() });
                compiled = AccessController.doPrivileged((PrivilegedAction<CompiledScript>)new PrivilegedAction<CompiledScript>() {
                    @Override
                    public CompiledScript run() {
                        try {
                            return ((Compilable)scriptEngine).compile(script.getScriptText());
                        }
                        catch (Throwable ex) {
                            ScriptManager.logger.warn("Error compiling script", ex);
                            return null;
                        }
                    }
                });
            }
            this.compiledScript = compiled;
        }
        
        @Override
        public ScriptEngine getScriptEngine() {
            return this.scriptEngine;
        }
        
        @Override
        public Object execute(final Bindings bindings) {
            if (this.compiledScript != null) {
                try {
                    return this.compiledScript.eval(bindings);
                }
                catch (ScriptException ex) {
                    ScriptManager.logger.error("Error running script " + this.script.getName(), ex);
                    return null;
                }
            }
            try {
                return this.scriptEngine.eval(this.script.getScriptText(), bindings);
            }
            catch (ScriptException ex) {
                ScriptManager.logger.error("Error running script " + this.script.getName(), ex);
                return null;
            }
        }
        
        @Override
        public AbstractScript getScript() {
            return this.script;
        }
    }
    
    private class ThreadLocalScriptRunner implements ScriptRunner
    {
        private final AbstractScript script;
        private final ThreadLocal<MainScriptRunner> runners;
        
        public ThreadLocalScriptRunner(final AbstractScript script) {
            this.runners = new ThreadLocal<MainScriptRunner>() {
                @Override
                protected MainScriptRunner initialValue() {
                    final ScriptEngine engine = ScriptManager.this.manager.getEngineByName(ThreadLocalScriptRunner.this.script.getLanguage());
                    return new MainScriptRunner(engine, ThreadLocalScriptRunner.this.script);
                }
            };
            this.script = script;
        }
        
        @Override
        public Object execute(final Bindings bindings) {
            return this.runners.get().execute(bindings);
        }
        
        @Override
        public AbstractScript getScript() {
            return this.script;
        }
        
        @Override
        public ScriptEngine getScriptEngine() {
            return this.runners.get().getScriptEngine();
        }
    }
    
    private interface ScriptRunner
    {
        Object execute(final Bindings p0);
        
        AbstractScript getScript();
        
        ScriptEngine getScriptEngine();
    }
}
