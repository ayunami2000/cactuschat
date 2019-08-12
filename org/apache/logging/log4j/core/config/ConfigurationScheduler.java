// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.config;

import org.apache.logging.log4j.status.StatusLogger;
import java.util.Date;
import org.apache.logging.log4j.core.util.CronExpression;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import org.apache.logging.log4j.core.async.DaemonThreadFactory;
import java.util.concurrent.ScheduledExecutorService;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.AbstractLifeCycle;

public class ConfigurationScheduler extends AbstractLifeCycle
{
    private static final Logger LOGGER;
    private static final long serialVersionUID = 4570411889877332287L;
    private ScheduledExecutorService executorService;
    private int scheduledItems;
    
    public ConfigurationScheduler() {
        this.scheduledItems = 0;
    }
    
    @Override
    public void start() {
        super.start();
        if (this.scheduledItems > 0) {
            ConfigurationScheduler.LOGGER.debug("Starting {} Log4j2Scheduled threads", new Object[] { this.scheduledItems });
            if (this.scheduledItems > 5) {
                this.scheduledItems = 5;
            }
            this.executorService = new ScheduledThreadPoolExecutor(this.scheduledItems, new DaemonThreadFactory("Log4j2Scheduled-"));
        }
        else {
            ConfigurationScheduler.LOGGER.debug("No scheduled items");
        }
    }
    
    @Override
    public void stop() {
        if (this.executorService != null) {
            ConfigurationScheduler.LOGGER.debug("Stopping Log4j2Scheduled threads.");
            this.executorService.shutdown();
        }
        super.stop();
    }
    
    public void incrementScheduledItems() {
        if (!this.isStarted()) {
            ++this.scheduledItems;
        }
        else {
            ConfigurationScheduler.LOGGER.error("Attempted to increment scheduled items after start");
        }
    }
    
    public void decrementScheduledItems() {
        if (!this.isStarted() && this.scheduledItems > 0) {
            --this.scheduledItems;
        }
    }
    
    public <V> ScheduledFuture<V> schedule(final Callable<V> callable, final long delay, final TimeUnit unit) {
        return this.executorService.schedule(callable, delay, unit);
    }
    
    public ScheduledFuture<?> schedule(final Runnable command, final long delay, final TimeUnit unit) {
        return this.executorService.schedule(command, delay, unit);
    }
    
    public CronScheduledFuture<?> scheduleWithCron(final CronExpression cronExpression, final Runnable command) {
        final CronRunnable runnable = new CronRunnable(command, cronExpression);
        final ScheduledFuture<?> future = this.schedule(runnable, this.nextFireInterval(cronExpression), TimeUnit.MILLISECONDS);
        final CronScheduledFuture<?> cronScheduledFuture = new CronScheduledFuture<Object>(future);
        runnable.setScheduledFuture(cronScheduledFuture);
        return cronScheduledFuture;
    }
    
    public ScheduledFuture<?> scheduleAtFixedRate(final Runnable command, final long initialDelay, final long period, final TimeUnit unit) {
        return this.executorService.scheduleAtFixedRate(command, initialDelay, period, unit);
    }
    
    public ScheduledFuture<?> scheduleWithFixedDelay(final Runnable command, final long initialDelay, final long delay, final TimeUnit unit) {
        return this.executorService.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }
    
    private long nextFireInterval(final CronExpression cronExpression) {
        final Date now = new Date();
        final Date fireDate = cronExpression.getNextValidTimeAfter(now);
        return fireDate.getTime() - now.getTime();
    }
    
    static {
        LOGGER = StatusLogger.getLogger();
    }
    
    private class CronRunnable implements Runnable
    {
        private final CronExpression cronExpression;
        private final Runnable runnable;
        private CronScheduledFuture<?> scheduledFuture;
        
        public CronRunnable(final Runnable runnable, final CronExpression cronExpression) {
            this.cronExpression = cronExpression;
            this.runnable = runnable;
        }
        
        public void setScheduledFuture(final CronScheduledFuture<?> future) {
            this.scheduledFuture = future;
        }
        
        @Override
        public void run() {
            try {
                this.runnable.run();
            }
            catch (Throwable ex) {
                ConfigurationScheduler.LOGGER.error("Error running command", ex);
            }
            finally {
                final ScheduledFuture<?> future = ConfigurationScheduler.this.schedule(this, ConfigurationScheduler.this.nextFireInterval(this.cronExpression), TimeUnit.MILLISECONDS);
                this.scheduledFuture.setScheduledFuture(future);
            }
        }
    }
}
