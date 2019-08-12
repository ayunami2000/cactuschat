// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.util;

import org.apache.logging.log4j.status.StatusLogger;
import java.util.List;
import java.util.ArrayList;
import java.io.InputStream;
import java.util.Iterator;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import org.apache.logging.log4j.Logger;

public final class PropertiesUtil
{
    private static final PropertiesUtil LOG4J_PROPERTIES;
    private static final Logger LOGGER;
    private final Properties props;
    
    public PropertiesUtil(final Properties props) {
        this.props = props;
    }
    
    public PropertiesUtil(final String propertiesFileName) {
        final Properties properties = new Properties();
        for (final URL url : LoaderUtil.findResources(propertiesFileName)) {
            InputStream in = null;
            try {
                in = url.openStream();
                properties.load(in);
            }
            catch (IOException ioe) {
                PropertiesUtil.LOGGER.error("Unable to read {}", new Object[] { url.toString(), ioe });
                if (in == null) {
                    continue;
                }
                try {
                    in.close();
                }
                catch (IOException ioe) {
                    PropertiesUtil.LOGGER.error("Unable to close {}", new Object[] { url.toString(), ioe });
                }
            }
            finally {
                if (in != null) {
                    try {
                        in.close();
                    }
                    catch (IOException ioe2) {
                        PropertiesUtil.LOGGER.error("Unable to close {}", new Object[] { url.toString(), ioe2 });
                    }
                }
            }
        }
        this.props = properties;
    }
    
    static Properties loadClose(final InputStream in, final Object source) {
        final Properties props = new Properties();
        if (null != in) {
            try {
                props.load(in);
            }
            catch (IOException e) {
                PropertiesUtil.LOGGER.error("Unable to read {}", new Object[] { source, e });
                try {
                    in.close();
                }
                catch (IOException e) {
                    PropertiesUtil.LOGGER.error("Unable to close {}", new Object[] { source, e });
                }
            }
            finally {
                try {
                    in.close();
                }
                catch (IOException e2) {
                    PropertiesUtil.LOGGER.error("Unable to close {}", new Object[] { source, e2 });
                }
            }
        }
        return props;
    }
    
    public static PropertiesUtil getProperties() {
        return PropertiesUtil.LOG4J_PROPERTIES;
    }
    
    public String getStringProperty(final String name) {
        String prop = null;
        try {
            prop = System.getProperty(name);
        }
        catch (SecurityException ex) {}
        return (prop == null) ? this.props.getProperty(name) : prop;
    }
    
    public int getIntegerProperty(final String name, final int defaultValue) {
        String prop = null;
        try {
            prop = System.getProperty(name);
        }
        catch (SecurityException ex) {}
        if (prop == null) {
            prop = this.props.getProperty(name);
        }
        if (prop != null) {
            try {
                return Integer.parseInt(prop);
            }
            catch (Exception ignored) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    public long getLongProperty(final String name, final long defaultValue) {
        String prop = null;
        try {
            prop = System.getProperty(name);
        }
        catch (SecurityException ex) {}
        if (prop == null) {
            prop = this.props.getProperty(name);
        }
        if (prop != null) {
            try {
                return Long.parseLong(prop);
            }
            catch (Exception ignored) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    public String getStringProperty(final String name, final String defaultValue) {
        final String prop = this.getStringProperty(name);
        return (prop == null) ? defaultValue : prop;
    }
    
    public boolean getBooleanProperty(final String name) {
        return this.getBooleanProperty(name, false);
    }
    
    public boolean getBooleanProperty(final String name, final boolean defaultValue) {
        final String prop = this.getStringProperty(name);
        return (prop == null) ? defaultValue : "true".equalsIgnoreCase(prop);
    }
    
    public static Properties getSystemProperties() {
        try {
            return new Properties(System.getProperties());
        }
        catch (SecurityException ex) {
            PropertiesUtil.LOGGER.error("Unable to access system properties.", ex);
            return new Properties();
        }
    }
    
    public static Properties extractSubset(final Properties properties, final String prefix) {
        final Properties subset = new Properties();
        if (prefix == null || prefix.length() == 0) {
            return subset;
        }
        final String prefixToMatch = (prefix.charAt(prefix.length() - 1) != '.') ? (prefix + '.') : prefix;
        final List<String> keys = new ArrayList<String>();
        for (final String key : properties.stringPropertyNames()) {
            if (key.startsWith(prefixToMatch)) {
                subset.setProperty(key.substring(prefixToMatch.length()), properties.getProperty(key));
                keys.add(key);
            }
        }
        for (final String key : keys) {
            properties.remove(key);
        }
        return subset;
    }
    
    public boolean isOsWindows() {
        return this.getStringProperty("os.name").startsWith("Windows");
    }
    
    static {
        LOG4J_PROPERTIES = new PropertiesUtil("log4j2.component.properties");
        LOGGER = StatusLogger.getLogger();
    }
}
