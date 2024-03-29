// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.appender.rolling;

import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import org.apache.logging.log4j.core.pattern.PatternParser;
import org.apache.logging.log4j.core.pattern.DatePatternConverter;
import org.apache.logging.log4j.core.pattern.PatternConverter;
import java.util.ArrayList;
import org.apache.logging.log4j.core.pattern.FormattingInfo;
import org.apache.logging.log4j.core.pattern.ArrayPatternConverter;
import org.apache.logging.log4j.Logger;

public class PatternProcessor
{
    protected static final Logger LOGGER;
    private static final String KEY = "FileConverter";
    private static final char YEAR_CHAR = 'y';
    private static final char MONTH_CHAR = 'M';
    private static final char[] WEEK_CHARS;
    private static final char[] DAY_CHARS;
    private static final char[] HOUR_CHARS;
    private static final char MINUTE_CHAR = 'm';
    private static final char SECOND_CHAR = 's';
    private static final char MILLIS_CHAR = 'S';
    private final ArrayPatternConverter[] patternConverters;
    private final FormattingInfo[] patternFields;
    private long prevFileTime;
    private long nextFileTime;
    private RolloverFrequency frequency;
    
    public PatternProcessor(final String pattern) {
        this.prevFileTime = 0L;
        this.nextFileTime = 0L;
        this.frequency = null;
        final PatternParser parser = this.createPatternParser();
        final List<PatternConverter> converters = new ArrayList<PatternConverter>();
        final List<FormattingInfo> fields = new ArrayList<FormattingInfo>();
        parser.parse(pattern, converters, fields, false, false);
        final FormattingInfo[] infoArray = new FormattingInfo[fields.size()];
        this.patternFields = fields.toArray(infoArray);
        final ArrayPatternConverter[] converterArray = new ArrayPatternConverter[converters.size()];
        this.patternConverters = converters.toArray(converterArray);
        for (final ArrayPatternConverter converter : this.patternConverters) {
            if (converter instanceof DatePatternConverter) {
                final DatePatternConverter dateConverter = (DatePatternConverter)converter;
                this.frequency = this.calculateFrequency(dateConverter.getPattern());
            }
        }
    }
    
    public long getNextTime(final long currentMillis, final int increment, final boolean modulus) {
        this.prevFileTime = this.nextFileTime;
        if (this.frequency == null) {
            throw new IllegalStateException("Pattern does not contain a date");
        }
        final Calendar currentCal = Calendar.getInstance();
        currentCal.setTimeInMillis(currentMillis);
        final Calendar cal = Calendar.getInstance();
        cal.set(currentCal.get(1), 0, 1, 0, 0, 0);
        cal.set(14, 0);
        if (this.frequency == RolloverFrequency.ANNUALLY) {
            this.increment(cal, 1, increment, modulus);
            final long nextTime = cal.getTimeInMillis();
            cal.add(1, -1);
            this.nextFileTime = cal.getTimeInMillis();
            return this.debugGetNextTime(nextTime);
        }
        cal.set(2, currentCal.get(2));
        if (this.frequency == RolloverFrequency.MONTHLY) {
            this.increment(cal, 2, increment, modulus);
            final long nextTime = cal.getTimeInMillis();
            cal.add(2, -1);
            this.nextFileTime = cal.getTimeInMillis();
            return this.debugGetNextTime(nextTime);
        }
        if (this.frequency == RolloverFrequency.WEEKLY) {
            cal.set(3, currentCal.get(3));
            this.increment(cal, 3, increment, modulus);
            cal.set(7, currentCal.getFirstDayOfWeek());
            final long nextTime = cal.getTimeInMillis();
            cal.add(3, -1);
            this.nextFileTime = cal.getTimeInMillis();
            return this.debugGetNextTime(nextTime);
        }
        cal.set(6, currentCal.get(6));
        if (this.frequency == RolloverFrequency.DAILY) {
            this.increment(cal, 6, increment, modulus);
            final long nextTime = cal.getTimeInMillis();
            cal.add(6, -1);
            this.nextFileTime = cal.getTimeInMillis();
            return this.debugGetNextTime(nextTime);
        }
        cal.set(11, currentCal.get(11));
        if (this.frequency == RolloverFrequency.HOURLY) {
            this.increment(cal, 11, increment, modulus);
            final long nextTime = cal.getTimeInMillis();
            cal.add(11, -1);
            this.nextFileTime = cal.getTimeInMillis();
            return this.debugGetNextTime(nextTime);
        }
        cal.set(12, currentCal.get(12));
        if (this.frequency == RolloverFrequency.EVERY_MINUTE) {
            this.increment(cal, 12, increment, modulus);
            final long nextTime = cal.getTimeInMillis();
            cal.add(12, -1);
            this.nextFileTime = cal.getTimeInMillis();
            return this.debugGetNextTime(nextTime);
        }
        cal.set(13, currentCal.get(13));
        if (this.frequency == RolloverFrequency.EVERY_SECOND) {
            this.increment(cal, 13, increment, modulus);
            final long nextTime = cal.getTimeInMillis();
            cal.add(13, -1);
            this.nextFileTime = cal.getTimeInMillis();
            return this.debugGetNextTime(nextTime);
        }
        cal.set(14, currentCal.get(14));
        this.increment(cal, 14, increment, modulus);
        final long nextTime = cal.getTimeInMillis();
        cal.add(14, -1);
        this.nextFileTime = cal.getTimeInMillis();
        return this.debugGetNextTime(nextTime);
    }
    
