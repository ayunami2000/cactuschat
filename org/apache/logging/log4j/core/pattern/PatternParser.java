// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.core.pattern;

import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.Strings;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;
import org.apache.logging.log4j.core.util.NanoClockFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import org.apache.logging.log4j.core.config.plugins.util.PluginType;
import java.util.LinkedHashMap;
import org.apache.logging.log4j.core.config.plugins.util.PluginManager;
import java.util.Map;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.Logger;

public final class PatternParser
{
    static final String NO_CONSOLE_NO_ANSI = "noConsoleNoAnsi";
    private static final char ESCAPE_CHAR = '%';
    private static final Logger LOGGER;
    private static final int BUF_SIZE = 32;
    private static final int DECIMAL = 10;
    private final Configuration config;
    private final Map<String, Class<PatternConverter>> converterRules;
    
    public PatternParser(final String converterKey) {
        this(null, converterKey, null, null);
    }
    
    public PatternParser(final Configuration config, final String converterKey, final Class<?> expected) {
        this(config, converterKey, expected, null);
    }
    
    public PatternParser(final Configuration config, final String converterKey, final Class<?> expectedClass, final Class<?> filterClass) {
        this.config = config;
        final PluginManager manager = new PluginManager(converterKey);
        manager.collectPlugins((config == null) ? null : config.getPluginPackages());
        final Map<String, PluginType<?>> plugins = manager.getPlugins();
        final Map<String, Class<PatternConverter>> converters = new LinkedHashMap<String, Class<PatternConverter>>();
        for (final PluginType<?> type : plugins.values()) {
            try {
                final Class<PatternConverter> clazz = (Class<PatternConverter>)type.getPluginClass();
                if (filterClass != null && !filterClass.isAssignableFrom(clazz)) {
                    continue;
                }
                final ConverterKeys keys = clazz.getAnnotation(ConverterKeys.class);
                if (keys == null) {
                    continue;
                }
                for (final String key : keys.value()) {
                    if (converters.containsKey(key)) {
                        PatternParser.LOGGER.warn("Converter key '{}' is already mapped to '{}'. Sorry, Dave, I can't let you do that! Ignoring plugin [{}].", new Object[] { key, converters.get(key), clazz });
                    }
                    else {
                        converters.put(key, clazz);
                    }
                }
            }
            catch (Exception ex) {
                PatternParser.LOGGER.error("Error processing plugin " + type.getElementName(), ex);
            }
        }
        this.converterRules = converters;
    }
    
    public List<PatternFormatter> parse(final String pattern) {
        return this.parse(pattern, false, false);
    }
    
    public List<PatternFormatter> parse(final String pattern, final boolean alwaysWriteExceptions, final boolean noConsoleNoAnsi) {
        final List<PatternFormatter> list = new ArrayList<PatternFormatter>();
        final List<PatternConverter> converters = new ArrayList<PatternConverter>();
        final List<FormattingInfo> fields = new ArrayList<FormattingInfo>();
        this.parse(pattern, converters, fields, noConsoleNoAnsi, true);
        final Iterator<FormattingInfo> fieldIter = fields.iterator();
        boolean handlesThrowable = false;
        NanoClockFactory.setMode(NanoClockFactory.Mode.Dummy);
        for (final PatternConverter converter : converters) {
            if (converter instanceof NanoTimePatternConverter) {
                NanoClockFactory.setMode(NanoClockFactory.Mode.System);
            }
            LogEventPatternConverter pc;
            if (converter instanceof LogEventPatternConverter) {
                pc = (LogEventPatternConverter)converter;
                handlesThrowable |= pc.handlesThrowable();
            }
            else {
                pc = new LiteralPatternConverter(this.config, "", true);
            }
            FormattingInfo field;
            if (fieldIter.hasNext()) {
                field = fieldIter.next();
            }
            else {
                field = FormattingInfo.getDefault();
            }
            list.add(new PatternFormatter(pc, field));
        }
        if (alwaysWriteExceptions && !handlesThrowable) {
            final LogEventPatternConverter pc2 = ExtendedThrowablePatternConverter.newInstance(null);
            list.add(new PatternFormatter(pc2, FormattingInfo.getDefault()));
        }
        return list;
    }
    
