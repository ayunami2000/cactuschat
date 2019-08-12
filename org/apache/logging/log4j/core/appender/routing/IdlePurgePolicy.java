// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.appender.routing;

import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.core.LogEvent;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import org.apache.logging.log4j.core.config.ConfigurationScheduler;
import java.util.concurrent.ConcurrentMap;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Scheduled;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.AbstractLifeCycle;

@Plugin(name = "IdlePurgePolicy", category = "Core", printObject = true)
@Scheduled
public class IdlePurgePolicy extends AbstractLifeCycle implements PurgePolicy, Runnable
{
    private static final Logger LOGGER;
    private static final long serialVersionUID = 7481062062560624564L;
    private final long timeToLive;
    private final ConcurrentMap<String, Long> appendersUsage;
    private RoutingAppender routingAppender;
    private final ConfigurationScheduler scheduler;
    private volatile ScheduledFuture<?> future;
    
    public IdlePurgePolicy(final long timeToLive, final ConfigurationScheduler scheduler) {
        this.appendersUsage = new ConcurrentHashMap<String, Long>();
        this.future = null;
        this.timeToLive = timeToLive;
        this.scheduler = scheduler;
    }
    
    @Override
    public void initialize(final RoutingAppender routingAppender) {
        this.routingAppender = routingAppender;
    }
    
    @Override
    public void stop() {
        super.stop();
        this.future.cancel(true);
    }
    
    @Override
    public void purge() {
        final long createTime = System.currentTimeMillis() - this.timeToLive;
        for (final Map.Entry<String, Long> entry : this.appendersUsage.entrySet()) {
            if (entry.getValue() < createTime) {
                IdlePurgePolicy.LOGGER.debug("Removing appender " + entry.getKey());
                this.appendersUsage.remove(entry.getKey());
                this.routingAppender.deleteAppender(entry.getKey());
            }
        }
    }
    
    @Override
    public void update(final String key, final LogEvent event) {
        final long now = System.currentTimeMillis();
        this.appendersUsage.put(key, now);
        if (this.future == null) {
            synchronized (this) {
                if (this.future == null) {
                    this.scheduleNext();
                }
            }
        }
    }
    
    @Override
    public void run() {
        this.purge();
        this.scheduleNext();
    }
    
    private void scheduleNext() {
        long createTime = Long.MAX_VALUE;
        for (final Map.Entry<String, Long> entry : this.appendersUsage.entrySet()) {
            if (entry.getValue() < createTime) {
                createTime = entry.getValue();
            }
        }
        if (createTime < Long.MAX_VALUE) {
            final long interval = this.timeToLive - (System.currentTimeMillis() - createTime);
            this.future = this.scheduler.schedule(this, interval, TimeUnit.MILLISECONDS);
        }
    }
    
    @PluginFactory
    public static PurgePolicy createPurgePolicy(@PluginAttribute("timeToLive") final String timeToLive, @PluginAttribute("timeUnit") final String timeUnit, @PluginConfiguration final Configuration configuration) {
        if (timeToLive == null) {
            IdlePurgePolicy.LOGGER.error("A timeToLive  value is required");
            return null;
        }
        TimeUnit units;
        if (timeUnit == null) {
            units = TimeUnit.MINUTES;
        }
        else {
            try {
                units = TimeUnit.valueOf(timeUnit.toUpperCase());
            }
            catch (Exception ex) {
                IdlePurgePolicy.LOGGER.error("Invalid time unit {}", new Object[] { timeUnit });
                units = TimeUnit.MINUTES;
            }
        }
        final long ttl = units.toMillis(Long.parseLong(timeToLive));
        return new IdlePurgePolicy(ttl, configuration.getScheduler());
    }
    
    @Override
    public String toString() {
        return "timeToLive=" + this.timeToLive;
    }
    
    static {
        LOGGER = StatusLogger.getLogger();
    }
}
