// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.appender.rolling;

import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.LogEvent;
import java.lang.reflect.Method;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.core.util.Loader;
import org.apache.logging.log4j.core.config.plugins.Plugin;

@Plugin(name = "OnStartupTriggeringPolicy", category = "Core", printObject = true)
public class OnStartupTriggeringPolicy implements TriggeringPolicy
{
    private static long JVM_START_TIME;
    private boolean evaluated;
    private RollingFileManager manager;
    
    public OnStartupTriggeringPolicy() {
        this.evaluated = false;
    }
    
    @Override
    public void initialize(final RollingFileManager manager) {
        this.manager = manager;
        if (OnStartupTriggeringPolicy.JVM_START_TIME == 0L) {
            this.evaluated = true;
        }
    }
    
    private static long initStartTime() {
        try {
            final Class<?> factoryClass = Loader.loadSystemClass("java.lang.management.ManagementFactory");
            final Method getRuntimeMXBean = factoryClass.getMethod("getRuntimeMXBean", (Class<?>[])new Class[0]);
            final Object runtimeMXBean = getRuntimeMXBean.invoke(null, new Object[0]);
            final Class<?> runtimeMXBeanClass = Loader.loadSystemClass("java.lang.management.RuntimeMXBean");
            final Method getStartTime = runtimeMXBeanClass.getMethod("getStartTime", (Class<?>[])new Class[0]);
            final Long result = (Long)getStartTime.invoke(runtimeMXBean, new Object[0]);
            return result;
        }
        catch (Throwable t) {
            StatusLogger.getLogger().error("Unable to call ManagementFactory.getRuntimeMXBean().getStartTime(), using system time for OnStartupTriggeringPolicy", t);
            return System.currentTimeMillis();
        }
    }
    
    @Override
    public boolean isTriggeringEvent(final LogEvent event) {
        if (this.evaluated) {
            return false;
        }
        this.evaluated = true;
        return this.manager.getFileTime() < OnStartupTriggeringPolicy.JVM_START_TIME;
    }
    
    @Override
    public String toString() {
        return "OnStartupTriggeringPolicy";
    }
    
    @PluginFactory
    public static OnStartupTriggeringPolicy createPolicy() {
        return new OnStartupTriggeringPolicy();
    }
    
    static {
        OnStartupTriggeringPolicy.JVM_START_TIME = initStartTime();
    }
}
