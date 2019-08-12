// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.appender;

import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.appender.rolling.action.Action;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.util.Integers;
import org.apache.logging.log4j.core.util.Booleans;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.appender.rolling.RolloverStrategy;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.appender.rolling.TriggeringPolicy;
import org.apache.logging.log4j.core.LogEvent;
import java.util.Map;
import java.util.HashMap;
import org.apache.logging.log4j.core.Filter;
import java.io.Serializable;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.net.Advertiser;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.appender.rolling.RollingFileManager;

@Plugin(name = "RollingFile", category = "Core", elementType = "appender", printObject = true)
public final class RollingFileAppender extends AbstractOutputStreamAppender<RollingFileManager>
{
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private static final long serialVersionUID = 1L;
    private final String fileName;
    private final String filePattern;
    private Object advertisement;
    private final Advertiser advertiser;
    
    private RollingFileAppender(final String name, final Layout<? extends Serializable> layout, final Filter filter, final RollingFileManager manager, final String fileName, final String filePattern, final boolean ignoreExceptions, final boolean immediateFlush, final Advertiser advertiser) {
        super(name, layout, filter, ignoreExceptions, immediateFlush, manager);
        if (advertiser != null) {
            final Map<String, String> configuration = new HashMap<String, String>(layout.getContentFormat());
            configuration.put("contentType", layout.getContentType());
            configuration.put("name", name);
            this.advertisement = advertiser.advertise(configuration);
        }
        this.fileName = fileName;
        this.filePattern = filePattern;
        this.advertiser = advertiser;
    }
    
    @Override
    public void stop() {
        super.stop();
        if (this.advertiser != null) {
            this.advertiser.unadvertise(this.advertisement);
        }
    }
    
    @Override
    public void append(final LogEvent event) {
        this.getManager().checkRollover(event);
        super.append(event);
    }
    
    public String getFileName() {
        return this.fileName;
    }
    
    public String getFilePattern() {
        return this.filePattern;
    }
    
    public <T extends TriggeringPolicy> T getTriggeringPolicy() {
        return this.getManager().getTriggeringPolicy();
    }
    
    @PluginFactory
    public static RollingFileAppender createAppender(@PluginAttribute("fileName") final String fileName, @PluginAttribute("filePattern") final String filePattern, @PluginAttribute("append") final String append, @PluginAttribute("name") final String name, @PluginAttribute("bufferedIO") final String bufferedIO, @PluginAttribute("bufferSize") final String bufferSizeStr, @PluginAttribute("immediateFlush") final String immediateFlush, @PluginElement("Policy") final TriggeringPolicy policy, @PluginElement("Strategy") RolloverStrategy strategy, @PluginElement("Layout") Layout<? extends Serializable> layout, @PluginElement("Filter") final Filter filter, @PluginAttribute("ignoreExceptions") final String ignore, @PluginAttribute("advertise") final String advertise, @PluginAttribute("advertiseURI") final String advertiseURI, @PluginConfiguration final Configuration config) {
        final boolean isAppend = Booleans.parseBoolean(append, true);
        final boolean ignoreExceptions = Booleans.parseBoolean(ignore, true);
        final boolean isBuffered = Booleans.parseBoolean(bufferedIO, true);
        final boolean isFlush = Booleans.parseBoolean(immediateFlush, true);
        final boolean isAdvertise = Boolean.parseBoolean(advertise);
        final int bufferSize = Integers.parseInt(bufferSizeStr, 8192);
        if (!isBuffered && bufferSize > 0) {
            RollingFileAppender.LOGGER.warn("The bufferSize is set to {} but bufferedIO is not true: {}", new Object[] { bufferSize, bufferedIO });
        }
        if (name == null) {
            RollingFileAppender.LOGGER.error("No name provided for FileAppender");
            return null;
        }
        if (fileName == null) {
            RollingFileAppender.LOGGER.error("No filename was provided for FileAppender with name " + name);
            return null;
        }
        if (filePattern == null) {
            RollingFileAppender.LOGGER.error("No filename pattern provided for FileAppender with name " + name);
            return null;
        }
        if (policy == null) {
            RollingFileAppender.LOGGER.error("A TriggeringPolicy must be provided");
            return null;
        }
        if (strategy == null) {
            strategy = DefaultRolloverStrategy.createStrategy(null, null, null, String.valueOf(-1), null, true, config);
        }
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }
        final RollingFileManager manager = RollingFileManager.getFileManager(fileName, filePattern, isAppend, isBuffered, policy, strategy, advertiseURI, layout, bufferSize);
        if (manager == null) {
            return null;
        }
        return new RollingFileAppender(name, layout, filter, manager, fileName, filePattern, ignoreExceptions, isFlush, isAdvertise ? config.getAdvertiser() : null);
    }
}
