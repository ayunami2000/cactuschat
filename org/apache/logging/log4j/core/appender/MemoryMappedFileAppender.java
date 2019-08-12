// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.appender;

import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.util.Integers;
import org.apache.logging.log4j.core.util.Booleans;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.LogEvent;
import java.util.Map;
import java.util.HashMap;
import org.apache.logging.log4j.core.Filter;
import java.io.Serializable;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.net.Advertiser;
import org.apache.logging.log4j.core.config.plugins.Plugin;

@Plugin(name = "MemoryMappedFile", category = "Core", elementType = "appender", printObject = true)
public final class MemoryMappedFileAppender extends AbstractOutputStreamAppender<MemoryMappedFileManager>
{
    private static final long serialVersionUID = 1L;
    private static final int BIT_POSITION_1GB = 30;
    private static final int MAX_REGION_LENGTH = 1073741824;
    private static final int MIN_REGION_LENGTH = 256;
    private final String fileName;
    private Object advertisement;
    private final Advertiser advertiser;
    
    private MemoryMappedFileAppender(final String name, final Layout<? extends Serializable> layout, final Filter filter, final MemoryMappedFileManager manager, final String filename, final boolean ignoreExceptions, final boolean immediateFlush, final Advertiser advertiser) {
        super(name, layout, filter, ignoreExceptions, immediateFlush, manager);
        if (advertiser != null) {
            final Map<String, String> configuration = new HashMap<String, String>(layout.getContentFormat());
            configuration.putAll(manager.getContentFormat());
            configuration.put("contentType", layout.getContentType());
            configuration.put("name", name);
            this.advertisement = advertiser.advertise(configuration);
        }
        this.fileName = filename;
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
        this.getManager().setEndOfBatch(event.isEndOfBatch());
        super.append(event);
    }
    
    public String getFileName() {
        return this.fileName;
    }
    
    public int getRegionLength() {
        return this.getManager().getRegionLength();
    }
    
    @PluginFactory
    public static MemoryMappedFileAppender createAppender(@PluginAttribute("fileName") final String fileName, @PluginAttribute("append") final String append, @PluginAttribute("name") final String name, @PluginAttribute("immediateFlush") final String immediateFlush, @PluginAttribute("regionLength") final String regionLengthStr, @PluginAttribute("ignoreExceptions") final String ignore, @PluginElement("Layout") Layout<? extends Serializable> layout, @PluginElement("Filter") final Filter filter, @PluginAttribute("advertise") final String advertise, @PluginAttribute("advertiseURI") final String advertiseURI, @PluginConfiguration final Configuration config) {
        final boolean isAppend = Booleans.parseBoolean(append, true);
        final boolean isForce = Booleans.parseBoolean(immediateFlush, false);
        final boolean ignoreExceptions = Booleans.parseBoolean(ignore, true);
        final boolean isAdvertise = Boolean.parseBoolean(advertise);
        final int regionLength = Integers.parseInt(regionLengthStr, 33554432);
        final int actualRegionLength = determineValidRegionLength(name, regionLength);
        if (name == null) {
            MemoryMappedFileAppender.LOGGER.error("No name provided for MemoryMappedFileAppender");
            return null;
        }
        if (fileName == null) {
            MemoryMappedFileAppender.LOGGER.error("No filename provided for MemoryMappedFileAppender with name " + name);
            return null;
        }
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }
        final MemoryMappedFileManager manager = MemoryMappedFileManager.getFileManager(fileName, isAppend, isForce, actualRegionLength, advertiseURI, layout);
        if (manager == null) {
            return null;
        }
        return new MemoryMappedFileAppender(name, layout, filter, manager, fileName, ignoreExceptions, isForce, isAdvertise ? config.getAdvertiser() : null);
    }
    
    private static int determineValidRegionLength(final String name, final int regionLength) {
        if (regionLength > 1073741824) {
            MemoryMappedFileAppender.LOGGER.info("MemoryMappedAppender[{}] Reduced region length from {} to max length: {}", new Object[] { name, regionLength, 1073741824 });
            return 1073741824;
        }
        if (regionLength < 256) {
            MemoryMappedFileAppender.LOGGER.info("MemoryMappedAppender[{}] Expanded region length from {} to min length: {}", new Object[] { name, regionLength, 256 });
            return 256;
        }
        final int result = Integers.ceilingNextPowerOfTwo(regionLength);
        if (regionLength != result) {
            MemoryMappedFileAppender.LOGGER.info("MemoryMappedAppender[{}] Rounded up region length from {} to next power of two: {}", new Object[] { name, regionLength, result });
        }
        return result;
    }
}
