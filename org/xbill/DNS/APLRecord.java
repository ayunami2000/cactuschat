// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import org.xbill.DNS.utils.base16;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

public class APLRecord extends Record
{
    private static final long serialVersionUID = -1348173791712935864L;
    private List elements;
    
    APLRecord() {
    }
    
    Record getObject() {
        return new APLRecord();
    }
    
    private static boolean validatePrefixLength(final int family, final int prefixLength) {
        return prefixLength >= 0 && prefixLength < 256 && (family != 1 || prefixLength <= 32) && (family != 2 || prefixLength <= 128);
    }
    
    public APLRecord(final Name name, final int dclass, final long ttl, final List elements) {
        super(name, 42, dclass, ttl);
        this.elements = new ArrayList(elements.size());
        for (final Object o : elements) {
            if (!(o instanceof Element)) {
                throw new IllegalArgumentException("illegal element");
            }
            final Element element = (Element)o;
            if (element.family != 1 && element.family != 2) {
                throw new IllegalArgumentException("unknown family");
            }
            this.elements.add(element);
        }
    }
    
    private static byte[] parseAddress(final byte[] in, final int length) throws WireParseException {
        if (in.length > length) {
            throw new WireParseException("invalid address length");
        }
        if (in.length == length) {
            return in;
        }
        final byte[] out = new byte[length];
        System.arraycopy(in, 0, out, 0, in.length);
        return out;
    }
    
    void rrFromWire(final DNSInput in) throws IOException {
        this.elements = new ArrayList(1);
        while (in.remaining() != 0) {
            final int family = in.readU16();
            final int prefix = in.readU8();
            int length = in.readU8();
            final boolean negative = (length & 0x80) != 0x0;
            length &= 0xFFFFFF7F;
            byte[] data = in.readByteArray(length);
            if (!validatePrefixLength(family, prefix)) {
                throw new WireParseException("invalid prefix length");
            }
            Element element;
            if (family == 1 || family == 2) {
                data = parseAddress(data, Address.addressLength(family));
                final InetAddress addr = InetAddress.getByAddress(data);
                element = new Element(negative, addr, prefix);
            }
            else {
                element = new Element(family, negative, (Object)data, prefix);
            }
            this.elements.add(element);
        }
    }
    
    void rdataFromString(final Tokenizer st, final Name origin) throws IOException {
        this.elements = new ArrayList(1);
        while (true) {
            final Tokenizer.Token t = st.get();
            if (!t.isString()) {
                st.unget();
                return;
            }
            boolean negative = false;
            int family = 0;
            int prefix = 0;
            final String s = t.value;
            int start = 0;
            if (s.startsWith("!")) {
                negative = true;
                start = 1;
            }
            final int colon = s.indexOf(58, start);
            if (colon < 0) {
                throw st.exception("invalid address prefix element");
            }
            final int slash = s.indexOf(47, colon);
            if (slash < 0) {
                throw st.exception("invalid address prefix element");
            }
            final String familyString = s.substring(start, colon);
            final String addressString = s.substring(colon + 1, slash);
            final String prefixString = s.substring(slash + 1);
            try {
                family = Integer.parseInt(familyString);
            }
            catch (NumberFormatException e) {
                throw st.exception("invalid family");
            }
            if (family != 1 && family != 2) {
                throw st.exception("unknown family");
            }
            try {
                prefix = Integer.parseInt(prefixString);
            }
            catch (NumberFormatException e) {
                throw st.exception("invalid prefix length");
            }
            if (!validatePrefixLength(family, prefix)) {
                throw st.exception("invalid prefix length");
            }
            final byte[] bytes = Address.toByteArray(addressString, family);
            if (bytes == null) {
                throw st.exception("invalid IP address " + addressString);
            }
            final InetAddress address = InetAddress.getByAddress(bytes);
            this.elements.add(new Element(negative, address, prefix));
        }
    }
    
    String rrToString() {
        final StringBuffer sb = new StringBuffer();
        final Iterator it = this.elements.iterator();
        while (it.hasNext()) {
            final Element element = it.next();
            sb.append(element);
            if (it.hasNext()) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }
    
    public List getElements() {
        return this.elements;
    }
    
    private static int addressLength(final byte[] addr) {
        for (int i = addr.length - 1; i >= 0; --i) {
            if (addr[i] != 0) {
                return i + 1;
            }
        }
        return 0;
    }
    
    void rrToWire(final DNSOutput out, final Compression c, final boolean canonical) {
        for (final Element element : this.elements) {
            int length = 0;
            byte[] data;
            if (element.family == 1 || element.family == 2) {
                final InetAddress addr = (InetAddress)element.address;
                data = addr.getAddress();
                length = addressLength(data);
            }
            else {
                data = (byte[])element.address;
                length = data.length;
            }
            int wlength = length;
            if (element.negative) {
                wlength |= 0x80;
            }
            out.writeU16(element.family);
            out.writeU8(element.prefixLength);
            out.writeU8(wlength);
            out.writeByteArray(data, 0, length);
        }
    }
    
    public static class Element
    {
        public final int family;
        public final boolean negative;
        public final int prefixLength;
        public final Object address;
        
        private Element(final int family, final boolean negative, final Object address, final int prefixLength) {
            this.family = family;
            this.negative = negative;
            this.address = address;
            this.prefixLength = prefixLength;
            if (!validatePrefixLength(family, prefixLength)) {
                throw new IllegalArgumentException("invalid prefix length");
            }
        }
        
        public Element(final boolean negative, final InetAddress address, final int prefixLength) {
            this(Address.familyOf(address), negative, address, prefixLength);
        }
        
        public String toString() {
            final StringBuffer sb = new StringBuffer();
            if (this.negative) {
                sb.append("!");
            }
            sb.append(this.family);
            sb.append(":");
            if (this.family == 1 || this.family == 2) {
                sb.append(((InetAddress)this.address).getHostAddress());
            }
            else {
                sb.append(base16.toString((byte[])this.address));
            }
            sb.append("/");
            sb.append(this.prefixLength);
            return sb.toString();
        }
        
        public boolean equals(final Object arg) {
            if (arg == null || !(arg instanceof Element)) {
                return false;
            }
            final Element elt = (Element)arg;
            return this.family == elt.family && this.negative == elt.negative && this.prefixLength == elt.prefixLength && this.address.equals(elt.address);
        }
        
        public int hashCode() {
            return this.address.hashCode() + this.prefixLength + (this.negative ? 1 : 0);
        }
    }
}
