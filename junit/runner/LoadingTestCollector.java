// 
// Decompiled by Procyon v0.5.36
// 

package junit.runner;

import junit.framework.Test;
import junit.framework.TestSuite;
import java.lang.reflect.Modifier;

public class LoadingTestCollector extends ClassPathTestCollector
{
    TestCaseClassLoader fLoader;
    
    public LoadingTestCollector() {
        this.fLoader = new TestCaseClassLoader();
    }
    
    protected boolean isTestClass(final String classFileName) {
        try {
            if (classFileName.endsWith(".class")) {
                final Class testClass = this.classFromFile(classFileName);
                return testClass != null && this.isTestClass(testClass);
            }
        }
        catch (ClassNotFoundException expected) {}
        catch (NoClassDefFoundError noClassDefFoundError) {}
        return false;
    }
    
    Class classFromFile(final String classFileName) throws ClassNotFoundException {
        final String className = this.classNameFromFile(classFileName);
        if (!this.fLoader.isExcluded(className)) {
            return this.fLoader.loadClass(className, false);
        }
        return null;
    }
    
    boolean isTestClass(final Class testClass) {
        return this.hasSuiteMethod(testClass) || (Test.class.isAssignableFrom(testClass) && Modifier.isPublic(testClass.getModifiers()) && this.hasPublicConstructor(testClass));
    }
    
    boolean hasSuiteMethod(final Class testClass) {
        try {
            testClass.getMethod("suite", (Class[])new Class[0]);
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }
    
    boolean hasPublicConstructor(final Class testClass) {
        try {
            TestSuite.getTestConstructor(testClass);
        }
        catch (NoSuchMethodException e) {
            return false;
        }
        return true;
    }
}
