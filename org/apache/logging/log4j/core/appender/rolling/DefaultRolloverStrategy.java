// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.appender.rolling;

import org.apache.logging.log4j.core.appender.rolling.action.CommonsCompressAction;
import org.apache.logging.log4j.core.appender.rolling.action.GzCompressAction;
import org.apache.logging.log4j.core.appender.rolling.action.ZipCompressAction;
import java.util.Objects;
import org.apache.logging.log4j.status.StatusLogger;
import java.util.Collection;
import org.apache.logging.log4j.core.appender.rolling.action.CompositeAction;
import java.util.concurrent.TimeUnit;
import java.io.File;
import org.apache.logging.log4j.core.appender.rolling.action.FileRenameAction;
import java.util.ArrayList;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.util.Integers;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import java.util.Arrays;
import java.util.Collections;
import org.apache.logging.log4j.core.appender.rolling.action.Action;
import java.util.List;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.plugins.Plugin;

@Plugin(name = "DefaultRolloverStrategy", category = "Core", printObject = true)
public class DefaultRolloverStrategy implements RolloverStrategy
{
    protected static final Logger LOGGER;
    private static final int MIN_WINDOW_SIZE = 1;
    private static final int DEFAULT_WINDOW_SIZE = 7;
    private final int maxIndex;
    private final int minIndex;
    private final boolean useMax;
    private final StrSubstitutor subst;
    private final int compressionLevel;
    private List<Action> customActions;
    private boolean stopCustomActionsOnError;
    
    protected DefaultRolloverStrategy(final int minIndex, final int maxIndex, final boolean useMax, final int compressionLevel, final StrSubstitutor subst, final Action[] customActions, final boolean stopCustomActionsOnError) {
        this.minIndex = minIndex;
        this.maxIndex = maxIndex;
        this.useMax = useMax;
        this.compressionLevel = compressionLevel;
        this.subst = subst;
        this.stopCustomActionsOnError = stopCustomActionsOnError;
        this.customActions = ((customActions == null) ? Collections.emptyList() : Arrays.asList(customActions));
    }
    
    @PluginFactory
    public static DefaultRolloverStrategy createStrategy(@PluginAttribute("max") final String max, @PluginAttribute("min") final String min, @PluginAttribute("fileIndex") final String fileIndex, @PluginAttribute("compressionLevel") final String compressionLevelStr, @PluginElement("Actions") final Action[] customActions, @PluginAttribute(value = "stopCustomActionsOnError", defaultBoolean = true) final boolean stopCustomActionsOnError, @PluginConfiguration final Configuration config) {
        final boolean useMax = fileIndex == null || fileIndex.equalsIgnoreCase("max");
        int minIndex = 1;
        if (min != null) {
            minIndex = Integer.parseInt(min);
            if (minIndex < 1) {
                DefaultRolloverStrategy.LOGGER.error("Minimum window size too small. Limited to 1");
                minIndex = 1;
            }
        }
        int maxIndex = 7;
        if (max != null) {
            maxIndex = Integer.parseInt(max);
            if (maxIndex < minIndex) {
                maxIndex = ((minIndex < 7) ? 7 : minIndex);
                DefaultRolloverStrategy.LOGGER.error("Maximum window size must be greater than the minimum windows size. Set to " + maxIndex);
            }
        }
        final int compressionLevel = Integers.parseInt(compressionLevelStr, -1);
        return new DefaultRolloverStrategy(minIndex, maxIndex, useMax, compressionLevel, config.getStrSubstitutor(), customActions, stopCustomActionsOnError);
    }
    
    public int getCompressionLevel() {
        return this.compressionLevel;
    }
    
    public int getMaxIndex() {
        return this.maxIndex;
    }
    
    public int getMinIndex() {
        return this.minIndex;
    }
    
    private int purge(final int lowIndex, final int highIndex, final RollingFileManager manager) {
        return this.useMax ? this.purgeAscending(lowIndex, highIndex, manager) : this.purgeDescending(lowIndex, highIndex, manager);
    }
    
