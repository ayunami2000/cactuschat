// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.util.Arrays;
import org.xbill.DNS.utils.base16;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.io.Serializable;

public abstract class Record implements Cloneable, Comparable, Serializable
{
    private static final long serialVersionUID = 2694906050116005466L;
    protected Name name;
    protected int type;
    protected int dclass;
    protected long ttl;
    private static final DecimalFormat byteFormat;
    
    protected Record() {
    }
    
    Record(final Name name, final int type, final int dclass, final long ttl) {
        if (!name.isAbsolute()) {
            throw new RelativeNameException(name);
        }
        Type.check(type);
        DClass.check(dclass);
        TTL.check(ttl);
        this.name = name;
        this.type = type;
        this.dclass = dclass;
        this.ttl = ttl;
    }
    
    abstract Record getObject();
    
    private static final Record getEmptyRecord(final Name name, final int type, final int dclass, final long ttl, final boolean hasData) {
        Record rec;
        if (hasData) {
            final Record proto = Type.getProto(type);
            if (proto != null) {
                rec = proto.getObject();
            }
            else {
                rec = new UNKRecord();
            }
        }
        else {
            rec = new EmptyRecord();
        }
        rec.name = name;
        rec.type = type;
        rec.dclass = dclass;
        rec.ttl = ttl;
        return rec;
    }
    
    abstract void rrFromWire(final DNSInput p0) throws IOException;
    
    private static Record newRecord(final Name name, final int type, final int dclass, final long ttl, final int length, final DNSInput in) throws IOException {
        final Record rec = getEmptyRecord(name, type, dclass, ttl, in != null);
        if (in != null) {
            if (in.remaining() < length) {
                throw new WireParseException("truncated record");
            }
            in.setActive(length);
            rec.rrFromWire(in);
            if (in.remaining() > 0) {
                throw new WireParseException("invalid record length");
            }
            in.clearActive();
        }
        return rec;
    }
    
    public static Record newRecord(final Name name, final int type, final int dclass, final long ttl, final int length, final byte[] data) {
        if (!name.isAbsolute()) {
            throw new RelativeNameException(name);
        }
        Type.check(type);
        DClass.check(dclass);
        TTL.check(ttl);
        DNSInput in;
        if (data != null) {
            in = new DNSInput(data);
        }
        else {
            in = null;
        }
        try {
            return newRecord(name, type, dclass, ttl, length, in);
        }
        catch (IOException e) {
            return null;
        }
    }
    
    public static Record newRecord(final Name name, final int type, final int dclass, final long ttl, final byte[] data) {
        return newRecord(name, type, dclass, ttl, data.length, data);
    }
    
    public static Record newRecord(final Name name, final int type, final int dclass, final long ttl) {
        if (!name.isAbsolute()) {
            throw new RelativeNameException(name);
        }
        Type.check(type);
        DClass.check(dclass);
        TTL.check(ttl);
        return getEmptyRecord(name, type, dclass, ttl, false);
    }
    
    public static Record newRecord(final Name name, final int type, final int dclass) {
        return newRecord(name, type, dclass, 0L);
    }
    
    static Record fromWire(final DNSInput in, final int section, final boolean isUpdate) throws IOException {
        final Name name = new Name(in);
        final int type = in.readU16();
        final int dclass = in.readU16();
        if (section == 0) {
            return newRecord(name, type, dclass);
        }
        final long ttl = in.readU32();
        final int length = in.readU16();
        if (length == 0 && isUpdate) {
            return newRecord(name, type, dclass, ttl);
        }
        final Record rec = newRecord(name, type, dclass, ttl, length, in);
        return rec;
    }
    
    static Record fromWire(final DNSInput in, final int section) throws IOException {
        return fromWire(in, section, false);
    }
    
    public static Record fromWire(final byte[] b, final int section) throws IOException {
        return fromWire(new DNSInput(b), section, false);
    }
    
    void toWire(final DNSOutput out, final int section, final Compression c) {
        this.name.toWire(out, c);
        out.writeU16(this.type);
        out.writeU16(this.dclass);
        if (section == 0) {
            return;
        }
        out.writeU32(this.ttl);
        final int lengthPosition = out.current();
        out.writeU16(0);
        this.rrToWire(out, c, false);
        final int rrlength = out.current() - lengthPosition - 2;
        out.save();
        out.jump(lengthPosition);
        out.writeU16(rrlength);
        out.restore();
    }
    
    public byte[] toWire(final int section) {
        final DNSOutput out = new DNSOutput();
        this.toWire(out, section, null);
        return out.toByteArray();
    }
    
