// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.io.IOException;
import java.text.DecimalFormat;
import java.io.Serializable;

public class Name implements Comparable, Serializable
{
    private static final long serialVersionUID = -7257019940971525644L;
    private static final int LABEL_NORMAL = 0;
    private static final int LABEL_COMPRESSION = 192;
    private static final int LABEL_MASK = 192;
    private byte[] name;
    private long offsets;
    private int hashcode;
    private static final byte[] emptyLabel;
    private static final byte[] wildLabel;
    public static final Name root;
    public static final Name empty;
    private static final int MAXNAME = 255;
    private static final int MAXLABEL = 63;
    private static final int MAXLABELS = 128;
    private static final int MAXOFFSETS = 7;
    private static final DecimalFormat byteFormat;
    private static final byte[] lowercase;
    private static final Name wild;
    
    private Name() {
    }
    
    private final void setoffset(final int n, final int offset) {
        if (n >= 7) {
            return;
        }
        final int shift = 8 * (7 - n);
        this.offsets &= ~(255L << shift);
        this.offsets |= (long)offset << shift;
    }
    
    private final int offset(final int n) {
        if (n == 0 && this.getlabels() == 0) {
            return 0;
        }
        if (n < 0 || n >= this.getlabels()) {
            throw new IllegalArgumentException("label out of range");
        }
        if (n < 7) {
            final int shift = 8 * (7 - n);
            return (int)(this.offsets >>> shift) & 0xFF;
        }
        int pos = this.offset(6);
        for (int i = 6; i < n; ++i) {
            pos += this.name[pos] + 1;
        }
        return pos;
    }
    
    private final void setlabels(final int labels) {
        this.offsets &= 0xFFFFFFFFFFFFFF00L;
        this.offsets |= labels;
    }
    
    private final int getlabels() {
        return (int)(this.offsets & 0xFFL);
    }
    
    private static final void copy(final Name src, final Name dst) {
        if (src.offset(0) == 0) {
            dst.name = src.name;
            dst.offsets = src.offsets;
        }
        else {
            final int offset0 = src.offset(0);
            final int namelen = src.name.length - offset0;
            final int labels = src.labels();
            dst.name = new byte[namelen];
            System.arraycopy(src.name, offset0, dst.name, 0, namelen);
            for (int i = 0; i < labels && i < 7; ++i) {
                dst.setoffset(i, src.offset(i) - offset0);
            }
            dst.setlabels(labels);
        }
    }
    
    private final void append(final byte[] array, final int start, final int n) throws NameTooLongException {
        final int length = (this.name == null) ? 0 : (this.name.length - this.offset(0));
        int alength = 0;
        int i = 0;
        int pos = start;
        while (i < n) {
            int len = array[pos];
            if (len > 63) {
                throw new IllegalStateException("invalid label");
            }
            ++len;
            pos += len;
            alength += len;
            ++i;
        }
        final int newlength = length + alength;
        if (newlength > 255) {
            throw new NameTooLongException();
        }
        final int labels = this.getlabels();
        final int newlabels = labels + n;
        if (newlabels > 128) {
            throw new IllegalStateException("too many labels");
        }
        final byte[] newname = new byte[newlength];
        if (length != 0) {
            System.arraycopy(this.name, this.offset(0), newname, 0, length);
        }
        System.arraycopy(array, start, newname, length, alength);
        this.name = newname;
        int j = 0;
        int pos2 = length;
        while (j < n) {
            this.setoffset(labels + j, pos2);
            pos2 += newname[pos2] + 1;
            ++j;
        }
        this.setlabels(newlabels);
    }
    
    private static TextParseException parseException(final String str, final String message) {
        return new TextParseException("'" + str + "': " + message);
    }
    
    private final void appendFromString(final String fullName, final byte[] array, final int start, final int n) throws TextParseException {
        try {
            this.append(array, start, n);
        }
        catch (NameTooLongException e) {
            throw parseException(fullName, "Name too long");
        }
    }
    
    private final void appendSafe(final byte[] array, final int start, final int n) {
        try {
            this.append(array, start, n);
        }
        catch (NameTooLongException ex) {}
    }
    
