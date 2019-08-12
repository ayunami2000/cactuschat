// 
// Decompiled by Procyon v0.5.36
// 

package junit.runner;

import java.util.Enumeration;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.Vector;

public class TestCaseClassLoader extends ClassLoader
{
    private Vector fPathItems;
    private String[] defaultExclusions;
    static final String EXCLUDED_FILE = "excluded.properties";
    private Vector fExcluded;
    
    public TestCaseClassLoader() {
        this(System.getProperty("java.class.path"));
    }
    
    public TestCaseClassLoader(final String classPath) {
        this.defaultExclusions = new String[] { "junit.framework.", "junit.extensions.", "junit.runner." };
        this.scanPath(classPath);
        this.readExcludedPackages();
    }
    
    private void scanPath(final String classPath) {
        final String separator = System.getProperty("path.separator");
        this.fPathItems = new Vector(10);
        final StringTokenizer st = new StringTokenizer(classPath, separator);
        while (st.hasMoreTokens()) {
            this.fPathItems.addElement(st.nextToken());
        }
    }
    
    public URL getResource(final String name) {
        return ClassLoader.getSystemResource(name);
    }
    
    public InputStream getResourceAsStream(final String name) {
        return ClassLoader.getSystemResourceAsStream(name);
    }
    
    public boolean isExcluded(final String name) {
        for (int i = 0; i < this.fExcluded.size(); ++i) {
            if (name.startsWith(this.fExcluded.elementAt(i))) {
                return true;
            }
        }
        return false;
    }
    
    public synchronized Class loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
        Class c = this.findLoadedClass(name);
        if (c != null) {
            return c;
        }
        if (this.isExcluded(name)) {
            try {
                c = this.findSystemClass(name);
                return c;
            }
            catch (ClassNotFoundException ex) {}
        }
        if (c == null) {
            final byte[] data = this.lookupClassData(name);
            if (data == null) {
                throw new ClassNotFoundException();
            }
            c = this.defineClass(name, data, 0, data.length);
        }
        if (resolve) {
            this.resolveClass(c);
        }
        return c;
    }
    
    private byte[] lookupClassData(final String className) throws ClassNotFoundException {
        byte[] data = null;
        for (int i = 0; i < this.fPathItems.size(); ++i) {
            final String path = this.fPathItems.elementAt(i);
            final String fileName = className.replace('.', '/') + ".class";
            if (this.isJar(path)) {
                data = this.loadJarData(path, fileName);
            }
            else {
                data = this.loadFileData(path, fileName);
            }
            if (data != null) {
                return data;
            }
        }
        throw new ClassNotFoundException(className);
    }
    
    boolean isJar(final String pathEntry) {
        return pathEntry.endsWith(".jar") || pathEntry.endsWith(".zip");
    }
    
    private byte[] loadFileData(final String path, final String fileName) {
        final File file = new File(path, fileName);
        if (file.exists()) {
            return this.getClassData(file);
        }
        return null;
    }
    
    private byte[] getClassData(final File f) {
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(f);
            final ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
            final byte[] b = new byte[1000];
            int n;
            while ((n = stream.read(b)) != -1) {
                out.write(b, 0, n);
            }
            stream.close();
            out.close();
            return out.toByteArray();
        }
        catch (IOException e) {}
        finally {
            if (stream != null) {
                try {
                    stream.close();
                }
                catch (IOException ex) {}
            }
        }
        return null;
    }
    
    private byte[] loadJarData(final String path, final String fileName) {
        ZipFile zipFile = null;
        InputStream stream = null;
        final File archive = new File(path);
        if (!archive.exists()) {
            return null;
        }
        try {
            zipFile = new ZipFile(archive);
        }
        catch (IOException io) {
            return null;
        }
        final ZipEntry entry = zipFile.getEntry(fileName);
        if (entry == null) {
            return null;
        }
        final int size = (int)entry.getSize();
        try {
            stream = zipFile.getInputStream(entry);
            final byte[] data = new byte[size];
            int n;
            for (int pos = 0; pos < size; pos += n) {
                n = stream.read(data, pos, data.length - pos);
            }
            zipFile.close();
            return data;
        }
        catch (IOException e) {}
        finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            }
            catch (IOException ex) {}
        }
        return null;
    }
    
    private void readExcludedPackages() {
        this.fExcluded = new Vector(10);
        for (int i = 0; i < this.defaultExclusions.length; ++i) {
            this.fExcluded.addElement(this.defaultExclusions[i]);
        }
        final InputStream is = this.getClass().getResourceAsStream("excluded.properties");
        if (is == null) {
            return;
        }
        final Properties p = new Properties();
        try {
            p.load(is);
        }
        catch (IOException e2) {
            return;
        }
        finally {
            try {
                is.close();
            }
            catch (IOException ex) {}
        }
        final Enumeration e = p.propertyNames();
        while (e.hasMoreElements()) {
            final String key = e.nextElement();
            if (key.startsWith("excluded.")) {
                String path = p.getProperty(key);
                path = path.trim();
                if (path.endsWith("*")) {
                    path = path.substring(0, path.length() - 1);
                }
                if (path.length() <= 0) {
                    continue;
                }
                this.fExcluded.addElement(path);
            }
        }
    }
}
