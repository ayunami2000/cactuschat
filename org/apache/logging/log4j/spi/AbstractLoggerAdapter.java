// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.spi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.util.LoaderUtil;
import java.util.concurrent.ConcurrentHashMap;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.Map;

public abstract class AbstractLoggerAdapter<L> implements LoggerAdapter<L>
{
    protected final Map<LoggerContext, ConcurrentMap<String, L>> registry;
    
    public AbstractLoggerAdapter() {
        this.registry = new WeakHashMap<LoggerContext, ConcurrentMap<String, L>>();
    }
    
    @Override
    public L getLogger(final String name) {
        final LoggerContext context = this.getContext();
        final ConcurrentMap<String, L> loggers = this.getLoggersInContext(context);
        final L logger = loggers.get(name);
        if (logger != null) {
            return logger;
        }
        loggers.putIfAbsent(name, this.newLogger(name, context));
        return loggers.get(name);
    }
    
    public ConcurrentMap<String, L> getLoggersInContext(final LoggerContext context) {
        synchronized (this.registry) {
            ConcurrentMap<String, L> loggers = this.registry.get(context);
            if (loggers == null) {
                loggers = new ConcurrentHashMap<String, L>();
                this.registry.put(context, loggers);
            }
            return loggers;
        }
    }
    
    protected abstract L newLogger(final String p0, final LoggerContext p1);
    
    protected abstract LoggerContext getContext();
    
    protected LoggerContext getContext(final Class<?> callerClass) {
        ClassLoader cl = null;
        if (callerClass != null) {
            cl = callerClass.getClassLoader();
        }
        if (cl == null) {
            cl = LoaderUtil.getThreadContextClassLoader();
        }
        return LogManager.getContext(cl, false);
    }
    
    @Override
    public void close() {
        this.registry.clear();
    }
}