    private void toWireCanonical(final DNSOutput out, final boolean noTTL) {
        this.name.toWireCanonical(out);
        out.writeU16(this.type);
        out.writeU16(this.dclass);
        if (noTTL) {
            out.writeU32(0L);
        }
        else {
            out.writeU32(this.ttl);
        }
        final int lengthPosition = out.current();
        out.writeU16(0);
        this.rrToWire(out, null, true);
        final int rrlength = out.current() - lengthPosition - 2;
        out.save();
        out.jump(lengthPosition);
        out.writeU16(rrlength);
        out.restore();
    }
    
    private byte[] toWireCanonical(final boolean noTTL) {
        final DNSOutput out = new DNSOutput();
        this.toWireCanonical(out, noTTL);
        return out.toByteArray();
    }
    
    public byte[] toWireCanonical() {
        return this.toWireCanonical(false);
    }
    
    public byte[] rdataToWireCanonical() {
        final DNSOutput out = new DNSOutput();
        this.rrToWire(out, null, true);
        return out.toByteArray();
    }
    
    abstract String rrToString();
    
    public String rdataToString() {
        return this.rrToString();
    }
    
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(this.name);
        if (sb.length() < 8) {
            sb.append("\t");
        }
        if (sb.length() < 16) {
            sb.append("\t");
        }
        sb.append("\t");
        if (Options.check("BINDTTL")) {
            sb.append(TTL.format(this.ttl));
        }
        else {
            sb.append(this.ttl);
        }
        sb.append("\t");
        if (this.dclass != 1 || !Options.check("noPrintIN")) {
            sb.append(DClass.string(this.dclass));
            sb.append("\t");
        }
        sb.append(Type.string(this.type));
        final String rdata = this.rrToString();
        if (!rdata.equals("")) {
            sb.append("\t");
            sb.append(rdata);
        }
        return sb.toString();
    }
    
    abstract void rdataFromString(final Tokenizer p0, final Name p1) throws IOException;
    
    protected static byte[] byteArrayFromString(final String s) throws TextParseException {
        byte[] array = s.getBytes();
        boolean escaped = false;
        boolean hasEscapes = false;
        for (int i = 0; i < array.length; ++i) {
            if (array[i] == 92) {
                hasEscapes = true;
                break;
            }
        }
        if (!hasEscapes) {
            if (array.length > 255) {
                throw new TextParseException("text string too long");
            }
            return array;
        }
        else {
            final ByteArrayOutputStream os = new ByteArrayOutputStream();
            int digits = 0;
            int intval = 0;
            for (int j = 0; j < array.length; ++j) {
                byte b = array[j];
                if (escaped) {
                    if (b >= 48 && b <= 57 && digits < 3) {
                        ++digits;
                        intval *= 10;
                        intval += b - 48;
                        if (intval > 255) {
                            throw new TextParseException("bad escape");
                        }
                        if (digits < 3) {
                            continue;
                        }
                        b = (byte)intval;
                    }
                    else if (digits > 0 && digits < 3) {
                        throw new TextParseException("bad escape");
                    }
                    os.write(b);
                    escaped = false;
                }
                else if (array[j] == 92) {
                    escaped = true;
                    digits = 0;
                    intval = 0;
                }
                else {
                    os.write(array[j]);
                }
            }
            if (digits > 0 && digits < 3) {
                throw new TextParseException("bad escape");
            }
            array = os.toByteArray();
            if (array.length > 255) {
                throw new TextParseException("text string too long");
            }
            return os.toByteArray();
        }
    }
    
    protected static String byteArrayToString(final byte[] array, final boolean quote) {
        final StringBuffer sb = new StringBuffer();
        if (quote) {
            sb.append('\"');
        }
        for (int i = 0; i < array.length; ++i) {
            final int b = array[i] & 0xFF;
            if (b < 32 || b >= 127) {
                sb.append('\\');
                sb.append(Record.byteFormat.format(b));
            }
            else if (b == 34 || b == 92) {
                sb.append('\\');
                sb.append((char)b);
            }
            else {
                sb.append((char)b);
            }
        }
        if (quote) {
            sb.append('\"');
        }
        return sb.toString();
    }
    
    protected static String unknownToString(final byte[] data) {
        final StringBuffer sb = new StringBuffer();
        sb.append("\\# ");
        sb.append(data.length);
        sb.append(" ");
        sb.append(base16.toString(data));
        return sb.toString();
    }
    
    public static Record fromString(final Name name, final int type, final int dclass, final long ttl, final Tokenizer st, final Name origin) throws IOException {
        if (!name.isAbsolute()) {
            throw new RelativeNameException(name);
        }
        Type.check(type);
        DClass.check(dclass);
        TTL.check(ttl);
        Tokenizer.Token t = st.get();
        if (t.type == 3 && t.value.equals("\\#")) {
            final int length = st.getUInt16();
            byte[] data = st.getHex();
            if (data == null) {
                data = new byte[0];
            }
            if (length != data.length) {
                throw st.exception("invalid unknown RR encoding: length mismatch");
            }
            final DNSInput in = new DNSInput(data);
            return newRecord(name, type, dclass, ttl, length, in);
        }
        else {
            st.unget();
            final Record rec = getEmptyRecord(name, type, dclass, ttl, true);
            rec.rdataFromString(st, origin);
            t = st.get();
            if (t.type != 1 && t.type != 0) {
                throw st.exception("unexpected tokens at end of record");
            }
            return rec;
        }
    }
    
    public static Record fromString(final Name name, final int type, final int dclass, final long ttl, final String s, final Name origin) throws IOException {
        return fromString(name, type, dclass, ttl, new Tokenizer(s), origin);
    }
    
    public Name getName() {
        return this.name;
    }
    
    public int getType() {
        return this.type;
    }
    
    public int getRRsetType() {
        if (this.type == 46) {
            final RRSIGRecord sig = (RRSIGRecord)this;
            return sig.getTypeCovered();
        }
        return this.type;
    }
    
    public int getDClass() {
        return this.dclass;
    }
    
    public long getTTL() {
        return this.ttl;
    }
    
    abstract void rrToWire(final DNSOutput p0, final Compression p1, final boolean p2);
    
    public boolean sameRRset(final Record rec) {
        return this.getRRsetType() == rec.getRRsetType() && this.dclass == rec.dclass && this.name.equals(rec.name);
    }
    
    public boolean equals(final Object arg) {
        if (arg == null || !(arg instanceof Record)) {
            return false;
        }
        final Record r = (Record)arg;
        if (this.type != r.type || this.dclass != r.dclass || !this.name.equals(r.name)) {
            return false;
        }
        final byte[] array1 = this.rdataToWireCanonical();
        final byte[] array2 = r.rdataToWireCanonical();
        return Arrays.equals(array1, array2);
    }
    
    public int hashCode() {
        final byte[] array = this.toWireCanonical(true);
        int code = 0;
        for (int i = 0; i < array.length; ++i) {
            code += (code << 3) + (array[i] & 0xFF);
        }
        return code;
    }
    
    Record cloneRecord() {
        try {
            return (Record)this.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new IllegalStateException();
        }
    }
    
    public Record withName(final Name name) {
        if (!name.isAbsolute()) {
            throw new RelativeNameException(name);
        }
        final Record rec = this.cloneRecord();
        rec.name = name;
        return rec;
    }
    
    Record withDClass(final int dclass, final long ttl) {
        final Record rec = this.cloneRecord();
        rec.dclass = dclass;
        rec.ttl = ttl;
        return rec;
    }
    
    void setTTL(final long ttl) {
        this.ttl = ttl;
    }
    
    public int compareTo(final Object o) {
        final Record arg = (Record)o;
        if (this == arg) {
            return 0;
        }
        int n = this.name.compareTo(arg.name);
        if (n != 0) {
            return n;
        }
        n = this.dclass - arg.dclass;
        if (n != 0) {
            return n;
        }
        n = this.type - arg.type;
        if (n != 0) {
            return n;
        }
        final byte[] rdata1 = this.rdataToWireCanonical();
        final byte[] rdata2 = arg.rdataToWireCanonical();
        for (int i = 0; i < rdata1.length && i < rdata2.length; ++i) {
            n = (rdata1[i] & 0xFF) - (rdata2[i] & 0xFF);
            if (n != 0) {
                return n;
            }
        }
        return rdata1.length - rdata2.length;
    }
    
    public Name getAdditionalName() {
        return null;
    }
    
    static int checkU8(final String field, final int val) {
        if (val < 0 || val > 255) {
            throw new IllegalArgumentException("\"" + field + "\" " + val + " must be an unsigned 8 " + "bit value");
        }
        return val;
    }
    
    static int checkU16(final String field, final int val) {
        if (val < 0 || val > 65535) {
            throw new IllegalArgumentException("\"" + field + "\" " + val + " must be an unsigned 16 " + "bit value");
        }
        return val;
    }
    
    static long checkU32(final String field, final long val) {
        if (val < 0L || val > 4294967295L) {
            throw new IllegalArgumentException("\"" + field + "\" " + val + " must be an unsigned 32 " + "bit value");
        }
        return val;
    }
    
    static Name checkName(final String field, final Name name) {
        if (!name.isAbsolute()) {
            throw new RelativeNameException(name);
        }
        return name;
    }
    
    static {
        (byteFormat = new DecimalFormat()).setMinimumIntegerDigits(3);
    }
}
