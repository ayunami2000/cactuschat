// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.util;

import java.lang.reflect.UndeclaredThrowableException;
import java.io.Closeable;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.Reader;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

public final class Throwables
{
    private Throwables() {
    }
    
    @Deprecated
    public static void addSuppressed(final Throwable throwable, final Throwable suppressedThrowable) {
        throwable.addSuppressed(suppressedThrowable);
    }
    
    public static Throwable getRootCause(final Throwable throwable) {
        Throwable root;
        Throwable cause;
        for (root = throwable; (cause = root.getCause()) != null; root = cause) {}
        return root;
    }
    
    @Deprecated
    public static Throwable[] getSuppressed(final Throwable throwable) {
        return throwable.getSuppressed();
    }
    
    @Deprecated
    public static boolean isGetSuppressedAvailable() {
        return true;
    }
    
    public static List<String> toStringList(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        try {
            throwable.printStackTrace(pw);
        }
        catch (RuntimeException ex2) {}
        pw.flush();
        final List<String> lines = new ArrayList<String>();
        final LineNumberReader reader = new LineNumberReader(new StringReader(sw.toString()));
        try {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                lines.add(line);
            }
        }
        catch (IOException ex) {
            if (ex instanceof InterruptedIOException) {
                Thread.currentThread().interrupt();
            }
            lines.add(ex.toString());
        }
        finally {
            Closer.closeSilently(reader);
        }
        return lines;
    }
    
    public static void rethrow(final Throwable t) {
        if (t instanceof RuntimeException) {
            throw (RuntimeException)t;
        }
        if (t instanceof Error) {
            throw (Error)t;
        }
        throw new UndeclaredThrowableException(t);
    }
}
