// 
// Decompiled by Procyon v0.5.36
// 

package jline.internal;

import java.io.PrintStream;

public final class Log
{
    public static final boolean TRACE;
    public static final boolean DEBUG;
    private static PrintStream output;
    
    public static PrintStream getOutput() {
        return Log.output;
    }
    
    public static void setOutput(final PrintStream out) {
        Log.output = Preconditions.checkNotNull(out);
    }
    
    @TestAccessible
    static void render(final PrintStream out, final Object message) {
        if (message.getClass().isArray()) {
            final Object[] array = (Object[])message;
            out.print("[");
            for (int i = 0; i < array.length; ++i) {
                out.print(array[i]);
                if (i + 1 < array.length) {
                    out.print(",");
                }
            }
            out.print("]");
        }
        else {
            out.print(message);
        }
    }
    
    @TestAccessible
    static void log(final Level level, final Object... messages) {
        synchronized (Log.output) {
            Log.output.format("[%s] ", level);
            for (int i = 0; i < messages.length; ++i) {
                if (i + 1 == messages.length && messages[i] instanceof Throwable) {
                    Log.output.println();
                    ((Throwable)messages[i]).printStackTrace(Log.output);
                }
                else {
                    render(Log.output, messages[i]);
                }
            }
            Log.output.println();
            Log.output.flush();
        }
    }
    
    public static void trace(final Object... messages) {
        if (Log.TRACE) {
            log(Level.TRACE, messages);
        }
    }
    
    public static void debug(final Object... messages) {
        if (Log.TRACE || Log.DEBUG) {
            log(Level.DEBUG, messages);
        }
    }
    
    public static void info(final Object... messages) {
        log(Level.INFO, messages);
    }
    
    public static void warn(final Object... messages) {
        log(Level.WARN, messages);
    }
    
    public static void error(final Object... messages) {
        log(Level.ERROR, messages);
    }
    
    static {
        DEBUG = ((TRACE = Boolean.getBoolean(Log.class.getName() + ".trace")) || Boolean.getBoolean(Log.class.getName() + ".debug"));
        Log.output = System.err;
    }
    
    public enum Level
    {
        TRACE, 
        DEBUG, 
        INFO, 
        WARN, 
        ERROR;
    }
}