    public Name(final String s, final Name origin) throws TextParseException {
        if (s.equals("")) {
            throw parseException(s, "empty name");
        }
        if (s.equals("@")) {
            if (origin == null) {
                copy(Name.empty, this);
            }
            else {
                copy(origin, this);
            }
            return;
        }
        if (s.equals(".")) {
            copy(Name.root, this);
            return;
        }
        int labelstart = -1;
        int pos = 1;
        final byte[] label = new byte[64];
        boolean escaped = false;
        int digits = 0;
        int intval = 0;
        boolean absolute = false;
        for (int i = 0; i < s.length(); ++i) {
            byte b = (byte)s.charAt(i);
            if (escaped) {
                if (b >= 48 && b <= 57 && digits < 3) {
                    ++digits;
                    intval *= 10;
                    intval += b - 48;
                    if (intval > 255) {
                        throw parseException(s, "bad escape");
                    }
                    if (digits < 3) {
                        continue;
                    }
                    b = (byte)intval;
                }
                else if (digits > 0 && digits < 3) {
                    throw parseException(s, "bad escape");
                }
                if (pos > 63) {
                    throw parseException(s, "label too long");
                }
                labelstart = pos;
                label[pos++] = b;
                escaped = false;
            }
            else if (b == 92) {
                escaped = true;
                digits = 0;
                intval = 0;
            }
            else if (b == 46) {
                if (labelstart == -1) {
                    throw parseException(s, "invalid empty label");
                }
                label[0] = (byte)(pos - 1);
                this.appendFromString(s, label, 0, 1);
                labelstart = -1;
                pos = 1;
            }
            else {
                if (labelstart == -1) {
                    labelstart = i;
                }
                if (pos > 63) {
                    throw parseException(s, "label too long");
                }
                label[pos++] = b;
            }
        }
        if (digits > 0 && digits < 3) {
            throw parseException(s, "bad escape");
        }
        if (escaped) {
            throw parseException(s, "bad escape");
        }
        if (labelstart == -1) {
            this.appendFromString(s, Name.emptyLabel, 0, 1);
            absolute = true;
        }
        else {
            label[0] = (byte)(pos - 1);
            this.appendFromString(s, label, 0, 1);
        }
        if (origin != null && !absolute) {
            this.appendFromString(s, origin.name, 0, origin.getlabels());
        }
    }
    
    public Name(final String s) throws TextParseException {
        this(s, null);
    }
    
    public static Name fromString(final String s, final Name origin) throws TextParseException {
        if (s.equals("@") && origin != null) {
            return origin;
        }
        if (s.equals(".")) {
            return Name.root;
        }
        return new Name(s, origin);
    }
    
    public static Name fromString(final String s) throws TextParseException {
        return fromString(s, null);
    }
    
    public static Name fromConstantString(final String s) {
        try {
            return fromString(s, null);
        }
        catch (TextParseException e) {
            throw new IllegalArgumentException("Invalid name '" + s + "'");
        }
    }
    
    public Name(final DNSInput in) throws WireParseException {
        boolean done = false;
        final byte[] label = new byte[64];
        boolean savedState = false;
        while (!done) {
            final int len = in.readU8();
            switch (len & 0xC0) {
                case 0: {
                    if (this.getlabels() >= 128) {
                        throw new WireParseException("too many labels");
                    }
                    if (len == 0) {
                        this.append(Name.emptyLabel, 0, 1);
                        done = true;
                        continue;
                    }
                    label[0] = (byte)len;
                    in.readByteArray(label, 1, len);
                    this.append(label, 0, 1);
                    continue;
                }
                case 192: {
                    int pos = in.readU8();
                    pos += (len & 0xFFFFFF3F) << 8;
                    if (Options.check("verbosecompression")) {
                        System.err.println("currently " + in.current() + ", pointer to " + pos);
                    }
                    if (pos >= in.current() - 2) {
                        throw new WireParseException("bad compression");
                    }
                    if (!savedState) {
                        in.save();
                        savedState = true;
                    }
                    in.jump(pos);
                    if (Options.check("verbosecompression")) {
                        System.err.println("current name '" + this + "', seeking to " + pos);
                        continue;
                    }
                    continue;
                }
                default: {
                    throw new WireParseException("bad label type");
                }
            }
        }
        if (savedState) {
            in.restore();
        }
    }
    
    public Name(final byte[] b) throws IOException {
        this(new DNSInput(b));
    }
    
    public Name(final Name src, final int n) {
        final int slabels = src.labels();
        if (n > slabels) {
            throw new IllegalArgumentException("attempted to remove too many labels");
        }
        this.name = src.name;
        this.setlabels(slabels - n);
        for (int i = 0; i < 7 && i < slabels - n; ++i) {
            this.setoffset(i, src.offset(i + n));
        }
    }
    
    public static Name concatenate(final Name prefix, final Name suffix) throws NameTooLongException {
        if (prefix.isAbsolute()) {
            return prefix;
        }
        final Name newname = new Name();
        copy(prefix, newname);
        newname.append(suffix.name, suffix.offset(0), suffix.getlabels());
        return newname;
    }
    
