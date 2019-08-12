// 
// Decompiled by Procyon v0.5.36
// 

package org.apache.logging.log4j.message;

import java.util.Iterator;
import java.util.Collection;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

public class ParameterizedMessage implements Message
{
    public static final String RECURSION_PREFIX = "[...";
    public static final String RECURSION_SUFFIX = "...]";
    public static final String ERROR_PREFIX = "[!!!";
    public static final String ERROR_SEPARATOR = "=>";
    public static final String ERROR_MSG_SEPARATOR = ":";
    public static final String ERROR_SUFFIX = "!!!]";
    private static final long serialVersionUID = -665975803997290697L;
    private static final int HASHVAL = 31;
    private static final char DELIM_START = '{';
    private static final char DELIM_STOP = '}';
    private static final char ESCAPE_CHAR = '\\';
    private final String messagePattern;
    private final String[] stringArgs;
    private transient Object[] argArray;
    private transient String formattedMessage;
    private transient Throwable throwable;
    
    public ParameterizedMessage(final String messagePattern, final String[] stringArgs, final Throwable throwable) {
        this.messagePattern = messagePattern;
        this.stringArgs = stringArgs;
        this.throwable = throwable;
    }
    
    public ParameterizedMessage(final String messagePattern, final Object[] objectArgs, final Throwable throwable) {
        this.messagePattern = messagePattern;
        this.throwable = throwable;
        this.stringArgs = this.argumentsToStrings(objectArgs);
    }
    
    public ParameterizedMessage(final String messagePattern, final Object[] arguments) {
        this.messagePattern = messagePattern;
        this.stringArgs = this.argumentsToStrings(arguments);
    }
    
    public ParameterizedMessage(final String messagePattern, final Object arg) {
        this(messagePattern, new Object[] { arg });
    }
    
    public ParameterizedMessage(final String messagePattern, final Object arg1, final Object arg2) {
        this(messagePattern, new Object[] { arg1, arg2 });
    }
    
    private String[] argumentsToStrings(final Object[] arguments) {
        if (arguments == null) {
            return null;
        }
        final int argsCount = countArgumentPlaceholders(this.messagePattern);
        int resultArgCount = arguments.length;
        if (argsCount < arguments.length && this.throwable == null && arguments[arguments.length - 1] instanceof Throwable) {
            this.throwable = (Throwable)arguments[arguments.length - 1];
            --resultArgCount;
        }
        System.arraycopy(arguments, 0, this.argArray = new Object[resultArgCount], 0, resultArgCount);
        String[] strArgs;
        if (argsCount == 1 && this.throwable == null && arguments.length > 1) {
            strArgs = new String[] { deepToString(arguments) };
        }
        else {
            strArgs = new String[resultArgCount];
            for (int i = 0; i < strArgs.length; ++i) {
                strArgs[i] = deepToString(arguments[i]);
            }
        }
        return strArgs;
    }
    
    @Override
    public String getFormattedMessage() {
        if (this.formattedMessage == null) {
            this.formattedMessage = this.formatMessage(this.messagePattern, this.stringArgs);
        }
        return this.formattedMessage;
    }
    
    @Override
    public String getFormat() {
        return this.messagePattern;
    }
    
    @Override
    public Object[] getParameters() {
        if (this.argArray != null) {
            return this.argArray;
        }
        return this.stringArgs;
    }
    
    @Override
    public Throwable getThrowable() {
        return this.throwable;
    }
    
