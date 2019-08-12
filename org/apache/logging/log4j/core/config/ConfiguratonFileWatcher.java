// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.config;

import java.util.Iterator;
import org.apache.logging.log4j.core.util.Log4jThread;
import java.io.File;
import java.util.List;
import org.apache.logging.log4j.core.util.FileWatcher;

public class ConfiguratonFileWatcher implements FileWatcher
{
    private Reconfigurable reconfigurable;
    private List<ConfigurationListener> listeners;
    
    public ConfiguratonFileWatcher(final Reconfigurable reconfigurable, final List<ConfigurationListener> listeners) {
        this.reconfigurable = reconfigurable;
        this.listeners = listeners;
    }
    
    @Override
    public void fileModified(final File file) {
        for (final ConfigurationListener listener : this.listeners) {
            final Thread thread = new Log4jThread(new ReconfigurationWorker(listener, this.reconfigurable));
            thread.setDaemon(true);
            thread.start();
        }
    }
    
    private static class ReconfigurationWorker implements Runnable
    {
        private final ConfigurationListener listener;
        private final Reconfigurable reconfigurable;
        
        public ReconfigurationWorker(final ConfigurationListener listener, final Reconfigurable reconfigurable) {
            this.listener = listener;
            this.reconfigurable = reconfigurable;
        }
        
        @Override
        public void run() {
            this.listener.onChange(this.reconfigurable);
        }
    }
}
