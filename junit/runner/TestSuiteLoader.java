// 
// Decompiled by Procyon v0.5.36
// 

package junit.runner;

public interface TestSuiteLoader
{
    Class load(final String p0) throws ClassNotFoundException;
    
    Class reload(final Class p0) throws ClassNotFoundException;
}
