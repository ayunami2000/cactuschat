// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

public class DNSOutput
{
    private byte[] array;
    private int pos;
    private int saved_pos;
    
    public DNSOutput(final int size) {
        this.array = new byte[size];
        this.pos = 0;
        this.saved_pos = -1;
    }
    
    public DNSOutput() {
        this(32);
    }
    
    public int current() {
        return this.pos;
    }
    
    private void check(final long val, final int bits) {
        long max = 1L;
        max <<= bits;
        if (val < 0L || val > max) {
            throw new IllegalArgumentException(val + " out of range for " + bits + " bit value");
        }
    }
    
    private void need(final int n) {
        if (this.array.length - this.pos >= n) {
            return;
        }
        int newsize = this.array.length * 2;
        if (newsize < this.pos + n) {
            newsize = this.pos + n;
        }
        final byte[] newarray = new byte[newsize];
        System.arraycopy(this.array, 0, newarray, 0, this.pos);
        this.array = newarray;
    }
    
    public void jump(final int index) {
        if (index > this.pos) {
            throw new IllegalArgumentException("cannot jump past end of data");
        }
        this.pos = index;
    }
    
    public void save() {
        this.saved_pos = this.pos;
    }
    
    public void restore() {
        if (this.saved_pos < 0) {
            throw new IllegalStateException("no previous state");
        }
        this.pos = this.saved_pos;
        this.saved_pos = -1;
    }
    
    public void writeU8(final int val) {
        this.check(val, 8);
        this.need(1);
        this.array[this.pos++] = (byte)(val & 0xFF);
    }
    
    public void writeU16(final int val) {
        this.check(val, 16);
        this.need(2);
        this.array[this.pos++] = (byte)(val >>> 8 & 0xFF);
        this.array[this.pos++] = (byte)(val & 0xFF);
    }
    
    public void writeU32(final long val) {
        this.check(val, 32);
        this.need(4);
        this.array[this.pos++] = (byte)(val >>> 24 & 0xFFL);
        this.array[this.pos++] = (byte)(val >>> 16 & 0xFFL);
        this.array[this.pos++] = (byte)(val >>> 8 & 0xFFL);
        this.array[this.pos++] = (byte)(val & 0xFFL);
    }
    
    public void writeByteArray(final byte[] b, final int off, final int len) {
        this.need(len);
        System.arraycopy(b, off, this.array, this.pos, len);
        this.pos += len;
    }
    
    public void writeByteArray(final byte[] b) {
        this.writeByteArray(b, 0, b.length);
    }
    
    public void writeCountedString(final byte[] s) {
        if (s.length > 255) {
            throw new IllegalArgumentException("Invalid counted string");
        }
        this.need(1 + s.length);
        this.array[this.pos++] = (byte)(s.length & 0xFF);
        this.writeByteArray(s, 0, s.length);
    }
    
    public byte[] toByteArray() {
        final byte[] out = new byte[this.pos];
        System.arraycopy(this.array, 0, out, 0, this.pos);
        return out;
    }
}