    private int purgeAscending(final int lowIndex, final int highIndex, final RollingFileManager manager) {
        final List<FileRenameAction> renames = new ArrayList<FileRenameAction>();
        final StringBuilder buf = new StringBuilder();
        manager.getPatternProcessor().formatFileName(this.subst, buf, highIndex);
        String highFilename = this.subst.replace(buf);
        final int suffixLength = this.suffixLength(highFilename);
        int maxIndex = 0;
        for (int i = highIndex; i >= lowIndex; --i) {
            File toRename = new File(highFilename);
            if (i == highIndex && toRename.exists()) {
                maxIndex = highIndex;
            }
            else if (maxIndex == 0 && toRename.exists()) {
                maxIndex = i + 1;
                break;
            }
            boolean isBase = false;
            if (suffixLength > 0) {
                final File toRenameBase = new File(highFilename.substring(0, highFilename.length() - suffixLength));
                if (toRename.exists()) {
                    if (toRenameBase.exists()) {
                        DefaultRolloverStrategy.LOGGER.debug("DefaultRolloverStrategy.purgeAscending deleting {} base of {}.", new Object[] { toRenameBase, toRename });
                        toRenameBase.delete();
                    }
                }
                else {
                    toRename = toRenameBase;
                    isBase = true;
                }
            }
            if (toRename.exists()) {
                if (i == lowIndex) {
                    DefaultRolloverStrategy.LOGGER.debug("DefaultRolloverStrategy.purgeAscending deleting {} at low index {}: all slots full.", new Object[] { toRename, i });
                    if (!toRename.delete()) {
                        return -1;
                    }
                    break;
                }
                else {
                    buf.setLength(0);
                    manager.getPatternProcessor().formatFileName(this.subst, buf, i - 1);
                    String renameTo;
                    final String lowFilename = renameTo = this.subst.replace(buf);
                    if (isBase) {
                        renameTo = lowFilename.substring(0, lowFilename.length() - suffixLength);
                    }
                    renames.add(new FileRenameAction(toRename, new File(renameTo), true));
                    highFilename = lowFilename;
                }
            }
            else {
                buf.setLength(0);
                manager.getPatternProcessor().formatFileName(this.subst, buf, i - 1);
                highFilename = this.subst.replace(buf);
            }
        }
        if (maxIndex == 0) {
            maxIndex = lowIndex;
        }
        for (int i = renames.size() - 1; i >= 0; --i) {
            final Action action = renames.get(i);
            try {
                DefaultRolloverStrategy.LOGGER.debug("DefaultRolloverStrategy.purgeAscending executing {} of {}: {}", new Object[] { i, renames.size(), action });
                if (!action.execute()) {
                    return -1;
                }
            }
            catch (Exception ex) {
                DefaultRolloverStrategy.LOGGER.warn("Exception during purge in RollingFileAppender", ex);
                return -1;
            }
        }
        return maxIndex;
    }
    
    private int purgeDescending(final int lowIndex, final int highIndex, final RollingFileManager manager) {
        final List<FileRenameAction> renames = new ArrayList<FileRenameAction>();
        final StringBuilder buf = new StringBuilder();
        manager.getPatternProcessor().formatFileName(this.subst, buf, lowIndex);
        String lowFilename = this.subst.replace(buf);
        final int suffixLength = this.suffixLength(lowFilename);
        int i = lowIndex;
        while (i <= highIndex) {
            File toRename = new File(lowFilename);
            boolean isBase = false;
            if (suffixLength > 0) {
                final File toRenameBase = new File(lowFilename.substring(0, lowFilename.length() - suffixLength));
                if (toRename.exists()) {
                    if (toRenameBase.exists()) {
                        DefaultRolloverStrategy.LOGGER.debug("DefaultRolloverStrategy.purgeDescending deleting {} base of {}.", new Object[] { toRenameBase, toRename });
                        toRenameBase.delete();
                    }
                }
                else {
                    toRename = toRenameBase;
                    isBase = true;
                }
            }
            if (!toRename.exists()) {
                break;
            }
            if (i == highIndex) {
                DefaultRolloverStrategy.LOGGER.debug("DefaultRolloverStrategy.purgeDescending deleting {} at high index {}: all slots full.", new Object[] { toRename, i });
                if (!toRename.delete()) {
                    return -1;
                }
                break;
            }
            else {
                buf.setLength(0);
                manager.getPatternProcessor().formatFileName(this.subst, buf, i + 1);
                String renameTo;
                final String highFilename = renameTo = this.subst.replace(buf);
                if (isBase) {
                    renameTo = highFilename.substring(0, highFilename.length() - suffixLength);
                }
                renames.add(new FileRenameAction(toRename, new File(renameTo), true));
                lowFilename = highFilename;
                ++i;
            }
        }
        for (i = renames.size() - 1; i >= 0; --i) {
            final Action action = renames.get(i);
            try {
                DefaultRolloverStrategy.LOGGER.debug("DefaultRolloverStrategy.purgeDescending executing {} of {}: {}", new Object[] { i, renames.size(), action });
                if (!action.execute()) {
                    return -1;
                }
            }
            catch (Exception ex) {
                DefaultRolloverStrategy.LOGGER.warn("Exception during purge in RollingFileAppender", ex);
                return -1;
            }
        }
        return lowIndex;
    }
    
    private int suffixLength(final String lowFilename) {
        for (final FileExtensions extension : FileExtensions.values()) {
            if (extension.isExtensionFor(lowFilename)) {
                return extension.length();
            }
        }
        return 0;
    }
    
