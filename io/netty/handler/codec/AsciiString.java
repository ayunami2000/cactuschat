// 
// Decompiled by Procyon v0.5.36
// 

package io.netty.handler.codec;

import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import io.netty.util.internal.EmptyArrays;
import java.nio.ByteBuffer;
import java.util.Arrays;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.nio.charset.Charset;
import java.util.Comparator;

public final class AsciiString implements CharSequence, Comparable<CharSequence>
{
    public static final AsciiString EMPTY_STRING;
    public static final Comparator<AsciiString> CASE_INSENSITIVE_ORDER;
    public static final Comparator<AsciiString> CASE_SENSITIVE_ORDER;
    public static final Comparator<CharSequence> CHARSEQUENCE_CASE_INSENSITIVE_ORDER;
    public static final Comparator<CharSequence> CHARSEQUENCE_CASE_SENSITIVE_ORDER;
    private final byte[] value;
    private String string;
    private int hash;
    
    public static int caseInsensitiveHashCode(final CharSequence value) {
        if (value instanceof AsciiString) {
            return value.hashCode();
        }
        int hash = 0;
        for (int end = value.length(), i = 0; i < end; ++i) {
            hash = (hash * 31 ^ (value.charAt(i) & '\u001f'));
        }
        return hash;
    }
    
    public static boolean equalsIgnoreCase(final CharSequence a, final CharSequence b) {
        if (a == b) {
            return true;
        }
        if (a instanceof AsciiString) {
            final AsciiString aa = (AsciiString)a;
            return aa.equalsIgnoreCase(b);
        }
        if (b instanceof AsciiString) {
            final AsciiString ab = (AsciiString)b;
            return ab.equalsIgnoreCase(a);
        }
        return a != null && b != null && a.toString().equalsIgnoreCase(b.toString());
    }
    
    public static boolean equals(final CharSequence a, final CharSequence b) {
        if (a == b) {
            return true;
        }
        if (a instanceof AsciiString) {
            final AsciiString aa = (AsciiString)a;
            return aa.equals(b);
        }
        if (b instanceof AsciiString) {
            final AsciiString ab = (AsciiString)b;
            return ab.equals(a);
        }
        return a != null && b != null && a.equals(b);
    }
    
    public static byte[] getBytes(final CharSequence v, final Charset charset) {
        if (v instanceof AsciiString) {
            return ((AsciiString)v).array();
        }
        if (v instanceof String) {
            return ((String)v).getBytes(charset);
        }
        if (v != null) {
            final ByteBuf buf = Unpooled.copiedBuffer(v, charset);
            try {
                if (buf.hasArray()) {
                    return buf.array();
                }
                final byte[] result = new byte[buf.readableBytes()];
                buf.readBytes(result);
                return result;
            }
            finally {
                buf.release();
            }
        }
        return null;
    }
    
    public static AsciiString of(final CharSequence string) {
        return (AsciiString)((string instanceof AsciiString) ? string : new AsciiString(string));
    }
    
    public AsciiString(final byte[] value) {
        this(value, true);
    }
    
    public AsciiString(final byte[] value, final boolean copy) {
        checkNull(value);
        if (copy) {
            this.value = value.clone();
        }
        else {
            this.value = value;
        }
    }
    
    public AsciiString(final byte[] value, final int start, final int length) {
        this(value, start, length, true);
    }
    
    public AsciiString(final byte[] value, final int start, final int length, final boolean copy) {
        checkNull(value);
        if (start < 0 || start > value.length - length) {
            throw new IndexOutOfBoundsException("expected: 0 <= start(" + start + ") <= start + length(" + length + ") <= " + "value.length(" + value.length + ')');
        }
        if (copy || start != 0 || length != value.length) {
            this.value = Arrays.copyOfRange(value, start, start + length);
        }
        else {
            this.value = value;
        }
    }
    
    public AsciiString(final char[] value) {
        this(checkNull(value), 0, value.length);
    }
    
