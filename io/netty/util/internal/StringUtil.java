// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.util.internal;

import java.util.Formatter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public final class StringUtil
{
    public static final String NEWLINE;
    public static final char DOUBLE_QUOTE = '\"';
    public static final char COMMA = ',';
    public static final char LINE_FEED = '\n';
    public static final char CARRIAGE_RETURN = '\r';
    private static final String[] BYTE2HEX_PAD;
    private static final String[] BYTE2HEX_NOPAD;
    private static final String EMPTY_STRING = "";
    private static final int CSV_NUMBER_ESCAPE_CHARACTERS = 7;
    
    public static String[] split(final String value, final char delim) {
        final int end = value.length();
        final List<String> res = new ArrayList<String>();
        int start = 0;
        for (int i = 0; i < end; ++i) {
            if (value.charAt(i) == delim) {
                if (start == i) {
                    res.add("");
                }
                else {
                    res.add(value.substring(start, i));
                }
                start = i + 1;
            }
        }
        if (start == 0) {
            res.add(value);
        }
        else if (start != end) {
            res.add(value.substring(start, end));
        }
        else {
            for (int i = res.size() - 1; i >= 0 && res.get(i).isEmpty(); --i) {
                res.remove(i);
            }
        }
        return res.toArray(new String[res.size()]);
    }
    
    public static String[] split(final String value, final char delim, final int maxParts) {
        final int end = value.length();
        final List<String> res = new ArrayList<String>();
        int start = 0;
        for (int cpt = 1, i = 0; i < end && cpt < maxParts; ++i) {
            if (value.charAt(i) == delim) {
                if (start == i) {
                    res.add("");
                }
                else {
                    res.add(value.substring(start, i));
                }
                start = i + 1;
                ++cpt;
            }
        }
        if (start == 0) {
            res.add(value);
        }
        else if (start != end) {
            res.add(value.substring(start, end));
        }
        else {
            for (int i = res.size() - 1; i >= 0 && res.get(i).isEmpty(); --i) {
                res.remove(i);
            }
        }
        return res.toArray(new String[res.size()]);
    }
    
    public static String substringAfter(final String value, final char delim) {
        final int pos = value.indexOf(delim);
        if (pos >= 0) {
            return value.substring(pos + 1);
        }
        return null;
    }
    
    public static String byteToHexStringPadded(final int value) {
        return StringUtil.BYTE2HEX_PAD[value & 0xFF];
    }
    
    public static <T extends Appendable> T byteToHexStringPadded(final T buf, final int value) {
        try {
            buf.append(byteToHexStringPadded(value));
        }
        catch (IOException e) {
            PlatformDependent.throwException(e);
        }
        return buf;
    }
    
    public static String toHexStringPadded(final byte[] src) {
        return toHexStringPadded(src, 0, src.length);
    }
    
    public static String toHexStringPadded(final byte[] src, final int offset, final int length) {
        return toHexStringPadded(new StringBuilder(length << 1), src, offset, length).toString();
    }
    
    public static <T extends Appendable> T toHexStringPadded(final T dst, final byte[] src) {
        return toHexStringPadded(dst, src, 0, src.length);
    }
    
    public static <T extends Appendable> T toHexStringPadded(final T dst, final byte[] src, final int offset, final int length) {
        for (int end = offset + length, i = offset; i < end; ++i) {
            byteToHexStringPadded(dst, src[i]);
        }
        return dst;
    }
    
    public static String byteToHexString(final int value) {
        return StringUtil.BYTE2HEX_NOPAD[value & 0xFF];
    }
    
    public static <T extends Appendable> T byteToHexString(final T buf, final int value) {
        try {
            buf.append(byteToHexString(value));
        }
        catch (IOException e) {
            PlatformDependent.throwException(e);
        }
        return buf;
    }
    
    public static String toHexString(final byte[] src) {
        return toHexString(src, 0, src.length);
    }
    
    public static String toHexString(final byte[] src, final int offset, final int length) {
        return toHexString(new StringBuilder(length << 1), src, offset, length).toString();
    }
    
    public static <T extends Appendable> T toHexString(final T dst, final byte[] src) {
        return toHexString(dst, src, 0, src.length);
    }
    
    public static <T extends Appendable> T toHexString(final T dst, final byte[] src, final int offset, final int length) {
        assert length >= 0;
        if (length == 0) {
            return dst;
        }
        final int end = offset + length;
        int endMinusOne;
        int i;
        for (endMinusOne = end - 1, i = offset; i < endMinusOne && src[i] == 0; ++i) {}
        byteToHexString(dst, src[i++]);
        final int remaining = end - i;
        toHexStringPadded((Appendable)dst, src, i, remaining);
        return dst;
    }
    
    public static String simpleClassName(final Object o) {
        if (o == null) {
            return "null_object";
        }
        return simpleClassName(o.getClass());
    }
    
    public static String simpleClassName(final Class<?> clazz) {
        if (clazz == null) {
            return "null_class";
        }
        final Package pkg = clazz.getPackage();
        if (pkg != null) {
            return clazz.getName().substring(pkg.getName().length() + 1);
        }
        return clazz.getName();
    }
    
    public static CharSequence escapeCsv(final CharSequence value) {
        final int length = ObjectUtil.checkNotNull(value, "value").length();
        if (length == 0) {
            return value;
        }
        final int last = length - 1;
        final boolean quoted = isDoubleQuote(value.charAt(0)) && isDoubleQuote(value.charAt(last)) && length != 1;
        boolean foundSpecialCharacter = false;
        boolean escapedDoubleQuote = false;
        final StringBuilder escaped = new StringBuilder(length + 7).append('\"');
        for (int i = 0; i < length; ++i) {
            final char current = value.charAt(i);
            Label_0237: {
                switch (current) {
                    case '\"': {
                        if (i == 0 || i == last) {
                            if (!quoted) {
                                escaped.append('\"');
                                break Label_0237;
                            }
                            continue;
                        }
                        else {
                            final boolean isNextCharDoubleQuote = isDoubleQuote(value.charAt(i + 1));
                            if (!isDoubleQuote(value.charAt(i - 1)) && (!isNextCharDoubleQuote || (isNextCharDoubleQuote && i + 1 == last))) {
                                escaped.append('\"');
                                escapedDoubleQuote = true;
                                break;
                            }
                            break;
                        }
                        break;
                    }
                    case '\n':
                    case '\r':
                    case ',': {
                        foundSpecialCharacter = true;
                        break;
                    }
                }
            }
            escaped.append(current);
        }
        return (escapedDoubleQuote || (foundSpecialCharacter && !quoted)) ? escaped.append('\"') : value;
    }
    
    private static boolean isDoubleQuote(final char c) {
        return c == '\"';
    }
    
    private StringUtil() {
    }
    
    static {
        BYTE2HEX_PAD = new String[256];
        BYTE2HEX_NOPAD = new String[256];
        String newLine;
        try {
            newLine = new Formatter().format("%n", new Object[0]).toString();
        }
        catch (Exception e) {
            newLine = "\n";
        }
        NEWLINE = newLine;
        int i;
        for (i = 0; i < 10; ++i) {
            final StringBuilder buf = new StringBuilder(2);
            buf.append('0');
            buf.append(i);
            StringUtil.BYTE2HEX_PAD[i] = buf.toString();
            StringUtil.BYTE2HEX_NOPAD[i] = String.valueOf(i);
        }
        while (i < 16) {
            final StringBuilder buf = new StringBuilder(2);
            final char c = (char)(97 + i - 10);
            buf.append('0');
            buf.append(c);
            StringUtil.BYTE2HEX_PAD[i] = buf.toString();
            StringUtil.BYTE2HEX_NOPAD[i] = String.valueOf(c);
            ++i;
        }
        while (i < StringUtil.BYTE2HEX_PAD.length) {
            final StringBuilder buf = new StringBuilder(2);
            buf.append(Integer.toHexString(i));
            final String str = buf.toString();
            StringUtil.BYTE2HEX_PAD[i] = str;
            StringUtil.BYTE2HEX_NOPAD[i] = str;
            ++i;
        }
    }
}