    @Override
    public RolloverDescription rollover(final RollingFileManager manager) throws SecurityException {
        if (this.maxIndex < 0) {
            return null;
        }
        final long startNanos = System.nanoTime();
        final int fileIndex = this.purge(this.minIndex, this.maxIndex, manager);
        if (fileIndex < 0) {
            return null;
        }
        if (DefaultRolloverStrategy.LOGGER.isTraceEnabled()) {
            final double durationMillis = (double)TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
            DefaultRolloverStrategy.LOGGER.trace("DefaultRolloverStrategy.purge() took {} milliseconds", new Object[] { durationMillis });
        }
        final StringBuilder buf = new StringBuilder(255);
        manager.getPatternProcessor().formatFileName(this.subst, buf, fileIndex);
        final String currentFileName = manager.getFileName();
        final String compressedName;
        String renameTo = compressedName = buf.toString();
        Action compressAction = null;
        for (final FileExtensions ext : FileExtensions.values()) {
            if (ext.isExtensionFor(renameTo)) {
                renameTo = renameTo.substring(0, renameTo.length() - ext.length());
                compressAction = ext.createCompressAction(renameTo, compressedName, true, this.compressionLevel);
                break;
            }
        }
        final FileRenameAction renameAction = new FileRenameAction(new File(currentFileName), new File(renameTo), false);
        final Action asyncAction = this.merge(compressAction, this.customActions, this.stopCustomActionsOnError);
        return new RolloverDescriptionImpl(currentFileName, false, renameAction, asyncAction);
    }
    
    private Action merge(final Action compressAction, final List<Action> custom, final boolean stopOnError) {
        if (custom.isEmpty()) {
            return compressAction;
        }
        if (compressAction == null) {
            return new CompositeAction(custom, stopOnError);
        }
        final List<Action> all = new ArrayList<Action>();
        all.add(compressAction);
        all.addAll(custom);
        return new CompositeAction(all, stopOnError);
    }
    
    @Override
    public String toString() {
        return "DefaultRolloverStrategy(min=" + this.minIndex + ", max=" + this.maxIndex + ')';
    }
    
    static {
        LOGGER = StatusLogger.getLogger();
    }
    
    enum FileExtensions
    {
        ZIP(".zip") {
            @Override
            Action createCompressAction(final String renameTo, final String compressedName, final boolean deleteSource, final int compressionLevel) {
                return new ZipCompressAction(this.source(renameTo), this.target(compressedName), deleteSource, compressionLevel);
            }
        }, 
        GZ(".gz") {
            @Override
            Action createCompressAction(final String renameTo, final String compressedName, final boolean deleteSource, final int compressionLevel) {
                return new GzCompressAction(this.source(renameTo), this.target(compressedName), deleteSource);
            }
        }, 
        BZIP2(".bz2") {
            @Override
            Action createCompressAction(final String renameTo, final String compressedName, final boolean deleteSource, final int compressionLevel) {
                return new CommonsCompressAction("bzip2", this.source(renameTo), this.target(compressedName), deleteSource);
            }
        }, 
        DEFLATE(".deflate") {
            @Override
            Action createCompressAction(final String renameTo, final String compressedName, final boolean deleteSource, final int compressionLevel) {
                return new CommonsCompressAction("deflate", this.source(renameTo), this.target(compressedName), deleteSource);
            }
        }, 
        PACK200(".pack200") {
            @Override
            Action createCompressAction(final String renameTo, final String compressedName, final boolean deleteSource, final int compressionLevel) {
                return new CommonsCompressAction("pack200", this.source(renameTo), this.target(compressedName), deleteSource);
            }
        }, 
        XY(".xy") {
            @Override
            Action createCompressAction(final String renameTo, final String compressedName, final boolean deleteSource, final int compressionLevel) {
                return new CommonsCompressAction("xy", this.source(renameTo), this.target(compressedName), deleteSource);
            }
        };
        
        private final String extension;
        
        private FileExtensions(final String extension) {
            Objects.requireNonNull(extension, "extension");
            this.extension = extension;
        }
        
        String getExtension() {
            return this.extension;
        }
        
        boolean isExtensionFor(final String s) {
            return s.endsWith(this.extension);
        }
        
        int length() {
            return this.extension.length();
        }
        
        File source(final String fileName) {
            return new File(fileName);
        }
        
        File target(final String fileName) {
            return new File(fileName);
        }
        
        abstract Action createCompressAction(final String p0, final String p1, final boolean p2, final int p3);
        
        static FileExtensions lookup(final String fileExtension) {
            for (final FileExtensions ext : values()) {
                if (ext.isExtensionFor(fileExtension)) {
                    return ext;
                }
            }
            return null;
        }
    }
}