    public Name relativize(final Name origin) {
        if (origin == null || !this.subdomain(origin)) {
            return this;
        }
        final Name newname = new Name();
        copy(this, newname);
        final int length = this.length() - origin.length();
        final int labels = newname.labels() - origin.labels();
        newname.setlabels(labels);
        newname.name = new byte[length];
        System.arraycopy(this.name, this.offset(0), newname.name, 0, length);
        return newname;
    }
    
    public Name wild(final int n) {
        if (n < 1) {
            throw new IllegalArgumentException("must replace 1 or more labels");
        }
        try {
            final Name newname = new Name();
            copy(Name.wild, newname);
            newname.append(this.name, this.offset(n), this.getlabels() - n);
            return newname;
        }
        catch (NameTooLongException e) {
            throw new IllegalStateException("Name.wild: concatenate failed");
        }
    }
    
    public Name fromDNAME(final DNAMERecord dname) throws NameTooLongException {
        final Name dnameowner = dname.getName();
        final Name dnametarget = dname.getTarget();
        if (!this.subdomain(dnameowner)) {
            return null;
        }
        final int plabels = this.labels() - dnameowner.labels();
        final int plength = this.length() - dnameowner.length();
        final int pstart = this.offset(0);
        final int dlabels = dnametarget.labels();
        final int dlength = dnametarget.length();
        if (plength + dlength > 255) {
            throw new NameTooLongException();
        }
        final Name newname = new Name();
        newname.setlabels(plabels + dlabels);
        newname.name = new byte[plength + dlength];
        System.arraycopy(this.name, pstart, newname.name, 0, plength);
        System.arraycopy(dnametarget.name, 0, newname.name, plength, dlength);
        int i = 0;
        int pos = 0;
        while (i < 7 && i < plabels + dlabels) {
            newname.setoffset(i, pos);
            pos += newname.name[pos] + 1;
            ++i;
        }
        return newname;
    }
    
    public boolean isWild() {
        return this.labels() != 0 && this.name[0] == 1 && this.name[1] == 42;
    }
    
    public boolean isAbsolute() {
        return this.labels() != 0 && this.name[this.name.length - 1] == 0;
    }
    
    public short length() {
        if (this.getlabels() == 0) {
            return 0;
        }
        return (short)(this.name.length - this.offset(0));
    }
    
    public int labels() {
        return this.getlabels();
    }
    
    public boolean subdomain(final Name domain) {
        final int labels = this.labels();
        final int dlabels = domain.labels();
        if (dlabels > labels) {
            return false;
        }
        if (dlabels == labels) {
            return this.equals(domain);
        }
        return domain.equals(this.name, this.offset(labels - dlabels));
    }
    
    private String byteString(final byte[] array, int pos) {
        final StringBuffer sb = new StringBuffer();
        for (int len = array[pos++], i = pos; i < pos + len; ++i) {
            final int b = array[i] & 0xFF;
            if (b <= 32 || b >= 127) {
                sb.append('\\');
                sb.append(Name.byteFormat.format(b));
            }
            else if (b == 34 || b == 40 || b == 41 || b == 46 || b == 59 || b == 92 || b == 64 || b == 36) {
                sb.append('\\');
                sb.append((char)b);
            }
            else {
                sb.append((char)b);
            }
        }
        return sb.toString();
    }
    
    public String toString() {
        final int labels = this.labels();
        if (labels == 0) {
            return "@";
        }
        if (labels == 1 && this.name[this.offset(0)] == 0) {
            return ".";
        }
        final StringBuffer sb = new StringBuffer();
        int i = 0;
        int pos = this.offset(0);
        while (i < labels) {
            final int len = this.name[pos];
            if (len > 63) {
                throw new IllegalStateException("invalid label");
            }
            if (len == 0) {
                break;
            }
            sb.append(this.byteString(this.name, pos));
            sb.append('.');
            pos += 1 + len;
            ++i;
        }
        if (!this.isAbsolute()) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }
    
    public byte[] getLabel(final int n) {
        final int pos = this.offset(n);
        final byte len = (byte)(this.name[pos] + 1);
        final byte[] label = new byte[len];
        System.arraycopy(this.name, pos, label, 0, len);
        return label;
    }
    
    public String getLabelString(final int n) {
        final int pos = this.offset(n);
        return this.byteString(this.name, pos);
    }
    
