// 
// Decompiled by Procyon v0.5.36
// 

package jline.internal;

import java.util.ArrayList;
import java.util.List;

public class ShutdownHooks
{
    public static final String JLINE_SHUTDOWNHOOK = "jline.shutdownhook";
    private static final boolean enabled;
    private static final List<Task> tasks;
    private static Thread hook;
    
    public static synchronized <T extends Task> T add(final T task) {
        Preconditions.checkNotNull(task);
        if (!ShutdownHooks.enabled) {
            Log.debug("Shutdown-hook is disabled; not installing: ", task);
            return task;
        }
        if (ShutdownHooks.hook == null) {
            ShutdownHooks.hook = addHook(new Thread("JLine Shutdown Hook") {
                @Override
                public void run() {
                    runTasks();
                }
            });
        }
        Log.debug("Adding shutdown-hook task: ", task);
        ShutdownHooks.tasks.add(task);
        return task;
    }
    
    private static synchronized void runTasks() {
        Log.debug("Running all shutdown-hook tasks");
        for (final Task task : ShutdownHooks.tasks.toArray(new Task[ShutdownHooks.tasks.size()])) {
            Log.debug("Running task: ", task);
            try {
                task.run();
            }
            catch (Throwable e) {
                Log.warn("Task failed", e);
            }
        }
        ShutdownHooks.tasks.clear();
    }
    
    private static Thread addHook(final Thread thread) {
        Log.debug("Registering shutdown-hook: ", thread);
        try {
            Runtime.getRuntime().addShutdownHook(thread);
        }
        catch (AbstractMethodError e) {
            Log.debug("Failed to register shutdown-hook", e);
        }
        return thread;
    }
    
    public static synchronized void remove(final Task task) {
        Preconditions.checkNotNull(task);
        if (!ShutdownHooks.enabled || ShutdownHooks.hook == null) {
            return;
        }
        ShutdownHooks.tasks.remove(task);
        if (ShutdownHooks.tasks.isEmpty()) {
            removeHook(ShutdownHooks.hook);
            ShutdownHooks.hook = null;
        }
    }
    
    private static void removeHook(final Thread thread) {
        Log.debug("Removing shutdown-hook: ", thread);
        try {
            Runtime.getRuntime().removeShutdownHook(thread);
        }
        catch (AbstractMethodError e) {
            Log.debug("Failed to remove shutdown-hook", e);
        }
        catch (IllegalStateException ex) {}
    }
    
    static {
        enabled = Configuration.getBoolean("jline.shutdownhook", true);
        tasks = new ArrayList<Task>();
    }
    
    public interface Task
    {
        void run() throws Exception;
    }
}