    public void updateTime() {
        this.prevFileTime = this.nextFileTime;
    }
    
    private long debugGetNextTime(final long nextTime) {
        if (PatternProcessor.LOGGER.isTraceEnabled()) {
            PatternProcessor.LOGGER.trace("PatternProcessor.getNextTime returning {}, nextFileTime={}, prevFileTime={}, current={}, freq={}", new Object[] { this.format(nextTime), this.format(this.nextFileTime), this.format(this.prevFileTime), this.format(System.currentTimeMillis()), this.frequency });
        }
        return nextTime;
    }
    
    private String format(final long time) {
        return new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss.SSS").format(new Date(time));
    }
    
    private void increment(final Calendar cal, final int type, final int increment, final boolean modulate) {
        final int interval = modulate ? (increment - cal.get(type) % increment) : increment;
        cal.add(type, interval);
    }
    
    public final void formatFileName(final StringBuilder buf, final Object obj) {
        final long time = (this.prevFileTime == 0L) ? System.currentTimeMillis() : this.prevFileTime;
        this.formatFileName(buf, new Date(time), obj);
    }
    
    public final void formatFileName(final StrSubstitutor subst, final StringBuilder buf, final Object obj) {
        final long time = (this.prevFileTime == 0L) ? System.currentTimeMillis() : this.prevFileTime;
        this.formatFileName(buf, new Date(time), obj);
        final LogEvent event = new Log4jLogEvent.Builder().setTimeMillis(time).build();
        final String fileName = subst.replace(event, buf);
        buf.setLength(0);
        buf.append(fileName);
    }
    
    protected final void formatFileName(final StringBuilder buf, final Object... objects) {
        for (int i = 0; i < this.patternConverters.length; ++i) {
            final int fieldStart = buf.length();
            this.patternConverters[i].format(buf, objects);
            if (this.patternFields[i] != null) {
                this.patternFields[i].format(fieldStart, buf);
            }
        }
    }
    
    private RolloverFrequency calculateFrequency(final String pattern) {
        if (this.patternContains(pattern, 'S')) {
            return RolloverFrequency.EVERY_MILLISECOND;
        }
        if (this.patternContains(pattern, 's')) {
            return RolloverFrequency.EVERY_SECOND;
        }
        if (this.patternContains(pattern, 'm')) {
            return RolloverFrequency.EVERY_MINUTE;
        }
        if (this.patternContains(pattern, PatternProcessor.HOUR_CHARS)) {
            return RolloverFrequency.HOURLY;
        }
        if (this.patternContains(pattern, PatternProcessor.DAY_CHARS)) {
            return RolloverFrequency.DAILY;
        }
        if (this.patternContains(pattern, PatternProcessor.WEEK_CHARS)) {
            return RolloverFrequency.WEEKLY;
        }
        if (this.patternContains(pattern, 'M')) {
            return RolloverFrequency.MONTHLY;
        }
        if (this.patternContains(pattern, 'y')) {
            return RolloverFrequency.ANNUALLY;
        }
        return null;
    }
    
    private PatternParser createPatternParser() {
        return new PatternParser(null, "FileConverter", null);
    }
    
    private boolean patternContains(final String pattern, final char... chars) {
        for (final char character : chars) {
            if (this.patternContains(pattern, character)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean patternContains(final String pattern, final char character) {
        return pattern.indexOf(character) >= 0;
    }
    
    static {
        LOGGER = StatusLogger.getLogger();
        WEEK_CHARS = new char[] { 'w', 'W' };
        DAY_CHARS = new char[] { 'D', 'd', 'F', 'E' };
        HOUR_CHARS = new char[] { 'H', 'K', 'h', 'k' };
    }
}