    private static int extractConverter(final char lastChar, final String pattern, final int start, final StringBuilder convBuf, final StringBuilder currentLiteral) {
        int i = start;
        convBuf.setLength(0);
        if (!Character.isUnicodeIdentifierStart(lastChar)) {
            return i;
        }
        convBuf.append(lastChar);
        while (i < pattern.length() && Character.isUnicodeIdentifierPart(pattern.charAt(i))) {
            convBuf.append(pattern.charAt(i));
            currentLiteral.append(pattern.charAt(i));
            ++i;
        }
        return i;
    }
    
    private static int extractOptions(final String pattern, final int start, final List<String> options) {
        int i;
        int end;
        for (i = start; i < pattern.length() && pattern.charAt(i) == '{'; i = end + 1) {
            final int begin = i++;
            int depth = 0;
            do {
                end = pattern.indexOf(125, i);
                if (end == -1) {
                    break;
                }
                final int next = pattern.indexOf("{", i);
                if (next != -1 && next < end) {
                    i = end + 1;
                    ++depth;
                }
                else {
                    if (depth <= 0) {
                        continue;
                    }
                    --depth;
                }
            } while (depth > 0);
            if (end == -1) {
                break;
            }
            final String r = pattern.substring(begin + 1, end);
            options.add(r);
        }
        return i;
    }
    
