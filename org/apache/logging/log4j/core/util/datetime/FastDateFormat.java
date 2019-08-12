// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.util.datetime;

import java.text.ParsePosition;
import java.text.ParseException;
import java.util.Calendar;
import java.text.FieldPosition;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.io.Serializable;

public class FastDateFormat extends Format implements DatePrinter, DateParser, Serializable
{
    public static final int FULL = 0;
    public static final int LONG = 1;
    public static final int MEDIUM = 2;
    public static final int SHORT = 3;
    private static final long serialVersionUID = 2L;
    private static final FormatCache<FastDateFormat> CACHE;
    private final FastDatePrinter printer;
    private final FastDateParser parser;
    
    protected FastDateFormat(final String pattern, final TimeZone timeZone, final Locale locale) {
        this(pattern, timeZone, locale, null);
    }
    
    protected FastDateFormat(final String pattern, final TimeZone timeZone, final Locale locale, final Date centuryStart) {
        this.printer = new FastDatePrinter(pattern, timeZone, locale);
        this.parser = new FastDateParser(pattern, timeZone, locale, centuryStart);
    }
    
    public static FastDateFormat getInstance() {
        return FastDateFormat.CACHE.getInstance();
    }
    
    public static FastDateFormat getInstance(final String pattern) {
        return FastDateFormat.CACHE.getInstance(pattern, null, null);
    }
    
    public static FastDateFormat getInstance(final String pattern, final TimeZone timeZone) {
        return FastDateFormat.CACHE.getInstance(pattern, timeZone, null);
    }
    
    public static FastDateFormat getInstance(final String pattern, final Locale locale) {
        return FastDateFormat.CACHE.getInstance(pattern, null, locale);
    }
    
    public static FastDateFormat getInstance(final String pattern, final TimeZone timeZone, final Locale locale) {
        return FastDateFormat.CACHE.getInstance(pattern, timeZone, locale);
    }
    
    public static FastDateFormat getDateInstance(final int style) {
        return FastDateFormat.CACHE.getDateInstance(style, null, null);
    }
    
    public static FastDateFormat getDateInstance(final int style, final Locale locale) {
        return FastDateFormat.CACHE.getDateInstance(style, null, locale);
    }
    
    public static FastDateFormat getDateInstance(final int style, final TimeZone timeZone) {
        return FastDateFormat.CACHE.getDateInstance(style, timeZone, null);
    }
    
    public static FastDateFormat getDateInstance(final int style, final TimeZone timeZone, final Locale locale) {
        return FastDateFormat.CACHE.getDateInstance(style, timeZone, locale);
    }
    
    public static FastDateFormat getTimeInstance(final int style) {
        return FastDateFormat.CACHE.getTimeInstance(style, null, null);
    }
    
    public static FastDateFormat getTimeInstance(final int style, final Locale locale) {
        return FastDateFormat.CACHE.getTimeInstance(style, null, locale);
    }
    
    public static FastDateFormat getTimeInstance(final int style, final TimeZone timeZone) {
        return FastDateFormat.CACHE.getTimeInstance(style, timeZone, null);
    }
    
    public static FastDateFormat getTimeInstance(final int style, final TimeZone timeZone, final Locale locale) {
        return FastDateFormat.CACHE.getTimeInstance(style, timeZone, locale);
    }
    
    public static FastDateFormat getDateTimeInstance(final int dateStyle, final int timeStyle) {
        return FastDateFormat.CACHE.getDateTimeInstance(dateStyle, timeStyle, null, null);
    }
    
    public static FastDateFormat getDateTimeInstance(final int dateStyle, final int timeStyle, final Locale locale) {
        return FastDateFormat.CACHE.getDateTimeInstance(dateStyle, timeStyle, null, locale);
    }
    
    public static FastDateFormat getDateTimeInstance(final int dateStyle, final int timeStyle, final TimeZone timeZone) {
        return getDateTimeInstance(dateStyle, timeStyle, timeZone, null);
    }
    
    public static FastDateFormat getDateTimeInstance(final int dateStyle, final int timeStyle, final TimeZone timeZone, final Locale locale) {
        return FastDateFormat.CACHE.getDateTimeInstance(dateStyle, timeStyle, timeZone, locale);
    }
    
    @Override
    public StringBuilder format(final Object obj, final StringBuilder toAppendTo, final FieldPosition pos) {
        return this.printer.format(obj, toAppendTo, pos);
    }
    
    @Override
    public String format(final long millis) {
        return this.printer.format(millis);
    }
    
    @Override
    public String format(final Date date) {
        return this.printer.format(date);
    }
    
    @Override
    public String format(final Calendar calendar) {
        return this.printer.format(calendar);
    }
    
    @Override
    public StringBuilder format(final long millis, final StringBuilder buf) {
        return this.printer.format(millis, buf);
    }
    
    @Override
    public StringBuilder format(final Date date, final StringBuilder buf) {
        return this.printer.format(date, buf);
    }
    
    @Override
    public StringBuilder format(final Calendar calendar, final StringBuilder buf) {
        return this.printer.format(calendar, buf);
    }
    
    @Override
    public Date parse(final String source) throws ParseException {
        return this.parser.parse(source);
    }
    
    @Override
    public Date parse(final String source, final ParsePosition pos) {
        return this.parser.parse(source, pos);
    }
    
    @Override
    public Object parseObject(final String source, final ParsePosition pos) {
        return this.parser.parseObject(source, pos);
    }
    
    @Override
    public String getPattern() {
        return this.printer.getPattern();
    }
    
    @Override
    public TimeZone getTimeZone() {
        return this.printer.getTimeZone();
    }
    
    @Override
    public Locale getLocale() {
        return this.printer.getLocale();
    }
    
    public int getMaxLengthEstimate() {
        return this.printer.getMaxLengthEstimate();
    }
    
    public String toPattern() {
        return this.printer.getPattern();
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof FastDateFormat)) {
            return false;
        }
        final FastDateFormat other = (FastDateFormat)obj;
        return this.printer.equals(other.printer);
    }
    
    @Override
    public int hashCode() {
        return this.printer.hashCode();
    }
    
    @Override
    public String toString() {
        return "FastDateFormat[" + this.printer.getPattern() + "," + this.printer.getLocale() + "," + this.printer.getTimeZone().getID() + "]";
    }
    
    protected StringBuilder applyRules(final Calendar calendar, final StringBuilder buf) {
        return this.printer.applyRules(calendar, buf);
    }
    
    static {
        CACHE = new FormatCache<FastDateFormat>() {
            @Override
            protected FastDateFormat createInstance(final String pattern, final TimeZone timeZone, final Locale locale) {
                return new FastDateFormat(pattern, timeZone, locale);
            }
        };
    }
}
