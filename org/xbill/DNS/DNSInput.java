// 
// Decompiled by Procyon v0.5.36
// 

package org.xbill.DNS;

public class DNSInput
{
    private byte[] array;
    private int pos;
    private int end;
    private int saved_pos;
    private int saved_end;
    
    public DNSInput(final byte[] input) {
        this.array = input;
        this.pos = 0;
        this.end = this.array.length;
        this.saved_pos = -1;
        this.saved_end = -1;
    }
    
    public int current() {
        return this.pos;
    }
    
    public int remaining() {
        return this.end - this.pos;
    }
    
    private void require(final int n) throws WireParseException {
        if (n > this.remaining()) {
            throw new WireParseException("end of input");
        }
    }
    
    public void setActive(final int len) {
        if (len > this.array.length - this.pos) {
            throw new IllegalArgumentException("cannot set active region past end of input");
        }
        this.end = this.pos + len;
    }
    
    public void clearActive() {
        this.end = this.array.length;
    }
    
    public void jump(final int index) {
        if (index >= this.array.length) {
            throw new IllegalArgumentException("cannot jump past end of input");
        }
        this.pos = index;
        this.end = this.array.length;
    }
    
    public void save() {
        this.saved_pos = this.pos;
        this.saved_end = this.end;
    }
    
    public void restore() {
        if (this.saved_pos < 0) {
            throw new IllegalStateException("no previous state");
        }
        this.pos = this.saved_pos;
        this.end = this.saved_end;
        this.saved_pos = -1;
        this.saved_end = -1;
    }
    
    public int readU8() throws WireParseException {
        this.require(1);
        return this.array[this.pos++] & 0xFF;
    }
    
    public int readU16() throws WireParseException {
        this.require(2);
        final int b1 = this.array[this.pos++] & 0xFF;
        final int b2 = this.array[this.pos++] & 0xFF;
        return (b1 << 8) + b2;
    }
    
    public long readU32() throws WireParseException {
        this.require(4);
        final int b1 = this.array[this.pos++] & 0xFF;
        final int b2 = this.array[this.pos++] & 0xFF;
        final int b3 = this.array[this.pos++] & 0xFF;
        final int b4 = this.array[this.pos++] & 0xFF;
        return ((long)b1 << 24) + (b2 << 16) + (b3 << 8) + b4;
    }
    
    public void readByteArray(final byte[] b, final int off, final int len) throws WireParseException {
        this.require(len);
        System.arraycopy(this.array, this.pos, b, off, len);
        this.pos += len;
    }
    
    public byte[] readByteArray(final int len) throws WireParseException {
        this.require(len);
        final byte[] out = new byte[len];
        System.arraycopy(this.array, this.pos, out, 0, len);
        this.pos += len;
        return out;
    }
    
    public byte[] readByteArray() {
        final int len = this.remaining();
        final byte[] out = new byte[len];
        System.arraycopy(this.array, this.pos, out, 0, len);
        this.pos += len;
        return out;
    }
    
    public byte[] readCountedString() throws WireParseException {
        this.require(1);
        final int len = this.array[this.pos++] & 0xFF;
        return this.readByteArray(len);
    }
}
