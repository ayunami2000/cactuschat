// 
// Decompiled by Procyon v0.5.36
// 

package jline.internal;

import java.nio.charset.Charset;
import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.net.URL;
import java.io.IOException;
import java.util.Properties;

public class Configuration
{
    public static final String JLINE_CONFIGURATION = "jline.configuration";
    public static final String JLINE_RC = ".jline.rc";
    private static volatile Properties properties;
    
    private static Properties initProperties() {
        final URL url = determineUrl();
        final Properties props = new Properties();
        try {
            loadProperties(url, props);
        }
        catch (IOException e) {
            Log.debug("Unable to read configuration from: ", url, e);
        }
        return props;
    }
    
    private static void loadProperties(final URL url, final Properties props) throws IOException {
        Log.debug("Loading properties from: ", url);
        final InputStream input = url.openStream();
        try {
            props.load(new BufferedInputStream(input));
        }
        finally {
            try {
                input.close();
            }
            catch (IOException ex) {}
        }
        if (Log.DEBUG) {
            Log.debug("Loaded properties:");
            for (final Map.Entry<Object, Object> entry : props.entrySet()) {
                Log.debug("  ", entry.getKey(), "=", entry.getValue());
            }
        }
    }
    
    private static URL determineUrl() {
        final String tmp = System.getProperty("jline.configuration");
        if (tmp != null) {
            return Urls.create(tmp);
        }
        final File file = new File(getUserHome(), ".jline.rc");
        return Urls.create(file);
    }
    
    public static void reset() {
        Log.debug("Resetting");
        Configuration.properties = null;
        getProperties();
    }
    
    public static Properties getProperties() {
        if (Configuration.properties == null) {
            Configuration.properties = initProperties();
        }
        return Configuration.properties;
    }
    
    public static String getString(final String name, final String defaultValue) {
        Preconditions.checkNotNull(name);
        String value = System.getProperty(name);
        if (value == null) {
            value = getProperties().getProperty(name);
            if (value == null) {
                value = defaultValue;
            }
        }
        return value;
    }
    
    public static String getString(final String name) {
        return getString(name, null);
    }
    
    public static boolean getBoolean(final String name, final boolean defaultValue) {
        final String value = getString(name);
        if (value == null) {
            return defaultValue;
        }
        return value.length() == 0 || value.equalsIgnoreCase("1") || value.equalsIgnoreCase("on") || value.equalsIgnoreCase("true");
    }
    
    public static int getInteger(final String name, final int defaultValue) {
        final String str = getString(name);
        if (str == null) {
            return defaultValue;
        }
        return Integer.parseInt(str);
    }
    
    public static long getLong(final String name, final long defaultValue) {
        final String str = getString(name);
        if (str == null) {
            return defaultValue;
        }
        return Long.parseLong(str);
    }
    
    public static String getLineSeparator() {
        return System.getProperty("line.separator");
    }
    
    public static File getUserHome() {
        return new File(System.getProperty("user.home"));
    }
    
    public static String getOsName() {
        return System.getProperty("os.name").toLowerCase();
    }
    
    public static boolean isWindows() {
        return getOsName().startsWith("windows");
    }
    
    public static String getFileEncoding() {
        return System.getProperty("file.encoding");
    }
    
    public static String getEncoding() {
        final String envEncoding = extractEncodingFromCtype(System.getenv("LC_CTYPE"));
        if (envEncoding != null) {
            return envEncoding;
        }
        return System.getProperty("input.encoding", Charset.defaultCharset().name());
    }
    
    static String extractEncodingFromCtype(final String ctype) {
        if (ctype == null || ctype.indexOf(46) <= 0) {
            return null;
        }
        final String encodingAndModifier = ctype.substring(ctype.indexOf(46) + 1);
        if (encodingAndModifier.indexOf(64) > 0) {
            return encodingAndModifier.substring(0, encodingAndModifier.indexOf(64));
        }
        return encodingAndModifier;
    }
}
