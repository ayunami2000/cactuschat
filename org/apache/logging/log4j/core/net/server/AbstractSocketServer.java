// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.net.server;

import org.apache.logging.log4j.core.config.xml.XmlConfiguration;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.FileNotFoundException;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import java.io.FileInputStream;
import java.io.File;
import org.apache.logging.log4j.util.Strings;
import org.apache.logging.log4j.core.config.Configuration;
import java.net.URI;
import org.apache.logging.log4j.core.config.xml.XmlConfigurationFactory;
import org.apache.logging.log4j.core.util.Log4jThread;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LogEventListener;
import java.io.InputStream;

public abstract class AbstractSocketServer<T extends InputStream> extends LogEventListener implements Runnable
{
    protected static final int MAX_PORT = 65534;
    private volatile boolean active;
    protected final LogEventBridge<T> logEventInput;
    protected final Logger logger;
    
    public AbstractSocketServer(final int port, final LogEventBridge<T> logEventInput) {
        this.active = true;
        this.logger = LogManager.getLogger(this.getClass().getName() + '.' + port);
        this.logEventInput = Objects.requireNonNull(logEventInput, "LogEventInput");
    }
    
    protected boolean isActive() {
        return this.active;
    }
    
    protected void setActive(final boolean isActive) {
        this.active = isActive;
    }
    
    public Thread startNewThread() {
        final Thread thread = new Log4jThread(this);
        thread.start();
        return thread;
    }
    
    protected static class ServerConfigurationFactory extends XmlConfigurationFactory
    {
        private final String path;
        
        public ServerConfigurationFactory(final String path) {
            this.path = path;
        }
        
        @Override
        public Configuration getConfiguration(final String name, final URI configLocation) {
            if (Strings.isNotEmpty(this.path)) {
                File file = null;
                ConfigurationSource source = null;
                try {
                    file = new File(this.path);
                    final FileInputStream is = new FileInputStream(file);
                    source = new ConfigurationSource(is, file);
                }
                catch (FileNotFoundException ex) {}
                if (source == null) {
                    try {
                        final URL url = new URL(this.path);
                        source = new ConfigurationSource(url.openStream(), url);
                    }
                    catch (MalformedURLException ex2) {}
                    catch (IOException ex3) {}
                }
                try {
                    if (source != null) {
                        return new XmlConfiguration(source);
                    }
                }
                catch (Exception ex4) {}
                System.err.println("Unable to process configuration at " + this.path + ", using default.");
            }
            return super.getConfiguration(name, configLocation);
        }
    }
}