    protected String formatMessage(final String msgPattern, final String[] sArgs) {
        return formatStringArgs(msgPattern, sArgs);
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final ParameterizedMessage that = (ParameterizedMessage)o;
        if (this.messagePattern != null) {
            if (this.messagePattern.equals(that.messagePattern)) {
                return Arrays.equals(this.stringArgs, that.stringArgs);
            }
        }
        else if (that.messagePattern == null) {
            return Arrays.equals(this.stringArgs, that.stringArgs);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int result = (this.messagePattern != null) ? this.messagePattern.hashCode() : 0;
        result = 31 * result + ((this.stringArgs != null) ? Arrays.hashCode(this.stringArgs) : 0);
        return result;
    }
    
    public static String format(final String messagePattern, final Object[] arguments) {
        if (messagePattern == null || arguments == null || arguments.length == 0) {
            return messagePattern;
        }
        if (arguments instanceof String[]) {
            return formatStringArgs(messagePattern, (String[])arguments);
        }
        final String[] stringArgs = new String[arguments.length];
        for (int i = 0; i < arguments.length; ++i) {
            stringArgs[i] = String.valueOf(arguments[i]);
        }
        return formatStringArgs(messagePattern, stringArgs);
    }
    
    static String formatStringArgs(final String messagePattern, final String[] arguments) {
        int len = 0;
        if (messagePattern == null || (len = messagePattern.length()) == 0 || arguments == null || arguments.length == 0) {
            return messagePattern;
        }
        return formatStringArgs0(messagePattern, len, arguments);
    }
    
    private static String formatStringArgs0(final String messagePattern, final int len, final String[] arguments) {
        final char[] result = new char[len + sumStringLengths(arguments)];
        int pos = 0;
        int escapeCounter = 0;
        int currentArgument = 0;
        int i;
        for (i = 0; i < len - 1; ++i) {
            final char curChar = messagePattern.charAt(i);
            if (curChar == '\\') {
                ++escapeCounter;
            }
            else {
                if (isDelimPair(curChar, messagePattern, i)) {
                    ++i;
                    pos = writeEscapedEscapeChars(escapeCounter, result, pos);
                    if (isOdd(escapeCounter)) {
                        pos = writeDelimPair(result, pos);
                    }
                    else {
                        pos = writeArgOrDelimPair(arguments, currentArgument, result, pos);
                        ++currentArgument;
                    }
                }
                else {
                    pos = handleLiteralChar(result, pos, escapeCounter, curChar);
                }
                escapeCounter = 0;
            }
        }
        pos = handleRemainingCharIfAny(messagePattern, len, result, pos, escapeCounter, i);
        return new String(result, 0, pos);
    }
    
    private static int sumStringLengths(final String[] arguments) {
        int result = 0;
        for (int i = 0; i < arguments.length; ++i) {
            result += String.valueOf(arguments[i]).length();
        }
        return result;
    }
    
    private static boolean isDelimPair(final char curChar, final String messagePattern, final int curCharIndex) {
        return curChar == '{' && messagePattern.charAt(curCharIndex + 1) == '}';
    }
    
    private static int handleRemainingCharIfAny(final String messagePattern, final int len, final char[] result, int pos, final int escapeCounter, final int i) {
        if (i == len - 1) {
            final char curChar = messagePattern.charAt(i);
            pos = handleLastChar(result, pos, escapeCounter, curChar);
        }
        return pos;
    }
    
    private static int handleLastChar(final char[] result, int pos, final int escapeCounter, final char curChar) {
        if (curChar == '\\') {
            pos = writeUnescapedEscapeChars(escapeCounter + 1, result, pos);
        }
        else {
            pos = handleLiteralChar(result, pos, escapeCounter, curChar);
        }
        return pos;
    }
    
    private static int handleLiteralChar(final char[] result, int pos, final int escapeCounter, final char curChar) {
        pos = writeUnescapedEscapeChars(escapeCounter, result, pos);
        result[pos++] = curChar;
        return pos;
    }
    
    private static int writeDelimPair(final char[] result, int pos) {
        result[pos++] = '{';
        result[pos++] = '}';
        return pos;
    }
    
    private static boolean isOdd(final int number) {
        return (number & 0x1) == 0x1;
    }
    
    private static int writeEscapedEscapeChars(final int escapeCounter, final char[] result, final int pos) {
        final int escapedEscapes = escapeCounter >> 1;
        return writeUnescapedEscapeChars(escapedEscapes, result, pos);
    }
    
    private static int writeUnescapedEscapeChars(int escapeCounter, final char[] result, int pos) {
        while (escapeCounter > 0) {
            result[pos++] = '\\';
            --escapeCounter;
        }
        return pos;
    }
    
    private static int writeArgOrDelimPair(final String[] arguments, final int currentArgument, final char[] result, int pos) {
        if (currentArgument < arguments.length) {
            pos = writeArgAt0(arguments, currentArgument, result, pos);
        }
        else {
            pos = writeDelimPair(result, pos);
        }
        return pos;
    }
    
    private static int writeArgAt0(final String[] arguments, final int currentArgument, final char[] result, final int pos) {
        final String arg = String.valueOf(arguments[currentArgument]);
        final int argLen = arg.length();
        arg.getChars(0, argLen, result, pos);
        return pos + argLen;
    }
    
    public static int countArgumentPlaceholders(final String messagePattern) {
        if (messagePattern == null) {
            return 0;
        }
        final int delim = messagePattern.indexOf(123);
        if (delim == -1) {
            return 0;
        }
        int result = 0;
        boolean isEscaped = false;
        for (int i = 0; i < messagePattern.length(); ++i) {
            final char curChar = messagePattern.charAt(i);
            if (curChar == '\\') {
                isEscaped = !isEscaped;
            }
            else if (curChar == '{') {
                if (!isEscaped && i < messagePattern.length() - 1 && messagePattern.charAt(i + 1) == '}') {
                    ++result;
                    ++i;
                }
                isEscaped = false;
            }
            else {
                isEscaped = false;
            }
        }
        return result;
    }
    
    public static String deepToString(final Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof String) {
            return (String)o;
        }
        final StringBuilder str = new StringBuilder();
        final Set<String> dejaVu = new HashSet<String>();
        recursiveDeepToString(o, str, dejaVu);
        return str.toString();
    }
    
    private static void recursiveDeepToString(final Object o, final StringBuilder str, final Set<String> dejaVu) {
        if (appendStringDateOrNull(o, str)) {
            return;
        }
        if (isMaybeRecursive(o)) {
            appendPotentiallyRecursiveValue(o, str, dejaVu);
        }
        else {
            tryObjectToString(o, str);
        }
    }
    
