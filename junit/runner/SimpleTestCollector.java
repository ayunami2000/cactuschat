// 
// Decompiled by Procyon v0.5.36
// 

package junit.runner;

public class SimpleTestCollector extends ClassPathTestCollector
{
    protected boolean isTestClass(final String classFileName) {
        return classFileName.endsWith(".class") && classFileName.indexOf(36) < 0 && classFileName.indexOf("Test") > 0;
    }
}
