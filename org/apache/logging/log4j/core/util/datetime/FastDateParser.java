// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.util.datetime;

import java.text.DateFormatSymbols;
import java.util.Iterator;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.text.ParsePosition;
import java.text.ParseException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.regex.Matcher;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import java.util.TimeZone;
import java.util.Locale;
import java.io.Serializable;

public class FastDateParser implements DateParser, Serializable
{
    static final Locale JAPANESE_IMPERIAL;
    private static final long serialVersionUID = 3L;
    private static final Strategy NUMBER_MONTH_STRATEGY;
    private static final Strategy ABBREVIATED_YEAR_STRATEGY;
    private static final Strategy LITERAL_YEAR_STRATEGY;
    private static final Strategy WEEK_OF_YEAR_STRATEGY;
    private static final Strategy WEEK_OF_MONTH_STRATEGY;
    private static final Strategy DAY_OF_YEAR_STRATEGY;
    private static final Strategy DAY_OF_MONTH_STRATEGY;
    private static final Strategy DAY_OF_WEEK_IN_MONTH_STRATEGY;
    private static final Strategy DAY_OF_WEEK_STRATEGY;
    private static final Strategy HOUR_OF_DAY_STRATEGY;
    private static final Strategy HOUR24_OF_DAY_STRATEGY;
    private static final Strategy HOUR12_STRATEGY;
    private static final Strategy HOUR_STRATEGY;
    private static final Strategy MINUTE_STRATEGY;
    private static final Strategy SECOND_STRATEGY;
    private static final Strategy MILLISECOND_STRATEGY;
    private static final Strategy ISO_8601_STRATEGY;
    private final String pattern;
    private final TimeZone timeZone;
    private final Locale locale;
    private final int century;
    private final int startYear;
    private final boolean lenient;
    private transient Pattern parsePattern;
    private transient Strategy[] strategies;
    private transient String currentFormatField;
    private transient Strategy nextStrategy;
    private static final Pattern formatPattern;
    private static final ConcurrentMap<Locale, Strategy>[] caches;
    
    protected FastDateParser(final String pattern, final TimeZone timeZone, final Locale locale) {
        this(pattern, timeZone, locale, null, true);
    }
    
    protected FastDateParser(final String pattern, final TimeZone timeZone, final Locale locale, final Date centuryStart) {
        this(pattern, timeZone, locale, centuryStart, true);
    }
    
    protected FastDateParser(final String pattern, final TimeZone timeZone, final Locale locale, final Date centuryStart, final boolean lenient) {
        this.pattern = pattern;
        this.timeZone = timeZone;
        this.locale = locale;
        this.lenient = lenient;
        final Calendar definingCalendar = Calendar.getInstance(timeZone, locale);
        int centuryStartYear;
        if (centuryStart != null) {
            definingCalendar.setTime(centuryStart);
            centuryStartYear = definingCalendar.get(1);
        }
        else if (locale.equals(FastDateParser.JAPANESE_IMPERIAL)) {
            centuryStartYear = 0;
        }
        else {
            definingCalendar.setTime(new Date());
            centuryStartYear = definingCalendar.get(1) - 80;
        }
        this.century = centuryStartYear / 100 * 100;
        this.startYear = centuryStartYear - this.century;
        this.init(definingCalendar);
    }
    
    private void init(final Calendar definingCalendar) {
        final StringBuilder regex = new StringBuilder();
        final List<Strategy> collector = new ArrayList<Strategy>();
        final Matcher patternMatcher = FastDateParser.formatPattern.matcher(this.pattern);
        if (!patternMatcher.lookingAt()) {
            throw new IllegalArgumentException("Illegal pattern character '" + this.pattern.charAt(patternMatcher.regionStart()) + "'");
        }
        this.currentFormatField = patternMatcher.group();
        Strategy currentStrategy = this.getStrategy(this.currentFormatField, definingCalendar);
        while (true) {
            patternMatcher.region(patternMatcher.end(), patternMatcher.regionEnd());
            if (!patternMatcher.lookingAt()) {
                break;
            }
            final String nextFormatField = patternMatcher.group();
            this.nextStrategy = this.getStrategy(nextFormatField, definingCalendar);
            if (currentStrategy.addRegex(this, regex)) {
                collector.add(currentStrategy);
            }
            this.currentFormatField = nextFormatField;
            currentStrategy = this.nextStrategy;
        }
        this.nextStrategy = null;
        if (patternMatcher.regionStart() != patternMatcher.regionEnd()) {
            throw new IllegalArgumentException("Failed to parse \"" + this.pattern + "\" ; gave up at index " + patternMatcher.regionStart());
        }
        if (currentStrategy.addRegex(this, regex)) {
            collector.add(currentStrategy);
        }
        this.currentFormatField = null;
        this.strategies = collector.toArray(new Strategy[collector.size()]);
        this.parsePattern = Pattern.compile(regex.toString());
    }
    
