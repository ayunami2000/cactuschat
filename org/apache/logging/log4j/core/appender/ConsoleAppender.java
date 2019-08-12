// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.appender;

import java.io.IOException;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.core.util.Builder;
import java.lang.reflect.Constructor;
import org.apache.logging.log4j.core.util.Loader;
import org.apache.logging.log4j.util.PropertiesUtil;
import java.io.UnsupportedEncodingException;
import org.apache.logging.log4j.core.util.CloseShieldOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.io.OutputStream;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.util.Booleans;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.Filter;
import java.io.Serializable;
import org.apache.logging.log4j.core.Layout;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.core.config.plugins.Plugin;

@Plugin(name = "Console", category = "Core", elementType = "appender", printObject = true)
public final class ConsoleAppender extends AbstractOutputStreamAppender<OutputStreamManager>
{
    private static final long serialVersionUID = 1L;
    private static final String JANSI_CLASS = "org.fusesource.jansi.WindowsAnsiOutputStream";
    private static ConsoleManagerFactory factory;
    private static final Target DEFAULT_TARGET;
    private static final AtomicInteger COUNT;
    private final Target target;
    
    private ConsoleAppender(final String name, final Layout<? extends Serializable> layout, final Filter filter, final OutputStreamManager manager, final boolean ignoreExceptions, final Target target) {
        super(name, layout, filter, ignoreExceptions, true, manager);
        this.target = target;
    }
    