    public void parse(final String pattern, final List<PatternConverter> patternConverters, final List<FormattingInfo> formattingInfos, final boolean noConsoleNoAnsi, final boolean convertBackslashes) {
        Objects.requireNonNull(pattern, "pattern");
        final StringBuilder currentLiteral = new StringBuilder(32);
        final int patternLength = pattern.length();
        ParserState state = ParserState.LITERAL_STATE;
        int i = 0;
        FormattingInfo formattingInfo = FormattingInfo.getDefault();
        while (i < patternLength) {
            final char c = pattern.charAt(i++);
            switch (state) {
                case LITERAL_STATE: {
                    if (i == patternLength) {
                        currentLiteral.append(c);
                        continue;
                    }
                    if (c != '%') {
                        currentLiteral.append(c);
                        continue;
                    }
                    switch (pattern.charAt(i)) {
                        case '%': {
                            currentLiteral.append(c);
                            ++i;
                            continue;
                        }
                        default: {
                            if (currentLiteral.length() != 0) {
                                patternConverters.add(new LiteralPatternConverter(this.config, currentLiteral.toString(), convertBackslashes));
                                formattingInfos.add(FormattingInfo.getDefault());
                            }
                            currentLiteral.setLength(0);
                            currentLiteral.append(c);
                            state = ParserState.CONVERTER_STATE;
                            formattingInfo = FormattingInfo.getDefault();
                            continue;
                        }
                    }
                    break;
                }
                case CONVERTER_STATE: {
                    currentLiteral.append(c);
                    switch (c) {
                        case '-': {
                            formattingInfo = new FormattingInfo(true, formattingInfo.getMinLength(), formattingInfo.getMaxLength(), formattingInfo.isLeftTruncate());
                            continue;
                        }
                        case '.': {
                            state = ParserState.DOT_STATE;
                            continue;
                        }
                        default: {
                            if (c >= '0' && c <= '9') {
                                formattingInfo = new FormattingInfo(formattingInfo.isLeftAligned(), c - '0', formattingInfo.getMaxLength(), formattingInfo.isLeftTruncate());
                                state = ParserState.MIN_STATE;
                                continue;
                            }
                            i = this.finalizeConverter(c, pattern, i, currentLiteral, formattingInfo, this.converterRules, patternConverters, formattingInfos, noConsoleNoAnsi, convertBackslashes);
                            state = ParserState.LITERAL_STATE;
                            formattingInfo = FormattingInfo.getDefault();
                            currentLiteral.setLength(0);
                            continue;
                        }
                    }
                    break;
                }
                case MIN_STATE: {
                    currentLiteral.append(c);
                    if (c >= '0' && c <= '9') {
                        formattingInfo = new FormattingInfo(formattingInfo.isLeftAligned(), formattingInfo.getMinLength() * 10 + c - 48, formattingInfo.getMaxLength(), formattingInfo.isLeftTruncate());
                        continue;
                    }
                    if (c == '.') {
                        state = ParserState.DOT_STATE;
                        continue;
                    }
                    i = this.finalizeConverter(c, pattern, i, currentLiteral, formattingInfo, this.converterRules, patternConverters, formattingInfos, noConsoleNoAnsi, convertBackslashes);
                    state = ParserState.LITERAL_STATE;
                    formattingInfo = FormattingInfo.getDefault();
                    currentLiteral.setLength(0);
                    continue;
                }
                case DOT_STATE: {
                    currentLiteral.append(c);
                    switch (c) {
                        case '-': {
                            formattingInfo = new FormattingInfo(formattingInfo.isLeftAligned(), formattingInfo.getMinLength(), formattingInfo.getMaxLength(), false);
                            continue;
                        }
                        default: {
                            if (c >= '0' && c <= '9') {
                                formattingInfo = new FormattingInfo(formattingInfo.isLeftAligned(), formattingInfo.getMinLength(), c - '0', formattingInfo.isLeftTruncate());
                                state = ParserState.MAX_STATE;
                                continue;
                            }
                            PatternParser.LOGGER.error("Error occurred in position " + i + ".\n Was expecting digit, instead got char \"" + c + "\".");
                            state = ParserState.LITERAL_STATE;
                            continue;
                        }
                    }
                    break;
                }
                case MAX_STATE: {
                    currentLiteral.append(c);
                    if (c >= '0' && c <= '9') {
                        formattingInfo = new FormattingInfo(formattingInfo.isLeftAligned(), formattingInfo.getMinLength(), formattingInfo.getMaxLength() * 10 + c - 48, formattingInfo.isLeftTruncate());
                        continue;
                    }
                    i = this.finalizeConverter(c, pattern, i, currentLiteral, formattingInfo, this.converterRules, patternConverters, formattingInfos, noConsoleNoAnsi, convertBackslashes);
                    state = ParserState.LITERAL_STATE;
                    formattingInfo = FormattingInfo.getDefault();
                    currentLiteral.setLength(0);
                    continue;
                }
            }
        }
        if (currentLiteral.length() != 0) {
            patternConverters.add(new LiteralPatternConverter(this.config, currentLiteral.toString(), convertBackslashes));
            formattingInfos.add(FormattingInfo.getDefault());
        }
    }
    