    @Override
    public String getPattern() {
        return this.pattern;
    }
    
    @Override
    public TimeZone getTimeZone() {
        return this.timeZone;
    }
    
    @Override
    public Locale getLocale() {
        return this.locale;
    }
    
    Pattern getParsePattern() {
        return this.parsePattern;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof FastDateParser)) {
            return false;
        }
        final FastDateParser other = (FastDateParser)obj;
        return this.pattern.equals(other.pattern) && this.timeZone.equals(other.timeZone) && this.locale.equals(other.locale);
    }
    
    @Override
    public int hashCode() {
        return this.pattern.hashCode() + 13 * (this.timeZone.hashCode() + 13 * this.locale.hashCode());
    }
    
    @Override
    public String toString() {
        return "FastDateParser[" + this.pattern + "," + this.locale + "," + this.timeZone.getID() + "]";
    }
    
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        final Calendar definingCalendar = Calendar.getInstance(this.timeZone, this.locale);
        this.init(definingCalendar);
    }
    
    @Override
    public Object parseObject(final String source) throws ParseException {
        return this.parse(source);
    }
    
    @Override
    public Date parse(final String source) throws ParseException {
        final Date date = this.parse(source, new ParsePosition(0));
        if (date != null) {
            return date;
        }
        if (this.locale.equals(FastDateParser.JAPANESE_IMPERIAL)) {
            throw new ParseException("(The " + this.locale + " locale does not support dates before 1868 AD)\n" + "Unparseable date: \"" + source + "\" does not match " + this.parsePattern.pattern(), 0);
        }
        throw new ParseException("Unparseable date: \"" + source + "\" does not match " + this.parsePattern.pattern(), 0);
    }
    
    @Override
    public Object parseObject(final String source, final ParsePosition pos) {
        return this.parse(source, pos);
    }
    
    @Override
    public Date parse(final String source, final ParsePosition pos) {
        final int offset = pos.getIndex();
        final Matcher matcher = this.parsePattern.matcher(source.substring(offset));
        if (!matcher.lookingAt()) {
            return null;
        }
        final Calendar cal = Calendar.getInstance(this.timeZone, this.locale);
        cal.clear();
        cal.setLenient(this.lenient);
        int i = 0;
        while (i < this.strategies.length) {
            final Strategy strategy = this.strategies[i++];
            strategy.setCalendar(this, cal, matcher.group(i));
        }
        pos.setIndex(offset + matcher.end());
        return cal.getTime();
    }
    
    private static StringBuilder simpleQuote(final StringBuilder sb, final String value) {
        for (int i = 0; i < value.length(); ++i) {
            final char c = value.charAt(i);
            switch (c) {
                case '$':
                case '(':
                case ')':
                case '*':
                case '+':
                case '.':
                case '?':
                case '[':
                case '\\':
                case '^':
                case '{':
                case '|': {
                    sb.append('\\');
                    break;
                }
            }
            sb.append(c);
        }
        return sb;
    }
    
    private static StringBuilder escapeRegex(final StringBuilder regex, final String value, final boolean unquote) {
        regex.append("\\Q");
        for (int i = 0; i < value.length(); ++i) {
            char c = value.charAt(i);
            switch (c) {
                case '\'': {
                    if (!unquote) {
                        break;
                    }
                    if (++i == value.length()) {
                        return regex;
                    }
                    c = value.charAt(i);
                    break;
                }
                case '\\': {
                    if (++i == value.length()) {
                        break;
                    }
                    regex.append(c);
                    c = value.charAt(i);
                    if (c == 'E') {
                        regex.append("E\\\\E\\");
                        c = 'Q';
                        break;
                    }
                    break;
                }
            }
            regex.append(c);
        }
        regex.append("\\E");
        return regex;
    }
    
    private static Map<String, Integer> getDisplayNames(final int field, final Calendar definingCalendar, final Locale locale) {
        return definingCalendar.getDisplayNames(field, 0, locale);
    }
    
    private int adjustYear(final int twoDigitYear) {
        final int trial = this.century + twoDigitYear;
        return (twoDigitYear >= this.startYear) ? trial : (trial + 100);
    }
    
    boolean isNextNumber() {
        return this.nextStrategy != null && this.nextStrategy.isNumber();
    }
    
    int getFieldWidth() {
        return this.currentFormatField.length();
    }
    
    private Strategy getStrategy(final String formatField, final Calendar definingCalendar) {
        switch (formatField.charAt(0)) {
            case '\'': {
                if (formatField.length() > 2) {
                    return new CopyQuotedStrategy(formatField.substring(1, formatField.length() - 1));
                }
                break;
            }
            case 'D': {
                return FastDateParser.DAY_OF_YEAR_STRATEGY;
            }
            case 'E': {
                return this.getLocaleSpecificStrategy(7, definingCalendar);
            }
            case 'F': {
                return FastDateParser.DAY_OF_WEEK_IN_MONTH_STRATEGY;
            }
            case 'G': {
                return this.getLocaleSpecificStrategy(0, definingCalendar);
            }
            case 'H': {
                return FastDateParser.HOUR_OF_DAY_STRATEGY;
            }
            case 'K': {
                return FastDateParser.HOUR_STRATEGY;
            }
            case 'M': {
                return (formatField.length() >= 3) ? this.getLocaleSpecificStrategy(2, definingCalendar) : FastDateParser.NUMBER_MONTH_STRATEGY;
            }
            case 'S': {
                return FastDateParser.MILLISECOND_STRATEGY;
            }
            case 'W': {
                return FastDateParser.WEEK_OF_MONTH_STRATEGY;
            }
            case 'a': {
                return this.getLocaleSpecificStrategy(9, definingCalendar);
            }
            case 'd': {
                return FastDateParser.DAY_OF_MONTH_STRATEGY;
            }
            case 'h': {
                return FastDateParser.HOUR12_STRATEGY;
            }
            case 'k': {
                return FastDateParser.HOUR24_OF_DAY_STRATEGY;
            }
            case 'm': {
                return FastDateParser.MINUTE_STRATEGY;
            }
            case 's': {
                return FastDateParser.SECOND_STRATEGY;
            }
            case 'u': {
                return FastDateParser.DAY_OF_WEEK_STRATEGY;
            }
            case 'w': {
                return FastDateParser.WEEK_OF_YEAR_STRATEGY;
            }
            case 'y': {
                return (formatField.length() > 2) ? FastDateParser.LITERAL_YEAR_STRATEGY : FastDateParser.ABBREVIATED_YEAR_STRATEGY;
            }
            case 'X': {
                return ISO8601TimeZoneStrategy.getStrategy(formatField.length());
            }
            case 'Z': {
                if (formatField.equals("ZZ")) {
                    return FastDateParser.ISO_8601_STRATEGY;
                }
                return this.getLocaleSpecificStrategy(15, definingCalendar);
            }
            case 'z': {
                return this.getLocaleSpecificStrategy(15, definingCalendar);
            }
        }
        return new CopyQuotedStrategy(formatField);
    }
    
    private static ConcurrentMap<Locale, Strategy> getCache(final int field) {
        synchronized (FastDateParser.caches) {
            if (FastDateParser.caches[field] == null) {
                FastDateParser.caches[field] = new ConcurrentHashMap<Locale, Strategy>(3);
            }
            return FastDateParser.caches[field];
        }
    }
    
    private Strategy getLocaleSpecificStrategy(final int field, final Calendar definingCalendar) {
        final ConcurrentMap<Locale, Strategy> cache = getCache(field);
        Strategy strategy = cache.get(this.locale);
        if (strategy == null) {
            strategy = ((field == 15) ? new TimeZoneStrategy(this.locale) : new CaseInsensitiveTextStrategy(field, definingCalendar, this.locale));
            final Strategy inCache = cache.putIfAbsent(this.locale, strategy);
            if (inCache != null) {
                return inCache;
            }
        }
        return strategy;
    }
    
    static {
        JAPANESE_IMPERIAL = new Locale("ja", "JP", "JP");
        NUMBER_MONTH_STRATEGY = new NumberStrategy(2) {
            @Override
            int modify(final int iValue) {
                return iValue - 1;
            }
        };
        ABBREVIATED_YEAR_STRATEGY = new NumberStrategy(1) {
            @Override
            void setCalendar(final FastDateParser parser, final Calendar cal, final String value) {
                int iValue = Integer.parseInt(value);
                if (iValue < 100) {
                    iValue = parser.adjustYear(iValue);
                }
                cal.set(1, iValue);
            }
        };
        LITERAL_YEAR_STRATEGY = new NumberStrategy(1);
        WEEK_OF_YEAR_STRATEGY = new NumberStrategy(3);
        WEEK_OF_MONTH_STRATEGY = new NumberStrategy(4);
        DAY_OF_YEAR_STRATEGY = new NumberStrategy(6);
        DAY_OF_MONTH_STRATEGY = new NumberStrategy(5);
        DAY_OF_WEEK_IN_MONTH_STRATEGY = new NumberStrategy(8);
        DAY_OF_WEEK_STRATEGY = new NumberStrategy(7);
        HOUR_OF_DAY_STRATEGY = new NumberStrategy(11);
        HOUR24_OF_DAY_STRATEGY = new NumberStrategy(11) {
            @Override
            int modify(final int iValue) {
                return (iValue == 24) ? 0 : iValue;
            }
        };
        HOUR12_STRATEGY = new NumberStrategy(10) {
            @Override
            int modify(final int iValue) {
                return (iValue == 12) ? 0 : iValue;
            }
        };
        HOUR_STRATEGY = new NumberStrategy(10);
        MINUTE_STRATEGY = new NumberStrategy(12);
        SECOND_STRATEGY = new NumberStrategy(13);
        MILLISECOND_STRATEGY = new NumberStrategy(14);
        ISO_8601_STRATEGY = new ISO8601TimeZoneStrategy("(Z|(?:[+-]\\d{2}(?::?\\d{2})?))");
        formatPattern = Pattern.compile("D+|E+|F+|G+|H+|K+|M+|S+|W+|X+|Z+|a+|d+|h+|k+|m+|s+|u+|w+|y+|z+|''|'[^']++(''[^']*+)*+'|[^'A-Za-z]++");
        caches = new ConcurrentMap[17];
    }
    
    private abstract static class Strategy
    {
        boolean isNumber() {
            return false;
        }
        
        void setCalendar(final FastDateParser parser, final Calendar cal, final String value) {
        }
        
        abstract boolean addRegex(final FastDateParser p0, final StringBuilder p1);
    }
    
    private static class CopyQuotedStrategy extends Strategy
    {
        private final String formatField;
        
        CopyQuotedStrategy(final String formatField) {
            this.formatField = formatField;
        }
        
        @Override
        boolean isNumber() {
            char c = this.formatField.charAt(0);
            if (c == '\'') {
                c = this.formatField.charAt(1);
            }
            return Character.isDigit(c);
        }
        
        @Override
        boolean addRegex(final FastDateParser parser, final StringBuilder regex) {
            escapeRegex(regex, this.formatField, true);
            return false;
        }
    }
    
    private static class CaseInsensitiveTextStrategy extends Strategy
    {
        private final int field;
        private final Locale locale;
        private final Map<String, Integer> lKeyValues;
        
        CaseInsensitiveTextStrategy(final int field, final Calendar definingCalendar, final Locale locale) {
            this.field = field;
            this.locale = locale;
            final Map<String, Integer> keyValues = getDisplayNames(field, definingCalendar, locale);
            this.lKeyValues = new HashMap<String, Integer>();
            for (final Map.Entry<String, Integer> entry : keyValues.entrySet()) {
                this.lKeyValues.put(entry.getKey().toLowerCase(locale), entry.getValue());
            }
        }
        
        @Override
        boolean addRegex(final FastDateParser parser, final StringBuilder regex) {
            regex.append("((?iu)");
            for (final String textKeyValue : this.lKeyValues.keySet()) {
                simpleQuote(regex, textKeyValue).append('|');
            }
            regex.setCharAt(regex.length() - 1, ')');
            return true;
        }
        
        @Override
        void setCalendar(final FastDateParser parser, final Calendar cal, final String value) {
            final Integer iVal = this.lKeyValues.get(value.toLowerCase(this.locale));
            if (iVal == null) {
                final StringBuilder sb = new StringBuilder(value);
                sb.append(" not in (");
                for (final String textKeyValue : this.lKeyValues.keySet()) {
                    sb.append(textKeyValue).append(' ');
                }
                sb.setCharAt(sb.length() - 1, ')');
                throw new IllegalArgumentException(sb.toString());
            }
            cal.set(this.field, iVal);
        }
    }
    
    private static class NumberStrategy extends Strategy
    {
        private final int field;
        
        NumberStrategy(final int field) {
            this.field = field;
        }
        
        @Override
        boolean isNumber() {
            return true;
        }
        
        @Override
        boolean addRegex(final FastDateParser parser, final StringBuilder regex) {
            if (parser.isNextNumber()) {
                regex.append("(\\p{Nd}{").append(parser.getFieldWidth()).append("}+)");
            }
            else {
                regex.append("(\\p{Nd}++)");
            }
            return true;
        }
        
        @Override
        void setCalendar(final FastDateParser parser, final Calendar cal, final String value) {
            cal.set(this.field, this.modify(Integer.parseInt(value)));
        }
        
        int modify(final int iValue) {
            return iValue;
        }
    }
    
    static class TimeZoneStrategy extends Strategy
    {
        private static final String RFC_822_TIME_ZONE = "[+-]\\d{4}";
        private static final String GMT_OPTION = "GMT[+-]\\d{1,2}:\\d{2}";
        private final Locale locale;
        private final Map<String, TimeZone> tzNames;
        private final String validTimeZoneChars;
        private static final int ID = 0;
        
        TimeZoneStrategy(final Locale locale) {
            this.tzNames = new HashMap<String, TimeZone>();
            this.locale = locale;
            final StringBuilder sb = new StringBuilder();
            sb.append("([+-]\\d{4}|(?iu)GMT[+-]\\d{1,2}:\\d{2}");
            final String[][] arr$;
            final String[][] zones = arr$ = DateFormatSymbols.getInstance(locale).getZoneStrings();
            for (final String[] zoneNames : arr$) {
                final String tzId = zoneNames[0];
                if (!tzId.equalsIgnoreCase("GMT")) {
                    final TimeZone tz = TimeZone.getTimeZone(tzId);
                    for (int i = 1; i < zoneNames.length; ++i) {
                        final String zoneName = zoneNames[i].toLowerCase(locale);
                        if (!this.tzNames.containsKey(zoneName)) {
                            this.tzNames.put(zoneName, tz);
                            simpleQuote(sb.append('|'), zoneName);
                        }
                    }
                }
            }
            sb.append(')');
            this.validTimeZoneChars = sb.toString();
        }
        
        @Override
        boolean addRegex(final FastDateParser parser, final StringBuilder regex) {
            regex.append(this.validTimeZoneChars);
            return true;
        }
        
        @Override
        void setCalendar(final FastDateParser parser, final Calendar cal, final String value) {
            TimeZone tz;
            if (value.charAt(0) == '+' || value.charAt(0) == '-') {
                tz = TimeZone.getTimeZone("GMT" + value);
            }
            else if (value.regionMatches(true, 0, "GMT", 0, 3)) {
                tz = TimeZone.getTimeZone(value.toUpperCase());
            }
            else {
                tz = this.tzNames.get(value.toLowerCase(this.locale));
                if (tz == null) {
                    throw new IllegalArgumentException(value + " is not a supported timezone name");
                }
            }
            cal.setTimeZone(tz);
        }
    }
    
    private static class ISO8601TimeZoneStrategy extends Strategy
    {
        private final String pattern;
        private static final Strategy ISO_8601_1_STRATEGY;
        private static final Strategy ISO_8601_2_STRATEGY;
        private static final Strategy ISO_8601_3_STRATEGY;
        
        ISO8601TimeZoneStrategy(final String pattern) {
            this.pattern = pattern;
        }
        
        @Override
        boolean addRegex(final FastDateParser parser, final StringBuilder regex) {
            regex.append(this.pattern);
            return true;
        }
        
        @Override
        void setCalendar(final FastDateParser parser, final Calendar cal, final String value) {
            if (value.equals("Z")) {
                cal.setTimeZone(TimeZone.getTimeZone("UTC"));
            }
            else {
                cal.setTimeZone(TimeZone.getTimeZone("GMT" + value));
            }
        }
        
        static Strategy getStrategy(final int tokenLen) {
            switch (tokenLen) {
                case 1: {
                    return ISO8601TimeZoneStrategy.ISO_8601_1_STRATEGY;
                }
                case 2: {
                    return ISO8601TimeZoneStrategy.ISO_8601_2_STRATEGY;
                }
                case 3: {
                    return ISO8601TimeZoneStrategy.ISO_8601_3_STRATEGY;
                }
                default: {
                    throw new IllegalArgumentException("invalid number of X");
                }
            }
        }
        
        static {
            ISO_8601_1_STRATEGY = new ISO8601TimeZoneStrategy("(Z|(?:[+-]\\d{2}))");
            ISO_8601_2_STRATEGY = new ISO8601TimeZoneStrategy("(Z|(?:[+-]\\d{2}\\d{2}))");
            ISO_8601_3_STRATEGY = new ISO8601TimeZoneStrategy("(Z|(?:[+-]\\d{2}(?::)\\d{2}))");
        }
    }
}
