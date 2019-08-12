// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

import java.io.IOException;
import java.util.Random;

public class Header implements Cloneable
{
    private int id;
    private int flags;
    private int[] counts;
    private static Random random;
    public static final int LENGTH = 12;
    
    private void init() {
        this.counts = new int[4];
        this.flags = 0;
        this.id = -1;
    }
    
    public Header(final int id) {
        this.init();
        this.setID(id);
    }
    
    public Header() {
        this.init();
    }
    
    Header(final DNSInput in) throws IOException {
        this(in.readU16());
        this.flags = in.readU16();
        for (int i = 0; i < this.counts.length; ++i) {
            this.counts[i] = in.readU16();
        }
    }
    
    public Header(final byte[] b) throws IOException {
        this(new DNSInput(b));
    }
    
    void toWire(final DNSOutput out) {
        out.writeU16(this.getID());
        out.writeU16(this.flags);
        for (int i = 0; i < this.counts.length; ++i) {
            out.writeU16(this.counts[i]);
        }
    }
    
    public byte[] toWire() {
        final DNSOutput out = new DNSOutput();
        this.toWire(out);
        return out.toByteArray();
    }
    
    private static boolean validFlag(final int bit) {
        return bit >= 0 && bit <= 15 && Flags.isFlag(bit);
    }
    
    private static void checkFlag(final int bit) {
        if (!validFlag(bit)) {
            throw new IllegalArgumentException("invalid flag bit " + bit);
        }
    }
    
    public void setFlag(final int bit) {
        checkFlag(bit);
        this.flags |= 1 << 15 - bit;
    }
    
    public void unsetFlag(final int bit) {
        checkFlag(bit);
        this.flags &= ~(1 << 15 - bit);
    }
    
    public boolean getFlag(final int bit) {
        checkFlag(bit);
        return (this.flags & 1 << 15 - bit) != 0x0;
    }
    
    boolean[] getFlags() {
        final boolean[] array = new boolean[16];
        for (int i = 0; i < array.length; ++i) {
            if (validFlag(i)) {
                array[i] = this.getFlag(i);
            }
        }
        return array;
    }
    
    public int getID() {
        if (this.id >= 0) {
            return this.id;
        }
        synchronized (this) {
            if (this.id < 0) {
                this.id = Header.random.nextInt(65535);
            }
            return this.id;
        }
    }
    
    public void setID(final int id) {
        if (id < 0 || id > 65535) {
            throw new IllegalArgumentException("DNS message ID " + id + " is out of range");
        }
        this.id = id;
    }
    
    public void setRcode(final int value) {
        if (value < 0 || value > 15) {
            throw new IllegalArgumentException("DNS Rcode " + value + " is out of range");
        }
        this.flags &= 0xFFFFFFF0;
        this.flags |= value;
    }
    
    public int getRcode() {
        return this.flags & 0xF;
    }
    
    public void setOpcode(final int value) {
        if (value < 0 || value > 15) {
            throw new IllegalArgumentException("DNS Opcode " + value + "is out of range");
        }
        this.flags &= 0x87FF;
        this.flags |= value << 11;
    }
    
    public int getOpcode() {
        return this.flags >> 11 & 0xF;
    }
    
    void setCount(final int field, final int value) {
        if (value < 0 || value > 65535) {
            throw new IllegalArgumentException("DNS section count " + value + " is out of range");
        }
        this.counts[field] = value;
    }
    
    void incCount(final int field) {
        if (this.counts[field] == 65535) {
            throw new IllegalStateException("DNS section count cannot be incremented");
        }
        final int[] counts = this.counts;
        ++counts[field];
    }
    
    void decCount(final int field) {
        if (this.counts[field] == 0) {
            throw new IllegalStateException("DNS section count cannot be decremented");
        }
        final int[] counts = this.counts;
        --counts[field];
    }
    
    public int getCount(final int field) {
        return this.counts[field];
    }
    
    public String printFlags() {
        final StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 16; ++i) {
            if (validFlag(i) && this.getFlag(i)) {
                sb.append(Flags.string(i));
                sb.append(" ");
            }
        }
        return sb.toString();
    }
    
    String toStringWithRcode(final int newrcode) {
        final StringBuffer sb = new StringBuffer();
        sb.append(";; ->>HEADER<<- ");
        sb.append("opcode: " + Opcode.string(this.getOpcode()));
        sb.append(", status: " + Rcode.string(newrcode));
        sb.append(", id: " + this.getID());
        sb.append("\n");
        sb.append(";; flags: " + this.printFlags());
        sb.append("; ");
        for (int i = 0; i < 4; ++i) {
            sb.append(Section.string(i) + ": " + this.getCount(i) + " ");
        }
        return sb.toString();
    }
    
    public String toString() {
        return this.toStringWithRcode(this.getRcode());
    }
    
    public Object clone() {
        final Header h = new Header();
        h.id = this.id;
        h.flags = this.flags;
        System.arraycopy(this.counts, 0, h.counts, 0, this.counts.length);
        return h;
    }
    
    static {
        Header.random = new Random();
    }
}
