// 
// Decompiled by Procyon v0.5.36
// 

package jline.console.internal;

import java.lang.reflect.Method;
import java.util.List;
import jline.console.completer.ArgumentCompleter;
import java.util.StringTokenizer;
import jline.console.completer.Completer;
import jline.console.history.History;
import jline.console.history.FileHistory;
import java.io.File;
import jline.internal.Configuration;
import jline.console.ConsoleReader;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Arrays;

public class ConsoleRunner
{
    public static final String property = "jline.history";
    
    public static void main(final String[] args) throws Exception {
        final List<String> argList = new ArrayList<String>(Arrays.asList(args));
        if (argList.size() == 0) {
            usage();
            return;
        }
        final String historyFileName = System.getProperty("jline.history", null);
        final String mainClass = argList.remove(0);
        final ConsoleReader reader = new ConsoleReader();
        if (historyFileName != null) {
            reader.setHistory(new FileHistory(new File(Configuration.getUserHome(), String.format(".jline-%s.%s.history", mainClass, historyFileName))));
        }
        else {
            reader.setHistory(new FileHistory(new File(Configuration.getUserHome(), String.format(".jline-%s.history", mainClass))));
        }
        final String completors = System.getProperty(ConsoleRunner.class.getName() + ".completers", "");
        final List<Completer> completorList = new ArrayList<Completer>();
        final StringTokenizer tok = new StringTokenizer(completors, ",");
        while (tok.hasMoreTokens()) {
            final Object obj = Class.forName(tok.nextToken()).newInstance();
            completorList.add((Completer)obj);
        }
        if (completorList.size() > 0) {
            reader.addCompleter(new ArgumentCompleter(completorList));
        }
        ConsoleReaderInputStream.setIn(reader);
        try {
            final Class type = Class.forName(mainClass);
            final Method method = type.getMethod("main", String[].class);
            method.invoke(null, new Object[0]);
        }
        finally {
            ConsoleReaderInputStream.restoreIn();
        }
    }
    
    private static void usage() {
        System.out.println("Usage: \n   java [-Djline.history='name'] " + ConsoleRunner.class.getName() + " <target class name> [args]" + "\n\nThe -Djline.history option will avoid history" + "\nmangling when running ConsoleRunner on the same application." + "\n\nargs will be passed directly to the target class name.");
    }
}
