// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.appender.rolling;

import org.apache.logging.log4j.status.StatusLogger;
import java.text.ParseException;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.LogEvent;
import java.util.Date;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.util.CronExpression;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Scheduled;
import org.apache.logging.log4j.core.config.plugins.Plugin;

@Plugin(name = "CronTriggeringPolicy", category = "Core", printObject = true)
@Scheduled
public final class CronTriggeringPolicy implements TriggeringPolicy
{
    private static Logger LOGGER;
    private static final String defaultSchedule = "0 0 0 * * ?";
    private RollingFileManager manager;
    private final CronExpression cronExpression;
    private final Configuration configuration;
    private final boolean checkOnStartup;
    
    private CronTriggeringPolicy(final CronExpression schedule, final boolean checkOnStartup, final Configuration configuration) {
        this.cronExpression = schedule;
        this.configuration = configuration;
        this.checkOnStartup = checkOnStartup;
    }
    
    @Override
    public void initialize(final RollingFileManager aManager) {
        this.manager = aManager;
        if (this.checkOnStartup) {
            final Date nextDate = this.cronExpression.getNextValidTimeAfter(new Date(this.manager.getFileTime()));
            if (nextDate.getTime() < System.currentTimeMillis()) {
                this.manager.rollover();
            }
        }
        this.configuration.getScheduler().scheduleWithCron(this.cronExpression, new CronTrigger());
    }
    
    @Override
    public boolean isTriggeringEvent(final LogEvent event) {
        return false;
    }
    
    public CronExpression getCronExpression() {
        return this.cronExpression;
    }
    
    @PluginFactory
    public static CronTriggeringPolicy createPolicy(@PluginConfiguration final Configuration configuration, @PluginAttribute("evaluateOnStartup") final String evaluateOnStartup, @PluginAttribute("schedule") final String schedule) {
        final boolean checkOnStartup = Boolean.parseBoolean(evaluateOnStartup);
        CronExpression cronExpression;
        if (schedule == null) {
            CronTriggeringPolicy.LOGGER.info("No schedule specified, defaulting to Daily");
            cronExpression = getSchedule("0 0 0 * * ?");
        }
        else {
            cronExpression = getSchedule(schedule);
            if (cronExpression == null) {
                CronTriggeringPolicy.LOGGER.error("Invalid expression specified. Defaulting to Daily");
                cronExpression = getSchedule("0 0 0 * * ?");
            }
        }
        return new CronTriggeringPolicy(cronExpression, checkOnStartup, configuration);
    }
    
    private static CronExpression getSchedule(final String expression) {
        try {
            return new CronExpression(expression);
        }
        catch (ParseException pe) {
            CronTriggeringPolicy.LOGGER.error("Invalid cron expression - " + expression, pe);
            return null;
        }
    }
    
    @Override
    public String toString() {
        return "CronTriggeringPolicy(schedule=" + this.cronExpression.getCronExpression() + ")";
    }
    
    static {
        CronTriggeringPolicy.LOGGER = StatusLogger.getLogger();
    }
    
    private class CronTrigger implements Runnable
    {
        @Override
        public void run() {
            CronTriggeringPolicy.this.manager.rollover();
        }
    }
}
