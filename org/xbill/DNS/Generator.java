// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

public class Generator
{
    public long start;
    public long end;
    public long step;
    public final String namePattern;
    public final int type;
    public final int dclass;
    public final long ttl;
    public final String rdataPattern;
    public final Name origin;
    private long current;
    
    public static boolean supportedType(final int type) {
        Type.check(type);
        return type == 12 || type == 5 || type == 39 || type == 1 || type == 28 || type == 2;
    }
    
    public Generator(final long start, final long end, final long step, final String namePattern, final int type, final int dclass, final long ttl, final String rdataPattern, final Name origin) {
        if (start < 0L || end < 0L || start > end || step <= 0L) {
            throw new IllegalArgumentException("invalid range specification");
        }
        if (!supportedType(type)) {
            throw new IllegalArgumentException("unsupported type");
        }
        DClass.check(dclass);
        this.start = start;
        this.end = end;
        this.step = step;
        this.namePattern = namePattern;
        this.type = type;
        this.dclass = dclass;
        this.ttl = ttl;
        this.rdataPattern = rdataPattern;
        this.origin = origin;
        this.current = start;
    }
    
    private String substitute(final String spec, final long n) throws IOException {
        boolean escaped = false;
        final byte[] str = spec.getBytes();
        final StringBuffer sb = new StringBuffer();
        for (int i = 0; i < str.length; ++i) {
            char c = (char)(str[i] & 0xFF);
            if (escaped) {
                sb.append(c);
                escaped = false;
            }
            else if (c == '\\') {
                if (i + 1 == str.length) {
                    throw new TextParseException("invalid escape character");
                }
                escaped = true;
            }
            else if (c == '$') {
                boolean negative = false;
                long offset = 0L;
                long width = 0L;
                long base = 10L;
                boolean wantUpperCase = false;
                if (i + 1 < str.length && str[i + 1] == 36) {
                    c = (char)(str[++i] & 0xFF);
                    sb.append(c);
                }
                else {
                    if (i + 1 < str.length && str[i + 1] == 123) {
                        if (++i + 1 < str.length && str[i + 1] == 45) {
                            negative = true;
                            ++i;
                        }
                        while (i + 1 < str.length) {
                            c = (char)(str[++i] & 0xFF);
                            if (c == ',') {
                                break;
                            }
                            if (c == '}') {
                                break;
                            }
                            if (c < '0' || c > '9') {
                                throw new TextParseException("invalid offset");
                            }
                            c -= '0';
                            offset *= 10L;
                            offset += c;
                        }
                        if (negative) {
                            offset = -offset;
                        }
                        if (c == ',') {
                            while (i + 1 < str.length) {
                                c = (char)(str[++i] & 0xFF);
                                if (c == ',') {
                                    break;
                                }
                                if (c == '}') {
                                    break;
                                }
                                if (c < '0' || c > '9') {
                                    throw new TextParseException("invalid width");
                                }
                                c -= '0';
                                width *= 10L;
                                width += c;
                            }
                        }
                        if (c == ',') {
                            if (i + 1 == str.length) {
                                throw new TextParseException("invalid base");
                            }
                            c = (char)(str[++i] & 0xFF);
                            if (c == 'o') {
                                base = 8L;
                            }
                            else if (c == 'x') {
                                base = 16L;
                            }
                            else if (c == 'X') {
                                base = 16L;
                                wantUpperCase = true;
                            }
                            else if (c != 'd') {
                                throw new TextParseException("invalid base");
                            }
                        }
                        if (i + 1 == str.length || str[i + 1] != 125) {
                            throw new TextParseException("invalid modifiers");
                        }
                        ++i;
                    }
                    final long v = n + offset;
                    if (v < 0L) {
                        throw new TextParseException("invalid offset expansion");
                    }
                    String number;
                    if (base == 8L) {
                        number = Long.toOctalString(v);
                    }
                    else if (base == 16L) {
                        number = Long.toHexString(v);
                    }
                    else {
                        number = Long.toString(v);
                    }
                    if (wantUpperCase) {
                        number = number.toUpperCase();
                    }
                    if (width != 0L && width > number.length()) {
                        int zeros = (int)width - number.length();
                        while (zeros-- > 0) {
                            sb.append('0');
                        }
                    }
                    sb.append(number);
                }
            }
            else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
    
    public Record nextRecord() throws IOException {
        if (this.current > this.end) {
            return null;
        }
        final String namestr = this.substitute(this.namePattern, this.current);
        final Name name = Name.fromString(namestr, this.origin);
        final String rdata = this.substitute(this.rdataPattern, this.current);
        this.current += this.step;
        return Record.fromString(name, this.type, this.dclass, this.ttl, rdata, this.origin);
    }
    
    public Record[] expand() throws IOException {
        final List list = new ArrayList();
        for (long i = this.start; i < this.end; i += this.step) {
            final String namestr = this.substitute(this.namePattern, this.current);
            final Name name = Name.fromString(namestr, this.origin);
            final String rdata = this.substitute(this.rdataPattern, this.current);
            list.add(Record.fromString(name, this.type, this.dclass, this.ttl, rdata, this.origin));
        }
        return list.toArray(new Record[list.size()]);
    }
    
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append("$GENERATE ");
        sb.append(this.start + "-" + this.end);
        if (this.step > 1L) {
            sb.append("/" + this.step);
        }
        sb.append(" ");
        sb.append(this.namePattern + " ");
        sb.append(this.ttl + " ");
        if (this.dclass != 1 || !Options.check("noPrintIN")) {
            sb.append(DClass.string(this.dclass) + " ");
        }
        sb.append(Type.string(this.type) + " ");
        sb.append(this.rdataPattern + " ");
        return sb.toString();
    }
}
