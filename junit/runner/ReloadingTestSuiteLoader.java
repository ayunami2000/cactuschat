// 
// Decompiled by Procyon v0.5.36
// 

package junit.runner;

public class ReloadingTestSuiteLoader implements TestSuiteLoader
{
    public Class load(final String suiteClassName) throws ClassNotFoundException {
        return this.createLoader().loadClass(suiteClassName, true);
    }
    
    public Class reload(final Class aClass) throws ClassNotFoundException {
        return this.createLoader().loadClass(aClass.getName(), true);
    }
    
    protected TestCaseClassLoader createLoader() {
        return new TestCaseClassLoader();
    }
}
