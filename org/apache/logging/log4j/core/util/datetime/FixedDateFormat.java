// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.util.datetime;

import java.util.Calendar;
import java.util.Objects;

public class FixedDateFormat
{
    private final FixedFormat fixedFormat;
    private final int length;
    private final int dateLength;
    private final FastDateFormat fastDateFormat;
    private final char timeSeparatorChar;
    private final char millisSeparatorChar;
    private final int timeSeparatorLength;
    private final int millisSeparatorLength;
    private volatile long midnightToday;
    private volatile long midnightTomorrow;
    private char[] cachedDate;
    
    FixedDateFormat(final FixedFormat fixedFormat) {
        this.midnightToday = 0L;
        this.midnightTomorrow = 0L;
        this.fixedFormat = Objects.requireNonNull(fixedFormat);
        this.timeSeparatorChar = fixedFormat.timeSeparatorChar;
        this.timeSeparatorLength = fixedFormat.timeSeparatorLength;
        this.millisSeparatorChar = fixedFormat.millisSeparatorChar;
        this.millisSeparatorLength = fixedFormat.millisSeparatorLength;
        this.length = fixedFormat.getLength();
        this.dateLength = fixedFormat.getDatePatternLength();
        this.fastDateFormat = fixedFormat.getFastDateFormat();
    }
    
    public static FixedDateFormat createIfSupported(final String... options) {
        if (options == null || options.length == 0 || options[0] == null) {
            return new FixedDateFormat(FixedFormat.DEFAULT);
        }
        if (options.length > 1) {
            return null;
        }
        final FixedFormat type = FixedFormat.lookup(options[0]);
        return (type == null) ? null : new FixedDateFormat(type);
    }
    
    public static FixedDateFormat create(final FixedFormat format) {
        return new FixedDateFormat(format);
    }
    
    public String getFormat() {
        return this.fixedFormat.getPattern();
    }
    
    private long millisSinceMidnight(final long now) {
        if (now >= this.midnightTomorrow || now < this.midnightToday) {
            this.updateMidnightMillis(now);
        }
        return now - this.midnightToday;
    }
    
    private void updateMidnightMillis(final long now) {
        this.updateCachedDate(now);
        this.midnightToday = calcMidnightMillis(now, 0);
        this.midnightTomorrow = calcMidnightMillis(now, 1);
    }
    