    public void toWire(final DNSOutput out, final Compression c) {
        if (!this.isAbsolute()) {
            throw new IllegalArgumentException("toWire() called on non-absolute name");
        }
        for (int labels = this.labels(), i = 0; i < labels - 1; ++i) {
            Name tname;
            if (i == 0) {
                tname = this;
            }
            else {
                tname = new Name(this, i);
            }
            int pos = -1;
            if (c != null) {
                pos = c.get(tname);
            }
            if (pos >= 0) {
                pos |= 0xC000;
                out.writeU16(pos);
                return;
            }
            if (c != null) {
                c.add(out.current(), tname);
            }
            final int off = this.offset(i);
            out.writeByteArray(this.name, off, this.name[off] + 1);
        }
        out.writeU8(0);
    }
    
    public byte[] toWire() {
        final DNSOutput out = new DNSOutput();
        this.toWire(out, null);
        return out.toByteArray();
    }
    
    public void toWireCanonical(final DNSOutput out) {
        final byte[] b = this.toWireCanonical();
        out.writeByteArray(b);
    }
    
    public byte[] toWireCanonical() {
        final int labels = this.labels();
        if (labels == 0) {
            return new byte[0];
        }
        final byte[] b = new byte[this.name.length - this.offset(0)];
        int i = 0;
        int spos = this.offset(0);
        int dpos = 0;
        while (i < labels) {
            final int len = this.name[spos];
            if (len > 63) {
                throw new IllegalStateException("invalid label");
            }
            b[dpos++] = this.name[spos++];
            for (int j = 0; j < len; ++j) {
                b[dpos++] = Name.lowercase[this.name[spos++] & 0xFF];
            }
            ++i;
        }
        return b;
    }
    
    public void toWire(final DNSOutput out, final Compression c, final boolean canonical) {
        if (canonical) {
            this.toWireCanonical(out);
        }
        else {
            this.toWire(out, c);
        }
    }
    
    private final boolean equals(final byte[] b, int bpos) {
        final int labels = this.labels();
        int i = 0;
        int pos = this.offset(0);
        while (i < labels) {
            if (this.name[pos] != b[bpos]) {
                return false;
            }
            final int len = this.name[pos++];
            ++bpos;
            if (len > 63) {
                throw new IllegalStateException("invalid label");
            }
            for (int j = 0; j < len; ++j) {
                if (Name.lowercase[this.name[pos++] & 0xFF] != Name.lowercase[b[bpos++] & 0xFF]) {
                    return false;
                }
            }
            ++i;
        }
        return true;
    }
    
    public boolean equals(final Object arg) {
        if (arg == this) {
            return true;
        }
        if (arg == null || !(arg instanceof Name)) {
            return false;
        }
        final Name d = (Name)arg;
        if (d.hashcode == 0) {
            d.hashCode();
        }
        if (this.hashcode == 0) {
            this.hashCode();
        }
        return d.hashcode == this.hashcode && d.labels() == this.labels() && this.equals(d.name, d.offset(0));
    }
    
    public int hashCode() {
        if (this.hashcode != 0) {
            return this.hashcode;
        }
        int code = 0;
        for (int i = this.offset(0); i < this.name.length; ++i) {
            code += (code << 3) + Name.lowercase[this.name[i] & 0xFF];
        }
        return this.hashcode = code;
    }
    
    public int compareTo(final Object o) {
        final Name arg = (Name)o;
        if (this == arg) {
            return 0;
        }
        final int labels = this.labels();
        final int alabels = arg.labels();
        for (int compares = (labels > alabels) ? alabels : labels, i = 1; i <= compares; ++i) {
            final int start = this.offset(labels - i);
            final int astart = arg.offset(alabels - i);
            final int length = this.name[start];
            final int alength = arg.name[astart];
            for (int j = 0; j < length && j < alength; ++j) {
                final int n = Name.lowercase[this.name[j + start + 1] & 0xFF] - Name.lowercase[arg.name[j + astart + 1] & 0xFF];
                if (n != 0) {
                    return n;
                }
            }
            if (length != alength) {
                return length - alength;
            }
        }
        return labels - alabels;
    }
    
    static {
        emptyLabel = new byte[] { 0 };
        wildLabel = new byte[] { 1, 42 };
        byteFormat = new DecimalFormat();
        lowercase = new byte[256];
        Name.byteFormat.setMinimumIntegerDigits(3);
        for (int i = 0; i < Name.lowercase.length; ++i) {
            if (i < 65 || i > 90) {
                Name.lowercase[i] = (byte)i;
            }
            else {
                Name.lowercase[i] = (byte)(i - 65 + 97);
            }
        }
        (root = new Name()).appendSafe(Name.emptyLabel, 0, 1);
        empty = new Name();
        Name.empty.name = new byte[0];
        (wild = new Name()).appendSafe(Name.wildLabel, 0, 1);
    }
}
