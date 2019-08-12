// 
// Decompiled by Procyon v0.5.36
// 

package junit.runner;

import java.util.StringTokenizer;
import java.io.File;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;

public abstract class ClassPathTestCollector implements TestCollector
{
    static final int SUFFIX_LENGTH;
    
    public Enumeration collectTests() {
        final String classPath = System.getProperty("java.class.path");
        final Hashtable result = this.collectFilesInPath(classPath);
        return result.elements();
    }
    
    public Hashtable collectFilesInPath(final String classPath) {
        final Hashtable result = this.collectFilesInRoots(this.splitClassPath(classPath));
        return result;
    }
    
    Hashtable collectFilesInRoots(final Vector roots) {
        final Hashtable result = new Hashtable(100);
        final Enumeration e = roots.elements();
        while (e.hasMoreElements()) {
            this.gatherFiles(new File(e.nextElement()), "", result);
        }
        return result;
    }
    
    void gatherFiles(final File classRoot, final String classFileName, final Hashtable result) {
        final File thisRoot = new File(classRoot, classFileName);
        if (thisRoot.isFile()) {
            if (this.isTestClass(classFileName)) {
                final String className = this.classNameFromFile(classFileName);
                result.put(className, className);
            }
            return;
        }
        final String[] contents = thisRoot.list();
        if (contents != null) {
            for (int i = 0; i < contents.length; ++i) {
                this.gatherFiles(classRoot, classFileName + File.separatorChar + contents[i], result);
            }
        }
    }
    
    Vector splitClassPath(final String classPath) {
        final Vector result = new Vector();
        final String separator = System.getProperty("path.separator");
        final StringTokenizer tokenizer = new StringTokenizer(classPath, separator);
        while (tokenizer.hasMoreTokens()) {
            result.addElement(tokenizer.nextToken());
        }
        return result;
    }
    
    protected boolean isTestClass(final String classFileName) {
        return classFileName.endsWith(".class") && classFileName.indexOf(36) < 0 && classFileName.indexOf("Test") > 0;
    }
    
    protected String classNameFromFile(final String classFileName) {
        final String s = classFileName.substring(0, classFileName.length() - ClassPathTestCollector.SUFFIX_LENGTH);
        final String s2 = s.replace(File.separatorChar, '.');
        if (s2.startsWith(".")) {
            return s2.substring(1);
        }
        return s2;
    }
    
    static {
        SUFFIX_LENGTH = ".class".length();
    }
}
