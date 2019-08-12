// 
// Decompiled by Procyon v0.5.36
// 

package org.fusesource.hawtjni.runtime;

import java.io.InputStream;
import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;

public class Library
{
    static final String SLASH;
    private final String name;
    private final String version;
    private final ClassLoader classLoader;
    private boolean loaded;
    
    public Library(final String name) {
        this(name, null, null);
    }
    
    public Library(final String name, final Class<?> clazz) {
        this(name, version(clazz), clazz.getClassLoader());
    }
    
    public Library(final String name, final String version) {
        this(name, version, null);
    }
    
    public Library(final String name, final String version, final ClassLoader classLoader) {
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }
        this.name = name;
        this.version = version;
        this.classLoader = classLoader;
    }
    
    private static String version(final Class<?> clazz) {
        try {
            return clazz.getPackage().getImplementationVersion();
        }
        catch (Throwable e) {
            return null;
        }
    }
    
    public static String getOperatingSystem() {
        final String name = System.getProperty("os.name").toLowerCase().trim();
        if (name.startsWith("linux")) {
            return "linux";
        }
        if (name.startsWith("mac os x")) {
            return "osx";
        }
        if (name.startsWith("win")) {
            return "windows";
        }
        return name.replaceAll("\\W+", "_");
    }
    
    public static String getPlatform() {
        return getOperatingSystem() + getBitModel();
    }
    
    public static int getBitModel() {
        String prop = System.getProperty("sun.arch.data.model");
        if (prop == null) {
            prop = System.getProperty("com.ibm.vm.bitmode");
        }
        if (prop != null) {
            return Integer.parseInt(prop);
        }
        return -1;
    }
    
    public synchronized void load() {
        if (this.loaded) {
            return;
        }
        this.doLoad();
        this.loaded = true;
    }
    
    private void doLoad() {
        String version = System.getProperty("library." + this.name + ".version");
        if (version == null) {
            version = this.version;
        }
        final ArrayList<String> errors = new ArrayList<String>();
        final String customPath = System.getProperty("library." + this.name + ".path");
        if (customPath != null) {
            if (version != null && this.load(errors, this.file(customPath, this.map(this.name + "-" + version)))) {
                return;
            }
            if (this.load(errors, this.file(customPath, this.map(this.name)))) {
                return;
            }
        }
        if (version != null && this.load(errors, this.name + getBitModel() + "-" + version)) {
            return;
        }
        if (version != null && this.load(errors, this.name + "-" + version)) {
            return;
        }
        if (this.load(errors, this.name)) {
            return;
        }
        if (this.classLoader != null) {
            if (this.exractAndLoad(errors, version, customPath, this.getPlatformSpecifcResourcePath())) {
                return;
            }
            if (this.exractAndLoad(errors, version, customPath, this.getOperatingSystemSpecifcResourcePath())) {
                return;
            }
            if (this.exractAndLoad(errors, version, customPath, this.getResorucePath())) {
                return;
            }
        }
        throw new UnsatisfiedLinkError("Could not load library. Reasons: " + errors.toString());
    }
    
    public final String getOperatingSystemSpecifcResourcePath() {
        return this.getPlatformSpecifcResourcePath(getOperatingSystem());
    }
    
    public final String getPlatformSpecifcResourcePath() {
        return this.getPlatformSpecifcResourcePath(getPlatform());
    }
    
    public final String getPlatformSpecifcResourcePath(final String platform) {
        return "META-INF/native/" + platform + "/" + this.map(this.name);
    }
    
    public final String getResorucePath() {
        return "META-INF/native/" + this.map(this.name);
    }
    
    public final String getLibraryFileName() {
        return this.map(this.name);
    }
    
    private boolean exractAndLoad(final ArrayList<String> errors, final String version, String customPath, final String resourcePath) {
        final URL resource = this.classLoader.getResource(resourcePath);
        if (resource != null) {
            String libName = this.name + "-" + getBitModel();
            if (version != null) {
                libName = libName + "-" + version;
            }
            final String[] libNameParts = this.map(libName).split("\\.");
            final String prefix = libNameParts[0] + "-";
            final String suffix = "." + libNameParts[1];
            if (customPath != null) {
                final File target = this.extract(errors, resource, prefix, suffix, this.file(customPath));
                if (target != null && this.load(errors, target)) {
                    return true;
                }
            }
            customPath = System.getProperty("java.io.tmpdir");
            final File target = this.extract(errors, resource, prefix, suffix, this.file(customPath));
            if (target != null && this.load(errors, target)) {
                return true;
            }
        }
        return false;
    }
    
    private File file(final String... paths) {
        File rc = null;
        for (final String path : paths) {
            if (rc == null) {
                rc = new File(path);
            }
            else {
                rc = new File(rc, path);
            }
        }
        return rc;
    }
    
    private String map(String libName) {
        libName = System.mapLibraryName(libName);
        final String ext = ".dylib";
        if (libName.endsWith(ext)) {
            libName = libName.substring(0, libName.length() - ext.length()) + ".jnilib";
        }
        return libName;
    }
    
    private File extract(final ArrayList<String> errors, final URL source, final String prefix, final String suffix, final File directory) {
        File target = null;
        try {
            FileOutputStream os = null;
            InputStream is = null;
            try {
                target = File.createTempFile(prefix, suffix, directory);
                is = source.openStream();
                if (is != null) {
                    final byte[] buffer = new byte[4096];
                    os = new FileOutputStream(target);
                    int read;
                    while ((read = is.read(buffer)) != -1) {
                        os.write(buffer, 0, read);
                    }
                    this.chmod("755", target);
                }
                target.deleteOnExit();
                return target;
            }
            finally {
                close(os);
                close(is);
            }
        }
        catch (Throwable e) {
            if (target != null) {
                target.delete();
            }
            errors.add(e.getMessage());
            return null;
        }
    }
    
    private static void close(final Closeable file) {
        if (file != null) {
            try {
                file.close();
            }
            catch (Exception ex) {}
        }
    }
    
    private void chmod(final String permision, final File path) {
        if (getPlatform().startsWith("windows")) {
            return;
        }
        try {
            Runtime.getRuntime().exec(new String[] { "chmod", permision, path.getCanonicalPath() }).waitFor();
        }
        catch (Throwable t) {}
    }
    
    private boolean load(final ArrayList<String> errors, final File lib) {
        try {
            System.load(lib.getPath());
            return true;
        }
        catch (UnsatisfiedLinkError e) {
            errors.add(e.getMessage());
            return false;
        }
    }
    
    private boolean load(final ArrayList<String> errors, final String lib) {
        try {
            System.loadLibrary(lib);
            return true;
        }
        catch (UnsatisfiedLinkError e) {
            errors.add(e.getMessage());
            return false;
        }
    }
    
    static {
        SLASH = System.getProperty("file.separator");
    }
}