    @Deprecated
    public static ConsoleAppender createAppender(Layout<? extends Serializable> layout, final Filter filter, final String targetStr, final String name, final String follow, final String ignore) {
        if (name == null) {
            ConsoleAppender.LOGGER.error("No name provided for ConsoleAppender");
            return null;
        }
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }
        final boolean isFollow = Boolean.parseBoolean(follow);
        final boolean ignoreExceptions = Booleans.parseBoolean(ignore, true);
        final Target target = (targetStr == null) ? ConsoleAppender.DEFAULT_TARGET : Target.valueOf(targetStr);
        return new ConsoleAppender(name, layout, filter, getManager(target, isFollow, layout), ignoreExceptions, target);
    }
    
    @PluginFactory
    public static ConsoleAppender createAppender(@PluginElement("Layout") Layout<? extends Serializable> layout, @PluginElement("Filter") final Filter filter, @PluginAttribute("target") Target target, @PluginAttribute("name") final String name, @PluginAttribute(value = "follow", defaultBoolean = false) final boolean follow, @PluginAttribute(value = "ignoreExceptions", defaultBoolean = true) final boolean ignoreExceptions) {
        if (name == null) {
            ConsoleAppender.LOGGER.error("No name provided for ConsoleAppender");
            return null;
        }
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }
        target = ((target == null) ? Target.SYSTEM_OUT : target);
        return new ConsoleAppender(name, layout, filter, getManager(target, follow, layout), ignoreExceptions, target);
    }
    
    public static ConsoleAppender createDefaultAppenderForLayout(final Layout<? extends Serializable> layout) {
        return new ConsoleAppender("DefaultConsole-" + ConsoleAppender.COUNT.incrementAndGet(), layout, null, getDefaultManager(ConsoleAppender.DEFAULT_TARGET, false, layout), true, ConsoleAppender.DEFAULT_TARGET);
    }
    
    @PluginBuilderFactory
    public static Builder newBuilder() {
        return new Builder();
    }
    
    private static OutputStreamManager getDefaultManager(final Target target, final boolean follow, final Layout<? extends Serializable> layout) {
        final OutputStream os = getOutputStream(follow, target);
        final String managerName = target.name() + '.' + follow + "-" + ConsoleAppender.COUNT.get();
        return OutputStreamManager.getManager(managerName, new FactoryData(os, managerName, layout), ConsoleAppender.factory);
    }
    
    private static OutputStreamManager getManager(final Target target, final boolean follow, final Layout<? extends Serializable> layout) {
        final OutputStream os = getOutputStream(follow, target);
        final String managerName = target.name() + '.' + follow;
        return OutputStreamManager.getManager(managerName, new FactoryData(os, managerName, layout), ConsoleAppender.factory);
    }
    
    private static OutputStream getOutputStream(final boolean follow, final Target target) {
        final String enc = Charset.defaultCharset().name();
        OutputStream outputStream = null;
        try {
            OutputStream outputStream2;
            if (target == Target.SYSTEM_OUT) {
                if (follow) {
                    final SystemOutStream out;
                    outputStream2 = new PrintStream(out, true, enc);
                    out = new SystemOutStream();
                }
                else {
                    outputStream2 = System.out;
                }
            }
            else if (follow) {
                final SystemErrStream out2;
                outputStream2 = new PrintStream(out2, true, enc);
                out2 = new SystemErrStream();
            }
            else {
                outputStream2 = System.err;
            }
            outputStream = outputStream2;
            outputStream = new CloseShieldOutputStream(outputStream);
        }
        catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException("Unsupported default encoding " + enc, ex);
        }
        final PropertiesUtil propsUtil = PropertiesUtil.getProperties();
        if (!propsUtil.isOsWindows() || propsUtil.getBooleanProperty("log4j.skipJansi")) {
            return outputStream;
        }
        try {
            final Class<?> clazz = Loader.loadClass("org.fusesource.jansi.WindowsAnsiOutputStream");
            final Constructor<?> constructor = clazz.getConstructor(OutputStream.class);
            return new CloseShieldOutputStream((OutputStream)constructor.newInstance(outputStream));
        }
        catch (ClassNotFoundException cnfe) {
            ConsoleAppender.LOGGER.debug("Jansi is not installed, cannot find {}", new Object[] { "org.fusesource.jansi.WindowsAnsiOutputStream" });
        }
        catch (NoSuchMethodException nsme) {
            ConsoleAppender.LOGGER.warn("{} is missing the proper constructor", new Object[] { "org.fusesource.jansi.WindowsAnsiOutputStream" });
        }
        catch (Exception ex2) {
            ConsoleAppender.LOGGER.warn("Unable to instantiate {}", new Object[] { "org.fusesource.jansi.WindowsAnsiOutputStream" });
        }
        return outputStream;
    }
    
    public Target getTarget() {
        return this.target;
    }
    
    static {
        ConsoleAppender.factory = new ConsoleManagerFactory();
        DEFAULT_TARGET = Target.SYSTEM_OUT;
        COUNT = new AtomicInteger();
    }
    
    public enum Target
    {
        SYSTEM_OUT, 
        SYSTEM_ERR;
    }
    
    public static class Builder implements org.apache.logging.log4j.core.util.Builder<ConsoleAppender>
    {
        @PluginElement("Layout")
        @Required
        private Layout<? extends Serializable> layout;
        @PluginElement("Filter")
        private Filter filter;
        @PluginBuilderAttribute
        @Required
        private Target target;
        @PluginBuilderAttribute
        @Required
        private String name;
        @PluginBuilderAttribute
        private boolean follow;
        @PluginBuilderAttribute
        private boolean ignoreExceptions;
        
        public Builder() {
            this.layout = PatternLayout.createDefaultLayout();
            this.target = ConsoleAppender.DEFAULT_TARGET;
            this.follow = false;
            this.ignoreExceptions = true;
        }
        
        public Builder setLayout(final Layout<? extends Serializable> aLayout) {
            this.layout = aLayout;
            return this;
        }
        
        public Builder setFilter(final Filter aFilter) {
            this.filter = aFilter;
            return this;
        }
        
        public Builder setTarget(final Target aTarget) {
            this.target = aTarget;
            return this;
        }
        
        public Builder setName(final String aName) {
            this.name = aName;
            return this;
        }
        
        public Builder setFollow(final boolean shouldFollow) {
            this.follow = shouldFollow;
            return this;
        }
        
        public Builder setIgnoreExceptions(final boolean shouldIgnoreExceptions) {
            this.ignoreExceptions = shouldIgnoreExceptions;
            return this;
        }
        
        @Override
        public ConsoleAppender build() {
            return new ConsoleAppender(this.name, this.layout, this.filter, getManager(this.target, this.follow, this.layout), this.ignoreExceptions, this.target, null);
        }
    }
    
    private static class SystemErrStream extends OutputStream
    {
        public SystemErrStream() {
        }
        
        @Override
        public void close() {
        }
        
        @Override
        public void flush() {
            System.err.flush();
        }
        
        @Override
        public void write(final byte[] b) throws IOException {
            System.err.write(b);
        }
        
        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
            System.err.write(b, off, len);
        }
        
        @Override
        public void write(final int b) {
            System.err.write(b);
        }
    }
    
    private static class SystemOutStream extends OutputStream
    {
        public SystemOutStream() {
        }
        
        @Override
        public void close() {
        }
        
        @Override
        public void flush() {
            System.out.flush();
        }
        
        @Override
        public void write(final byte[] b) throws IOException {
            System.out.write(b);
        }
        
        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
            System.out.write(b, off, len);
        }
        
        @Override
        public void write(final int b) throws IOException {
            System.out.write(b);
        }
    }
    
    private static class FactoryData
    {
        private final OutputStream os;
        private final String name;
        private final Layout<? extends Serializable> layout;
        
        public FactoryData(final OutputStream os, final String type, final Layout<? extends Serializable> layout) {
            this.os = os;
            this.name = type;
            this.layout = layout;
        }
    }
    
    private static class ConsoleManagerFactory implements ManagerFactory<OutputStreamManager, FactoryData>
    {
        @Override
        public OutputStreamManager createManager(final String name, final FactoryData data) {
            return new OutputStreamManager(data.os, data.name, data.layout, true);
        }
    }
}