    private static boolean appendStringDateOrNull(final Object o, final StringBuilder str) {
        if (o == null || o instanceof String) {
            str.append(String.valueOf(o));
            return true;
        }
        return appendDate(o, str);
    }
    
    private static boolean appendDate(final Object o, final StringBuilder str) {
        if (!(o instanceof Date)) {
            return false;
        }
        final Date date = (Date)o;
        final SimpleDateFormat format = getSimpleDateFormat();
        str.append(format.format(date));
        return true;
    }
    
    private static SimpleDateFormat getSimpleDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    }
    
    private static boolean isMaybeRecursive(final Object o) {
        return o.getClass().isArray() || o instanceof Map || o instanceof Collection;
    }
    
    private static void appendPotentiallyRecursiveValue(final Object o, final StringBuilder str, final Set<String> dejaVu) {
        final Class<?> oClass = o.getClass();
        if (oClass.isArray()) {
            appendArray(o, str, dejaVu, oClass);
        }
        else if (o instanceof Map) {
            appendMap(o, str, dejaVu);
        }
        else if (o instanceof Collection) {
            appendCollection(o, str, dejaVu);
        }
    }
    
    private static void appendArray(final Object o, final StringBuilder str, final Set<String> dejaVu, final Class<?> oClass) {
        if (oClass == byte[].class) {
            str.append(Arrays.toString((byte[])o));
        }
        else if (oClass == short[].class) {
            str.append(Arrays.toString((short[])o));
        }
        else if (oClass == int[].class) {
            str.append(Arrays.toString((int[])o));
        }
        else if (oClass == long[].class) {
            str.append(Arrays.toString((long[])o));
        }
        else if (oClass == float[].class) {
            str.append(Arrays.toString((float[])o));
        }
        else if (oClass == double[].class) {
            str.append(Arrays.toString((double[])o));
        }
        else if (oClass == boolean[].class) {
            str.append(Arrays.toString((boolean[])o));
        }
        else if (oClass == char[].class) {
            str.append(Arrays.toString((char[])o));
        }
        else {
            final String id = identityToString(o);
            if (dejaVu.contains(id)) {
                str.append("[...").append(id).append("...]");
            }
            else {
                dejaVu.add(id);
                final Object[] oArray = (Object[])o;
                str.append('[');
                boolean first = true;
                for (final Object current : oArray) {
                    if (first) {
                        first = false;
                    }
                    else {
                        str.append(", ");
                    }
                    recursiveDeepToString(current, str, new HashSet<String>(dejaVu));
                }
                str.append(']');
            }
        }
    }
    
    private static void appendMap(final Object o, final StringBuilder str, final Set<String> dejaVu) {
        final String id = identityToString(o);
        if (dejaVu.contains(id)) {
            str.append("[...").append(id).append("...]");
        }
        else {
            dejaVu.add(id);
            final Map<?, ?> oMap = (Map<?, ?>)o;
            str.append('{');
            boolean isFirst = true;
            for (final Object o2 : oMap.entrySet()) {
                final Map.Entry<?, ?> current = (Map.Entry<?, ?>)o2;
                if (isFirst) {
                    isFirst = false;
                }
                else {
                    str.append(", ");
                }
                final Object key = current.getKey();
                final Object value = current.getValue();
                recursiveDeepToString(key, str, new HashSet<String>(dejaVu));
                str.append('=');
                recursiveDeepToString(value, str, new HashSet<String>(dejaVu));
            }
            str.append('}');
        }
    }
    
    private static void appendCollection(final Object o, final StringBuilder str, final Set<String> dejaVu) {
        final String id = identityToString(o);
        if (dejaVu.contains(id)) {
            str.append("[...").append(id).append("...]");
        }
        else {
            dejaVu.add(id);
            final Collection<?> oCol = (Collection<?>)o;
            str.append('[');
            boolean isFirst = true;
            for (final Object anOCol : oCol) {
                if (isFirst) {
                    isFirst = false;
                }
                else {
                    str.append(", ");
                }
                recursiveDeepToString(anOCol, str, new HashSet<String>(dejaVu));
            }
            str.append(']');
        }
    }
    
    private static void tryObjectToString(final Object o, final StringBuilder str) {
        try {
            str.append(o.toString());
        }
        catch (Throwable t) {
            handleErrorInObjectToString(o, str, t);
        }
    }
    
    private static void handleErrorInObjectToString(final Object o, final StringBuilder str, final Throwable t) {
        str.append("[!!!");
        str.append(identityToString(o));
        str.append("=>");
        final String msg = t.getMessage();
        final String className = t.getClass().getName();
        str.append(className);
        if (!className.equals(msg)) {
            str.append(":");
            str.append(msg);
        }
        str.append("!!!]");
    }
    
    public static String identityToString(final Object obj) {
        if (obj == null) {
            return null;
        }
        return obj.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(obj));
    }
    
    @Override
    public String toString() {
        return "ParameterizedMessage[messagePattern=" + this.messagePattern + ", stringArgs=" + Arrays.toString(this.stringArgs) + ", throwable=" + this.throwable + ']';
    }
}
