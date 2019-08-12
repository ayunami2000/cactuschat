// 
// Decompiled by Procyon v0.5.36
// 

package junit.runner;

public class StandardTestSuiteLoader implements TestSuiteLoader
{
    public Class load(final String suiteClassName) throws ClassNotFoundException {
        return Class.forName(suiteClassName);
    }
    
    public Class reload(final Class aClass) throws ClassNotFoundException {
        return aClass;
    }
}