    private PatternConverter createConverter(final String converterId, final StringBuilder currentLiteral, final Map<String, Class<PatternConverter>> rules, final List<String> options, final boolean noConsoleNoAnsi) {
        String converterName = converterId;
        Class<PatternConverter> converterClass = null;
        if (rules == null) {
            PatternParser.LOGGER.error("Null rules for [" + converterId + ']');
            return null;
        }
        for (int i = converterId.length(); i > 0 && converterClass == null; converterClass = rules.get(converterName), --i) {
            converterName = converterName.substring(0, i);
        }
        if (converterClass == null) {
            PatternParser.LOGGER.error("Unrecognized format specifier [" + converterId + ']');
            return null;
        }
        if (AnsiConverter.class.isAssignableFrom(converterClass)) {
            options.add("noConsoleNoAnsi=" + noConsoleNoAnsi);
        }
        final Method[] methods = converterClass.getDeclaredMethods();
        Method newInstanceMethod = null;
        for (final Method method : methods) {
            if (Modifier.isStatic(method.getModifiers()) && method.getDeclaringClass().equals(converterClass) && method.getName().equals("newInstance")) {
                if (newInstanceMethod == null) {
                    newInstanceMethod = method;
                }
                else if (method.getReturnType().equals(newInstanceMethod.getReturnType())) {
                    PatternParser.LOGGER.error("Class " + converterClass + " cannot contain multiple static newInstance methods");
                    return null;
                }
            }
        }
        if (newInstanceMethod == null) {
            PatternParser.LOGGER.error("Class " + converterClass + " does not contain a static newInstance method");
            return null;
        }
        final Class<?>[] parmTypes = newInstanceMethod.getParameterTypes();
        final Object[] parms = (Object[])((parmTypes.length > 0) ? new Object[parmTypes.length] : null);
        if (parms != null) {
            int j = 0;
            boolean errors = false;
            for (final Class<?> clazz : parmTypes) {
                if (clazz.isArray() && clazz.getName().equals("[Ljava.lang.String;")) {
                    final String[] optionsArray = options.toArray(new String[options.size()]);
                    parms[j] = optionsArray;
                }
                else if (clazz.isAssignableFrom(Configuration.class)) {
                    parms[j] = this.config;
                }
                else {
                    PatternParser.LOGGER.error("Unknown parameter type " + clazz.getName() + " for static newInstance method of " + converterClass.getName());
                    errors = true;
                }
                ++j;
            }
            if (errors) {
                return null;
            }
        }
        try {
            final Object newObj = newInstanceMethod.invoke(null, parms);
            if (newObj instanceof PatternConverter) {
                currentLiteral.delete(0, currentLiteral.length() - (converterId.length() - converterName.length()));
                return (PatternConverter)newObj;
            }
            PatternParser.LOGGER.warn("Class {} does not extend PatternConverter.", new Object[] { converterClass.getName() });
        }
        catch (Exception ex) {
            PatternParser.LOGGER.error("Error creating converter for " + converterId, ex);
        }
        return null;
    }
    
    private int finalizeConverter(final char c, final String pattern, final int start, final StringBuilder currentLiteral, final FormattingInfo formattingInfo, final Map<String, Class<PatternConverter>> rules, final List<PatternConverter> patternConverters, final List<FormattingInfo> formattingInfos, final boolean noConsoleNoAnsi, final boolean convertBackslashes) {
        int i = start;
        final StringBuilder convBuf = new StringBuilder();
        i = extractConverter(c, pattern, i, convBuf, currentLiteral);
        final String converterId = convBuf.toString();
        final List<String> options = new ArrayList<String>();
        i = extractOptions(pattern, i, options);
        final PatternConverter pc = this.createConverter(converterId, currentLiteral, rules, options, noConsoleNoAnsi);
        if (pc == null) {
            StringBuilder msg;
            if (Strings.isEmpty(converterId)) {
                msg = new StringBuilder("Empty conversion specifier starting at position ");
            }
            else {
                msg = new StringBuilder("Unrecognized conversion specifier [");
                msg.append(converterId);
                msg.append("] starting at position ");
            }
            msg.append(Integer.toString(i));
            msg.append(" in conversion pattern.");
            PatternParser.LOGGER.error(msg.toString());
            patternConverters.add(new LiteralPatternConverter(this.config, currentLiteral.toString(), convertBackslashes));
            formattingInfos.add(FormattingInfo.getDefault());
        }
        else {
            patternConverters.add(pc);
            formattingInfos.add(formattingInfo);
            if (currentLiteral.length() > 0) {
                patternConverters.add(new LiteralPatternConverter(this.config, currentLiteral.toString(), convertBackslashes));
                formattingInfos.add(FormattingInfo.getDefault());
            }
        }
        currentLiteral.setLength(0);
        return i;
    }
    
    static {
        LOGGER = StatusLogger.getLogger();
    }
    
    private enum ParserState
    {
        LITERAL_STATE, 
        CONVERTER_STATE, 
        DOT_STATE, 
        MIN_STATE, 
        MAX_STATE;
    }
}