    static long calcMidnightMillis(final long time, final int addDays) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        cal.set(11, 0);
        cal.set(12, 0);
        cal.set(13, 0);
        cal.set(14, 0);
        cal.add(5, addDays);
        return cal.getTimeInMillis();
    }
    
    private void updateCachedDate(final long now) {
        if (this.fastDateFormat != null) {
            final StringBuilder result = this.fastDateFormat.format(now, new StringBuilder());
            this.cachedDate = result.toString().toCharArray();
        }
    }
    
    public String format(final long time) {
        final char[] result = new char[this.length];
        final int written = this.format(time, result, 0);
        return new String(result, 0, written);
    }
    
    public int format(final long time, final char[] buffer, final int startPos) {
        final int ms = (int)this.millisSinceMidnight(time);
        this.writeDate(buffer, startPos);
        return this.writeTime(ms, buffer, startPos + this.dateLength) - startPos;
    }
    
    private void writeDate(final char[] buffer, final int startPos) {
        if (this.cachedDate != null) {
            System.arraycopy(this.cachedDate, 0, buffer, startPos, this.dateLength);
        }
    }
    
    private int writeTime(int ms, final char[] buffer, int pos) {
        final int hours = ms / 3600000;
        ms -= 3600000 * hours;
        final int minutes = ms / 60000;
        ms -= 60000 * minutes;
        final int seconds = ms / 1000;
        ms -= 1000 * seconds;
        int temp = hours / 10;
        buffer[pos++] = (char)(temp + 48);
        buffer[pos++] = (char)(hours - 10 * temp + 48);
        buffer[pos] = this.timeSeparatorChar;
        pos += this.timeSeparatorLength;
        temp = minutes / 10;
        buffer[pos++] = (char)(temp + 48);
        buffer[pos++] = (char)(minutes - 10 * temp + 48);
        buffer[pos] = this.timeSeparatorChar;
        pos += this.timeSeparatorLength;
        temp = seconds / 10;
        buffer[pos++] = (char)(temp + 48);
        buffer[pos++] = (char)(seconds - 10 * temp + 48);
        buffer[pos] = this.millisSeparatorChar;
        pos += this.millisSeparatorLength;
        temp = ms / 100;
        buffer[pos++] = (char)(temp + 48);
        ms -= 100 * temp;
        temp = ms / 10;
        buffer[pos++] = (char)(temp + 48);
        ms -= 10 * temp;
        buffer[pos++] = (char)(ms + 48);
        return pos;
    }
    
    public enum FixedFormat
    {
        ABSOLUTE("HH:mm:ss,SSS", (String)null, 0, ':', 1, ',', 1), 
        ABSOLUTE_PERIOD("HH:mm:ss.SSS", (String)null, 0, ':', 1, '.', 1), 
        COMPACT("yyyyMMddHHmmssSSS", "yyyyMMdd", 0, ' ', 0, ' ', 0), 
        DATE("dd MMM yyyy HH:mm:ss,SSS", "dd MMM yyyy ", 0, ':', 1, ',', 1), 
        DATE_PERIOD("dd MMM yyyy HH:mm:ss.SSS", "dd MMM yyyy ", 0, ':', 1, '.', 1), 
        DEFAULT("yyyy-MM-dd HH:mm:ss,SSS", "yyyy-MM-dd ", 0, ':', 1, ',', 1), 
        DEFAULT_PERIOD("yyyy-MM-dd HH:mm:ss.SSS", "yyyy-MM-dd ", 0, ':', 1, '.', 1), 
        ISO8601_BASIC("yyyyMMdd'T'HHmmss,SSS", "yyyyMMdd'T'", 2, ' ', 0, ',', 1), 
        ISO8601("yyyy-MM-dd'T'HH:mm:ss,SSS", "yyyy-MM-dd'T'", 2, ':', 1, ',', 1);
        
        private final String pattern;
        private final String datePattern;
        private final int escapeCount;
        private final char timeSeparatorChar;
        private final int timeSeparatorLength;
        private final char millisSeparatorChar;
        private final int millisSeparatorLength;
        
        private FixedFormat(final String pattern, final String datePattern, final int escapeCount, final char timeSeparator, final int timeSepLength, final char millisSeparator, final int millisSepLength) {
            this.timeSeparatorChar = timeSeparator;
            this.timeSeparatorLength = timeSepLength;
            this.millisSeparatorChar = millisSeparator;
            this.millisSeparatorLength = millisSepLength;
            this.pattern = Objects.requireNonNull(pattern);
            this.datePattern = datePattern;
            this.escapeCount = escapeCount;
        }
        
        public String getPattern() {
            return this.pattern;
        }
        
        public String getDatePattern() {
            return this.datePattern;
        }
        
        public static FixedFormat lookup(final String nameOrPattern) {
            for (final FixedFormat type : values()) {
                if (type.name().equals(nameOrPattern) || type.getPattern().equals(nameOrPattern)) {
                    return type;
                }
            }
            return null;
        }
        
        public int getLength() {
            return this.pattern.length() - this.escapeCount;
        }
        
        public int getDatePatternLength() {
            return (this.getDatePattern() == null) ? 0 : (this.getDatePattern().length() - this.escapeCount);
        }
        
        public FastDateFormat getFastDateFormat() {
            return (this.getDatePattern() == null) ? null : FastDateFormat.getInstance(this.getDatePattern());
        }
    }
}
