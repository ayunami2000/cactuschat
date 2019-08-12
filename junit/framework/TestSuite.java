// 
// Decompiled by Procyon v0.5.36
// 

package junit.framework;

import java.util.Enumeration;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

public class TestSuite implements Test
{
    private String fName;
    private Vector fTests;
    
    public static Test createTest(final Class theClass, final String name) {
        Constructor constructor;
        try {
            constructor = getTestConstructor(theClass);
        }
        catch (NoSuchMethodException e4) {
            return warning("Class " + theClass.getName() + " has no public constructor TestCase(String name) or TestCase()");
        }
        Object test;
        try {
            if (constructor.getParameterTypes().length == 0) {
                test = constructor.newInstance(new Object[0]);
                if (test instanceof TestCase) {
                    ((TestCase)test).setName(name);
                }
            }
            else {
                test = constructor.newInstance(name);
            }
        }
        catch (InstantiationException e) {
            return warning("Cannot instantiate test case: " + name + " (" + exceptionToString(e) + ")");
        }
        catch (InvocationTargetException e2) {
            return warning("Exception in constructor: " + name + " (" + exceptionToString(e2.getTargetException()) + ")");
        }
        catch (IllegalAccessException e3) {
            return warning("Cannot access test case: " + name + " (" + exceptionToString(e3) + ")");
        }
        return (Test)test;
    }
    
    public static Constructor getTestConstructor(final Class theClass) throws NoSuchMethodException {
        final Class[] args = { String.class };
        try {
            return theClass.getConstructor((Class[])args);
        }
        catch (NoSuchMethodException e) {
            return theClass.getConstructor((Class[])new Class[0]);
        }
    }
    
    public static Test warning(final String message) {
        return new TestCase("warning") {
            protected void runTest() {
                Assert.fail(message);
            }
        };
    }
    
    private static String exceptionToString(final Throwable t) {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter);
        t.printStackTrace(writer);
        return stringWriter.toString();
    }
    
    public TestSuite() {
        this.fTests = new Vector(10);
    }
    
    public TestSuite(final Class theClass) {
        this.fTests = new Vector(10);
        this.fName = theClass.getName();
        try {
            getTestConstructor(theClass);
        }
        catch (NoSuchMethodException e) {
            this.addTest(warning("Class " + theClass.getName() + " has no public constructor TestCase(String name) or TestCase()"));
            return;
        }
        if (!Modifier.isPublic(theClass.getModifiers())) {
            this.addTest(warning("Class " + theClass.getName() + " is not public"));
            return;
        }
        Class superClass = theClass;
        final Vector names = new Vector();
        while (Test.class.isAssignableFrom(superClass)) {
            final Method[] methods = superClass.getDeclaredMethods();
            for (int i = 0; i < methods.length; ++i) {
                this.addTestMethod(methods[i], names, theClass);
            }
            superClass = superClass.getSuperclass();
        }
        if (this.fTests.size() == 0) {
            this.addTest(warning("No tests found in " + theClass.getName()));
        }
    }
    
    public TestSuite(final Class theClass, final String name) {
        this(theClass);
        this.setName(name);
    }
    
    public TestSuite(final String name) {
        this.fTests = new Vector(10);
        this.setName(name);
    }
    
    public TestSuite(final Class[] classes) {
        this.fTests = new Vector(10);
        for (int i = 0; i < classes.length; ++i) {
            this.addTest(new TestSuite(classes[i]));
        }
    }
    
    public TestSuite(final Class[] classes, final String name) {
        this(classes);
        this.setName(name);
    }
    
    public void addTest(final Test test) {
        this.fTests.addElement(test);
    }
    
    public void addTestSuite(final Class testClass) {
        this.addTest(new TestSuite(testClass));
    }
    
    public int countTestCases() {
        int count = 0;
        final Enumeration e = this.tests();
        while (e.hasMoreElements()) {
            final Test test = e.nextElement();
            count += test.countTestCases();
        }
        return count;
    }
    
    public String getName() {
        return this.fName;
    }
    
    public void run(final TestResult result) {
        final Enumeration e = this.tests();
        while (e.hasMoreElements() && !result.shouldStop()) {
            final Test test = e.nextElement();
            this.runTest(test, result);
        }
    }
    
    public void runTest(final Test test, final TestResult result) {
        test.run(result);
    }
    
    public void setName(final String name) {
        this.fName = name;
    }
    
    public Test testAt(final int index) {
        return this.fTests.elementAt(index);
    }
    
    public int testCount() {
        return this.fTests.size();
    }
    
    public Enumeration tests() {
        return this.fTests.elements();
    }
    
    public String toString() {
        if (this.getName() != null) {
            return this.getName();
        }
        return super.toString();
    }
    
    private void addTestMethod(final Method m, final Vector names, final Class theClass) {
        final String name = m.getName();
        if (names.contains(name)) {
            return;
        }
        if (!this.isPublicTestMethod(m)) {
            if (this.isTestMethod(m)) {
                this.addTest(warning("Test method isn't public: " + m.getName()));
            }
            return;
        }
        names.addElement(name);
        this.addTest(createTest(theClass, name));
    }
    
    private boolean isPublicTestMethod(final Method m) {
        return this.isTestMethod(m) && Modifier.isPublic(m.getModifiers());
    }
    
    private boolean isTestMethod(final Method m) {
        final String name = m.getName();
        final Class[] parameters = m.getParameterTypes();
        final Class returnType = m.getReturnType();
        return parameters.length == 0 && name.startsWith("test") && returnType.equals(Void.TYPE);
    }
}
