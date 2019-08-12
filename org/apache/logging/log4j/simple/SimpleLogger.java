// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.simple;

import java.util.Map;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.util.Strings;
import java.util.Date;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.Marker;
import java.text.SimpleDateFormat;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.apache.logging.log4j.message.MessageFactory;
import java.io.PrintStream;
import org.apache.logging.log4j.Level;
import java.text.DateFormat;
import org.apache.logging.log4j.spi.AbstractLogger;

public class SimpleLogger extends AbstractLogger
{
    private static final long serialVersionUID = 1L;
    private static final char SPACE = ' ';
    private DateFormat dateFormatter;
    private Level level;
    private final boolean showDateTime;
    private final boolean showContextMap;
    private PrintStream stream;
    private final String logName;
    
    public SimpleLogger(final String name, final Level defaultLevel, final boolean showLogName, final boolean showShortLogName, final boolean showDateTime, final boolean showContextMap, final String dateTimeFormat, final MessageFactory messageFactory, final PropertiesUtil props, final PrintStream stream) {
        super(name, messageFactory);
        final String lvl = props.getStringProperty("org.apache.logging.log4j.simplelog." + name + ".level");
        this.level = Level.toLevel(lvl, defaultLevel);
        if (showShortLogName) {
            final int index = name.lastIndexOf(".");
            if (index > 0 && index < name.length()) {
                this.logName = name.substring(index + 1);
            }
            else {
                this.logName = name;
            }
        }
        else if (showLogName) {
            this.logName = name;
        }
        else {
            this.logName = null;
        }
        this.showDateTime = showDateTime;
        this.showContextMap = showContextMap;
        this.stream = stream;
        if (showDateTime) {
            try {
                this.dateFormatter = new SimpleDateFormat(dateTimeFormat);
            }
            catch (IllegalArgumentException e) {
                this.dateFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS zzz");
            }
        }
    }
    
    @Override
    public Level getLevel() {
        return this.level;
    }
    
    @Override
    public boolean isEnabled(final Level testLevel, final Marker marker, final Message msg, final Throwable t) {
        return this.level.intLevel() >= testLevel.intLevel();
    }
    
    @Override
    public boolean isEnabled(final Level testLevel, final Marker marker, final Object msg, final Throwable t) {
        return this.level.intLevel() >= testLevel.intLevel();
    }
    
    @Override
    public boolean isEnabled(final Level testLevel, final Marker marker, final String msg) {
        return this.level.intLevel() >= testLevel.intLevel();
    }
    
    @Override
    public boolean isEnabled(final Level testLevel, final Marker marker, final String msg, final Object... p1) {
        return this.level.intLevel() >= testLevel.intLevel();
    }
    
    @Override
    public boolean isEnabled(final Level testLevel, final Marker marker, final String msg, final Throwable t) {
        return this.level.intLevel() >= testLevel.intLevel();
    }
    
    @Override
    public void logMessage(final String fqcn, final Level mgsLevel, final Marker marker, final Message msg, final Throwable throwable) {
        final StringBuilder sb = new StringBuilder();
        if (this.showDateTime) {
            final Date now = new Date();
            final String dateText;
            synchronized (this.dateFormatter) {
                dateText = this.dateFormatter.format(now);
            }
            sb.append(dateText);
            sb.append(' ');
        }
        sb.append(mgsLevel.toString());
        sb.append(' ');
        if (Strings.isNotEmpty(this.logName)) {
            sb.append(this.logName);
            sb.append(' ');
        }
        sb.append(msg.getFormattedMessage());
        if (this.showContextMap) {
            final Map<String, String> mdc = ThreadContext.getImmutableContext();
            if (mdc.size() > 0) {
                sb.append(' ');
                sb.append(mdc.toString());
                sb.append(' ');
            }
        }
        final Object[] params = msg.getParameters();
        Throwable t;
        if (throwable == null && params != null && params.length > 0 && params[params.length - 1] instanceof Throwable) {
            t = (Throwable)params[params.length - 1];
        }
        else {
            t = throwable;
        }
        this.stream.println(sb.toString());
        if (t != null) {
            this.stream.print(' ');
            t.printStackTrace(this.stream);
        }
    }
    
    public void setLevel(final Level level) {
        if (level != null) {
            this.level = level;
        }
    }
    
    public void setStream(final PrintStream stream) {
        this.stream = stream;
    }
}