    public AsciiString(final char[] value, final int start, final int length) {
        checkNull(value);
        if (start < 0 || start > value.length - length) {
            throw new IndexOutOfBoundsException("expected: 0 <= start(" + start + ") <= start + length(" + length + ") <= " + "value.length(" + value.length + ')');
        }
        this.value = new byte[length];
        for (int i = 0, j = start; i < length; ++i, ++j) {
            this.value[i] = c2b(value[j]);
        }
    }
    
    public AsciiString(final CharSequence value) {
        this(checkNull(value), 0, value.length());
    }
    
    public AsciiString(final CharSequence value, final int start, final int length) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        if (start < 0 || length < 0 || length > value.length() - start) {
            throw new IndexOutOfBoundsException("expected: 0 <= start(" + start + ") <= start + length(" + length + ") <= " + "value.length(" + value.length() + ')');
        }
        this.value = new byte[length];
        for (int i = 0; i < length; ++i) {
            this.value[i] = c2b(value.charAt(start + i));
        }
    }
    
    public AsciiString(final ByteBuffer value) {
        this(checkNull(value), value.position(), value.remaining());
    }
    
    public AsciiString(final ByteBuffer value, final int start, final int length) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        if (start < 0 || length > value.capacity() - start) {
            throw new IndexOutOfBoundsException("expected: 0 <= start(" + start + ") <= start + length(" + length + ") <= " + "value.capacity(" + value.capacity() + ')');
        }
        if (value.hasArray()) {
            final int baseOffset = value.arrayOffset() + start;
            this.value = Arrays.copyOfRange(value.array(), baseOffset, baseOffset + length);
        }
        else {
            this.value = new byte[length];
            final int oldPos = value.position();
            value.get(this.value, 0, this.value.length);
            value.position(oldPos);
        }
    }
    
    private static <T> T checkNull(final T value) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        return value;
    }
    
    @Override
    public int length() {
        return this.value.length;
    }
    
    @Override
    public char charAt(final int index) {
        return (char)(this.byteAt(index) & 0xFF);
    }
    
    public byte byteAt(final int index) {
        return this.value[index];
    }
    
    public byte[] array() {
        return this.value;
    }
    
    public int arrayOffset() {
        return 0;
    }
    
    private static byte c2b(final char c) {
        if (c > '\u00ff') {
            return 63;
        }
        return (byte)c;
    }
    
    private static byte toLowerCase(final byte b) {
        if (65 <= b && b <= 90) {
            return (byte)(b + 32);
        }
        return b;
    }
    
    private static char toLowerCase(final char c) {
        if ('A' <= c && c <= 'Z') {
            return (char)(c + ' ');
        }
        return c;
    }
    
    private static byte toUpperCase(final byte b) {
        if (97 <= b && b <= 122) {
            return (byte)(b - 32);
        }
        return b;
    }
    
    public AsciiString subSequence(final int start) {
        return this.subSequence(start, this.length());
    }
    
    @Override
    public AsciiString subSequence(final int start, final int end) {
        if (start < 0 || start > end || end > this.length()) {
            throw new IndexOutOfBoundsException("expected: 0 <= start(" + start + ") <= end (" + end + ") <= length(" + this.length() + ')');
        }
        final byte[] value = this.value;
        if (start == 0 && end == value.length) {
            return this;
        }
        if (end == start) {
            return AsciiString.EMPTY_STRING;
        }
        return new AsciiString(value, start, end - start, false);
    }
    
    @Override
    public int hashCode() {
        int hash = this.hash;
        final byte[] value = this.value;
        if (hash != 0 || value.length == 0) {
            return hash;
        }
        for (int i = 0; i < value.length; ++i) {
            hash = (hash * 31 ^ (value[i] & 0x1F));
        }
        return this.hash = hash;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof AsciiString)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        final AsciiString that = (AsciiString)obj;
        final int thisHash = this.hashCode();
        final int thatHash = that.hashCode();
        if (thisHash != thatHash || this.length() != that.length()) {
            return false;
        }
        final byte[] thisValue = this.value;
        final byte[] thatValue = that.value;
        for (int end = thisValue.length, i = 0, j = 0; i < end; ++i, ++j) {
            if (thisValue[i] != thatValue[j]) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public String toString() {
        final String string = this.string;
        if (string != null) {
            return string;
        }
        final byte[] value = this.value;
        return this.string = new String(value, 0, 0, value.length);
    }
    
    public String toString(final int start, final int end) {
        final byte[] value = this.value;
        if (start == 0 && end == value.length) {
            return this.toString();
        }
        final int length = end - start;
        if (length == 0) {
            return "";
        }
        return new String(value, 0, start, length);
    }
    
    @Override
    public int compareTo(final CharSequence string) {
        if (this == string) {
            return 0;
        }
        final int length1 = this.length();
        final int length2 = string.length();
        final int minLength = Math.min(length1, length2);
        final byte[] value = this.value;
        int i = 0;
        for (int j = 0; j < minLength; ++j) {
            final int result = (value[i] & 0xFF) - string.charAt(j);
            if (result != 0) {
                return result;
            }
            ++i;
        }
        return length1 - length2;
    }
    
    public int compareToIgnoreCase(final CharSequence string) {
        return AsciiString.CHARSEQUENCE_CASE_INSENSITIVE_ORDER.compare(this, string);
    }
    
    public AsciiString concat(final CharSequence string) {
        final int thisLen = this.length();
        final int thatLen = string.length();
        if (thatLen == 0) {
            return this;
        }
        if (string instanceof AsciiString) {
            final AsciiString that = (AsciiString)string;
            if (this.isEmpty()) {
                return that;
            }
            final byte[] newValue = Arrays.copyOf(this.value, thisLen + thatLen);
            System.arraycopy(that.value, 0, newValue, thisLen, thatLen);
            return new AsciiString(newValue, false);
        }
        else {
            if (this.isEmpty()) {
                return new AsciiString(string);
            }
            final int newLen = thisLen + thatLen;
            final byte[] newValue = Arrays.copyOf(this.value, newLen);
            for (int i = thisLen, j = 0; i < newLen; ++i, ++j) {
                newValue[i] = c2b(string.charAt(j));
            }
            return new AsciiString(newValue, false);
        }
    }
    
    public boolean endsWith(final CharSequence suffix) {
        final int suffixLen = suffix.length();
        return this.regionMatches(this.length() - suffixLen, suffix, 0, suffixLen);
    }
    
    public boolean equalsIgnoreCase(final CharSequence string) {
        if (string == this) {
            return true;
        }
        if (string == null) {
            return false;
        }
        final byte[] value = this.value;
        final int thisLen = value.length;
        final int thatLen = string.length();
        if (thisLen != thatLen) {
            return false;
        }
        for (int i = 0; i < thisLen; ++i) {
            final char c1 = (char)(value[i] & 0xFF);
            final char c2 = string.charAt(i);
            if (c1 != c2 && toLowerCase(c1) != toLowerCase(c2)) {
                return false;
            }
        }
        return true;
    }
    
    public byte[] toByteArray() {
        return this.toByteArray(0, this.length());
    }
    
    public byte[] toByteArray(final int start, final int end) {
        return Arrays.copyOfRange(this.value, start, end);
    }
    
    public char[] toCharArray() {
        return this.toCharArray(0, this.length());
    }
    
    public char[] toCharArray(final int start, final int end) {
        final int length = end - start;
        if (length == 0) {
            return EmptyArrays.EMPTY_CHARS;
        }
        final byte[] value = this.value;
        final char[] buffer = new char[length];
        for (int i = 0, j = start; i < length; ++i, ++j) {
            buffer[i] = (char)(value[j] & 0xFF);
        }
        return buffer;
    }
    
    public void copy(final int srcIdx, final ByteBuf dst, final int dstIdx, final int length) {
        if (dst == null) {
            throw new NullPointerException("dst");
        }
        final byte[] value = this.value;
        final int thisLen = value.length;
        if (srcIdx < 0 || length > thisLen - srcIdx) {
            throw new IndexOutOfBoundsException("expected: 0 <= srcIdx(" + srcIdx + ") <= srcIdx + length(" + length + ") <= srcLen(" + thisLen + ')');
        }
        dst.setBytes(dstIdx, value, srcIdx, length);
    }
    
    public void copy(final int srcIdx, final ByteBuf dst, final int length) {
        if (dst == null) {
            throw new NullPointerException("dst");
        }
        final byte[] value = this.value;
        final int thisLen = value.length;
        if (srcIdx < 0 || length > thisLen - srcIdx) {
            throw new IndexOutOfBoundsException("expected: 0 <= srcIdx(" + srcIdx + ") <= srcIdx + length(" + length + ") <= srcLen(" + thisLen + ')');
        }
        dst.writeBytes(value, srcIdx, length);
    }
    
    public void copy(final int srcIdx, final byte[] dst, final int dstIdx, final int length) {
        if (dst == null) {
            throw new NullPointerException("dst");
        }
        final byte[] value = this.value;
        final int thisLen = value.length;
        if (srcIdx < 0 || length > thisLen - srcIdx) {
            throw new IndexOutOfBoundsException("expected: 0 <= srcIdx(" + srcIdx + ") <= srcIdx + length(" + length + ") <= srcLen(" + thisLen + ')');
        }
        System.arraycopy(value, srcIdx, dst, dstIdx, length);
    }
    
    public void copy(final int srcIdx, final char[] dst, final int dstIdx, final int length) {
        if (dst == null) {
            throw new NullPointerException("dst");
        }
        final byte[] value = this.value;
        final int thisLen = value.length;
        if (srcIdx < 0 || length > thisLen - srcIdx) {
            throw new IndexOutOfBoundsException("expected: 0 <= srcIdx(" + srcIdx + ") <= srcIdx + length(" + length + ") <= srcLen(" + thisLen + ')');
        }
        final int dstEnd = dstIdx + length;
        int i = srcIdx;
        for (int j = dstIdx; j < dstEnd; ++j) {
            dst[j] = (char)(value[i] & 0xFF);
            ++i;
        }
    }
    
    public int indexOf(final int c) {
        return this.indexOf(c, 0);
    }
    
    public int indexOf(final int c, int start) {
        final byte[] value = this.value;
        final int length = value.length;
        if (start < length) {
            if (start < 0) {
                start = 0;
            }
            for (int i = start; i < length; ++i) {
                if ((value[i] & 0xFF) == c) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    public int indexOf(final CharSequence string) {
        return this.indexOf(string, 0);
    }
    
    public int indexOf(final CharSequence subString, int start) {
        if (start < 0) {
            start = 0;
        }
        final byte[] value = this.value;
        final int thisLen = value.length;
        final int subCount = subString.length();
        if (subCount <= 0) {
            return (start < thisLen) ? start : thisLen;
        }
        if (subCount > thisLen - start) {
            return -1;
        }
        final char firstChar = subString.charAt(0);
        while (true) {
            final int i = this.indexOf(firstChar, start);
            if (i == -1 || subCount + i > thisLen) {
                return -1;
            }
            int o1 = i;
            int o2 = 0;
            while (++o2 < subCount && (value[++o1] & 0xFF) == subString.charAt(o2)) {}
            if (o2 == subCount) {
                return i;
            }
            start = i + 1;
        }
    }
    
    public int lastIndexOf(final int c) {
        return this.lastIndexOf(c, this.length() - 1);
    }
    
    public int lastIndexOf(final int c, int start) {
        if (start >= 0) {
            final byte[] value = this.value;
            final int length = value.length;
            if (start >= length) {
                start = length - 1;
            }
            for (int i = start; i >= 0; --i) {
                if ((value[i] & 0xFF) == c) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    public int lastIndexOf(final CharSequence string) {
        return this.lastIndexOf(string, this.length());
    }
    
    public int lastIndexOf(final CharSequence subString, int start) {
        final byte[] value = this.value;
        final int thisLen = value.length;
        final int subCount = subString.length();
        if (subCount > thisLen || start < 0) {
            return -1;
        }
        if (subCount <= 0) {
            return (start < thisLen) ? start : thisLen;
        }
        start = Math.min(start, thisLen - subCount);
        final char firstChar = subString.charAt(0);
        while (true) {
            final int i = this.lastIndexOf(firstChar, start);
            if (i == -1) {
                return -1;
            }
            int o1 = i;
            int o2 = 0;
            while (++o2 < subCount && (value[++o1] & 0xFF) == subString.charAt(o2)) {}
            if (o2 == subCount) {
                return i;
            }
            start = i - 1;
        }
    }
    
    public boolean isEmpty() {
        return this.value.length == 0;
    }
    
    public boolean regionMatches(final int thisStart, final CharSequence string, final int start, final int length) {
        if (string == null) {
            throw new NullPointerException("string");
        }
        if (start < 0 || string.length() - start < length) {
            return false;
        }
        final byte[] value = this.value;
        final int thisLen = value.length;
        if (thisStart < 0 || thisLen - thisStart < length) {
            return false;
        }
        if (length <= 0) {
            return true;
        }
        for (int thisEnd = thisStart + length, i = thisStart, j = start; i < thisEnd; ++i, ++j) {
            if ((value[i] & 0xFF) != string.charAt(j)) {
                return false;
            }
        }
        return true;
    }
    
    public boolean regionMatches(final boolean ignoreCase, int thisStart, final CharSequence string, int start, final int length) {
        if (!ignoreCase) {
            return this.regionMatches(thisStart, string, start, length);
        }
        if (string == null) {
            throw new NullPointerException("string");
        }
        final byte[] value = this.value;
        final int thisLen = value.length;
        if (thisStart < 0 || length > thisLen - thisStart) {
            return false;
        }
        if (start < 0 || length > string.length() - start) {
            return false;
        }
        final int thisEnd = thisStart + length;
        while (thisStart < thisEnd) {
            final char c1 = (char)(value[thisStart++] & 0xFF);
            final char c2 = string.charAt(start++);
            if (c1 != c2 && toLowerCase(c1) != toLowerCase(c2)) {
                return false;
            }
        }
        return true;
    }
    
    public AsciiString replace(final char oldChar, final char newChar) {
        final int index = this.indexOf(oldChar, 0);
        if (index == -1) {
            return this;
        }
        final byte[] value = this.value;
        final int count = value.length;
        final byte[] buffer = new byte[count];
        for (int i = 0, j = 0; i < value.length; ++i, ++j) {
            byte b = value[i];
            if ((char)(b & 0xFF) == oldChar) {
                b = (byte)newChar;
            }
            buffer[j] = b;
        }
        return new AsciiString(buffer, false);
    }
    
    public boolean startsWith(final CharSequence prefix) {
        return this.startsWith(prefix, 0);
    }
    
    public boolean startsWith(final CharSequence prefix, final int start) {
        return this.regionMatches(start, prefix, 0, prefix.length());
    }
    
    public AsciiString toLowerCase() {
        boolean lowercased = true;
        final byte[] value = this.value;
        for (int i = 0; i < value.length; ++i) {
            final byte b = value[i];
            if (b >= 65 && b <= 90) {
                lowercased = false;
                break;
            }
        }
        if (lowercased) {
            return this;
        }
        final int length = value.length;
        final byte[] newValue = new byte[length];
        for (int i = 0, j = 0; i < length; ++i, ++j) {
            newValue[i] = toLowerCase(value[j]);
        }
        return new AsciiString(newValue, false);
    }
    
    public AsciiString toUpperCase() {
        final byte[] value = this.value;
        boolean uppercased = true;
        for (int i = 0; i < value.length; ++i) {
            final byte b = value[i];
            if (b >= 97 && b <= 122) {
                uppercased = false;
                break;
            }
        }
        if (uppercased) {
            return this;
        }
        final int length = value.length;
        final byte[] newValue = new byte[length];
        for (int i = 0, j = 0; i < length; ++i, ++j) {
            newValue[i] = toUpperCase(value[j]);
        }
        return new AsciiString(newValue, false);
    }
    
    public AsciiString trim() {
        byte[] value;
        int start;
        int end;
        int last;
        for (value = this.value, start = 0, last = (end = value.length); start <= end && value[start] <= 32; ++start) {}
        while (end >= start && value[end] <= 32) {
            --end;
        }
        if (start == 0 && end == last) {
            return this;
        }
        return new AsciiString(value, start, end - start + 1, false);
    }
    
    public boolean contentEquals(final CharSequence cs) {
        if (cs == null) {
            throw new NullPointerException();
        }
        final int length1 = this.length();
        final int length2 = cs.length();
        return length1 == length2 && ((length1 == 0 && length2 == 0) || this.regionMatches(0, cs, 0, length2));
    }
    
    public boolean matches(final String expr) {
        return Pattern.matches(expr, this);
    }
    
    public AsciiString[] split(final String expr, final int max) {
        return toAsciiStringArray(Pattern.compile(expr).split(this, max));
    }
    
    private static AsciiString[] toAsciiStringArray(final String[] jdkResult) {
        final AsciiString[] res = new AsciiString[jdkResult.length];
        for (int i = 0; i < jdkResult.length; ++i) {
            res[i] = new AsciiString(jdkResult[i]);
        }
        return res;
    }
    
    public AsciiString[] split(final char delim) {
        final List<AsciiString> res = new ArrayList<AsciiString>();
        int start = 0;
        final byte[] value = this.value;
        final int length = value.length;
        for (int i = start; i < length; ++i) {
            if (this.charAt(i) == delim) {
                if (start == i) {
                    res.add(AsciiString.EMPTY_STRING);
                }
                else {
                    res.add(new AsciiString(value, start, i - start, false));
                }
                start = i + 1;
            }
        }
        if (start == 0) {
            res.add(this);
        }
        else if (start != length) {
            res.add(new AsciiString(value, start, length - start, false));
        }
        else {
            for (int i = res.size() - 1; i >= 0 && res.get(i).isEmpty(); --i) {
                res.remove(i);
            }
        }
        return res.toArray(new AsciiString[res.size()]);
    }
    
    public boolean contains(final CharSequence cs) {
        if (cs == null) {
            throw new NullPointerException();
        }
        return this.indexOf(cs) >= 0;
    }
    
    public int parseInt() {
        return this.parseInt(0, this.length(), 10);
    }
    
    public int parseInt(final int radix) {
        return this.parseInt(0, this.length(), radix);
    }
    
    public int parseInt(final int start, final int end) {
        return this.parseInt(start, end, 10);
    }
    
    public int parseInt(final int start, final int end, final int radix) {
        if (radix < 2 || radix > 36) {
            throw new NumberFormatException();
        }
        if (start == end) {
            throw new NumberFormatException();
        }
        int i = start;
        final boolean negative = this.charAt(i) == '-';
        if (negative && ++i == end) {
            throw new NumberFormatException(this.subSequence(start, end).toString());
        }
        return this.parseInt(i, end, radix, negative);
    }
    
    private int parseInt(final int start, final int end, final int radix, final boolean negative) {
        final byte[] value = this.value;
        final int max = Integer.MIN_VALUE / radix;
        int result = 0;
        int offset = start;
        while (offset < end) {
            final int digit = Character.digit((char)(value[offset++] & 0xFF), radix);
            if (digit == -1) {
                throw new NumberFormatException(this.subSequence(start, end).toString());
            }
            if (max > result) {
                throw new NumberFormatException(this.subSequence(start, end).toString());
            }
            final int next = result * radix - digit;
            if (next > result) {
                throw new NumberFormatException(this.subSequence(start, end).toString());
            }
            result = next;
        }
        if (!negative) {
            result = -result;
            if (result < 0) {
                throw new NumberFormatException(this.subSequence(start, end).toString());
            }
        }
        return result;
    }
    
    public long parseLong() {
        return this.parseLong(0, this.length(), 10);
    }
    
    public long parseLong(final int radix) {
        return this.parseLong(0, this.length(), radix);
    }
    
    public long parseLong(final int start, final int end) {
        return this.parseLong(start, end, 10);
    }
    
    public long parseLong(final int start, final int end, final int radix) {
        if (radix < 2 || radix > 36) {
            throw new NumberFormatException();
        }
        if (start == end) {
            throw new NumberFormatException();
        }
        int i = start;
        final boolean negative = this.charAt(i) == '-';
        if (negative && ++i == end) {
            throw new NumberFormatException(this.subSequence(start, end).toString());
        }
        return this.parseLong(i, end, radix, negative);
    }
    
    private long parseLong(final int start, final int end, final int radix, final boolean negative) {
        final byte[] value = this.value;
        final long max = Long.MIN_VALUE / radix;
        long result = 0L;
        int offset = start;
        while (offset < end) {
            final int digit = Character.digit((char)(value[offset++] & 0xFF), radix);
            if (digit == -1) {
                throw new NumberFormatException(this.subSequence(start, end).toString());
            }
            if (max > result) {
                throw new NumberFormatException(this.subSequence(start, end).toString());
            }
            final long next = result * radix - digit;
            if (next > result) {
                throw new NumberFormatException(this.subSequence(start, end).toString());
            }
            result = next;
        }
        if (!negative) {
            result = -result;
            if (result < 0L) {
                throw new NumberFormatException(this.subSequence(start, end).toString());
            }
        }
        return result;
    }
    
    public short parseShort() {
        return this.parseShort(0, this.length(), 10);
    }
    
    public short parseShort(final int radix) {
        return this.parseShort(0, this.length(), radix);
    }
    
    public short parseShort(final int start, final int end) {
        return this.parseShort(start, end, 10);
    }
    
    public short parseShort(final int start, final int end, final int radix) {
        final int intValue = this.parseInt(start, end, radix);
        final short result = (short)intValue;
        if (result != intValue) {
            throw new NumberFormatException(this.subSequence(start, end).toString());
        }
        return result;
    }
    
    public float parseFloat() {
        return this.parseFloat(0, this.length());
    }
    
    public float parseFloat(final int start, final int end) {
        return Float.parseFloat(this.toString(start, end));
    }
    
    public double parseDouble() {
        return this.parseDouble(0, this.length());
    }
    
    public double parseDouble(final int start, final int end) {
        return Double.parseDouble(this.toString(start, end));
    }
    
    static {
        EMPTY_STRING = new AsciiString("");
        CASE_INSENSITIVE_ORDER = new Comparator<AsciiString>() {
            @Override
            public int compare(final AsciiString o1, final AsciiString o2) {
                return AsciiString.CHARSEQUENCE_CASE_INSENSITIVE_ORDER.compare(o1, o2);
            }
        };
        CASE_SENSITIVE_ORDER = new Comparator<AsciiString>() {
            @Override
            public int compare(final AsciiString o1, final AsciiString o2) {
                return AsciiString.CHARSEQUENCE_CASE_SENSITIVE_ORDER.compare(o1, o2);
            }
        };
        CHARSEQUENCE_CASE_INSENSITIVE_ORDER = new Comparator<CharSequence>() {
            @Override
            public int compare(final CharSequence o1, final CharSequence o2) {
                if (o1 == o2) {
                    return 0;
                }
                final AsciiString a1 = (o1 instanceof AsciiString) ? ((AsciiString)o1) : null;
                final AsciiString a2 = (o2 instanceof AsciiString) ? ((AsciiString)o2) : null;
                final int length1 = o1.length();
                final int length2 = o2.length();
                final int minLength = Math.min(length1, length2);
                if (a1 != null && a2 != null) {
                    final byte[] thisValue = a1.value;
                    final byte[] thatValue = a2.value;
                    for (int i = 0; i < minLength; ++i) {
                        final byte v1 = thisValue[i];
                        final byte v2 = thatValue[i];
                        if (v1 != v2) {
                            final int c1 = toLowerCase(v1) & 0xFF;
                            final int c2 = toLowerCase(v2) & 0xFF;
                            final int result = c1 - c2;
                            if (result != 0) {
                                return result;
                            }
                        }
                    }
                }
                else if (a1 != null) {
                    final byte[] thisValue = a1.value;
                    for (int j = 0; j < minLength; ++j) {
                        final int c3 = toLowerCase(thisValue[j]) & 0xFF;
                        final int c4 = toLowerCase(o2.charAt(j));
                        final int result = c3 - c4;
                        if (result != 0) {
                            return result;
                        }
                    }
                }
                else if (a2 != null) {
                    final byte[] thatValue2 = a2.value;
                    for (int j = 0; j < minLength; ++j) {
                        final int c3 = toLowerCase(o1.charAt(j));
                        final int c4 = toLowerCase(thatValue2[j]) & 0xFF;
                        final int result = c3 - c4;
                        if (result != 0) {
                            return result;
                        }
                    }
                }
                else {
                    for (int k = 0; k < minLength; ++k) {
                        final int c5 = toLowerCase(o1.charAt(k));
                        final int c6 = toLowerCase(o2.charAt(k));
                        final int result = c5 - c6;
                        if (result != 0) {
                            return result;
                        }
                    }
                }
                return length1 - length2;
            }
        };
        CHARSEQUENCE_CASE_SENSITIVE_ORDER = new Comparator<CharSequence>() {
            @Override
            public int compare(final CharSequence o1, final CharSequence o2) {
                if (o1 == o2) {
                    return 0;
                }
                final AsciiString a1 = (o1 instanceof AsciiString) ? ((AsciiString)o1) : null;
                final AsciiString a2 = (o2 instanceof AsciiString) ? ((AsciiString)o2) : null;
                final int length1 = o1.length();
                final int length2 = o2.length();
                final int minLength = Math.min(length1, length2);
                if (a1 != null && a2 != null) {
                    final byte[] thisValue = a1.value;
                    final byte[] thatValue = a2.value;
                    for (int i = 0; i < minLength; ++i) {
                        final byte v1 = thisValue[i];
                        final byte v2 = thatValue[i];
                        final int result = v1 - v2;
                        if (result != 0) {
                            return result;
                        }
                    }
                }
                else if (a1 != null) {
                    final byte[] thisValue = a1.value;
                    for (int j = 0; j < minLength; ++j) {
                        final int c1 = thisValue[j];
                        final int c2 = o2.charAt(j);
                        final int result = c1 - c2;
                        if (result != 0) {
                            return result;
                        }
                    }
                }
                else if (a2 != null) {
                    final byte[] thatValue2 = a2.value;
                    for (int j = 0; j < minLength; ++j) {
                        final int c1 = o1.charAt(j);
                        final int c2 = thatValue2[j];
                        final int result = c1 - c2;
                        if (result != 0) {
                            return result;
                        }
                    }
                }
                else {
                    for (int k = 0; k < minLength; ++k) {
                        final int c3 = o1.charAt(k);
                        final int c4 = o2.charAt(k);
                        final int result = c3 - c4;
                        if (result != 0) {
                            return result;
                        }
                    }
                }
                return length1 - length2;
            }
        };
    }
}
