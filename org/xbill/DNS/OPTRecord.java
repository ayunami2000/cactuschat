// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import org.xbill.DNS.utils.base16;
import java.util.Collections;
import java.util.Iterator;
import java.io.IOException;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

public class OPTRecord extends Record
{
    private static final long serialVersionUID = -6254521894809367938L;
    private List options;
    
    OPTRecord() {
    }
    
    Record getObject() {
        return new OPTRecord();
    }
    
    public OPTRecord(final int payloadSize, final int xrcode, final int version, final int flags, final List options) {
        super(Name.root, 41, payloadSize, 0L);
        Record.checkU16("payloadSize", payloadSize);
        Record.checkU8("xrcode", xrcode);
        Record.checkU8("version", version);
        Record.checkU16("flags", flags);
        this.ttl = ((long)xrcode << 24) + ((long)version << 16) + flags;
        if (options != null) {
            this.options = new ArrayList(options);
        }
    }
    
    public OPTRecord(final int payloadSize, final int xrcode, final int version, final int flags) {
        this(payloadSize, xrcode, version, flags, null);
    }
    
    public OPTRecord(final int payloadSize, final int xrcode, final int version) {
        this(payloadSize, xrcode, version, 0, null);
    }
    
    void rrFromWire(final DNSInput in) throws IOException {
        if (in.remaining() > 0) {
            this.options = new ArrayList();
        }
        while (in.remaining() > 0) {
            final int code = in.readU16();
            final int len = in.readU16();
            final byte[] data = in.readByteArray(len);
            this.options.add(new Option(code, data));
        }
    }
    
    void rdataFromString(final Tokenizer st, final Name origin) throws IOException {
        throw st.exception("no text format defined for OPT");
    }
    
    String rrToString() {
        final StringBuffer sb = new StringBuffer();
        if (this.options != null) {
            sb.append(this.options);
            sb.append(" ");
        }
        sb.append(" ; payload ");
        sb.append(this.getPayloadSize());
        sb.append(", xrcode ");
        sb.append(this.getExtendedRcode());
        sb.append(", version ");
        sb.append(this.getVersion());
        sb.append(", flags ");
        sb.append(this.getFlags());
        return sb.toString();
    }
    
    public int getPayloadSize() {
        return this.dclass;
    }
    
    public int getExtendedRcode() {
        return (int)(this.ttl >>> 24);
    }
    
    public int getVersion() {
        return (int)(this.ttl >>> 16 & 0xFFL);
    }
    
    public int getFlags() {
        return (int)(this.ttl & 0xFFFFL);
    }
    
    void rrToWire(final DNSOutput out, final Compression c, final boolean canonical) {
        if (this.options == null) {
            return;
        }
        for (final Option opt : this.options) {
            out.writeU16(opt.code);
            out.writeU16(opt.data.length);
            out.writeByteArray(opt.data);
        }
    }
    
    public List getOptions() {
        if (this.options == null) {
            return Collections.EMPTY_LIST;
        }
        return Collections.unmodifiableList((List<?>)this.options);
    }
    
    public List getOptions(final int code) {
        if (this.options == null) {
            return Collections.EMPTY_LIST;
        }
        List list = null;
        for (final Option opt : this.options) {
            if (opt.code == code) {
                if (list == null) {
                    list = new ArrayList();
                }
                list.add(opt.data);
            }
        }
        if (list == null) {
            return Collections.EMPTY_LIST;
        }
        return list;
    }
    
    public static class Option
    {
        public final int code;
        public final byte[] data;
        
        public Option(final int code, final byte[] data) {
            this.code = Record.checkU8("option code", code);
            this.data = data;
        }
        
        public String toString() {
            return "{" + this.code + " <" + base16.toString(this.data) + ">}";
        }
    }
}
